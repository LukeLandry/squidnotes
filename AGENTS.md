# Squid Notes Agent Notes

## Verify
- Run `./gradlew build` for the same check CI runs. It currently covers compilation, packaging, and `test`, but there are no `src/test` sources yet.
- Run `./gradlew :fabric:runClient` for real behavior checks; this mod's main feature is a client UI around note-block interaction.
- There is no dedicated lint or typecheck task configured in Gradle. Do not invent `lint`/`checkstyle`/`spotless` steps.
- For a focused test once tests exist: `./gradlew test --tests 'com.squidpowered.squidnotes.YourTestClass'`.

## Docs
- Keep `README.md`, `docs/implementation-plan.md`, and this file in sync with behavior changes. Do not leave docs describing scaffold or planned behavior after the code has shipped.

## Stack
- Multi-project Gradle build with `shared`, `fabric`, and `paper` modules.
- The Fabric module uses Fabric Loom `1.16.1` for Minecraft `26.1.2`.
- The Paper module targets prerelease Paper builds for the 26.1 line.
- Java is compiled with `options.release = 25`; `fabric.mod.json` also requires Java 25. The build uses a Java 25 toolchain and CI runs on JDK 25.
- The Fabric module uses `splitEnvironmentSourceSets()`: shared/server-safe code lives in `fabric/src/main/java`, client-only code lives in `fabric/src/client/java`.

## Entrypoints
- Fabric main mod entrypoint: `fabric/src/main/java/com/squidpowered/squidnotes/SquidNotesMod.java`.
- Fabric client entrypoint: `fabric/src/client/java/com/squidpowered/squidnotes/client/SquidNotesClient.java`.
- Paper plugin entrypoint: `paper/src/main/java/com/squidpowered/squidnotes/paper/SquidNotesPaperPlugin.java`.
- `fabric/src/main/resources/fabric.mod.json` and `paper/src/main/resources/paper-plugin.yml` are the source of truth for platform entrypoints and dependency floors.

## Note-Block Flow
- Shared note metadata and wire helpers live in `shared/src/main/java/com/squidpowered/squidnotes/shared/noteblock`.
- Fabric server registration is in `fabric/src/main/java/com/squidpowered/squidnotes/feature/noteblock/NoteBlockFeature.java`: it registers Squid Notes payloads, sends the hello packet, and handles note updates server-side.
- Fabric client registration is in `fabric/src/client/java/com/squidpowered/squidnotes/client/feature/noteblock/NoteBlockFeatureClient.java`: it opens `NoteBlockKeyboardScreen` on note-block right-click once the server handshake succeeds.
- Fabric client networking setup is in `fabric/src/client/java/com/squidpowered/squidnotes/client/feature/noteblock/FabricClientNetworking.java`.
- Paper support is implemented in `paper/src/main/java/com/squidpowered/squidnotes/paper` via plugin messaging plus `PlayerInteractEvent` cancellation for supported clients.
- Shift-right-click should pass through on both sides via `player.isSecondaryUseActive()` so vanilla block placement against note blocks still works.
- Keep previews client-side only. Any committed note change must still go through the Squid Notes protocol and server validation before mutating the world.
- Reuse `NoteBlockNotes.NOTES` / `NoteBlockNotes.get(...)` from `shared` for the 25 note values instead of duplicating note labels or accidental logic.
- UI strings belong in `fabric/src/main/resources/assets/squidnotes/lang/en_us.json`; existing screen code already uses translation keys.

## Repo Pointers
- `docs/implementation-plan.md` explains the intended note-block UX, but current behavior should be verified from the Java sources above when docs and code diverge.
