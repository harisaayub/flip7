# Speedometer

A native Android speedometer app. No ads, no accounts, no bundled maps —
just your current speed, read straight from GPS.

## Features

- Live speed reading from `LocationManager` GPS (falls back to distance/time
  between fixes if a provider doesn't report speed directly)
- Animated analog-style gauge with color-coded zones (green/amber/red)
- mph / km/h toggle, persisted across launches
- Session max-speed tracker with a one-tap reset
- GPS signal indicator (strong / fair / weak / searching) based on fix accuracy
- Keeps the screen on while the app is in the foreground
- No Google Play Services dependency — works on any Android 8.0+ (API 26) device
  with a GPS chip

## Building

Requires Android Studio (Koala+) or the command line with an Android SDK
installed (`ANDROID_HOME` set, platform 34 + build-tools installed).

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```

Install to a connected device/emulator:

```bash
./gradlew installDebug
```

## Getting a build without a computer

Every push to a `claude/**` branch (or `main`) triggers
`.github/workflows/android-debug-build.yml`, which builds a debug APK on
GitHub's runners and publishes it as a GitHub Release asset tagged
`dev-build-<branch-name>` — that release is overwritten on each push, so
there's always one current build per branch.

To install on your phone with nothing but a browser:

1. Open the repo's **Releases** page (or the *Actions* tab → latest run →
   `dev-build-...`) in a mobile browser and download the `app-debug.apk`
   asset.
2. Open the downloaded file. Android will prompt to allow installs from
   whichever app you downloaded it with (Chrome, Files, etc.) the first
   time — allow it, then install.
3. It's a debug-signed build, so no Play Store account or code signing is
   needed; just re-download and reinstall after each new push to pick up
   changes.

## How it works

- `SpeedTracker` requests location updates from the GPS provider (falling
  back to network) and exposes speed as a `StateFlow<SpeedState>`.
- `SpeedUnit` holds the mph/km/h conversion factors and each unit's gauge
  full-scale value.
- `UnitsStore` persists the selected unit via Jetpack DataStore.
- `MainActivity` wires permission handling (runtime `ACCESS_FINE_LOCATION` /
  `ACCESS_COARSE_LOCATION`) and lifecycle-based start/stop of location
  updates to the Compose UI in `SpeedometerScreen` / `SpeedGauge`.

## Notes

This project was scaffolded and written in a sandboxed environment without
an Android SDK installed, so it hasn't been compiled here. The Gradle
wrapper is included and pinned to Gradle 8.14.3 / AGP 8.5.2 / Kotlin 2.0.21;
open it in a recent Android Studio and it should sync and build without
changes. Test on a real device — emulators generally don't produce a live
GPS signal without a manually configured mock route.
