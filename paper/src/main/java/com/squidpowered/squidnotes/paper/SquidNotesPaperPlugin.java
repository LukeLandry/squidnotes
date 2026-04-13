package com.squidpowered.squidnotes.paper;

import com.squidpowered.squidnotes.shared.noteblock.SquidNotesProtocol;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SquidNotesPaperPlugin extends JavaPlugin {
	private SquidNotesPaperHandshake handshake;

	@Override
	public void onEnable() {
		this.handshake = new SquidNotesPaperHandshake(this);
		Bukkit.getPluginManager().registerEvents(new SquidNotesPaperNoteBlockListener(this.handshake), this);
		this.getServer().getMessenger().registerIncomingPluginChannel(this, SquidNotesProtocol.HELLO_CHANNEL, this.handshake);
		this.getServer().getMessenger().registerIncomingPluginChannel(this, SquidNotesProtocol.SET_NOTE_CHANNEL, this.handshake);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, SquidNotesProtocol.HELLO_CHANNEL);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, SquidNotesProtocol.SET_NOTE_CHANNEL);

		for (Player player : this.getServer().getOnlinePlayers()) {
			this.handshake.sendHello(player);
		}
	}

	@Override
	public void onDisable() {
		if (this.handshake != null) {
			this.handshake.clear();
		}

		this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
		this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
	}
}
