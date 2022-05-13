package com.ariasaproject.advancerofrpg.graphics.glutils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;

public class GLOnlyTextureData implements TextureData {
	int width = 0;
	int height = 0;
	boolean isPrepared = false;
	int mipLevel = 0;
	int internalFormat;
	int format;
	int type;

	public GLOnlyTextureData(int width, int height, int mipMapLevel, int internalFormat, int format, int type) {
		this.width = width;
		this.height = height;
		this.mipLevel = mipMapLevel;
		this.internalFormat = internalFormat;
		this.format = format;
		this.type = type;
	}

	@Override
	public TextureDataType getType() {
		return TextureDataType.Custom;
	}

	@Override
	public boolean isPrepared() {
		return isPrepared;
	}

	@Override
	public void prepare() {
		if (isPrepared)
			throw new RuntimeException("Already prepared");
		isPrepared = true;
	}

	@Override
	public void consumeCustomData(int target) {
		GraphFunc.tgf.glTexImage2D(target, mipLevel, internalFormat, width, height, 0, format, type, null);
	}

	@Override
	public Pixmap consumePixmap() {
		throw new RuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public boolean disposePixmap() {
		throw new RuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Format getFormat() {
		return Format.RGBA8888;
	}

	@Override
	public boolean useMipMaps() {
		return false;
	}

	@Override
	public boolean isManaged() {
		return false;
	}
}
