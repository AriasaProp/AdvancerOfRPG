package com.ariasaproject.advancerofrpg.screen;

import com.ariasaproject.advancerofrpg.ApplicationListener;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.graphics.Camera;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.PerspectiveCamera;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.VertexAttributes.Usage;
import com.ariasaproject.advancerofrpg.graphics.g3d.Material;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelBatch;
import com.ariasaproject.advancerofrpg.graphics.g3d.ModelInstance;
import com.ariasaproject.advancerofrpg.graphics.g3d.attributes.ColorAttribute;
import com.ariasaproject.advancerofrpg.graphics.g3d.environment.DirectionalLights.DirectionalLight;
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
import com.ariasaproject.advancerofrpg.math.collision.BoundingBox;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.scenes2d.actions.Actions;
import com.ariasaproject.advancerofrpg.scenes2d.ui.ImageButton;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Skin;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Touchpad;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ClickListener;

public class WorldView extends Scene {
	Camera camera;
	ModelBatch modelBatch;
	ModelInstance[] instance;
	Model model;
	AnimationController knightAnim;
	DirectionalLight d1;
	//PointLight p1;


	public WorldView(ApplicationListener appl){
		super(appl);
	}
	@Override
	public void show() {
		camera = new PerspectiveCamera(60, 240f, 144f);
		camera.near = 0.01f;
		camera.far = 347;
		camera.position.set(-100, 100f, -100);
		camera.lookAt(0, 0, 0);
		camera.update();
		modelBatch = new ModelBatch();
		modelBatch.environment.add(d1 = new DirectionalLight(new Color(1, 1, 1, 1), new Vector3(1f, -.5f, -.5f).nor()));
		// modelBatch.environment.add(d2 = new DirectionalLight(new Color(1, 1, 1, 1),
		// new Vector3(1f, -.5f, .5f).nor()));
		//modelBatch.environment.add(p1 = new PointLight(new Vector3(15, 35, -15), new Color(0, 1, 1, 1), 7f, 250f));
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		modelBuilder.node().id = "Lamp";
		MeshPartBuilder build = modelBuilder.part("Lamp", TGF.GL_TRIANGLES,
												  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
												  new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
		build.setVertexTransform(new Matrix4(new Vector3(15, 35, -15), new Quaternion()));
		SphereShapeBuilder.build(build, 5, 5, 5, 50, 50);
		modelBuilder.node().id = "Ball";
		build = modelBuilder.part("Ball", TGF.GL_TRIANGLES,
								  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(Color.GREEN)));
		build.setVertexTransform(new Matrix4(new Vector3(00, 20, -20), new Quaternion()));
		SphereShapeBuilder.build(build, 13, 13, 13, 20, 20);
		modelBuilder.node().id = "Box";
		build = modelBuilder.part("Box", TGF.GL_TRIANGLES,
								  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(Color.YELLOW)));
		build.setVertexTransform(new Matrix4(new Vector3(55, 30, -15), new Quaternion(new Vector3(1,1,0), 40)));
		BoxShapeBuilder.build(build, 3, 55, 50);
		modelBuilder.node().id = "Terrain";
		build = modelBuilder.part("Terrain", TGF.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(0.2f, 0.2f, 0.55f, 1)));
		// BoxShapeBuilder.build(build, 300, 10, 300);
		TerrainShapeBuilder.buildFlat(build, new Vector3(), 5000, 5000);
		// TerrainShapeBuilder.buildNoise(build, Vector3.Zero, 5, 5, new
		// GridPoint2(60,60), 0.1f);
		modelBuilder.node().id = "caps";
		build = modelBuilder.part("caps", TGF.GL_TRIANGLES,
								  Usage.Position | Usage.Normal | Usage.ColorPacked | Usage.TextureCoordinates,
								  new Material(ColorAttribute.createDiffuse(Color.RED)));
		build.setVertexTransform(new Matrix4(new Vector3(-50, 25, 50), new Quaternion()));
		CapsuleShapeBuilder.build(build, 5, 50, 20);
		model = modelBuilder.end();
		instance = new ModelInstance[2];
		instance[0] = new ModelInstance(model);
		instance[1] = new ModelInstance(
			ApplicationListener.asset.<Model>get("model/KnightCharacter.g3db"),
			new Matrix4().scl(0.08f)
		);
		/*
		 for(Material m : A5.materials){
		 GraphFunc.app.log(ClassReflection.getSimpleName(getClass()), m.id);
		 }*/
		knightAnim = new AnimationController(instance[1]);
		knightAnim.allowSameAnimation = false;
		knightAnim.animate("HumanArmature|Idle", -1, null, 0);

		final Stage st = appl.stage;
		st.addAction(Actions.fadeIn(0.1f));
		final float stWidth = st.getWidth(), stHeight = st.getHeight();
		final Skin skin = ApplicationListener.asset.<Skin>get("uiskin/system/systemskin.json");

		Actor camPad = new Actor();
		camPad.addListener(new InputListener() {
				Vector3 tmpV1 = new Vector3(), tmpV2 = new Vector3(), t = new Vector3();
				BoundingBox box = new BoundingBox();
				Matrix4 tmpInv = new Matrix4();

				Vector2 tmp2 = new Vector2();

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
					tmp2.set(x, y);
					return true;
				}
				Vector3 tmpVec = new Vector3();
				Matrix4 tmpMat = new Matrix4();
				Quaternion quatTemp = new Quaternion();
				@Override
				public void touchDragged(InputEvent event, float x, float y, int pointer) {
					//tmpV2.set(0,0,0).mul(movedMatrix.cpy().inv());
					tmpV1.set(camera.direction).crs(camera.up).y = 0f;
					tmpVec.set(tmpV2).sub(camera.position);
					camera.translate(tmpVec);
					
					quatTemp.setFromAxis(tmpV1, y - tmp2.y);
					
					camera.rotate(quatTemp);
					tmpVec.mul(quatTemp);
					
					quatTemp.setFromAxis(0, 1, 0, tmp2.x - x);
					
					camera.rotate(quatTemp);
					tmpVec.mul(quatTemp);
					
					camera.translate(tmpVec.scl(-1));
					camera.update();
					tmp2.set(x, y);
				}

			});
		camPad.setBounds(0, 0, stWidth, stHeight);
		st.addActor(camPad);
		padMoves = new Touchpad(skin);
		padMoves.addListener(new InputListener() {
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
		padMoves.setBounds(0, 0, stHeight * 0.47f, stHeight * 0.47f);
		st.addActor(padMoves);
		ImageButton ib;
		ib = new ImageButton(skin, "attack");
		ib.addListener(new ClickListener() {
				boolean hasLight = true;

				@Override
				public void clicked(InputEvent e, float x, float y) {
					if (hasLight)
						modelBatch.environment.remove(d1);
					else
						modelBatch.environment.add(d1);
					hasLight = !hasLight;
				}
			});
		ib.setBounds(stWidth - 80f, 0, 80, 80);
		st.addActor(ib);
		/*
		 ib = new ImageButton(skin, "jump");
		 ib.addListener(new ClickListener() {
		 boolean hasLight = true;

		 @Override
		 public void clicked(InputEvent e, float x, float y) {
		 if (hasLight)
		 modelBatch.environment.remove(p1);
		 else
		 modelBatch.environment.add(p1);
		 hasLight = !hasLight;
		 }
		 });
		 ib.setBounds(stWidth - 160, 0, 80, 80);
		 st.addActor(ib);
		 */
		ib = new ImageButton(skin, "act");
		ib.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent e, float x, float y) {
					modelBatch.environment.setUseShadowMapping(!modelBatch.environment.isUseShadowMapping());
				}
			});
		ib.setBounds(stWidth - 100, 70, 80, 80);
		st.addActor(ib);
		GraphFunc.app.getInput().setInputProcessor(st);
	}
	//moves algorithm cached data initialize
	final Matrix4 movedMatrix = new Matrix4();
	final Matrix4 intialMatrix = new Matrix4().scl(0.08f);
	final Quaternion rot = new Quaternion();
	final Vector3 moves = new Vector3();
	final Vector2 padInput = new Vector2();
	Touchpad padMoves;
	//end
	@Override
	public void render(float delta) {
		final Stage st = appl.stage;
		st.act(delta);
		padInput.set(padMoves.knobPercent);
		if (padInput.len() > MathUtils.FLOAT_ROUNDING_ERROR) {
			moves.set(-padInput.x, 0, padInput.y).nor();
			movedMatrix.rotateTowardDirection(moves, new Vector3(0, 1, 0));
			float rad = (float) Math.atan(moves.y / moves.x);
			rad -= instance[1].transform.getRotation(rot, true).getYawRad();
			//rad += Math.atan(camera.direction.z / camera.direction.x);
			movedMatrix.rotateRad(new Vector3(0, 1, 0), rad);
			movedMatrix.translate(0, 0, delta * 650);
			instance[1].transform.set(intialMatrix).mul(movedMatrix);
		}
		knightAnim.update(delta);
		modelBatch.render(camera, instance);

		st.draw();
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
