# إيروما مانجا — Iruma Manga

This repository is the Iruma Manga adaptation branch based on Kotatsu Next.

- Upstream source: https://github.com/Ero-gamer/Kotatsu-Next
- Upstream base branch: dev3
- License: GNU GPL v3.0. Preserve the upstream copyright notices and license.
- App version: 1.4.0 (versionCode 10400).
- This migration build uses a separate applicationId (com.aistudio.irumamanga.rkfpxq.next) so it can be tested alongside the previously installed Iruma Manga build without uninstalling it. It is not yet an in-place update of the old package.
- The current commit is only the first migration step: app branding and version identity. Library-category migration, the temporary منزلها workflow, custom launcher artwork, and secure server-backed Admin authentication still require implementation and tests. No fake local-only Admin role is provided.
- Play Protect cannot be bypassed by the app. Stable future updates require a stable signing certificate; CI release signing must use a private keystore stored as GitHub Actions secrets, not committed to this repository.

Do not remove the GPL license or upstream notices. Changes to a distributed derivative must comply with GPL-3.0.
