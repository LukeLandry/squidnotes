package com.squidpowered.squidnotes.feature.noteblock;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class NoteBlockFeature {
	private static final double INTERACTION_RANGE = 8.0D;

	private NoteBlockFeature() {
	}

	public static void initialize() {
		PayloadTypeRegistry.serverboundPlay().register(NoteBlockSetNotePayload.ID, NoteBlockSetNotePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(NoteBlockSetNotePayload.ID, (payload, context) -> applySelection(context.player(), payload));
		UseBlockCallback.EVENT.register(NoteBlockFeature::handleVanillaTuningFallback);
	}

	private static InteractionResult handleVanillaTuningFallback(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || player.isSpectator() || player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}

		BlockPos blockPos = hitResult.getBlockPos();
		if (!(world instanceof ServerLevel serverWorld) || !serverWorld.isLoaded(blockPos) || !player.isWithinBlockInteractionRange(blockPos, INTERACTION_RANGE)) {
			return InteractionResult.PASS;
		}

		BlockState state = world.getBlockState(blockPos);
		if (!state.is(Blocks.NOTE_BLOCK)) {
			return InteractionResult.PASS;
		}

		BlockState updatedState = state.cycle(NoteBlock.NOTE);
		serverWorld.setBlock(blockPos, updatedState, 3);
		serverWorld.blockEvent(blockPos, Blocks.NOTE_BLOCK, 0, 0);
		player.awardStat(Stats.TUNE_NOTEBLOCK);
		return InteractionResult.SUCCESS_SERVER;
	}

	private static void applySelection(ServerPlayer player, NoteBlockSetNotePayload payload) {
		if (!isValidNoteValue(payload.noteValue())) {
			return;
		}

		ServerLevel world = player.level();
		BlockPos blockPos = payload.blockPos();

		if (!world.isLoaded(blockPos) || !player.isWithinBlockInteractionRange(blockPos, INTERACTION_RANGE)) {
			return;
		}

		BlockState state = world.getBlockState(blockPos);

		if (!state.is(Blocks.NOTE_BLOCK)) {
			return;
		}

		BlockState updatedState = state.setValue(NoteBlock.NOTE, payload.noteValue());

		if (updatedState.equals(state)) {
			return;
		}

		world.setBlock(blockPos, updatedState, 3);
		world.blockEvent(blockPos, Blocks.NOTE_BLOCK, 0, 0);
		player.awardStat(Stats.TUNE_NOTEBLOCK);
	}

	public static boolean isValidNoteValue(int noteValue) {
		return noteValue >= 0 && noteValue < NoteBlockNotes.NOTES.size();
	}
}
