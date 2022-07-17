package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.audio.AudioDevice;
import com.ariasaproject.advancerofrpg.audio.AudioRecorder;
import com.ariasaproject.advancerofrpg.audio.Music;
import com.ariasaproject.advancerofrpg.audio.Sound;

public class LWJGLAudio implements Audio {

	@Override
	public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sound newSound(FileHandle fileHandle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Music newMusic(FileHandle file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disposeMusic(Music music) {
		// TODO Auto-generated method stub

	}

}
