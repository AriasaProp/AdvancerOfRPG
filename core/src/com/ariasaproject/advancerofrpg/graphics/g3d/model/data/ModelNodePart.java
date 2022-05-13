package com.ariasaproject.advancerofrpg.graphics.g3d.model.data;

import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.utils.ArrayMap;

public class ModelNodePart {
	public String materialId;
	public String meshPartId;
	public ArrayMap<String, Matrix4> bones;
	public int[][] uvMapping;
}
