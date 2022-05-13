package com.ariasaproject.advancerofrpg.graphics.g3d.particles.renderers;

import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParallelArray.FloatChannel;

/**
 * Render data used by billboard particle batches
 *
 * @author Inferno
 */
public class BillboardControllerRenderData extends ParticleControllerRenderData {
	public FloatChannel regionChannel, colorChannel, scaleChannel, rotationChannel;
}
