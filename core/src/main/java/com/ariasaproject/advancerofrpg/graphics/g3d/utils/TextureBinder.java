package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.GLTexture;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.utils.BufferUtils;

import java.nio.IntBuffer;

public class TextureBinder {
    public final static int ROUNDROBIN = 0;
    public final static int LRU = 1;
    public final static int MAX_GLES_UNITS = 32;
    private final int offset;
    private final int count;
    private final GLTexture[] textures;
    private final int[] unitsLRU;
    private final int method;
    private final TextureDescriptor tempDesc = new TextureDescriptor();

    private boolean reused;
    private int currentTexture = 0;

    public TextureBinder(final int method) {
        this(method, 0);
    }

    public TextureBinder(final int method, final int offset) {
        this(method, offset, -1);
    }

    public TextureBinder(final int method, final int offset, int count) {
        final int max = Math.min(getMaxTextureUnits(), MAX_GLES_UNITS);
        if (count < 0)
            count = max - offset;
        if (offset < 0 || count < 0 || (offset + count) > max)
            throw new RuntimeException("Illegal arguments");
        this.method = method;
        this.offset = offset;
        this.count = count;
        this.textures = new GLTexture[count];
        this.unitsLRU = (method == LRU) ? new int[count] : null;
    }

    private static int getMaxTextureUnits() {
        IntBuffer buffer = BufferUtils.newIntBuffer(1);
        GraphFunc.tgf.glGetIntegerv(TGF.GL_MAX_TEXTURE_IMAGE_UNITS, buffer);
        return buffer.get(0);
    }

    public void begin() {
        for (int i = 0; i < count; i++) {
            textures[i] = null;
            if (unitsLRU != null)
                unitsLRU[i] = i;
        }
    }

    public void end() {
        GraphFunc.tgf.glActiveTexture(0);
    }

    public final int bind(final TextureDescriptor textureDesc) {
        return bindTexture(textureDesc, false);
    }

    public final int bind(final GLTexture texture) {
        tempDesc.set(texture, null, null, null, null);
        return bindTexture(tempDesc, false);
    }

    private final int bindTexture(final TextureDescriptor textureDesc, final boolean rebind) {
        final int result;
        final GLTexture texture = textureDesc.texture;
        reused = false;
        switch (method) {
            case ROUNDROBIN:
                result = offset + bindTextureRoundRobin(texture);
                break;
            case LRU:
                result = offset + bindTextureLRU(texture);
                break;
            default:
                return -1;
        }
        if (reused) {
            if (rebind)
                texture.bind(result);
            else
                GraphFunc.tgf.glActiveTexture(result);
        }
        texture.unsafeSetWrap(textureDesc.uWrap, textureDesc.vWrap);
        texture.unsafeSetFilter(textureDesc.minFilter, textureDesc.magFilter);
        return result;
    }

    private final int bindTextureRoundRobin(final GLTexture texture) {
        for (int i = 0; i < count; i++) {
            final int idx = (currentTexture + i) % count;
            if (textures[idx] == texture) {
                reused = true;
                return idx;
            }
        }
        currentTexture = (currentTexture + 1) % count;
        textures[currentTexture] = texture;
        texture.bind(offset + currentTexture);
        return currentTexture;
    }

    private final int bindTextureLRU(final GLTexture texture) {
        int i;
        for (i = 0; i < count; i++) {
            final int idx = unitsLRU[i];
            if (textures[idx] == texture) {
                reused = true;
                break;
            }
            if (textures[idx] == null) {
                break;
            }
        }
        if (i >= count)
            i = count - 1;
        final int idx = unitsLRU[i];
        while (i > 0) {
            unitsLRU[i] = unitsLRU[i - 1];
            i--;
        }
        unitsLRU[0] = idx;
        if (!reused) {
            textures[idx] = texture;
            texture.bind(offset + idx);
        }
        return idx;
    }
}
