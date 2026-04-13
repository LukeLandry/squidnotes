package com.squidpowered.squidnotes.client.feature.noteblock;

import com.squidpowered.squidnotes.feature.noteblock.NoteBlockNotes;
import com.squidpowered.squidnotes.feature.noteblock.NoteBlockNotes.NoteDefinition;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NoteBlockKeyboardScreen extends Screen {
	private static final int PANEL_WIDTH = 430;
	private static final int PANEL_HEIGHT = 224;
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
	private ButtonWidget confirmButton;
	private int panelLeft;
	private int panelTop;

	public NoteBlockKeyboardScreen(BlockPos blockPos, int initialNoteValue, NoteBlockInstrument instrument) {
		super(Text.translatable("screen.squidnotes.note_block.title"));
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

		this.confirmButton = this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.squidnotes.note_block.apply"), button -> {
			this.applyPendingSelection();
		})
			.dimensions(this.panelLeft + PANEL_WIDTH - 150, this.panelTop + PANEL_HEIGHT - 28, 64, 20)
			.build());
		this.confirmButton.active = false;

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("screen.squidnotes.note_block.cancel"), button -> this.close())
			.dimensions(this.panelLeft + PANEL_WIDTH - 78, this.panelTop + PANEL_HEIGHT - 28, 64, 20)
			.build());
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.fill(0, 0, this.width, this.height, SCREEN_BACKDROP);
		context.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_WIDTH, this.panelTop + PANEL_HEIGHT, PANEL_BACKGROUND);
		context.drawStrokedRectangle(this.panelLeft, this.panelTop, PANEL_WIDTH, PANEL_HEIGHT, PANEL_BORDER);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.panelTop + 12, Colors.WHITE);

		Text committedLabel = Text.translatable("screen.squidnotes.note_block.current", NoteBlockNotes.get(this.committedNoteValue).label());
		Text pendingLabel = Text.translatable("screen.squidnotes.note_block.pending", NoteBlockNotes.get(this.pendingNoteValue).label());
		Text instrumentLabel = Text.translatable("screen.squidnotes.note_block.instrument", prettifyInstrumentName(this.instrument));

		context.drawTextWithShadow(this.textRenderer, committedLabel, this.panelLeft + 16, this.panelTop + 34, 0xFFE6D3B3);
		context.drawTextWithShadow(this.textRenderer, pendingLabel, this.panelLeft + 16, this.panelTop + 48, this.pendingNoteValue == this.committedNoteValue ? 0xFFCEBA9B : 0xFFFFD37A);
		context.drawTextWithShadow(this.textRenderer, instrumentLabel, this.panelLeft + 16, this.panelTop + 62, 0xFFCEBA9B);

		KeyBounds hoveredKey = this.getKeyAt(mouseX, mouseY);
		this.renderWhiteKeys(context, hoveredKey);
		this.renderBlackKeys(context, hoveredKey);

		if (hoveredKey != null) {
			NoteDefinition definition = NoteBlockNotes.get(hoveredKey.noteValue());
			Text hoverText = Text.literal(definition.label()).formatted(Formatting.BOLD);
			context.drawTooltip(this.textRenderer, hoverText, mouseX, mouseY);
		}

		super.render(context, mouseX, mouseY, deltaTicks);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubleClick) {
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
		this.close();
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

	private void renderWhiteKeys(DrawContext context, KeyBounds hoveredKey) {
		for (KeyBounds key : this.whiteKeys) {
			int fillColor = key.noteValue() == this.pendingNoteValue ? WHITE_KEY_PENDING : WHITE_KEY_FILL;
			context.fill(key.left(), key.top(), key.right(), key.bottom(), fillColor);
			context.drawStrokedRectangle(key.left(), key.top(), key.width(), key.height(), 0xFF4E382D);

			if (key.noteValue() == this.committedNoteValue) {
				context.drawStrokedRectangle(key.left() + 2, key.top() + 2, key.width() - 4, key.height() - 4, COMMITTED_BORDER);
			}

			if (hoveredKey != null && hoveredKey.noteValue() == key.noteValue()) {
				context.drawStrokedRectangle(key.left() + 1, key.top() + 1, key.width() - 2, key.height() - 2, HOVER_BORDER);
			}

			NoteDefinition definition = NoteBlockNotes.get(key.noteValue());
			context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(definition.label().replace("#", "♯")), key.left() + (key.width() / 2), key.bottom() - 14, 0xFF35261F);
		}
	}

	private void renderBlackKeys(DrawContext context, KeyBounds hoveredKey) {
		for (KeyBounds key : this.blackKeys) {
			int fillColor = key.noteValue() == this.pendingNoteValue ? BLACK_KEY_PENDING : BLACK_KEY_FILL;
			context.fill(key.left(), key.top(), key.right(), key.bottom(), fillColor);
			context.drawStrokedRectangle(key.left(), key.top(), key.width(), key.height(), 0xFF0C0908);

			if (key.noteValue() == this.committedNoteValue) {
				context.drawStrokedRectangle(key.left() + 2, key.top() + 2, key.width() - 4, key.height() - 4, COMMITTED_BORDER);
			}

			if (hoveredKey != null && hoveredKey.noteValue() == key.noteValue()) {
				context.drawStrokedRectangle(key.left() + 1, key.top() + 1, key.width() - 2, key.height() - 2, HOVER_BORDER);
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
		if (this.client == null) {
			return;
		}

		SoundEvent soundEvent = this.instrument.getSound().value();
		float pitch = this.instrument.canBePitched() ? NoteBlock.getNotePitch(noteValue) : 1.0F;

		this.client.getSoundManager().play(new PositionedSoundInstance(
			soundEvent,
			SoundCategory.BLOCKS,
			1.0F,
			pitch,
			SoundInstance.createRandom(),
			this.blockPos
		));
	}

	private static String prettifyInstrumentName(NoteBlockInstrument instrument) {
		String[] words = instrument.asString().split("_");
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
