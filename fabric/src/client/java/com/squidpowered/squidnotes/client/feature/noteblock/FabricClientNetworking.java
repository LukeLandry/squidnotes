package com.squidpowered.squidnotes.client.feature.noteblock;

import com.squidpowered.squidnotes.feature.noteblock.FabricHelloPayload;
import com.squidpowered.squidnotes.feature.noteblock.FabricSquidNotesTransport;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricClientNetworking {
	private FabricClientNetworking() {
	}

	public static void initialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FabricSquidNotesTransport.initializeServerSupport());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FabricSquidNotesTransport.resetServerSupport());
		ClientPlayNetworking.registerGlobalReceiver(FabricHelloPayload.ID, (payload, context) -> FabricSquidNotesTransport.markServerSupported(payload.message()));
	}
}
