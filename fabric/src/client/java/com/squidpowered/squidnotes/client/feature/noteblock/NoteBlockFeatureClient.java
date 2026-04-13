package com.squidpowered.squidnotes.client.feature.noteblock;

import com.squidpowered.squidnotes.feature.noteblock.FabricSquidNotesTransport;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class NoteBlockFeatureClient {
	private NoteBlockFeatureClient() {
	}

	public static void initializeClient() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand != InteractionHand.MAIN_HAND || player.isSpectator() || player.isSecondaryUseActive()) {
				return InteractionResult.PASS;
			}

			if (!FabricSquidNotesTransport.isServerSupported()) {
				return InteractionResult.PASS;
			}

			BlockState state = world.getBlockState(hitResult.getBlockPos());

			if (!state.is(Blocks.NOTE_BLOCK)) {
				return InteractionResult.PASS;
			}

			Minecraft client = Minecraft.getInstance();

			if (client.screen == null) {
				client.setScreen(new NoteBlockKeyboardScreen(
					hitResult.getBlockPos().immutable(),
					state.getValue(NoteBlock.NOTE),
					state.getValue(NoteBlock.INSTRUMENT)
				));
			}

			return InteractionResult.FAIL;
		});
	}

	public static void sendNoteSelection(int noteValue, BlockPos blockPos) {
		FabricSquidNotesTransport.sendNoteSelection(noteValue, blockPos);
	}
}
