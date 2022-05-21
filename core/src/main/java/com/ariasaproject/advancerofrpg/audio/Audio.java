package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.Files.FileHandle;

public interface Audio {
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono);

    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono);

    public Sound newSound(FileHandle fileHandle);

    public Music newMusic(FileHandle file);

    public void disposeMusic(Music music);
}
