package com.squidpowered.squidnotes.client;

import com.squidpowered.squidnotes.client.feature.noteblock.NoteBlockFeatureClient;
import net.fabricmc.api.ClientModInitializer;

public class SquidNotesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		NoteBlockFeatureClient.initializeClient();
	}
}