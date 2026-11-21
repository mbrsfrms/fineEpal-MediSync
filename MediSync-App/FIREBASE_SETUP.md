# Firebase setup for Google Sign-In (do this once)

The project includes the Google Sign-In and Firebase Authentication source integration, but it cannot authenticate real users until you connect **your own** Firebase project and test it on an Android device or Google Play emulator.
No project-specific client IDs or credentials are included. They are generated from the `app/google-services.json` file you download from Firebase.
Without that file, the app should display a setup message instead of pretending that sign-in succeeded.

Package name (already set in `app/build.gradle`): **`com.medisync.app`**

## 1. Create the Firebase project and register the app
1. Open the [Firebase Console](https://console.firebase.google.com) and create a project (or pick one).
2. Project overview > **Add app** > Android. Package name: `com.medisync.app`. Nickname: MediSync.
3. Skip the SDK steps; the Gradle files are already configured.

## 2. Add your signing fingerprints (SHA-1 and SHA-256)
Google rejects sign-in from an app whose signing certificate it does not know. Add **every** certificate you run with.

Debug build (Android Studio Run button), from the project folder:
```
./gradlew :app:signingReport
```
(on Windows: `gradlew.bat :app:signingReport`). Copy the **SHA-1** and **SHA-256** of the `debug` variant.
If the wrapper is missing, use `keytool`:
```
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```
Firebase Console > Project settings > Your apps > MediSync > **Add fingerprint**. Add both values.

Release build: add the SHA-1 and SHA-256 of your release keystore. If you publish with Google Play App Signing, also add
the "App signing key certificate" fingerprints from Play Console > Setup > App signing.

## 3. Enable the Google provider
Firebase Console > **Authentication** > Get started > **Sign-in method** > **Google** > Enable.
Choose a project support email and Save. (Leave Email/Password disabled; MediSync no longer uses it.)

## 4. Download `google-services.json`
Do this **after** steps 2 and 3. Project settings > Your apps > MediSync > **Download google-services.json**.
Put it in **`MediSync/app/`** (next to `app/build.gradle`), then Sync Gradle.

Check that the file's `oauth_client` list contains an entry with `"client_type": 3`. That is the web client ID the
app uses (`default_web_client_id`). If it is missing, the Google provider was not enabled or no SHA-1 was added
before you downloaded; fix steps 2 to 3 and download the file again.

## 5. Run on a device that can sign in
- A real phone or an emulator image **with Google Play** (not a plain "Google APIs" image), with Google Play services up to date.
- At least one Google account added in the device's Settings (the picker also offers to add one).
- Internet access.

## Troubleshooting
| Symptom | Cause |
|---|---|
| Button shows "Firebase is not set up in this build" | `app/google-services.json` is missing. Add it and rebuild. |
| Button shows "Google sign-in is not enabled for this Firebase project" | The JSON has no web client. Redo steps 2 to 4. |
| Account picker closes with a generic failure, or "developer error" in Logcat | The SHA-1/SHA-256 of the build you are running is not registered (or the package name differs). |
| "No Google account is available" | No Google account on the device. |
| "Google sign-in isn't available on this device" | Google Play services missing or outdated (emulator without Google Play). |

## Optional hardening
- Do not commit `google-services.json` (it is listed in `.gitignore`). It holds no password, but it is per-developer.
- In Google Cloud Console > APIs & Services > Credentials you can restrict the Android API key to your package name and SHA-1.
