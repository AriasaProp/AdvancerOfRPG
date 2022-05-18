package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont.BitmapFontData;
import com.ariasaproject.advancerofrpg.graphics.g2d.GlyphLayout;
import com.ariasaproject.advancerofrpg.graphics.g2d.GlyphLayout.GlyphRun;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.input.Input.Keys;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.Group;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ChangeListener.ChangeEvent;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ClickListener;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Disableable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.FloatArray;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pools;
import com.ariasaproject.advancerofrpg.utils.Timer;
import com.ariasaproject.advancerofrpg.utils.Timer.Task;

public class TextField extends Widget implements Disableable {
	static protected final char BACKSPACE = 8;
	static protected final char CARRIAGE_RETURN = '\r';
	static protected final char NEWLINE = '\n';
	static protected final char TAB = '\t';
	static protected final char DELETE = 127;
	static protected final char BULLET = 149;

	static private final Vector2 tmp1 = new Vector2();
	static private final Vector2 tmp2 = new Vector2();
	static private final Vector2 tmp3 = new Vector2();

	static public float keyRepeatInitialTime = 0.4f;
	static public float keyRepeatTime = 0.1f;
	protected final GlyphLayout layout = new GlyphLayout();
	protected final FloatArray glyphPositions = new FloatArray();
	final KeyRepeatTask keyRepeatTask = new KeyRepeatTask();
	protected String text;
	protected int cursor, selectionStart;
	protected boolean hasSelection;
	protected boolean writeEnters;
	protected CharSequence displayText;
	protected float fontOffset, textHeight, textOffset;
	TextFieldStyle style;
	Clipboard clipboard;
	InputListener inputListener;
	@Null
	TextFieldListener listener;
	@Null
	TextFieldFilter filter;
	OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();
	boolean focusTraversal = true, onlyFontChars = true, disabled;
	String undoText = "";
	long lastChangeTime;
	boolean passwordMode;
	float renderOffset;
	boolean focused;
	boolean cursorOn;
	final Task blinkTask = new Task() {
		@Override
		public void run() {
			if (getStage() == null) {
				cancel();
				return;
			}
			cursorOn = !cursorOn;
		}
	};
	float blinkTime = 0.32f;
	boolean programmaticChangeEvents;
	private String messageText;
	private int textHAlign = Align.left;
	private float selectionX, selectionWidth;
	private StringBuilder passwordBuffer;
	private char passwordCharacter = BULLET;
	private int visibleTextStart, visibleTextEnd;
	private int maxLength;

	public TextField(@Null String text, Skin skin) {
		this(text, skin.get(TextFieldStyle.class));
	}

	public TextField(@Null String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, TextFieldStyle.class));
	}

	public TextField(@Null String text, TextFieldStyle style) {
		setStyle(style);
		clipboard = GraphFunc.app.getClipboard();
		initialize();
		setText(text);
		setSize(getPrefWidth(), getPrefHeight());
	}

	protected void initialize() {
		addListener(inputListener = createInputListener());
	}

	protected InputListener createInputListener() {
		return new TextFieldClickListener();
	}

	protected int letterUnderCursor(float x) {
		x -= textOffset + fontOffset - style.font.getData().cursorX - glyphPositions.get(visibleTextStart);
		Drawable background = getBackgroundDrawable();
		if (background != null)
			x -= style.background.getLeftWidth();
		int n = this.glyphPositions.size;
		float[] glyphPositions = this.glyphPositions.items;
		for (int i = 1; i < n; i++) {
			if (glyphPositions[i] > x) {
				if (glyphPositions[i] - x <= x - glyphPositions[i - 1])
					return i;
				return i - 1;
			}
		}
		return n - 1;
	}

	protected boolean isWordCharacter(char c) {
		return Character.isLetterOrDigit(c);
	}

	protected int[] wordUnderCursor(int at) {
		String text = this.text;
		int start = at, right = text.length(), left = 0, index = start;
		if (at >= text.length()) {
			left = text.length();
			right = 0;
		} else {
			for (; index < right; index++) {
				if (!isWordCharacter(text.charAt(index))) {
					right = index;
					break;
				}
			}
			for (index = start - 1; index > -1; index--) {
				if (!isWordCharacter(text.charAt(index))) {
					left = index + 1;
					break;
				}
			}
		}
		return new int[] { left, right };
	}

	int[] wordUnderCursor(float x) {
		return wordUnderCursor(letterUnderCursor(x));
	}

	boolean withinMaxLength(int size) {
		return maxLength <= 0 || size < maxLength;
	}

	public int getMaxLength() {
		return this.maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * When false, text set by {@link #setText(String)} may contain characters not
	 * in the font, a space will be displayed instead. When true (the default),
	 * characters not in the font are stripped by setText. Characters not in the
	 * font are always stripped when typed or pasted.
	 */
	public void setOnlyFontChars(boolean onlyFontChars) {
		this.onlyFontChars = onlyFontChars;
	}

	/**
	 * Returns the text field's style. Modifying the returned style may not have an
	 * effect until {@link #setStyle(TextFieldStyle)} is called.
	 */
	public TextFieldStyle getStyle() {
		return style;
	}

	public void setStyle(TextFieldStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		textHeight = style.font.getCapHeight() - style.font.getDescent() * 2;
		invalidateHierarchy();
	}

	protected void calculateOffsets() {
		float visibleWidth = getWidth();
		Drawable background = getBackgroundDrawable();
		if (background != null)
			visibleWidth -= background.getLeftWidth() + background.getRightWidth();
		int glyphCount = glyphPositions.size;
		float[] glyphPositions = this.glyphPositions.items;
		// Check if the cursor has gone out the left or right side of the visible area
		// and adjust renderOffset.
		float distance = glyphPositions[Math.max(0, cursor - 1)] + renderOffset;
		if (distance <= 0)
			renderOffset -= distance;
		else {
			int index = Math.min(glyphCount - 1, cursor + 1);
			float minX = glyphPositions[index] - visibleWidth;
			if (-renderOffset < minX)
				renderOffset = -minX;
		}
		// Prevent renderOffset from starting too close to the end, eg after text was
		// deleted.
		float maxOffset = 0;
		float width = glyphPositions[glyphCount - 1];
		for (int i = glyphCount - 2; i >= 0; i--) {
			float x = glyphPositions[i];
			if (width - x > visibleWidth)
				break;
			maxOffset = x;
		}
		if (-renderOffset > maxOffset)
			renderOffset = -maxOffset;
		// calculate first visible char based on render offset
		visibleTextStart = 0;
		float startX = 0;
		for (int i = 0; i < glyphCount; i++) {
			if (glyphPositions[i] >= -renderOffset) {
				visibleTextStart = i;
				startX = glyphPositions[i];
				break;
			}
		}
		// calculate last visible char based on visible width and render offset
		int end = visibleTextStart + 1;
		float endX = visibleWidth - renderOffset;
		for (int n = Math.min(displayText.length(), glyphCount); end <= n; end++)
			if (glyphPositions[end] > endX)
				break;
		visibleTextEnd = Math.max(0, end - 1);
		if ((textHAlign & Align.left) == 0) {
			textOffset = visibleWidth - glyphPositions[visibleTextEnd] - fontOffset + startX;
			if ((textHAlign & Align.center) != 0)
				textOffset = Math.round(textOffset * 0.5f);
		} else
			textOffset = startX + renderOffset;
		// calculate selection x position and width
		if (hasSelection) {
			int minIndex = Math.min(cursor, selectionStart);
			int maxIndex = Math.max(cursor, selectionStart);
			float minX = Math.max(glyphPositions[minIndex] - glyphPositions[visibleTextStart], -textOffset);
			float maxX = Math.min(glyphPositions[maxIndex] - glyphPositions[visibleTextStart], visibleWidth - textOffset);
			selectionX = minX;
			selectionWidth = maxX - minX - style.font.getData().cursorX;
		}
	}

	@Null
	private Drawable getBackgroundDrawable() {
		boolean focused = hasKeyboardFocus();
		return (disabled && style.disabledBackground != null) ? style.disabledBackground : ((focused && style.focusedBackground != null) ? style.focusedBackground : style.background);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		boolean focused = hasKeyboardFocus();
		if (focused != this.focused || (focused && !blinkTask.isScheduled())) {
			this.focused = focused;
			blinkTask.cancel();
			cursorOn = focused;
			if (focused)
				Timer.schedule(blinkTask, blinkTime, blinkTime);
			else
				keyRepeatTask.cancel();
		} else if (!focused) //
			cursorOn = false;
		final BitmapFont font = style.font;
		final Color fontColor = (disabled && style.disabledFontColor != null) ? style.disabledFontColor : ((focused && style.focusedFontColor != null) ? style.focusedFontColor : style.fontColor);
		final Drawable selection = style.selection;
		final Drawable cursorPatch = style.cursor;
		final Drawable background = getBackgroundDrawable();
		Color color = getColor();
		float x = getX();
		float y = getY();
		float width = getWidth();
		float height = getHeight();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		float bgLeftWidth = 0, bgRightWidth = 0;
		if (background != null) {
			background.draw(batch, x, y, width, height);
			bgLeftWidth = background.getLeftWidth();
			bgRightWidth = background.getRightWidth();
		}
		float textY = getTextY(font, background);
		calculateOffsets();
		if (focused && hasSelection && selection != null) {
			drawSelection(selection, batch, font, x + bgLeftWidth, y + textY);
		}
		float yOffset = /* font.isFlipped() ? -textHeight : */ 0;
		if (displayText.length() == 0) {
			if (!focused && messageText != null) {
				BitmapFont messageFont = style.messageFont != null ? style.messageFont : font;
				if (style.messageFontColor != null) {
					messageFont.setColor(style.messageFontColor.r, style.messageFontColor.g, style.messageFontColor.b, style.messageFontColor.a * color.a * parentAlpha);
				} else
					messageFont.setColor(0.7f, 0.7f, 0.7f, color.a * parentAlpha);
				drawMessageText(batch, messageFont, x + bgLeftWidth, y + textY + yOffset, width - bgLeftWidth - bgRightWidth);
			}
		} else {
			font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * color.a * parentAlpha);
			drawText(batch, font, x + bgLeftWidth, y + textY + yOffset);
		}
		if (!disabled && cursorOn && cursorPatch != null) {
			drawCursor(cursorPatch, batch, font, x + bgLeftWidth, y + textY);
		}
	}

	protected float getTextY(BitmapFont font, @Null Drawable background) {
		float height = getHeight();
		float textY = textHeight / 2 + font.getDescent();
		if (background != null) {
			float bottom = background.getBottomHeight();
			textY = textY + (height - background.getTopHeight() - bottom) / 2 + bottom;
		} else {
			textY = textY + height / 2;
		}
		if (font.usesIntegerPositions())
			textY = (int) textY;
		return textY;
	}

	/**
	 * Draws selection rectangle
	 **/
	protected void drawSelection(Drawable selection, Batch batch, BitmapFont font, float x, float y) {
		selection.draw(batch, x + textOffset + selectionX + fontOffset, y - textHeight - font.getDescent(), selectionWidth, textHeight);
	}

	protected void drawText(Batch batch, BitmapFont font, float x, float y) {
		font.draw(batch, displayText, x + textOffset, y, visibleTextStart, visibleTextEnd, 0, Align.left, false);
	}

	protected void drawMessageText(Batch batch, BitmapFont font, float x, float y, float maxWidth) {
		font.draw(batch, messageText, x, y, 0, messageText.length(), maxWidth, textHAlign, false, "...");
	}

	protected void drawCursor(Drawable cursorPatch, Batch batch, BitmapFont font, float x, float y) {
		cursorPatch.draw(batch, x + textOffset + glyphPositions.get(cursor) - glyphPositions.get(visibleTextStart) + fontOffset + font.getData().cursorX, y - textHeight - font.getDescent(), cursorPatch.getMinWidth(), textHeight);
	}

	void updateDisplayText() {
		BitmapFont font = style.font;
		BitmapFontData data = font.getData();
		String text = this.text;
		int textLength = text.length();
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < textLength; i++) {
			char c = text.charAt(i);
			buffer.append(data.hasGlyph(c) ? c : ' ');
		}
		String newDisplayText = buffer.toString();
		if (passwordMode && data.hasGlyph(passwordCharacter)) {
			if (passwordBuffer == null)
				passwordBuffer = new StringBuilder(newDisplayText.length());
			if (passwordBuffer.length() > textLength)
				passwordBuffer.setLength(textLength);
			else {
				for (int i = passwordBuffer.length(); i < textLength; i++)
					passwordBuffer.append(passwordCharacter);
			}
			displayText = passwordBuffer;
		} else
			displayText = newDisplayText;
		layout.setText(font, displayText.toString().replace('\r', ' ').replace('\n', ' '));
		glyphPositions.clear();
		float x = 0;
		if (layout.runs.size > 0) {
			GlyphRun run = layout.runs.first();
			FloatArray xAdvances = run.xAdvances;
			fontOffset = xAdvances.first();
			for (int i = 1, n = xAdvances.size; i < n; i++) {
				glyphPositions.add(x);
				x += xAdvances.get(i);
			}
		} else
			fontOffset = 0;
		glyphPositions.add(x);
		visibleTextStart = Math.min(visibleTextStart, glyphPositions.size - 1);
		visibleTextEnd = MathUtils.clamp(visibleTextEnd, visibleTextStart, glyphPositions.size - 1);
		if (selectionStart > newDisplayText.length())
			selectionStart = textLength;
	}

	/**
	 * Copies the contents of this TextField to the {@link Clipboard} implementation
	 * set on this TextField.
	 */
	public void copy() {
		if (hasSelection && !passwordMode) {
			clipboard.setContents(text.substring(Math.min(cursor, selectionStart), Math.max(cursor, selectionStart)));
		}
	}

	/**
	 * Copies the selected contents of this TextField to the {@link Clipboard}
	 * implementation set on this TextField, then removes it.
	 */
	public void cut() {
		cut(programmaticChangeEvents);
	}

	void cut(boolean fireChangeEvent) {
		if (hasSelection && !passwordMode) {
			copy();
			cursor = delete(fireChangeEvent);
			updateDisplayText();
		}
	}

	void paste(@Null String content, boolean fireChangeEvent) {
		if (content == null)
			return;
		StringBuilder buffer = new StringBuilder();
		int textLength = text.length();
		if (hasSelection)
			textLength -= Math.abs(cursor - selectionStart);
		BitmapFontData data = style.font.getData();
		for (int i = 0, n = content.length(); i < n; i++) {
			if (!withinMaxLength(textLength + buffer.length()))
				break;
			char c = content.charAt(i);
			if (!(writeEnters && (c == NEWLINE || c == CARRIAGE_RETURN))) {
				if (c == '\r' || c == '\n')
					continue;
				if (onlyFontChars && !data.hasGlyph(c))
					continue;
				if (filter != null && !filter.acceptChar(this, c))
					continue;
			}
			buffer.append(c);
		}
		content = buffer.toString();
		if (hasSelection)
			cursor = delete(fireChangeEvent);
		if (fireChangeEvent)
			changeText(text, insert(cursor, content, text));
		else
			text = insert(cursor, content, text);
		updateDisplayText();
		cursor += content.length();
	}

	String insert(int position, CharSequence text, String to) {
		if (to.length() == 0)
			return text.toString();
		return to.substring(0, position) + text + to.substring(position);
	}

	int delete(boolean fireChangeEvent) {
		int from = selectionStart;
		int to = cursor;
		int minIndex = Math.min(from, to);
		int maxIndex = Math.max(from, to);
		String newText = (minIndex > 0 ? text.substring(0, minIndex) : "") + (maxIndex < text.length() ? text.substring(maxIndex) : "");
		if (fireChangeEvent)
			changeText(text, newText);
		else
			text = newText;
		clearSelection();
		return minIndex;
	}

	/**
	 * Sets the {@link Stage#setKeyboardFocus(Actor) keyboard focus} to the next
	 * TextField. If no next text field is found, the onscreen keyboard is hidden.
	 * Does nothing if the text field is not in a stage.
	 *
	 * @param up If true, the text field with the same or next smallest y coordinate
	 *           is found, else the next highest.
	 */
	public void next(boolean up) {
		Stage stage = getStage();
		if (stage == null)
			return;
		TextField current = this;
		Vector2 currentCoords = current.getParent().localToStageCoordinates(tmp2.set(current.getX(), current.getY()));
		Vector2 bestCoords = tmp1;
		while (true) {
			TextField textField = current.findNextTextField(stage.getActors(), null, bestCoords, currentCoords, up);
			if (textField == null) { // Try to wrap around.
				if (up)
					currentCoords.set(-Float.MAX_VALUE, -Float.MAX_VALUE);
				else
					currentCoords.set(Float.MAX_VALUE, Float.MAX_VALUE);
				textField = current.findNextTextField(stage.getActors(), null, bestCoords, currentCoords, up);
			}
			if (textField == null) {
				GraphFunc.app.getInput().setOnscreenKeyboardVisible(false);
				break;
			}
			if (stage.setKeyboardFocus(textField)) {
				textField.selectAll();
				break;
			}
			current = textField;
			currentCoords.set(bestCoords);
		}
	}

	/**
	 * @return May be null.
	 */
	@Null
	private TextField findNextTextField(Array<Actor> actors, @Null TextField best, Vector2 bestCoords, Vector2 currentCoords, boolean up) {
		for (int i = 0, n = actors.size; i < n; i++) {
			Actor actor = actors.get(i);
			if (actor instanceof TextField) {
				if (actor == this)
					continue;
				TextField textField = (TextField) actor;
				if (textField.isDisabled() || !textField.focusTraversal || !textField.ancestorsVisible())
					continue;
				Vector2 actorCoords = actor.getParent().localToStageCoordinates(tmp3.set(actor.getX(), actor.getY()));
				boolean below = actorCoords.y != currentCoords.y && (actorCoords.y < currentCoords.y ^ up);
				boolean right = actorCoords.y == currentCoords.y && (actorCoords.x > currentCoords.x ^ up);
				if (!below && !right)
					continue;
				boolean better = best == null || (actorCoords.y != bestCoords.y && (actorCoords.y > bestCoords.y ^ up));
				if (!better)
					better = actorCoords.y == bestCoords.y && (actorCoords.x < bestCoords.x ^ up);
				if (better) {
					best = (TextField) actor;
					bestCoords.set(actorCoords);
				}
			} else if (actor instanceof Group)
				best = findNextTextField(((Group) actor).getChildren(), best, bestCoords, currentCoords, up);
		}
		return best;
	}

	public InputListener getDefaultInputListener() {
		return inputListener;
	}

	/**
	 * @param listener May be null.
	 */
	public void setTextFieldListener(@Null TextFieldListener listener) {
		this.listener = listener;
	}

	@Null
	public TextFieldFilter getTextFieldFilter() {
		return filter;
	}

	/**
	 * @param filter May be null.
	 */
	public void setTextFieldFilter(@Null TextFieldFilter filter) {
		this.filter = filter;
	}

	/**
	 * If true (the default), tab/shift+tab will move to the next text field.
	 */
	public void setFocusTraversal(boolean focusTraversal) {
		this.focusTraversal = focusTraversal;
	}

	/**
	 * @return May be null.
	 */
	@Null
	public String getMessageText() {
		return messageText;
	}

	/**
	 * Sets the text that will be drawn in the text field if no text has been
	 * entered.
	 *
	 * @param messageText may be null.
	 */
	public void setMessageText(@Null String messageText) {
		this.messageText = messageText;
	}

	/**
	 * @param str If null, "" is used.
	 */
	public void appendText(@Null String str) {
		if (str == null)
			str = "";
		clearSelection();
		cursor = text.length();
		paste(str, programmaticChangeEvents);
	}

	/**
	 * @return Never null, might be an empty string.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param str If null, "" is used.
	 */
	public void setText(@Null String str) {
		if (str == null)
			str = "";
		if (str.equals(text))
			return;
		clearSelection();
		String oldText = text;
		text = "";
		paste(str, false);
		if (programmaticChangeEvents)
			changeText(oldText, text);
		cursor = 0;
	}

	/**
	 * @return True if the text was changed.
	 */
	boolean changeText(String oldText, String newText) {
		if (newText.equals(oldText))
			return false;
		text = newText;
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		boolean cancelled = fire(changeEvent);
		if (cancelled)
			text = oldText;
		Pools.free(changeEvent);
		return !cancelled;
	}

	public boolean getProgrammaticChangeEvents() {
		return programmaticChangeEvents;
	}

	/**
	 * If false, methods that change the text will not fire {@link ChangeEvent}, the
	 * event will be fired only when user changes the text.
	 */
	public void setProgrammaticChangeEvents(boolean programmaticChangeEvents) {
		this.programmaticChangeEvents = programmaticChangeEvents;
	}

	public int getSelectionStart() {
		return selectionStart;
	}

	public String getSelection() {
		return hasSelection ? text.substring(Math.min(selectionStart, cursor), Math.max(selectionStart, cursor)) : "";
	}

	/**
	 * Sets the selected text.
	 */
	public void setSelection(int selectionStart, int selectionEnd) {
		if (selectionStart < 0)
			throw new IllegalArgumentException("selectionStart must be >= 0");
		if (selectionEnd < 0)
			throw new IllegalArgumentException("selectionEnd must be >= 0");
		selectionStart = Math.min(text.length(), selectionStart);
		selectionEnd = Math.min(text.length(), selectionEnd);
		if (selectionEnd == selectionStart) {
			clearSelection();
			return;
		}
		if (selectionEnd < selectionStart) {
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}
		hasSelection = true;
		this.selectionStart = selectionStart;
		cursor = selectionEnd;
	}

	public void selectAll() {
		setSelection(0, text.length());
	}

	public void clearSelection() {
		hasSelection = false;
	}

	public int getCursorPosition() {
		return cursor;
	}

	/**
	 * Sets the cursor position and clears any selection.
	 */
	public void setCursorPosition(int cursorPosition) {
		if (cursorPosition < 0)
			throw new IllegalArgumentException("cursorPosition must be >= 0");
		clearSelection();
		cursor = Math.min(cursorPosition, text.length());
	}

	/**
	 * Default is an instance of {@link DefaultOnscreenKeyboard}.
	 */
	public OnscreenKeyboard getOnscreenKeyboard() {
		return keyboard;
	}

	public void setOnscreenKeyboard(OnscreenKeyboard keyboard) {
		this.keyboard = keyboard;
	}

	public void setClipboard(Clipboard clipboard) {
		this.clipboard = clipboard;
	}

	@Override
	public float getPrefWidth() {
		return 150;
	}

	@Override
	public float getPrefHeight() {
		float topAndBottom = 0, minHeight = 0;
		if (style.background != null) {
			topAndBottom = Math.max(topAndBottom, style.background.getBottomHeight() + style.background.getTopHeight());
			minHeight = Math.max(minHeight, style.background.getMinHeight());
		}
		if (style.focusedBackground != null) {
			topAndBottom = Math.max(topAndBottom, style.focusedBackground.getBottomHeight() + style.focusedBackground.getTopHeight());
			minHeight = Math.max(minHeight, style.focusedBackground.getMinHeight());
		}
		if (style.disabledBackground != null) {
			topAndBottom = Math.max(topAndBottom, style.disabledBackground.getBottomHeight() + style.disabledBackground.getTopHeight());
			minHeight = Math.max(minHeight, style.disabledBackground.getMinHeight());
		}
		return Math.max(topAndBottom + textHeight, minHeight);
	}

	public int getAlignment() {
		return textHAlign;
	}

	/**
	 * Sets text horizontal alignment (left, center or right).
	 *
	 * @see Align
	 */
	public void setAlignment(int alignment) {
		this.textHAlign = alignment;
	}

	public boolean isPasswordMode() {
		return passwordMode;
	}

	/**
	 * If true, the text in this text field will be shown as bullet characters.
	 *
	 * @see #setPasswordCharacter(char)
	 */
	public void setPasswordMode(boolean passwordMode) {
		this.passwordMode = passwordMode;
		updateDisplayText();
	}

	/**
	 * Sets the password character for the text field. The character must be present
	 * in the {@link BitmapFont}. Default is 149 (bullet).
	 */
	public void setPasswordCharacter(char passwordCharacter) {
		this.passwordCharacter = passwordCharacter;
		if (passwordMode)
			updateDisplayText();
	}

	public void setBlinkTime(float blinkTime) {
		this.blinkTime = blinkTime;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	protected void moveCursor(boolean forward, boolean jump) {
		int limit = forward ? text.length() : 0;
		int charOffset = forward ? 0 : -1;
		while ((forward ? ++cursor < limit : --cursor > limit) && jump) {
			if (!continueCursor(cursor, charOffset))
				break;
		}
	}

	protected boolean continueCursor(int index, int offset) {
		char c = text.charAt(index + offset);
		return isWordCharacter(c);
	}

	/**
	 * Interface for listening to typed characters.
	 *
	 * @author mzechner
	 */
	public interface TextFieldListener {
		void keyTyped(TextField textField, char c);
	}

	/**
	 * Interface for filtering characters entered into the text field.
	 *
	 * @author mzechner
	 */
	public interface TextFieldFilter {
		boolean acceptChar(TextField textField, char c);

		class DigitsOnlyFilter implements TextFieldFilter {
			@Override
			public boolean acceptChar(TextField textField, char c) {
				return Character.isDigit(c);
			}
		}
	}

	/**
	 * An interface for onscreen keyboards. Can invoke the default keyboard or
	 * render your own keyboard!
	 *
	 * @author mzechner
	 */
	public interface OnscreenKeyboard {
		void show(boolean visible);
	}

	/**
	 * The default {@link OnscreenKeyboard} used by all {@link TextField} instances.
	 * Just uses {@link Input#setOnscreenKeyboardVisible(boolean)} as appropriate.
	 * Might overlap your actual rendering, so use with care!
	 *
	 * @author mzechner
	 */
	static public class DefaultOnscreenKeyboard implements OnscreenKeyboard {
		@Override
		public void show(boolean visible) {
			GraphFunc.app.getInput().setOnscreenKeyboardVisible(visible);
		}
	}

	/**
	 * The style for a text field, see {@link TextField}.
	 *
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	static public class TextFieldStyle {
		public BitmapFont font;
		public Color fontColor;
		/**
		 * Optional.
		 */
		@Null
		public Color focusedFontColor, disabledFontColor;
		/**
		 * Optional.
		 */
		@Null
		public Drawable background, focusedBackground, disabledBackground, cursor, selection;
		/**
		 * Optional.
		 */
		@Null
		public BitmapFont messageFont;
		/**
		 * Optional.
		 */
		@Null
		public Color messageFontColor;

		public TextFieldStyle() {
		}

		public TextFieldStyle(BitmapFont font, Color fontColor, @Null Drawable cursor, @Null Drawable selection, @Null Drawable background) {
			this.background = background;
			this.cursor = cursor;
			this.font = font;
			this.fontColor = fontColor;
			this.selection = selection;
		}

		public TextFieldStyle(TextFieldStyle style) {
			this.messageFont = style.messageFont;
			if (style.messageFontColor != null)
				this.messageFontColor = new Color(style.messageFontColor);
			this.background = style.background;
			this.focusedBackground = style.focusedBackground;
			this.disabledBackground = style.disabledBackground;
			this.cursor = style.cursor;
			this.font = style.font;
			if (style.fontColor != null)
				this.fontColor = new Color(style.fontColor);
			if (style.focusedFontColor != null)
				this.focusedFontColor = new Color(style.focusedFontColor);
			if (style.disabledFontColor != null)
				this.disabledFontColor = new Color(style.disabledFontColor);
			this.selection = style.selection;
		}
	}

	class KeyRepeatTask extends Task {
		int keycode;

		@Override
		public void run() {
			if (getStage() == null) {
				cancel();
				return;
			}
			inputListener.keyDown(null, keycode);
		}
	}

	/**
	 * Basic input listener for the text field
	 */
	public class TextFieldClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			int count = getTapCount() % 4;
			if (count == 0)
				clearSelection();
			if (count == 2) {
				int[] array = wordUnderCursor(x);
				setSelection(array[0], array[1]);
			}
			if (count == 3)
				selectAll();
		}

		@Override
		public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
			if (!super.touchDown(event, x, y, pointer, button))
				return false;
			if (pointer == 0 && button != 0)
				return false;
			if (disabled)
				return true;
			setCursorPosition(x, y);
			selectionStart = cursor;
			Stage stage = getStage();
			if (stage != null)
				stage.setKeyboardFocus(TextField.this);
			keyboard.show(true);
			hasSelection = true;
			return true;
		}

		@Override
		public void touchDragged(InputEvent event, float x, float y, int pointer) {
			super.touchDragged(event, x, y, pointer);
			setCursorPosition(x, y);
		}

		@Override
		public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
			if (selectionStart == cursor)
				hasSelection = false;
			super.touchUp(event, x, y, pointer, button);
		}

		protected void setCursorPosition(float x, float y) {
			cursor = letterUnderCursor(x);
			cursorOn = focused;
			blinkTask.cancel();
			if (focused)
				Timer.schedule(blinkTask, blinkTime, blinkTime);
		}

		protected void goHome(boolean jump) {
			cursor = 0;
		}

		protected void goEnd(boolean jump) {
			cursor = text.length();
		}

		@Override
		public boolean keyDown(InputEvent event, int keycode) {
			if (disabled)
				return false;
			cursorOn = focused;
			blinkTask.cancel();
			if (focused)
				Timer.schedule(blinkTask, blinkTime, blinkTime);
			if (!hasKeyboardFocus())
				return false;
			boolean repeat = false;
			boolean ctrl = GraphFunc.app.getInput().isKeyPressed(Keys.CONTROL_LEFT) || GraphFunc.app.getInput().isKeyPressed(Keys.CONTROL_RIGHT);
			boolean jump = ctrl && !passwordMode;
			boolean handled = true;
			if (ctrl) {
				switch (keycode) {
				case Keys.V:
					paste(clipboard.getContents(), true);
					repeat = true;
					break;
				case Keys.C:
				case Keys.INSERT:
					copy();
					return true;
				case Keys.X:
					cut(true);
					return true;
				case Keys.A:
					selectAll();
					return true;
				case Keys.Z:
					String oldText = text;
					setText(undoText);
					undoText = oldText;
					updateDisplayText();
					return true;
				default:
					handled = false;
				}
			}
			if (GraphFunc.app.getInput().isKeyPressed(Keys.SHIFT_LEFT) || GraphFunc.app.getInput().isKeyPressed(Keys.SHIFT_RIGHT)) {
				switch (keycode) {
				case Keys.INSERT:
					paste(clipboard.getContents(), true);
					break;
				case Keys.FORWARD_DEL:
					cut(true);
					break;
				}
				selection: {
					int temp = cursor;
					keys: {
						switch (keycode) {
						case Keys.LEFT:
							moveCursor(false, jump);
							repeat = true;
							handled = true;
							break keys;
						case Keys.RIGHT:
							moveCursor(true, jump);
							repeat = true;
							handled = true;
							break keys;
						case Keys.HOME:
							goHome(jump);
							handled = true;
							break keys;
						case Keys.END:
							goEnd(jump);
							handled = true;
							break keys;
						}
						break selection;
					}
					if (!hasSelection) {
						selectionStart = temp;
						hasSelection = true;
					}
				}
			} else {
				// Cursor movement or other keys (kills selection).
				switch (keycode) {
				case Keys.LEFT:
					moveCursor(false, jump);
					clearSelection();
					repeat = true;
					handled = true;
					break;
				case Keys.RIGHT:
					moveCursor(true, jump);
					clearSelection();
					repeat = true;
					handled = true;
					break;
				case Keys.HOME:
					goHome(jump);
					clearSelection();
					handled = true;
					break;
				case Keys.END:
					goEnd(jump);
					clearSelection();
					handled = true;
					break;
				}
			}
			cursor = MathUtils.clamp(cursor, 0, text.length());
			if (repeat)
				scheduleKeyRepeatTask(keycode);
			return handled;
		}

		protected void scheduleKeyRepeatTask(int keycode) {
			if (!keyRepeatTask.isScheduled() || keyRepeatTask.keycode != keycode) {
				keyRepeatTask.keycode = keycode;
				keyRepeatTask.cancel();
				Timer.schedule(keyRepeatTask, keyRepeatInitialTime, keyRepeatTime);
			}
		}

		@Override
		public boolean keyUp(InputEvent event, int keycode) {
			if (disabled)
				return false;
			keyRepeatTask.cancel();
			return true;
		}

		/**
		 * Checks if focus traversal should be triggered. The default implementation
		 * uses {@link TextField#focusTraversal} and the typed character, depending on
		 * the OS.
		 *
		 * @param character The character that triggered a possible focus traversal.
		 * @return true if the focus should change to the {@link TextField#next(boolean)
		 *         next} input field.
		 */
		protected boolean checkFocusTraversal(char character) {
			return focusTraversal && (character == TAB || ((character == CARRIAGE_RETURN || character == NEWLINE)));
		}

		@Override
		public boolean keyTyped(InputEvent event, char character) {
			if (disabled)
				return false;
			// Disallow "typing" most ASCII control characters, which would show up as a
			// space when onlyFontChars is true.
			switch (character) {
			case BACKSPACE:
			case TAB:
			case NEWLINE:
			case CARRIAGE_RETURN:
				break;
			default:
				if (character < 32)
					return false;
			}
			if (!hasKeyboardFocus())
				return false;
			if (checkFocusTraversal(character))
				next(GraphFunc.app.getInput().isKeyPressed(Keys.SHIFT_LEFT) || GraphFunc.app.getInput().isKeyPressed(Keys.SHIFT_RIGHT));
			else {
				boolean enter = character == CARRIAGE_RETURN || character == NEWLINE;
				boolean delete = character == DELETE;
				boolean backspace = character == BACKSPACE;
				boolean add = enter ? writeEnters : (!onlyFontChars || style.font.getData().hasGlyph(character));
				boolean remove = backspace || delete;
				if (add || remove) {
					String oldText = text;
					int oldCursor = cursor;
					if (remove) {
						if (hasSelection)
							cursor = delete(false);
						else {
							if (backspace && cursor > 0) {
								text = text.substring(0, cursor - 1) + text.substring(cursor--);
								renderOffset = 0;
							}
							if (delete && cursor < text.length()) {
								text = text.substring(0, cursor) + text.substring(cursor + 1);
							}
						}
					}
					if (add && !remove) {
						// Character may be added to the text.
						if (!enter && filter != null && !filter.acceptChar(TextField.this, character))
							return true;
						if (!withinMaxLength(text.length() - (hasSelection ? Math.abs(cursor - selectionStart) : 0)))
							return true;
						if (hasSelection)
							cursor = delete(false);
						String insertion = enter ? "\n" : String.valueOf(character);
						text = insert(cursor++, insertion, text);
					}
					//String tempUndoText = undoText;
					if (changeText(oldText, text)) {
						long time = System.currentTimeMillis();
						if (time - 750 > lastChangeTime)
							undoText = oldText;
						lastChangeTime = time;
					} else
						cursor = oldCursor;
					updateDisplayText();
				}
			}
			if (listener != null)
				listener.keyTyped(TextField.this, character);
			return true;
		}
	}
}
