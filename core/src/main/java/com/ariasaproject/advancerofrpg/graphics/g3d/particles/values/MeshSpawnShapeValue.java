package com.ariasaproject.advancerofrpg.graphics.g3d.particles.values;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ResourceData;
import com.ariasaproject.advancerofrpg.graphics.g3d.particles.ResourceData.SaveData;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Vector3;

public abstract class MeshSpawnShapeValue extends SpawnShapeValue {
    protected Mesh mesh;
    protected Model model;

    public MeshSpawnShapeValue(MeshSpawnShapeValue value) {
        super(value);
    }

    public MeshSpawnShapeValue() {
    }

    @Override
    public void load(ParticleValue value) {
        super.load(value);
        MeshSpawnShapeValue spawnShapeValue = (MeshSpawnShapeValue) value;
        setMesh(spawnShapeValue.mesh, spawnShapeValue.model);
    }

    public void setMesh(Mesh mesh, Model model) {
        if (mesh.getVertexAttribute(Usage.Position) == null)
            throw new RuntimeException("Mesh vertices must have Usage.Position");
        this.model = model;
        this.mesh = mesh;
    }

    public void setMesh(Mesh mesh) {
        this.setMesh(mesh, null);
    }

    @Override
    public void save(AssetContainer manager, ResourceData data) {
        if (model != null) {
            SaveData saveData = data.createSaveData();
            saveData.saveAsset(manager.getAssetFileName(model), Model.class);
            saveData.save("index", model.meshes.indexOf(mesh, true));
        }
    }

    @Override
    public void load(AssetContainer manager, ResourceData data) {
        SaveData saveData = data.getSaveData();
        AssetDescriptor<Model> descriptor = saveData.loadAsset();
        if (descriptor != null) {
            Model model = manager.get(descriptor);
            setMesh(model.meshes.get((Integer) saveData.load("index")), model);
        }
    }

    public static class Triangle {
        float x1, y1, z1, x2, y2, z2, x3, y3, z3;

        public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;
        }

        public static Vector3 pick(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, Vector3 vector) {
            float a = MathUtils.random(), b = MathUtils.random();
            return vector.set(x1 + a * (x2 - x1) + b * (x3 - x1), y1 + a * (y2 - y1) + b * (y3 - y1), z1 + a * (z2 - z1) + b * (z3 - z1));
        }

        public Vector3 pick(Vector3 vector) {
            float a = MathUtils.random(), b = MathUtils.random();
            return vector.set(x1 + a * (x2 - x1) + b * (x3 - x1), y1 + a * (y2 - y1) + b * (y3 - y1), z1 + a * (z2 - z1) + b * (z3 - z1));
        }
    }

}
