\set ON_ERROR_STOP on
\set VERBOSITY terse
BEGIN ISOLATION LEVEL REPEATABLE READ;
SET LOCAL search_path = public, pg_temp;
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '240s';

SELECT pg_advisory_xact_lock(33005085) \gset

DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT count(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name IN ('up3_simulation', 'up3_simulation_creation', 'up3_simulation_return_event');
    IF table_count NOT IN (0, 3) THEN
        RAISE EXCEPTION 'Incomplete simulator schema; no data deleted';
    END IF;
END $$;

SELECT to_regclass('public.up3_simulation') IS NOT NULL AS has_storage \gset
\if :has_storage

CREATE TEMP TABLE cleanup_orders (id UUID PRIMARY KEY) ON COMMIT DROP;
INSERT INTO cleanup_orders
SELECT order_id FROM up3_simulation
UNION
SELECT order_id FROM up3_simulation_creation WHERE order_id IS NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM up3_simulation s
        WHERE NOT EXISTS (SELECT 1 FROM up3_simulation_creation c WHERE c.order_id = s.order_id)
    ) OR EXISTS (
        SELECT 1 FROM orders o JOIN cleanup_orders c ON c.id = o.id
        WHERE NOT EXISTS (SELECT 1 FROM up3_simulation s WHERE s.order_id = o.id)
    ) THEN
        RAISE EXCEPTION 'Order provenance is inconsistent; no data deleted';
    END IF;
    IF EXISTS (
        SELECT 1 FROM fms_submission_result f
        JOIN fms_device_wearer_submission_result d ON d.id = f.fms_device_wearer_result_id
        WHERE d.device_wearer_id LIKE 'sim-%'
          AND NOT EXISTS (SELECT 1 FROM cleanup_orders c WHERE c.id = f.order_id)
    ) THEN
        RAISE EXCEPTION 'Unattributed synthetic FMS submission; no data deleted';
    END IF;
END $$;

CREATE TEMP TABLE cleanup_versions ON COMMIT DROP AS
SELECT v.id, v.fms_result_id FROM order_version v JOIN cleanup_orders c ON c.id = v.order_id;
CREATE TEMP TABLE cleanup_submissions ON COMMIT DROP AS
SELECT f.* FROM fms_submission_result f JOIN cleanup_orders c ON c.id = f.order_id;
CREATE TEMP TABLE cleanup_cases ON COMMIT DROP AS
SELECT case_id FROM up3_simulation
UNION
SELECT d.device_wearer_id
FROM fms_device_wearer_submission_result d
JOIN cleanup_submissions f ON f.fms_device_wearer_result_id = d.id
WHERE d.device_wearer_id <> '';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM order_version v
        JOIN cleanup_submissions f ON f.id = v.fms_result_id
        WHERE NOT EXISTS (SELECT 1 FROM cleanup_orders c WHERE c.id = v.order_id)
    ) OR EXISTS (
        SELECT 1 FROM cleanup_versions v
        WHERE v.fms_result_id IS NOT NULL
          AND NOT EXISTS (SELECT 1 FROM cleanup_submissions f WHERE f.id = v.fms_result_id)
    ) THEN
        RAISE EXCEPTION 'Cross-order FMS reference; no data deleted';
    END IF;
    IF EXISTS (SELECT 1 FROM cleanup_cases WHERE case_id NOT LIKE 'sim-%') THEN
        RAISE EXCEPTION 'Non-synthetic case belongs to cleanup target; no data deleted';
    END IF;
    IF EXISTS (
        SELECT 1 FROM fms_order_details
        WHERE case_id LIKE 'sim-%' AND case_id NOT IN (SELECT case_id FROM cleanup_cases)
    ) OR EXISTS (
        SELECT 1 FROM fms_device_wearer_submission_result
        WHERE device_wearer_id LIKE 'sim-%'
          AND id NOT IN (SELECT fms_device_wearer_result_id FROM cleanup_submissions
                         WHERE fms_device_wearer_result_id IS NOT NULL)
    ) OR EXISTS (
        SELECT 1 FROM fms_monitoring_order_submission_result
        WHERE monitoring_order_id LIKE 'sim-%'
          AND id NOT IN (SELECT fms_monitoring_order_result_id FROM cleanup_submissions
                         WHERE fms_monitoring_order_result_id IS NOT NULL)
    ) THEN
        RAISE EXCEPTION 'Unattributed synthetic cache or FMS result; no data deleted';
    END IF;
END $$;

SELECT json_build_object(
    'orders', (SELECT count(*) FROM orders WHERE id IN (SELECT id FROM cleanup_orders)),
    'versions', (SELECT count(*) FROM cleanup_versions),
    'submissions', (SELECT count(*) FROM cleanup_submissions),
    'return_events', (SELECT count(*) FROM up3_simulation_return_event),
    'retained_creation_receipts', (SELECT count(*) FROM up3_simulation_creation)
);

\if :apply
DELETE FROM status_update_reason
WHERE status_update_id IN (SELECT id FROM status_update WHERE version_id IN (SELECT id FROM cleanup_versions));
DELETE FROM status_update WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM additional_documents WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM alcohol_monitoring WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM contact_details WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM curfew WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM curfew_release_date WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM curfew_timetable WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM device_wearer WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM enforcement_zone WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM installation_and_risk WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM interested_parties WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM mandatory_attendance WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM monitoring_conditions WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM responsible_adult WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM trail_monitoring WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM variation_details WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM probation_delivery_unit WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM installation_location WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM installation_appointment WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM order_parameters WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM dapo WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM offence WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM mappa WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM offence_additional_details WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM details_of_installation WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM address WHERE version_id IN (SELECT id FROM cleanup_versions);
DELETE FROM order_version WHERE id IN (SELECT id FROM cleanup_versions);
DELETE FROM up3_simulation_return_event WHERE order_id IN (SELECT id FROM cleanup_orders);
DELETE FROM up3_simulation WHERE order_id IN (SELECT id FROM cleanup_orders);
DELETE FROM orders WHERE id IN (SELECT id FROM cleanup_orders);
DELETE FROM fms_order_details WHERE case_id IN (SELECT case_id FROM cleanup_cases);
DELETE FROM fms_attachment_submission_result WHERE fms_attachment_result_id IN (SELECT id FROM cleanup_submissions);
DELETE FROM fms_submission_result WHERE id IN (SELECT id FROM cleanup_submissions);
DELETE FROM fms_device_wearer_submission_result
WHERE id IN (SELECT fms_device_wearer_result_id FROM cleanup_submissions);
DELETE FROM fms_monitoring_order_submission_result
WHERE id IN (SELECT fms_monitoring_order_result_id FROM cleanup_submissions);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM orders WHERE id IN (SELECT id FROM cleanup_orders))
        OR EXISTS (SELECT 1 FROM order_version WHERE id IN (SELECT id FROM cleanup_versions))
        OR EXISTS (SELECT 1 FROM fms_submission_result WHERE order_id IN (SELECT id FROM cleanup_orders))
        OR EXISTS (SELECT 1 FROM up3_simulation)
        OR EXISTS (SELECT 1 FROM up3_simulation_return_event)
        OR EXISTS (SELECT 1 FROM fms_order_details WHERE case_id IN (SELECT case_id FROM cleanup_cases))
        OR EXISTS (
            SELECT 1 FROM fms_device_wearer_submission_result
            WHERE id IN (SELECT fms_device_wearer_result_id FROM cleanup_submissions)
        )
        OR EXISTS (
            SELECT 1 FROM fms_monitoring_order_submission_result
            WHERE id IN (SELECT fms_monitoring_order_result_id FROM cleanup_submissions)
        )
        OR EXISTS (
            SELECT 1 FROM fms_attachment_submission_result
            WHERE fms_attachment_result_id IN (SELECT id FROM cleanup_submissions)
        )
    THEN
        RAISE EXCEPTION 'Cleanup verification failed; transaction rolled back';
    END IF;
END $$;
\endif

\else
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM fms_device_wearer_submission_result WHERE device_wearer_id LIKE 'sim-%')
        OR EXISTS (SELECT 1 FROM fms_monitoring_order_submission_result WHERE monitoring_order_id LIKE 'sim-%')
        OR EXISTS (SELECT 1 FROM fms_order_details WHERE case_id LIKE 'sim-%')
    THEN
        RAISE EXCEPTION 'Synthetic data exists without simulator storage; no data deleted';
    END IF;
END $$;
SELECT json_build_object('orders', 0, 'versions', 0, 'submissions', 0,
                         'return_events', 0, 'retained_creation_receipts', 0);
\endif

\if :apply
COMMIT;
\else
ROLLBACK;
\endif
