package com.ariasaproject.advancerofrpg.graphics.g3d.particles.batches;

import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleSorter;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.renderers.ParticleControllerRenderData;
import com.ariasaproject.advancerofrpg.utils.Array;

/**
 * Base class of all the batches requiring to buffer
 * {@link ParticleControllerRenderData}
 *
 * @author Inferno
 */
public abstract class BufferedParticleBatch<T extends ParticleControllerRenderData> implements ParticleBatch<T> {
	protected Array<T> renderData;
	protected int bufferedParticlesCount, currentCapacity = 0;
	protected ParticleSorter sorter;
	protected Camera camera;

	protected BufferedParticleBatch(Class<T> type) {
		this.sorter = new ParticleSorter.Distance();
		renderData = new com.ariasaproject.advancerofrpg.utils.Array<T>(false, 10, type);
	}

	@Override
	public void begin() {
		renderData.clear();
		bufferedParticlesCount = 0;
	}

	@Override
	public void draw(T data) {
		if (data.controller.particles.size > 0) {
			renderData.add(data);
			bufferedParticlesCount += data.controller.particles.size;
		}
	}

	/**
	 *
	 */
	@Override
	public void end() {
		if (bufferedParticlesCount > 0) {
			ensureCapacity(bufferedParticlesCount);
			flush(sorter.sort(renderData));
		}
	}

	/**
	 * Ensure the batch can contain the passed in amount of particles
	 */
	public void ensureCapacity(int capacity) {
		if (currentCapacity >= capacity)
			return;
		sorter.ensureCapacity(capacity);
		allocParticlesData(capacity);
		currentCapacity = capacity;
	}

	public void resetCapacity() {
		currentCapacity = bufferedParticlesCount = 0;
	}

	protected abstract void allocParticlesData(int capacity);

	public void setCamera(Camera camera) {
		this.camera = camera;
		sorter.setCamera(camera);
	}

	public ParticleSorter getSorter() {
		return sorter;
	}

	public void setSorter(ParticleSorter sorter) {
		this.sorter = sorter;
		sorter.setCamera(camera);
		sorter.ensureCapacity(currentCapacity);
	}

	/**
	 * Sends the data to the gpu. This method must use the calculated offsets to
	 * build the particles meshes. The offsets represent the position at which a
	 * particle should be placed into the vertex array.
	 *
	 * @param offsets the calculated offsets
	 */
	protected abstract void flush(int[] offsets);

	public int getBufferedCount() {
		return bufferedParticlesCount;
	}
}
