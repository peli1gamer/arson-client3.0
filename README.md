# Arson Client V3

A modular Fabric client for Minecraft 1.21.11, built for Java 21.

## Current capabilities

- Module registry with categories, enable/disable lifecycle, keybinds, favorites, and per-module settings
- Orange-themed module grid and settings pages
- Persistent module, setting, profile, and HUD layout configuration
- HUD widgets with layout editing, formatting, and saved positions
- Entity, item, block, and storage visualization
- Player, world, render, server, and combat telemetry modules
- Client-side `/arson` commands and a public `ArsonApi` facade
- Fabric client runtime smoke check and packaged JAR verification in GitHub Actions

## Storage ESP

Storage ESP indexes containers only in chunks already loaded by the Minecraft client. It uses the client render-distance setting as its search boundary and checks that a chunk is available before reading it; it never requests an unloaded chunk. The index updates as chunks and block entities load or unload, with a periodic refresh to catch changes.

The renderer always draws storage boxes, outlines, tracers, and enabled labels through terrain. Nearby targets can be summarized as storage clusters. The target range setting filters what is shown after scanning the loaded chunk boundary.

Supported built-in types include chests, barrels, shulker boxes, ender chests, hoppers, dispensers, and droppers. To include other blocks, enter comma-separated registry IDs in the Container ESP **Custom Block IDs** setting, for example:

```text
minecraft:chest, examplemod:storage_crate
```

Custom matches are read from loaded block entities. Enable **Other and Custom Storage** to render those matches. You can change settings in the ClickGUI or with `/arson setting <module> <setting> <value>`.

## Render architecture

World visualization is separated into three stages:

1. Discovery reads client-available world objects and entities.
2. Render commands convert targets into lightweight render data.
3. Drawing consumes that data from the Fabric world-render lifecycle.

This keeps scanning separate from rendering and makes the visual modules easier to test and extend. World ESP layers use no-depth rendering so qualifying targets remain visible through blocks.

## Commands and API

Run `/arson help` in game for the current command tree. Commands cover module discovery and control, settings, categories, HUD formatting, configuration, profiles, and ClickGUI preferences.

Addons can use `ArsonApi` for module discovery, settings, HUD positions, favorites, server summaries, and waypoint telemetry.

## Build and verification

Use Java 21 with Gradle:

```text
gradle build
```

GitHub Actions runs focused tests, a Fabric client runtime smoke check, the complete remapped build, and packaged JAR inspection on pushes and pull requests.
