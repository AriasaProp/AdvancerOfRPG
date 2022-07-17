package com.ariasaproject.advancerofrpg;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.audio.AudioDevice;
import com.ariasaproject.advancerofrpg.audio.AudioRecorder;
import com.ariasaproject.advancerofrpg.audio.Music;
import com.ariasaproject.advancerofrpg.audio.Sound;
import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.SnapshotArray;

public class LwjglApplication implements Application, Graphics, Audio, Runnable {
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
	protected LWJGLInput input;
	protected LWJGLFiles files;
	protected LWJGLNet net;
	protected LWJGLClipboard clipboard;
	int mayorV, minorV, revision;
	volatile boolean resume = false, pause = false, destroy = false, resize = false, hasFocus = true, mExited = false;
	long frameStart = System.currentTimeMillis(), lastFrameTime = System.currentTimeMillis();
	int frames, fps, width = 0, height = 0;
	float deltaTime = 0;
	static Thread mainTGFThread;
	// graphics params
	private LWJGLTGF tgf;
	// audio params

	final String title = "Advancer of RPG";

	public static void main(String[] args) {
		new LwjglApplication();
	}

	public LwjglApplication() {
		GraphFunc.app = this;
		input = new LWJGLInput();
		files = new LWJGLFiles();
		net = new LWJGLNet();
		clipboard = new LWJGLClipboard();

		this.tgf = new LWJGLOPENGL();
		GraphFunc.tgf = tgf;
		mainTGFThread = new Thread(this, "GLThread");
		mainTGFThread.start();

		GLFW.nglfwGetVersion(mayorV, minorV, revision);

		net.destroy();
		// audio destroy
		synchronized (musics) {
			ArrayList<Music> musicsCopy = new ArrayList<Music>(musics);
			for (Music music : musicsCopy) {
				music.dispose();
			}
		}

	}

	@Override
	public void restart() {
		// todo list
	}

	@Override
	public void exit() {
	}

	/*
	 * @Override public synchronized void onWindowFocusChanged(final boolean
	 * hasFocus) { super.onWindowFocusChanged(hasFocus); this.hasFocus = hasFocus;
	 * if (hasFocus) { // audio onResume synchronized (musics) { for (int i = 0; i <
	 * musics.size(); i++) { if (musics.get(i).wasPlaying) musics.get(i).play(); } }
	 * this.soundPool.autoResume(); } }
	 */

	@Override
	public Audio getAudio() {
		return this;
	}

	@Override
	public Input getInput() {
		return input;
	}

	@Override
	public Files getFiles() {
		return files;
	}

	@Override
	public Graphics getGraphics() {
		return this;
	}

	@Override
	public Net getNet() {
		return net;
	}

	@Override
	public Clipboard getClipboard() {
		return clipboard;
	}

	@Override
	public void postRunnable(Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	@Override
	public void log(String tag, String message) {
		System.out.println(TAG + " log : " + message);
	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		System.out.println(TAG + " log : " + message + "\n" + exception.getMessage());
	}

	@Override
	public void error(String tag, String message) {
		System.err.println(TAG + " : " + message);
	}

	@Override
	public void error(String tag, String message, Throwable exception) {
		System.err.println(TAG + " : " + message + "\n" + exception.getMessage());
	}

	@Override
	public void debug(String tag, String message) {
		System.out.println(TAG + " debug : " + message);
	}

	@Override
	public void debug(String tag, String message, Throwable exception) {
		System.out.println(TAG + " debug : " + message + "\n" + exception.getMessage());
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	// graphics loop function
	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	@Override
	public float getDeltaTime() {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setVSync(boolean vsync) {
//TODO
	}

	// main loop
	@Override
	public void run() {
		ApplicationListener appl = ApplicationListener.getApplicationListener();
		boolean lcreated = false, lresume = true, lpause, ldestroy;
		try {
			if (!GLFW.glfwInit())
				throw new RuntimeException("Window : Couldn't initialize GLFW.");
			long window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
			if (window == 0)
				throw new RuntimeException("Window couldn't be create.");
			GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			GLFW.glfwSetWindowPos(window, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2);
			GLFW.glfwMakeContextCurrent(window);
			// createCallbacks();
			// set FPS
			GLFW.glfwSwapInterval(1);
			
			appl.create();
			lcreated = true;
			
			frameStart = System.currentTimeMillis();

			while (!destroy && !GLFW.glfwWindowShouldClose(window)) {
				GLFW.glfwPollEvents();
				long time = System.currentTimeMillis();

				if (lresume) {
					synchronized (lifecycleListeners) {
						LifecycleListener[] listeners = lifecycleListeners.begin();
						for (int i = 0, n = lifecycleListeners.size; i < n; i++) {
							listeners[i].resume();
						}
						lifecycleListeners.end();
					}
					// appl.resume();
					time = frameStart = lastFrameTime = 0;
				}
				if (time - frameStart > 1000l) {
					fps = frames;
					frames = 0;
					frameStart = time;
				}
				deltaTime = (time - lastFrameTime) / 1000f;
				lastFrameTime = time;

				synchronized (runnables) {
					for (Runnable r : runnables)
						r.run();
					runnables.clear();
				}
				getInput().processEvents();
				appl.render(deltaTime);

				GLFW.glfwSwapBuffers(window);

				frames++;
			}
		} catch (Throwable e) {
			// fall thru and exit normally
			for (StackTraceElement s : e.getStackTrace()) {
				error(TAG, s.toString());
			}
			error(TAG, "error " + e.getMessage());
		} finally {
			// dispose all resources
			if(lcreated) appl.destroy();
			tgf.clear();

			LifecycleListener[] listeners = lifecycleListeners.begin();
			for (int i = 0, n = lifecycleListeners.size; i < n; i++) {
				listeners[i].dispose();
			}
			lifecycleListeners.end();

			// end thread
			synchronized (this) {
				mExited = true;
				notifyAll();
			}
		}
	}
	// audio stage

	protected final List<LWJGLMusic> musics = new ArrayList<LWJGLMusic>();

	@Override
	public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
		return new LWJGLAudioDevice(samplingRate, isMono);
	}

	@Override
	public Music newMusic(FileHandle file) {
		// TODO
		return null;
	}

	@Override
	public Sound newSound(FileHandle file) {
		try {
			return new LWJGLSound();
		} catch (Exception ex) {
			throw new RuntimeException("Error loading audio file: " + file, ex);
		}
	}

	@Override
	public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
		return new LWJGLAudioRecorder(samplingRate, isMono);
	}

	@Override
	public void disposeMusic(Music music) {
		synchronized (musics) {
			musics.remove(music);
		}
	}
}
