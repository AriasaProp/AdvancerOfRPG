package com.ariasaproject.advancerofrpg;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.egl.EGL14;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.Files.FileType;
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

public class LwjglApplication implements Application, Graphics, Runnable {
	protected final Array<Runnable> runnables = new Array<Runnable>();
	protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
	protected final List<LWJGLMusic> musics = new ArrayList<LWJGLMusic>();
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
		LwjglApplication app = new LwjglApplication();
	}

	public LwjglApplication() {
		GraphFunc.app = this;
		input = new LWJGLInput();
		files = new LWJGLFiles();
		net = new LWJGLNet();
		clipboard = new LWJGLClipboard();
		
		
		this.tgf = new LWJGLTGF();
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
	protected synchronized void onPause() {
		input.onPause();
		// audio pause
		synchronized (musics) {
			for (LWJGLMusic music : musics) {
				if (music.isPlaying()) {
					music.pause();
					music.wasPlaying = true;
				} else
					music.wasPlaying = false;
			}
		}
		soundPool.autoPause();
		// graphics
		pause = true;
	}
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
		// ignored cause LWJGL gles doesn't support VSync
	}

	// main loop
	@Override
	public void run() {
		ApplicationListener appl = ApplicationListener.getApplicationListener();
		try {
			byte eglDestroyRequest = 0;// to destroy egl surface, egl contex, egl display, ?....
			boolean newContext = true, // indicator
					created = false, lrunning = true, lresize, lresume = false, lpause = false;// on running state
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
			frameStart = System.currentTimeMillis();

			while (!destroy && !GLFW.glfwWindowShouldClose(window)) {
				GLFW.glfwPollEvents();
				synchronized (this) {
					// egl destroy request
					if (mEglSurface != null && (eglDestroyRequest > 0 || !hasSurface)) {
						EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
						if (!EGL14.eglDestroySurface(mEglDisplay, mEglSurface))
							throw new RuntimeException("eglDestroySurface failed: " + Integer.toHexString(EGL14.eglGetError()));
						mEglSurface = null;
						if (mEglContext != null && (eglDestroyRequest > 1)) {
							if (!EGL14.eglDestroyContext(mEglDisplay, mEglContext))
								throw new RuntimeException("eglDestroyContext failed: " + Integer.toHexString(EGL14.eglGetError()));
							mEglContext = null;
							newContext = true;
							if (mEglDisplay != null && (eglDestroyRequest > 3)) {
								EGL14.eglTerminate(mEglDisplay);
								mEglDisplay = null;
							}
						}
						eglDestroyRequest = 0;
					}
					// end destroy request

					lresize = resize;
					if (resize)
						resize = false;
					if (lpause)
						lrunning = false;
					lpause = pause;
					if (pause)
						pause = false;
					lresume = resume;
					if (resume) {
						resume = false;
						lrunning = true;
					}
					// Ready to draw?
					if (!lrunning || !hasSurface) {
						wait();
						continue;
					}
				}

				if (mEglDisplay == null) {
					final int[] temp = new int[2]; // for chaching value output

					mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
					if (mEglDisplay == EGL14.EGL_NO_DISPLAY || mEglDisplay == null) {
						mEglDisplay = null;
						throw new RuntimeException("eglGetDisplay failed " + Integer.toHexString(EGL14.eglGetError()));
					}
					if (EGL14.eglInitialize(mEglDisplay, temp, 0, temp, 1))
						log(TAG, "version EGL " + temp[0] + "." + temp[1]);
					else
						throw new RuntimeException("eglInitialize failed " + Integer.toHexString(EGL14.eglGetError()));

					if (mEglConfig == null) {
						// choose best config
						final int[] s_configAttribs2 = { EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER, // rgb color
								// buffer
								// should
								// exist
								EGL14.EGL_NONE // end config
						};
						EGL14.eglChooseConfig(mEglDisplay, s_configAttribs2, 0, null, 0, 0, temp, 0);
						if (temp[0] <= 0)
							throw new IllegalArgumentException("No configs match with configSpec");
						EGLConfig[] configs = new EGLConfig[temp[0]];
						EGL14.eglChooseConfig(mEglDisplay, s_configAttribs2, 0, configs, 0, configs.length, temp, 0);
						int lastSc = -1, curSc;
						mEglConfig = configs[0];
						for (EGLConfig config : configs) {
							temp[0] = -1;
							// alpha should 0
							// choose higher depth, stencil, color buffer(rgba)
							curSc = -1;
							for (int attr : new int[] { EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_BUFFER_SIZE, EGL14.EGL_ALPHA_SIZE, EGL14.EGL_DEPTH_SIZE, EGL14.EGL_STENCIL_SIZE }) {
								if (EGL14.eglGetConfigAttrib(mEglDisplay, config, attr, temp, 0)) {
									if (attr == EGL14.EGL_ALPHA_SIZE)
										temp[0] *= -1;
									curSc += temp[0];
								} else {
									int error;
									while ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS)
										error(TAG, String.format("EglConfigAttribute : EGL error: 0x%x", error));
								}
							}
							if (curSc > lastSc) {
								lastSc = curSc;
								mEglConfig = config;
							}
						}
					}
				}
				if (newContext || mEglSurface == null) {
					if (newContext) {
						final int[] attrib_list = { EGL14.EGL_CONTEXT_CLIENT_VERSION, mayorV, EGL14.EGL_NONE };
						mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, attrib_list, 0);
						if (mEglContext == null || mEglContext == EGL14.EGL_NO_CONTEXT) {
							mEglContext = null;
							throw new RuntimeException("createContext failed: " + Integer.toHexString(EGL14.eglGetError()));
						}
					}
					mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, holder, null, 0);
					if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
						mEglSurface = null;
						throw new RuntimeException("Create EGL Surface failed: " + Integer.toHexString(EGL14.eglGetError()));
					}
					if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext))
						throw new RuntimeException("Make EGL failed: " + Integer.toHexString(EGL14.eglGetError()));

					if (newContext) {

						if (!created)

						appl.resize(width, height);
						lresize = false;
						lastFrameTime = System.currentTimeMillis();
						newContext = false;
					}
				}
				if (lresize) {
					EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
					EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
					appl.resize(width, height);
				}

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
				if (lpause) {
					LifecycleListener[] listeners = lifecycleListeners.begin();
					for (int i = 0, n = lifecycleListeners.size; i < n; i++) {
						listeners[i].pause();
					}
					lifecycleListeners.end();
					appl.pause();
					eglDestroyRequest |= (tgf.limitGLESContext() ? 2 : 1);
				}
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
			appl.destroy();
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

	@Override
	public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
		return new LWJGLAudioDevice(samplingRate, isMono);
	}

	@Override
	public Music newMusic(FileHandle file) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		if (file.type() == FileType.Internal) {
			try {
				AssetFileDescriptor descriptor = files.descriptor(file);
				mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
				descriptor.close();
				mediaPlayer.prepare();
				LWJGLMusic music = new LWJGLMusic(this, mediaPlayer);
				synchronized (musics) {
					musics.add(music);
				}
				return music;
			} catch (Exception ex) {
				throw new RuntimeException("Error loading audio file: " + file + "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {
				mediaPlayer.setDataSource(file.file().getPath());
				mediaPlayer.prepare();
				LWJGLMusic music = new LWJGLMusic(this, mediaPlayer);
				synchronized (musics) {
					musics.add(music);
				}
				return music;
			} catch (Exception ex) {
				throw new RuntimeException("Error loading audio file: " + file, ex);
			}
		}

	}

	public Music newMusic(FileDescriptor fd) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(fd);
			mediaPlayer.prepare();
			LWJGLMusic music = new LWJGLMusic(this, mediaPlayer);
			synchronized (musics) {
				musics.add(music);
			}
			return music;
		} catch (Exception ex) {
			throw new RuntimeException("Error loading audio from FileDescriptor", ex);
		}
	}

	@Override
	public Sound newSound(FileHandle file) {
		if (file.type() == FileType.Internal) {
			try {
				AssetFileDescriptor descriptor = files.descriptor(file);
				Sound sound = new LWJGLSound(soundPool, manager, soundPool.load(descriptor, 1));
				descriptor.close();
				return sound;
			} catch (IOException ex) {
				throw new RuntimeException("Error loading audio file: " + file + "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {
				return new LWJGLSound(soundPool, manager, soundPool.load(file.file().getPath(), 1));
			} catch (Exception ex) {
				throw new RuntimeException("Error loading audio file: " + file, ex);
			}
		}
	}

	public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
		return new LWJGLAudioRecorder(samplingRate, isMono);
	}

	public void disposeMusic(Music music) {
		synchronized (musics) {
			musics.remove(music);
		}
	}

}
