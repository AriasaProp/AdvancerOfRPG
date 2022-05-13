package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.files.Files.FileHandle;
import com.ariasaproject.advancerofrpg.utils.Array;

public class TextureArray extends GLTexture {
	final static Array<TextureArray> managedTextureArrays = new Array<TextureArray>();
	private TextureArrayData data;

	public TextureArray(String... internalPaths) {
		this(getInternalHandles(internalPaths));
	}

	public TextureArray(FileHandle... files) {
		this(false, files);
	}

	public TextureArray(boolean useMipMaps, FileHandle... files) {
		this(useMipMaps, Pixmap.Format.RGBA8888, files);
	}

	public TextureArray(boolean useMipMaps, Pixmap.Format format, FileHandle... files) {
		this(TextureArrayData.Factory.loadFromFiles(format, useMipMaps, files));
	}

	public TextureArray(TextureArrayData data) {
		super(TGF.GL_TEXTURE_2D_ARRAY, GraphFunc.tgf.glGenTexture());
		load(data);
		if (data.isManaged())
			managedTextureArrays.add(this);
	}

	private static FileHandle[] getInternalHandles(String... internalPaths) {
		FileHandle[] handles = new FileHandle[internalPaths.length];
		for (int i = 0; i < internalPaths.length; i++) {
			handles[i] = GraphFunc.app.getFiles().internal(internalPaths[i]);
		}
		return handles;
	}

	public static void clearAllTextureArrays() {
		managedTextureArrays.clear();
	}

	public static void invalidateAllTextureArrays() {
		for (TextureArray textureArray : managedTextureArrays) {
			if (!textureArray.isManaged())
				throw new RuntimeException("Tried to reload an unmanaged TextureArray");
			textureArray.glHandle = GraphFunc.tgf.glGenTexture();
			textureArray.load(textureArray.data);
		}
	}

	public static int getNumManagedTextureArrays() {
		return managedTextureArrays.size;
	}

	private void load(TextureArrayData data) {
		if (this.data != null && data.isManaged() != this.data.isManaged())
			throw new RuntimeException("New data must have the same managed status as the old data");
		this.data = data;
		bind();
		final TGF g = GraphFunc.tgf;
		g.glTexImage3D(TGF.GL_TEXTURE_2D_ARRAY, 0, data.getFormat().InternalGLFormat, data.getWidth(),
				data.getHeight(), data.getDepth(), 0, data.getFormat().GLFormat, data.getFormat().GLType, null);
		if (!data.isPrepared())
			data.prepare();
		data.consumeTextureArrayData();
		setFilter(minFilter, magFilter);
		setWrap(uWrap, vWrap);
		g.glBindTexture(glTarget, 0);
	}

	@Override
	public int getWidth() {
		return data.getWidth();
	}

	@Override
	public int getHeight() {
		return data.getHeight();
	}

	@Override
	public int getDepth() {
		return data.getDepth();
	}

	@Override
	public boolean isManaged() {
		return data.isManaged();
	}

}
