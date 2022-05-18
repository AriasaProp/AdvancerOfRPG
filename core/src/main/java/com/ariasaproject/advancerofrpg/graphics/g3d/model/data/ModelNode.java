package com.ariasaproject.advancerofrpg.graphics.g3d.model.data;

import com.ariasaproject.advancerofrpg.math.Quaternion;
import com.ariasaproject.advancerofrpg.math.Vector3;

public class ModelNode {
	public String id;
	public Vector3 translation;
	public Quaternion rotation;
	public Vector3 scale;
	public String meshId;
	public ModelNodePart[] parts;
	public ModelNode[] children;
}
