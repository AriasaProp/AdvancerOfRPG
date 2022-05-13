package com.ariasaproject.advancerofrpg.graphics.g3d.particles.values;

import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Vector3;

/**
 * Encapsulate the formulas to spawn a particle on a line shape.
 *
 * @author Inferno
 */
public final class LineSpawnShapeValue extends PrimitiveSpawnShapeValue {

	public LineSpawnShapeValue(LineSpawnShapeValue value) {
		super(value);
		load(value);
	}

	public LineSpawnShapeValue() {
	}

	@Override
	public void spawnAux(Vector3 vector, float percent) {
		float width = spawnWidth + (spawnWidthDiff * spawnWidthValue.getScale(percent));
		float height = spawnHeight + (spawnHeightDiff * spawnHeightValue.getScale(percent));
		float depth = spawnDepth + (spawnDepthDiff * spawnDepthValue.getScale(percent));
		float a = MathUtils.random();
		vector.x = a * width;
		vector.y = a * height;
		vector.z = a * depth;
	}

	@Override
	public SpawnShapeValue copy() {
		return new LineSpawnShapeValue(this);
	}
}
