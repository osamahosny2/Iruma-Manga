# Iruma Manga 1.4.0 migration checklist

## Implemented in the first migration commit
- Forked the complete Kotatsu Next Android application rather than only its parser library.
- Set launcher app name to إيروما مانجا.
- Set versionName=1.4.0 and versionCode=10400.
- Used a separate package suffix to preserve the installed 1.3.1 app during side-by-side testing.
- Added a GitHub Actions debug build and unit-test workflow.
- Preserved the upstream GPL-3.0 license and attribution.

## Required before calling 1.4.0 complete
- Import the exact Iruma Manga launcher artwork into Android launcher resources.
- Implement the custom library status categories and preserve their exact semantics, including the temporary منزلها return-to-previous-category behavior.
- Define and test a safe import/backup path from the old app; Kotatsu's database is not schema-compatible with the prior AI Studio app.
- Configure stable release signing via private GitHub Actions secrets. Do not commit a keystore or passwords.
- Implement server-backed authentication and server-verified Admin authorization for hosnyosama61@gmail.com; do not grant admin from a local email comparison.
- Test source search, manga details, chapters, covers, reader, downloads, backup/restore and category migration on-device.
- Do not claim Play Protect warnings can be bypassed. The APK should be built transparently and signed consistently.
