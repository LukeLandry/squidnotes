package com.squidpowered.squidnotes.feature.noteblock;

import com.squidpowered.squidnotes.shared.noteblock.NoteSelectionMessage;
import com.squidpowered.squidnotes.shared.noteblock.SquidNotesProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FabricNoteSelectionPayload(NoteSelectionMessage message) implements CustomPacketPayload {
	public static final Type<FabricNoteSelectionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(SquidNotesProtocol.MOD_ID, "set_note"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FabricNoteSelectionPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		payload -> payload.message().x(),
		ByteBufCodecs.INT,
		payload -> payload.message().y(),
		ByteBufCodecs.INT,
		payload -> payload.message().z(),
		ByteBufCodecs.INT,
		payload -> payload.message().noteValue(),
		(x, y, z, noteValue) -> new FabricNoteSelectionPayload(new NoteSelectionMessage(x, y, z, noteValue))
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
