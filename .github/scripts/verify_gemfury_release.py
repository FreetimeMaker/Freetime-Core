"""Verify anonymous access to unified Maven publications before tagging a release."""

import argparse
import hashlib
import json
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
    files = set()
    for module in MODULES:
        directory = Path("com/freetime") / module / version
        prefix = f"{module}-{version}"

        def require(filename, kind):
            relative = directory / filename
            path = staging / relative
            if not path.resolve().is_relative_to(staging.resolve()):
                raise ValueError(f"Unsafe staged artifact path: {path}")
            if not path.is_file():
                raise ValueError(f"Missing staged {kind}: {path}")
            files.add(relative)
            return path

        metadata_path = require(f"{prefix}.module", "module metadata")
        for suffix in (".pom", ".jar", "-android.aar", "-desktop.jar"):
            require(prefix + suffix, "main artifact")
        try:
            metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
        except (ValueError, UnicodeError) as error:
            raise ValueError(
                f"Invalid module JSON: {metadata_path}: {error}"
            ) from error
        if not isinstance(metadata, dict) or metadata.get("formatVersion") != "1.1":
            raise ValueError(
                f"Invalid module formatVersion (expected 1.1): {metadata_path}"
            )
        component = metadata.get("component")
        if not isinstance(component, dict) or any(
            component.get(key) != value
            for key, value in {
                "group": "com.freetime",
                "module": module,
                "version": version,
            }.items()
        ):
            raise ValueError(
                f"Invalid module component (expected com.freetime:{module}:{version}): {metadata_path}"
            )
        variants = metadata.get("variants")
        if not isinstance(variants, list) or not variants:
            raise ValueError(f"Missing module variants: {metadata_path}")
        targets = set()
        for variant in variants:
            if not isinstance(variant, dict) or "available-at" in variant:
                raise ValueError(
                    f"Unexpected variant or available-at redirect: {metadata_path}"
                )
            attributes = variant.get("attributes")
            if not isinstance(attributes, dict):
                raise ValueError(f"Missing variant attributes: {metadata_path}")
            platform = attributes.get("org.jetbrains.kotlin.platform.type")
            usage = attributes.get("org.gradle.usage")
            if platform not in ("common", "androidJvm", "jvm") or not isinstance(
                usage, str
            ):
                raise ValueError(
                    f"Invalid variant platform/usage attributes: {metadata_path}"
                )
            entries = variant.get("files")
            if not isinstance(entries, list) or not entries:
                raise ValueError(f"Missing variant files: {metadata_path}")
            urls = set()
            for entry in entries:
                url = entry.get("url") if isinstance(entry, dict) else None
                if not isinstance(url, str) or not re.fullmatch(
                    re.escape(prefix) + r"(?:-[A-Za-z0-9_-]+)?\.[A-Za-z0-9]+", url
                ):
                    raise ValueError(
                        f"Unsafe or non-root artifact URL {url!r}: {metadata_path}"
                    )
                urls.add(url)
                require(url, "metadata-referenced artifact")
            expected = (
                prefix
                + {
                    "common": ".jar",
                    "androidJvm": "-android.aar",
                    "jvm": "-desktop.jar",
                }[platform]
            )
            common_metadata = platform == "common" and usage == "kotlin-metadata"
            if attributes.get("org.gradle.category") != "documentation" and (
                common_metadata
                or usage in ("kotlin-api", "java-api", "kotlin-runtime", "java-runtime")
            ):
                if expected not in urls:
                    raise ValueError(
                        f"Missing {platform} main artifact reference: {metadata_path}"
                    )
                role = "api" if common_metadata or usage.endswith("-api") else "runtime"
                targets.add((platform, role))
        required = {
            ("common", "api"),
            ("androidJvm", "api"),
            ("androidJvm", "runtime"),
            ("jvm", "api"),
            ("jvm", "runtime"),
        }
        if not required.issubset(targets):
            raise ValueError(
                f"Missing target API/runtime variants {sorted(required - targets)}: {metadata_path}"
            )
    return sorted(files)


def verify_release(staging, version, attempts=3):
    staging = Path(staging)
    files = release_files(staging, version)
    for relative in files:
        expected = hashlib.sha256((staging / relative).read_bytes()).digest()
        url = REPOSITORY + relative.as_posix()
        for attempt in range(attempts):
            try:
                # No token: consumers must resolve the same bytes anonymously.
                with urllib.request.urlopen(url, timeout=60) as response:
                    actual = hashlib.sha256(response.read()).digest()
                if actual != expected:
                    raise ValueError(f"Published content differs from staging: {url}")
                break
            except (OSError, ValueError) as error:
                if attempt + 1 == attempts:
                    kind = (
                        "module metadata"
                        if relative.suffix == ".module"
                        else "artifact"
                    )
                    raise ValueError(
                        f"Anonymous {kind} verification failed: {url}: {error}. "
                        "Check repository routing/visibility; upload success does not prove public access."
                    ) from error
                time.sleep(5)
        print(f"Verified: {url}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version")
    parser.add_argument("--staging", type=Path, default=Path("build/public-maven"))
    parser.add_argument(
        "--local-only",
        action="store_true",
        help="Validate staging without network access",
    )
    args = parser.parse_args()
    try:
        if args.local_only:
            files = release_files(args.staging, args.version)
            print(
                f"Validated {len(files)} files across {len(MODULES)} root publications"
            )
        else:
            verify_release(args.staging, args.version)
    except ValueError as error:
        parser.error(str(error))


if __name__ == "__main__":
    main()
