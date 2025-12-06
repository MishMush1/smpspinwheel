# Chat Wheel (Fabric 1.21.1)
This mod listens to chat for messages like `PlayerXYZ paid you $25K.` and builds a wheel of payers.
When a configured target total is reached the mod opens an in-game spinning wheel and selects a winner.

## How to build
- Install Java 17 and Gradle.
- Run `./gradlew build` (or use your IDE import).
- The mod jar will be in `build/libs`.

## Notes
- This project contains simple, unskinned wheel rendering. Tweak visuals in `WheelScreen.java`.
- Update versions in `build.gradle` if needed.