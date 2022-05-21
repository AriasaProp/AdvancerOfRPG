package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.ApplicationListener;
import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters.LoadedCallback;
import com.ariasaproject.advancerofrpg.assets.loaders.TextureLoader.TextureParameter;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.glutils.FileTextureData;
import com.ariasaproject.advancerofrpg.graphics.glutils.PixmapTextureData;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;
import com.ariasaproject.advancerofrpg.utils.Array;

public class Texture extends GLTexture {
    final static Array<Texture> managedTextures = new Array<Texture>();
    TextureData data;

    public Texture(String internalPath) {
        this(GraphFunc.app.getFiles().internal(internalPath));
    }

    public Texture(FileHandle file) {
        this(file, null, false);
    }

    public Texture(FileHandle file, boolean useMipMaps) {
        this(file, null, useMipMaps);
    }

    public Texture(FileHandle file, Format format, boolean useMipMaps) {
        this(new FileTextureData(file, format, useMipMaps));
    }

    public Texture(Pixmap pixmap) {
        this(new PixmapTextureData(pixmap, null, false, false));
    }

    public Texture(Pixmap pixmap, boolean useMipMaps) {
        this(new PixmapTextureData(pixmap, null, useMipMaps, false));
    }

    public Texture(Pixmap pixmap, Format format, boolean useMipMaps) {
        this(new PixmapTextureData(pixmap, format, useMipMaps, false));
    }

    public Texture(int width, int height, Format format) {
        this(new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
    }

    public Texture(TextureData data) {
        this(TGF.GL_TEXTURE_2D, GraphFunc.tgf.glGenTexture(), data);
    }

    protected Texture(int glTarget, int glHandle, TextureData data) {
        super(glTarget, glHandle);
        load(data);
        if (data.isManaged())
            managedTextures.add(this);
    }

    public static void clearAllTextures() {
        final AssetContainer assetManager = ApplicationListener.asset;
        assetManager.finishLoading();
        for (Texture texture : managedTextures) {
            String fileName = assetManager.getAssetFileName(texture);
            if (fileName == null) {
                texture.dispose();
            } else {
                assetManager.unload(fileName);
            }
        }
        managedTextures.clear();
    }

    public static void invalidateAllTextures() {
        final AssetContainer assetManager = ApplicationListener.asset;
        assetManager.finishLoading();
        for (Texture texture : managedTextures) {
            String fileName = assetManager.getAssetFileName(texture);
            if (fileName == null) {
                if (!texture.isManaged())
                    throw new RuntimeException("Tried to reload unmanaged Texture");
                texture.glHandle = GraphFunc.tgf.glGenTexture();
                texture.load(texture.data);
            } else {
                final int refCount = assetManager.getReferenceCount(fileName);
                assetManager.setReferenceCount(fileName, 0);
                texture.glHandle = 0;
                TextureParameter params = new TextureParameter();
                params.textureData = texture.getTextureData();
                params.minFilter = texture.getMinFilter();
                params.magFilter = texture.getMagFilter();
                params.wrapU = texture.getUWrap();
                params.wrapV = texture.getVWrap();
                params.genMipMaps = texture.data.useMipMaps(); // not sure about this?
                params.texture = texture; // special parameter which will ensure that the references stay the same.
                params.loadedCallback = new LoadedCallback() {
                    @Override
                    public void finishedLoading(AssetContainer assetManager, String fileName, Class type) {
                        assetManager.setReferenceCount(fileName, refCount);
                    }
                };
                assetManager.unload(fileName);
                texture.glHandle = GraphFunc.tgf.glGenTexture();
                assetManager.load(new AssetDescriptor<Texture>(fileName, Texture.class, params));
            }
        }
    }

    public static int getNumManagedTextures() {
        return managedTextures.size;
    }

    public void load(TextureData data) {
        if (this.data != null && data.isManaged() != this.data.isManaged())
            throw new RuntimeException("New data must have the same managed status as the old data");
        this.data = data;
        if (!data.isPrepared())
            data.prepare();
        bind();
        uploadImageData(TGF.GL_TEXTURE_2D, data);
        unsafeSetFilter(minFilter, magFilter, true);
        unsafeSetWrap(uWrap, vWrap, true);
        unsafeSetAnisotropicFilter(anisotropicFilterLevel, true);
        GraphFunc.tgf.glBindTexture(glTarget, 0);
    }

    public void draw(Pixmap pixmap, int x, int y) {
        if (data.isManaged())
            throw new RuntimeException("can't draw to a managed texture");
        bind();
        GraphFunc.tgf.glTexSubImage2D(glTarget, 0, x, y, pixmap.width, pixmap.height, pixmap.format.GLFormat, pixmap.format.GLType, pixmap.getPixels());
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

    public TextureData getTextureData() {
        return data;
    }

    @Override
    public boolean isManaged() {
        return data.isManaged();
    }

    @Override
    public void dispose() {
        if (glHandle == 0)
            return;
        delete();
        if (data.isManaged())
            managedTextures.removeValue(this, true);
    }

    @Override
    public String toString() {
        if (data instanceof FileTextureData)
            return data.toString();
        return super.toString();
    }

    public enum TextureFilter {
        Nearest(TGF.GL_NEAREST), Linear(TGF.GL_LINEAR), MipMap(TGF.GL_LINEAR_MIPMAP_LINEAR), MipMapNearestNearest(TGF.GL_NEAREST_MIPMAP_NEAREST), MipMapLinearNearest(TGF.GL_LINEAR_MIPMAP_NEAREST), MipMapNearestLinear(TGF.GL_NEAREST_MIPMAP_LINEAR), MipMapLinearLinear(TGF.GL_LINEAR_MIPMAP_LINEAR);
        public final int glEnum;

        TextureFilter(final int glEnum) {
            this.glEnum = glEnum;
        }

        public boolean isMipMap() {
            return glEnum != TGF.GL_NEAREST && glEnum != TGF.GL_LINEAR;
        }
    }

    public enum TextureWrap {
        MirroredRepeat(TGF.GL_MIRRORED_REPEAT), ClampToEdge(TGF.GL_CLAMP_TO_EDGE), Repeat(TGF.GL_REPEAT);
        public final int glEnum;

        TextureWrap(final int glEnum) {
            this.glEnum = glEnum;
        }
    }
}
