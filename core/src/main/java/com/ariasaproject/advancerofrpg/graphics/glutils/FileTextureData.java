package com.ariasaproject.advancerofrpg.graphics.glutils;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;

public class FileTextureData implements TextureData {
    final FileHandle file;
    int width = 0;
    int height = 0;
    Format format;
    Pixmap pixmap;
    boolean useMipMaps;
    boolean isPrepared = false;

    public FileTextureData(FileHandle file, boolean useMipMaps) {
        this(file, null, Format.RGB888, useMipMaps);
    }

    public FileTextureData(FileHandle file, Format format, boolean useMipMaps) {
        this(file, null, format, useMipMaps);
    }

    public FileTextureData(FileHandle file, Pixmap preloadedPixmap, Format format, boolean useMipMaps) {
        this.file = file;
        this.pixmap = preloadedPixmap;
        this.format = format;
        this.useMipMaps = useMipMaps;
        if (pixmap != null) {
            width = pixmap.width;
            height = pixmap.height;
            if (format == null)
                this.format = pixmap.format;
        }
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void prepare() {
        if (isPrepared)
            return;
        if (pixmap == null) {
            pixmap = new Pixmap(file);
            width = pixmap.width;
            height = pixmap.height;
            if (format == null)
                format = pixmap.format;
        }
        isPrepared = true;
    }

    @Override
    public Pixmap consumePixmap() {
        if (!isPrepared)
            throw new RuntimeException("Call prepare() before calling getPixmap()");
        isPrepared = false;
        Pixmap pixmap = this.pixmap;
        this.pixmap = null;
        return pixmap;
    }

    @Override
    public boolean disposePixmap() {
        return true;
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
        return format;
    }

    @Override
    public boolean useMipMaps() {
        return useMipMaps;
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    public FileHandle getFileHandle() {
        return file;
    }

    @Override
    public TextureDataType getType() {
        return TextureDataType.Pixmap;
    }

    @Override
    public void consumeCustomData(int target) {
        throw new RuntimeException("This TextureData implementation does not upload data itself");
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
