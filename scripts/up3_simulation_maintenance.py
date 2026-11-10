#!/usr/bin/env python3
"""Dev-only simulator maintenance. Database credentials stay inside Kubernetes."""

import argparse
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
import time
import uuid


ROOT = Path(__file__).resolve().parents[1]
APP = "hmpps-electronic-monitoring-create-an-order-api"
REPOSITORY = f"ghcr.io/ministryofjustice/{APP}"
CAPABILITY = "uk.gov.justice.hmpps.up3-simulation-aware"
CONFIRMATION = "DELETE SIMULATED DEV ORDERS"
REPLICAS_ANNOTATION = "cemo.hmpps.justice.gov.uk/up3-cleanup-replicas"
JOB_LABEL = "cemo.hmpps.justice.gov.uk/up3-maintenance"


class MaintenanceError(RuntimeError):
    pass


def command(arguments, **kwargs):
    try:
        return subprocess.run(arguments, text=True, capture_output=True, check=True, **kwargs).stdout
    except subprocess.CalledProcessError as error:
        raise MaintenanceError(f"{arguments[0]} failed: {error.stderr.strip()[:2000]}") from error


def image_metadata(image):
    if not image.startswith(REPOSITORY + ":") and not image.startswith(REPOSITORY + "@"):
        raise MaintenanceError("Unexpected API image repository")
    command(["docker", "pull", image])
    metadata = json.loads(command(["docker", "image", "inspect", image]))[0]
    digests = [
        value.split("@", 1)[1] for value in metadata["RepoDigests"]
        if value.startswith(REPOSITORY + "@")
    ]
    if len(digests) != 1 or not re.fullmatch(r"sha256:[0-9a-f]{64}", digests[0]):
        raise MaintenanceError("Cannot establish the API image digest")
    labels = metadata["Config"].get("Labels") or {}
    return labels.get(CAPABILITY) == "true", digests[0]


class Maintenance:
    def __init__(self, namespace):
        if not re.fullmatch(r"[a-z0-9][a-z0-9-]*-dev", namespace):
            raise MaintenanceError("Maintenance requires an explicitly dev-suffixed namespace")
        self.namespace = namespace

    def kube(self, *arguments, data=None, json_output=False):
        output = command(
            ["kubectl", "--namespace", self.namespace, *arguments],
            input=json.dumps(data) if data is not None else None,
        )
        return json.loads(output) if json_output else output

    def deployment(self):
        deployment = self.kube("get", "deployment", APP, "-o", "json", json_output=True)
        containers = deployment["spec"]["template"]["spec"]["containers"]
        container = next((item for item in containers if item["name"] == APP), None)
        profile = next(
            (item.get("value") for item in container.get("env", []) if item["name"] == "SPRING_PROFILES_ACTIVE"),
            None,
        ) if container else None
        if profile != "dev" or deployment["metadata"]["namespace"] != self.namespace:
            raise MaintenanceError("Target is not the expected dev API deployment")
        return deployment, container

    def selector(self, deployment):
        selector = deployment["spec"]["selector"]
        if selector.get("matchExpressions") or not selector.get("matchLabels"):
            raise MaintenanceError("Unsupported API pod selector")
        return ",".join(f"{key}={value}" for key, value in sorted(selector["matchLabels"].items()))

    def pods(self, deployment):
        return self.kube(
            "get", "pods", "--selector", self.selector(deployment), "-o", "json", json_output=True,
        )["items"]

    def assert_no_active_jobs(self):
        jobs = self.kube(
            "get", "jobs", "--selector", f"{JOB_LABEL}=true", "-o", "json", json_output=True,
        )["items"]
        for job in jobs:
            if not any(
                condition["type"] in {"Complete", "Failed"} and condition["status"] == "True"
                for condition in job.get("status", {}).get("conditions", [])
            ):
                raise MaintenanceError("An earlier maintenance Job is still running; wait for it to terminate")

    def assert_paused(self):
        deployment, _ = self.deployment()
        if deployment["spec"].get("replicas") != 0 or self.pods(deployment):
            raise MaintenanceError("API replicas reappeared during maintenance; no deployment is permitted")

    def pause(self):
        deployment, _ = self.deployment()
        hpas = self.kube("get", "hpa", "-o", "json", json_output=True)["items"]
        if any(item["spec"]["scaleTargetRef"]["name"] == APP for item in hpas):
            raise MaintenanceError("Disable the API autoscaler before maintenance")
        cronjobs = self.kube("get", "cronjobs", "-o", "json", json_output=True)["items"]
        if any(
            not item["spec"].get("suspend", False)
            and APP in json.dumps(item["spec"])
            and "scale" in json.dumps(item["spec"])
            for item in cronjobs
        ):
            raise MaintenanceError("Suspend scheduled API scaling before maintenance")
        annotations = deployment["metadata"].get("annotations", {})
        original = annotations.get(REPLICAS_ANNOTATION, str(deployment["spec"]["replicas"]))
        if not re.fullmatch(r"\d{1,3}", original):
            raise MaintenanceError("Invalid saved API replica count")
        self.kube("annotate", "deployment", APP, f"{REPLICAS_ANNOTATION}={original}", "--overwrite")
        self.kube("scale", "deployment", APP, "--replicas=0")
        deadline = time.monotonic() + 300
        while self.pods(deployment):
            if time.monotonic() >= deadline:
                raise MaintenanceError("Timed out stopping API pods; API remains in maintenance")
            time.sleep(3)
        self.assert_paused()
        return int(original)

    def database_job(self, apply):
        self.assert_no_active_jobs()
        if apply:
            self.assert_paused()
        name = f"up3-cleanup-{uuid.uuid4().hex[:16]}"
        labels = {JOB_LABEL: "true"}
        shell = """
export PGHOST="$DB_SERVER" PGPORT=5432 PGSSLMODE=require PGCONNECT_TIMEOUT=10
case "$DB_SERVER" in
  *:*) export PGHOST="${DB_SERVER%:*}" PGPORT="${DB_SERVER##*:}" ;;
esac
exec psql -X --no-password --quiet --tuples-only --no-align \\
  --set ON_ERROR_STOP=1 --set apply="$CLEANUP_APPLY" --file /cleanup/cleanup.sql
"""
        secrets = {
            "DB_SERVER": "rds_instance_endpoint", "PGDATABASE": "database_name",
            "PGUSER": "database_username", "PGPASSWORD": "database_password",
        }
        environment = [
            {"name": key, "valueFrom": {"secretKeyRef": {
                "name": "rds-postgresql-instance-output", "key": value,
            }}} for key, value in secrets.items()
        ] + [{"name": "CLEANUP_APPLY", "value": str(apply).lower()}]
        job = {
            "apiVersion": "batch/v1", "kind": "Job",
            "metadata": {"name": name, "labels": labels},
            "spec": {
                "backoffLimit": 0, "activeDeadlineSeconds": 300, "ttlSecondsAfterFinished": 3600,
                "template": {
                    "metadata": {"labels": labels},
                    "spec": {
                        "automountServiceAccountToken": False, "restartPolicy": "Never",
                        "securityContext": {
                            "runAsNonRoot": True, "runAsUser": 2000, "runAsGroup": 2000,
                            "seccompProfile": {"type": "RuntimeDefault"},
                        },
                        "containers": [{
                            "name": "cleanup", "image": "postgres:18.6",
                            "command": ["/bin/sh", "-eu", "-c", shell], "env": environment,
                            "securityContext": {
                                "allowPrivilegeEscalation": False, "readOnlyRootFilesystem": True,
                                "capabilities": {"drop": ["ALL"]},
                            },
                            "resources": {
                                "requests": {"cpu": "50m", "memory": "64Mi"},
                                "limits": {"cpu": "1", "memory": "256Mi"},
                            },
                            "volumeMounts": [{"name": "cleanup", "mountPath": "/cleanup", "readOnly": True}],
                        }],
                        "volumes": [{"name": "cleanup", "configMap": {"name": name}}],
                    },
                },
            },
        }
        created = self.kube("create", "-f", "-", "-o", "json", data=job, json_output=True)
        config = {
            "apiVersion": "v1", "kind": "ConfigMap",
            "metadata": {
                "name": name, "labels": labels,
                "ownerReferences": [{"apiVersion": "batch/v1", "kind": "Job", "name": name,
                                     "uid": created["metadata"]["uid"]}],
            },
            "data": {"cleanup.sql": (ROOT / "scripts/up3-simulation-cleanup.sql").read_text()},
        }
        terminal = False
        try:
            self.kube("create", "-f", "-", data=config)
            deadline = time.monotonic() + 360
            while time.monotonic() < deadline:
                if apply:
                    self.assert_paused()
                status = self.kube("get", "job", name, "-o", "json", json_output=True).get("status", {})
                if status.get("succeeded") == 1:
                    terminal = True
                    if apply:
                        self.assert_paused()
                    report = json.loads(self.kube("logs", f"job/{name}"))
                    if not isinstance(report, dict) or set(report) != {
                        "orders", "versions", "submissions", "return_events", "retained_creation_receipts",
                    } or any(type(value) is not int or value < 0 for value in report.values()):
                        raise MaintenanceError("Invalid cleanup verification report")
                    print(json.dumps(report), flush=True)
                    return report
                if any(
                    item["type"] == "Failed" and item["status"] == "True"
                    for item in status.get("conditions", [])
                ):
                    terminal = True
                    raise MaintenanceError(f"Database maintenance failed; inspect Kubernetes Job {name}")
                time.sleep(3)
            raise MaintenanceError(f"Database maintenance timed out; inspect Kubernetes Job {name}")
        except (MaintenanceError, json.JSONDecodeError):
            if not terminal:
                self.kube("delete", "job", name, "--wait=true", "--timeout=60s")
            raise

    def resume(self, replicas):
        self.kube("scale", "deployment", APP, f"--replicas={replicas}")
        self.kube("rollout", "status", f"deployment/{APP}", "--timeout=300s")
        self.kube("annotate", "deployment", APP, f"{REPLICAS_ANNOTATION}-")

    def deploy(self, version, digest, aware, replicas):
        chart = ROOT / "helm_deploy" / APP
        chart_file = chart / "Chart.yaml"
        content, replacements = re.subn(
            r"(?m)^appVersion:.*$", f"appVersion: '{version}'", chart_file.read_text(),
        )
        if replacements != 1:
            raise MaintenanceError("Cannot set Helm application version")
        chart_file.write_text(content)
        command(["helm", "dependency", "update", str(chart)])
        arguments = [
            "helm", "upgrade", APP, str(chart), "--namespace", self.namespace,
            "--install", "--reset-values", "--history-max", "10", "--wait", "--timeout", "5m",
            "--values", str(ROOT / "helm_deploy/values-dev.yaml"),
            "--set-string", f"generic-service.image.tag={version}",
            "--set-string", f"version={version}", "--set-string", "global.environment=dev",
            "--post-renderer", "up3-pin-api-image",
        ]
        if replicas is not None:
            arguments += ["--set", f"generic-service.replicaCount={replicas}"]
        if not aware:
            arguments += ["--set", "generic-service.env.UP3_SIMULATION_ENABLED=false"]
        minute = int(hashlib.md5(APP.encode()).hexdigest()[:4], 16) % 60
        arguments += [
            "--set-string", f"generic-service.scheduledDowntime.startup={minute} 6 * * 1-5",
            "--set-string", f"generic-service.scheduledDowntime.shutdown={minute} 21 * * 1-5",
        ]
        allowlist_version = os.environ.get("HMPPS_IP_ALLOWLIST_GROUPS_VERSION")
        if allowlist_version:
            arguments += ["--set-string", f"generic-service.allowlist_version={allowlist_version}"]
        with tempfile.TemporaryDirectory(prefix="up3-helm-") as directory:
            environment = {
                **os.environ, "HELM_DATA_HOME": str(Path(directory) / "data"),
                "HELM_PLUGINS": str(Path(directory) / "data/plugins"),
                "UP3_API_IMAGE": f"{REPOSITORY}:{version}", "UP3_API_DIGEST": digest,
            }
            command(["helm", "plugin", "install", str(ROOT / "scripts/helm-api-image")], env=environment)
            allowlist = os.environ.get("HMPPS_IP_ALLOWLIST_GROUPS_YAML")
            if allowlist:
                path = Path(directory) / "allowlist.yaml"
                path.write_bytes(base64.b64decode(allowlist, validate=True))
                arguments += ["--values", str(path)]
            print(command(arguments, env=environment), flush=True)

    def execute(self, mode, version="", confirmation=""):
        deployment, container = self.deployment()
        self.assert_no_active_jobs()
        if mode == "preview":
            self.database_job(False)
            return
        if mode == "cleanup":
            if confirmation != CONFIRMATION:
                raise MaintenanceError(f"Cleanup requires confirmation: {CONFIRMATION}")
            replicas = self.pause()
            self.database_job(True)
            self.resume(replicas)
            return
        if not re.fullmatch(
            r"[A-Za-z0-9](?:[A-Za-z0-9_.-]{0,61}[A-Za-z0-9])?", version,
        ) or version in {"dev", "latest"}:
            raise MaintenanceError("Deploy requires a versioned image tag valid as a Kubernetes label, not dev/latest")
        aware, digest = image_metadata(f"{REPOSITORY}:{version}")
        if not aware and confirmation != CONFIRMATION:
            raise MaintenanceError(
                f"Legacy image requires cleanup first. Re-run with confirmation: {CONFIRMATION}"
            )
        current_aware = False
        if re.fullmatch(
            re.escape(REPOSITORY) + r"(?::[A-Za-z0-9_][A-Za-z0-9_.-]*)?@sha256:[0-9a-f]{64}",
            container["image"],
        ):
            current_aware, _ = image_metadata(container["image"])
        replicas = None
        saved = deployment["metadata"].get("annotations", {}).get(REPLICAS_ANNOTATION)
        status = deployment.get("status", {})
        complete = (
            status.get("observedGeneration") == deployment["metadata"]["generation"]
            and status.get("updatedReplicas", 0) == deployment["spec"]["replicas"]
            and status.get("availableReplicas", 0) == deployment["spec"]["replicas"]
        )
        if complete and current_aware:
            pods = self.pods(deployment)
            complete = len(pods) == deployment["spec"]["replicas"] and all(
                not pod["metadata"].get("deletionTimestamp") and any(
                    item["name"] == APP and item["image"] == container["image"]
                    for item in pod["spec"]["containers"]
                ) for pod in pods
            )
        if not aware or not current_aware or saved is not None or not complete:
            replicas = self.pause()
        if not aware:
            self.database_job(True)
        self.deploy(version, digest, aware, replicas)
        if replicas is not None:
            self.resume(replicas)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("mode", choices=["preview", "cleanup", "deploy"])
    args = parser.parse_args()
    try:
        Maintenance(os.environ.get("KUBE_NAMESPACE", "")).execute(
            args.mode, os.environ.get("APP_VERSION", ""), os.environ.get("CLEANUP_CONFIRMATION", ""),
        )
    except (MaintenanceError, json.JSONDecodeError, ValueError, KeyError) as error:
        print(f"UP3 maintenance failed: {error}. No automatic rollback or replica recovery was attempted.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
