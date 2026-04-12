package com.squidpowered.squidnotes.feature.noteblock;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record NoteBlockSetNotePayload(BlockPos blockPos, int noteValue) implements CustomPayload {
	public static final Id<NoteBlockSetNotePayload> ID = new Id<>(Identifier.of("squidnotes", "set_note"));
	public static final PacketCodec<RegistryByteBuf, NoteBlockSetNotePayload> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC,
		NoteBlockSetNotePayload::blockPos,
		PacketCodecs.VAR_INT,
		NoteBlockSetNotePayload::noteValue,
		NoteBlockSetNotePayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}