package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Scaling;

/**
 * A checkbox is a button that contains an image indicating the checked or
 * unchecked state and a label.
 *
 * @author Nathan Sweet
 */
public class CheckBox extends TextButton {
	private final Image image;
	private final Cell imageCell;
	private CheckBoxStyle style;

	public CheckBox(@Null String text, Skin skin) {
		this(text, skin.get(CheckBoxStyle.class));
	}

	public CheckBox(@Null String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, CheckBoxStyle.class));
	}

	public CheckBox(@Null String text, CheckBoxStyle style) {
		super(text, style);
		clearChildren();
		Label label = getLabel();
		imageCell = add(image = new Image(style.checkboxOff, Scaling.none));
		add(label);
		label.setAlignment(Align.left);
		setSize(getPrefWidth(), getPrefHeight());
	}

	/**
	 * Returns the checkbox's style. Modifying the returned style may not have an
	 * effect until {@link #setStyle(ButtonStyle)} is called.
	 */
	@Override
	public CheckBoxStyle getStyle() {
		return style;
	}

	@Override
	public void setStyle(ButtonStyle style) {
		if (!(style instanceof CheckBoxStyle))
			throw new IllegalArgumentException("style must be a CheckBoxStyle.");
		super.setStyle(style);
		this.style = (CheckBoxStyle) style;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		Drawable checkbox = null;
		if (isDisabled()) {
			if (isChecked && style.checkboxOnDisabled != null)
				checkbox = style.checkboxOnDisabled;
			else
				checkbox = style.checkboxOffDisabled;
		}
		if (checkbox == null) {
			boolean over = isOver() && !isDisabled();
			if (isChecked && style.checkboxOn != null)
				checkbox = over && style.checkboxOnOver != null ? style.checkboxOnOver : style.checkboxOn;
			else if (over && style.checkboxOver != null)
				checkbox = style.checkboxOver;
			else
				checkbox = style.checkboxOff;
		}
		image.setDrawable(checkbox);
		super.draw(batch, parentAlpha);
	}

	public Image getImage() {
		return image;
	}

	public Cell getImageCell() {
		return imageCell;
	}

	/**
	 * The style for a select box, see {@link CheckBox}.
	 *
	 * @author Nathan Sweet
	 */
	static public class CheckBoxStyle extends TextButtonStyle {
		public Drawable checkboxOn, checkboxOff;
		/**
		 * Optional.
		 */
		@Null
		public Drawable checkboxOnOver, checkboxOver, checkboxOnDisabled, checkboxOffDisabled;

		public CheckBoxStyle() {
		}

		public CheckBoxStyle(Drawable checkboxOff, Drawable checkboxOn, BitmapFont font, @Null Color fontColor) {
			this.checkboxOff = checkboxOff;
			this.checkboxOn = checkboxOn;
			this.font = font;
			this.fontColor = fontColor;
		}

		public CheckBoxStyle(CheckBoxStyle style) {
			super(style);
			this.checkboxOff = style.checkboxOff;
			this.checkboxOn = style.checkboxOn;
			this.checkboxOver = style.checkboxOver;
			this.checkboxOffDisabled = style.checkboxOffDisabled;
			this.checkboxOnDisabled = style.checkboxOnDisabled;
		}
	}
}
