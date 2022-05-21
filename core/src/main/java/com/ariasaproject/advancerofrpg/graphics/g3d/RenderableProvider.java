package com.ariasaproject.advancerofrpg.graphics.g3d;

import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Pool;

public interface RenderableProvider {
    void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool);
}
