package com.ariasaproject.advancerofrpg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.loaders.resolvers.InternalFileHandleResolver;
import com.ariasaproject.advancerofrpg.graphics.OrthographicCamera;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.graphics.g2d.GlyphLayout;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.screen.Scene;
import com.ariasaproject.advancerofrpg.screen.SplashScreen;
import com.ariasaproject.advancerofrpg.utils.Viewport;
import com.ariasaproject.advancerofrpg.graphics.Graphics;

public class ApplicationListener {
	public Stage stage;
	public Batch batch;
	public BitmapFont fps;
	public GlyphLayout layDebug;
	public Viewport uiView;
	private Scene screen;
	protected static ExecutorService exec = Executors.newFixedThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable r) {
				Thread thread = new Thread(r, "Background Task");
				thread.setDaemon(true);
				return thread;
			}
		});
	public static final AssetContainer asset = new AssetContainer(exec, new InternalFileHandleResolver());
	public void create() {
		batch = new Batch();
		uiView = new Viewport(new OrthographicCamera(), 800, 600, 1920, 1080);
		stage = new Stage(uiView, batch);
		fps = new BitmapFont();
		fps.getData().setScale(2.25f);
		layDebug = new GlyphLayout(fps, "FPS:000|####/##/##_#|N{c++}");
		setScene(new SplashScreen(this));
	}

	public void setScene(final Scene s) {
		if (screen != null) {
			screen.hide();
			stage.clear();
			GraphFunc.app.getInput().setInputProcessor(null);
		}
		screen = s;
		if (s != null) {
			s.show();
			s.resize(GraphFunc.app.getGraphics().getWidth(), GraphFunc.app.getGraphics().getHeight());
		}
	}

	public void resize(int width, int height) {
		GraphFunc.tgf.glViewport(0, 0, width, height);
		uiView.update();
		if (screen != null)
			screen.resize(width, height);
	}
	public void resume() {
		if (screen != null)
			screen.resume();
	}
	public String logUpt = "";
	private String logUp = "";

	public void render(float delta) {
		GraphFunc.tgf.glClearColorMask(TGF.GL_COLOR_BUFFER_BIT | TGF.GL_DEPTH_BUFFER_BIT | TGF.GL_STENCIL_BUFFER_BIT, 0, 0, 0, 1);
		asset.update();
		if (screen != null)
			screen.render(delta);
		batch.begin();
		final Runtime r = Runtime.getRuntime();
		final Graphics g = GraphFunc.app.getGraphics();
		logUpt = String.format("used: %03.2f of RAM" , (float) (r.totalMemory() - r.freeMemory()) * 100 / r.maxMemory());
		logUp = String.format("FPS:%03d|2022/05/12_0|N{%s}\n%s", g.getFramesPerSecond(), GraphFunc.nativeLog(), logUpt);
		layDebug.setText(fps, logUp);
		fps.draw(batch, layDebug, (g.getWidth() - layDebug.width) / 2, g.getHeight() - (layDebug.height / 2.25f));
		batch.end();
	}

	public void pause() {
		if (screen != null)
			screen.pause();
	}

	public void destroy() {
		fps.dispose();
		if (screen != null)
			screen.hide();
		stage.dispose();
		batch.dispose();
		uiView = null;
		asset.dispose();

		exec.shutdown();
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Couldn't shutdown loading thread", e);
		}
	}
}
