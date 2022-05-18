package com.ariasaproject.advancerofrpg.graphics.g3d.model;

import com.ariasaproject.advancerofrpg.utils.Array;

public class Animation {
	/**
	 * the unique id of the animation
	 **/
	public String id;
	/**
	 * the duration in seconds
	 **/
	public float duration;
	/**
	 * the animation curves for individual nodes
	 **/
	public Array<NodeAnimation> nodeAnimations = new Array<NodeAnimation>();
}
