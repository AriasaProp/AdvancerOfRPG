package com.ariasaproject.advancerofrpg.graphics.g3d.utils;

import com.ariasaproject.advancerofrpg.graphics.GLTexture;
import com.ariasaproject.advancerofrpg.graphics.Texture;

public class TextureDescriptor<T extends GLTexture> implements Comparable<TextureDescriptor<T>> {
	public T texture = null;
	public Texture.TextureFilter minFilter;
	public Texture.TextureFilter magFilter;
	public Texture.TextureWrap uWrap;
	public Texture.TextureWrap vWrap;

	public TextureDescriptor(final T texture, final Texture.TextureFilter minFilter, final Texture.TextureFilter magFilter, final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
		set(texture, minFilter, magFilter, uWrap, vWrap);
	}

	public TextureDescriptor(final T texture) {
		this(texture, null, null, null, null);
	}

	public TextureDescriptor() {
	}

	public void set(final T texture, final Texture.TextureFilter minFilter, final Texture.TextureFilter magFilter, final Texture.TextureWrap uWrap, final Texture.TextureWrap vWrap) {
		this.texture = texture;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.uWrap = uWrap;
		this.vWrap = vWrap;
	}

	public <V extends T> void set(final TextureDescriptor<V> other) {
		texture = other.texture;
		minFilter = other.minFilter;
		magFilter = other.magFilter;
		uWrap = other.uWrap;
		vWrap = other.vWrap;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof TextureDescriptor))
			return false;
		final TextureDescriptor other = (TextureDescriptor) obj;
		return other.texture == texture && other.minFilter == minFilter && other.magFilter == magFilter && other.uWrap == uWrap && other.vWrap == vWrap;
	}

	@Override
	public int hashCode() {
		long result = (texture == null ? 0 : texture.glTarget);
		result = 811 * result + (texture == null ? 0 : texture.getTextureObjectHandle());
		result = 811 * result + (minFilter == null ? 0 : minFilter.glEnum);
		result = 811 * result + (magFilter == null ? 0 : magFilter.glEnum);
		result = 811 * result + (uWrap == null ? 0 : uWrap.glEnum);
		result = 811 * result + (vWrap == null ? 0 : vWrap.glEnum);
		return (int) (result ^ (result >> 32));
	}

	@Override
	public int compareTo(TextureDescriptor<T> o) {
		if (o == this)
			return 0;
		int t1 = texture == null ? 0 : texture.glTarget;
		int t2 = o.texture == null ? 0 : o.texture.glTarget;
		if (t1 != t2)
			return t1 - t2;
		int h1 = texture == null ? 0 : texture.getTextureObjectHandle();
		int h2 = o.texture == null ? 0 : o.texture.getTextureObjectHandle();
		if (h1 != h2)
			return h1 - h2;
		if (minFilter != o.minFilter)
			return (minFilter == null ? 0 : minFilter.glEnum) - (o.minFilter == null ? 0 : o.minFilter.glEnum);
		if (magFilter != o.magFilter)
			return (magFilter == null ? 0 : magFilter.glEnum) - (o.magFilter == null ? 0 : o.magFilter.glEnum);
		if (uWrap != o.uWrap)
			return (uWrap == null ? 0 : uWrap.glEnum) - (o.uWrap == null ? 0 : o.uWrap.glEnum);
		if (vWrap != o.vWrap)
			return (vWrap == null ? 0 : vWrap.glEnum) - (o.vWrap == null ? 0 : o.vWrap.glEnum);
		return 0;
	}
}
