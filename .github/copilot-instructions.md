# Copilot instructions for Squid Notes

## Build, test, and run commands

- `./gradlew build` builds the mod jar and runs the configured verification tasks.
- `./gradlew test` runs the Gradle test task. There are currently no test sources under `src/test`, so this is mainly a regression check on the current build setup.
- `./gradlew test --tests 'com.squidpowered.squidnotes.YourTestClass'` runs a single test class once tests exist.
- `./gradlew runClient` launches a dev client for manual UI/gameplay testing.
- `./gradlew tasks --all` is the quickest way to inspect available Fabric Loom tasks in this repo.

There is no dedicated lint task configured in Gradle today.

## High-level architecture

This is a Fabric mod for Minecraft `26.1.2` using Java 25 and Loom's split environment source sets.

- `src/main/java` contains shared and server-side code. `SquidNotesMod` initializes `NoteBlockFeature`, which registers the note-block interaction interception and the client-to-server payload receiver.
- `src/client/java` contains client-only code. `SquidNotesClient` initializes `NoteBlockFeatureClient`, which opens the note-selection screen on note-block right-click.
- The note-block flow is intentionally split across client and server:
  - Client: intercept the note-block interaction, open `NoteBlockKeyboardScreen`, track pending UI state, and play local preview audio.
  - Shared payload: `NoteBlockSetNotePayload` carries the target `BlockPos` and selected note value.
  - Server: `NoteBlockFeature.applySelection(...)` validates the payload, updates the block state, triggers the block event, and records the vanilla tuning stat.
- `NoteBlockNotes` is the canonical note table for all 25 note-block values, including display labels and whether each note is an accidental.
- UI text is localized through `src/main/resources/assets/squidnotes/lang/en_us.json`, and screen code uses translation keys rather than hard-coded user-facing strings.

## Key conventions

- Keep client-only classes in `src/client/java` and shared/server-safe classes in `src/main/java`. The project depends on Loom's `splitEnvironmentSourceSets()` setup; do not move client classes into common code just because they are small.
- For note metadata, reuse `NoteBlockNotes.NOTES` and `NoteBlockNotes.get(...)` instead of duplicating note labels, note ranges, or accidental/white-key logic in new code.
- Preserve the current interaction model: previews are client-side only, but any committed note change must go through the networking payload and be validated on the server before mutating the world.
- When extending note-block behavior, prefer the existing `UseBlockCallback` interception pattern before reaching for mixins. Both client and server currently participate so the UI opens client-side while vanilla note cycling is suppressed server-side.
- Follow the screen's two-state model: `committedNoteValue` reflects the block state captured when the UI opened, while `pendingNoteValue` is the user's local selection until they apply it.
- For new UI strings, add translation keys to `en_us.json` and continue using `Component.translatable(...)`.
