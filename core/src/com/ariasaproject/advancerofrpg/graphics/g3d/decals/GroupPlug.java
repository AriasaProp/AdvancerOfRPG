package com.ariasaproject.advancerofrpg.graphics.g3d.decals;

import com.ariasaproject.advancerofrpg.utils.Array;

/**
 * Handles a single group's pre and post render arrangements. Can be plugged
 * into {@link PluggableGroupStrategy} to build modular {@link GroupStrategy
 * GroupStrategies}.
 */
public interface GroupPlug {
	void beforeGroup(Array<Decal> contents);

	void afterGroup();
}
