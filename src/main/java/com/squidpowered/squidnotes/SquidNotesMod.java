package com.squidpowered.squidnotes;

import com.squidpowered.squidnotes.feature.noteblock.NoteBlockFeature;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquidNotesMod implements ModInitializer {
	public static final String MOD_ID = "squidnotes";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NoteBlockFeature.initialize();
		LOGGER.info("Initialized Squid Notes scaffold.");
	}
}