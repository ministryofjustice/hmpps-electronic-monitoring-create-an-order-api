import copy
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import Mock, patch


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts"))
sys.path.insert(0, str(ROOT / "scripts/helm-api-image"))

import pin_api_image as renderer
import up3_simulation_maintenance as module


DIGEST = "sha256:" + "a" * 64


def completed_pod(report):
    return {
        "metadata": {"ownerReferences": [{"kind": "Job", "uid": "job-uid", "controller": True}]},
        "status": {
            "phase": "Succeeded",
            "containerStatuses": [{
                "name": "cleanup",
                "state": {"terminated": {"exitCode": 0, "message": report}},
            }],
        },
    }


def deployment(profile="dev"):
    return {
        "metadata": {"namespace": "cemo-dev", "generation": 1, "annotations": {}},
        "spec": {
            "replicas": 2, "selector": {"matchLabels": {"app": module.APP}},
            "template": {"spec": {"containers": [{
                "name": module.APP, "image": module.REPOSITORY + ":previous@" + DIGEST,
                "env": [{"name": "SPRING_PROFILES_ACTIVE", "value": profile}],
            }]}},
        },
        "status": {"observedGeneration": 1, "updatedReplicas": 2, "availableReplicas": 2},
    }


class MaintenanceTest(unittest.TestCase):
    def setUp(self):
        self.maintenance = module.Maintenance("cemo-dev")
        self.deployment = deployment()
        self.container = self.deployment["spec"]["template"]["spec"]["containers"][0]

    def prepare(self):
        self.maintenance.deployment = Mock(return_value=(self.deployment, self.container))
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.assert_paused = Mock()
        self.maintenance.database_job = Mock()
        self.maintenance.deploy = Mock()
        self.maintenance.kube = Mock(side_effect=AssertionError("Unexpected cluster command"))
        self.maintenance.pods = Mock(return_value=[
            {"metadata": {}, "spec": {"containers": [copy.deepcopy(self.container)]}},
            {"metadata": {}, "spec": {"containers": [copy.deepcopy(self.container)]}},
        ])

    def test_non_dev_namespace_and_profile_are_rejected(self):
        for namespace in ["", "cemo-prod", "cemo-preprod", "dev", "cemo-dev;other"]:
            with self.assertRaises(module.MaintenanceError):
                module.Maintenance(namespace)
        self.maintenance.kube = Mock(return_value=deployment("prod"))
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.deployment()

    def test_preview_does_not_pause_or_deploy(self):
        self.prepare()
        self.maintenance.execute("preview")
        self.maintenance.database_job.assert_called_once_with(False)
        self.maintenance.assert_paused.assert_not_called()
        self.maintenance.deploy.assert_not_called()

    def test_cleanup_uses_manual_stop_and_leaves_api_stopped(self):
        self.deployment["spec"]["replicas"] = 0
        self.maintenance.deployment = Mock(return_value=(self.deployment, self.container))
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.pods = Mock(return_value=[])
        self.maintenance.database_job = Mock()
        self.maintenance.kube = Mock(side_effect=AssertionError("Unexpected cluster mutation or scheduler access"))
        self.maintenance.execute("cleanup", confirmation=module.CONFIRMATION)
        self.maintenance.database_job.assert_called_once_with(True)
        self.maintenance.kube.assert_not_called()

    def test_cleanup_needs_confirmation_before_checking_stopped_state(self):
        self.prepare()
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("cleanup")
        self.maintenance.assert_paused.assert_not_called()
        self.maintenance.database_job.assert_not_called()

    def test_failed_cleanup_never_changes_replicas_or_deploys(self):
        self.prepare()
        self.maintenance.database_job.side_effect = module.MaintenanceError("DB failed")
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("cleanup", confirmation=module.CONFIRMATION)
        self.maintenance.kube.assert_not_called()
        self.maintenance.deploy.assert_not_called()

    def test_cleanup_checks_stopped_state_before_database_changes(self):
        self.prepare()
        calls = Mock()
        for name in ["assert_paused", "database_job"]:
            calls.attach_mock(getattr(self.maintenance, name), name)
        self.maintenance.execute("cleanup", confirmation=module.CONFIRMATION)
        self.assertEqual([call[0] for call in calls.mock_calls], ["assert_paused", "database_job"])

    @patch.object(module, "image_metadata", return_value=(False, DIGEST))
    def test_legacy_deployment_without_confirmation_does_not_modify_dev(self, image):
        self.prepare()
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("deploy", version="old-version")
        self.maintenance.assert_paused.assert_not_called()
        self.maintenance.database_job.assert_not_called()
        self.maintenance.deploy.assert_not_called()

    @patch.object(module, "image_metadata", side_effect=[(False, DIGEST), (True, DIGEST)])
    def test_legacy_deployment_cleans_and_keeps_new_image_stopped(self, image):
        self.prepare()
        self.deployment["spec"]["replicas"] = 0
        calls = Mock()
        for name in ["assert_paused", "database_job", "deploy"]:
            calls.attach_mock(getattr(self.maintenance, name), name)
        self.maintenance.execute("deploy", version="old-version", confirmation=module.CONFIRMATION)
        self.assertEqual(
            [call[0] for call in calls.mock_calls], ["assert_paused", "database_job", "deploy", "assert_paused"],
        )
        self.maintenance.deploy.assert_called_once_with("old-version", DIGEST, False, 0)
        self.maintenance.kube.assert_not_called()

    @patch.object(module, "image_metadata", return_value=(True, DIGEST))
    def test_compatible_rolling_deployment_preserves_orders(self, image):
        self.prepare()
        self.maintenance.execute("deploy", version="new-version")
        self.maintenance.assert_paused.assert_not_called()
        self.maintenance.database_job.assert_not_called()
        self.maintenance.deploy.assert_called_once_with("new-version", DIGEST, True, 2)

    @patch.object(module, "image_metadata", side_effect=[(True, DIGEST), (False, DIGEST)])
    def test_first_simulator_release_requires_manual_stop(self, image):
        self.prepare()
        del self.maintenance.assert_paused
        with self.assertRaisesRegex(module.MaintenanceError, "Stop the dev API manually"):
            self.maintenance.execute("deploy", version="new-version")
        self.maintenance.database_job.assert_not_called()
        self.maintenance.deploy.assert_not_called()

    @patch.object(module, "image_metadata", side_effect=[(True, DIGEST), (False, DIGEST)])
    def test_first_simulator_release_updates_a_manually_stopped_api(self, image):
        self.prepare()
        self.deployment["spec"]["replicas"] = 0
        self.maintenance.pods.return_value = []
        del self.maintenance.assert_paused
        self.maintenance.execute("deploy", version="new-version")
        self.maintenance.deploy.assert_called_once_with("new-version", DIGEST, True, 0)
        self.maintenance.database_job.assert_not_called()
        self.maintenance.kube.assert_not_called()

    @patch.object(module, "image_metadata", return_value=(True, DIGEST))
    def test_mutable_current_tag_is_not_treated_as_proof_of_running_image_safety(self, image):
        self.prepare()
        self.container["image"] = module.REPOSITORY + ":dev"
        del self.maintenance.assert_paused
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("deploy", version="new-version")
        self.maintenance.deploy.assert_not_called()
        self.assertEqual(image.call_count, 1)

    @patch.object(module, "image_metadata", return_value=(True, DIGEST))
    def test_mixed_running_pod_images_require_quiescence(self, image):
        self.prepare()
        self.maintenance.pods.return_value[0]["spec"]["containers"][0]["image"] = module.REPOSITORY + ":old"
        del self.maintenance.assert_paused
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("deploy", version="new-version")
        self.maintenance.deploy.assert_not_called()

    @patch.object(module, "image_metadata", return_value=(True, DIGEST))
    def test_failed_rollout_has_no_automatic_resume_or_rollback(self, image):
        self.prepare()
        self.deployment["spec"]["replicas"] = 0
        self.maintenance.deploy.side_effect = module.MaintenanceError("Helm failed")
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("deploy", version="new-version")
        self.maintenance.kube.assert_not_called()
        self.maintenance.database_job.assert_not_called()

    @patch.object(module, "image_metadata", side_effect=[(False, DIGEST), (True, DIGEST)])
    def test_cleanup_failure_blocks_legacy_rollout(self, image):
        self.prepare()
        self.maintenance.database_job.side_effect = module.MaintenanceError("DB failed")
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("deploy", version="old", confirmation=module.CONFIRMATION)
        self.maintenance.deploy.assert_not_called()
        self.maintenance.kube.assert_not_called()

    def test_mutable_or_invalid_image_tags_fail_before_any_write(self):
        self.prepare()
        for version in ["", "dev", "latest", "-bad", "_bad", "bad-", "a" * 64, "foo;echo", "tag@sha256:abc"]:
            with self.assertRaises(module.MaintenanceError):
                self.maintenance.execute("deploy", version=version)
        self.maintenance.assert_paused.assert_not_called()

    def test_stopped_check_rejects_remaining_or_terminating_pods(self):
        self.deployment["spec"]["replicas"] = 0
        self.maintenance.deployment = Mock(return_value=(self.deployment, self.container))
        for pods in [[{"metadata": {}}], [{"metadata": {"deletionTimestamp": "2026-11-11T12:00:00Z"}}]]:
            with self.subTest(pods=pods):
                self.maintenance.pods = Mock(return_value=pods)
                with self.assertRaises(module.MaintenanceError):
                    self.maintenance.assert_paused()

    def test_cleanup_refuses_a_running_api_before_starting_the_job(self):
        self.prepare()
        del self.maintenance.assert_paused
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("cleanup", confirmation=module.CONFIRMATION)
        self.maintenance.database_job.assert_not_called()
        self.maintenance.kube.assert_not_called()

    @patch.object(module, "image_metadata", side_effect=[(False, DIGEST), (True, DIGEST)])
    def test_legacy_deployment_refuses_a_running_api(self, image):
        self.prepare()
        del self.maintenance.assert_paused
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.execute("deploy", version="old", confirmation=module.CONFIRMATION)
        self.maintenance.database_job.assert_not_called()
        self.maintenance.deploy.assert_not_called()

    def test_stopped_check_accepts_zero_replicas_and_no_pods(self):
        self.deployment["spec"]["replicas"] = 0
        self.maintenance.deployment = Mock(return_value=(self.deployment, self.container))
        self.maintenance.pods = Mock(return_value=[])
        self.maintenance.assert_paused()

    def test_existing_unfinished_database_job_blocks_new_operations(self):
        self.maintenance.kube = Mock(return_value={"items": [{"status": {}}]})
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.assert_no_active_jobs()

    @patch.object(module, "command")
    def test_deploy_pins_the_image_and_does_not_enable_helm_automatic_rollback(self, command):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            chart = root / "helm_deploy" / module.APP
            chart.mkdir(parents=True)
            (chart / "Chart.yaml").write_text("appVersion: '1.0'\n")
            command.return_value = ""
            with patch.object(module, "ROOT", root):
                self.maintenance.deploy("release-123", DIGEST, False, 2)
        arguments = command.call_args.args[0]
        self.assertNotIn("--rollback-on-failure", arguments)
        self.assertNotIn("--atomic", arguments)
        self.assertIn("--post-renderer", arguments)
        self.assertIn("generic-service.replicaCount=2", arguments)
        self.assertIn("generic-service.scheduledDowntime.enabled=false", arguments)
        self.assertFalse(any("scheduledDowntime.startup" in argument or "scheduledDowntime.shutdown" in argument
                     for argument in arguments))
        self.assertIn("generic-service.env.UP3_SIMULATION_ENABLED=false", arguments)
        self.assertEqual(command.call_args.kwargs["env"]["UP3_API_DIGEST"], DIGEST)

    @patch.object(module, "command", return_value="")
    def test_stopped_deploy_keeps_zero_replicas_and_rechecks_before_helm(self, command):
        for restarted in [False, True]:
            with self.subTest(restarted=restarted), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                chart = root / "helm_deploy" / module.APP
                chart.mkdir(parents=True)
                (chart / "Chart.yaml").write_text("appVersion: '1.0'\n")
                command.reset_mock()
                self.maintenance.assert_paused = Mock(
                    side_effect=module.MaintenanceError("API restarted") if restarted else None,
                )
                with patch.object(module, "ROOT", root):
                    if restarted:
                        with self.assertRaises(module.MaintenanceError):
                            self.maintenance.deploy("release-123", DIGEST, True, 0)
                        self.assertFalse(any(call.args[0][:2] == ["helm", "upgrade"]
                                             for call in command.call_args_list))
                    else:
                        self.maintenance.deploy("release-123", DIGEST, True, 0)
                        self.assertIn("generic-service.replicaCount=0", command.call_args.args[0])
                self.maintenance.assert_paused.assert_called_once()

    def test_database_job_keeps_credentials_in_cluster_and_owns_its_configmap(self):
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.assert_paused = Mock()
        resources = []

        def kube(*args, data=None, json_output=False):
            if args[0] == "create":
                resources.append(copy.deepcopy(data))
                return {"metadata": {"uid": "job-uid"}}
            if args[:2] == ("get", "job"):
                return {"status": {"succeeded": 1}}
            if args[:2] == ("get", "pods"):
                return {"items": [completed_pod(json.dumps({
                    "orders": 1, "versions": 2, "submissions": 2, "return_events": 1,
                    "retained_creation_receipts": 1,
                }))]}
            raise AssertionError(args)

        self.maintenance.kube = Mock(side_effect=kube)
        self.maintenance.database_job(True)
        pod = resources[0]["spec"]["template"]["spec"]
        self.assertFalse(pod["automountServiceAccountToken"])
        self.assertEqual(pod["containers"][0]["terminationMessagePath"], "/dev/termination-log")
        self.assertEqual(pod["containers"][0]["terminationMessagePolicy"], "File")
        self.assertEqual(pod["containers"][0]["env"][-1], {"name": "CLEANUP_APPLY", "value": "true"})
        for variable in pod["containers"][0]["env"][:-1]:
            self.assertEqual(
                variable["valueFrom"]["secretKeyRef"]["name"], "rds-postgresql-instance-output",
            )
        self.assertEqual(resources[1]["metadata"]["ownerReferences"][0]["uid"], "job-uid")

        for output, exit_code, expected_exit in [('counts', 0, 0), ('counts', 3, 3), ('x' * 3501, 0, 1)]:
            with self.subTest(exit_code=exit_code, output_length=len(output)), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                executable = root / "psql"
                executable.write_text('#!/bin/sh\nprintf "%s" "$STUB_OUTPUT"\nexit "$STUB_EXIT"\n')
                executable.chmod(0o755)
                report_path = root / "report"
                report_path.touch()
                arguments = pod["containers"][0]["command"].copy()
                arguments[-1] = arguments[-1].replace("/dev/termination-log", str(report_path))
                result = subprocess.run(arguments, text=True, capture_output=True, env={
                    **os.environ, "PATH": f"{directory}:{os.environ['PATH']}",
                    "DB_SERVER": "localhost:5432", "CLEANUP_APPLY": "false",
                    "STUB_OUTPUT": output, "STUB_EXIT": str(exit_code),
                })
                self.assertEqual(result.returncode, expected_exit, result.stderr)
                self.assertEqual(report_path.read_text(), output if expected_exit == 0 else "")

    def test_failed_job_is_retained_for_inspection(self):
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.assert_paused = Mock()
        self.maintenance.kube = Mock(side_effect=[
            {"metadata": {"uid": "job-uid"}}, "",
            {"status": {"conditions": [{"type": "Failed", "status": "True"}]}},
        ])
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.database_job(True)
        self.assertFalse(any(call.args[0] == "delete" for call in self.maintenance.kube.call_args_list))

    def test_database_job_is_cancelled_if_api_restarts_during_cleanup(self):
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.assert_paused = Mock(side_effect=[None, module.MaintenanceError("API restarted")])
        self.maintenance.kube = Mock(side_effect=[{"metadata": {"uid": "job-uid"}}, "", ""])
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.database_job(True)
        self.assertEqual(self.maintenance.kube.call_args.args[:2], ("delete", "job"))
        self.assertFalse(any(call.args[:2] == ("get", "pods") for call in self.maintenance.kube.call_args_list))

    def test_job_is_cancelled_if_configmap_creation_fails(self):
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.assert_paused = Mock()
        self.maintenance.kube = Mock(side_effect=[
            {"metadata": {"uid": "job-uid"}}, module.MaintenanceError("ConfigMap creation failed"), "",
        ])
        with self.assertRaises(module.MaintenanceError):
            self.maintenance.database_job(True)
        self.assertEqual(self.maintenance.kube.call_args.args[0], "delete")

    def test_invalid_success_report_blocks_recovery(self):
        self.maintenance.assert_no_active_jobs = Mock()
        self.maintenance.assert_paused = Mock()
        for report in ['{}', '"not an object"', '{"orders":true}', 'not json']:
            with self.subTest(report=report):
                self.maintenance.kube = Mock(side_effect=[
                    {"metadata": {"uid": "job-uid"}}, "", {"status": {"succeeded": 1}},
                    {"items": [completed_pod(report)]},
                ])
                with self.assertRaises((module.MaintenanceError, json.JSONDecodeError)):
                    self.maintenance.database_job(True)
                self.assertFalse(any(
                    call.args[0] == "delete" for call in self.maintenance.kube.call_args_list
                ))

    def test_database_report_requires_one_successful_pod_owned_by_this_job(self):
        pod = completed_pod('{}')
        wrong_owner = copy.deepcopy(pod)
        wrong_owner["metadata"]["ownerReferences"][0]["uid"] = "another-job"
        failed_pod = copy.deepcopy(pod)
        failed_pod["status"]["phase"] = "Failed"
        for pods in [[], [pod, pod], [wrong_owner], [failed_pod]]:
            with self.subTest(pods=pods):
                self.maintenance.kube = Mock(return_value={"items": pods})
                with self.assertRaises(module.MaintenanceError):
                    self.maintenance.database_report("job-uid")
                self.maintenance.kube.assert_called_once_with(
                    "get", "pods", "--selector", "batch.kubernetes.io/controller-uid=job-uid",
                    "-o", "json", json_output=True,
                )

    def test_database_report_requires_a_successful_cleanup_container(self):
        for containers in [
            [],
            [{"name": "sidecar", "state": {"terminated": {"exitCode": 0, "message": '{}'}}}],
            [{"name": "cleanup", "state": {"running": {}}}],
            [{"name": "cleanup", "state": {"terminated": {"exitCode": 1, "message": '{}'}}}],
        ]:
            with self.subTest(containers=containers):
                pod = completed_pod('{}')
                pod["status"]["containerStatuses"] = containers
                self.maintenance.kube = Mock(return_value={"items": [pod]})
                with self.assertRaises(module.MaintenanceError):
                    self.maintenance.database_report("job-uid")

    def test_database_report_rejects_missing_oversized_or_invalid_counts(self):
        valid = dict(orders=1, versions=2, submissions=2, return_events=1, retained_creation_receipts=1)
        for report in [
            None, "", "x" * 3501,
            json.dumps({**valid, "orders": -1}),
            json.dumps({**valid, "orders": True}),
            json.dumps({**valid, "orders": "1"}),
            json.dumps({**valid, "extra": 0}),
        ]:
            with self.subTest(report=report):
                self.maintenance.kube = Mock(return_value={"items": [completed_pod(report)]})
                with self.assertRaises(module.MaintenanceError):
                    self.maintenance.database_report("job-uid")


class ImagePinningTest(unittest.TestCase):
    def test_only_the_verified_api_image_is_replaced(self):
        image = module.REPOSITORY + ":release-123"
        for quoted in [image, f'"{image}"', f"'{image}'"]:
            manifest = f"    image: {quoted}\n    image: gotenberg/gotenberg:8\n"
            result = renderer.pin_manifest(manifest, image, DIGEST)
            self.assertIn(image + "@" + DIGEST, result)
            self.assertIn("image: gotenberg/gotenberg:8", result)

    def test_missing_image_or_invalid_digest_fails_closed(self):
        with self.assertRaises(ValueError):
            renderer.pin_manifest("image: unrelated:latest\n", module.REPOSITORY + ":123", DIGEST)
        with self.assertRaises(ValueError):
            renderer.pin_manifest("", module.REPOSITORY + ":123", "not-a-digest")

    @patch.object(module, "command")
    def test_image_capability_is_read_from_the_pulled_image_not_from_a_user_input(self, command):
        command.side_effect = [
            "", json.dumps([{
                "RepoDigests": [module.REPOSITORY + "@" + DIGEST],
                "Config": {"Labels": {module.CAPABILITY: "true"}},
            }]),
        ]
        self.assertEqual(module.image_metadata(module.REPOSITORY + ":123"), (True, DIGEST))


@unittest.skipUnless(os.environ.get("UP3_MAINTENANCE_HELM_TEST") == "true", "Set UP3_MAINTENANCE_HELM_TEST=true")
class HelmRenderingTest(unittest.TestCase):
    def test_real_dev_chart_uses_the_verified_digest_without_putting_it_in_version_labels(self):
        with tempfile.TemporaryDirectory() as directory:
            chart = Path(directory) / "chart"
            shutil.copytree(ROOT / "helm_deploy" / module.APP, chart)
            environment = {
                **os.environ,
                "HELM_PLUGINS": str(Path(directory) / "data/plugins"),
                "HELM_CACHE_HOME": str(Path(directory) / "cache"),
                "HELM_CONFIG_HOME": str(Path(directory) / "config"),
                "HELM_DATA_HOME": str(Path(directory) / "data"),
                "UP3_API_IMAGE": module.REPOSITORY + ":release-123",
                "UP3_API_DIGEST": DIGEST,
            }

            def helm(*arguments):
                result = subprocess.run(
                    ["helm", *arguments], env=environment, text=True, capture_output=True, timeout=120,
                )
                self.assertEqual(result.returncode, 0, result.stderr)
                return result.stdout

            helm("dependency", "update", str(chart))
            helm("plugin", "install", str(ROOT / "scripts/helm-api-image"))
            rendered = helm(
                "template", module.APP, str(chart), "--namespace", "cemo-dev",
                "--values", str(ROOT / "helm_deploy/values-dev.yaml"),
                "--set-string", "generic-service.image.tag=release-123",
                "--set", "generic-service.env.UP3_SIMULATION_ENABLED=false",
                "--set", "generic-service.replicaCount=0",
                "--set", "generic-service.scheduledDowntime.enabled=false",
                "--post-renderer", "up3-pin-api-image",
            )
            self.assertRegex(rendered, r'image:\s*["\']?' + re.escape(environment["UP3_API_IMAGE"] + "@" + DIGEST))
            self.assertNotRegex(rendered, r"app.kubernetes.io/version:.*@sha256")
            self.assertRegex(rendered, r'name: UP3_SIMULATION_ENABLED\s+value: ["\']?false')
            self.assertRegex(rendered, r"replicas:\s*0")
            self.assertNotRegex(rendered, r"kind:\s*CronJob")


if __name__ == "__main__":
    unittest.main()
