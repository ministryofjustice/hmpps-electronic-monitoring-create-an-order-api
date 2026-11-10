#!/usr/bin/env python3
"""Helm 4 post-renderer: deploy the exact API image inspected by the maintenance gate."""

import os
import re
import sys


def pin_manifest(manifest, image, digest):
    if not re.fullmatch(
        r"ghcr\.io/ministryofjustice/hmpps-electronic-monitoring-create-an-order-api:[A-Za-z0-9_][A-Za-z0-9_.-]*",
        image,
    ) or not re.fullmatch(r"sha256:[0-9a-f]{64}", digest):
        raise ValueError("Invalid verified API image reference")
    result, count = re.subn(
        r"(?m)^([ \t]*image:[ \t]*[\"']?)" + re.escape(image) + r"([\"']?[ \t]*)$",
        lambda match: match[1] + image + "@" + digest + match[2],
        manifest,
    )
    if not count:
        raise ValueError("Rendered Helm deployment did not contain the verified API image")
    return result


if __name__ == "__main__":
    try:
        sys.stdout.write(pin_manifest(sys.stdin.read(), os.environ["UP3_API_IMAGE"], os.environ["UP3_API_DIGEST"]))
    except (KeyError, ValueError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
