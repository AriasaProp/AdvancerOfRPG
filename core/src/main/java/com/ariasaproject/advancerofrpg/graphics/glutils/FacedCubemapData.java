package com.ariasaproject.advancerofrpg.graphics.glutils;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.Cubemap.CubemapSide;
import com.ariasaproject.advancerofrpg.graphics.CubemapData;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.TGF;

/**
 * A FacedCubemapData holds a cubemap data definition based on a
 * {@link TextureData} per face.
 *
 * @author Vincent Nousquet
 */
public class FacedCubemapData implements CubemapData {

    protected final TextureData[] data = new TextureData[6];

    /**
     * Construct a Cubemap with the specified texture files for the sides,
     * optionally generating mipmaps.
     */
    public FacedCubemapData(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(new FileTextureData(positiveX, false), new FileTextureData(negativeX, false), new FileTextureData(positiveY, false), new FileTextureData(negativeY, false), new FileTextureData(positiveZ, false), new FileTextureData(negativeZ, false));
    }

    /**
     * Construct a Cubemap with the specified texture files for the sides,
     * optionally generating mipmaps.
     */
    public FacedCubemapData(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
        this(new FileTextureData(positiveX, useMipMaps), new FileTextureData(negativeX, useMipMaps), new FileTextureData(positiveY, useMipMaps), new FileTextureData(negativeY, useMipMaps), new FileTextureData(positiveZ, useMipMaps), new FileTextureData(negativeZ, useMipMaps));
    }

    /**
     * Construct a Cubemap with the specified {@link Pixmap}s for the sides,
     * optionally generating mipmaps.
     */
    public FacedCubemapData(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps) {
        this(positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false), negativeX == null ? null : new PixmapTextureData(negativeX, null, useMipMaps, false), positiveY == null ? null : new PixmapTextureData(positiveY, null, useMipMaps, false), negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false), positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false), negativeZ == null ? null : new PixmapTextureData(negativeZ, null, useMipMaps, false));
    }

    /**
     * Construct a Cubemap with {@link Pixmap}s for each side of the specified size.
     */
    public FacedCubemapData(int width, int height, int depth, Format format) {
        this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
    }

    /**
     * Construct a Cubemap with the specified {@link TextureData}'s for the sides
     */
    public FacedCubemapData(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
        data[0] = positiveX;
        data[1] = negativeX;
        data[2] = positiveY;
        data[3] = negativeY;
        data[4] = positiveZ;
        data[5] = negativeZ;
    }

    @Override
    public boolean isManaged() {
        for (TextureData data : this.data)
            if (!data.isManaged())
                return false;
        return true;
    }

    /**
     * Loads the texture specified using the {@link FileHandle} and sets it to
     * specified side, overwriting any previous data set to that side. Note that you
     * need to reload through {@link Cubemap#load(CubemapData)} any cubemap using
     * this data for the change to be taken in account.
     *
     * @param side The {@link CubemapSide}
     * @param file The texture {@link FileHandle}
     */
    public void load(CubemapSide side, FileHandle file) {
        data[side.index] = new FileTextureData(file, false);
    }

    /**
     * Sets the specified side of this cubemap to the specified {@link Pixmap},
     * overwriting any previous data set to that side. Note that you need to reload
     * through {@link Cubemap#load(CubemapData)} any cubemap using this data for the
     * change to be taken in account.
     *
     * @param side   The {@link CubemapSide}
     * @param pixmap The {@link Pixmap}
     */
    public void load(CubemapSide side, Pixmap pixmap) {
        data[side.index] = pixmap == null ? null : new PixmapTextureData(pixmap, null, false, false);
    }

    /**
     * @return True if all sides of this cubemap are set, false otherwise.
     */
    public boolean isComplete() {
        for (int i = 0; i < data.length; i++)
            if (data[i] == null)
                return false;
        return true;
    }

    /**
     * @return The {@link TextureData} for the specified side, can be null if the
     * cubemap is incomplete.
     */
    public TextureData getTextureData(CubemapSide side) {
        return data[side.index];
    }

    @Override
    public int getWidth() {
        int tmp, width = 0;
        if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getWidth()) > width)
            width = tmp;
        if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getWidth()) > width)
            width = tmp;
        if (data[CubemapSide.PositiveY.index] != null && (tmp = data[CubemapSide.PositiveY.index].getWidth()) > width)
            width = tmp;
        if (data[CubemapSide.NegativeY.index] != null && (tmp = data[CubemapSide.NegativeY.index].getWidth()) > width)
            width = tmp;
        return width;
    }

    @Override
    public int getHeight() {
        int tmp, height = 0;
        if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getHeight()) > height)
            height = tmp;
        if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getHeight()) > height)
            height = tmp;
        if (data[CubemapSide.PositiveX.index] != null && (tmp = data[CubemapSide.PositiveX.index].getHeight()) > height)
            height = tmp;
        if (data[CubemapSide.NegativeX.index] != null && (tmp = data[CubemapSide.NegativeX.index].getHeight()) > height)
            height = tmp;
        return height;
    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @Override
    public void prepare() {
        if (!isComplete())
            throw new RuntimeException("You need to complete your cubemap data before using it");
        for (int i = 0; i < data.length; i++)
            if (!data[i].isPrepared())
                data[i].prepare();
    }

    @Override
    public void consumeCubemapData() {
        for (int i = 0; i < data.length; i++) {
            if (data[i].getType() == TextureData.TextureDataType.Custom) {
                data[i].consumeCustomData(TGF.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
            } else {
                Pixmap pixmap = data[i].consumePixmap();
                boolean disposePixmap = data[i].disposePixmap();
                if (data[i].getFormat() != pixmap.format) {
                    Pixmap tmp = new Pixmap(pixmap.width, pixmap.height, data[i].getFormat());
                    tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.width, pixmap.height);
                    if (data[i].disposePixmap())
                        pixmap.dispose();
                    pixmap = tmp;
                    disposePixmap = true;
                }
                GraphFunc.tgf.glPixelStorei(TGF.GL_UNPACK_ALIGNMENT, 1);
                GraphFunc.tgf.glTexImage2D(TGF.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, pixmap.format.InternalGLFormat, pixmap.width, pixmap.height, 0, pixmap.format.GLFormat, pixmap.format.GLType, pixmap.getPixels());
                if (disposePixmap)
                    pixmap.dispose();
            }
        }
    }

}
