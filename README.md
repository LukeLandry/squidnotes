# Squid Notes

Squid Notes is a Fabric mod for Minecraft Java Edition that replaces the default note-block right-click cycle with a note-selection UI for players who have the mod installed. Right-click opens a piano-style keyboard where the player can preview notes locally, choose a pending note directly, and commit it only after pressing confirm. Shift-right-click keeps vanilla placement behavior so blocks can still be placed against a note block.

## Current Behavior

The project is initialized from the official Fabric example mod and retargeted for:

- Mod ID: `squidnotes`
- Maven group: `com.squidpowered`
- Java package: `com.squidpowered.squidnotes`
- Target stack: Fabric Loader, Fabric API, and Loom versions from the current Fabric example template

The current mod includes:

- Common mod initialization
- Client mod initialization
- Shared note metadata for the 25 note-block values
- A client note-selection screen for note blocks
- A client-to-server payload that applies the chosen note after server validation
- Compatibility so vanilla clients on a modded server still get normal note-block tuning on right-click
- Sneak interaction pass-through so shift-right-click can still place blocks against note blocks
- A detailed implementation plan in `docs/implementation-plan.md`

## Development

This scaffold targets Minecraft `26.1.2` and Java 25.

Run the client in a development environment:

```sh
./gradlew runClient
```

Build the mod jar:

```sh
./gradlew build
```

## Notes

For players with the mod installed, the client only enables the note-selection UI when the connected server advertises Squid Notes support, and it cancels the normal note-block interaction packet locally when opening the UI. Players without the mod keep vanilla right-click tuning behavior. Shift-right-click is intentionally left alone so vanilla block placement still works.

## License

The scaffold currently retains the template's CC0 license. Change that before release if you want a different project license.
