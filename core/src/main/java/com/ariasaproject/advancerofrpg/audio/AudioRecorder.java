package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.utils.Disposable;

public interface AudioRecorder extends Disposable {
    public void read(short[] samples, int offset, int numSamples);

    @Override
    public void dispose();
}
