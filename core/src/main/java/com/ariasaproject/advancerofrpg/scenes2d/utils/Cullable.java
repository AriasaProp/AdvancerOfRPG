package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.math.Rectangle;
import com.ariasaproject.advancerofrpg.utils.Null;

/**
 * Allows a parent to set the area that is visible on a child actor to allow the
 * child to cull when drawing itself. This must only be used for actors that are
 * not rotated or scaled.
 *
 * @author Nathan Sweet
 */
public interface Cullable {
    /**
     * @param cullingArea The culling area in the child actor's coordinates.
     */
    void setCullingArea(@Null Rectangle cullingArea);
}
