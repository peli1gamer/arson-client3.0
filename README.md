# Arson Client V3

A modular Fabric client foundation for Minecraft 26.2.

## Current foundation

- Fabric 26.2 / Java 25
- Central module registry
- Module categories
- Enable/disable lifecycle
- Right Shift menu keybind
- Client menu that works from the title screen and in-game
- Clean separation between client entrypoint, modules, and UI
- Configuration-friendly structure for future settings

## Development direction

The V3 architecture is intentionally modular. New systems should live behind focused managers/modules instead of growing one monolithic client class.

Planned areas include:

- Click GUI and themes
- Per-module settings
- HUD framework
- Render utilities
- Storage/entity information overlays
- Player/world utilities
- Combat and movement modules where appropriate
- Config persistence
- Addon/API layer
- Performance profiling and diagnostics

The project prioritizes predictable behavior, low overhead, compatibility, and isolated module failures. It does not implement anti-cheat evasion or detection-bypass mechanisms.

## Build

Use Java 25 and a current Gradle installation, then run:

```text
gradle build
```

Fabric's current 26.2 documentation recommends Loom 1.17, Gradle 9.5.1, and Java 25 for this target.
