package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Label.LabelStyle;
import com.ariasaproject.advancerofrpg.scenes2d.ui.TextButton.TextButtonStyle;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Scaling;

/**
 * A button with a child {@link Image} and {@link Label}.
 *
 * @author Nathan Sweet
 * @see ImageButton
 * @see TextButton
 * @see Button
 */
public class ImageTextButton extends Button {
	private final Image image;
	private Label label;
	private ImageTextButtonStyle style;

	public ImageTextButton(@Null String text, Skin skin) {
		this(text, skin.get(ImageTextButtonStyle.class));
		setSkin(skin);
	}

	public ImageTextButton(@Null String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, ImageTextButtonStyle.class));
		setSkin(skin);
	}

	public ImageTextButton(@Null String text, ImageTextButtonStyle style) {
		super(style);
		this.style = style;
		defaults().space(3);
		image = new Image();
		image.setScaling(Scaling.fit);
		label = new Label(text, new LabelStyle(style.font, style.fontColor));
		label.setAlignment(Align.center);
		add(image);
		add(label);
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
	}

	@Override
	public ImageTextButtonStyle getStyle() {
		return style;
	}

	@Override
	public void setStyle(ButtonStyle style) {
		if (!(style instanceof ImageTextButtonStyle))
			throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
		super.setStyle(style);
		this.style = (ImageTextButtonStyle) style;
		if (image != null)
			updateImage();
		if (label != null) {
			ImageTextButtonStyle textButtonStyle = (ImageTextButtonStyle) style;
			LabelStyle labelStyle = label.getStyle();
			labelStyle.font = textButtonStyle.font;
			labelStyle.fontColor = textButtonStyle.fontColor;
			label.setStyle(labelStyle);
		}
	}

	/**
	 * Updates the Image with the appropriate Drawable from the style before it is
	 * drawn.
	 */
	protected void updateImage() {
		Drawable drawable = null;
		if (isDisabled() && style.imageDisabled != null)
			drawable = style.imageDisabled;
		else if (isPressed() && style.imageDown != null)
			drawable = style.imageDown;
		else if (isChecked && style.imageChecked != null)
			drawable = (style.imageCheckedOver != null && isOver()) ? style.imageCheckedOver : style.imageChecked;
		else if (isOver() && style.imageOver != null)
			drawable = style.imageOver;
		else if (style.imageUp != null) //
			drawable = style.imageUp;
		image.setDrawable(drawable);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		updateImage();
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

	public Image getImage() {
		return image;
	}

	public Cell getImageCell() {
		return getCell(image);
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		getLabelCell().setActor(label);
		this.label = label;
	}

	public Cell getLabelCell() {
		return getCell(label);
	}

	public CharSequence getText() {
		return label.getText();
	}

	public void setText(CharSequence text) {
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
		return (className.indexOf('$') != -1 ? "ImageTextButton " : "") + className + ": " + image.getDrawable() + " "
				+ label.getText();
	}

	/**
	 * The style for an image text button, see {@link ImageTextButton}.
	 *
	 * @author Nathan Sweet
	 */
	static public class ImageTextButtonStyle extends TextButtonStyle {
		/**
		 * Optional.
		 */
		@Null
		public Drawable imageUp, imageDown, imageOver, imageChecked, imageCheckedOver, imageDisabled;

		public ImageTextButtonStyle() {
		}

		public ImageTextButtonStyle(@Null Drawable up, @Null Drawable down, @Null Drawable checked, BitmapFont font) {
			super(up, down, checked, font);
		}

		public ImageTextButtonStyle(ImageTextButtonStyle style) {
			super(style);
			if (style.imageUp != null)
				this.imageUp = style.imageUp;
			if (style.imageDown != null)
				this.imageDown = style.imageDown;
			if (style.imageOver != null)
				this.imageOver = style.imageOver;
			if (style.imageChecked != null)
				this.imageChecked = style.imageChecked;
			if (style.imageCheckedOver != null)
				this.imageCheckedOver = style.imageCheckedOver;
			if (style.imageDisabled != null)
				this.imageDisabled = style.imageDisabled;
		}

		public ImageTextButtonStyle(TextButtonStyle style) {
			super(style);
		}
	}
}
