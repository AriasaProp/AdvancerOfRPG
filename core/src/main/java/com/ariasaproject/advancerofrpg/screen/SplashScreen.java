package com.ariasaproject.advancerofrpg.screen;

import com.ariasaproject.advancerofrpg.ApplicationListener;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.math.Interpolation;
import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.actions.Actions;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Label;
import com.ariasaproject.advancerofrpg.scenes2d.ui.ProgressBar;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Skin;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Table;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Value;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Pool;

import java.util.Locale;

public class SplashScreen extends Scene {

    public SplashScreen(ApplicationListener appl) {
        super(appl);
    }

    @Override
    public void render(float delta) {
        appl.stage.act(delta);
        appl.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void show() {
        ApplicationListener.asset.load(new AssetDescriptor<Skin>("uiskin/system/systemskin.json", Skin.class));
        final Skin skin = ApplicationListener.asset.finishLoadingAsset("uiskin/system/systemskin.json");
        final Table table = new Table();
        table.setFillParent(true);
        table.bottom();
        appl.stage.addActor(table);
        Label label = new Label("Loading ( ? ? ? )", skin);
        label.setName("load");
        label.setAlignment(Align.center);
        table.add(label).growX();
        table.row();
        ProgressBar progressBar = new ProgressBar(0, 1, 0.0001f, false, skin);
        progressBar.setName("bar");
        progressBar.setValue(0);
        progressBar.setAnimateDuration(2.0f / 3.0f);
        progressBar.setAnimateInterpolation(Interpolation.fastSlow);
        table.add(progressBar).height(Value.percentHeight(0.062f, table)).fillY().growX();

        // new
        // ColladaModelLoader().loadModel(GraphFunc.app.getFiles().internal("model/model.dae"),
        // new ModelLoader.ModelParameters());
        /*
         * ModelData md = new G3dModelLoader(new
         * UBJsonReader()).loadModelData(GraphFunc.app.getFiles().internal(
         * "model/KnightCharacter.g3db")); UBJsonWriter jw = new
         * UBJsonWriter(GraphFunc.app.getFiles().external("json_log.txt").write(false));
         * try { jw.value(md); jw.flush(); jw.close(); } catch (IOException e) {
         *
         * }
         *
         */
        appl.stage.addAction(Actions.sequence(Actions.run(new Runnable() {
            @Override
            public void run() {
                ApplicationListener.asset.load(new AssetDescriptor<Texture>("texture/badlogic.jpg", Texture.class), new AssetDescriptor<Texture>("texture/android.jpg", Texture.class), new AssetDescriptor<Model>("model/KnightCharacter.g3db", Model.class));
            }
        }), new Action() {
            private boolean began = false, complete = false;

            @Override
            public void restart() {
                began = false;
                complete = false;
            }

            @Override
            public boolean act(float delta) {
                if (complete)
                    return true;
                Pool pool = getPool();
                setPool(null); // Ensure this action can't be returned to the pool while executing.
                try {
                    ProgressBar p = table.findActor("bar");
                    if (!began) {
                        p.setValue(0);
                        began = true;
                    }
                    if ((p.getVisualValue() >= 1) & ApplicationListener.asset.update()) {
                        ((Label) table.findActor("load")).setText("Loading Is Done!");
                        return true;
                    }
                    final String status = String.format(Locale.getDefault(), "Loading data [ %02d queued -> %02d loaded ] ", ApplicationListener.asset.getQueuedAssets(), ApplicationListener.asset.getLoadedAssets());
                    final float prog = ApplicationListener.asset.getProgress();
                    if (p.getValue() < prog) {
                        p.setValue(prog);
                    }

                    ((Label) table.findActor("load")).setText(status);

                    return false;
                } catch (Exception e) {
                    GraphFunc.app.error("Screen", e.getMessage());
                    throw new RuntimeException("Screen error " + e);
                } finally {
                    setPool(pool);
                }
            }
        }, Actions.fadeOut(0.25f), Actions.run(new Runnable() {
            @Override
            public void run() {
                appl.setScene(new Dashboard(appl));
            }
        })));
    }
}
