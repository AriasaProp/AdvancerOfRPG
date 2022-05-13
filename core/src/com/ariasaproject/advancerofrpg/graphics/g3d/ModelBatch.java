package com.ariasaproject.advancerofrpg.graphics.g3d;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.GLTexture;
import com.ariasaproject.advancerofrpg.graphics.Mesh;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttribute;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.DirectionalLights;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.DirectionalLights.DirectionalLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.PointLights;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.PointLights.PointLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.SpotLights;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.SpotLights.SpotLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.shaders.DepthShader;
import com.ariasaproject.advancerofrpg.graphics.g3d.shaders.ModelShader;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.TextureDescriptor;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.FlushablePool;
import java.util.Comparator;

public class ModelBatch implements Disposable {
	// used for var
	public final Environment environment = new Environment();
	final Array<DepthShader> depthShaders = new Array<DepthShader>();
	final Array<ModelShader> modelShaders = new Array<ModelShader>();
	private final RenderablePool renderablesPool = new RenderablePool();
	private final TextureBinder textureBinder = new TextureBinder();
	private final Array<Renderable> renderables = new Array<Renderable>();
	/*
	 private final Mesh mPrev;
	 private final ShaderProgram programPrev;
	 */
	private final Mesh skyboxesMesh;
	public ModelBatch() {
		/*
		 mPrev = new Mesh(true, true, 4, 6,
		 new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
		 new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
		 mPrev.setVertices(
		 new float[] { -.15f, 0.35f, 0, 1, 0.15f, 0.35f, 1, 1, -0.15f, -0.35f, 0, 0, 0.15f, -0.35f, 1, 0 });
		 mPrev.setIndices(new short[] { 2, 1, 0, 2, 3, 1 });
		 programPrev = new ShaderProgram("\nuniform vec2 u_pivot;" + "\nin vec2 a_position;" + "\nin vec2 a_texCoord;"
		 + "\nout vec2 v_texCoord;" + "\n" + "\nvoid main(){"
		 + "\n    gl_Position = vec4(u_pivot + a_position, 1.0, 1.0);" + "\n    v_texCoord = a_texCoord;" + "\n}"
		 + "\n<break>" + "\nout vec4 gl_FragColor;" + "\n" + "\nuniform sampler2DArray textures;"
		 + "\nuniform int index;" + "\nin vec2 v_texCoord;" + "\n" + "\nvoid main(){"
		 + "\n    gl_FragColor = vec4(texture(textures, vec3(v_texCoord, float(index))).r);" + "\n}" + "\n", "");
		 */
		skyboxesMesh = new Mesh(true, true, 120, 36,
								new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
								new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"));
		skyboxesMesh.setVertices(new float[]{
									 1,1,1, 0,0,//x+
									 1,1,-1, 1,0,//x+
									 1,-1,1, 0,1,//x+
									 1,-1,-1, 1,1,//x+
									 -1,1,-1, 0,0,//x-
									 -1,1,1, 1,0,//x-
									 -1,-1,-1, 0,1,//x-
									 -1,-1,1, 1,1,//x-
									 -1,1,1, 0,0,//z+
									 1,1,1, 1,0,//z+
									 -1,-1,1, 0,1,//z+
									 1,-1,1, 1,1,//z+
									 1,1,-1, 0,0,//z-
									 -1,1,-1, 1,0,//z-
									 1,-1,-1, 0,1,//z-
									 -1,-1,-1, 1,1,//z-
									 -1,1,-1, 0,0,//y+
									 1,1,-1, 1,0,//y+
									 -1,1,1, 0,1,//y+
									 1,1,1, 1,1,//y+
									 -1,-1,1, 0,0,//y-
									 1,-1,1, 1,0,//y-
									 -1,-1,-1, 0,1,//y-
									 1,-1,-1, 1,1,//y-
								 });
		skyboxesMesh.setIndices(new short[]{
									2,1,0, 2,3,1,//x+
									6,5,4, 6,7,5,//x-
									10,9,8, 10,11,9,//z+
									14,13,12, 14,15,13,//z-
									18,17,16, 18,19,17,//y+
									22,21,20, 22,23,21//y-
								});
	}

    private final ShaderProgram skyboxes = new ShaderProgram(GraphFunc.app.getFiles().internal("shader/skybox.shaderprogram"));


	public <T extends RenderableProvider> void render(final Camera camera, final T... renderableProviders) {
		TGF g = GraphFunc.tgf;
		for (final RenderableProvider renderableProvider : renderableProviders)
			renderableProvider.getRenderables(renderables, renderablesPool);

		g.capabilitySwitch(true, TGF.GL_CULL_FACE); // surface that draw in facing back
		g.setCullFace(TGF.GL_BACK);
		g.capabilitySwitch(false, TGF.GL_BLEND);
		g.capabilitySwitch(true, TGF.GL_DEPTH_TEST); // ignore pixel out of depth range
		if (environment.isUseShadowMapping() && (environment.directionalLights.size() + environment.pointLights.size()
			+ environment.spotLights.size() > 0)) {
			environment.prepare();
			g.setDepthMask(true);
			g.glDepthFunc(TGF.GL_LESS);
			DepthShader currentDepthShader = null;
			g.capabilitySwitch(true, TGF.GL_SCISSOR_TEST);
			g.glViewport(-1, -1, environment.getShadow_quality() + 1, environment.getShadow_quality() + 1);
			g.glScissor(0, 0, environment.getShadow_quality(), environment.getShadow_quality());
			DirectionalLights dirLights = environment.directionalLights;
			dirLights.prepare(camera);
			int k = 0;
			for (int i = 0, j = dirLights.size(); i < j; i++) {
				dirLights.setShadowIndex(i, k++);
				g.glBindFramebuffer(TGF.GL_FRAMEBUFFER, environment.fbosBuffer[dirLights.getShadowIndex(i)]);
				g.glClearColorMask(TGF.GL_DEPTH_BUFFER_BIT, 0, 0, 0, 1);
				for (Renderable renderable : renderables) {
					if (currentDepthShader == null || !currentDepthShader.canRender(renderable)) {
						if (currentDepthShader != null) {
							currentDepthShader.end();
							currentDepthShader = null;
						}
						for (DepthShader shader : depthShaders) {
							if (shader.canRender(renderable))
								currentDepthShader = shader;
						}
						if (currentDepthShader == null) {
							currentDepthShader = new DepthShader(renderable);
							depthShaders.add(currentDepthShader);
						}
						currentDepthShader.begin(dirLights.getProjection(i), textureBinder);
					}
					currentDepthShader.render(renderable);
				}

				if (currentDepthShader != null)
					currentDepthShader.end();
			}
			PointLights pLights = environment.pointLights;
			pLights.prepare(environment);
			for (int i = 0, j = pLights.size(); i < j; i++) {
				pLights.get(i).setShadowIndex(k);
				k += 6;
				for (int l = 0; l < 6; l++) {
					g.glBindFramebuffer(TGF.GL_FRAMEBUFFER, environment.fbosBuffer[pLights.get(i).getShadowIndex() + l]);
					g.glClearColorMask(TGF.GL_DEPTH_BUFFER_BIT, 0, 0, 0, 1);
					for (Renderable renderable : renderables) {
						if (currentDepthShader == null || !currentDepthShader.canRender(renderable)) {
							if (currentDepthShader != null) {
								currentDepthShader.end();
								currentDepthShader = null;
							}
							for (DepthShader shader : depthShaders) {
								if (shader.canRender(renderable))
									currentDepthShader = shader;
							}
							if (currentDepthShader == null) {
								currentDepthShader = new DepthShader(renderable);
								depthShaders.add(currentDepthShader);
							}
							currentDepthShader.begin(pLights.get(i).getProjection(l), textureBinder);
						}
						currentDepthShader.render(renderable);
					}
				}

				if (currentDepthShader != null)
					currentDepthShader.end();
			}

			// render a shadow for light mapping
			g.glBindFramebuffer(TGF.GL_FRAMEBUFFER, 0);
			g.glClearColorMask(TGF.GL_DEPTH_BUFFER_BIT, 0, 0, 0, 1);
		}
		// end shadow shading
		if (environment.isChanged) {
			while (modelShaders.notEmpty())
				modelShaders.pop().dispose();
			environment.isChanged = false;
		}
		g.capabilitySwitch(false, TGF.GL_SCISSOR_TEST);
		g.setDepthMask(false);
		// test end
		pos.set(camera.position);
		renderables.sort(sorter);
		g.glViewport(0, 0, GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
		{
			skyboxes.bind();
			final Vector3 sunDir = new Vector3(0, 1, 0);
			if (environment.directionalLights.size() > 0) {
				sunDir.set(environment.directionalLights.get(0).d);
			}
			skyboxes.setUniformf("u_sunDirection", sunDir);
			//skyboxes.setUniformf("u_time", GraphFunc.app.getGraphics().getDeltaTime());
			skyboxesMesh.render(skyboxes, TGF.GL_TRIANGLES);
		}
		ModelShader currentModelShader = null;
		g.glDepthFunc(TGF.GL_LEQUAL);
		for (Renderable renderable : renderables) {
			if (currentModelShader == null || !currentModelShader.canRender(renderable)) {
				if (currentModelShader != null) {
					currentModelShader.end();
					currentModelShader = null;
				}
				for (ModelShader shader : modelShaders)
					if (shader.canRender(renderable))
						currentModelShader = shader;
				if (currentModelShader == null) {
					currentModelShader = new ModelShader(renderable, environment);
					modelShaders.add(currentModelShader);
				}
				currentModelShader.begin(camera, textureBinder, environment);
			}
			currentModelShader.render(renderable, environment);
		}

		if (currentModelShader != null)
			currentModelShader.end();
		textureBinder.end();
		g.capabilitySwitch(false, TGF.GL_BLEND);
		g.capabilitySwitch(false, TGF.GL_CULL_FACE);
		g.capabilitySwitch(false, TGF.GL_DEPTH_TEST);
		// end normal shading
		// end rendering
		renderablesPool.flush();
		renderables.clear();
		/*
		 if (environment.pointLights.size() * 6 + environment.directionalLights.size() > 0) {
		 programPrev.bind();
		 programPrev.setUniformi("textures", textureBinder.bind(TGF.GL_TEXTURE_2D_ARRAY, environment.shadowMapArray));
		 for (int i = 0; i < environment.pointLights.size() * 6 + environment.directionalLights.size(); i++) {
		 programPrev.setUniform2fv("u_pivot",
		 new float[] { -0.85f + (i % 3) * 0.302f, 0.65f - (Math.round(i / 3) * 0.702f) }, 0, 1);
		 programPrev.setUniformi("index", i);
		 mPrev.render(programPrev, TGF.GL_TRIANGLES);
		 }
		 }
		 */
	}

	@Override
	public void dispose() {
		for (DepthShader shader : depthShaders)
			shader.dispose();
		depthShaders.clear();
		for (ModelShader shader : modelShaders)
			shader.dispose();
		modelShaders.clear();
		skyboxes.dispose();
		skyboxesMesh.dispose();
		/*
		 mPrev.dispose();
		 programPrev.dispose();
		 */
	}

	public static class Environment implements Disposable {
		// State for shadowMapping
		private boolean useShadowMapping = true;
		private int shadow_quality = GraphFunc.tgf.getMaxTextureSize() >> 1;// from maximum texture divided by 2 each bit

		public DirectionalLights directionalLights = new DirectionalLights();
		public PointLights pointLights = new PointLights();
		public SpotLights spotLights = new SpotLights();
		public boolean isChanged = false;

		public Environment() {

		}

		private int shadowMapArray = -1;
		private int[] fbosBuffer;

		public void setShadow_quality(int shadow_quality) {
			this.isChanged = true;
			shadow_quality = MathUtils.clamp(shadow_quality, 0, 9);
			this.shadow_quality = GraphFunc.tgf.getMaxTextureSize() >> (9 - shadow_quality);
		}

		public int getShadow_quality() {
			return shadow_quality;
		}

		public void setUseShadowMapping(boolean useShadowMapping) {
			this.isChanged = true;
			this.useShadowMapping = useShadowMapping;
		}

		public boolean isUseShadowMapping() {
			return useShadowMapping;
		}

		private void prepare() {
			final int size = directionalLights.size() + pointLights.size() * 6 + spotLights.size();
			if (size <= 0)
				return;
			TGF g = GraphFunc.tgf;
			if (!useShadowMapping) {
				if (fbosBuffer != null) {
					if (g.glIsFramebuffer(fbosBuffer[0])) {
						g.glDeleteFramebuffers(fbosBuffer.length, fbosBuffer, 0);
					}
					fbosBuffer = null;
				}
				if (shadowMapArray >= 0) {
					if (g.glIsTexture(shadowMapArray)) {
						g.glDeleteTexture(shadowMapArray);
					}
					shadowMapArray = -1;
				}
				return;
			}
			if ((fbosBuffer != null && fbosBuffer.length != size) || isChanged) {
				if (fbosBuffer != null && g.glIsFramebuffer(fbosBuffer[0])) {
					g.glDeleteFramebuffers(fbosBuffer.length, fbosBuffer, 0);
				}
				if (g.glIsTexture(shadowMapArray)) {
					g.glDeleteTexture(shadowMapArray);
					shadowMapArray = -1;
				}
				fbosBuffer = null;
			}
			if (!g.glIsTexture(shadowMapArray) || shadowMapArray < 0) {
				shadowMapArray = g.glGenTexture();
				g.glBindTexture(TGF.GL_TEXTURE_2D_ARRAY, shadowMapArray);
				g.glTexImage3D(TGF.GL_TEXTURE_2D_ARRAY, 0, TGF.GL_DEPTH_COMPONENT32F, shadow_quality, shadow_quality,
							   size, 0, TGF.GL_DEPTH_COMPONENT, TGF.GL_FLOAT, null);
				g.glTexParameteri(TGF.GL_TEXTURE_2D_ARRAY, TGF.GL_TEXTURE_WRAP_S, TGF.GL_CLAMP_TO_EDGE);
				g.glTexParameteri(TGF.GL_TEXTURE_2D_ARRAY, TGF.GL_TEXTURE_WRAP_T, TGF.GL_CLAMP_TO_EDGE);
				g.glTexParameteri(TGF.GL_TEXTURE_2D_ARRAY, TGF.GL_TEXTURE_WRAP_R, TGF.GL_CLAMP_TO_EDGE);
				g.glTexParameteri(TGF.GL_TEXTURE_2D_ARRAY, TGF.GL_TEXTURE_MIN_FILTER, TGF.GL_NEAREST);
				g.glTexParameteri(TGF.GL_TEXTURE_2D_ARRAY, TGF.GL_TEXTURE_MAG_FILTER, TGF.GL_NEAREST);
				g.glBindTexture(TGF.GL_TEXTURE_2D_ARRAY, 0);
			}
			if (fbosBuffer == null || !g.glIsFramebuffer(fbosBuffer[0])) {
				if (fbosBuffer == null) {
					fbosBuffer = new int[size];
				}
				g.glGenFramebuffers(size, fbosBuffer, 0);
				g.glBindTexture(TGF.GL_TEXTURE_2D_ARRAY, shadowMapArray);
				for (int i = 0; i < size; i++) {
					g.glBindFramebuffer(TGF.GL_FRAMEBUFFER, fbosBuffer[i]);
					g.glFramebufferTextureLayer(TGF.GL_FRAMEBUFFER, TGF.GL_DEPTH_ATTACHMENT, shadowMapArray, 0, i);
					g.glDrawBuffers(1, new int[] { TGF.GL_NONE }, 0);
					g.glReadBuffer(TGF.GL_NONE);
				}
				g.glBindTexture(TGF.GL_TEXTURE_2D_ARRAY, 0);
			}
		}

		public int getTexHandler() {
			return shadowMapArray;
		}
		public int addDirectionalLight(Color color, Vector3 dir) {
			add(new DirectionalLight(color, dir));
			return directionalLights.size() - 1;
		}
		public int addPointLight(Color color, Vector3 pos) {
			add(new PointLight(color, pos));
			return pointLights.size() - 1;
		}
		public int addSpotLight(Color c, Vector3 pos, Vector3 d, float n, float f, float cOA, float e) {
			add(new SpotLight(c, pos, d, n, f, cOA, e));
			return spotLights.size() - 1;
		}

		public void add(final BaseLight... lights) {
			for (final BaseLight light : lights)
				add(light);
		}

		public void add(final Iterable<BaseLight> lights) {
			for (final BaseLight light : lights)
				add(light);
		}

		public void add(final BaseLight light) {
			isChanged = true;
			if (light instanceof DirectionalLight) {
				directionalLights.add((DirectionalLight) light);
			} else if (light instanceof PointLight) {
				pointLights.add((PointLight) light);
			} else if (light instanceof SpotLight) {
				spotLights.add((SpotLight) light);
			}
		}

		public void remove(final BaseLight... lights) {
			for (final BaseLight light : lights)
				remove(light);
		}

		public void remove(final Iterable<BaseLight> lights) {
			for (final BaseLight light : lights)
				remove(light);
		}

		public void remove(final BaseLight light) {
			isChanged = true;
			if (light instanceof DirectionalLight) {
				directionalLights.removeValue((DirectionalLight) light);
			} else if (light instanceof PointLight) {
				pointLights.removeValue((PointLight) light);
			} else if (light instanceof SpotLight) {
				spotLights.removeValue((SpotLight) light);
			}
		}

		@Override
		public void dispose() {
			directionalLights.dispose();
			pointLights.dispose();
			spotLights.dispose();
			TGF g = GraphFunc.tgf;
			if (fbosBuffer != null) {
				if (g.glIsFramebuffer(fbosBuffer[0]))
					g.glDeleteFramebuffers(fbosBuffer.length, fbosBuffer, 0);
				fbosBuffer = null;
			}
			if (shadowMapArray >= 0) {
				if (g.glIsTexture(shadowMapArray)) {
					g.glDeleteTexture(shadowMapArray);
				}
				shadowMapArray = -1;
			}
		}
	}

	protected static class RenderablePool extends FlushablePool<Renderable> {
		@Override
		protected Renderable newObject() {
			return new Renderable();
		}

		@Override
		public Renderable obtain() {
			Renderable renderable = super.obtain();
			renderable.material = null;
			renderable.meshPart.set("", null, 0, 0, 0);
			return renderable;
		}
	}

	public static class TextureBinder {
		int size = 0;
		final int[] texTarget, texId;

		public TextureBinder() {
			final int maxUnit = GraphFunc.tgf.getMaxTextureUnit();
			texTarget = new int[maxUnit];
			texId = new int[maxUnit];
		}

		public final int bind(final TextureDescriptor textDesc) {
			textDesc.texture.setWrap(textDesc.uWrap, textDesc.vWrap);
			textDesc.texture.setFilter(textDesc.minFilter, textDesc.magFilter);
			return bind(textDesc.texture);
		}

		public final int bind(final GLTexture texture) {
			return bind(texture.glTarget, texture.getTextureObjectHandle());
		}

		public final int bind(final int target, final int texture) {
			if (size > 0) {
				for (int i = 0; i < size; i++)
					if (texId[i] == texture)
						return i;
			}
			texTarget[size] = target;
			texId[size] = texture;
			TGF g = GraphFunc.tgf;
			g.glActiveTexture(size);
			g.glBindTexture(target, texture);
			size++;
			return size - 1;
		}

		public final void end() {
			TGF g = GraphFunc.tgf;
			while (size > 0) {
				size--;
				g.glActiveTexture(size);
				g.glBindTexture(texTarget[size], texId[size]);
				texTarget[size] = -1;
				texId[size] = -1;
			}
		}
	}

	final Vector3 pos = new Vector3();
	final Comparator<Renderable> sorter = new Comparator<Renderable>() {
		Vector3 hE1 = new Vector3(), hE2 = new Vector3(), loc1 = new Vector3(), loc2 = new Vector3();
		float far1, far2;

		@Override
		public int compare(Renderable p1, Renderable p2) {
			hE1.set(p1.meshPart.halfExtents);
			hE2.set(p2.meshPart.halfExtents);
			far1 = -1;
			far2 = -1;
			Vector3 par = new Vector3();
			for (par.x = -1; par.x <= 1f; par.x += 1) {
				for (par.y = -1; par.y <= 1f; par.y += 1) {
					for (par.z = -1; par.z <= 1f; par.z += 1) {
						loc1.set(p1.meshPart.center).mulAdd(hE1, par).mul(p1.worldTransform);
						loc2.set(p2.meshPart.center).mulAdd(hE2, par).mul(p2.worldTransform);
						far1 = Math.max(far1, loc1.dst2(pos) * 1000);
						far2 = Math.max(far2, loc2.dst2(pos) * 1000);
					}
				}
			}
			return (int) Math.signum(far2 - far1);
		}
	};

	public interface BaseLight {
		public final Color color = new Color(0, 0, 0, 1);
	}
}
