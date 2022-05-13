package com.ariasaproject.advancerofrpg.graphics.g3d.decals;

import java.util.Comparator;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;
import com.ariasaproject.advancerofrpg.utils.Pool;

public class CameraGroupStrategy implements GroupStrategy, Disposable {
	private static final int GROUP_OPAQUE = 0;
	private static final int GROUP_BLEND = 1;
	private final Comparator<Decal> cameraSorter;
	Pool<Array<Decal>> arrayPool = new Pool<Array<Decal>>(16) {
		@Override
		protected Array<Decal> newObject() {
			return new Array<Decal>();
		}
	};
	Array<Array<Decal>> usedArrays = new Array<Array<Decal>>();
	ObjectMap<DecalMaterial, Array<Decal>> materialGroups = new ObjectMap<DecalMaterial, Array<Decal>>();
	Camera camera;
	ShaderProgram shader;

	public CameraGroupStrategy(final Camera camera) {
		this(camera, new Comparator<Decal>() {
			@Override
			public int compare(Decal o1, Decal o2) {
				float dist1 = camera.position.dst(o1.position);
				float dist2 = camera.position.dst(o2.position);
				return (int) Math.signum(dist2 - dist1);
			}
		});
	}

	public CameraGroupStrategy(Camera camera, Comparator<Decal> sorter) {
		this.camera = camera;
		this.cameraSorter = sorter;
		shader = new ShaderProgram(GraphFunc.app.getFiles().internal("shader/basic.shaderprogram"));

	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public int decideGroup(Decal decal) {
		return decal.getMaterial().isOpaque() ? GROUP_OPAQUE : GROUP_BLEND;
	}

	@Override
	public void beforeGroup(int group, Array<Decal> contents) {
		if (group == GROUP_BLEND) {
			GraphFunc.tgf.capabilitySwitch(true, TGF.GL_BLEND);
			contents.sort(cameraSorter);
		} else {
			for (int i = 0, n = contents.size; i < n; i++) {
				Decal decal = contents.get(i);
				Array<Decal> materialGroup = materialGroups.get(decal.material);
				if (materialGroup == null) {
					materialGroup = arrayPool.obtain();
					materialGroup.clear();
					usedArrays.add(materialGroup);
					materialGroups.put(decal.material, materialGroup);
				}
				materialGroup.add(decal);
			}
			contents.clear();
			for (Array<Decal> materialGroup : materialGroups.values()) {
				contents.addAll(materialGroup);
			}
			materialGroups.clear();
			arrayPool.freeAll(usedArrays);
			usedArrays.clear();
		}
	}

	@Override
	public void afterGroup(int group) {
		if (group == GROUP_BLEND) {
			GraphFunc.tgf.capabilitySwitch(false, TGF.GL_BLEND);
		}
	}

	@Override
	public void beforeGroups() {
		GraphFunc.tgf.capabilitySwitch(true, TGF.GL_DEPTH_TEST);
		shader.bind();
		shader.setUniformMatrix("u_projectionViewMatrix", camera.combined);
		shader.setUniformi("u_texture", 0);
	}

	@Override
	public void afterGroups() {
		GraphFunc.tgf.capabilitySwitch(false, TGF.GL_DEPTH_TEST);
	}

	@Override
	public ShaderProgram getGroupShader(int group) {
		return shader;
	}

	@Override
	public void dispose() {
		if (shader != null)
			shader.dispose();
	}
}
