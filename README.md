# Squid Notes

Squid Notes is a Minecraft Java Edition mod that ships as both a Fabric mod and a Paper plugin. The Fabric client mod replaces the default note-block right-click cycle with a note-selection UI for players who have the mod installed. Right-click opens a piano-style keyboard where the player can preview notes locally, choose a pending note directly, and commit it only after pressing confirm. Shift-right-click keeps vanilla placement behavior so blocks can still be placed against a note block.

## Current Behavior

The repository is organized as a multi-project Gradle build:

- `shared/`: platform-neutral note metadata and Squid Notes protocol classes
- `fabric/`: Fabric mod with the client UI and Fabric server integration
- `paper/`: Paper plugin that interoperates with the Fabric client mod

The current implementation includes:

- Shared note metadata for the 25 note-block values
- A shared hello and note-selection protocol used by both platforms
- A Fabric client note-selection screen for note blocks
- Fabric server support for the Squid Notes protocol
- Paper plugin support for the Squid Notes protocol
- Compatibility so vanilla clients on supported servers still get normal note-block tuning on right-click
- Sneak interaction pass-through so shift-right-click can still place blocks against note blocks
- A detailed implementation plan in `docs/implementation-plan.md`

## Development

This project targets Minecraft `26.1.2` and Java 25.

Run the Fabric client in a development environment:

```sh
./gradlew :fabric:runClient
```

Build all artifacts:

```sh
./gradlew build
```

Build one artifact at a time:

```sh
./gradlew :fabric:build
./gradlew :paper:build
```

Built jars are written under each module's `build/libs/` directory.

## Notes

For players with the Fabric client mod installed, the client only enables the note-selection UI when the connected server advertises Squid Notes support. That support can come from either the Fabric server module or the Paper plugin. Players without the mod keep vanilla right-click tuning behavior. Shift-right-click is intentionally left alone so vanilla block placement still works.

## License

This project is licensed under the GNU General Public License v3.0.
