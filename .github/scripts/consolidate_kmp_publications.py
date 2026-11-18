#!/usr/bin/env python3
"""Inline native KMP publications into one classifier-based GAV per module.

Only referenced resolution artifacts are copied. POM generation belongs to the
parent publication. Validation of the entire input precedes any output writes;
individual output files are replaced atomically (not the whole repository).
"""

import argparse
import copy
import hashlib
import json
import os
import re
import tempfile
from pathlib import Path
from urllib.parse import urlsplit

MODULES = ("Core", "Design", "Browser", "Donations", "FreetimeWarn", "Notifications")
GROUP = "com.freetime"
_NUM = r"(?:0|[1-9][0-9]*)"
_PRE = r"(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)"
SEMVER = re.compile(
    rf"{_NUM}\.{_NUM}\.{_NUM}(?:-{_PRE}(?:\.{_PRE})*)?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?",
    re.ASCII,
)
HASHES = ("sha512", "sha256", "sha1", "md5")


def _local(base, url, boundary):
    if not isinstance(url, str) or not url or "\\" in url or "%" in url:
        raise ValueError(f"Invalid local URL: {url!r}")
    parsed = urlsplit(url)
    if (
        parsed.scheme
        or parsed.netloc
        or parsed.query
        or parsed.fragment
        or url.startswith("/")
    ):
        raise ValueError(f"Non-local URL: {url!r}")
    path = (base / url).resolve()
    if not path.is_relative_to(boundary):
        raise ValueError(f"URL escapes staging: {url}")
    return path


def _load(path, boundary, module, version, target=None):
    path = _local(path.parent, path.name, boundary)
    data = json.loads(path.read_text())
    component = data["component"]
    if (
        component.get("group") != GROUP
        or component.get("version") != version
        or component.get("module") not in {module, target}
    ):
        raise ValueError(f"Unexpected component/version in {path}")
    if "url" in component:
        expected = (
            boundary / "com/freetime" / module / version / f"{module}-{version}.module"
        )
        if _local(path.parent, component["url"], boundary) != expected:
            raise ValueError(f"Unexpected component URL in {path}")
    if data.get("formatVersion") != "1.1":
        raise ValueError(f"Unsupported metadata format in {path}")
    names = [v["name"] for v in data["variants"]]
    if len(names) != len(set(names)):
        raise ValueError(f"Duplicate variants in {path}")
    return data


def _remap(variant, modules):
    for key in ("dependencies", "dependencyConstraints"):
        for dep in variant.get(key, []):
            if dep.get("group") == GROUP:
                for module in modules:
                    if dep.get("module") in (module + "-android", module + "-desktop"):
                        dep["module"] = module
                        break


def consolidate(staging: Path, output: Path, version: str, modules=MODULES):
    """Return sorted output Paths; invalid input leaves output untouched.

    Staging and output must be disjoint. Existing unrelated output files are not
    deleted; callers should use a fresh directory for an exact repository tree.
    """
    if not isinstance(version, str) or not SEMVER.fullmatch(version):
        raise ValueError(f"Invalid SemVer: {version!r}")
    modules = tuple(modules)
    if len(set(modules)) != len(modules) or any(m not in MODULES for m in modules):
        raise ValueError("Modules must be unique known root artifact IDs")
    staging, output = Path(staging).resolve(), Path(output).resolve()
    if staging.is_relative_to(output) or output.is_relative_to(staging):
        raise ValueError("Staging and output must be disjoint")
    planned = {}
    for module in modules:
        relative = Path("com/freetime") / module / version
        directory = staging / relative
        root = _load(directory / f"{module}-{version}.module", staging, module, version)
        root = copy.deepcopy(root)
        root["component"].pop("url", None)
        artifacts = {}
        variants = []
        roles = set()
        for original in root["variants"]:
            variant = copy.deepcopy(original)
            source_dir = directory
            platform = None
            if "available-at" in variant:
                ref = variant["available-at"]
                target = ref.get("module")
                if (
                    ref.get("group") != GROUP
                    or ref.get("version") != version
                    or target not in (module + "-android", module + "-desktop")
                ):
                    raise ValueError(f"Unknown target: {ref}")
                platform = target.removeprefix(module + "-")
                target_path = _local(directory, ref["url"], staging)
                expected = (
                    staging
                    / "com/freetime"
                    / target
                    / version
                    / f"{target}-{version}.module"
                )
                if target_path != expected:
                    raise ValueError(f"Unexpected target path: {target_path}")
                metadata = _load(target_path, staging, module, version, target)
                name = original["name"].removesuffix("-published")
                matches = [
                    v
                    for v in metadata["variants"]
                    if v["name"].removesuffix("-published") == name
                ]
                if not matches:
                    matches = [
                        v
                        for v in metadata["variants"]
                        if v.get("attributes") == original.get("attributes")
                    ]
                if len(matches) != 1 or matches[0].get("attributes") != original.get(
                    "attributes"
                ):
                    raise ValueError(
                        f"Unknown/ambiguous target variant: {original['name']}"
                    )
                variant = copy.deepcopy(matches[0])
                variant["name"] = original["name"]
                source_dir = target_path.parent
            if "available-at" in variant:
                raise ValueError("Nested available-at is unsupported")
            attrs = variant.get("attributes", {})
            expected_type = {None: "common", "android": "androidJvm", "desktop": "jvm"}[
                platform
            ]
            sources = attrs.get("org.gradle.docstype") == "sources"
            if attrs.get("org.jetbrains.kotlin.platform.type") != expected_type or not (
                (sources and attrs.get("org.gradle.category") == "documentation")
                or (
                    attrs.get("org.gradle.category") == "library"
                    and attrs.get("org.gradle.usage")
                    in (
                        ("kotlin-metadata",)
                        if platform is None
                        else ("java-api", "java-runtime")
                    )
                )
            ):
                raise ValueError(f"Unknown variant: {variant['name']}")
            role = (platform, "sources" if sources else attrs.get("org.gradle.usage"))
            if role in roles:
                raise ValueError(f"Duplicate variant role: {variant['name']}")
            roles.add(role)
            files = variant.get("files", [])
            if len(files) != 1:
                raise ValueError(f"Expected one resolution artifact: {variant['name']}")
            file = files[0]
            payload = _local(source_dir, file["url"], staging).read_bytes()
            if "size" in file and file["size"] != len(payload):
                raise ValueError(f"Size mismatch: {file['url']}")
            digests = {h: hashlib.new(h, payload).hexdigest() for h in HASHES}
            for h in HASHES:
                if h in file and file[h] != digests[h]:
                    raise ValueError(f"{h} mismatch: {file['url']}")
            suffix = (f"-{platform}" if platform else "") + (
                "-sources" if sources else ""
            )
            extension = "aar" if platform == "android" and not sources else "jar"
            if Path(file["url"]).suffix != "." + extension:
                raise ValueError(f"Unexpected artifact extension: {file['url']}")
            filename = f"{module}-{version}{suffix}.{extension}"
            if filename in artifacts and artifacts[filename] != payload:
                raise ValueError(f"Conflicting artifact: {filename}")
            artifacts[filename] = payload
            file.update(name=filename, url=filename, size=len(payload), **digests)
            _remap(variant, MODULES)
            variants.append(variant)
        expected_names = {
            f"{module}-{version}{suffix}"
            for suffix in (
                ".jar",
                "-sources.jar",
                "-android.aar",
                "-android-sources.jar",
                "-desktop.jar",
                "-desktop-sources.jar",
            )
        }
        expected_roles = {(None, "kotlin-metadata"), (None, "sources")} | {
            (platform, role)
            for platform in ("android", "desktop")
            for role in ("java-api", "java-runtime", "sources")
        }
        if set(artifacts) != expected_names or roles != expected_roles:
            raise ValueError(f"Incomplete artifact/variant set for {module}")
        root["variants"] = variants
        artifacts[f"{module}-{version}.module"] = (
            json.dumps(root, indent=2) + "\n"
        ).encode()
        for name, payload in artifacts.items():
            destination = output / relative / name
            if not destination.resolve().is_relative_to(output):
                raise ValueError(f"Output symlink escapes destination: {destination}")
            planned[destination] = payload
    for path, payload in sorted(planned.items()):
        path.parent.mkdir(parents=True, exist_ok=True)
        temporary = None
        try:
            with tempfile.NamedTemporaryFile(dir=path.parent, delete=False) as stream:
                temporary = stream.name
                stream.write(payload)
            os.replace(temporary, path)
        finally:
            if temporary and os.path.exists(temporary):
                os.unlink(temporary)
    return sorted(planned)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version", metavar="VERSION")
    parser.add_argument("--staging", type=Path, default=Path("build/native-maven"))
    parser.add_argument("--output", type=Path, default=Path("build/unified-inputs"))
    args = parser.parse_args()
    try:
        paths = consolidate(args.staging, args.output, args.version)
    except (ValueError, OSError, KeyError, TypeError) as error:
        parser.exit(1, f"Consolidation failed: {error}\n")
    print(f"Wrote {len(paths)} files to {args.output}")


if __name__ == "__main__":
    main()
