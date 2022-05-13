package com.ariasaproject.advancerofrpg.graphics.g3d.environment;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.BaseLight;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.Null;

public class DirectionalLights implements Iterable<DirectionalLights.DirectionalLight>, Disposable {
	public DirectionalLight[] items = new DirectionalLight[1];
	protected int size = 0;
	private EntityIterable iterable;

	public DirectionalLights() {
	}

	public void setShadowIndex(int index, int shadowIndex) {
		get(index).shadowIndex = shadowIndex;
	}

	public int getShadowIndex(int index) {
		return get(index).shadowIndex;
	}

	public void prepare(Camera cam) {
		if (size == 0)
			return;
		for (int i = 0; i < size; i++) {
			DirectionalLight l = items[i];
			final float r = Math.max(cam.far - cam.near, Math.max(cam.viewportHeight, cam.viewportWidth)) * 0.7f;
			l.d.nor();
			l.s.set(l.u).crs(l.d).nor();
			l.u.set(l.d).crs(l.s).nor();
			l.s.set(l.u).crs(l.d).nor();
			l.pos.set(cam.position).mulAdd(cam.direction, cam.far / 2).mulAdd(l.d, -r);
			l.proj[0] = l.s.x / r;
			l.proj[4] = l.s.y / r;
			l.proj[8] = l.s.z / r;
			l.proj[12] = (-l.s.x * l.pos.x - l.s.y * l.pos.y - l.s.z * l.pos.z) / r;
			l.proj[1] = l.u.x / r;
			l.proj[5] = l.u.y / r;
			l.proj[9] = l.u.z / r;
			l.proj[13] = (-l.u.x * l.pos.x - l.u.y * l.pos.y - l.u.z * l.pos.z) / r;
			l.proj[2] = l.d.x / r;
			l.proj[6] = l.d.y / r;
			l.proj[10] = l.d.y / r;
			l.proj[14] = ((-l.d.x * l.pos.x - l.d.y * l.pos.y - l.d.z * l.pos.z) / r) - 1;
			l.proj[3] = l.proj[7] = l.proj[11] = 0;
			l.proj[15] = 1;

		}
	}

	public float[] getProjection(int index) {
		return items[index].proj;
	}

	public void add(DirectionalLight... values) {
		final DirectionalLight[] items = this.items;
		if (size + values.length - 1 >= items.length) {
			DirectionalLight[] newItems = new DirectionalLight[size + values.length];
			System.arraycopy(items, 0, newItems, 0, items.length);
			this.items = newItems;
		}
		for (DirectionalLight value : values) {
			this.items[size++] = value;
		}
	}

	public DirectionalLight get(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		return items[index];
	}

	public boolean removeValue(@Null DirectionalLight value) {
		if (value != null) {
			DirectionalLight[] items = this.items;
			for (int i = 0, n = size; i < n; i++) {
				if (value.equals(items[i])) {
					removeIndex(i);
					return true;
				}
			}
		}
		return false;
	}

	public DirectionalLight removeIndex(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		DirectionalLight[] items = this.items;
		DirectionalLight value = items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
		items[size] = null;
		return value;
	}

	public final int size() {
		return size;
	}

	@Override
	public void dispose() {
		Arrays.fill(items, null);
		size = 0;
	}

	@Override
	public EntityIterator iterator() {
		if (iterable == null)
			iterable = new EntityIterable(this);
		return iterable.iterator();
	}

	@Override
	public int hashCode() {
		DirectionalLight[] items = this.items;
		int h = 1;
		for (int i = 0, n = size; i < n; i++) {
			h *= 31;
			Object item = items[i];
			if (item != null)
				h += item.hashCode();
		}
		return h;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this)
			return true;
		if (!(object instanceof DirectionalLights))
			return false;
		DirectionalLights array = (DirectionalLights) object;
		int n = size;
		if (n != array.size)
			return false;
		DirectionalLight[] items1 = this.items, items2 = array.items;
		for (int i = 0; i < n; i++) {
			final Object o1 = items1[i], o2 = items2[i];
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return true;
	}

	static public class EntityIterator implements Iterator<DirectionalLight>, Iterable<DirectionalLight> {
		private final DirectionalLights array;
		int index;
		boolean valid = true;

		public EntityIterator(DirectionalLights array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			if (!valid) {
				throw new RuntimeException("#iterator() cannot be used nested.");
			}
			return index < array.size;
		}

		@Override
		public DirectionalLight next() {
			if (index >= array.size)
				throw new NoSuchElementException(String.valueOf(index));
			if (!valid) {
				throw new RuntimeException("#iterator() cannot be used nested.");
			}
			return array.items[index++];
		}

		@Override
		public void remove() {
			index--;
			array.removeIndex(index);
		}

		public void reset() {
			index = 0;
		}

		@Override
		public EntityIterator iterator() {
			return this;
		}
	}

	static public class EntityIterable implements Iterable<DirectionalLight> {
		private final DirectionalLights array;
		private EntityIterator iterator1, iterator2;

		public EntityIterable(DirectionalLights array) {
			this.array = array;
		}

		@Override
		public EntityIterator iterator() {
			if (iterator1 == null) {
				iterator1 = new EntityIterator(array);
				iterator2 = new EntityIterator(array);
			}
			if (!iterator1.valid) {
				iterator1.index = 0;
				iterator1.valid = true;
				iterator2.valid = false;
				return iterator1;
			}
			iterator2.index = 0;
			iterator2.valid = true;
			iterator1.valid = false;
			return iterator2;
		}
	}

	public static class DirectionalLight implements BaseLight {
		public final Vector3 d = new Vector3(-1, -1, -1).nor(), u = new Vector3(0, 1, 0), s = new Vector3(1, 0, 0),
				pos = new Vector3();
		protected final float[] proj = new float[16];
		private int shadowIndex = -1;

		public DirectionalLight(Color light, Vector3 dir) {
			BaseLight.color.set(light);
			this.d.set(dir);
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 == null)
				return false;
			if (!(arg0 instanceof DirectionalLight))
				return false;
			DirectionalLight other = (DirectionalLight) arg0;
			if (!color.equals(BaseLight.color))
				return false;
			return d.equals(other.d);

		}
	}
}
