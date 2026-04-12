package com.squidpowered.squidnotes.client.feature.noteblock;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class NoteBlockFeatureClient {
	private NoteBlockFeatureClient() {
	}

	public static void initializeClient() {
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand != Hand.MAIN_HAND || player.isSpectator()) {
				return ActionResult.PASS;
			}

			BlockState state = world.getBlockState(hitResult.getBlockPos());

			if (!state.isOf(Blocks.NOTE_BLOCK)) {
				return ActionResult.PASS;
			}

			MinecraftClient client = MinecraftClient.getInstance();

			if (client.currentScreen == null) {
				client.setScreen(new NoteBlockKeyboardScreen(
					hitResult.getBlockPos().toImmutable(),
					state.get(NoteBlock.NOTE),
					state.get(NoteBlock.INSTRUMENT)
				));
			}

			return ActionResult.SUCCESS;
		});
	}

	public static void sendNoteSelection(int noteValue, net.minecraft.util.math.BlockPos blockPos) {
		ClientPlayNetworking.send(new com.squidpowered.squidnotes.feature.noteblock.NoteBlockSetNotePayload(blockPos, noteValue));
	}
}