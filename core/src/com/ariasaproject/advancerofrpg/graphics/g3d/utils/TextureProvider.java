package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.graphics.g3d.model.data.ModelData;

/**
 * Used by {@link Model} to load textures from {@link ModelData}.
 *
 * @author badlogic
 */
public interface TextureProvider {
	Texture load(String fileName);

	class FileTextureProvider implements TextureProvider {
		private final Texture.TextureFilter minFilter;
		private final Texture.TextureFilter magFilter;
		private final Texture.TextureWrap uWrap;
		private final Texture.TextureWrap vWrap;
		private final boolean useMipMaps;

		public FileTextureProvider() {
			minFilter = magFilter = Texture.TextureFilter.Linear;
			uWrap = vWrap = Texture.TextureWrap.Repeat;
			useMipMaps = false;
		}

		public FileTextureProvider(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter, Texture.TextureWrap uWrap, Texture.TextureWrap vWrap, boolean useMipMaps) {
			this.minFilter = minFilter;
			this.magFilter = magFilter;
			this.uWrap = uWrap;
			this.vWrap = vWrap;
			this.useMipMaps = useMipMaps;
		}

		@Override
		public Texture load(String fileName) {
			Texture result = new Texture(GraphFunc.app.getFiles().internal(fileName), useMipMaps);
			result.setFilter(minFilter, magFilter);
			result.setWrap(uWrap, vWrap);
			return result;
		}
	}

	class AssetTextureProvider implements TextureProvider {
		public final AssetContainer assetManager;

		public AssetTextureProvider(final AssetContainer assetManager) {
			this.assetManager = assetManager;
		}

		@Override
		public Texture load(String fileName) {
			return assetManager.get(fileName, Texture.class);
		}
	}
}
