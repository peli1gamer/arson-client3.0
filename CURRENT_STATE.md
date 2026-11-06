# Current State

_Last verified: 2026-11-06_

## Source control state

- Repository: peli1gamer/arson-client3.0
- Working branch: audit/fix-test-cycle
- Verified branch head: 67aac40672aa5501ec52b157d4cc016bbcbe8d17
- The audit/fix work is on this branch. The main branch has not been changed by this work.
- No pull request or merge is implied by this document.

## Build and test status

**CI PASSED**

The GitHub Actions run for the branch head completed successfully:

- Workflow run: [42181804484](https://github.com/peli1gamer/arson-client3.0/actions/runs/42181804484)
- Java 21 setup: passed
- Focused integration tests: passed
- Fabric client runtime smoke test: passed
- Complete build and remap: passed
- Packaged JAR inspection: passed
- Independent artifact inspection and hash: passed

The run reports **43 Java test source files** in src/test/java. This is a file count, not a claim that every possible runtime scenario is covered.

Verified CI artifact:

- Artifact: arson-client-verified
- Artifact ID: 13486180503
- SHA-256: 6bd14e27b8a4aaad4ce7803027c38a7c04a332f40c4bb655723a940fca806930
- [Workflow run and downloadable artifact](https://github.com/peli1gamer/arson-client3.0/actions/runs/42181804484)

This is a successful CI artifact, not a published GitHub release.

## Platform/build target

- Minecraft: 1.21.11
- Java toolchain/release: 21
- Fabric Loader: 0.18.1
- Fabric API: 0.141.3+1.21.11
- Loom remap plugin: 1.14.10
- Mod environment: client-only

## Working systems present in source

- Central module registration and module lifecycle.
- Module categories, keybinds, favorites, and settings.
- Right Shift ClickGUI entry point.
- Search/filter and category navigation in the ClickGUI.
- JSON config persistence, profile operations, and legacy HUD-layout migration.
- HUD rendering and HUD layout/editor code.
- Entity, item, block, and storage render stages.
- Shared entity scanning and cached block/storage discovery.
- Feature context and Fabric context adapter.
- Client commands and notification center.
- Player, world, render, and combat information modules.

“Present in source” means implemented code exists. Individual options still need feature-level verification before being described as fully working.

## Fixes included on this branch

1. AutoTotem now maps player inventory indices to menu slots correctly.
2. AutoTotem respects preserveHeld when choosing a hotbar source slot.
3. AttributeSwap applies the configured mace filters to its explicit slot path.
4. HoverTotem supports inventory fallback when hover is not required.
5. HoverTotem uses menu slot IDs and player-inventory menu boundaries consistently.
6. HUD config migration tolerates missing or malformed legacy settings objects.
7. Config load/save operations return success/failure results; command feedback uses those results for the config subcommands.
8. /arson reset-category is registered.
9. Duplicate viewport information was removed from the world HUD info output.
10. HUD group dragging uses the actual clicked element as its drag anchor.
11. Shift-clicking a selected HUD element toggles that selection off.
12. The HUD API position setter updates the active HUD layout.
13. Module enable failures roll the enabled state back and attempt cleanup.
14. A module tick exception is logged without preventing later modules from ticking.
15. BlockScanner refresh timing uses the configured scan interval.
16. BlockScanner cache invalidation includes the configured render styles.

Regression tests were added for AutoTotem slot mapping, HoverTotem slot behavior, malformed config migration, module lifecycle rollback, and BlockScanner refresh timing.

## Known status / limitations

- No build failure is currently known at the verified branch head.
- A passing CI workflow does not prove that every feature behaves correctly on every server, world, screen size, or Minecraft runtime state.
- The full source tree has not yet completed a systematic line-by-line audit.
- The items in TODO.md are review tasks and unverified candidates unless explicitly described as confirmed.
- The root-level /arson save response path still needs a direct behavior check; the config subcommand paths were the ones corrected in this batch.
- World-render stage exception isolation and StorageScanner refresh behavior remain audit items.

## Updating this file

Whenever a code change is made:
1. Update the branch head and date.
2. Record only CI results that actually completed.
3. Separate source presence from runtime verification.
4. Move resolved items out of the known review queue.
5. Never mark a feature “working” based only on a successful compile.
