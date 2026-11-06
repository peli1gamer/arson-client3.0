# TODO — Development Backlog

_Last updated: 2026-11-06_

This is the active backlog for Arson Client V3. Items are not claims that a bug definitely exists unless the wording says it is confirmed. Verify each candidate against the current source before changing it.

## Priority 0 — Keep the verified baseline healthy

- [ ] Keep Java 21 CI green on every code-changing branch.
- [ ] Keep the Fabric client runtime smoke test and packaged-JAR inspection enabled.
- [ ] Run the full workflow after each meaningful repair batch.
- [ ] Update CURRENT_STATE.md after the branch head or CI status changes.

## Confirmed Storage ESP findings (Anubis comparison pending)

These are confirmed by tracing the current Arson source. They are not yet patched; compare the reference JAR before choosing the final behavior.

- [ ] StorageScanner has a hard-coded 20-tick refresh while ContainerESP exposes a configurable 1–20 tick Scan Interval. The user-facing setting does not control the scanner's own cache refresh.
- [ ] ContainerESP exposes Label Background, Label Padding, and Label Background Color, but StorageRenderStage only draws label text and never reads those three settings.
- [ ] StorageScanner range-filters targets by their centre relative to the player, while StorageOverlay applies a second range check from the target's minimum corner relative to the camera. This mixes origins and reference points and can drop targets inconsistently near the range boundary.

## Priority 1 — Finish the reliability audit

- [ ] Review WorldRenderBridge: decide whether each render stage should be isolated so one stage failure cannot skip later stages. Preserve useful error logging and ensure render state is restored correctly.
- [ ] Review StorageScanner refresh behavior. It currently has its own fixed 20-tick cache interval; compare this with the user-facing storage scan settings and make the behavior explicit and testable.
- [ ] Add a focused test for ModuleManager tick isolation: one module throws, and later modules still receive their tick.
- [ ] Review enable/disable cleanup for modules that register callbacks, retain targets, or hold temporary state.
- [ ] Check world change, disconnect, respawn, and screen transition handling for stale cached state.
- [ ] Review both mixins for callback ordering, null/state assumptions, and safe behavior when the relevant screen or player is absent.
- [ ] Check render stages for state cleanup on early return and exception paths.

## Priority 2 — Verify settings actually affect behavior

- [ ] Audit every setting declaration against its runtime read path. Record settings that are UI-only, persisted-only, unused, or intentionally reserved.
- [ ] Recheck AimAssist FOV and Target Lock setting paths from setting declaration through target selection and render feedback. Treat any suspected unused setting as unconfirmed until traced.
- [ ] Recheck ContainerESP label/display settings and distance/range options end-to-end.
- [ ] Recheck BlockESP and storage scan interval controls against their scanner cache behavior.
- [ ] Verify each module's enable state, setting values, and keybind survive save/restart/load as intended.
- [ ] Verify setting dependency/visibility rules do not accidentally prevent valid configuration.

## Priority 3 — Commands and configuration

- [ ] Verify every /arson config operation against missing files, malformed JSON, invalid profile names, and write failures.
- [ ] Directly verify the root-level /arson save response path reports the actual save result.
- [ ] Add tests for profile load/save/delete and invalid profile names where coverage is missing.
- [ ] Check legacy config migrations against representative old config files, not only synthetic minimal cases.
- [ ] Ensure future config schema changes increment the version and include migration tests.

## Priority 4 — GUI and HUD polish

- [ ] Test ClickGUI navigation and keyboard focus with empty search results, long module lists, narrow windows, and repeated open/close.
- [ ] Test HUD editor multi-selection, drag anchors, group movement, snapping, and resize behavior at different GUI scales.
- [ ] Verify HUD positions and anchors round-trip through config save/load.
- [ ] Check that GUI preference changes are persisted consistently and do not cause unnecessary rebuilds.
- [ ] Check for duplicate or conflicting HUD information sources between legacy HUD and HUD layout modules.

## Priority 5 — Performance and maintainability

- [ ] Profile entity, block, and storage scanning in a dense world; measure allocations and worst-case scan time.
- [ ] Review scanner cache invalidation for world changes, player movement, range changes, and setting changes.
- [ ] Look for repeated per-frame allocations in render stages and HUD renderers.
- [ ] Review module registration for dead, duplicate, or overlapping modules.
- [ ] Document public API contracts and add compatibility tests before changing them.
- [ ] Keep tests for pure layout, setting, lifecycle, and migration logic independent of live game state where practical.

## Completion rules

A task is complete only when:
1. The source behavior has been inspected and the intended change is implemented.
2. A regression test covers the original failure or important edge case.
3. The Java 21 build and relevant tests pass.
4. The Fabric runtime smoke test passes when the change can affect client startup/runtime.
5. CURRENT_STATE.md and DECISIONS.md are updated if the change affects project status or an architectural rule.

Do not mark an item complete because code was merely edited or because a different test passed.
