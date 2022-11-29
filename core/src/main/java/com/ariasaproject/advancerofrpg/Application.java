package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;

public interface Application {

    Graphics getGraphics();
    Audio getAudio();
    Input getInput();
    Files getFiles();
    Net getNet();
    Clipboard getClipboard();

    void log(String tag, String message);
    void log(String tag, String message, Throwable exception);
    void error(String tag, String message);
    void error(String tag, String message, Throwable exception);
    void debug(String tag, String message);
    void debug(String tag, String message, Throwable exception);
    
    void postRunnable(Runnable runnable);
    void restart();
    void exit();
    void addLifecycleListener(LifecycleListener listener);
    void removeLifecycleListener(LifecycleListener listener);
}
