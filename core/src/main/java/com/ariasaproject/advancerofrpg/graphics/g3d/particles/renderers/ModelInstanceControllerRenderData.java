package com.ariasaproject.advancerofrpg.graphics.g3d.particles.renderers;

import com.ariasaproject.advancerofrpg.graphics.g3d.ModelInstance;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParallelArray.ObjectChannel;

/**
 * Render data used by model instance particle batches
 *
 * @author Inferno
 */
public class ModelInstanceControllerRenderData extends ParticleControllerRenderData {
    public ObjectChannel<ModelInstance> modelInstanceChannel;
    public FloatChannel colorChannel, scaleChannel, rotationChannel;

}
