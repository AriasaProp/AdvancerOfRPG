package com.ariasaproject.advancerofrpg.graphics.g3d.environment;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.BaseLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.Environment;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.StrBuilder;

public class PointLights implements Iterable<PointLights.PointLight>, Disposable {
	static final float mathFOY = (float) Math.tan(Math.PI / 4.0f);
	// face view sorted by x+, x-, y+, y-, z+, z-
	static final float[] faceView = new float[] { 01, 0, 0, -01, 0, 0, 00, 1, 0, 00, -1, 0, 00, 0, 1, 00, 0, -1 },
			upView = new float[] { 00, 1, 0, 00, 1, 0, 00, 0, -1, 00, 0, -1, 00, 1, 0, 00, 1, 0 },
			sideView = new float[] { 00, 0, -1, 00, 0, 1, 01, 0, 0, 01, 0, 0, 01, 0, 0, -01, 0, 0 };

	protected PointLight[] items = new PointLight[1];
	protected int size = 0;
	private EntityIterable iterable;

	public PointLights() {
	}

	public void prepare(final Environment env) {
		if (size == 0)
			return;
		TGF g = GraphFunc.tgf;
		for (PointLight p : items) {

			final float mathZ = (p.far + p.near) / (p.near - p.far);
			for (int i = 0; i < 6; i++) {
				final int indF = i * 3;
				p.r[i][0] = sideView[indF] / mathFOY;
				p.r[i][1] = upView[indF] / mathFOY;
				p.r[i][2] = -faceView[indF] * mathZ;
				p.r[i][3] = faceView[indF];
				p.r[i][4] = sideView[indF + 1] / mathFOY;
				p.r[i][5] = upView[indF + 1] / mathFOY;
				p.r[i][6] = -faceView[indF + 1] * mathZ;
				p.r[i][7] = faceView[indF + 1];
				p.r[i][8] = sideView[indF + 2] / mathFOY;
				p.r[i][9] = upView[indF + 2] / mathFOY;
				p.r[i][10] = -faceView[indF + 2] * mathZ;
				p.r[i][11] = faceView[indF + 2];
				p.r[i][12] = (-(sideView[indF] * p.position.x) - sideView[indF + 1] * p.position.y - sideView[indF + 2] * p.position.z) / mathFOY;
				p.r[i][13] = (-(upView[indF] * p.position.x) - upView[indF + 1] * p.position.y - upView[indF + 2] * p.position.z) / mathFOY;
				p.r[i][14] = (faceView[indF] * p.position.x + faceView[indF + 1] * p.position.y + faceView[indF + 2] * p.position.z) * mathZ - 1;
				p.r[i][15] = -(faceView[indF] * p.position.x + faceView[indF + 1] * p.position.y + faceView[indF + 2] * p.position.z);
			}
		}
	}

	public void add(PointLight... values) {
		final PointLight[] items = this.items;
		if (size + values.length - 1 >= items.length) {
			PointLight[] newItems = new PointLight[size + values.length];
			System.arraycopy(items, 0, newItems, 0, items.length);
			this.items = newItems;
		}
		for (PointLight value : values) {
			this.items[size++] = value;
		}
	}

	public final int size() {
		return size;
	}

	public PointLight get(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		return items[index];
	}

	public boolean removeValue(@Null PointLight value) {
		if (value == null)
			return false;
		PointLight[] items = this.items;
		for (int i = 0, n = size; i < n; i++) {
			if (value.equals(items[i])) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	public PointLight removeIndex(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		PointLight[] items = this.items;
		PointLight value = items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
		items[size] = null;
		return value;
	}

	@Override
	public void dispose() {
		Arrays.fill(items, null);
		size = 0;
		// GL g = GraphFunc.gl;
	}

	@Override
	public EntityIterator iterator() {
		if (iterable == null)
			iterable = new EntityIterable(this);
		return iterable.iterator();
	}

	@Override
	public int hashCode() {
		PointLight[] items = this.items;
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
	public boolean equals(Object object) {
		if (object == this)
			return true;
		if (!(object instanceof PointLights))
			return false;
		PointLights array = (PointLights) object;
		int n = size;
		if (n != array.size)
			return false;
		PointLight[] items1 = this.items, items2 = array.items;
		for (int i = 0; i < n; i++) {
			Object o1 = items1[i], o2 = items2[i];
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return true;
	}

	final StrBuilder buffer = new StrBuilder(32);

	@Override
	public String toString() {
		if (size == 0)
			return "[]";
		PointLight[] items = this.items;
		buffer.clear();
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	static public class EntityIterator implements Iterator<PointLight>, Iterable<PointLight> {
		private final PointLights array;
		int index;
		boolean valid = true;

		public EntityIterator(PointLights array) {
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
		public PointLight next() {
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

	static public class EntityIterable implements Iterable<PointLight> {
		private final PointLights array;
		private EntityIterator iterator1, iterator2;

		public EntityIterable(PointLights array) {
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

	public static class PointLight implements BaseLight {
		public final Vector3 position = new Vector3(0, 0, 0);
		public float near = 0, far = 100;
		private int shadowIndex = -1;
		final float[][] r = new float[6][16];

		public PointLight(Color color, Vector3 pos) {
			this(color, pos, 0, 100);
		}

		public PointLight(Color color, Vector3 position, float nearLight, float farLight) {
			BaseLight.color.set(color);
			this.position.set(position);
			this.near = nearLight;
			this.far = farLight;
		}

		public void setShadowIndex(final int index) {
			shadowIndex = index;
		}

		public int getShadowIndex() {
			return shadowIndex;
		}

		// face is 0 - > 5
		public final float[] getProjection(int face) {
			if (face < 0 || face > 5)
				return null;
			return r[face];
		}

		public void set(PointLight o) {
			this.position.set(o.position);
			BaseLight.color.set(BaseLight.color);
			this.near = o.near;
			this.far = o.far;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 == null)
				return false;
			if (!(arg0 instanceof PointLight))
				return false;
			PointLight other = (PointLight) arg0;
			if (!color.equals(BaseLight.color))
				return false;
			if (!position.equals(other.position))
				return false;
			if (near != other.near)
				return false;
			return far == other.far;
		}
	}
}
