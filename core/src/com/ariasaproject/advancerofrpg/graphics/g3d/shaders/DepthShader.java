package com.ariasaproject.advancerofrpg.graphics.g3d.shaders;

import com.ariasaproject.advancerofrpg.Files;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.GLTexture;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttribute;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.g3d.Attributes;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.TextureBinder;
import com.ariasaproject.advancerofrpg.graphics.g3d.Renderable;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.BlendingAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.TextureAttribute;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.IntArray;
import com.ariasaproject.advancerofrpg.utils.IntIntMap;

public class DepthShader implements Disposable {
	private final Array<String> uniforms = new Array<String>();
	private final Array<Validator> validators = new Array<Validator>();
	private final Array<Setter> setters = new Array<Setter>();
	private final IntArray localUniforms = new IntArray();
	private final IntIntMap attributes = new IntIntMap();
	private final IntArray tempArray = new IntArray();
	private final Attributes combinedAttributes = new Attributes();
	public ShaderProgram program;
	public TextureBinder textureBinder;
	private final int[] locations;
	private Mesh currentMesh;

	static final String defaultShaderProgram = Files.readFileAsString("shader/depth.shaderprogram", Files.FileType.Internal);
	// Global uniform locations
	private final int u_projViewTrans;
	// Object uniforms locations
	private final int u_worldTrans;
	// private final int u_bones;
	// Material uniforms
	// private final int u_diffuseTexture;
	// another uniform
	private final int numBones;
	private final int weights;
	/**
	 * The attributes that this shader supports
	 */
	protected final long attributesMask;
	private final long vertexMask;

	public DepthShader(final Renderable renderable) {
		combinedAttributes.clear();
		if (renderable.material != null)
			combinedAttributes.set(renderable.material);

		// create prefix based attribute local, global etc.
		// prepared with light uniform to
		String prefix = "";
		this.attributesMask = combinedAttributes.getMask();
		this.vertexMask = renderable.meshPart.mesh.getVertexAttributes().getMaskWithSizePacked();
		if ((vertexMask & Usage.Position) != Usage.Position)
			throw new RuntimeException("renderable has not position attribute? why?");

		int w = 0;
		for (final VertexAttribute attr : renderable.meshPart.mesh.getVertexAttributes()) {
			if (attr.usage == Usage.BoneWeight) {
				prefix += "#define boneWeight" + attr.unit + "Flag\n";
				w |= (1 << attr.unit);
			} else if (attr.usage == Usage.TextureCoordinates)
				prefix += "#define texCoord" + attr.unit + "Flag\n";
		}
		weights = w;
		if ((attributesMask & BlendingAttribute.Type) == BlendingAttribute.Type)
			prefix += "#define " + BlendingAttribute.Alias + "Flag\n";
		if ((attributesMask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse) {
			prefix += "#define " + TextureAttribute.DiffuseAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.DiffuseAlias + "Coord texCoord0\n"; // FIXME
																						// implement
																						// UV
																						// mapping
		}
		this.numBones = renderable.bones == null ? 0 : 12;
		if (this.numBones > 0) {
			prefix += "#define numBones " + this.numBones + "\n";
		}
		this.program = new ShaderProgram(defaultShaderProgram, prefix);

		// Global Uniforms
		u_projViewTrans = program.fetchUniformLocation("u_projViewTrans", true);
		// Object uniforms
		u_worldTrans = program.fetchUniformLocation("u_worldTrans", true);
		// u_diffuseTexture = register(Inputs.diffuseTexture,
		// Setters.diffuseTexture);
		// u_bones = (this.numBones > 0) ? register(Inputs.bones, new
		// Setters.Bones(numBones)) : -1;

		final int n = uniforms.size;
		locations = new int[n];
		for (int i = 0; i < n; i++) {
			final String input = uniforms.get(i);
			final Validator validator = validators.get(i);
			final Setter setter = setters.get(i);
			if (validator != null && !validator.validate(this, i, renderable))
				locations[i] = -1;
			else {
				locations[i] = program.fetchUniformLocation(input, false);
				if (locations[i] >= 0 && setter != null) {
					localUniforms.add(i);
				}
			}
			if (locations[i] < 0) {
				validators.set(i, null);
				setters.set(i, null);
			}
		}
		if (renderable != null) {
			final VertexAttributes attrs = renderable.meshPart.mesh.getVertexAttributes();
			final int c = attrs.size();
			for (int i = 0; i < c; i++) {
				final VertexAttribute attr = attrs.get(i);
				final int location = program.getAttributeLocation(attr.alias);
				if (location >= 0)
					this.attributes.put(attr.getKey(), location);
			}
		}
	}

	public int compareTo(DepthShader other) {
		if (other == null)
			return -1;
		if (other == this)
			return 0;
		return 0; // FIXME compare shaders on their impact on performance
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof DepthShader))
			return false;
		DepthShader o = (DepthShader) obj;
		return o == this;
	}

	public void begin(final float[] projView, TextureBinder textureBinder) {
		this.textureBinder = textureBinder;
		program.bind();
		currentMesh = null;
		program.setUniformMatrix4fv(u_projViewTrans, projView, 0, 1);
	}

	public void end() {
		if (currentMesh != null) {
			currentMesh.unbind();
			currentMesh = null;
		}
	}

	public boolean canRender(Renderable renderable) {
		final Attributes attrs = new Attributes();
		if (renderable.material != null)
			attrs.set(renderable.material);
		if (attrs.has(BlendingAttribute.Type)) {
			if ((attributesMask & BlendingAttribute.Type) != BlendingAttribute.Type)
				return false;
			if (attrs.has(TextureAttribute.Diffuse) != ((attributesMask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse))
				return false;
		}
		final boolean skinned = ((renderable.meshPart.mesh.getVertexAttributes().getMask() & Usage.BoneWeight) == Usage.BoneWeight);
		if (skinned != (numBones > 0))
			return false;
		if (!skinned)
			return true;
		int w = 0;
		for (final VertexAttribute attr : renderable.meshPart.mesh.getVertexAttributes()) {
			if (attr.usage == Usage.BoneWeight)
				w |= (1 << attr.unit);
		}
		return w == weights;
	}

	public void render(Renderable renderable) {
		if (renderable.worldTransform.det3x3() == 0)
			return;
		combinedAttributes.clear();
		if (renderable.material != null)
			combinedAttributes.set(renderable.material);
		TGF g = GraphFunc.tgf;
		boolean blended = false;
		int blendSrc = TGF.GL_SRC_ALPHA, blendDest = TGF.GL_ONE_MINUS_SRC_ALPHA;
		if (combinedAttributes.has(BlendingAttribute.Type)) {
			BlendingAttribute blend = combinedAttributes.<BlendingAttribute>get(BlendingAttribute.Type);
			blended = blend.blended;
			blendSrc = blend.sourceFunction;
			blendDest = blend.destFunction;
		}
		g.setBlending(blended, blendSrc, blendDest);
		for (int u, i = 0; i < localUniforms.size; ++i)
			if (setters.get(u = localUniforms.get(i)) != null)
				setters.get(u).set(this, u, renderable, combinedAttributes);
		program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
		if (currentMesh != renderable.meshPart.mesh) {
			if (currentMesh != null)
				currentMesh.unbind();
			currentMesh = renderable.meshPart.mesh;
			VertexAttributes attrs = currentMesh.getVertexAttributes();
			tempArray.clear();
			for (int i = 0, j = attrs.size(); i < j; i++) {
				tempArray.add(attributes.get(attrs.get(i).getKey(), -1));
			}
			tempArray.shrink();
			currentMesh.bind(program, tempArray.items);
		}
		renderable.meshPart.render(program, false);
	}

	public int register(final String alias, final Validator validator, final Setter setter) {
		final int existing = uniforms.indexOf(alias, true);
		if (existing >= 0) {
			validators.set(existing, validator);
			setters.set(existing, setter);
			return existing;
		}
		uniforms.add(alias);
		validators.add(validator);
		setters.add(setter);
		return uniforms.size - 1;
	}

	public int register(final String alias, final Validator validator) {
		return register(alias, validator, null);
	}

	public int register(final String alias, final Setter setter) {
		return register(alias, null, setter);
	}

	public int register(final String alias) {
		return register(alias, null, null);
	}

	public int register(final Uniform uniform, final Setter setter) {
		return register(uniform.alias, uniform, setter);
	}

	public int register(final Uniform uniform) {
		return register(uniform, null);
	}

	@Override
	public void dispose() {
		program.dispose();
		program = null;
		uniforms.clear();
		validators.clear();
		setters.clear();
		localUniforms.clear();
	}

	public final int loc(final int inputID) {
		return (inputID >= 0 && inputID < locations.length) ? locations[inputID] : -1;
	}

	public final boolean set(final int uniform, final GLTexture texture) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], textureBinder.bind(texture));
		return true;
	}

	public interface Validator {
		boolean validate(final DepthShader depthshader, final int inputID, final Renderable renderable);
	}

	public interface Setter {
		void set(final DepthShader shader, final int inputID, final Renderable renderable, final Attributes combinedAttributes);
	}

	public static class Uniform implements Validator {
		public final String alias;
		public final long materialMask;

		public Uniform(final String alias, final long materialMask) {
			this.alias = alias;
			this.materialMask = materialMask;
		}

		public Uniform(final String alias) {
			this(alias, 0);
		}

		@Override
		public boolean validate(final DepthShader depthshader, final int inputID, final Renderable renderable) {
			final long matFlags = (renderable != null && renderable.material != null) ? renderable.material.getMask() : 0;
			return ((matFlags & materialMask) == materialMask);
		}
	}

	public static class Inputs {
		public final static Uniform bones = new Uniform("u_bones");
		public final static Uniform diffuseTexture = new Uniform("u_diffuseTexture", TextureAttribute.Diffuse);
	}

	public static class Setters {
		public final static Setter diffuseTexture = new Setter() {
			@Override
			public void set(DepthShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, combinedAttributes.<TextureAttribute>get(TextureAttribute.Diffuse).texture);
			}
		};

		public static class Bones implements Setter {
			private final static Matrix4 idtMatrix = new Matrix4();
			public final int numBones;
			public final float[] bones;

			public Bones(final int numBones) {
				this.numBones = numBones;
				this.bones = new float[numBones * 16];
			}

			@Override
			public void set(DepthShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				for (int i = 0; i < bones.length; i++) {
					final int idx = i / 16;
					bones[i] = (renderable.bones == null || idx >= renderable.bones.length || renderable.bones[idx] == null) ? idtMatrix.val[i % 16] : renderable.bones[idx].val[i % 16];
				}
				shader.program.setUniformMatrix4fv(shader.loc(inputID), bones, 0, numBones);
			}
		}
	}
}
