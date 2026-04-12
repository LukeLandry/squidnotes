# Squid Notes

Squid Notes is a Fabric mod for Minecraft Java Edition that replaces the default note-block right-click cycle with a note-selection UI. The planned interaction is a piano-style keyboard where the player can preview notes locally, choose a pending note directly, and commit it only after pressing confirm.

## Current Scaffold

The project is initialized from the official Fabric example mod and retargeted for:

- Mod ID: `squidnotes`
- Maven group: `com.squidpowered`
- Java package: `com.squidpowered.squidnotes`
- Target stack: Fabric Loader, Fabric API, and Loom versions from the current Fabric example template

Initial source structure is in place for:

- Common mod initialization
- Client mod initialization
- Shared note metadata for the 25 note-block values
- A client screen scaffold for the future keyboard UI
- A detailed implementation plan in `docs/implementation-plan.md`

## Development

This scaffold targets Minecraft `1.21.11` and Java 21.

Run the client in a development environment:

```sh
./gradlew runClient
```

Build the mod jar:

```sh
./gradlew build
```

## Notes

The current scaffold does not yet intercept note-block interaction. That is intentional: the baseline project remains stable while the feature is implemented in phases.

## License

The scaffold currently retains the template's CC0 license. Change that before release if you want a different project license.
