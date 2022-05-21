package com.ariasaproject.advancerofrpg.graphics.g3d.model;

import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.math.Quaternion;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;

/**
 * A NodeAnimation defines keyframes for a {@link Node} in a {@link Model}. The
 * keyframes are given as a translation vector, a rotation quaternion and a
 * scale vector. Keyframes are interpolated linearly for now. Keytimes are given
 * in seconds.
 *
 * @author badlogic, Xoppa
 */
public class NodeAnimation {
    /**
     * the Node affected by this animation
     **/
    public Node node;
    /**
     * the translation keyframes if any (might be null), sorted by time ascending
     **/
    public Array<NodeKeyframe<Vector3>> translation = null;
    /**
     * the rotation keyframes if any (might be null), sorted by time ascending
     **/
    public Array<NodeKeyframe<Quaternion>> rotation = null;
    /**
     * the scaling keyframes if any (might be null), sorted by time ascending
     **/
    public Array<NodeKeyframe<Vector3>> scaling = null;
}
