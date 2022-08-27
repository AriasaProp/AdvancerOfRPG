package com.ariasaproject.advancerofrpg;

import java.nio.IntBuffer;
import java.util.prefs.Preferences;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.AMDDebugOutput;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.Callback;

import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.audio.Lwjgl3Audio;
import com.ariasaproject.advancerofrpg.audio.OpenALLwjgl3Audio;
import com.ariasaproject.advancerofrpg.audio.mock.MockAudio;
import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;

public class Lwjgl3Application implements Application {

	static {
		System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");
		try {
		    System.loadLibrary("ext");
		} catch (Exception e) {
		    System.out.println("failed to load library : ext");
		    System.exit(0);
		}
	}
	public static void main(String[] args) {
		System.out.println("Started Application");
		new Lwjgl3Application();
	}
	
	
	private final Lwjgl3ApplicationConfiguration config;
	final Array<Lwjgl3Window> windows = new Array<Lwjgl3Window>();
	private volatile Lwjgl3Window currentWindow;
	private Lwjgl3Audio audio;
	private final Files files;
	private final Net net;
	private final ObjectMap<String, Preferences> preferences = new ObjectMap<String, Preferences>();
	private final Lwjgl3Clipboard clipboard;
	private volatile boolean running = true;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();	
	private final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
	private static GLFWErrorCallback errorCallback;
	private static GLVersion glVersion;
	private static Callback glDebugCallback;

	public Lwjgl3Application() {
		if (errorCallback == null) {
			errorCallback = GLFWErrorCallback.createPrint(System.err);
			GLFW.glfwSetErrorCallback(errorCallback);
			GLFW.glfwInitHint(GLFW.GLFW_JOYSTICK_HAT_BUTTONS, GLFW.GLFW_FALSE);
			if (!GLFW.glfwInit()) {
				throw new RuntimeException("Unable to initialize GLFW");
			}
		}
		ApplicationListener listener = new ApplicationListener();
		this.config = new Lwjgl3ApplicationConfiguration();
		if (config.title == null) config.title = listener.getClass().getSimpleName();
		GraphFunc.app = this;
		if (!config.disableAudio) {
			try {
				this.audio = createAudio(config);
			} catch (Throwable t) {
				log("Lwjgl3Application", "Couldn't initialize audio, disabling audio", t);
				this.audio = new MockAudio();
			}
		} else {
			this.audio = new MockAudio();
		}
		this.files = new Lwjgl3Files();
		this.net = new Lwjgl3Net(config);
		this.clipboard = new Lwjgl3Clipboard();

		Lwjgl3Window window = createWindow(config, listener, 0);
		windows.add(window);
		try {
			Array<Lwjgl3Window> closedWindows = new Array<Lwjgl3Window>();
			while (running && windows.size > 0) {
				// FIXME put it on a separate thread
				audio.update();

				boolean haveWindowsRendered = false;
				closedWindows.clear();
				for (Lwjgl3Window window : windows) {
					window.makeCurrent();
					currentWindow = window;
					synchronized (lifecycleListeners) {
						haveWindowsRendered |= window.update();
					}
					if (window.shouldClose()) {
						closedWindows.add(window);
					}
				}
				GLFW.glfwPollEvents();

				boolean shouldRequestRendering;
				synchronized (runnables) {
					shouldRequestRendering = runnables.size > 0;
					executedRunnables.clear();
					executedRunnables.addAll(runnables);
					runnables.clear();
				}
				for (Runnable runnable : executedRunnables) {
					runnable.run();
				}
				if (shouldRequestRendering){
					// Must follow Runnables execution so changes done by Runnables are reflected
					// in the following render.
					for (Lwjgl3Window window : windows) {
						if (!window.getGraphics().isContinuousRendering())
							window.requestRendering();
					}
				}

				for (Lwjgl3Window closedWindow : closedWindows) {
					if (windows.size == 1) {
						for (int i = lifecycleListeners.size - 1; i >= 0; i--) {
							LifecycleListener l = lifecycleListeners.get(i);
							l.pause();
							l.dispose();
						}
						lifecycleListeners.clear();
					}
					closedWindow.dispose();

					windows.removeValue(closedWindow, false);
				}

				if (!haveWindowsRendered) {
					try {
						Thread.sleep(1000 / config.idleFPS);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
			// window cleanup
			synchronized (lifecycleListeners) {
				for(LifecycleListener lifecycleListener : lifecycleListeners){
					lifecycleListener.pause();
					lifecycleListener.dispose();
				}
			}
			for (Lwjgl3Window window : windows) {
				window.dispose();
			}
			windows.clear();
		} catch(Throwable e) {
			for (StackTraceElement s : e.getStackTrace())
				error(TAG, s.toString());
			error(TAG, "error " + e.getMessage());
		} finally {
			Lwjgl3Cursor.disposeSystemCursors();
			audio.dispose();
			errorCallback.free();
			errorCallback = null;
			if (glDebugCallback != null) {
				glDebugCallback.free();
				glDebugCallback = null;
			}
			GLFW.glfwTerminate();
		}
	}

	@Override
	public Graphics getGraphics() {
		return currentWindow.getGraphics();
	}

	@Override
	public Audio getAudio() {
		return audio;
	}

	@Override
	public Input getInput() {
		return currentWindow.getInput();
	}

	@Override
	public Files getFiles() {
		return files;
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
	public void exit() {
		running = false;
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.add(listener);
		}
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		synchronized (lifecycleListeners) {
			lifecycleListeners.removeValue(listener, true);
		}
	}

	@Override
	public Lwjgl3Audio createAudio (Lwjgl3ApplicationConfiguration config) {
		return new OpenALLwjgl3Audio(config.audioDeviceSimultaneousSources,
			config.audioDeviceBufferCount, config.audioDeviceBufferSize);
	}

	public Lwjgl3Window newWindow(ApplicationListener listener, Lwjgl3WindowConfiguration config) {
		Lwjgl3ApplicationConfiguration appConfig = Lwjgl3ApplicationConfiguration.copy(this.config);
		appConfig.setWindowConfiguration(config);
		return createWindow(appConfig, listener, windows.get(0).getWindowHandle());
	}

	private Lwjgl3Window createWindow (final Lwjgl3ApplicationConfiguration config, ApplicationListener listener,
		final long sharedContext) {
		final Lwjgl3Window window = new Lwjgl3Window(listener, config, this);
		if (sharedContext == 0) {
			// the main window is created immediately
			createWindow(window, config, sharedContext);
		} else {
			// creation of additional windows is deferred to avoid GL context trouble
			postRunnable(new Runnable() {
				public void run () {
					createWindow(window, config, sharedContext);
					windows.add(window);
				}
			});
		}
		return window;
	}

	void createWindow(Lwjgl3Window window, Lwjgl3ApplicationConfiguration config, long sharedContext) {
		//create GLFW with spesification
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, config.windowResizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, config.windowMaximized ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, config.autoIconify ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

		if(sharedContextWindow == 0) {
			GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, config.r);
			GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, config.g);
			GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, config.b);
			GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, config.a);
			GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, config.stencil);
			GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.depth);
			GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.samples);
		}

		if (config.useGL30) {
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, config.gles30ContextMajorVersion);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, config.gles30ContextMinorVersion);
			if (SharedLibraryLoader.isMac) {
				// hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
				// WGL_ARB_create_context extension is not available
				// see: http://www.glfw.org/docs/latest/compat.html
				GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
				GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
			}
		}

		if (config.transparentFramebuffer) {
			GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
		}

		if (config.debug) {
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
		}

		long windowHandle = 0;
		if(config.fullscreenMode != null) {
			GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, config.fullscreenMode.refreshRate);
			windowHandle = GLFW.glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title, config.fullscreenMode.getMonitor(), sharedContextWindow);
		} else {
			GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, config.windowDecorated? GLFW.GLFW_TRUE: GLFW.GLFW_FALSE);
			windowHandle = GLFW.glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, sharedContextWindow);			
		}
		if (windowHandle == 0)
			throw new RuntimeException("Couldn't create window");
		Lwjgl3Window.setSizeLimits(windowHandle, config.windowMinWidth, config.windowMinHeight, config.windowMaxWidth, config.windowMaxHeight);
		if (config.fullscreenMode == null) {
			if (config.windowX == -1 && config.windowY == -1) {
				int windowWidth = Math.max(config.windowWidth, config.windowMinWidth);
				int windowHeight = Math.max(config.windowHeight, config.windowMinHeight);
				if (config.windowMaxWidth > -1) windowWidth = Math.min(windowWidth, config.windowMaxWidth);
				if (config.windowMaxHeight > -1) windowHeight = Math.min(windowHeight, config.windowMaxHeight);

				long monitorHandle = GLFW.glfwGetPrimaryMonitor();
				if (config.windowMaximized && config.maximizedMonitor != null) {
					monitorHandle = config.maximizedMonitor.monitorHandle;
				}

				IntBuffer areaXPos = BufferUtils.createIntBuffer(1);
				IntBuffer areaYPos = BufferUtils.createIntBuffer(1);
				IntBuffer areaWidth = BufferUtils.createIntBuffer(1);
				IntBuffer areaHeight = BufferUtils.createIntBuffer(1);
				GLFW.glfwGetMonitorWorkarea(monitorHandle, areaXPos, areaYPos, areaWidth, areaHeight);

				GLFW.glfwSetWindowPos(windowHandle,
					    areaXPos.get(0) + areaWidth.get(0) / 2 - windowWidth / 2,
					    areaYPos.get(0) + areaHeight.get(0) / 2 - windowHeight / 2);
			} else
				GLFW.glfwSetWindowPos(windowHandle, config.windowX, config.windowY);

			if (config.windowMaximized)
				GLFW.glfwMaximizeWindow(windowHandle);
		}
		if (config.windowIconPaths != null)
			Lwjgl3Window.setIcon(windowHandle, config.windowIconPaths, config.windowIconFileType);
		GLFW.glfwMakeContextCurrent(windowHandle);
		GLFW.glfwSwapInterval(config.vSyncEnabled ? 1 : 0);
		GL.createCapabilities();
		String versionString = GL11.glGetString(GL11.GL_VERSION);
		String vendorString = GL11.glGetString(GL11.GL_VENDOR);
		String rendererString = GL11.glGetString(GL11.GL_RENDERER);
		if (!glVersion.isVersionEqualToOrHigher(3, 0))
			throw new RuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
					+ GL11.glGetString(GL11.GL_VERSION) + "\n" + glVersion.getDebugVersionString());
		if (config.debug) {
			glDebugCallback = GLUtil.setupDebugMessageCallback(config.debugStream);
			setGLDebugMessageControl(GLDebugMessageSeverity.NOTIFICATION, false);
		}
		//end
		window.create(windowHandle);
		window.setVisible(config.initialVisible);

		for (int i = 0; i < 2; i++) {
			GL11.glClearColor(config.initialBackgroundColor.r, config.initialBackgroundColor.g, config.initialBackgroundColor.b,
					config.initialBackgroundColor.a);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GLFW.glfwSwapBuffers(windowHandle);
		}
	}

	public enum GLDebugMessageSeverity {
		HIGH(
				GL43.GL_DEBUG_SEVERITY_HIGH,
				KHRDebug.GL_DEBUG_SEVERITY_HIGH,
				ARBDebugOutput.GL_DEBUG_SEVERITY_HIGH_ARB,
				AMDDebugOutput.GL_DEBUG_SEVERITY_HIGH_AMD),
		MEDIUM(
				GL43.GL_DEBUG_SEVERITY_MEDIUM,
				KHRDebug.GL_DEBUG_SEVERITY_MEDIUM,
				ARBDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_ARB,
				AMDDebugOutput.GL_DEBUG_SEVERITY_MEDIUM_AMD),
		LOW(
				GL43.GL_DEBUG_SEVERITY_LOW,
				KHRDebug.GL_DEBUG_SEVERITY_LOW,
				ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB,
				AMDDebugOutput.GL_DEBUG_SEVERITY_LOW_AMD),
		NOTIFICATION(
				GL43.GL_DEBUG_SEVERITY_NOTIFICATION,
				KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
				-1,
				-1);

		final int gl43, khr, arb, amd;

		GLDebugMessageSeverity(int gl43, int khr, int arb, int amd) {
			this.gl43 = gl43;
			this.khr = khr;
			this.arb = arb;
			this.amd = amd;
		}
	}

	public static boolean setGLDebugMessageControl (GLDebugMessageSeverity severity, boolean enabled) {
		GLCapabilities caps = GL.getCapabilities();
		final int GL_DONT_CARE = 0x1100; // not defined anywhere yet

		if (caps.OpenGL43) {
			GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.gl43, (IntBuffer) null, enabled);
			return true;
		}

		if (caps.GL_KHR_debug) {
			KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, severity.khr, (IntBuffer) null, enabled);
			return true;
		}

		if (caps.GL_ARB_debug_output && severity.arb != -1) {
			ARBDebugOutput.glDebugMessageControlARB(GL_DONT_CARE, GL_DONT_CARE, severity.arb, (IntBuffer) null, enabled);
			return true;
		}

		if (caps.GL_AMD_debug_output && severity.amd != -1) {
			AMDDebugOutput.glDebugMessageEnableAMD(GL_DONT_CARE, severity.amd, (IntBuffer) null, enabled);
			return true;
		}

		return false;
	}

	@Override
	public void restart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log (String tag, String message) {
		System.out.println("[" + tag + "] " + message);
	}

	@Override
	public void log (String tag, String message, Throwable exception) {
		System.out.println("[" + tag + "] " + message);
		exception.printStackTrace(System.out);
	}

	@Override
	public void error (String tag, String message) {
		System.err.println("[" + tag + "] " + message);
	}

	@Override
	public void error (String tag, String message, Throwable exception) {
		System.err.println("[" + tag + "] " + message);
		exception.printStackTrace(System.err);
	}	

	@Override
	public void debug (String tag, String message) {
		System.out.println("[" + tag + "] " + message);
	}

	@Override
	public void debug (String tag, String message, Throwable exception) {
		System.out.println("[" + tag + "] " + message);
		exception.printStackTrace(System.out);
	}
}
