package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.utils.Disposable;

public interface AudioDevice extends Disposable {
	public boolean isMono();

	public void writeSamples(short[] samples, int offset, int numSamples);

	public void writeSamples(float[] samples, int offset, int numSamples);

	public int getLatency();

	@Override
	public void dispose();

	public void setVolume(float volume);

	public void pause();

	public void resume();
}
