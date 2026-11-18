"""Verify anonymous access to every staged KMP publication before tagging a release."""

import argparse
import hashlib
import re
import time
import urllib.request
from pathlib import Path

MODULES = ("Core", "Design", "Browser", "Donations", "FreetimeWarn", "Notifications")
REPOSITORY = "https://repo.free-time.me/maven/"


def release_files(staging, version):
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:[.-][0-9A-Za-z.-]+)?", version):
        raise ValueError(f"Invalid release version: {version}")
    staging = Path(staging)
    files = []
    for module in MODULES:
        for target in ("", "-android", "-desktop"):
            artifact = f"{module}{target}"
            directory = Path("com/freetime") / artifact / version
            prefix = f"{artifact}-{version}"
            for extension in (
                "pom",
                "module",
                "aar" if target == "-android" else "jar",
            ):
                path = staging / directory / f"{prefix}.{extension}"
                if not path.is_file():
                    raise ValueError(f"Missing staged release file: {path}")
            files.extend(
                path.relative_to(staging)
                for path in (staging / directory).rglob("*")
                if path.is_file() and not path.name.endswith(
                    (".md5", ".sha1", ".sha256", ".sha512")
                )
            )
    return sorted(files)


def verify_release(staging, version, attempts=3):
    files = release_files(staging, version)
    for relative in files:
        expected = hashlib.sha256((staging / relative).read_bytes()).digest()
        url = REPOSITORY + relative.as_posix()
        for attempt in range(attempts):
            try:
                # No token: consumers must be able to resolve every target and metadata file.
                with urllib.request.urlopen(url, timeout=60) as response:
                    actual = hashlib.sha256(response.read()).digest()
                if actual != expected:
                    raise ValueError(f"Published content differs from staging: {url}")
                break
            except (OSError, ValueError) as error:
                if attempt + 1 == attempts:
                    raise ValueError(
                        f"Anonymous KMP artifact verification failed: {url}. "
                        "KMP .module metadata and every target/classifier are required; "
                        "an accepted upload does not prove visibility. Native Maven publishing "
                        "cannot force public visibility. Check repository routing and visibility."
                    ) from error
                time.sleep(5)
        print(f"Verified: {url}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version")
    parser.add_argument("--staging", type=Path, default=Path("build/public-maven"))
    args = parser.parse_args()
    verify_release(args.staging, args.version)


if __name__ == "__main__":
    main()
