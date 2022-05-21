package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureRegion;

/**
 * Draws a {@link TextureRegion} repeatedly to fill the area, instead of
 * stretching it.
 *
 * @author Nathan Sweet
 */
public class TiledDrawable extends TextureRegionDrawable {
    static private final Color temp = new Color();

    private final Color color = new Color(1, 1, 1, 1);
    private float scale = 1;

    public TiledDrawable() {
        super();
    }

    public TiledDrawable(TextureRegion region) {
        super(region);
    }

    public TiledDrawable(TextureRegionDrawable drawable) {
        super(drawable);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        Color batchColor = batch.getColor();
        temp.set(batchColor);
        batch.setColor(batchColor.mul(color));
        TextureRegion region = getRegion();
        float regionWidth = region.getRegionWidth() * scale, regionHeight = region.getRegionHeight() * scale;
        int fullX = (int) (width / regionWidth), fullY = (int) (height / regionHeight);
        float remainingX = width - regionWidth * fullX, remainingY = height - regionHeight * fullY;
        float startX = x, startY = y;
        //float endX = x + width - remainingX, endY = y + height - remainingY;
        for (int i = 0; i < fullX; i++) {
            y = startY;
            for (int ii = 0; ii < fullY; ii++) {
                batch.draw(region, x, y, regionWidth, regionHeight);
                y += regionHeight;
            }
            x += regionWidth;
        }
        Texture texture = region.getTexture();
        float u = region.getU();
        float v2 = region.getV2();
        if (remainingX > 0) {
            // Right edge.
            float u2 = u + remainingX / texture.getWidth();
            float v = region.getV();
            y = startY;
            for (int ii = 0; ii < fullY; ii++) {
                batch.draw(texture, x, y, remainingX, regionHeight, u, v2, u2, v);
                y += regionHeight;
            }
            // Upper right corner.
            if (remainingY > 0) {
                v = v2 - remainingY / texture.getHeight();
                batch.draw(texture, x, y, remainingX, remainingY, u, v2, u2, v);
            }
        }
        if (remainingY > 0) {
            // Top edge.
            float u2 = region.getU2();
            float v = v2 - remainingY / texture.getHeight();
            x = startX;
            for (int i = 0; i < fullX; i++) {
                batch.draw(texture, x, y, regionWidth, remainingY, u, v2, u2, v);
                x += regionWidth;
            }
        }
        batch.setColor(temp);
    }

    @Override
    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        throw new UnsupportedOperationException();
    }

    public Color getColor() {
        return color;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public TiledDrawable tint(Color tint) {
        TiledDrawable drawable = new TiledDrawable(this);
        drawable.color.set(tint);
        drawable.setLeftWidth(getLeftWidth());
        drawable.setRightWidth(getRightWidth());
        drawable.setTopHeight(getTopHeight());
        drawable.setBottomHeight(getBottomHeight());
        return drawable;
    }
}
