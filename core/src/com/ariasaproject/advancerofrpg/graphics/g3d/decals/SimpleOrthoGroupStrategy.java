package com.ariasaproject.advancerofrpg.graphics.g3d.decals;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Sort;

public class SimpleOrthoGroupStrategy implements GroupStrategy {
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;
	private final Comparator comparator = new Comparator();

	@Override
	public int decideGroup(Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	@Override
	public void beforeGroup(int group, Array<Decal> contents) {
		if (group == GROUP_BLEND) {
			Sort.instance().sort(contents, comparator);
			GraphFunc.tgf.capabilitySwitch(true, TGF.GL_BLEND);
			// no need for writing into the z buffer if transparent decals are the last
			// thing to be rendered
			// and they are rendered back to front
			GraphFunc.tgf.setDepthMask(false);
		} else {
			// FIXME sort by material
		}
	}

	@Override
	public void afterGroup(int group) {
		if (group == GROUP_BLEND) {
			GraphFunc.tgf.setDepthMask(true);
			GraphFunc.tgf.capabilitySwitch(false, TGF.GL_BLEND);
		}
	}

	@Override
	public void beforeGroups() {
		GraphFunc.tgf.capabilitySwitch(true, TGF.GL_TEXTURE_2D);
	}

	@Override
	public void afterGroups() {
		GraphFunc.tgf.capabilitySwitch(false, TGF.GL_TEXTURE_2D);
	}

	@Override
	public ShaderProgram getGroupShader(int group) {
		return null;
	}

	class Comparator implements java.util.Comparator<Decal> {
		@Override
		public int compare(Decal a, Decal b) {
			if (a.getZ() == b.getZ())
				return 0;
			return a.getZ() - b.getZ() < 0 ? -1 : 1;
		}
	}
}
