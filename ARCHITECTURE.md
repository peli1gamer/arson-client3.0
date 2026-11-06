# Architecture — Arson Client V3

_Last updated: 2026-11-06_

## 1. Startup and composition root

io.arson.client.ArsonClient is the Fabric client initializer and composition root.

At client startup it:

1. Creates the ModuleManager.
2. Registers the default modules.
3. Registers the Right Shift ClickGUI keybind.
4. Registers the /arson command tree.
5. Registers HUD elements with Fabric's HUD element registry.
6. Creates the shared FeatureContext and FabricFeatureContextAdapter.
7. Creates the WorldRenderBridge and registers the world-render stages.
8. Hooks end-of-client-tick processing for context updates, keybinds, module ticks, runtime smoke behavior, and periodic config saves.
9. Loads the saved configuration.

Keep this class focused on wiring systems together. Feature logic belongs in modules, scanners, render stages, or focused services.

## 2. Module system

Main files:

- module/Module.java
- module/ModuleManager.java
- Individual feature modules under module/

### Module

A module owns:
- A stable string ID.
- Display name, category, and description.
- Enabled state, favorite state, and keybind.
- A list of settings.
- Lifecycle hooks: onEnable, onDisable, and onTick.

The base class controls enable/disable transitions. If onEnable throws, it rolls the enabled flag back, attempts onDisable cleanup, and rethrows the original failure.

### ModuleManager

The manager:
- Registers modules and rejects duplicate IDs.
- Provides lookup, category organization, and search.
- Exposes module collections to the UI and config layer.
- Ticks modules from the client tick callback.
- Logs a module's runtime tick exception and continues to later modules.

Modules should not depend on GUI widget instances. UI reads and changes module state through the module/setting API.

## 3. Settings

Settings live under settings/. The codebase includes boolean, double, color, enum, and string settings, plus setting groups and the shared setting base.

A setting ID is part of the configuration contract. Keep IDs stable when changing display labels or UI presentation. If an ID must change, add an explicit config migration and test it.

## 4. Feature context and platform boundary

Main files:

- context/FeatureContext.java
- context/ClientState.java
- context/RenderState.java
- platform/FeatureContextAdapter.java
- platform/FabricFeatureContextAdapter.java

FeatureContext stores client lifecycle/state and render state without importing Fabric or Minecraft types. The platform adapter reads the live game and propagates state into that context during tick and render callbacks.

The intended boundary is:

Minecraft/Fabric callbacks → platform adapter → FeatureContext → feature consumers

Keep pure state models free of platform dependencies where possible so they remain testable.

## 5. World rendering pipeline

Main files:

- render/WorldRenderBridge.java
- render/StorageScanner.java
- render/EntityScanner.java
- render/BlockScanner.java
- render/*RenderStage.java
- Render data models such as EntityTarget and BlockTarget

The bridge attaches to Fabric's AFTER_ENTITIES world-render event. It updates the render context and invokes registered stages.

The current stage wiring in ArsonClient includes:

1. Storage render stage
2. Entity render stage
3. Item render stage
4. Entity information stage
5. Entity tracer stage
6. Block render stage
7. Aim Assist render feedback stage

### Discovery → data → drawing

The render architecture separates three responsibilities:

1. **Discovery:** scanners inspect loaded world data and identify relevant targets.
2. **Render data:** scanners/stages convert discoveries into small target or render-command models.
3. **Drawing:** renderers consume those models from the appropriate Fabric/Minecraft render callback.

This separation is important. Do not move expensive world scans into low-level drawing helpers, and do not make scanners mutate global render state.

### Shared scans and caches

- One EntityScanner instance is shared by the entity, item, entity-info, and tracer stages in the initializer.
- BlockScanner caches block targets and invalidates based on world, player chunk, configuration, and scan interval.
- StorageScanner caches loaded storage targets and applies the current distance filter after retrieving cached targets.

When adding a setting that changes discovery results, include it in the relevant cache invalidation logic and add a regression test.

## 6. HUD and ClickGUI

Main files:

- ui/ArsonScreen.java
- ui/ClickGuiLayoutModel.java
- ui/HudEditorScreen.java
- ui/HudSelectionModel.java
- ui/HudAnchorConstraint.java
- render/HudRenderer.java
- render/ArrayListRenderer.java
- render/CombatInfoRenderer.java
- module/HudModule.java
- module/HudLayoutModule.java

The ClickGUI is a Minecraft screen. It queries modules from ModuleManager, displays categories/search results, and edits module settings. Layout geometry is delegated to a separate model to keep calculations testable.

HUD output is registered through Fabric's HUD element registry. HUD layout data is represented by the active HUD layout module and edited through the HUD editor. The legacy HUD module still exists; avoid adding new position APIs to the legacy path when the active layout module owns that element.

## 7. Configuration and profiles

Main file: config/ConfigManager.java

Configuration is stored as JSON under the Minecraft game directory's config folder. The current primary file is arson-v3.json; named profiles are stored under arson-v3-profiles/.

The manager serializes module state and setting values, loads known module IDs/settings, tolerates individual malformed setting values, and includes legacy HUD layout migration. Load/save operations return booleans so callers can report actual operation results.

Treat module IDs, setting IDs, and schema migrations as persistence compatibility contracts.

## 8. Commands and notifications

- command/ArsonCommand.java registers the client command tree.
- notification/NotificationCenter.java stores/presents client notifications.

Command handlers should delegate to the relevant manager/API and report the returned result. Do not report success before a config or profile operation has succeeded.

## 9. Mixins and platform integration

The project has two mixin classes under mixin/, alongside Fabric lifecycle, keybinding, HUD, and world-render event hooks.

Use Fabric APIs when they provide the required integration point. Keep mixins narrow and document why a target cannot be handled through a supported event/API. Any mixin change should receive a runtime smoke check.

## 10. Testing strategy

Tests are under src/test/java and cover settings, module catalog/behavior, layout models, HUD persistence, feature context, config migration/persistence, render bridge behavior, and targeted regression cases.

Prefer tests in this order:
1. Pure model/helper tests.
2. Module behavior tests with controlled state.
3. Integration tests across module/context/render/config boundaries.
4. Fabric client runtime smoke test for startup and platform integration.

A passing compile is not sufficient evidence that a feature works. See TODO.md for remaining verification work.
