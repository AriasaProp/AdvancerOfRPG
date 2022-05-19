package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.net.Net;

public interface Application {
    static {

        try {
            final String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win"))
                System.loadLibrary("ext");
            else if (osName.contains("mac"))
                System.loadLibrary("ext");
            else if (osName.contains("unix") || osName.contains("nux"))
                System.loadLibrary("ext");
            else if (osName.contains("android"))
                System.loadLibrary("ext");
        } catch (Exception e) {
            System.out.println("failed to load library : " + osName);
            System.err.println("failed load library");
            System.exit(0);
        }
    }

    public Graphics getGraphics();

    public Audio getAudio();

    public Input getInput();

    public Files getFiles();

    public Net getNet();

    public Clipboard getClipboard();

    public void log(String tag, String message);

    public void log(String tag, String message, Throwable exception);

    public void error(String tag, String message);

    public void error(String tag, String message, Throwable exception);

    public void debug(String tag, String message);

    public void debug(String tag, String message, Throwable exception);

    public void postRunnable(Runnable runnable);

    public void restart();

    public void exit();

    public void addLifecycleListener(LifecycleListener listener);

    public void removeLifecycleListener(LifecycleListener listener);
}
