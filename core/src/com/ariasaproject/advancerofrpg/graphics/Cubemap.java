package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters.LoadedCallback;
import com.ariasaproject.advancerofrpg.assets.loaders.CubemapLoader.CubemapParameter;
import com.ariasaproject.advancerofrpg.files.FileHandle;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureFilter;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureWrap;
import com.ariasaproject.advancerofrpg.graphics.glutils.FacedCubemapData;
import com.ariasaproject.advancerofrpg.graphics.glutils.FileTextureData;
import com.ariasaproject.advancerofrpg.graphics.glutils.PixmapTextureData;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.ApplicationListener;

public class Cubemap extends GLTexture {
	final static Array<Cubemap> managedCubemaps = new Array<Cubemap>();
	protected CubemapData data;

	/**
	 * Construct a Cubemap based on the given CubemapData.
	 */
	public Cubemap(CubemapData data) {
		super(TGF.GL_TEXTURE_CUBE_MAP);
		this.data = data;
		load(data);
	}

	/**
	 * Construct a Cubemap with the specified texture files for the sides, does not
	 * generate mipmaps.
	 */
	public Cubemap(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY,
			FileHandle positiveZ, FileHandle negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	/**
	 * Construct a Cubemap with the specified texture files for the sides,
	 * optionally generating mipmaps.
	 */
	public Cubemap(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY,
			FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
		this(new FileTextureData(positiveX, useMipMaps), new FileTextureData(negativeX, useMipMaps),
				new FileTextureData(positiveY, useMipMaps), new FileTextureData(negativeY, useMipMaps),
				new FileTextureData(positiveZ, useMipMaps), new FileTextureData(negativeZ, useMipMaps));
	}

	/**
	 * Construct a Cubemap with the specified {@link Pixmap}s for the sides, does
	 * not generate mipmaps.
	 */
	public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ,
			Pixmap negativeZ) {
		this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
	}

	/**
	 * Construct a Cubemap with the specified {@link Pixmap}s for the sides,
	 * optionally generating mipmaps.
	 */
	public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ,
			Pixmap negativeZ, boolean useMipMaps) {
		this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false),
				negativeX == null ? null : new PixmapTextureData(negativeX, null, useMipMaps, false),
				positiveY == null ? null : new PixmapTextureData(positiveY, null, useMipMaps, false),
				negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false),
				positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false),
				negativeZ == null ? null : new PixmapTextureData(negativeZ, null, useMipMaps, false));
	}

	/**
	 * Construct a Cubemap with {@link Pixmap}s for each side of the specified size.
	 */
	public Cubemap(int width, int height, int depth, Format format) {
		this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true),
				new PixmapTextureData(new Pixmap(depth, height, format), null, false, true),
				new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
				new PixmapTextureData(new Pixmap(width, depth, format), null, false, true),
				new PixmapTextureData(new Pixmap(width, height, format), null, false, true),
				new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
	}

	/**
	 * Construct a Cubemap with the specified {@link TextureData}'s for the sides
	 */
	public Cubemap(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY,
			TextureData positiveZ, TextureData negativeZ) {
		super(TGF.GL_TEXTURE_CUBE_MAP);
		minFilter = TextureFilter.Nearest;
		magFilter = TextureFilter.Nearest;
		uWrap = TextureWrap.ClampToEdge;
		vWrap = TextureWrap.ClampToEdge;
		data = new FacedCubemapData(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
		load(data);
	}

	/**
	 * Clears all managed cubemaps. This is an internal method. Do not use it!
	 */
	public static void clearAllCubemaps() {
		managedCubemaps.clear();
	}

	/**
	 * Invalidate all managed cubemaps. This is an internal method. Do not use it!
	 */
	public static void invalidateAllCubemaps() {
		final AssetContainer assetManager = ApplicationListener.asset;
		assetManager.finishLoading();
		for (Cubemap cubemap : managedCubemaps) {
			String fileName = assetManager.getAssetFileName(cubemap);
			if (fileName == null) {
				if (!cubemap.isManaged())
					throw new RuntimeException("Tried to reload an unmanaged Cubemap");
				cubemap.glHandle = GraphFunc.tgf.glGenTexture();
				cubemap.load(cubemap.data);
			} else {
				final int refCount = assetManager.getReferenceCount(fileName);
				assetManager.setReferenceCount(fileName, 0);
				cubemap.glHandle = 0;
				CubemapParameter params = new CubemapParameter();
				params.cubemapData = cubemap.getCubemapData();
				params.minFilter = cubemap.getMinFilter();
				params.magFilter = cubemap.getMagFilter();
				params.wrapU = cubemap.getUWrap();
				params.wrapV = cubemap.getVWrap();
				params.cubemap = cubemap; // special parameter which will ensure that the references stay the same.
				params.loadedCallback = new LoadedCallback() {
					@Override
					public void finishedLoading(AssetContainer assetManager, String fileName, Class type) {
						assetManager.setReferenceCount(fileName, refCount);
					}
				};
				// unload the c, create a new gl handle then reload it.
				assetManager.unload(fileName);
				cubemap.glHandle = GraphFunc.tgf.glGenTexture();
				assetManager.load(new AssetDescriptor<Cubemap>(fileName, Cubemap.class, params));
			}
		}
	}
	public static int getNumManagedCubemaps() {
		return managedCubemaps.size;
	}

	/**
	 * Sets the sides of this cubemap to the specified {@link CubemapData}.
	 */
	public void load(CubemapData data) {
		if (!data.isPrepared())
			data.prepare();
		bind();
		unsafeSetFilter(minFilter, magFilter, true);
		unsafeSetWrap(uWrap, vWrap, true);
		unsafeSetAnisotropicFilter(anisotropicFilterLevel, true);
		data.consumeCubemapData();
		GraphFunc.tgf.glBindTexture(glTarget, 0);
	}

	public CubemapData getCubemapData() {
		return data;
	}

	@Override
	public boolean isManaged() {
		return data.isManaged();
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
		return 0;
	}

	@Override
	public void dispose() {
		if (glHandle == 0)
			return;
		delete();
		if (data.isManaged())
			managedCubemaps.removeValue(this, true);
	}

	public enum CubemapSide {
		PositiveX(0, TGF.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, -1, 0, 1, 0, 0),
		NegativeX(1, TGF.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, -1, 0, -1, 0, 0),
		PositiveY(2, TGF.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, 0, 1, 0, 1, 0),
		NegativeY(3, TGF.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, 0, -1, 0, -1, 0),
		PositiveZ(4, TGF.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, -1, 0, 0, 0, 1),
		NegativeZ(5, TGF.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, -1, 0, 0, 0, -1);

		public final int index, glEnum;
		public final Vector3 up, direction;

		CubemapSide(int index, int glEnum, float upX, float upY, float upZ, float directionX, float directionY,
				float directionZ) {
			this.index = index;
			this.glEnum = glEnum;
			this.up = new Vector3(upX, upY, upZ);
			this.direction = new Vector3(directionX, directionY, directionZ);
		}

		public int getGLEnum() {
			return glEnum;
		}

		public Vector3 getUp(Vector3 out) {
			return out.set(up);
		}

		public Vector3 getDirection(Vector3 out) {
			return out.set(direction);
		}
	}

}
