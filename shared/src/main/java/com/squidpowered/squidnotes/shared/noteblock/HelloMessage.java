package com.squidpowered.squidnotes.shared.noteblock;

public record HelloMessage(int protocolVersion) {
	public boolean isSupported() {
		return this.protocolVersion == SquidNotesProtocol.PROTOCOL_VERSION;
	}
}
