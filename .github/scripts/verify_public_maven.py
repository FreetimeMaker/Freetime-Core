"""Reject the retired AAR/POM-only verification contract."""


def main():
    raise SystemExit(
        "Legacy AAR/POM-only verification is disabled: it cannot verify a KMP release. "
        "Use a Maven-compatible repository and verify all staged publications, including "
        ".module metadata, target artifacts and referenced files. Do not use this "
        "legacy check as a release-tag gate."
    )


if __name__ == "__main__":
    main()
