package com.squidpowered.squidnotes.shared.noteblock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class SquidNotesWire {
	private SquidNotesWire() {
	}

	public static byte[] encodeHello(HelloMessage message) {
		return writeBytes(output -> output.writeInt(message.protocolVersion()));
	}

	public static HelloMessage decodeHello(byte[] bytes) {
		return readBytes(bytes, input -> new HelloMessage(input.readInt()));
	}

	public static byte[] encodeNoteSelection(NoteSelectionMessage message) {
		return writeBytes(output -> {
			output.writeInt(message.x());
			output.writeInt(message.y());
			output.writeInt(message.z());
			output.writeInt(message.noteValue());
		});
	}

	public static NoteSelectionMessage decodeNoteSelection(byte[] bytes) {
		return readBytes(bytes, input -> new NoteSelectionMessage(
			input.readInt(),
			input.readInt(),
			input.readInt(),
			input.readInt()
		));
	}

	private static byte[] writeBytes(IoWriter writer) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

		try (DataOutputStream output = new DataOutputStream(byteStream)) {
			writer.write(output);
			return byteStream.toByteArray();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private static <T> T readBytes(byte[] bytes, IoReader<T> reader) {
		try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
			return reader.read(input);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	@FunctionalInterface
	private interface IoWriter {
		void write(DataOutputStream output) throws IOException;
	}

	@FunctionalInterface
	private interface IoReader<T> {
		T read(DataInputStream input) throws IOException;
	}
}
