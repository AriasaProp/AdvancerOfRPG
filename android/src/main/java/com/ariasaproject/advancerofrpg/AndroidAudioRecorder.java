package com.ariasaproject.advancerofrpg;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.ariasaproject.advancerofrpg.audio.AudioRecorder;

public class AndroidAudioRecorder implements AudioRecorder {
    private final AudioRecord recorder;

    public AndroidAudioRecorder(int samplingRate, boolean isMono) throws SecurityException {
        int channelConfig = isMono ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        int minBufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED)
            throw new RuntimeException("Unable to initialize AudioRecorder.\nDo you have the RECORD_AUDIO permission?");
        recorder.startRecording();
    }

    @Override
    public void dispose() {
        recorder.stop();
        recorder.release();
    }

    @Override
    public void read(short[] samples, int offset, int numSamples) {
        int read = 0;
        while (read != numSamples) {
            read += recorder.read(samples, offset + read, numSamples - read);
        }
    }

}
