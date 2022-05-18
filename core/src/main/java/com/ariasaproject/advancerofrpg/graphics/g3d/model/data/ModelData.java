package com.ariasaproject.advancerofrpg.graphics.g3d.model.data;

import com.ariasaproject.advancerofrpg.assets.loaders.ModelLoader;
import com.ariasaproject.advancerofrpg.utils.Array;

/**
 * Returned by a {@link ModelLoader}, contains meshes, materials, nodes and
 * animations. OpenGL resources like textures or vertex buffer objects are not
 * stored. Instead, a ModelData instance needs to be converted to a Model first.
 *
 * @author badlogic
 */
public class ModelData {
	public final short[] version = new short[2];
	public final Array<ModelMesh> meshes = new Array<ModelMesh>();
	public final Array<ModelMaterial> materials = new Array<ModelMaterial>();
	public final Array<ModelNode> nodes = new Array<ModelNode>();
	public final Array<ModelAnimation> animations = new Array<ModelAnimation>();
	public String id;

	public void addMesh(ModelMesh mesh) {
		for (ModelMesh other : meshes) {
			if (other.id.equals(mesh.id)) {
				throw new RuntimeException("Mesh with id '" + other.id + "' already in model");
			}
		}
		meshes.add(mesh);
	}
}
