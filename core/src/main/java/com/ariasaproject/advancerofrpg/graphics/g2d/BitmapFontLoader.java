package com.ariasaproject.advancerofrpg.graphics.g2d;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.assets.loaders.AsynchronousAssetLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureFilter;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureParameter;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont.BitmapFontData;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureAtlas.AtlasRegion;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Skin;
import com.ariasaproject.advancerofrpg.utils.Array;

public class BitmapFontLoader extends AsynchronousAssetLoader<BitmapFont, BitmapFontLoader.BitmapFontParameter> {
    BitmapFontData data;

    public BitmapFontLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, BitmapFontParameter parameter) {
        Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
        if (parameter != null && parameter.bitmapFontData != null) {
            data = parameter.bitmapFontData;
            return deps;
        }
        data = new BitmapFontData(file);
        if (parameter != null && parameter.atlasName != null) {
            deps.add(new AssetDescriptor<TextureAtlas>(parameter.atlasName, TextureAtlas.class));
        } else {
            for (int i = 0; i < data.getImagePaths().length; i++) {
                String path = data.getImagePath(i);
                FileHandle resolved = resolve(path);
                TextureParameter textureParams = new TextureParameter();
                if (parameter != null) {
                    textureParams.genMipMaps = parameter.genMipMaps;
                    textureParams.minFilter = parameter.minFilter;
                    textureParams.magFilter = parameter.magFilter;
                }
                AssetDescriptor descriptor = new AssetDescriptor<Texture>(resolved, Texture.class, textureParams);
                deps.add(descriptor);
            }
        }
        return deps;
    }

    @Override
    public void loadAsync(AssetContainer manager, String fileName, FileHandle file, BitmapFontParameter parameter) {
    }

    @Override
    public BitmapFont loadSync(AssetContainer manager, String fileName, FileHandle file, BitmapFontParameter parameter) {
        if (parameter != null && parameter.atlasName != null) {
            TextureAtlas atlas = manager.get(parameter.atlasName, TextureAtlas.class);
            String name = file.sibling(data.imagePaths[0]).nameWithoutExtension();
            AtlasRegion region = atlas.findRegion(name);
            if (region == null)
                throw new RuntimeException("Could not find font region " + name + " in atlas " + parameter.atlasName);
            return new BitmapFont(file, region);
        } else {
            int n = data.getImagePaths().length;
            Array<TextureRegion> regs = new Array(n);
            for (int i = 0; i < n; i++) {
                regs.add(new TextureRegion(manager.get(data.getImagePath(i), Texture.class)));
            }
            return new BitmapFont(data, regs, true);
        }
    }

    static public class BitmapFontParameter extends AssetLoaderParameters<BitmapFont> {

        /**
         * Generates mipmaps for the font if {@code true}. Defaults to {@code false}.
         **/
        public boolean genMipMaps = false;

        /**
         * The {@link TextureFilter} to use when scaling down the {@link BitmapFont}.
         * Defaults to {@link TextureFilter#Nearest}.
         */
        public TextureFilter minFilter = TextureFilter.Nearest;

        /**
         * The {@link TextureFilter} to use when scaling up the {@link BitmapFont}.
         * Defaults to {@link TextureFilter#Nearest}.
         */
        public TextureFilter magFilter = TextureFilter.Nearest;

        /**
         * optional {@link BitmapFontData} to be used instead of loading the
         * {@link Texture} directly. Use this if your font is embedded in a
         * {@link Skin}.
         **/
        public BitmapFontData bitmapFontData = null;

        /**
         * The name of the {@link TextureAtlas} to load the {@link BitmapFont} itself
         * from. Optional; if {@code null}, will look for a separate image
         */
        public String atlasName = null;
    }
}
