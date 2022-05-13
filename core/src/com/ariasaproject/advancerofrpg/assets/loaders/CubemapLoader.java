package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.CubemapData;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureFilter;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureWrap;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;
import com.ariasaproject.advancerofrpg.utils.Array;

public class CubemapLoader extends AsynchronousAssetLoader<Cubemap, CubemapLoader.CubemapParameter> {
	CubemapLoaderInfo info = new CubemapLoaderInfo();

	public CubemapLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetContainer manager, String fileName, FileHandle file, CubemapParameter parameter) {
		info.filename = fileName;
		if (parameter == null || parameter.cubemapData == null) {
			Format format = null;
			boolean genMipMaps = false;
			info.cubemap = null;
			if (parameter != null) {
				format = parameter.format;
				info.cubemap = parameter.cubemap;
			}
		} else {
			info.data = parameter.cubemapData;
			info.cubemap = parameter.cubemap;
		}
		if (!info.data.isPrepared())
			info.data.prepare();
	}

	@Override
	public Cubemap loadSync(AssetContainer manager, String fileName, FileHandle file, CubemapParameter parameter) {
		if (info == null)
			return null;
		Cubemap cubemap = info.cubemap;
		if (cubemap != null) {
			cubemap.load(info.data);
		} else {
			cubemap = new Cubemap(info.data);
		}
		if (parameter != null) {
			cubemap.setFilter(parameter.minFilter, parameter.magFilter);
			cubemap.setWrap(parameter.wrapU, parameter.wrapV);
		}
		return cubemap;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, CubemapParameter parameter) {
		return null;
	}

	static public class CubemapLoaderInfo {
		String filename;
		CubemapData data;
		Cubemap cubemap;
	}

	static public class CubemapParameter extends AssetLoaderParameters<Cubemap> {
		/**
		 * the format of the final Texture. Uses the source images format if null
		 **/
		public Format format = null;
		/**
		 * The texture to put the {@link TextureData} in, optional.
		 **/
		public Cubemap cubemap = null;
		/**
		 * CubemapData for textures created on the fly, optional. When set, all format
		 * and genMipMaps are ignored
		 */
		public CubemapData cubemapData = null;
		public TextureFilter minFilter = TextureFilter.Nearest;
		public TextureFilter magFilter = TextureFilter.Nearest;
		public TextureWrap wrapU = TextureWrap.ClampToEdge;
		public TextureWrap wrapV = TextureWrap.ClampToEdge;
	}
}
