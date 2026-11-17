# Nova Music Player

A native Kotlin + Jetpack Compose offline music-player starter with a Media3 playback service.

## Included in this codebase
- Runtime audio-library permission handling (Android 13+ and legacy Android).
- Local MediaStore scanning for music tracks, with a configurable minimum duration.
- Search across title, artist and album.
- Dark AMOLED-inspired Compose library UI and bottom navigation shell.
- Media3 ExoPlayer + MediaSessionService for background playback, audio focus and system media notification/session integration.
- Headphone-unplug auto-pause through Media3 `setHandleAudioBecomingNoisy(true)`.

## Important scope and production notes
This is a buildable starter, **not** a claim that every feature in the original specification is finished. Playlist persistence/editor, folder exclusion UI, tag writing, LRC synchronization, equalizer/virtualizer controls, crossfade, sleep timer, drag-reorder queue, artwork extraction/dynamic theming, visualizer, Bluetooth routing tests and complete QA still need implementation. The UI currently provides a Songs-first library shell; the other tabs are navigation placeholders.

The app scans MediaStore-supported audio entries. It does not bypass Android scoped-storage rules or promise access to every arbitrary file. A production release should add Room persistence, migration tests, robust error handling, accessibility/localization, instrumentation tests, privacy policy and device/API-level testing.

## Build
Requirements:
- Android Studio Ladybug or newer
- JDK 17
- Android SDK Platform 35
- Android Gradle Plugin 8.7.3

Open this folder in Android Studio, allow Gradle sync, then choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

CLI (after installing Android SDK and generating/adding the Gradle wrapper):
```bash
gradle :app:assembleDebug
```
APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Package
`com.nova.music`

No signing key is included. For Play Store or distribution, create a private release keystore and configure release signing locally. Never commit the keystore or passwords.
