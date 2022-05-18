package com.ariasaproject.advancerofrpg.graphics.g3d.attributes;

import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.g3d.Attribute;

public class CubemapAttribute extends Attribute {
	public final static String EnvironmentMapAlias = "environmentCubemap";
	public final static long EnvironmentMap = register(EnvironmentMapAlias);

	protected static long Mask = EnvironmentMap;
	public final Cubemap texture;

	public CubemapAttribute(final long type, final Cubemap texture) {
		super(type);
		if (!is(type))
			throw new RuntimeException("Invalid type specified");
		this.texture = texture;
	}

	public CubemapAttribute(final CubemapAttribute copyFrom) {
		this(copyFrom.type, copyFrom.texture);
	}

	public final static boolean is(final long mask) {
		return (mask & Mask) != 0;
	}

	@Override
	public Attribute copy() {
		return new CubemapAttribute(this);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 967 * result + texture.hashCode();
		return result;
	}

	@Override
	public int compareTo(Attribute o) {
		if (type != o.type)
			return (int) (type - o.type);
		return texture.compareTo(((CubemapAttribute) o).texture);
	}
}
