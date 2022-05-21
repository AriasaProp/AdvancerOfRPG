package com.ariasaproject.advancerofrpg.graphics;

public interface Graphics {
    public static final String TAG = "Graphics";

    int getWidth();

    int getHeight();

    float getDeltaTime();

    int getFramesPerSecond();

    void setVSync(boolean vsync);
}
