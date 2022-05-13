package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.utils.Disposable;

public interface Music extends Disposable {
	public void play();

	public void pause();

	public void stop();

	public boolean isPlaying();

	public void setLooping(boolean isLooping);

	public boolean isLooping();

	public void setVolume(float volume);

	public float getVolume();

	public void setPan(float pan, float volume);

	public void setPosition(float position);

	public float getPosition();

	@Override
	public void dispose();

	public void setOnCompletionListener(Runnable listener);
}
