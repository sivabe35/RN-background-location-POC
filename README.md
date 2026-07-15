# RN-background-location-POC

Reference implementation (POC) for background location tracking in React Native 0.82

This repository contains a production-oriented proof-of-concept demonstrating continuous background location tracking for both Android and iOS. It includes:

- A TypeScript wrapper to start/stop tracking, update interval, and subscribe to location events.
- Android native module (Kotlin) + ForegroundService using FusedLocationProviderClient.
- iOS native module (Swift) using CLLocationManager with background update throttling.
- Example React Native app under /example showing how to use the API.

Important
- Bundle / application IDs used in native code:
  - Android: com.sivarn.backgroundlocation
  - iOS: com.sivarn.backgroundlocation

See README.md for setup and build instructions.
