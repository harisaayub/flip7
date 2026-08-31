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
