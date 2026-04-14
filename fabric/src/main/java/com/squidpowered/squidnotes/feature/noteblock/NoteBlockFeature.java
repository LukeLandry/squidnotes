package com.squidpowered.squidnotes.feature.noteblock;

import com.squidpowered.squidnotes.shared.noteblock.HelloMessage;
import com.squidpowered.squidnotes.shared.noteblock.NoteBlockNotes;
import com.squidpowered.squidnotes.shared.noteblock.NoteSelectionMessage;
import com.squidpowered.squidnotes.shared.noteblock.SquidNotesProtocol;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NoteBlockFeature {
	private static final double INTERACTION_RANGE = 8.0D;
	private static final Set<UUID> SUPPORTED_PLAYERS = ConcurrentHashMap.newKeySet();

	private NoteBlockFeature() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(FabricHelloPayload.ID, FabricHelloPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(FabricHelloPayload.ID, FabricHelloPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(FabricNoteSelectionPayload.ID, FabricNoteSelectionPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerPlayNetworking.send(handler.player, new FabricHelloPayload(new HelloMessage(SquidNotesProtocol.PROTOCOL_VERSION))));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SUPPORTED_PLAYERS.remove(handler.player.getUUID()));
		ServerPlayNetworking.registerGlobalReceiver(FabricHelloPayload.ID, (payload, context) -> {
			if (payload.message().isSupported()) {
				SUPPORTED_PLAYERS.add(context.player().getUUID());
			} else {
				SUPPORTED_PLAYERS.remove(context.player().getUUID());
			}
		});
		ServerPlayNetworking.registerGlobalReceiver(FabricNoteSelectionPayload.ID, (payload, context) -> applySelection(context.player(), payload.message()));
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

	private static void applySelection(ServerPlayer player, NoteSelectionMessage message) {
		if (!SUPPORTED_PLAYERS.contains(player.getUUID()) || !message.hasValidNoteValue()) {
			return;
		}

		ServerLevel world = player.level();
		BlockPos blockPos = new BlockPos(message.x(), message.y(), message.z());

		if (!world.isLoaded(blockPos) || !player.isWithinBlockInteractionRange(blockPos, INTERACTION_RANGE)) {
			return;
		}

		BlockState state = world.getBlockState(blockPos);

		if (!state.is(Blocks.NOTE_BLOCK)) {
			return;
		}

		BlockState updatedState = state.setValue(NoteBlock.NOTE, message.noteValue());

		if (updatedState.equals(state)) {
			return;
		}

		world.setBlock(blockPos, updatedState, 3);
		world.blockEvent(blockPos, Blocks.NOTE_BLOCK, 0, 0);
		player.awardStat(Stats.TUNE_NOTEBLOCK);
	}

	public static boolean isValidNoteValue(int noteValue) {
		return NoteBlockNotes.isValid(noteValue);
	}
}
