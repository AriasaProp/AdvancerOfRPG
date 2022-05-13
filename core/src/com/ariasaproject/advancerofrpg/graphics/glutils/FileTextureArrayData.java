package com.ariasaproject.advancerofrpg.graphics.glutils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.TextureArrayData;

public class FileTextureArrayData implements TextureArrayData {

	private final TextureData[] textureDatas;
	private final Format format;
	private final int depth;
	boolean useMipMaps;
	private boolean prepared;

	public FileTextureArrayData(Format format, boolean useMipMaps, FileHandle[] files) {
		this.format = format;
		this.useMipMaps = useMipMaps;
		this.depth = files.length;
		textureDatas = new TextureData[files.length];
		for (int i = 0; i < files.length; i++) {
			textureDatas[i] = new FileTextureData(files[i], format, useMipMaps);
		}
	}

	@Override
	public boolean isPrepared() {
		return prepared;
	}

	@Override
	public void prepare() {
		int width = -1;
		int height = -1;
		for (TextureData data : textureDatas) {
			data.prepare();
			if (width == -1) {
				width = data.getWidth();
				height = data.getHeight();
				continue;
			}
			if (width != data.getWidth() || height != data.getHeight()) {
				throw new RuntimeException("Error whilst preparing TextureArray: TextureArray Textures must have equal dimensions.");
			}
		}
		prepared = true;
	}

	@Override
	public void consumeTextureArrayData() {
		for (int i = 0; i < textureDatas.length; i++) {
			if (textureDatas[i].getType() == TextureData.TextureDataType.Custom) {
				textureDatas[i].consumeCustomData(TGF.GL_TEXTURE_2D_ARRAY);
			} else {
				TextureData texData = textureDatas[i];
				Pixmap pixmap = texData.consumePixmap();
				boolean disposePixmap = texData.disposePixmap();
				if (texData.getFormat() != pixmap.format) {
					Pixmap temp = new Pixmap(pixmap.width, pixmap.height, texData.getFormat());
					temp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.width, pixmap.height);
					if (texData.disposePixmap()) {
						pixmap.dispose();
					}
					pixmap = temp;
					disposePixmap = true;
				}
				GraphFunc.tgf.glTexSubImage3D(TGF.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, pixmap.width, pixmap.height, 1, pixmap.format.GLFormat, pixmap.format.GLType, pixmap.getPixels());
				if (disposePixmap)
					pixmap.dispose();
			}
		}
	}

	@Override
	public int getWidth() {
		return textureDatas[0].getWidth();
	}

	@Override
	public int getHeight() {
		return textureDatas[0].getHeight();
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public Format getFormat() {
		return format;
	}

	@Override
	public boolean isManaged() {
		for (TextureData data : textureDatas) {
			if (!data.isManaged()) {
				return false;
			}
		}
		return true;
	}
}
