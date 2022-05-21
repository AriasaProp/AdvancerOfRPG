package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;

public final class VertexAttribute {
    public final int usage;
    public final int numComponents;
    public final boolean normalized;
    public final int type;
    private final int usageIndex;
    public int offset;
    public String alias;
    public int unit;

    public VertexAttribute(int usage, int numComponents, String alias) {
        this(usage, numComponents, alias, 0);
    }

    public VertexAttribute(int usage, int numComponents, String alias, int unit) {
        this(usage, numComponents, usage == Usage.ColorPacked ? TGF.GL_UNSIGNED_BYTE : TGF.GL_FLOAT, usage == Usage.ColorPacked, alias, unit);
    }

    public VertexAttribute(int usage, int numComponents, int type, boolean normalized, String alias) {
        this(usage, numComponents, type, normalized, alias, 0);
    }

    public VertexAttribute(int usage, int numComponents, int type, boolean normalized, String alias, int unit) {
        this.usage = usage;
        this.numComponents = numComponents;
        this.type = type;
        this.normalized = normalized;
        this.alias = alias;
        this.unit = unit;
        this.usageIndex = Integer.numberOfTrailingZeros(usage);
    }

    public static VertexAttribute Position() {
        return new VertexAttribute(Usage.Position, 3, "a_position");
    }

    public static VertexAttribute TexCoords(int unit) {
        return new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord" + unit, unit);
    }

    public static VertexAttribute Normal() {
        return new VertexAttribute(Usage.Normal, 3, "a_normal");
    }

    public static VertexAttribute ColorPacked() {
        return new VertexAttribute(Usage.ColorPacked, 4, TGF.GL_UNSIGNED_BYTE, true, "a_color");
    }

    public static VertexAttribute ColorUnpacked() {
        return new VertexAttribute(Usage.ColorUnpacked, 4, TGF.GL_FLOAT, false, "a_color");
    }

    public static VertexAttribute Tangent() {
        return new VertexAttribute(Usage.Tangent, 3, "a_tangent");
    }

    public static VertexAttribute Binormal() {
        return new VertexAttribute(Usage.BiNormal, 3, "a_binormal");
    }

    public static VertexAttribute BoneWeight(int unit) {
        return new VertexAttribute(Usage.BoneWeight, 2, "a_boneWeight" + unit, unit);
    }

    public VertexAttribute copy() {
        return new VertexAttribute(usage, numComponents, type, normalized, alias, unit);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof VertexAttribute))
            return false;
        return equals((VertexAttribute) obj);
    }

    public boolean equals(final VertexAttribute other) {
        return other != null && usage == other.usage && numComponents == other.numComponents && type == other.type && normalized == other.normalized && alias.equals(other.alias) && unit == other.unit;
    }

    public int getKey() {
        return (usageIndex << 8) + (unit & 0xFF);
    }

    public int getSizeInBytes() {
        switch (type) {
            case TGF.GL_FLOAT:
            case TGF.GL_FIXED:
                return 4 * numComponents;
            case TGF.GL_UNSIGNED_BYTE:
            case TGF.GL_BYTE:
                return numComponents;
            case TGF.GL_UNSIGNED_SHORT:
            case TGF.GL_SHORT:
                return 2 * numComponents;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int result = getKey();
        result = 541 * result + numComponents;
        result = 541 * result + alias.hashCode();
        return result;
    }
}
