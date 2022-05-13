package com.ariasaproject.advancerofrpg.graphics.g3d.shaders;

import java.util.Arrays;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.GLTexture;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttribute;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.g3d.Attributes;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.BaseLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.Environment;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch.TextureBinder;
import com.ariasaproject.advancerofrpg.graphics.g3d.Renderable;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.BlendingAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.ColorAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.CubemapAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.FloatAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.TextureAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.AmbientCubemap;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.DirectionalLights;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.PointLights;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.SpotLights;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.TextureDescriptor;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.math.Matrix3;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.IntArray;
import com.ariasaproject.advancerofrpg.utils.IntIntMap;

public class ModelShader implements Disposable {
	private final Array<String> uniforms = new Array<String>();
	private final Array<Validator> validators = new Array<Validator>();
	private final Array<Setter> setters = new Array<Setter>();
	private final IntArray globalUniforms = new IntArray();
	private final IntArray localUniforms = new IntArray();
	private final IntIntMap attributes = new IntIntMap();
	private final IntArray tempArray = new IntArray();
	private final Attributes combinedAttributes = new Attributes();
	public ShaderProgram program;
	public TextureBinder textureBinder;
	public Camera camera;
	private final int[] locations;
	private Mesh currentMesh;

	private static String defaultShaderProgram = null;

	public final int u_cameraPosition;
	// Object uniforms
	public final int u_normalMatrix;
	public final int u_bones;
	// Material uniforms
	public final int u_shininess;
	public final int u_diffuseColor;
	public final int u_diffuseTexture;
	public final int u_diffuseUVTransform;
	public final int u_specularColor;
	public final int u_specularTexture;
	public final int u_specularUVTransform;
	public final int u_emissiveColor;
	public final int u_emissiveTexture;
	public final int u_emissiveUVTransform;
	public final int u_reflectionColor;
	public final int u_reflectionTexture;
	public final int u_reflectionUVTransform;
	public final int u_normalTexture;
	public final int u_normalUVTransform;
	public final int u_ambientTexture;
	public final int u_ambientUVTransform;
	// another uniform
	public final int numBones;
	// Lighting uniforms
	protected final int u_ambientCubemap;
	protected final int u_environmentCubemap;

	protected final AmbientCubemap ambientCubemap = new AmbientCubemap();
	protected final int directionalLightsNum, pointLightsNum, spotLightsNum;
	protected final float[] dirLightTemp, pointLightTemp, spotLightTemp;
	protected final float[] cacheDirsTemp, cachePointTemp, cacheSpotTemp;

	protected final long attributesMask;
	protected final long vertexMask;
	private Environment environment;

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

	public ModelShader(final Renderable renderable, final Environment environment) {
		this.environment = (environment != null) ? environment : new Environment();
		combinedAttributes.clear();
		if (renderable.material != null)
			combinedAttributes.set(renderable.material);
		if (defaultShaderProgram == null)
			defaultShaderProgram = GraphFunc.app.getFiles().internal("shader/model.shaderprogram").readString();

		directionalLightsNum = environment.directionalLights.size();
		dirLightTemp = new float[directionalLightsNum * 23];
		cacheDirsTemp = new float[directionalLightsNum * 23];
		pointLightsNum = environment.pointLights.size();
		pointLightTemp = new float[pointLightsNum * 9];
		cachePointTemp = new float[pointLightsNum * 9];
		spotLightsNum = environment.spotLights.size();
		spotLightTemp = new float[spotLightsNum * 12];
		cacheSpotTemp = new float[spotLightsNum * 12];
		// create prefix based attribute local, global etc.
		// prepared with light uniform to
		String prefix = "";
		this.attributesMask = combinedAttributes.getMask();
		final VertexAttributes attrs = renderable.meshPart.mesh.getVertexAttributes();
		this.vertexMask = attrs.getMaskWithSizePacked();
		if ((vertexMask & Usage.Position) != Usage.Position)
			throw new RuntimeException("renderable has not position vertex attribute? why?");
		if ((vertexMask & (Usage.ColorUnpacked | Usage.ColorPacked)) != 0)
			prefix += "#define colorFlag\n";
		if ((vertexMask & Usage.BiNormal) == Usage.BiNormal)
			prefix += "#define binormalFlag\n";
		if ((vertexMask & Usage.Tangent) == Usage.Tangent)
			prefix += "#define tangentFlag\n";
		if ((vertexMask & Usage.Normal) == Usage.Normal)
			prefix += "#define normalFlag\n";
		if (((vertexMask & Usage.Normal) == Usage.Normal) || ((vertexMask & (Usage.Tangent | Usage.BiNormal)) != 0)) {
			prefix += "#define numDirectionalLights " + directionalLightsNum + "\n";
			prefix += "#define numPointLights " + pointLightsNum + "\n";
			prefix += "#define numSpotLights " + spotLightsNum + "\n";
			prefix += "#define ambientCubemapFlag\n";
			if (environment.isUseShadowMapping())
				prefix += "#define shadowPCFOffset " + (1.0f / environment.getShadow_quality()) + "\n";
			if (combinedAttributes.has(ColorAttribute.Fog))
				prefix += "#define fogFlag\n";
			if (combinedAttributes.has(CubemapAttribute.EnvironmentMap))
				prefix += "#define environmentCubemapFlag\n";
		}
		final int n = renderable.meshPart.mesh.getVertexAttributes().size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = renderable.meshPart.mesh.getVertexAttributes().get(i);
			if (attr.usage == Usage.BoneWeight)
				prefix += "#define boneWeight" + attr.unit + "Flag\n";
			else if (attr.usage == Usage.TextureCoordinates)
				prefix += "#define texCoord" + attr.unit + "Flag\n";
		}
		if ((attributesMask & BlendingAttribute.Type) == BlendingAttribute.Type)
			prefix += "#define " + BlendingAttribute.Alias + "Flag\n";
		if ((attributesMask & TextureAttribute.Diffuse) == TextureAttribute.Diffuse) {
			prefix += "#define " + TextureAttribute.DiffuseAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.DiffuseAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Specular) == TextureAttribute.Specular) {
			prefix += "#define " + TextureAttribute.SpecularAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.SpecularAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Normal) == TextureAttribute.Normal) {
			prefix += "#define " + TextureAttribute.NormalAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.NormalAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Emissive) == TextureAttribute.Emissive) {
			prefix += "#define " + TextureAttribute.EmissiveAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.EmissiveAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Reflection) == TextureAttribute.Reflection) {
			prefix += "#define " + TextureAttribute.ReflectionAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.ReflectionAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & TextureAttribute.Ambient) == TextureAttribute.Ambient) {
			prefix += "#define " + TextureAttribute.AmbientAlias + "Flag\n";
			prefix += "#define " + TextureAttribute.AmbientAlias + "Coord texCoord0\n"; // FIXME implement UV mapping
		}
		if ((attributesMask & ColorAttribute.Diffuse) == ColorAttribute.Diffuse) {
			prefix += "#define " + ColorAttribute.DiffuseAlias + "Flag\n";
		}
		if ((attributesMask & ColorAttribute.Specular) == ColorAttribute.Specular) {
			prefix += "#define " + ColorAttribute.SpecularAlias + "Flag\n";
		}
		if ((attributesMask & ColorAttribute.Emissive) == ColorAttribute.Emissive) {
			prefix += "#define " + ColorAttribute.EmissiveAlias + "Flag\n";
		}
		if ((attributesMask & ColorAttribute.Reflection) == ColorAttribute.Reflection) {
			prefix += "#define " + ColorAttribute.ReflectionAlias + "Flag\n";
		}
		if ((attributesMask & FloatAttribute.Shininess) == FloatAttribute.Shininess) {
			prefix += "#define " + FloatAttribute.ShininessAlias + "Flag\n";
		}
		this.numBones = renderable.bones == null ? 0 : 12;
		if (this.numBones > 0)
			prefix += "#define numBones " + this.numBones + "\n";

		this.program = new ShaderProgram(defaultShaderProgram, prefix);

		u_cameraPosition = register(Inputs.cameraPosition, Setters.cameraPosition);
		// Object uniforms
		u_normalMatrix = register(Inputs.normalMatrix, Setters.normalMatrix);
		u_bones = (renderable.bones != null && numBones > 0) ? register(Inputs.bones, new Setters.Bones(numBones)) : -1;
		u_shininess = register(Inputs.shininess, Setters.shininess);
		u_diffuseColor = register(Inputs.diffuseColor, Setters.diffuseColor);
		u_diffuseTexture = register(Inputs.diffuseTexture, Setters.diffuseTexture);
		u_diffuseUVTransform = register(Inputs.diffuseUVTransform, Setters.diffuseUVTransform);
		u_specularColor = register(Inputs.specularColor, Setters.specularColor);
		u_specularTexture = register(Inputs.specularTexture, Setters.specularTexture);
		u_specularUVTransform = register(Inputs.specularUVTransform, Setters.specularUVTransform);
		u_emissiveColor = register(Inputs.emissiveColor, Setters.emissiveColor);
		u_emissiveTexture = register(Inputs.emissiveTexture, Setters.emissiveTexture);
		u_emissiveUVTransform = register(Inputs.emissiveUVTransform, Setters.emissiveUVTransform);
		u_reflectionColor = register(Inputs.reflectionColor, Setters.reflectionColor);
		u_reflectionTexture = register(Inputs.reflectionTexture, Setters.reflectionTexture);
		u_reflectionUVTransform = register(Inputs.reflectionUVTransform, Setters.reflectionUVTransform);
		u_normalTexture = register(Inputs.normalTexture, Setters.normalTexture);
		u_normalUVTransform = register(Inputs.normalUVTransform, Setters.normalUVTransform);
		u_ambientTexture = register(Inputs.ambientTexture, Setters.ambientTexture);
		u_ambientUVTransform = register(Inputs.ambientUVTransform, Setters.ambientUVTransform);
		u_ambientCubemap = register(Inputs.ambientCube, new Setters.ACubemap(directionalLightsNum, pointLightsNum));
		u_environmentCubemap = combinedAttributes.has(CubemapAttribute.EnvironmentMap)
			? register(Inputs.environmentCubemap, Setters.environmentCubemap)
			: -1;
		locations = new int[uniforms.size];
		for (int i = 0; i < uniforms.size; i++) {
			final String input = uniforms.get(i);
			final Validator validator = validators.get(i);
			final Setter setter = setters.get(i);
			if (validator != null && !validator.validate(this, i, renderable))
				locations[i] = -1;
			else {
				locations[i] = program.fetchUniformLocation(input, false);
				if (locations[i] >= 0 && setter != null) {
					if (setter.isGlobal(this, i))
						globalUniforms.add(i);
					else
						localUniforms.add(i);
				}
			}
			if (locations[i] < 0) {
				validators.set(i, null);
				setters.set(i, null);
			}
		}
		for (final VertexAttribute attr : attrs) {
			final int location = program.getAttributeLocation(attr.alias);
			if (location >= 0)
				attributes.put(attr.getKey(), location);
		}

	}

	public boolean canRender(final Renderable renderable) {
		long renderableMask = 0;
		if (renderable.material != null)
			renderableMask |= renderable.material.getMask();
		if (attributesMask != renderableMask)
			return false;
		return (vertexMask == renderable.meshPart.mesh.getVertexAttributes().getMaskWithSizePacked());
	}

	public int compareTo(ModelShader other) {
		if (other == null)
			return -1;
		if (other == this)
			return 0;
		return 0; // FIXME compare shaders on their impact on performance
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof ModelShader))
			return false;
		ModelShader o = (ModelShader) obj;
		return o == this;
	}

	public void begin(final Camera camera, final TextureBinder textureBinder, final Environment environment) {
		this.textureBinder = textureBinder;
		this.camera = camera;
		currentMesh = null;
		program.bind();
		program.setUniformMatrix("u_projViewTrans", camera.combined);

		for (int u, i = 0; i < globalUniforms.size; ++i)
			if (setters.get(u = globalUniforms.get(i)) != null)
				setters.get(u).set(this, u, null, null);
		if (directionalLightsNum + pointLightsNum + spotLightsNum > 0) {
			final boolean shadowMapping = environment.isUseShadowMapping();
			final IntArray sInd = new IntArray();
			int i, j = directionalLightsNum, k;
			if (j > 0) {
				final DirectionalLights dirs = environment.directionalLights;
				k = 0;
				for (i = 0; i < j; i++) {
					;
					dirs.get(i);
					final Color c = BaseLight.color;
					dirLightTemp[k++] = c.r;
					dirLightTemp[k++] = c.g;
					dirLightTemp[k++] = c.b;
					dirLightTemp[k++] = c.a;
				}
				for (i = 0; i < j; i++) {
					dirLightTemp[k++] = dirs.get(i).d.x;
					dirLightTemp[k++] = dirs.get(i).d.y;
					dirLightTemp[k++] = dirs.get(i).d.z;
				}
				if (shadowMapping) {
					sInd.clear();
					for (i = 0; i < j; i++, k += 16) {
						System.arraycopy(dirs.getProjection(i), 0, dirLightTemp, k, 16);
						sInd.add(environment.directionalLights.getShadowIndex(i));
					}
				}
				if (!Arrays.equals(cacheDirsTemp, dirLightTemp)) {
					System.arraycopy(dirLightTemp, 0, cacheDirsTemp, 0, cacheDirsTemp.length);
					program.setUniform4fv("u_dirLights_color", dirLightTemp, 0, j);
					program.setUniform3fv("u_dirLights_direction", dirLightTemp, j * 4, j);
					if (shadowMapping) {
						program.setUniformMatrix4fv("u_dirLights_projection", dirLightTemp, j * 7, j);
					}
				}

				if (shadowMapping) {
					program.setUniformiv("u_dirLights_shadowIndex", sInd.toArray(), 0, j);
				}
			}
			j = pointLightsNum;
			if (j > 0) {
				k = 0;
				final PointLights point = environment.pointLights;
				for (i = 0; i < j; i++) {
					;
					point.get(i);
					final Color c = BaseLight.color;
					pointLightTemp[k++] = c.r;
					pointLightTemp[k++] = c.g;
					pointLightTemp[k++] = c.b;
					pointLightTemp[k++] = c.a;
				}
				for (i = 0; i < j; i++) {
					pointLightTemp[k++] = point.get(i).position.x;
					pointLightTemp[k++] = point.get(i).position.y;
					pointLightTemp[k++] = point.get(i).position.z;
				}
				for (i = 0; i < j; i++) {
					pointLightTemp[k++] = point.get(i).near;
					pointLightTemp[k++] = point.get(i).far;
				}
				if (shadowMapping) {
					sInd.clear();
					for (i = 0; i < j; i++) {
						sInd.add(point.get(i).getShadowIndex());
					}
				}
				if (!Arrays.equals(cachePointTemp, pointLightTemp)) {
					System.arraycopy(pointLightTemp, 0, cachePointTemp, 0, cachePointTemp.length);
					program.setUniform4fv("u_pointLights_color", pointLightTemp, 0, j);
					program.setUniform3fv("u_pointLights_position", pointLightTemp, j * 4, j);
					program.setUniform2fv("u_pointLights_nearFar", pointLightTemp, j * 7, j);
				}
				if (shadowMapping) {
					program.setUniformiv("u_pointLights_shadowIndex", sInd.toArray(), 0, j);
				}
			}
			j = spotLightsNum;
			if (j > 0) {
				k = 0;
				final SpotLights spot = environment.spotLights;
				for (i = 0; i < j; i++) {
					spot.get(i);
					final Color c = BaseLight.color;
					spotLightTemp[k++] = c.r;
					spotLightTemp[k++] = c.g;
					spotLightTemp[k++] = c.b;
					spotLightTemp[k++] = c.a;
				}
				for (i = 0; i < j; i++) {
					spotLightTemp[k++] = spot.get(i).pos.x;
					spotLightTemp[k++] = spot.get(i).pos.y;
					spotLightTemp[k++] = spot.get(i).pos.z;
				}
				for (i = 0; i < j; i++) {
					spotLightTemp[k++] = spot.get(i).d.x;
					spotLightTemp[k++] = spot.get(i).d.y;
					spotLightTemp[k++] = spot.get(i).d.z;
				}
				for (i = 0; i < j; i++) {
					spotLightTemp[k++] = spot.get(i).cutoffAngle;
					spotLightTemp[k++] = spot.get(i).exponent;
					spotLightTemp[k++] = spot.get(i).near;
					spotLightTemp[k++] = spot.get(i).far;
				}

				if (shadowMapping) {
					sInd.clear();
					for (i = 0; i < j; i++) {
						sInd.add(-1);
					}
				}
				if (!Arrays.equals(cacheSpotTemp, spotLightTemp)) {
					System.arraycopy(spotLightTemp, 0, cacheSpotTemp, 0, cacheSpotTemp.length);
					program.setUniform4fv("u_spotLights_color", spotLightTemp, 0, j);
					program.setUniform3fv("u_spotLights_position", spotLightTemp, j * 4, j);
					program.setUniform3fv("u_spotLights_direction", spotLightTemp, j * 7, j);
					// for cuttoffAngle, exponent, near, far
					program.setUniform4fv("u_spotLights_cAENF", spotLightTemp, j * 10, j);
				}

				if (shadowMapping) {
					program.setUniformiv("u_spotLights_shadowIndex", sInd.toArray(), 0, j);
				}
			}

			if (shadowMapping) {
				if (environment.getTexHandler() >= 0)
					program.setUniformi("u_shadowMaps",
										textureBinder.bind(TGF.GL_TEXTURE_2D_ARRAY, environment.getTexHandler()));
			}
		}
	}

	public void render(Renderable renderable, final Environment environment) {
		if (renderable.worldTransform.det3x3() == 0)
			return;
		combinedAttributes.clear();
		if (renderable.material != null)
			combinedAttributes.set(renderable.material);
		final TGF g = GraphFunc.tgf;

		int blendSrc = TGF.GL_SRC_ALPHA, blendDest = TGF.GL_ONE_MINUS_SRC_ALPHA;
		boolean blend = false;
		if (blend = combinedAttributes.has(BlendingAttribute.Type)) {
			BlendingAttribute attr = combinedAttributes.<BlendingAttribute>get(BlendingAttribute.Type);
			blendSrc = attr.sourceFunction;
			blendDest = attr.destFunction;
			program.setUniformf("u_opacity", attr.opacity);
		}
		g.setBlending(blend, blendSrc, blendDest);
		if (combinedAttributes.has(ColorAttribute.Fog)) {
			program.setUniformf("u_fogColor", ((ColorAttribute) combinedAttributes.get(ColorAttribute.Fog)).color);
		}

		program.setUniformMatrix("u_worldTrans", renderable.worldTransform);

		for (int u, i = 0; i < localUniforms.size; ++i)
			if (setters.get(u = localUniforms.get(i)) != null)
				setters.get(u).set(this, u, renderable, combinedAttributes);
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

	public void end() {
		if (currentMesh != null) {
			currentMesh.unbind();
			currentMesh = null;
		}
	}

	@Override
	public void dispose() {
		program.dispose();
		program = null;
		uniforms.clear();
		validators.clear();
		setters.clear();
		localUniforms.clear();
		globalUniforms.clear();
	}

	public final int loc(final int inputID) {
		return (inputID >= 0 && inputID < locations.length) ? locations[inputID] : -1;
	}

	public final boolean set(final int uniform, final Matrix4 value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformMatrix(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final Matrix3 value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformMatrix(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final Vector3 value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final Vector2 value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final Color value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final float value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final float v1, final float v2) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], v1, v2);
		return true;
	}

	public final boolean set(final int uniform, final float v1, final float v2, final float v3) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], v1, v2, v3);
		return true;
	}

	public final boolean set(final int uniform, final float v1, final float v2, final float v3, final float v4) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformf(locations[uniform], v1, v2, v3, v4);
		return true;
	}

	public final boolean set(final int uniform, final int value) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], value);
		return true;
	}

	public final boolean set(final int uniform, final int v1, final int v2) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], v1, v2);
		return true;
	}

	public final boolean set(final int uniform, final int v1, final int v2, final int v3) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], v1, v2, v3);
		return true;
	}

	public final boolean set(final int uniform, final int v1, final int v2, final int v3, final int v4) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], v1, v2, v3, v4);
		return true;
	}

	public final boolean set(final int uniform, final TextureDescriptor textureDesc) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], textureBinder.bind(textureDesc));
		return true;
	}

	public final boolean set(final int uniform, final GLTexture texture) {
		if (locations[uniform] < 0)
			return false;
		program.setUniformi(locations[uniform], textureBinder.bind(texture));
		return true;
	}

	public interface Validator {
		boolean validate(final ModelShader shader, final int inputID, final Renderable renderable);
	}

	public interface Setter {
		boolean isGlobal(final ModelShader shader, final int inputID);

		void set(final ModelShader shader, final int inputID, final Renderable renderable,
				 final Attributes combinedAttributes);
	}

	public abstract static class GlobalSetter implements Setter {
		@Override
		public boolean isGlobal(final ModelShader shader, final int inputID) {
			return true;
		}
	}

	public abstract static class LocalSetter implements Setter {
		@Override
		public boolean isGlobal(final ModelShader shader, final int inputID) {
			return false;
		}
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
		public boolean validate(final ModelShader shader, final int inputID, final Renderable renderable) {
			final long matFlags = (renderable != null && renderable.material != null) ? renderable.material.getMask()
				: 0;
			return ((matFlags & materialMask) == materialMask);
		}
	}

	public static class Inputs {
		public final static Uniform cameraPosition = new Uniform("u_cameraPosition");

		public final static Uniform normalMatrix = new Uniform("u_normalMatrix");
		public final static Uniform bones = new Uniform("u_bones");

		public final static Uniform shininess = new Uniform("u_shininess", FloatAttribute.Shininess);
		public final static Uniform diffuseColor = new Uniform("u_diffuseColor", ColorAttribute.Diffuse);
		public final static Uniform diffuseTexture = new Uniform("u_diffuseTexture", TextureAttribute.Diffuse);
		public final static Uniform diffuseUVTransform = new Uniform("u_diffuseUVTransform", TextureAttribute.Diffuse);
		public final static Uniform specularColor = new Uniform("u_specularColor", ColorAttribute.Specular);
		public final static Uniform specularTexture = new Uniform("u_specularTexture", TextureAttribute.Specular);
		public final static Uniform specularUVTransform = new Uniform("u_specularUVTransform",
																	  TextureAttribute.Specular);
		public final static Uniform emissiveColor = new Uniform("u_emissiveColor", ColorAttribute.Emissive);
		public final static Uniform emissiveTexture = new Uniform("u_emissiveTexture", TextureAttribute.Emissive);
		public final static Uniform emissiveUVTransform = new Uniform("u_emissiveUVTransform",
																	  TextureAttribute.Emissive);
		public final static Uniform reflectionColor = new Uniform("u_reflectionColor", ColorAttribute.Reflection);
		public final static Uniform reflectionTexture = new Uniform("u_reflectionTexture", TextureAttribute.Reflection);
		public final static Uniform reflectionUVTransform = new Uniform("u_reflectionUVTransform",
																		TextureAttribute.Reflection);
		public final static Uniform normalTexture = new Uniform("u_normalTexture", TextureAttribute.Normal);
		public final static Uniform normalUVTransform = new Uniform("u_normalUVTransform", TextureAttribute.Normal);
		public final static Uniform ambientTexture = new Uniform("u_ambientTexture", TextureAttribute.Ambient);
		public final static Uniform ambientUVTransform = new Uniform("u_ambientUVTransform", TextureAttribute.Ambient);

		public final static Uniform ambientCube = new Uniform("u_ambientCubemap");
		public final static Uniform environmentCubemap = new Uniform("u_environmentCubemap");
	}

	public static class Setters {
		public final static Setter cameraPosition = new GlobalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, shader.camera.position.x, shader.camera.position.y, shader.camera.position.z,
						   1.1881f / (shader.camera.far * shader.camera.far));
			}
		};
		public final static Setter normalMatrix = new LocalSetter() {
			private final Matrix3 tmpM = new Matrix3();

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, tmpM.set(renderable.worldTransform).inv().transpose());
			}
		};
		public final static Setter shininess = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((FloatAttribute) (combinedAttributes.get(FloatAttribute.Shininess))).value);
			}
		};
		public final static Setter diffuseColor = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((ColorAttribute) (combinedAttributes.get(ColorAttribute.Diffuse))).color);
			}
		};
		public final static Setter diffuseTexture = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.textureBinder.bind(
					((TextureAttribute) (combinedAttributes.get(TextureAttribute.Diffuse))).texture);
				shader.set(inputID, unit);
			}
		};
		public final static Setter diffuseUVTransform = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = combinedAttributes.get(TextureAttribute.Diffuse);
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter specularColor = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, ((ColorAttribute) (combinedAttributes.get(ColorAttribute.Specular))).color);
			}
		};
		public final static Setter specularTexture = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.textureBinder.bind(
					((TextureAttribute) (combinedAttributes.get(TextureAttribute.Specular))).texture);
				shader.set(inputID, unit);
			}
		};
		public final static Setter specularUVTransform = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = combinedAttributes.get(TextureAttribute.Specular);
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter emissiveColor = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, combinedAttributes.<ColorAttribute>get(ColorAttribute.Emissive).color);
			}
		};
		public final static Setter emissiveTexture = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.textureBinder
					.bind(combinedAttributes.<TextureAttribute>get(TextureAttribute.Emissive).texture);
				shader.set(inputID, unit);
			}
		};
		public final static Setter emissiveUVTransform = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = combinedAttributes.get(TextureAttribute.Emissive);
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter reflectionColor = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				shader.set(inputID, combinedAttributes.<ColorAttribute>get(ColorAttribute.Reflection).color);
			}
		};
		public final static Setter reflectionTexture = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.textureBinder
					.bind(combinedAttributes.<TextureAttribute>get(TextureAttribute.Reflection).texture);
				shader.set(inputID, unit);
			}
		};
		public final static Setter reflectionUVTransform = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = combinedAttributes.get(TextureAttribute.Reflection);
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter normalTexture = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.textureBinder
					.bind(combinedAttributes.<TextureAttribute>get(TextureAttribute.Normal).texture);
				shader.set(inputID, unit);
			}
		};
		public final static Setter normalUVTransform = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = combinedAttributes.get(TextureAttribute.Normal);
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter ambientTexture = new LocalSetter() {
			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final int unit = shader.textureBinder.bind(
					((TextureAttribute) (combinedAttributes.get(TextureAttribute.Ambient))).texture);
				shader.set(inputID, unit);
			}
		};
		public final static Setter ambientUVTransform = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				final TextureAttribute ta = combinedAttributes.get(TextureAttribute.Ambient);
				shader.set(inputID, ta.offsetU, ta.offsetV, ta.scaleU, ta.scaleV);
			}
		};
		public final static Setter environmentCubemap = new LocalSetter() {

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				if (combinedAttributes.has(CubemapAttribute.EnvironmentMap)) {
					shader.set(inputID, shader.textureBinder.bind(combinedAttributes.<CubemapAttribute>get(CubemapAttribute.EnvironmentMap).texture));
				}
			}
		};

		public static class Bones extends LocalSetter {
			private final static Matrix4 idtMatrix = new Matrix4();
			public final int numBones;
			public final float[] bones;

			public Bones(final int numBones) {
				this.numBones = numBones;
				this.bones = new float[numBones * 16];
			}

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				for (int i = 0; i < bones.length; i++) {
					final int idx = i / 16;
					bones[i] = (renderable.bones == null || idx >= renderable.bones.length
						|| renderable.bones[idx] == null) ? idtMatrix.val[i % 16]
						: renderable.bones[idx].val[i % 16];
				}
				shader.program.setUniformMatrix4fv(shader.loc(inputID), bones, 0, numBones);
			}
		}

		public static class ACubemap extends LocalSetter {
			private final static float[] ones = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			private final static Vector3 tmpV1 = new Vector3();
			public final int dirLightsOffset;
			public final int pointLightsOffset;
			private final AmbientCubemap cacheAmbientCubemap = new AmbientCubemap();

			public ACubemap(final int dirLightsOffset, final int pointLightsOffset) {
				this.dirLightsOffset = dirLightsOffset;
				this.pointLightsOffset = pointLightsOffset;
			}

			@Override
			public void set(ModelShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
				if (shader.environment == null)
					shader.program.setUniform3fv(shader.loc(inputID), ones, 0, 6);
				else {
					renderable.worldTransform.getTranslation(tmpV1);
					if (combinedAttributes.has(ColorAttribute.AmbientLight))
						cacheAmbientCubemap
							.set((combinedAttributes.<ColorAttribute>get(ColorAttribute.AmbientLight)).color);
					DirectionalLights dirLights = shader.environment.directionalLights;
					for (int i = dirLightsOffset; i < dirLights.size(); i++) {
						;
						dirLights.get(i);
						cacheAmbientCubemap.add(BaseLight.color, dirLights.get(i).d);
					}
					PointLights pLights = shader.environment.pointLights;
					for (int i = pointLightsOffset; i < pLights.size(); i++) {
						;
						pLights.get(i);
						cacheAmbientCubemap.add(BaseLight.color, pLights.get(i).position, tmpV1, pLights.get(i).far);
					}
					cacheAmbientCubemap.clamp();
					shader.program.setUniform3fv(shader.loc(inputID), cacheAmbientCubemap.data, 0, 6);
				}
			}
		}
	}
}
