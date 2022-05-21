package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.files.Files.FileType;
import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.utils.Array;

import java.awt.Graphics;

public class LwjglApplicationConfiguration {
    public int samples = 0;
    /**
     * width & height of application window
     **/
    public int width = 480, height = 320;
    /**
     * x & y of application window, -1 for center
     **/
    public int x = -1, y = -1;
    /**
     * fullscreen
     **/
    public boolean fullscreen = false;
    /**
     * whether to use CPU synching. If this is false display vsynching is used, which might not work in windowed mode
     **/
    public boolean useCPUSynch = true;
    /**
     * whether to enable vsync, can be changed at runtime via {@link Graphics#setVSync(boolean)}
     **/
    public boolean vSyncEnabled = true;
    /**
     * title of application
     **/
    public String title = "Lwjgl Application";
    /**
     * whether to call System.exit() on tear-down. Needed for Webstarts on some versions of Mac OS X it seems
     **/
    public boolean forceExit = true;
    /**
     * whether the window is resizable
     **/
    public boolean resizable = true;
    /**
     * the audio device buffer size in samples
     **/
    public int audioDeviceBufferSize = 512;
    /**
     * the audio device buffer count
     **/
    public int audioDeviceBufferCount = 9;
    public Color initialBackgroundColor = Color.BLACK;

    Array<String> iconPaths = new Array<String>();
    Array<FileType> iconFileTypes = new Array<FileType>();

    /**
     * Adds a window icon. Icons are tried in the order added, the first one that works is used. Typically three icons should be
     * provided: 128x128 (for Mac), 32x32 (for Windows and Linux), and 16x16 (for Windows).
     */
    public void addIcon(String path, FileType fileType) {
        iconPaths.add(path);
        iconFileTypes.add(fileType);
    }
}
