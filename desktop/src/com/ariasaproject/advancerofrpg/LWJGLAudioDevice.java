package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.audio.AudioDevice;

public class LWJGLAudioDevice implements AudioDevice {
	boolean mono;
	int samplingRate;

	public LWJGLAudioDevice(int samplingRate, boolean mono) {
		super();
		this.mono = mono;
		this.samplingRate = samplingRate;
	}

	@Override
	public boolean isMono() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void writeSamples(short[] samples, int offset, int numSamples) {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeSamples(float[] samples, int offset, int numSamples) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLatency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVolume(float volume) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

}
