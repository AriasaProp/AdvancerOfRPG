package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.assets.loaders.TextureLoader.TextureParameter;
import com.ariasaproject.advancerofrpg.files.FileHandle;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureAtlas;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.ariasaproject.advancerofrpg.utils.Array;

public class TextureAtlasLoader extends SynchronousAssetLoader<TextureAtlas, TextureAtlasLoader.TextureAtlasParameter> {
	TextureAtlasData data;

	public TextureAtlasLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public TextureAtlas load(AssetContainer assetManager, String fileName, FileHandle file,
			TextureAtlasParameter parameter) {
		for (Page page : data.getPages()) {
			Texture texture = assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), Texture.class);
			page.texture = texture;
		}
		TextureAtlas atlas = new TextureAtlas(data);
		data = null;
		return atlas;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle atlasFile,
			TextureAtlasParameter parameter) {
		FileHandle imgDir = atlasFile.parent();
		if (parameter != null)
			data = new TextureAtlasData(atlasFile, imgDir, parameter.flip);
		else {
			data = new TextureAtlasData(atlasFile, imgDir, false);
		}
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		for (Page page : data.getPages()) {
			TextureParameter params = new TextureParameter();
			params.format = page.format;
			params.genMipMaps = page.useMipMaps;
			params.minFilter = page.minFilter;
			params.magFilter = page.magFilter;
			dependencies.add(new AssetDescriptor<Texture>(page.textureFile, Texture.class, params));
		}
		return dependencies;
	}

	static public class TextureAtlasParameter extends AssetLoaderParameters<TextureAtlas> {
		/**
		 * whether to flip the texture atlas vertically
		 **/
		public boolean flip = false;

		public TextureAtlasParameter() {
		}

		public TextureAtlasParameter(boolean flip) {
			this.flip = flip;
		}
	}
}
