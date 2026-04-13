# Squid Notes Agent Notes

## Verify
- Run `./gradlew build` for the same check CI runs. It currently covers compilation, packaging, and `test`, but there are no `src/test` sources yet.
- Run `./gradlew runClient` for real behavior checks; this mod's main feature is a client UI around note-block interaction.
- There is no dedicated lint or typecheck task configured in Gradle. Do not invent `lint`/`checkstyle`/`spotless` steps.
- For a focused test once tests exist: `./gradlew test --tests 'com.squidpowered.squidnotes.YourTestClass'`.

## Docs
- Keep `README.md`, `docs/implementation-plan.md`, and this file in sync with behavior changes. Do not leave docs describing scaffold or planned behavior after the code has shipped.

## Stack
- Single Gradle project using Fabric Loom `1.16.1` for Minecraft `26.1.2`.
- Java is compiled with `options.release = 25`; `fabric.mod.json` also requires Java 25. The build uses a Java 25 toolchain and CI runs on JDK 25.
- Loom uses `splitEnvironmentSourceSets()`: shared/server-safe code lives in `src/main/java`, client-only code lives in `src/client/java`.

## Entrypoints
- Main mod entrypoint: `src/main/java/com/squidpowered/squidnotes/SquidNotesMod.java`.
- Client entrypoint: `src/client/java/com/squidpowered/squidnotes/client/SquidNotesClient.java`.
- `fabric.mod.json` is the source of truth for entrypoints and dependency floors.

## Note-Block Flow
- Server/shared registration is in `feature/noteblock/NoteBlockFeature.java`: it registers the C2S payload and a `UseBlockCallback` that suppresses vanilla note cycling by handling note-block right-clicks server-side.
- Client registration is in `client/feature/noteblock/NoteBlockFeatureClient.java`: it opens `NoteBlockKeyboardScreen` on note-block right-click and sends `NoteBlockSetNotePayload` on confirm.
- Shift-right-click should pass through on both sides via `player.isSecondaryUseActive()` so vanilla block placement against note blocks still works.
- Keep previews client-side only. Any committed note change must still go through `NoteBlockSetNotePayload` and server validation before mutating the world.
- Reuse `NoteBlockNotes.NOTES` / `NoteBlockNotes.get(...)` for the 25 note values instead of duplicating note labels or accidental logic.
- UI strings belong in `src/main/resources/assets/squidnotes/lang/en_us.json`; existing screen code already uses translation keys.

## Repo Pointers
- `docs/implementation-plan.md` explains the intended note-block UX, but current behavior should be verified from the Java sources above when docs and code diverge.
