/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ariasaproject.advancerofrpg;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowMaximizeCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;

import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public class Lwjgl3Window implements Disposable {
	private long windowHandle;
	final ApplicationListener listener;
	final Lwjgl3Application application;
	private boolean listenerInitialized = false;
	private Lwjgl3Graphics graphics;
	private Lwjgl3Input input;
	private final Lwjgl3ApplicationConfiguration config;
	private final Array<Runnable> runnables = new Array<Runnable>();
	private final Array<Runnable> executedRunnables = new Array<Runnable>();
	private final IntBuffer tmpBuffer;
	private final IntBuffer tmpBuffer2;
	boolean iconified = false;
	private boolean requestRendering = false;

	private final GLFWWindowFocusCallback focusCallback = new GLFWWindowFocusCallback() {
		@Override
		public void invoke(long windowHandle, final boolean focused) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					// TODO : add windows on focus changed
				}
			});
		}
	};

	private final GLFWWindowIconifyCallback iconifyCallback = new GLFWWindowIconifyCallback() {
		@Override
		public void invoke(long windowHandle, final boolean iconified) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					// TODO : on iconfield changed or something
					Lwjgl3Window.this.iconified = iconified;
					if (iconified) {
						listener.pause();
					} else {
						listener.resume();
					}
				}
			});
		}
	};

	private final GLFWWindowMaximizeCallback maximizeCallback = new GLFWWindowMaximizeCallback() {
		@Override
		public void invoke(long windowHandle, final boolean maximized) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					// TODO : on maximized windows
				}
			});
		}

	};

	private final GLFWWindowCloseCallback closeCallback = new GLFWWindowCloseCallback() {
		@Override
		public void invoke(final long windowHandle) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					//TODO : on windows closed callback
					//GLFW.glfwSetWindowShouldClose(windowHandle, false);
				}
			});
		}
	};

	private final GLFWDropCallback dropCallback = new GLFWDropCallback() {
		@Override
		public void invoke(final long windowHandle, final int count, final long names) {
			final String[] files = new String[count];
			for (int i = 0; i < count; i++) {
				files[i] = getName(names, i);
			}
			postRunnable(new Runnable() {
				@Override
				public void run() {
					//TODO : on file dropped in Windows
				}
			});
		}
	};

	private final GLFWWindowRefreshCallback refreshCallback = new GLFWWindowRefreshCallback() {
		@Override
		public void invoke(long windowHandle) {
			postRunnable(new Runnable() {
				@Override
				public void run() {
					//TODO : windows was refreshed
				}
			});
		}
	};

	Lwjgl3Window(ApplicationListener listener, Lwjgl3ApplicationConfiguration config, Lwjgl3Application application) {
		this.listener = listener;
		this.config = config;
		this.application = application;
		this.tmpBuffer = BufferUtils.createIntBuffer(1);
		this.tmpBuffer2 = BufferUtils.createIntBuffer(1);
	}

	void create(long windowHandle) {
		this.windowHandle = windowHandle;
		this.input = new Lwjgl3Input(this);
		this.graphics = new Lwjgl3Graphics(this);

		GLFW.glfwSetWindowFocusCallback(windowHandle, focusCallback);
		GLFW.glfwSetWindowIconifyCallback(windowHandle, iconifyCallback);
		GLFW.glfwSetWindowMaximizeCallback(windowHandle, maximizeCallback);
		GLFW.glfwSetWindowCloseCallback(windowHandle, closeCallback);
		GLFW.glfwSetDropCallback(windowHandle, dropCallback);
		GLFW.glfwSetWindowRefreshCallback(windowHandle, refreshCallback);

		//TODO : when windows callback
	}

	public ApplicationListener getListener() {
		return listener;
	}

	public void postRunnable(Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

	/**
	 * Sets the position of the window in logical coordinates. All monitors span a
	 * virtual surface together. The coordinates are relative to the first monitor
	 * in the virtual surface.
	 **/
	public void setPosition(int x, int y) {
		GLFW.glfwSetWindowPos(windowHandle, x, y);
	}

	/**
	 * @return the window position in logical coordinates. All monitors span a
	 *         virtual surface together. The coordinates are relative to the first
	 *         monitor in the virtual surface.
	 **/
	public int getPositionX() {
		GLFW.glfwGetWindowPos(windowHandle, tmpBuffer, tmpBuffer2);
		return tmpBuffer.get(0);
	}

	/**
	 * @return the window position in logical coordinates. All monitors span a
	 *         virtual surface together. The coordinates are relative to the first
	 *         monitor in the virtual surface.
	 **/
	public int getPositionY() {
		GLFW.glfwGetWindowPos(windowHandle, tmpBuffer, tmpBuffer2);
		return tmpBuffer2.get(0);
	}

	/**
	 * Sets the visibility of the window. Invisible windows will still call their
	 * {@link ApplicationListener}
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			GLFW.glfwShowWindow(windowHandle);
		} else {
			GLFW.glfwHideWindow(windowHandle);
		}
	}

	/**
	 * Closes this window and pauses and disposes the associated
	 * {@link ApplicationListener}.
	 */
	public void closeWindow() {
		GLFW.glfwSetWindowShouldClose(windowHandle, true);
	}

	/**
	 * Minimizes (iconifies) the window. Iconified windows do not call their
	 * {@link ApplicationListener} until the window is restored.
	 */
	public void iconifyWindow() {
		GLFW.glfwIconifyWindow(windowHandle);
	}

	/**
	 * De-minimizes (de-iconifies) and de-maximizes the window.
	 */
	public void restoreWindow() {
		GLFW.glfwRestoreWindow(windowHandle);
	}

	/**
	 * Maximizes the window.
	 */
	public void maximizeWindow() {
		GLFW.glfwMaximizeWindow(windowHandle);
	}

	/**
	 * Brings the window to front and sets input focus. The window should already be
	 * visible and not iconified.
	 */
	public void focusWindow() {
		GLFW.glfwFocusWindow(windowHandle);
	}

	/**
	 * Sets the icon that will be used in the window's title bar. Has no effect in
	 * macOS, which doesn't use window icons.
	 * 
	 * @param image One or more images. The one closest to the system's desired size
	 *              will be scaled. Good sizes include 16x16, 32x32 and 48x48.
	 *              Pixmap format
	 *              {@link com.badlogic.GraphFunc.graphics.Pixmap.Format#RGBA8888
	 *              RGBA8888} is preferred so the images will not have to be copied
	 *              and converted. The chosen image is copied, and the provided
	 *              Pixmaps are not disposed.
	 */
	public void setIcon(Pixmap... image) {
		setIcon(windowHandle, image);
	}

	static void setIcon(long windowHandle, String[] imagePaths, Files.FileType imageFileType) {
		Pixmap[] pixmaps = new Pixmap[imagePaths.length];
		for (int i = 0; i < imagePaths.length; i++) {
			pixmaps[i] = new Pixmap(GraphFunc.app.getFiles().getFileHandle(imagePaths[i], imageFileType));
		}

		setIcon(windowHandle, pixmaps);

		for (Pixmap pixmap : pixmaps) {
			pixmap.dispose();
		}
	}

	static void setIcon(long windowHandle, Pixmap[] images) {
		GLFWImage.Buffer buffer = GLFWImage.malloc(images.length);
		Pixmap[] tmpPixmaps = new Pixmap[images.length];

		for (int i = 0; i < images.length; i++) {
			Pixmap pixmap = images[i];

			if (pixmap.format != Pixmap.Format.RGBA8888) {
				Pixmap rgba = new Pixmap(pixmap.width, pixmap.height, Pixmap.Format.RGBA8888);
				rgba.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.width, pixmap.height);
				tmpPixmaps[i] = rgba;
				pixmap = rgba;
			}

			GLFWImage icon = GLFWImage.malloc();
			icon.set(pixmap.width, pixmap.height, pixmap.getPixels());
			buffer.put(icon);

			icon.free();
		}

		buffer.position(0);
		GLFW.glfwSetWindowIcon(windowHandle, buffer);

		buffer.free();
		for (Pixmap pixmap : tmpPixmaps) {
			if (pixmap != null) {
				pixmap.dispose();
			}
		}

	}

	public void setTitle(CharSequence title) {
		GLFW.glfwSetWindowTitle(windowHandle, title);
	}

	/**
	 * Sets minimum and maximum size limits for the window. If the window is full
	 * screen or not resizable, these limits are ignored. Use -1 to indicate an
	 * unrestricted dimension.
	 */
	public void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
		setSizeLimits(windowHandle, minWidth, minHeight, maxWidth, maxHeight);
	}

	static void setSizeLimits(long windowHandle, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		GLFW.glfwSetWindowSizeLimits(windowHandle, minWidth > -1 ? minWidth : GLFW.GLFW_DONT_CARE, minHeight > -1 ? minHeight : GLFW.GLFW_DONT_CARE, maxWidth > -1 ? maxWidth : GLFW.GLFW_DONT_CARE, maxHeight > -1 ? maxHeight : GLFW.GLFW_DONT_CARE);
	}

	Lwjgl3Graphics getGraphics() {
		return graphics;
	}

	Lwjgl3Input getInput() {
		return input;
	}

	public long getWindowHandle() {
		return windowHandle;
	}

	void windowHandleChanged(long windowHandle) {
		this.windowHandle = windowHandle;
		input.windowHandleChanged(windowHandle);
	}

	boolean update() {
		if (!listenerInitialized) {
			initializeListener();
		}
		synchronized (runnables) {
			executedRunnables.addAll(runnables);
			runnables.clear();
		}
		for (Runnable runnable : executedRunnables) {
			runnable.run();
		}
		boolean shouldRender = executedRunnables.size > 0 || graphics.isContinuousRendering();
		executedRunnables.clear();

		if (!iconified)
			input.update();

		synchronized (this) {
			shouldRender |= requestRendering && !iconified;
			requestRendering = false;
		}

		if (shouldRender) {
			graphics.update();
			//TODO : need delta time directly
			listener.render(application.getGraphics().getDeltaTime());
			GLFW.glfwSwapBuffers(windowHandle);
		}

		if (!iconified)
			input.prepareNext();

		return shouldRender;
	}

	void requestRendering() {
		synchronized (this) {
			this.requestRendering = true;
		}
	}

	boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowHandle);
	}

	Lwjgl3ApplicationConfiguration getConfig() {
		return config;
	}

	boolean isListenerInitialized() {
		return listenerInitialized;
	}

	void initializeListener() {
		if (!listenerInitialized) {
			listener.create();
			listener.resize(graphics.getWidth(), graphics.getHeight());
			listenerInitialized = true;
		}
	}

	void makeCurrent() {
		GLFW.glfwMakeContextCurrent(windowHandle);
	}

	@Override
	public void dispose() {
		listener.pause();
		listener.destroy();
		Lwjgl3Cursor.dispose(this);
		graphics.dispose();
		input.dispose();
		GLFW.glfwSetWindowFocusCallback(windowHandle, null);
		GLFW.glfwSetWindowIconifyCallback(windowHandle, null);
		GLFW.glfwSetWindowCloseCallback(windowHandle, null);
		GLFW.glfwSetDropCallback(windowHandle, null);
		GLFW.glfwDestroyWindow(windowHandle);

		focusCallback.free();
		iconifyCallback.free();
		maximizeCallback.free();
		closeCallback.free();
		dropCallback.free();
		refreshCallback.free();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (windowHandle ^ (windowHandle >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Lwjgl3Window other = (Lwjgl3Window) obj;
		if (windowHandle != other.windowHandle)
			return false;
		return true;
	}
}
