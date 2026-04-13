package com.squidpowered.squidnotes.shared.noteblock;

import java.util.List;

public final class NoteBlockNotes {
	public static final List<NoteDefinition> NOTES = List.of(
		new NoteDefinition(0, "F#3", true),
		new NoteDefinition(1, "G3", false),
		new NoteDefinition(2, "G#3", true),
		new NoteDefinition(3, "A3", false),
		new NoteDefinition(4, "A#3", true),
		new NoteDefinition(5, "B3", false),
		new NoteDefinition(6, "C4", false),
		new NoteDefinition(7, "C#4", true),
		new NoteDefinition(8, "D4", false),
		new NoteDefinition(9, "D#4", true),
		new NoteDefinition(10, "E4", false),
		new NoteDefinition(11, "F4", false),
		new NoteDefinition(12, "F#4", true),
		new NoteDefinition(13, "G4", false),
		new NoteDefinition(14, "G#4", true),
		new NoteDefinition(15, "A4", false),
		new NoteDefinition(16, "A#4", true),
		new NoteDefinition(17, "B4", false),
		new NoteDefinition(18, "C5", false),
		new NoteDefinition(19, "C#5", true),
		new NoteDefinition(20, "D5", false),
		new NoteDefinition(21, "D#5", true),
		new NoteDefinition(22, "E5", false),
		new NoteDefinition(23, "F5", false),
		new NoteDefinition(24, "F#5", true)
	);

	private NoteBlockNotes() {
	}

	public static NoteDefinition get(int noteValue) {
		if (!isValid(noteValue)) {
			throw new IllegalArgumentException("Note value out of range: " + noteValue);
		}

		return NOTES.get(noteValue);
	}

	public static boolean isValid(int noteValue) {
		return noteValue >= 0 && noteValue < NOTES.size();
	}

	public record NoteDefinition(int value, String label, boolean accidental) {
	}
}
