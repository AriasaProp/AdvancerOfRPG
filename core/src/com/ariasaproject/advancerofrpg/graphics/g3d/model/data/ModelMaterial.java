package com.ariasaproject.advancerofrpg.graphics.g3d.model.data;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.utils.Array;

public class ModelMaterial {
	public String id;
	public MaterialType type;
	public Color ambient;
	public Color diffuse;
	public Color specular;
	public Color emissive;
	public Color reflection;
	public float shininess;
	public float opacity = 1.f;
	public Array<ModelTexture> textures;

	public enum MaterialType {
		Lambert, Phong
	}
}
