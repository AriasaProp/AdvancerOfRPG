package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.utils.Disposable;

public interface Music extends Disposable {
    public void play();

    public void pause();

    public void stop();

    public boolean isPlaying();

    public boolean isLooping();

    public void setLooping(boolean isLooping);

    public float getVolume();

    public void setVolume(float volume);

    public void setPan(float pan, float volume);

    public float getPosition();

    public void setPosition(float position);

    @Override
    public void dispose();

    public void setOnCompletionListener(Runnable listener);
}
