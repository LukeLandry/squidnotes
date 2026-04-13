# Squid Notes Implementation Plan

## Goal

Replace the note block's default right-click note cycling with a direct note picker for supported clients on both Fabric servers and Paper servers.

Desired player flow:

1. Player right-clicks a note block.
2. Vanilla note increment is suppressed for supported clients, while vanilla clients keep default tuning behavior.
3. A client UI opens with a piano-style keyboard covering note values `0..24`.
4. The current note is visibly highlighted.
5. Clicking a key updates the local selection and plays a client-side-only preview of that note.
6. The block is not modified until the player presses a confirm button.
7. Clicking cancel closes the UI without changing the note.
8. Shift-right-click skips the custom UI so vanilla block placement against the note block still works.

## Proposed Architecture

### 1. Repository layout

Use a multi-project build with three modules:

- `shared`: note metadata and protocol messages
- `fabric`: Fabric client/server implementation
- `paper`: Paper server implementation

### 2. Interaction interception

Use `UseBlockCallback` to detect note-block interaction instead of starting with a mixin.

- On the Fabric client side, detect a right-click on a note block, open the keyboard screen only when the connected server supports the Squid Notes protocol, and cancel the normal interaction packet.
- On the server side, only suppress vanilla note-block tuning for players that have completed the Squid Notes handshake.
- Only intercept the main-hand interaction path unless testing shows off-hand handling is also necessary.
- Pass through shift-right-click by checking the platform-specific secondary-use state so vanilla placement behavior still works.

Reasoning: this is lower-risk than patching `NoteBlock` immediately and keeps the implementation easier to maintain across game updates.

### 3. Client UI

Implement `NoteBlockKeyboardScreen` as a custom screen with:

- White and black piano keys laid out in note order.
- A highlighted key for the block's current note value.
- A separate pending selection state that can differ from the block's current committed note.
- Hover and pressed states for clarity.
- A confirm button that commits the pending note.
- A cancel button that closes without sending a packet.

Interaction behavior:

- Clicking a piano key updates the pending selection only.
- Each key click plays a local preview tone for the selected note.
- The preview must remain client-side and must not send packets or broadcast a sound event to other players.
- The confirm button is the only action that sends the selected note to the server.

Useful UI data already scaffolded:

- `NoteBlockNotes` provides the 25 note values and display labels.
- `NoteBlockKeyboardScreen` lives in the Fabric client module.

### 4. Networking

Add a shared Squid Notes protocol carrying:

- a hello handshake and protocol version
- target block coordinates
- selected note value

Platform-specific adapters should serialize and deserialize the same logical messages for Fabric custom payloads and Paper plugin messaging channels.

Server validation rules:

- The chunk is loaded.
- The block at the target position is still a note block.
- The player is still close enough to interact.
- The note value is between `0` and `24`.

If valid, update the block state with the chosen note and trigger the normal committed note feedback on the server.

### 5. State synchronization

When opening the screen, capture:

- The target block position
- The current note block value
- The instrument if you want future UI polish such as labeling or previews

While the screen is open, track two note values separately:

- The committed block value when the UI was opened or last synced.
- The pending selection value the player is previewing locally.

Do not rely on stale client state when applying the selection; always validate and set on the server.

### 6. Optional polish after the first working version

- Show note labels on hover.
- Support keyboard navigation.
- Show instrument name alongside the pitch.

## Recommended Implementation Order

1. Split the repository into `shared`, `fabric`, and `paper` modules.
2. Move note metadata and protocol semantics into `shared`.
3. Wire the Fabric client and server to the shared protocol.
4. Implement the Paper plugin handshake and note update handling.
5. Confirm the note picker still works on Fabric servers.
6. Confirm the Fabric client can interoperate with the Paper plugin.
7. Test edge cases and multiplayer behavior.

## Validation Checklist

- Right-clicking a note block opens the UI instead of incrementing the note for supported clients.
- Vanilla clients connected to a supported server still increment the note on right-click.
- Shift-right-clicking a note block does not open the UI and still allows block placement against it.
- Cancel leaves the note unchanged.
- Clicking a note previews the pitch locally without changing the block.
- Pressing confirm updates the block to the exact requested value.
- Multiplayer clients cannot set invalid values or edit unloaded blocks.
- Preview tones are not broadcast to the server or other players.
- Off-hand interactions do not double-open the UI.
- The highlighted committed key always matches the current block note.
- The pending selection is visually distinct from the committed note when they differ.
- Breaking or replacing the block while the UI is open fails safely.
- Fabric client to Paper server compatibility works through the shared Squid Notes protocol.
