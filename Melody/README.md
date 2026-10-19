# Melody Android starter

A small, original offline Android music-player starter built with Kotlin, Jetpack Compose, and Media3 ExoPlayer. It scans the device's music library and supports basic play/pause/next playback.

## Build
1. Install Android Studio and Android SDK Platform 35.
2. Open this `Melody` folder in Android Studio.
3. Allow Gradle to sync and download dependencies.
4. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

This ZIP is source code, not a prebuilt APK. The project requires internet access during the first Gradle sync. It is an original starter, not a Spotify clone, and does not include all production features (background playback service/media notification, playlists, artwork, and persistent favorites).


## Security hardening in this audited source revision
- Disabled Android app-data backup (`allowBackup=false`) to reduce accidental exposure of app-private data.
- Explicitly disabled cleartext HTTP traffic (`usesCleartextTraffic=false`); this app is intended to play local audio and does not need HTTP.
- Updated AndroidX Media3 ExoPlayer and Common from 1.5.1 to 1.11.1 (stable release listed by Android Developers as of October 2026).

## Audit limitations
This archive is Android Studio source code, not an installable APK. It has not been compiled, signed, installed, dynamically tested, or submitted to a malware reputation service in this environment. Build and run tests in Android Studio with the Android SDK and network access before distributing it.
