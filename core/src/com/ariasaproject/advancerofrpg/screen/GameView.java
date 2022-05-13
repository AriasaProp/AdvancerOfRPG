package com.ariasaproject.advancerofrpg.screen;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.OrthographicCamera;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.g3d.Material;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelInstance;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.ColorAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.DirectionalLights.DirectionalLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.PointLights.PointLight;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.AnimationController;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.MeshPartBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.ModelBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.CapsuleShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.ariasaproject.advancerofrpg.graphics.g3d.utils.shapebuilders.TerrainShapeBuilder;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Quaternion;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.math.Vector3;
import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.scenes2d.ui.ImageButton;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Skin;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Touchpad;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ClickListener;
import com.ariasaproject.advancerofrpg.ApplicationListener;

public class GameView extends Scene {
	Camera cam;
	ModelBatch modelBatch;
	ModelInstance[] instance;
	Model model;
	AnimationController knightAnim;
	Vector3 moves = new Vector3();
	Vector3 center = new Vector3();
	DirectionalLight dl;
	PointLight pl = new PointLight(new Color(0, 1, 1, 1), new Vector3(15, 25, -15),  7f, 90f);

	public GameView(ApplicationListener appl){
		super(appl);
	}
	@Override
	public void show() {
		cam = new OrthographicCamera(240f, 144f);
		cam.far = 416;
		cam.position.set(-120, 120f, -120);
		cam.lookAt(0, 0, 0);
		cam.update();
		modelBatch = new ModelBatch();
		modelBatch.environment.add(dl = new DirectionalLight(new Color(1, 1, 1, 1), new Vector3(1f, -.5f, -.5f).nor()));
		// modelBatch.environment.add(pl = new PointLight(new Vector3(15, 25, -15), new
		// Color(0, 1, 1, 1), 7f, 90f));
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		modelBuilder.node().id = "Lamp";
		MeshPartBuilder build = modelBuilder.part("Lamp", TGF.GL_TRIANGLES,
												  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
												  new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
		build.setVertexTransform(new Matrix4().translate(15, 25, -15));
		SphereShapeBuilder.build(build, 5, 5, 5, 50, 50);
		modelBuilder.node().id = "Ball";
		build = modelBuilder.part("Ball", TGF.GL_TRIANGLES,
								  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(Color.GREEN)));
		build.setVertexTransform(new Matrix4().translate(00, 20, -20));
		SphereShapeBuilder.build(build, 13, 13, 13, 20, 20);
		modelBuilder.node().id = "Box";
		build = modelBuilder.part("Box", TGF.GL_TRIANGLES,
								  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
		build.setVertexTransform(new Matrix4().translate(30, 17, 10).rotate(new Vector3(1, 1, 0), 40));
		BoxShapeBuilder.build(build, 3, 40, 35);
		modelBuilder.node().id = "Terrain";
		build = modelBuilder.part("Terrain", TGF.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(1, 1, 1, 1)));
		// BoxShapeBuilder.build(build, 300, 10, 300);
		// TerrainShapeBuilder.buildFlat(build, Vector3.Zero, 5000, 5000);
		TerrainShapeBuilder.buildNoise(build, new Vector3(), 5, 60, 60, .05f);
		modelBuilder.node().id = "caps";
		build = modelBuilder.part("caps", TGF.GL_TRIANGLES,
								  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(Color.RED)));
		build.setVertexTransform(new Matrix4().translate(-120, 10, 150));
		CapsuleShapeBuilder.build(build, 5, 50, 20);
		model = modelBuilder.end();
		instance = new ModelInstance[2];
		instance[0] = new ModelInstance(model);
		instance[1] = new ModelInstance(
			ApplicationListener.asset.<Model>get("model/KnightCharacter.g3db"),
			new Matrix4().scale(0.08f));

		knightAnim = new AnimationController(instance[1]);
		knightAnim.allowSameAnimation = false;
		knightAnim.animate("HumanArmature|Idle", -1, null, 0);

		Skin skin = ApplicationListener.asset.get("uiskin/system/systemskin.json");
		final Touchpad touchpad = new Touchpad(skin);

		touchpad.addListener(new InputListener() {
				Vector2 temp2 = new Vector2();

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					temp2.set(x, y);
					changeAnim(true);
					return true;
				}

				@Override
				public void touchDragged(InputEvent event, float x, float y, int pointer) {
					temp2.set(x, y);
					changeAnim(true);
				}

				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
					temp2.set(x, y);
					changeAnim(false);
				}

				boolean bCur = false, bCha = false;

				void changeAnim(boolean changed) {
					bCur = (changed & temp2.len() > MathUtils.FLOAT_ROUNDING_ERROR);
					if (bCur != bCha) {
						bCha = bCur;
						knightAnim.animate(bCha ? "HumanArmature|Run" : "HumanArmature|Idle", -1, null, 0.1f);
					}
				}
			});
		touchpad.addAction(new Action() {
				final Quaternion rot = new Quaternion();
				final Vector3 moves = new Vector3();

				@Override
				public boolean act(float delta) {
					Touchpad t = (Touchpad) getActor();
					if (t.knobPercent.len() > MathUtils.FLOAT_ROUNDING_ERROR) {
						rot.setFromMatrix(cam.combined);
						moves.set(t.knobPercent.x, 0, t.knobPercent.y);
						moves.mul(rot);
						moves.rotateRad(-rot.getPitchRad(), 1, 0, 0);
						moves.rotateRad(-rot.getRollRad(), 0, 0, 1);
						moves.nor();
						moves.scl(delta * 500);
						instance[1].transform.translate(moves);
					}
					return false;
				}
			});
		touchpad.setName("touchpad");
		touchpad.setPosition(0, 0);
		final Stage st = appl.stage;
		st.addActor(touchpad);
		ImageButton ibutton = new ImageButton(skin, "attack");
		ibutton.addListener(new ClickListener() {
				boolean HasL = false;

				@Override
				public void clicked(InputEvent e, float x, float y) {
					if (HasL)
						modelBatch.environment.remove(pl);
					else
						modelBatch.environment.add(pl);
					HasL = !HasL;
				}
			});
		ibutton.setPosition(st.getWidth() - ibutton.getWidth(), 0);
		st.addActor(ibutton);
		ibutton = new ImageButton(skin, "attack");
		ibutton.addListener(new ClickListener() {
				boolean HasL = true;

				@Override
				public void clicked(InputEvent e, float x, float y) {
					if (HasL)
						modelBatch.environment.remove(dl);
					else
						modelBatch.environment.add(dl);
					HasL = !HasL;
				}
			});
		ibutton.setPosition(st.getWidth() - ibutton.getWidth() * 2, 0);
		st.addActor(ibutton);
		ibutton = new ImageButton(skin, "jump");
		ibutton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					modelBatch.environment.setUseShadowMapping(!modelBatch.environment.isUseShadowMapping());
				}
			});
		ibutton.setPosition(st.getWidth() - ibutton.getWidth(), st.getHeight() - ibutton.getHeight());
		st.addActor(ibutton);
		GraphFunc.app.getInput().setInputProcessor(st);
	}

	@Override
	public void render(float delta) {
		appl.stage.act(delta);
		knightAnim.update(delta);
		modelBatch.render(cam, instance);
		appl.stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		modelBatch.dispose();
		instance = null;
		model.dispose();
	}
}
