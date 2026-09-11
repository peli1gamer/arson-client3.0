# Arson Client V3

A modular Fabric client foundation for Minecraft 1.21.11.

## Current foundation

- Fabric 1.21.11 / Java 21 target
- Central module registry
- Module categories
- Enable/disable lifecycle
- Right Shift menu keybind
- Client menu that works from the title screen and in-game
- Clean separation between client entrypoint, modules, and UI
- Configuration-friendly structure for future settings
- Renderer-agnostic render command pipeline
- Per-storage-type render profiles and colors
- Configurable storage overlay range

## Render architecture

The render layer is split into three stages:

1. **Discovery** finds relevant world objects/entities.
2. **Render commands** convert discovered objects into lightweight `RenderBox` data.
3. **Drawing** consumes those commands from the Minecraft/Fabric render lifecycle.

This keeps world scanning separate from drawing and makes visual modules easier to test and extend.

Storage profiles currently support:

- Chest
- Barrel
- Shulker
- Ender Chest
- Other storage

Each category has an independent enabled state and color.

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

The project prioritizes predictable behavior, low overhead, compatibility, and isolated module failures.

## Build

Use Java 21 with a current Gradle installation, then run:

```text
gradle build
```
