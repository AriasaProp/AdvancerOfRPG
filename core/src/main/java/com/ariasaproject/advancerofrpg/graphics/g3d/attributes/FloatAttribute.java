package com.ariasaproject.advancerofrpg.graphics.g3d.attributes;

import com.ariasaproject.advancerofrpg.graphics.g3d.Attribute;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.utils.NumberUtils;

public class FloatAttribute extends Attribute {
    public static final String ShininessAlias = "shininess";
    public static final long Shininess = register(ShininessAlias);
    public float value;

    public FloatAttribute(long type) {
        super(type);
    }

    public FloatAttribute(long type, float value) {
        super(type);
        this.value = value;
    }

    public static FloatAttribute createShininess(float value) {
        return new FloatAttribute(Shininess, value);
    }

    @Override
    public Attribute copy() {
        return new FloatAttribute(type, value);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 977 * result + NumberUtils.floatToRawIntBits(value);
        return result;
    }

    @Override
    public int compareTo(Attribute o) {
        if (type != o.type)
            return (int) (type - o.type);
        final float v = ((FloatAttribute) o).value;
        return MathUtils.isEqual(value, v) ? 0 : value < v ? -1 : 1;
    }
}
