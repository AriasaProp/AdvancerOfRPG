package com.ariasaproject.advancerofrpg.graphics.glutils;

import java.nio.FloatBuffer;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;

public class FloatTextureData implements TextureData {
	int width = 0;
	int height = 0;

	int internalFormat;
	int format;
	int type;

	boolean isGpuOnly;

	boolean isPrepared = false;
	FloatBuffer buffer;

	public FloatTextureData(int w, int h, int internalFormat, int format, int type, boolean isGpuOnly) {
		this.width = w;
		this.height = h;
		this.internalFormat = internalFormat;
		this.format = format;
		this.type = type;
		this.isGpuOnly = isGpuOnly;
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
		if (!isGpuOnly) {
			int amountOfFloats = 4;
			if (internalFormat == TGF.GL_RGBA16F || internalFormat == TGF.GL_RGBA32F)
				amountOfFloats = 4;
			if (internalFormat == TGF.GL_RGB16F || internalFormat == TGF.GL_RGB32F)
				amountOfFloats = 3;
			if (internalFormat == TGF.GL_RG16F || internalFormat == TGF.GL_RG32F)
				amountOfFloats = 2;
			if (internalFormat == TGF.GL_R16F || internalFormat == TGF.GL_R32F)
				amountOfFloats = 1;
			this.buffer = BufferUtils.newFloatBuffer(width * height * amountOfFloats);
		}
		isPrepared = true;
	}

	@Override
	public void consumeCustomData(int target) {
		// GLES and WebGL defines texture format by 3rd and 8th argument,
		// so to get a float texture one needs to supply GL_RGBA and GL_FLOAT there.
		GraphFunc.tgf.glTexImage2D(target, 0, TGF.GL_RGBA, width, height, 0, TGF.GL_RGBA, TGF.GL_FLOAT, buffer);
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
		return Format.RGBA8888; // it's not true, but FloatTextureData.getFormat() isn't used anywhere
	}

	@Override
	public boolean useMipMaps() {
		return false;
	}

	@Override
	public boolean isManaged() {
		return true;
	}

	public FloatBuffer getBuffer() {
		return buffer;
	}
}
