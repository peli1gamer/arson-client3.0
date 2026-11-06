# Technical Decisions

_Last updated: 2026-11-06_

This file records constraints and design decisions that should not be casually reversed during cleanup or feature work.

## 1. Platform and compatibility target

- The project is a **client-only Fabric mod**.
- The current target is Minecraft **1.21.11** and Java **21**.
- Fabric Loader/API and Loom versions are pinned in gradle.properties and build.gradle.
- Do not silently change Minecraft mappings, loader, API, or Java release as part of an unrelated feature. Change them deliberately and run the full build/runtime workflow.

## 2. Keep the module architecture

- Module is the common lifecycle/settings contract.
- ModuleManager owns registration, lookup, organization, search, and ticking.
- Feature-specific behavior belongs in focused modules/services.
- Do not turn ArsonClient into a feature implementation dump; it is the composition root.
- Module IDs are stable identifiers used by config and integrations. Do not rename them casually.

## 3. Preserve setting/config compatibility

- Setting IDs are persisted keys. Display names may change without changing IDs.
- Config schema changes require a version/migration decision and regression test.
- Invalid individual setting values should not unnecessarily prevent unrelated valid settings from loading.
- Load/save APIs return success/failure. Callers should not replace actual results with unconditional success messages.
- Preserve profile isolation and legacy HUD migration unless a deliberate migration replaces them.

## 4. Keep render discovery separate from drawing

The intended flow is:

world state → scanner/discovery → target/render data → render stage/renderer

- Do not put large world scans inside drawing primitives.
- Keep render data models small and straightforward.
- Include all result-affecting settings in cache invalidation.
- Keep range filtering dynamic when it can be applied cheaply after cached discovery.
- Shared scanners should remain shared where multiple stages intentionally consume the same snapshot.

## 5. Preserve lifecycle failure handling

- A failed module enable must not leave the module marked enabled.
- Cleanup should be attempted after a failed enable.
- A tick failure in one module should not prevent unrelated modules from ticking.
- Preserve useful logging; do not silently swallow new runtime failures.
- If render stages are isolated in the future, preserve the same principle without leaving graphics/render state corrupted.

## 6. Keep UI geometry testable

- Keep reusable geometry calculations in pure layout/model classes where practical.
- The GUI should read/write module state rather than duplicating module configuration.
- HUD layout positions belong to the active HUD layout system. Do not route new position updates through legacy HUD coordinates.
- Group drag calculations must be anchored to the element the user actually grabbed, not an incidental collection iteration order.

## 7. Use the platform boundary

- FeatureContext is intended to remain independent of Fabric/Minecraft classes.
- Translate live platform state through the adapter instead of adding platform dependencies to pure context models.
- Keep context lifecycle transitions explicit (initialize, world available, disconnect, stop).

## 8. Tests and verification are part of the change

- Add regression tests for bug fixes.
- Run focused tests while iterating, then the full CI workflow.
- Keep the Java 21 build, Fabric runtime smoke test, remap, JAR inspection, and independent artifact verification.
- Never label a JAR verified unless the workflow's inspection steps passed for that exact commit.

## 9. Scope boundaries

- Keep this project focused on ordinary client functionality, correctness, compatibility, stability, and performance.
- Do not add stealth, anti-cheat evasion, or detection-bypass behavior as an architectural requirement.
- Do not copy implementation code from unrelated client projects without an explicit licensing and provenance review.

## 10. Package layout caution

The source tree currently contains both io.arson.client and a smaller com.arson.client.render package area. Do not mass-rename or merge package namespaces without first tracing imports, resource references, and compatibility implications.

## 11. Updating decisions

When changing one of these rules:
1. Explain why the old decision no longer fits.
2. Identify affected modules, settings, config data, and tests.
3. Add migration/compatibility handling if persisted data or public APIs are affected.
4. Update this document in the same change as the architecture change.
