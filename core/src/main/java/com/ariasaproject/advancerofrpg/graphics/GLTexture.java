package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureFilter;
import com.ariasaproject.advancerofrpg.graphics.Texture.TextureWrap;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData;
import com.ariasaproject.advancerofrpg.graphics.glutils.TextureData.TextureDataType;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public abstract class GLTexture implements Disposable, Comparable<GLTexture> {
    public final int glTarget;
    protected int glHandle;
    protected TextureFilter minFilter = TextureFilter.Nearest;
    protected TextureFilter magFilter = TextureFilter.Nearest;
    protected TextureWrap uWrap = TextureWrap.ClampToEdge;
    protected TextureWrap vWrap = TextureWrap.ClampToEdge;

    protected float anisotropicFilterLevel = 1.0f;

    public GLTexture(int glTarget) {
        this(glTarget, GraphFunc.tgf.glGenTexture());
    }

    public GLTexture(int glTarget, int glHandle) {
        this.glTarget = glTarget;
        this.glHandle = glHandle;
    }

    protected static void uploadImageData(int target, TextureData data) {
        uploadImageData(target, data, 0);
    }

    public static void uploadImageData(int target, TextureData data, int miplevel) {
        if (data == null) {
            // FIXME: remove texture on target?
            return;
        }
        if (!data.isPrepared())
            data.prepare();
        final TextureDataType type = data.getType();
        if (type == TextureDataType.Custom) {
            data.consumeCustomData(target);
            return;
        }
        Pixmap pixmap = data.consumePixmap();
        boolean disposePixmap = data.disposePixmap();
        if (data.getFormat() != pixmap.format) {
            Pixmap tmp = new Pixmap(pixmap.width, pixmap.height, data.getFormat());
            tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.width, pixmap.height);
            if (data.disposePixmap()) {
                pixmap.dispose();
            }
            pixmap = tmp;
            disposePixmap = true;
        }
        TGF g = GraphFunc.tgf;
        g.glPixelStorei(TGF.GL_UNPACK_ALIGNMENT, 1);
        if (data.useMipMaps()) {
            g.glTexImage2D(target, 0, pixmap.format.InternalGLFormat, pixmap.width, pixmap.height, 0, pixmap.format.GLFormat, pixmap.format.GLType, pixmap.getPixels());
            g.glGenerateMipmap(target);
        } else {
            GraphFunc.tgf.glTexImage2D(target, miplevel, pixmap.format.InternalGLFormat, pixmap.width, pixmap.height, 0, pixmap.format.GLFormat, pixmap.format.GLType, pixmap.getPixels());
        }
        if (disposePixmap)
            pixmap.dispose();
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract int getDepth();

    public abstract boolean isManaged();

    public void bind() {
        GraphFunc.tgf.glBindTexture(glTarget, glHandle);
    }

    public void bind(int unit) {
        GraphFunc.tgf.glActiveTexture(unit);
        GraphFunc.tgf.glBindTexture(glTarget, glHandle);
    }

    public TextureFilter getMinFilter() {
        return minFilter;
    }

    public TextureFilter getMagFilter() {
        return magFilter;
    }

    public TextureWrap getUWrap() {
        return uWrap;
    }

    public TextureWrap getVWrap() {
        return vWrap;
    }

    public int getTextureObjectHandle() {
        return glHandle;
    }

    public void unsafeSetWrap(TextureWrap u, TextureWrap v) {
        unsafeSetWrap(u, v, false);
    }

    public void unsafeSetWrap(TextureWrap u, TextureWrap v, boolean force) {
        if (u != null && (force || uWrap != u)) {
            GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_WRAP_S, u.glEnum);
            uWrap = u;
        }
        if (v != null && (force || vWrap != v)) {
            GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_WRAP_T, v.glEnum);
            vWrap = v;
        }
    }

    public void setWrap(TextureWrap u, TextureWrap v) {
        this.uWrap = u;
        this.vWrap = v;
        bind();
        GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_WRAP_S, u.glEnum);
        GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_WRAP_T, v.glEnum);
    }

    public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter) {
        unsafeSetFilter(minFilter, magFilter, false);
    }

    public void unsafeSetFilter(TextureFilter minFilter, TextureFilter magFilter, boolean force) {
        if (minFilter != null && (force || this.minFilter != minFilter)) {
            GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_MIN_FILTER, minFilter.glEnum);
            this.minFilter = minFilter;
        }
        if (magFilter != null && (force || this.magFilter != magFilter)) {
            GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_MAG_FILTER, magFilter.glEnum);
            this.magFilter = magFilter;
        }
    }

    public void setFilter(TextureFilter minFilter, TextureFilter magFilter) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        bind();
        GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_MIN_FILTER, minFilter.glEnum);
        GraphFunc.tgf.glTexParameteri(glTarget, TGF.GL_TEXTURE_MAG_FILTER, magFilter.glEnum);
    }

    public float unsafeSetAnisotropicFilter(float level) {
        return unsafeSetAnisotropicFilter(level, false);
    }

    public float unsafeSetAnisotropicFilter(float level, boolean force) {
        if (GraphFunc.tgf.getMaxAnisotropicFilterLevel() <= 1f)
            return 1f;
        level = Math.min(level, GraphFunc.tgf.getMaxAnisotropicFilterLevel());
        if (!force && MathUtils.isEqual(level, anisotropicFilterLevel, 0.1f))
            return anisotropicFilterLevel;
        GraphFunc.tgf.glTexParameterf(TGF.GL_TEXTURE_2D, 0x84FE, level);
        return anisotropicFilterLevel = level;
    }

    public float setAnisotropicFilter(float level) {
        TGF g = GraphFunc.tgf;
        if (g.getMaxAnisotropicFilterLevel() <= 1f)
            return 1f;
        level = Math.min(level, g.getMaxAnisotropicFilterLevel());
        if (MathUtils.isEqual(level, anisotropicFilterLevel, 0.1f))
            return level;
        bind();
        g.glTexParameterf(TGF.GL_TEXTURE_2D, 0x84FE, level);
        return anisotropicFilterLevel = level;
    }

    public float getAnisotropicFilter() {
        return anisotropicFilterLevel;
    }

    protected void delete() {
        if (glHandle != 0) {
            GraphFunc.tgf.glDeleteTexture(glHandle);
            glHandle = 0;
        }
    }

    @Override
    public void dispose() {
        delete();
    }

    @Override
    public int compareTo(GLTexture o) {
        if (o == null)
            return 1;
        if (o == this)
            return 0;
        if (glTarget != o.glTarget)
            return glTarget - o.glTarget;
        if (getTextureObjectHandle() != o.getTextureObjectHandle())
            return getTextureObjectHandle() - o.getTextureObjectHandle();
        return 0;
    }
}
