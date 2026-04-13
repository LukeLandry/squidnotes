package com.squidpowered.squidnotes.client;

import com.squidpowered.squidnotes.client.feature.noteblock.FabricClientNetworking;
import com.squidpowered.squidnotes.client.feature.noteblock.NoteBlockFeatureClient;
import net.fabricmc.api.ClientModInitializer;

public class SquidNotesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricClientNetworking.initialize();
		NoteBlockFeatureClient.initializeClient();
	}
}
