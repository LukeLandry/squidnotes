package com.squidpowered.squidnotes.shared.noteblock;

public record NoteSelectionMessage(int x, int y, int z, int noteValue) {
	public boolean hasValidNoteValue() {
		return NoteBlockNotes.isValid(this.noteValue);
	}
}
