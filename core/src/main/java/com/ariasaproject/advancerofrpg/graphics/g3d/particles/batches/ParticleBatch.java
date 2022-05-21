package com.ariasaproject.advancerofrpg.graphics.g3d.particles.batches;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.graphics.g3d.RenderableProvider;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ResourceData;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.renderers.ParticleControllerRenderData;

/**
 * Common interface to all the batches that render particles.
 *
 * @author Inferno
 */
public interface ParticleBatch<T extends ParticleControllerRenderData> extends RenderableProvider, ResourceData.Configurable {

    /**
     * Must be called once before any drawing operation
     */
    void begin();

    void draw(T controller);

    /**
     * Must be called after all the drawing operations
     */
    void end();

    @Override
    void save(AssetContainer manager, ResourceData assetDependencyData);

    @Override
    void load(AssetContainer manager, ResourceData assetDependencyData);
}
