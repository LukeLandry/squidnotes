package com.squidpowered.squidnotes.paper;

import com.squidpowered.squidnotes.shared.noteblock.HelloMessage;
import com.squidpowered.squidnotes.shared.noteblock.NoteSelectionMessage;
import com.squidpowered.squidnotes.shared.noteblock.SquidNotesProtocol;
import com.squidpowered.squidnotes.shared.noteblock.SquidNotesWire;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SquidNotesPaperHandshake implements PluginMessageListener, Listener {
	private static final double INTERACTION_RANGE_SQUARED = 64.0D;

	private final SquidNotesPaperPlugin plugin;
	private final Set<UUID> supportedPlayers = ConcurrentHashMap.newKeySet();

	public SquidNotesPaperHandshake(SquidNotesPaperPlugin plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public boolean isSupported(Player player) {
		return this.supportedPlayers.contains(player.getUniqueId());
	}

	public void clear() {
		this.supportedPlayers.clear();
	}

	public void sendHello(Player player) {
		player.sendPluginMessage(this.plugin, SquidNotesProtocol.HELLO_CHANNEL, SquidNotesWire.encodeHello(new HelloMessage(SquidNotesProtocol.PROTOCOL_VERSION)));
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		try {
			if (SquidNotesProtocol.HELLO_CHANNEL.equals(channel)) {
				this.handleHello(player, message);
				return;
			}

			if (SquidNotesProtocol.SET_NOTE_CHANNEL.equals(channel)) {
				this.handleSetNote(player, message);
			}
		} catch (RuntimeException exception) {
			this.plugin.getLogger().warning("Ignored invalid Squid Notes plugin message on channel " + channel + " from " + player.getName() + ": " + exception.getMessage());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		this.sendHello(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.supportedPlayers.remove(event.getPlayer().getUniqueId());
	}

	private void handleHello(Player player, byte[] message) {
		HelloMessage hello = SquidNotesWire.decodeHello(message);

		if (hello.isSupported()) {
			this.supportedPlayers.add(player.getUniqueId());
		}
	}

	private void handleSetNote(Player player, byte[] message) {
		if (!this.isSupported(player) || player.isDead() || player.getGameMode() == GameMode.SPECTATOR) {
			return;
		}

		NoteSelectionMessage selection = SquidNotesWire.decodeNoteSelection(message);

		if (!selection.hasValidNoteValue()) {
			return;
		}

		int chunkX = selection.x() >> 4;
		int chunkZ = selection.z() >> 4;

		if (!player.getWorld().isChunkLoaded(chunkX, chunkZ)) {
			return;
		}

		double centerX = selection.x() + 0.5D;
		double centerY = selection.y() + 0.5D;
		double centerZ = selection.z() + 0.5D;
		double distanceX = centerX - player.getEyeLocation().getX();
		double distanceY = centerY - player.getEyeLocation().getY();
		double distanceZ = centerZ - player.getEyeLocation().getZ();

		if ((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ) > INTERACTION_RANGE_SQUARED) {
			return;
		}

		Block block = player.getWorld().getBlockAt(selection.x(), selection.y(), selection.z());

		if (block.getType() != Material.NOTE_BLOCK) {
			return;
		}

		NoteBlock data = (NoteBlock) block.getBlockData();
		Note updatedNote = new Note(selection.noteValue());

		if (data.getNote().equals(updatedNote)) {
			return;
		}

		data.setNote(updatedNote);
		block.setBlockData(data, true);
		player.getWorld().playNote(block.getLocation(), data.getInstrument(), updatedNote);
	}
}
