package com.squidpowered.squidnotes.paper;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class SquidNotesPaperNoteBlockListener implements Listener {
	private final SquidNotesPaperHandshake handshake;

	public SquidNotesPaperNoteBlockListener(SquidNotesPaperHandshake handshake) {
		this.handshake = handshake;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
			return;
		}

		if (event.getPlayer().getGameMode() == GameMode.SPECTATOR || event.getPlayer().isSneaking() || !this.handshake.isSupported(event.getPlayer())) {
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.NOTE_BLOCK || !(block.getBlockData() instanceof NoteBlock)) {
			return;
		}

		event.setCancelled(true);
	}
}
