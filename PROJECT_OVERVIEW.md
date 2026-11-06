# Arson Client V3 — Project Overview

_Last updated: 2026-11-06_

## What this project is

Arson Client V3 is a client-side Minecraft mod built on Fabric. It is organized around independently configurable modules, a ClickGUI, HUD elements, and a staged world-rendering pipeline.

The repository currently targets:

- Minecraft **1.21.11**
- Java **21**
- Fabric Loader **0.18.1**
- Fabric API **0.141.3+1.21.11**
- Gradle with Fabric Loom Remap plugin **1.14.10**

The mod metadata identifies the project as client-only and starts through io.arson.client.ArsonClient.

## Main capabilities present in the codebase

- A central module registry with categories, enable/disable state, favorites, keybinds, and per-module settings.
- A Right Shift keybind that opens the ClickGUI.
- A category-based, searchable ClickGUI with filtering, theme preferences, and settings controls.
- Boolean, numeric, color, enum, and string setting types.
- JSON configuration persistence and named profiles.
- A HUD pipeline and a HUD layout/editor system.
- World overlays for entities, items, blocks, and storage.
- Player, world, render, combat, and client telemetry modules.
- Client commands under the /arson command tree.
- A feature context shared between the Fabric adapter, tick lifecycle, and render lifecycle.

This list describes code present in the repository; it does not imply that every setting or every edge case has received a complete manual runtime audit.

## Repository shape

- src/main/java/io/arson/client/ — main client implementation.
- module/ — module base class, module manager, and feature modules.
- settings/ — setting models and setting groups.
- ui/ — ClickGUI, HUD editor, and layout models.
- render/ — scanners, render stages, HUD renderers, and world-render bridge.
- context/ and platform/ — platform-independent feature state and Fabric adapter.
- config/ — configuration and profile persistence.
- command/ — client command registration and handlers.
- mixin/ — Minecraft integration mixins.
- src/test/java/ — unit and integration-oriented regression tests.

## Development principles

1. Keep the client modular. New features should normally be focused modules or services, not additions to one giant initializer.
2. Keep world discovery, render-data preparation, and drawing separate.
3. Prefer deterministic, testable logic for layout, configuration, lifecycle, and selection behavior.
4. Treat configuration compatibility as part of feature correctness.
5. Isolate failures where one feature should not disable unrelated features.
6. Verify changes with the repository's Java 21 CI workflow before calling a build complete.

## Useful documents

- [CURRENT_STATE.md](CURRENT_STATE.md) — verified build state, existing capabilities, recent fixes, and known review items.
- [TODO.md](TODO.md) — prioritized unfinished work and audit queue.
- [ARCHITECTURE.md](ARCHITECTURE.md) — how the main systems connect.
- [DECISIONS.md](DECISIONS.md) — technical constraints and decisions to preserve.
