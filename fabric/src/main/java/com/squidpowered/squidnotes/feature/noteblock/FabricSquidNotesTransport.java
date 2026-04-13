package com.squidpowered.squidnotes.feature.noteblock;

import com.squidpowered.squidnotes.shared.noteblock.HelloMessage;
import com.squidpowered.squidnotes.shared.noteblock.NoteSelectionMessage;
import com.squidpowered.squidnotes.shared.noteblock.SquidNotesProtocol;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;

public final class FabricSquidNotesTransport {
	private static boolean serverSupported;

	private FabricSquidNotesTransport() {
	}

	public static void initializeServerSupport() {
		serverSupported = ClientPlayNetworking.canSend(FabricHelloPayload.ID) || ClientPlayNetworking.canSend(FabricNoteSelectionPayload.ID);

		if (ClientPlayNetworking.canSend(FabricHelloPayload.ID)) {
			ClientPlayNetworking.send(new FabricHelloPayload(new HelloMessage(SquidNotesProtocol.PROTOCOL_VERSION)));
		}
	}

	public static boolean isServerSupported() {
		return serverSupported;
	}

	public static void markServerSupported(HelloMessage message) {
		serverSupported = message.isSupported();

		if (serverSupported) {
			ClientPlayNetworking.send(new FabricHelloPayload(new HelloMessage(SquidNotesProtocol.PROTOCOL_VERSION)));
		}
	}

	public static void resetServerSupport() {
		serverSupported = false;
	}

	public static void sendNoteSelection(int noteValue, BlockPos blockPos) {
		if (!isServerSupported()) {
			return;
		}

		ClientPlayNetworking.send(new FabricNoteSelectionPayload(new NoteSelectionMessage(blockPos.getX(), blockPos.getY(), blockPos.getZ(), noteValue)));
	}
}
