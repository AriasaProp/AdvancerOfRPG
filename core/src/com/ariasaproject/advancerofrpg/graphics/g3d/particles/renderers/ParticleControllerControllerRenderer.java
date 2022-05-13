package com.ariasaproject.advancerofrpg.graphics.g3d.particles.renderers;

import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleChannels;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleController;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleControllerComponent;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.batches.ParticleBatch;

public class ParticleControllerControllerRenderer extends ParticleControllerRenderer {
	ObjectChannel<ParticleController> controllerChannel;

	@Override
	public void init() {
		controllerChannel = controller.particles.getChannel(ParticleChannels.ParticleController);
		if (controllerChannel == null)
			throw new RuntimeException(
					"ParticleController channel not found, specify an influencer which will allocate it please.");
	}

	@Override
	public void update() {
		for (int i = 0, c = controller.particles.size; i < c; ++i) {
			controllerChannel.data[i].draw();
		}
	}

	@Override
	public ParticleControllerComponent copy() {
		return new ParticleControllerControllerRenderer();
	}

	@Override
	public boolean isCompatible(ParticleBatch batch) {
		return false;
	}

}
