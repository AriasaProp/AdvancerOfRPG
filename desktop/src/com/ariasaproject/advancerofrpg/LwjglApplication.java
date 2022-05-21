package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.files.Files;
import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;
import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.net.Net;
import com.ariasaproject.advancerofrpg.openal.OpenALAudio;
import com.ariasaproject.advancerofrpg.utils.Array;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

import java.nio.ByteBuffer;

public class LwjglApplication implements Application, Graphics, Runnable {

    static int major, minor;

    static {
        try {
            System.loadLibrary("ext");
        } catch (Throwable ex) {
            System.err.println("Library : Couldn't load shared library of graph " + ex.getMessage());
        }
    }

    protected final OpenALAudio audio;
    protected final LwjglFiles files;
    protected final LwjglInput input;
    protected final LwjglNet net;
    protected final LwjglClipboard clipboard;
    protected final ApplicationListener listener;
    protected final Thread mainLoopThread;
    protected final Array<Runnable> runnables = new Array<Runnable>();
    protected final Array<LifecycleListener> lifecycleListeners = new Array<LifecycleListener>();
    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    final boolean fullscreen;
    protected boolean running = true;
    float deltaTime = 0;
    long frameStart = 0;
    int frames = 0;
    int fps;
    long lastTime = System.nanoTime();
    boolean vsync = false;
    boolean resize = false;
    int width, height;
    private TGF tgf;

    public LwjglApplication(ApplicationListener listener) {
        fullscreen = config.fullscreen;
        width = config.width;
        height = config.height;

        this.audio = new OpenALAudio(16, config.audioDeviceBufferCount, config.audioDeviceBufferSize);
        this.files = new LwjglFiles();
        this.input = new LwjglInput();
        this.net = new LwjglNet();
        this.clipboard = new LwjglClipboard();
        this.listener = listener;

        GraphFunc.app = this;
        mainLoopThread = new Thread(this, "GLThread");
        mainLoopThread.start();
    }

    @Override
    public void run() {
        LwjglApplication.this.setVSync(LwjglApplication.this.config.vSyncEnabled);
        try {
            try {
                if (getWidth() != width || getHeight() != height || Display.isFullscreen() != fullscreen) {
                    org.lwjgl.opengl.DisplayMode targetDisplayMode = null;
                    if (fullscreen) {
                        org.lwjgl.opengl.DisplayMode[] modes = Display.getAvailableDisplayModes();
                        int freq = 0;
                        targetDisplayMode = modes[0];
                        for (int i = 0; i < modes.length; i++) {
                            org.lwjgl.opengl.DisplayMode current = modes[i];

                            if ((current.getWidth() == width) && (current.getHeight() == height)) {
                                if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
                                    if ((targetDisplayMode == null)
                                            || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
                                        targetDisplayMode = current;
                                        freq = targetDisplayMode.getFrequency();
                                    }
                                }
                                if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
                                        && (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
                                    targetDisplayMode = current;
                                    break;
                                }
                            }
                        }
                    } else {
                        targetDisplayMode = new org.lwjgl.opengl.DisplayMode(width, height);
                    }

                    if (targetDisplayMode == null) {
                        throw new RuntimeException();
                    }

                    Display.setDisplayMode(targetDisplayMode);
                    Display.setFullscreen(fullscreen);
                    if (GraphFunc.tgf != null)
                        GraphFunc.tgf.glViewport(0, 0, targetDisplayMode.getWidth(), targetDisplayMode.getHeight());
                    config.width = targetDisplayMode.getWidth();
                    config.height = targetDisplayMode.getHeight();
                    resize = true;
                }
                Display.setTitle(config.title);
                Display.setResizable(config.resizable);
                Display.setInitialBackground(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
                        config.initialBackgroundColor.b);

                if (config.iconPaths.size > 0) {
                    ByteBuffer[] icons = new ByteBuffer[config.iconPaths.size];
                    for (int i = 0, n = config.iconPaths.size; i < n; i++) {
                        Pixmap pixmap = new Pixmap(GraphFunc.app.getFiles().getFileHandle(config.iconPaths.get(i),
                                config.iconFileTypes.get(i)));
                        if (pixmap.getFormat() != Format.RGBA8888) {
                            Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
                            rgba.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
                            pixmap = rgba;
                        }
                        icons[i] = ByteBuffer.allocateDirect(pixmap.getPixels().limit());
                        icons[i].put(pixmap.getPixels()).flip();
                        pixmap.dispose();
                    }
                    Display.setIcon(icons);
                }

                if (config.x != -1 && config.y != -1)
                    Display.setLocation(config.x, config.y);
                try {
                    Display.create(new PixelFormat());
                } catch (Exception ex3) {
                    if (ex3.getMessage().contains("Pixel format not accelerated"))
                        throw new RuntimeException("OpenGL is not supported by the video driver.", ex3);
                    throw new RuntimeException("Unable to create OpenGL display.", ex3);
                }
                config.x = Display.getX();
                config.y = Display.getY();
                GraphFunc.tgf = tgf = new LwjglGL30();
            } catch (LWJGLException e) {
                throw new RuntimeException(e);
            }

            listener.create();
            listener.resize(getWidth(), getHeight());
            resize = false;

            lastTime = System.nanoTime();
            while (running) {
                Display.processMessages();
                if (Display.isCloseRequested()) {
                    exit();
                }

                boolean shouldRender = false;

                config.x = Display.getX();
                config.y = Display.getY();
                if (resize || Display.wasResized() || Display.getWidth() != config.width
                        || Display.getHeight() != config.height) {
                    resize = false;
                    tgf.glViewport(0, 0, Display.getWidth(), Display.getHeight());
                    config.width = Display.getWidth();
                    config.height = Display.getHeight();
                    if (listener != null)
                        listener.resize(Display.getWidth(), Display.getHeight());
                }
                synchronized (runnables) {
                    while (runnables.notEmpty())
                        runnables.pop().run();
                }
                if (!running)
                    break;

                input.update();
                shouldRender |= Display.isDirty();
                input.processEvents();
                if (audio != null)
                    audio.update();
                if (shouldRender) {
                    updateTime();
                    listener.render(getDeltaTime());
                    Display.update();
                    if (vsync && config.useCPUSynch) {
                        Display.sync(60);
                    }
                } else {
                    // Effectively sleeps for a little while so we don't spend all available
                    // cpu power in an essentially empty loop.
                    Display.sync(60);
                }
            }

            Array<LifecycleListener> listeners = lifecycleListeners;
            synchronized (listeners) {
                for (LifecycleListener listener : listeners) {
                    listener.pause();
                    listener.dispose();
                }
            }
            listener.pause();
            listener.dispose();
            Display.destroy();
            if (audio != null)
                audio.dispose();
            if (config.forceExit)
                System.exit(-1);

        } catch (Throwable t) {
            if (audio != null)
                audio.dispose();
            throw new RuntimeException(t);
        }
    }

    @Override
    public Audio getAudio() {
        return audio;
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
    public Input getInput() {
        return input;
    }

    @Override
    public Net getNet() {
        return net;
    }

    public void stop() {
        running = false;
        try {
            mainLoopThread.join();
        } catch (Exception ex) {
        }
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
    public void debug(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        System.out.println(tag + ": " + message);
        exception.printStackTrace(System.out);
    }

    public void log(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        System.out.println(tag + ": " + message);
        exception.printStackTrace(System.out);
    }

    @Override
    public void error(String tag, String message) {
        System.err.println(tag + ": " + message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        System.err.println(tag + ": " + message);
        exception.printStackTrace(System.err);
    }

    @Override
    public void exit() {
        postRunnable(new Runnable() {
            @Override
            public void run() {
                running = false;
            }
        });
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
    public void restart() {
        // TODO Auto-generated method stub

    }

    public int getHeight() {
        return Display.getHeight();
    }

    public int getWidth() {
        return Display.getWidth();
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public int getFramesPerSecond() {
        return fps;
    }

    void updateTime() {
        long time = System.nanoTime();
        deltaTime = (time - lastTime) / 1000000000.0f;
        lastTime = time;

        if (time - frameStart >= 1000000000) {
            fps = frames;
            frames = 0;
            frameStart = time;
        }
        frames++;
    }

    public void setTitle(String title) {
        Display.setTitle(title);
    }

    @Override
    public void setVSync(boolean vsync) {
        this.vsync = vsync;
        if (vsync && !config.useCPUSynch)
            Display.setVSyncEnabled(true);
        if (!vsync && !config.useCPUSynch)
            Display.setVSyncEnabled(false);
    }

}
