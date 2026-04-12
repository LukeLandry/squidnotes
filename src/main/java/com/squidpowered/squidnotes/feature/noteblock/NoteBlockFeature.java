package com.squidpowered.squidnotes.feature.noteblock;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public final class NoteBlockFeature {
	private static final double INTERACTION_RANGE = 8.0D;

	private NoteBlockFeature() {
	}

	public static void initialize() {
		PayloadTypeRegistry.playC2S().register(NoteBlockSetNotePayload.ID, NoteBlockSetNotePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(NoteBlockSetNotePayload.ID, (payload, context) -> applySelection(context.player(), payload));
		UseBlockCallback.EVENT.register(NoteBlockFeature::interceptInteraction);
	}

	private static ActionResult interceptInteraction(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (world.isClient() || hand != Hand.MAIN_HAND || player.isSpectator()) {
			return ActionResult.PASS;
		}

		if (!world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.NOTE_BLOCK)) {
			return ActionResult.PASS;
		}

		return ActionResult.SUCCESS;
	}

	private static void applySelection(ServerPlayerEntity player, NoteBlockSetNotePayload payload) {
		if (!isValidNoteValue(payload.noteValue())) {
			return;
		}

		ServerWorld world = (ServerWorld) player.getEntityWorld();
		BlockPos blockPos = payload.blockPos();

		if (!world.isChunkLoaded(ChunkPos.toLong(blockPos)) || !player.canInteractWithBlockAt(blockPos, INTERACTION_RANGE)) {
			return;
		}

		BlockState state = world.getBlockState(blockPos);

		if (!state.isOf(Blocks.NOTE_BLOCK)) {
			return;
		}

		BlockState updatedState = state.with(NoteBlock.NOTE, payload.noteValue());

		if (updatedState.equals(state)) {
			return;
		}

		world.setBlockState(blockPos, updatedState, 3);
		world.addSyncedBlockEvent(blockPos, Blocks.NOTE_BLOCK, 0, 0);
		player.incrementStat(Stats.TUNE_NOTEBLOCK);
	}

	public static boolean isValidNoteValue(int noteValue) {
		return noteValue >= 0 && noteValue < NoteBlockNotes.NOTES.size();
	}
}