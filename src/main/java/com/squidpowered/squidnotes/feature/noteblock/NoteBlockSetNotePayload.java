package com.squidpowered.squidnotes.feature.noteblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NoteBlockSetNotePayload(BlockPos blockPos, int noteValue) implements CustomPacketPayload {
	public static final Type<NoteBlockSetNotePayload> ID = new Type<>(Identifier.fromNamespaceAndPath("squidnotes", "set_note"));
	public static final StreamCodec<RegistryFriendlyByteBuf, NoteBlockSetNotePayload> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		NoteBlockSetNotePayload::blockPos,
		ByteBufCodecs.VAR_INT,
		NoteBlockSetNotePayload::noteValue,
		NoteBlockSetNotePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
