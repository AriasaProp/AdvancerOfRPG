package com.ariasaproject.advancerofrpg.graphics.g3d;

import com.ariasaproject.advancerofrpg.graphics.g3d.model.MeshPart;
import com.ariasaproject.advancerofrpg.math.Matrix4;

public class Renderable {
    public final Matrix4 worldTransform = new Matrix4();
    public final MeshPart meshPart = new MeshPart();
    public Material material;
    public Matrix4[] bones;

    public Renderable set(Renderable renderable) {
        worldTransform.set(renderable.worldTransform);
        material = renderable.material;
        meshPart.set(renderable.meshPart);
        bones = renderable.bones;
        return this;
    }
}
