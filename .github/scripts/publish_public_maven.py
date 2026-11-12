"""Upload the real Gradle release artifacts to Gemfury with public=true."""

import argparse
import io
import json
import os
import re
import urllib.error
import urllib.request
import uuid
import xml.etree.ElementTree as ET
import zipfile
from pathlib import Path

from verify_public_maven import MODULES, REPOSITORY

ROOT = Path(__file__).resolve().parents[2]
ACCOUNT = "freetimemaker"
GROUP = "com.freetime"
NAMESPACE = {"m": "http://maven.apache.org/POM/4.0.0"}


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        # Never forward the API token to a redirected host.
        return None


def read_token():
    token = os.environ.get("FURY_TOKEN")
    if token:
        return token
    gradle_home = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle"))
    for path in (ROOT / "gradle.properties", gradle_home / "gradle.properties"):
        if path.is_file():
            for line in path.read_text().splitlines():
                if line.lstrip().startswith(("#", "!")) or "=" not in line:
                    continue
                key, value = line.split("=", 1)
                if key.strip() == "furyToken" and value.strip():
                    return value.strip()
    raise ValueError("Set FURY_TOKEN or furyToken in ~/.gradle/gradle.properties")


def prepare_archive(path, pom, module, version):
    metadata = f"META-INF/maven/{GROUP}/{module}/"
    output = io.BytesIO()
    with zipfile.ZipFile(path) as source, zipfile.ZipFile(output, "w") as target:
        for entry in source.infolist():
            if entry.filename not in (
                metadata + "pom.xml",
                metadata + "pom.properties",
            ):
                target.writestr(entry, source.read(entry))
        target.writestr(zipfile.ZipInfo(metadata + "pom.xml"), pom)
        target.writestr(
            zipfile.ZipInfo(metadata + "pom.properties"),
            f"groupId={GROUP}\nartifactId={module}\nversion={version}\n",
        )
    return output.getvalue()


def prepare_release(staging, modules, version):
    artifacts = []
    for module in modules:
        directory = staging / "com" / "freetime" / module / version
        prefix = f"{module}-{version}"
        pom = (directory / f"{prefix}.pom").read_bytes()
        root = ET.fromstring(pom)
        expected = {
            "groupId": GROUP,
            "artifactId": module,
            "version": version,
            "packaging": "aar",
        }
        for key, value in expected.items():
            if root.findtext(f"m:{key}", namespaces=NAMESPACE) != value:
                raise ValueError(f"Unexpected {key} in {directory / (prefix + '.pom')}")
        path = directory / (prefix + ".aar")
        artifacts.append(
            (module, path.name, prepare_archive(path, pom, module, version))
        )
    return artifacts


def archive_contents(archive):
    with zipfile.ZipFile(io.BytesIO(archive)) as source:
        return {
            entry.filename: source.read(entry)
            for entry in source.infolist()
            if not entry.is_dir()
        }


def matching_public_release(module, filename, archive, version):
    url = f"{REPOSITORY}com/freetime/{module}/{version}/{filename}"
    try:
        with urllib.request.urlopen(url, timeout=30) as response:
            existing = response.read()
        return archive_contents(existing) == archive_contents(archive)
    except (OSError, zipfile.BadZipFile):
        return False


def upload(filename, archive, token):
    # Source: gemfury/cli api/push.go, PushPkg: multipart public=true and file.
    # Embedded Maven POMs let Gemfury identify coordinates without inventing versions.
    boundary = "freetime-" + uuid.uuid4().hex
    body = (
        (
            f'--{boundary}\r\nContent-Disposition: form-data; name="public"\r\n\r\ntrue\r\n'
            f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{filename}"\r\n'
            "Content-Type: application/octet-stream\r\n\r\n"
        ).encode()
        + archive
        + f"\r\n--{boundary}--\r\n".encode()
    )
    request = urllib.request.Request(
        f"https://push.fury.io/uploads?as={ACCOUNT}",
        data=body,
        method="POST",
        headers={
            "Authorization": token,
            "Accept": "application/vnd.fury.v1",
            "Content-Type": f"multipart/form-data; boundary={boundary}",
        },
    )
    # Do not retry a POST: a timed-out upload may already have been stored.
    try:
        with urllib.request.build_opener(NoRedirect()).open(
            request, timeout=180
        ) as response:
            result = json.load(response)
    except urllib.error.HTTPError as error:
        if error.code == 409:
            return False
        raise ValueError(
            f"Upload failed for {filename}: HTTP {error.code}. "
            "Check token permissions and whether this version already exists."
        ) from None
    if (
        not isinstance(result, dict)
        or not result.get("package_id")
        or not result.get("version_id")
    ):
        raise ValueError(
            f"Gemfury did not confirm a stored package/version for {filename}"
        )
    return True


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version")
    parser.add_argument("--staging", type=Path, default=ROOT / "build" / "public-maven")
    parser.add_argument("--module", choices=MODULES, action="append", dest="modules")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:[.-][0-9A-Za-z.-]+)?", args.version):
        parser.error("Expected a semantic release version")
    try:
        # Validate the complete release before making any remote changes.
        artifacts = prepare_release(args.staging, args.modules or MODULES, args.version)
        token = None if args.dry_run else read_token()
        for module, filename, archive in artifacts:
            if args.dry_run:
                print(f"Ready: {GROUP}:{module}:{args.version} / {filename}")
            else:
                if upload(filename, archive, token):
                    print(
                        f"Public upload accepted: {GROUP}:{module}:{args.version} / {filename}"
                    )
                elif matching_public_release(module, filename, archive, args.version):
                    print(
                        f"Already public and identical: {GROUP}:{module}:{args.version}"
                    )
                else:
                    raise ValueError(
                        f"Gemfury refused an existing version of {GROUP}:{module}:{args.version}. "
                        "The existing AAR is private, unavailable or has different contents. "
                        "Refusing to overwrite it; resolve visibility or publish a new release version."
                    )
        if not args.dry_run:
            print(
                f"Verify anonymous downloads at {REPOSITORY} before tagging the release."
            )
    except (OSError, ValueError, ET.ParseError, zipfile.BadZipFile) as error:
        raise SystemExit(str(error)) from None


if __name__ == "__main__":
    main()
