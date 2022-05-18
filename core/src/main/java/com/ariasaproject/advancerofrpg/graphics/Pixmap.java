package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.utils.Disposable;

import java.nio.ByteBuffer;

public class Pixmap implements Disposable {
    static final long[] nativeData = new long[4];

    static {
        initialize();
    }

    public final int width, height;
    private final long basePtr;
    private final ByteBuffer pixels;
    public Format format;
    private Filter filter = Filter.Linear;
    private boolean disposed;

    public Pixmap(int width, int height, Format format) {
        this.pixels = newPixmap(nativeData, width, height, format.ordinal());
        this.basePtr = nativeData[0];
        this.width = (int) nativeData[1];
        this.height = (int) nativeData[2];
        this.format = Format.values()[(int) nativeData[3] - 1];
        clear(0xffffffff);
    }

    public Pixmap(byte[] encodedData, int offset, int len) {
        this.pixels = load(nativeData, encodedData, offset, len);
        if (this.pixels == null)
            throw new RuntimeException("Couldn't load pixmap from image data " + getFailureReason());
        this.basePtr = nativeData[0];
        this.width = (int) nativeData[1];
        this.height = (int) nativeData[2];
        this.format = Format.values()[(int) nativeData[3] - 1];
    }

    public Pixmap(FileHandle file) {
        switch (file.type()) {
            case Internal:
            case Local:
                this.pixels = loadFromInternalFilePath(nativeData, file.path());

                break;
            default:
                final byte[] bytes = file.readBytes();
                this.pixels = load(nativeData, bytes, 0, bytes.length);
                break;
        }

        if (pixels == null)
            throw new RuntimeException("Couldn't load file: " + file + " " + getFailureReason());
        basePtr = nativeData[0];
        this.width = (int) nativeData[1];
        this.height = (int) nativeData[2];
        this.format = Format.values()[(int) nativeData[3] - 1];
    }

    static native void initialize();

    private static native ByteBuffer loadFromInternalFilePath(long[] nativeData, String internalPath);

    private static native ByteBuffer load(long[] nativeData, byte[] buffer, int offset, int len);

    private static native ByteBuffer newPixmap(long[] nativeData, int width, int height, int format);

    public static native String getFailureReason();

    private native void drawPixmap(long src, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight);

    private native void free();

    public native void clear(int color);

    public native void setPixel(int x, int y, int color);

    public native int getPixel(int x, int y);

    private native void setScaleType(boolean scale);

    public void drawPixmap(Pixmap pixmap, int srcX, int srcY, int dstX, int dstY, int width, int height) {
        drawPixmap(pixmap.basePtr, srcX, srcY, width, height, dstX, dstY, width, height);
    }

    public void drawPixmap(Pixmap pixmap, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY, int dstWidth, int dstHeight) {
        drawPixmap(pixmap.basePtr, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
    }

    @Override
    public void dispose() {
        if (disposed)
            throw new RuntimeException("Pixmap already disposed!");
        free();
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public ByteBuffer getPixels() {
        if (disposed)
            throw new RuntimeException("Pixmap already disposed");
        return pixels;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        setScaleType(filter.nativeFormat);
    }

    public enum Format {
        Alpha(TGF.GL_ALPHA, TGF.GL_ALPHA, TGF.GL_UNSIGNED_BYTE), LuminanceAlpha(TGF.GL_LUMINANCE_ALPHA, TGF.GL_LUMINANCE_ALPHA, TGF.GL_UNSIGNED_BYTE), RGB888(TGF.GL_RGB8, TGF.GL_RGB, TGF.GL_UNSIGNED_BYTE), RGBA8888(TGF.GL_RGBA8, TGF.GL_RGBA, TGF.GL_UNSIGNED_BYTE), RGB565(TGF.GL_RGB565, TGF.GL_RGB, TGF.GL_UNSIGNED_SHORT_5_6_5), RGBA4444(TGF.GL_RGBA4, TGF.GL_RGBA, TGF.GL_UNSIGNED_SHORT_4_4_4_4);

        public final int InternalGLFormat, GLFormat, GLType;
        /*
         * GL Format should be one of them Red?(?, TGF.GL_RED, ?), RG?(?, TGF.GL_RG, ?),
         * RGB?(?, TGF.GL_RGB, ?), BGR?(?, TGF.GL_BGR, ?), RGBA?(?, TGF.GL_RGBA, ?),
         * BGRA?(?, TGF.GL_BGRA, ?), Stencil?(?, TGF.GL_STENCIL_INDEX, ?), Depth?(?,
         * TGF.GL_DEPTH_COMPONENT, ?), DepthStencil?(?, TGF.GL_DEPTH_STENCIL, ?),
         *
         * GL_RED_INTEGER, GL_RG_INTEGER, GL_RGB_INTEGER, GL_BGR_INTEGER,
         * GL_RGBA_INTEGER, GL_BGRA_INTEGER,
         */

        Format(final int a, final int b, final int c) {
            InternalGLFormat = a;
            GLFormat = b;
            GLType = c;
        }
    }

    public enum Filter {
        Nearest(false), Linear(true);
        final boolean nativeFormat;

        Filter(final boolean n) {
            this.nativeFormat = n;
        }
    }

}
