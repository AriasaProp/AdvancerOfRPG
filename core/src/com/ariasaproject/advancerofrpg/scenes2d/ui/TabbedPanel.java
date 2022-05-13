package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;

public class TabbedPanel extends Widget {
	TabbedPanelStyle style;

	public TabbedPanel(Skin skin) {
		this(skin.get(TabbedPanelStyle.class));
	}

	public TabbedPanel(Skin skin, String styleName) {
		this(skin.get(styleName, TabbedPanelStyle.class));
	}

	public TabbedPanel(TabbedPanelStyle style) {
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

	}

	public void setStyle(TabbedPanelStyle style) {
		this.style = style;
	}

	static class TabbedPanelStyle {
		public BitmapFont tabFont;
		public Drawable selectedTab, unSelectedTab, tabBackground;
		public Drawable windowBackground;

	}
}
