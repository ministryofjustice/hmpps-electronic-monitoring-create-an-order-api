from contextlib import contextmanager
import json
import os
from pathlib import Path
import re
import subprocess
import unittest
import uuid


ROOT = Path(__file__).resolve().parents[2]
ENABLED = os.environ.get("UP3_CLEANUP_POSTGRES_TEST") == "true"


@unittest.skipUnless(ENABLED, "Set UP3_CLEANUP_POSTGRES_TEST=true for an isolated PostgreSQL test database")
class CleanupPostgresTest(unittest.TestCase):
    @classmethod
    def psql(cls, sql, database=None, apply=False, check=True):
        container = os.environ.get("UP3_CLEANUP_TEST_CONTAINER")
        command = ["docker", "exec", "-i", container, "psql"] if container else ["psql"]
        command += [
            "-X", "--no-password", "--quiet", "--tuples-only", "--no-align",
            "--username", os.environ.get("PGUSER", "postgres"),
            "--dbname", database or cls.database,
            "--set", "ON_ERROR_STOP=1", "--set", f"apply={'true' if apply else 'false'}",
        ]
        result = subprocess.run(command, input=sql, text=True, capture_output=True)
        if check and result.returncode:
            raise AssertionError(result.stderr)
        return result

    @classmethod
    def setUpClass(cls):
        cls.database = f"up3_cleanup_test_{uuid.uuid4().hex}"
        cls.psql(f'CREATE DATABASE "{cls.database}";', database="postgres")
        try:
            migrations = sorted(
                (ROOT / "src/main/resources/db/migration").glob("V*.sql"),
                key=lambda path: int(re.match(r"V(\d+)", path.name)[1]),
            )
            cls.psql("\n".join(path.read_text() for path in migrations))
            cls.psql("\n".join(
                path.read_text() for path in sorted((ROOT / "src/main/resources/db/up3-simulation").glob("V*.sql"))
            ))
            cls.tables = cls.psql(
                "SELECT table_name FROM information_schema.tables "
                "WHERE table_schema='public' AND table_type='BASE TABLE' ORDER BY table_name"
            ).stdout.splitlines()
            columns = json.loads(cls.psql(
                "SELECT json_agg(json_build_object("
                "'table', table_name, 'name', column_name, 'type', udt_name, "
                "'required', is_nullable='NO' AND column_default IS NULL)) "
                "FROM information_schema.columns WHERE table_schema='public'"
            ).stdout)
            cls.required_columns = {
                table: [column for column in columns if column["table"] == table and column["required"]]
                for table in cls.tables
            }
            cls.version_tables = sorted({
                column["table"] for column in columns if column["name"] == "version_id"
            } - {"address", "order_version", "up3_simulation"})
        except Exception:
            cls.psql(f'DROP DATABASE "{cls.database}";', database="postgres")
            raise

    @classmethod
    def tearDownClass(cls):
        cls.psql(f'DROP DATABASE "{cls.database}";', database="postgres")

    def setUp(self):
        self.psql("TRUNCATE " + ", ".join(self.tables) + " CASCADE;")

    def row(self, table, **values):
        data = {"id": str(uuid.uuid4()), **values}
        for column in self.required_columns[table]:
            if column["name"] in data:
                continue
            kind = column["type"]
            defaults = {
                "uuid": str(uuid.uuid4()), "varchar": "test", "text": "test",
                "int4": 1, "int8": 1, "bool": False, "date": "2026-01-01",
                "timestamp": "2026-01-01 12:00:00", "timestamptz": "2026-01-01 12:00:00+00",
                "jsonb": "{}", "json": "{}",
            }
            if kind not in defaults:
                raise AssertionError(f"Unhandled test column type: {table}.{column['name']} {kind}")
            data[column["name"]] = defaults[kind]
        if table in {
            "fms_order_details", "up3_simulation", "up3_simulation_creation", "up3_simulation_return_event",
        }:
            data.pop("id")
        literals = [
            "NULL" if value is None else "'" + str(value).replace("'", "''") + "'"
            for value in data.values()
        ]
        self.psql(f"INSERT INTO {table} ({','.join(data)}) VALUES ({','.join(literals)});")
        return data

    def seed(self, simulated, versions=1):
        order = self.row("orders")
        version_ids = []
        for number in range(versions):
            case = ("sim-" if simulated else "real-") + str(uuid.uuid4())
            wearer = self.row("fms_device_wearer_submission_result", device_wearer_id=case)
            monitoring = self.row(
                "fms_monitoring_order_submission_result",
                monitoring_order_id=("sim-mo-" if simulated else "real-mo-") + str(uuid.uuid4()),
            )
            submission = self.row(
                "fms_submission_result", order_id=order["id"],
                fms_device_wearer_result_id=wearer["id"], fms_monitoring_order_result_id=monitoring["id"],
            )
            self.row("fms_attachment_submission_result", fms_attachment_result_id=submission["id"])
            self.row("fms_order_details", case_id=case)
            version = self.row(
                "order_version", order_id=order["id"], version_id=number,
                fms_result_id=submission["id"], status="SUBMITTED",
            )
            version_ids.append(version["id"])
            self.row("address", version_id=version["id"])
            for table in self.version_tables:
                child = self.row(table, version_id=version["id"])
                if table == "status_update":
                    self.row("status_update_reason", status_update_id=child["id"])
        if simulated:
            self.row(
                "up3_simulation_creation", order_id=order["id"],
                created_order_version_id=version_ids[0], creation_request_id=str(uuid.uuid4()),
            )
            self.row(
                "up3_simulation", order_id=order["id"], version_id=version_ids[-1], case_id=case,
            )
            self.row(
                "up3_simulation_return_event", order_id=order["id"],
                target_order_version_id=version_ids[-1], simulated_submission_version_id=version_ids[-1],
                case_id=case, event_id=str(uuid.uuid4()), status="QUEUED",
            )
        return order, version_ids

    def snapshot(self, exclude=()):
        queries = [
            f"SELECT '{table}' AS name, coalesce(json_agg(r ORDER BY r::text), '[]'::json) AS data FROM "
            f"(SELECT to_jsonb(t) AS r FROM {table} t) q"
            for table in self.tables if table not in exclude
        ]
        return json.loads(self.psql(
            "SELECT json_object_agg(name, data) FROM (" + " UNION ALL ".join(queries) + ") snapshots"
        ).stdout)

    def cleanup(self, apply=True, check=True):
        return self.psql((ROOT / "scripts/up3-simulation-cleanup.sql").read_text(), apply=apply, check=check)

    def test_preview_changes_nothing(self):
        self.seed(False)
        self.seed(True, versions=2)
        before = self.snapshot()
        report = json.loads(self.cleanup(apply=False).stdout)
        self.assertEqual(report["orders"], 1)
        self.assertEqual(report["versions"], 2)
        self.assertEqual(self.snapshot(), before)

    def test_full_cleanup_preserves_ordinary_data_and_creation_tombstones(self):
        self.seed(False, versions=2)
        ordinary = self.snapshot(exclude={"up3_simulation_creation"})
        self.seed(True, versions=2)
        receipts = self.snapshot()["up3_simulation_creation"]
        self.cleanup()
        self.assertEqual(self.snapshot(exclude={"up3_simulation_creation"}), ordinary)
        self.assertEqual(self.snapshot()["up3_simulation_creation"], receipts)
        self.cleanup()
        self.assertEqual(self.snapshot(exclude={"up3_simulation_creation"}), ordinary)

    def test_unproven_membership_fails_without_deleting_anything(self):
        self.seed(False)
        self.seed(True)
        self.psql("DELETE FROM up3_simulation_creation;")
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_cross_order_fms_reference_fails_without_deleting_anything(self):
        ordinary, _ = self.seed(False)
        simulated, _ = self.seed(True)
        self.psql(
            "UPDATE order_version SET fms_result_id=(SELECT id FROM fms_submission_result "
            f"WHERE order_id='{simulated['id']}') WHERE order_id='{ordinary['id']}';"
        )
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_unknown_dependency_rolls_back_all_prior_deletes(self):
        simulated, versions = self.seed(True)
        self.psql(
            "CREATE TABLE unexpected_child (version_id UUID REFERENCES order_version(id));"
            f"INSERT INTO unexpected_child VALUES ('{versions[0]}');"
        )
        try:
            before = self.snapshot()
            self.assertNotEqual(self.cleanup(check=False).returncode, 0)
            self.assertEqual(self.snapshot(), before)
            self.assertEqual(
                self.psql(f"SELECT count(*) FROM orders WHERE id='{simulated['id']}'").stdout.strip(), "1"
            )
        finally:
            self.psql("DROP TABLE unexpected_child;")

    def test_leftover_fms_rows_for_a_deleted_created_order_are_removed(self):
        order = str(uuid.uuid4())
        self.row("up3_simulation_creation", order_id=order, creation_request_id=str(uuid.uuid4()))
        wearer = self.row("fms_device_wearer_submission_result", device_wearer_id="sim-orphan")
        monitoring = self.row("fms_monitoring_order_submission_result")
        self.row(
            "fms_submission_result", order_id=order, fms_device_wearer_result_id=wearer["id"],
            fms_monitoring_order_result_id=monitoring["id"],
        )
        self.row("fms_order_details", case_id="sim-orphan")
        self.cleanup()
        self.assertEqual(self.psql("SELECT count(*) FROM fms_submission_result").stdout.strip(), "0")

    def test_unattributed_synthetic_submission_blocks_cleanup(self):
        self.seed(True)
        wearer = self.row("fms_device_wearer_submission_result", device_wearer_id="sim-unattributed")
        self.row("fms_submission_result", order_id=str(uuid.uuid4()), fms_device_wearer_result_id=wearer["id"])
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_failed_submission_without_a_case_can_be_cleaned(self):
        order, _ = self.seed(True)
        self.psql(
            "UPDATE fms_device_wearer_submission_result SET device_wearer_id='' "
            "WHERE id IN (SELECT fms_device_wearer_result_id FROM fms_submission_result "
            f"WHERE order_id='{order['id']}');"
        )
        self.cleanup()
        self.assertEqual(self.psql("SELECT count(*) FROM orders").stdout.strip(), "0")

    def test_unattributed_case_cache_blocks_cleanup(self):
        self.seed(True)
        self.row("fms_order_details", case_id="sim-unattributed")
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_receipt_for_a_live_unenrolled_order_blocks_cleanup(self):
        order, _ = self.seed(True)
        self.psql(f"DELETE FROM up3_simulation WHERE order_id='{order['id']}';")
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_missing_submission_reference_blocks_cleanup(self):
        order, _ = self.seed(True)
        self.psql(f"UPDATE order_version SET fms_result_id='{uuid.uuid4()}' WHERE order_id='{order['id']}';")
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_non_synthetic_target_case_blocks_cleanup(self):
        self.seed(True)
        self.psql("UPDATE up3_simulation SET case_id='real-case';")
        before = self.snapshot()
        self.assertNotEqual(self.cleanup(check=False).returncode, 0)
        self.assertEqual(self.snapshot(), before)

    def test_unattributed_synthetic_results_block_cleanup(self):
        for table, field in [
            ("fms_device_wearer_submission_result", "device_wearer_id"),
            ("fms_monitoring_order_submission_result", "monitoring_order_id"),
        ]:
            with self.subTest(table=table):
                result = self.row(table, **{field: "sim-unattributed"})
                before = self.snapshot()
                self.assertNotEqual(self.cleanup(check=False).returncode, 0)
                self.assertEqual(self.snapshot(), before)
                self.psql(f"DELETE FROM {table} WHERE id='{result['id']}';")

    @contextmanager
    def without_storage(self, partial=False):
        tables = "up3_simulation_return_event, up3_simulation, up3_simulation_creation"
        self.psql("DROP TABLE " + ("up3_simulation_return_event" if partial else tables))
        try:
            yield
        finally:
            self.psql("DROP TABLE IF EXISTS " + tables)
            self.psql("\n".join(
                path.read_text() for path in sorted((ROOT / "src/main/resources/db/up3-simulation").glob("V*.sql"))
            ))

    def test_pre_simulator_database_can_be_verified_without_creating_storage(self):
        optional = {"up3_simulation_return_event", "up3_simulation", "up3_simulation_creation"}
        self.seed(False)
        with self.without_storage():
            before = self.snapshot(exclude=optional)
            self.assertEqual(json.loads(self.cleanup().stdout)["orders"], 0)
            self.assertEqual(self.snapshot(exclude=optional), before)
            self.assertEqual(
                self.psql("SELECT to_regclass('up3_simulation') IS NULL").stdout.strip(), "t",
            )

    def test_partial_optional_schema_fails_closed(self):
        with self.without_storage(partial=True):
            self.assertNotEqual(self.cleanup(check=False).returncode, 0)

    def test_synthetic_cache_without_storage_blocks_cleanup(self):
        with self.without_storage():
            self.row("fms_order_details", case_id="sim-orphan")
            self.assertNotEqual(self.cleanup(check=False).returncode, 0)
            self.assertEqual(self.psql("SELECT count(*) FROM fms_order_details").stdout.strip(), "1")


if __name__ == "__main__":
    unittest.main()
