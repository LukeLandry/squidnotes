package com.squidpowered.squidnotes.client.feature.noteblock;

import com.squidpowered.squidnotes.shared.noteblock.NoteBlockNotes;
import com.squidpowered.squidnotes.shared.noteblock.NoteBlockNotes.NoteDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NoteBlockKeyboardScreen extends Screen {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int PANEL_WIDTH = 430;
	private static final int PANEL_HEIGHT = 232;
	private static final int WHITE_KEY_WIDTH = 24;
	private static final int WHITE_KEY_HEIGHT = 112;
	private static final int BLACK_KEY_WIDTH = 16;
	private static final int BLACK_KEY_HEIGHT = 68;
	private static final int WHITE_KEY_GAP = 2;
	private static final int SCREEN_BACKDROP = 0xA0141618;
	private static final int PANEL_BACKGROUND = 0xF034383D;
	private static final int PANEL_BORDER = 0xFF979EA6;
	private static final int WHITE_KEY_FILL = 0xFFF6E7D5;
	private static final int WHITE_KEY_PENDING = 0xFFF3B35A;
	private static final int BLACK_KEY_FILL = 0xFF2A1C17;
	private static final int BLACK_KEY_PENDING = 0xFFE38B2C;
	private static final int COMMITTED_BORDER = 0xFF67D68F;
	private static final int HOVER_BORDER = 0xFFEAD7B7;

	private final BlockPos blockPos;
	private final int committedNoteValue;
	private final NoteBlockInstrument instrument;
	private final List<KeyBounds> whiteKeys = new ArrayList<>();
	private final List<KeyBounds> blackKeys = new ArrayList<>();
	private int pendingNoteValue;
	private int lastClickedNoteValue = -1;
	private Button confirmButton;
	private int panelLeft;
	private int panelTop;

	public NoteBlockKeyboardScreen(BlockPos blockPos, int initialNoteValue, NoteBlockInstrument instrument) {
		super(Component.translatable("screen.squidnotes.note_block.title"));
		this.blockPos = blockPos;
		this.committedNoteValue = initialNoteValue;
		this.pendingNoteValue = initialNoteValue;
		this.instrument = instrument;
	}

	@Override
	protected void init() {
		this.panelLeft = (this.width - PANEL_WIDTH) / 2;
		this.panelTop = (this.height - PANEL_HEIGHT) / 2;
		this.rebuildKeyLayout();

		this.confirmButton = this.addRenderableWidget(Button.builder(Component.translatable("screen.squidnotes.note_block.apply"), button -> {
			this.applyPendingSelection();
		})
			.bounds(this.panelLeft + PANEL_WIDTH - 150, this.panelTop + PANEL_HEIGHT - 28, 64, 20)
			.build());
		this.confirmButton.active = false;

		this.addRenderableWidget(Button.builder(Component.translatable("screen.squidnotes.note_block.cancel"), button -> this.closeScreen())
			.bounds(this.panelLeft + PANEL_WIDTH - 78, this.panelTop + PANEL_HEIGHT - 28, 64, 20)
			.build());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		context.fill(0, 0, this.width, this.height, SCREEN_BACKDROP);
		context.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + PANEL_HEIGHT, PANEL_BACKGROUND);
		context.outline(this.panelLeft, this.panelTop, PANEL_WIDTH, PANEL_HEIGHT, PANEL_BORDER);
		context.centeredText(this.font, this.title, this.width / 2, this.panelTop + 12, WHITE);

		Component committedLabel = Component.translatable("screen.squidnotes.note_block.current", formatNoteLabel(this.committedNoteValue));
		Component pendingLabel = Component.translatable("screen.squidnotes.note_block.pending", formatNoteLabel(this.pendingNoteValue));
		Component instrumentLabel = Component.translatable("screen.squidnotes.note_block.instrument", prettifyInstrumentName(this.instrument));

		context.text(this.font, committedLabel, this.panelLeft + 16, this.panelTop + 34, 0xFFE6D3B3, true);
		context.text(this.font, pendingLabel, this.panelLeft + 16, this.panelTop + 48, this.pendingNoteValue == this.committedNoteValue ? 0xFFCEBA9B : 0xFFFFD37A, true);
		context.text(this.font, instrumentLabel, this.panelLeft + 16, this.panelTop + 62, 0xFFCEBA9B, true);

		KeyBounds hoveredKey = this.getKeyAt(mouseX, mouseY);
		this.renderWhiteKeys(context, hoveredKey);
		this.renderBlackKeys(context, hoveredKey);

		if (hoveredKey != null) {
			Component hoverText = formatNoteLabel(hoveredKey.noteValue()).copy().withStyle(ChatFormatting.BOLD);
			context.setTooltipForNextFrame(this.font, hoverText, mouseX, mouseY);
		}

		super.extractRenderState(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
		if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			KeyBounds key = this.getKeyAt(click.x(), click.y());

			if (key != null) {
				boolean confirmSelection = doubleClick && key.noteValue() == this.lastClickedNoteValue;
				this.pendingNoteValue = key.noteValue();
				this.lastClickedNoteValue = key.noteValue();
				this.confirmButton.active = this.pendingNoteValue != this.committedNoteValue;
				this.playPreview(key.noteValue());

				if (confirmSelection) {
					this.applyPendingSelection();
				}

				return true;
			}
		}

		return super.mouseClicked(click, doubleClick);
	}

	private void applyPendingSelection() {
		if (this.pendingNoteValue == this.committedNoteValue) {
			return;
		}

		NoteBlockFeatureClient.sendNoteSelection(this.pendingNoteValue, this.blockPos);
		this.closeScreen();
	}

	private void closeScreen() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(null);
		}
	}

	private void rebuildKeyLayout() {
		this.whiteKeys.clear();
		this.blackKeys.clear();

		int keyboardLeft = this.panelLeft + (PANEL_WIDTH - this.getKeyboardWidth()) / 2;
		int keyboardTop = this.panelTop + 86;
		int whiteIndex = 0;

		for (NoteDefinition definition : NoteBlockNotes.NOTES) {
			if (!definition.accidental()) {
				int x = keyboardLeft + whiteIndex * (WHITE_KEY_WIDTH + WHITE_KEY_GAP);
				this.whiteKeys.add(new KeyBounds(definition.value(), x, keyboardTop, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT, false));
				whiteIndex++;
			}
		}

		for (NoteDefinition definition : NoteBlockNotes.NOTES) {
			if (!definition.accidental()) {
				continue;
			}

			int x = definition.value() == 0
				? keyboardLeft - (BLACK_KEY_WIDTH / 2)
				: keyboardLeft + this.findPreviousWhiteIndex(definition.value()) * (WHITE_KEY_WIDTH + WHITE_KEY_GAP) + WHITE_KEY_WIDTH - (BLACK_KEY_WIDTH / 2);
			this.blackKeys.add(new KeyBounds(definition.value(), x, keyboardTop, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, true));
		}
	}

	private int getKeyboardWidth() {
		int whiteKeyCount = 0;

		for (NoteDefinition definition : NoteBlockNotes.NOTES) {
			if (!definition.accidental()) {
				whiteKeyCount++;
			}
		}

		return whiteKeyCount * WHITE_KEY_WIDTH + Math.max(whiteKeyCount - 1, 0) * WHITE_KEY_GAP;
	}

	private int findPreviousWhiteIndex(int noteValue) {
		int whiteIndex = -1;

		for (NoteDefinition definition : NoteBlockNotes.NOTES) {
			if (definition.value() >= noteValue) {
				break;
			}

			if (!definition.accidental()) {
				whiteIndex++;
			}
		}

		return Math.max(whiteIndex, 0);
	}

	private void renderWhiteKeys(GuiGraphicsExtractor context, KeyBounds hoveredKey) {
		for (KeyBounds key : this.whiteKeys) {
			int fillColor = key.noteValue() == this.pendingNoteValue ? WHITE_KEY_PENDING : WHITE_KEY_FILL;
			context.fill(key.left(), key.top(), key.right(), key.bottom(), fillColor);
			context.outline(key.left(), key.top(), key.width(), key.height(), 0xFF4E382D);

			if (key.noteValue() == this.committedNoteValue) {
				context.outline(key.left() + 2, key.top() + 2, key.width() - 4, key.height() - 4, COMMITTED_BORDER);
			}

			if (hoveredKey != null && hoveredKey.noteValue() == key.noteValue()) {
				context.outline(key.left() + 1, key.top() + 1, key.width() - 2, key.height() - 2, HOVER_BORDER);
			}

			NoteDefinition definition = NoteBlockNotes.get(key.noteValue());
			context.centeredText(this.font, Component.literal(definition.label().replace("#", "#")), key.left() + (key.width() / 2), key.bottom() - 14, 0xFF35261F);
		}
	}

	private void renderBlackKeys(GuiGraphicsExtractor context, KeyBounds hoveredKey) {
		for (KeyBounds key : this.blackKeys) {
			int fillColor = key.noteValue() == this.pendingNoteValue ? BLACK_KEY_PENDING : BLACK_KEY_FILL;
			context.fill(key.left(), key.top(), key.right(), key.bottom(), fillColor);
			context.outline(key.left(), key.top(), key.width(), key.height(), 0xFF0C0908);

			if (key.noteValue() == this.committedNoteValue) {
				context.outline(key.left() + 2, key.top() + 2, key.width() - 4, key.height() - 4, COMMITTED_BORDER);
			}

			if (hoveredKey != null && hoveredKey.noteValue() == key.noteValue()) {
				context.outline(key.left() + 1, key.top() + 1, key.width() - 2, key.height() - 2, HOVER_BORDER);
			}
		}
	}

	private KeyBounds getKeyAt(double mouseX, double mouseY) {
		for (KeyBounds key : this.blackKeys) {
			if (key.contains(mouseX, mouseY)) {
				return key;
			}
		}

		for (KeyBounds key : this.whiteKeys) {
			if (key.contains(mouseX, mouseY)) {
				return key;
			}
		}

		return null;
	}

	private void playPreview(int noteValue) {
		if (this.minecraft == null) {
			return;
		}

		SoundEvent soundEvent = this.instrument.getSoundEvent().value();
		float pitch = this.instrument.isTunable() ? NoteBlock.getPitchFromNote(noteValue) : 1.0F;

		this.minecraft.getSoundManager().play(new SimpleSoundInstance(
			soundEvent,
			SoundSource.BLOCKS,
			1.0F,
			pitch,
			SoundInstance.createUnseededRandom(),
			this.blockPos
		));
	}

	private static Component formatNoteLabel(int noteValue) {
		NoteDefinition definition = NoteBlockNotes.get(noteValue);
		return Component.literal(definition.label() + " (" + definition.value() + ")");
	}

	private static String prettifyInstrumentName(NoteBlockInstrument instrument) {
		String[] words = instrument.getSerializedName().split("_");
		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < words.length; index++) {
			if (index > 0) {
				builder.append(' ');
			}

			String word = words[index];
			builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}

		return builder.toString();
	}

	private record KeyBounds(int noteValue, int left, int top, int width, int height, boolean black) {
		private int right() {
			return this.left + this.width;
		}

		private int bottom() {
			return this.top + this.height;
		}

		private boolean contains(double mouseX, double mouseY) {
			return mouseX >= this.left && mouseX < this.right() && mouseY >= this.top && mouseY < this.bottom();
		}
	}
}
