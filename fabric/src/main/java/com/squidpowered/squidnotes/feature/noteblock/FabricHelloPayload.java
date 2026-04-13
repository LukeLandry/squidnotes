package com.squidpowered.squidnotes.feature.noteblock;

import com.squidpowered.squidnotes.shared.noteblock.HelloMessage;
import com.squidpowered.squidnotes.shared.noteblock.SquidNotesProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FabricHelloPayload(HelloMessage message) implements CustomPacketPayload {
	public static final Type<FabricHelloPayload> ID = new Type<>(Identifier.fromNamespaceAndPath(SquidNotesProtocol.MOD_ID, "hello"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FabricHelloPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		payload -> payload.message().protocolVersion(),
		protocolVersion -> new FabricHelloPayload(new HelloMessage(protocolVersion))
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
