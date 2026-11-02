# Releasing

Releases are built and published by GitHub Actions. A pushed tag is the only
thing that publishes.

## One-time setup: the release key

Every install of the app is tied to the signing key, so this key must be created
once and kept forever. Losing it means users have to uninstall and reinstall.

```sh
keytool -genkeypair -v \
  -keystore release.jks -alias sithakuru \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=Sithakuru, O=Sithakuru, C=LK"
```

Back the `.jks` file up somewhere safe (never in this repository — `*.jks` and
`keystore.properties` are gitignored).

Add four repository secrets under **Settings → Secrets and variables → Actions**:

| Secret              | Value                                              |
| ------------------- | -------------------------------------------------- |
| `KEYSTORE_BASE64`   | `base64 -w0 release.jks`                           |
| `KEYSTORE_PASSWORD` | the `storepass` you chose                          |
| `KEY_ALIAS`         | `sithakuru`                                        |
| `KEY_PASSWORD`      | the `keypass` you chose                            |

If `KEYSTORE_BASE64` is missing, builds still succeed locally but are signed
with a throwaway debug key; the release job refuses to publish such an APK.

### Building locally

Create an untracked `keystore.properties` in the repo root:

```properties
storeFile=app/release.jks
storePassword=...
keyAlias=sithakuru
keyPassword=...
```

## Cutting a release

1. Bump `versionCode` (must increase) and `versionName` in
   `app/build.gradle.kts`.
2. Add the user-facing notes as `docs/release-notes/<versionName>.md`. That file
   becomes the release body; without it GitHub generates notes from commits.
3. Commit, then tag and push:

   ```sh
   git tag -a v2.0.1 -m "2.0.1"
   git push origin main
   git push origin v2.0.1
   ```

4. The workflow builds `app-sithakuru-release.apk`, renames it to
   `sithakuru-<version>.apk`, writes a `.sha256` next to it, and publishes both
   on the tag. Watch it under **Actions → Build APK**.

Verify a published APK yourself:

```sh
apksigner verify --print-certs sithakuru-2.0.1.apk
sha256sum -c sithakuru-2.0.1.apk.sha256
```

The certificate SHA-256 must match the release key every time. A mismatch means
an APK was signed with something other than `release.jks` — do not ship it.

## Before the first release

The app changed its Singlish engine, so smoke-test a debug build on a device:
`ch`, `Ch`, `ksh`, `sRu`, `NG`, repaya, yansaya, rakaransaya, plus emoji and
symbol input followed by more Singlish typing.
