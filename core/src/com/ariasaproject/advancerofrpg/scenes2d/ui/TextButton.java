package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Label.LabelStyle;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Null;

/**
 * A button with a child {@link Label} to display text.
 *
 * @author Nathan Sweet
 */
public class TextButton extends Button {
	private Label label;
	private TextButtonStyle style;

	public TextButton(@Null String text, Skin skin) {
		this(text, skin.get(TextButtonStyle.class));
		setSkin(skin);
	}

	public TextButton(@Null String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, TextButtonStyle.class));
		setSkin(skin);
	}

	public TextButton(@Null String text, TextButtonStyle style) {
		super();
		setStyle(style);
		this.style = style;
		label = new Label(text, new LabelStyle(style.font, style.fontColor));
		label.setAlignment(Align.center);
		add(label).expand().fill();
		setSize(getPrefWidth(), getPrefHeight());
	}

	@Override
	public TextButtonStyle getStyle() {
		return style;
	}

	@Override
	public void setStyle(ButtonStyle style) {
		if (style == null)
			throw new NullPointerException("style cannot be null");
		if (!(style instanceof TextButtonStyle))
			throw new IllegalArgumentException("style must be a TextButtonStyle.");
		super.setStyle(style);
		this.style = (TextButtonStyle) style;
		if (label != null) {
			TextButtonStyle textButtonStyle = (TextButtonStyle) style;
			LabelStyle labelStyle = label.getStyle();
			labelStyle.font = textButtonStyle.font;
			labelStyle.fontColor = textButtonStyle.fontColor;
			label.setStyle(labelStyle);
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		Color fontColor;
		if (isDisabled() && style.disabledFontColor != null)
			fontColor = style.disabledFontColor;
		else if (isPressed() && style.downFontColor != null)
			fontColor = style.downFontColor;
		else if (isChecked && style.checkedFontColor != null)
			fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor
					: style.checkedFontColor;
		else if (isOver() && style.overFontColor != null)
			fontColor = style.overFontColor;
		else
			fontColor = style.fontColor;
		if (fontColor != null)
			label.getStyle().fontColor = fontColor;
		super.draw(batch, parentAlpha);
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		getLabelCell().setActor(label);
		this.label = label;
	}

	public Cell<Label> getLabelCell() {
		return getCell(label);
	}

	public CharSequence getText() {
		return label.getText();
	}

	public void setText(@Null String text) {
		label.setText(text);
	}

	@Override
	public String toString() {
		String name = getName();
		if (name != null)
			return name;
		String className = getClass().getName();
		int dotIndex = className.lastIndexOf('.');
		if (dotIndex != -1)
			className = className.substring(dotIndex + 1);
		return (className.indexOf('$') != -1 ? "TextButton " : "") + className + ": " + label.getText();
	}

	/**
	 * The style for a text button, see {@link TextButton}.
	 *
	 * @author Nathan Sweet
	 */
	static public class TextButtonStyle extends ButtonStyle {
		public BitmapFont font;
		/**
		 * Optional.
		 */
		@Null
		public Color fontColor, downFontColor, overFontColor, checkedFontColor, checkedOverFontColor, disabledFontColor;

		public TextButtonStyle() {
		}

		public TextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, @Null BitmapFont font) {
			super(up, down, checked);
			this.font = font;
		}

		public TextButtonStyle(TextButtonStyle style) {
			super(style);
			this.font = style.font;
			if (style.fontColor != null)
				this.fontColor = new Color(style.fontColor);
			if (style.downFontColor != null)
				this.downFontColor = new Color(style.downFontColor);
			if (style.overFontColor != null)
				this.overFontColor = new Color(style.overFontColor);
			if (style.checkedFontColor != null)
				this.checkedFontColor = new Color(style.checkedFontColor);
			if (style.checkedOverFontColor != null)
				this.checkedOverFontColor = new Color(style.checkedOverFontColor);
			if (style.disabledFontColor != null)
				this.disabledFontColor = new Color(style.disabledFontColor);
		}
	}
}
