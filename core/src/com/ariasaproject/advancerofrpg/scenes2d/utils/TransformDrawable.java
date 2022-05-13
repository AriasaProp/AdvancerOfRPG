package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;

public interface TransformDrawable extends Drawable {
	void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX,
			float scaleY, float rotation);
}
