package com.ariasaproject.advancerofrpg;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import com.ariasaproject.advancerofrpg.audio.AudioDevice;

class AndroidAudioDevice implements AudioDevice {
    private final AudioTrack track;
    private final boolean isMono;
    private final int latency;
    private short[] buffer = new short[1024];

    public AndroidAudioDevice(int samplingRate, boolean isMono) {
        this.isMono = isMono;
        int minSize = AudioTrack.getMinBufferSize(samplingRate,
                isMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        if (Build.VERSION.SDK_INT >= 23) {
            track = forAPI23(minSize, samplingRate);
        } else if (Build.VERSION.SDK_INT >= 21) {
            track = forAPI21(minSize, samplingRate);
        } else {
            track = forBase(minSize, samplingRate);
        }
        track.play();
        latency = minSize / (isMono ? 1 : 2);
    }

    @TargetApi(21)
    private final AudioTrack forAPI21(final int size, final int samplingRate) {
        AudioAttributes attributes = new AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        AudioFormat format = new AudioFormat.Builder()
                .setChannelMask(isMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(samplingRate).build();
        return new AudioTrack(attributes, format, size, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
    }

    @TargetApi(23)
    private final AudioTrack forAPI23(final int size, final int samplingRate) {
        AudioAttributes attributes = new AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        AudioFormat format = new AudioFormat.Builder()
                .setChannelMask(isMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(samplingRate).build();
        return new AudioTrack.Builder().setAudioAttributes(attributes).setAudioFormat(format).setBufferSizeInBytes(size)
                .setTransferMode(AudioTrack.MODE_STREAM).build();
    }

    @SuppressWarnings("deprecation")
    private final AudioTrack forBase(final int size, final int samplingRate) {
        return new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate,
                isMono ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                size, AudioTrack.MODE_STREAM);
    }

    @Override
    public void dispose() {
        track.stop();
        track.release();
    }

    @Override
    public boolean isMono() {
        return isMono;
    }

    @Override
    public void writeSamples(short[] samples, int offset, int numSamples) {
        int writtenSamples = track.write(samples, offset, numSamples);
        while (writtenSamples != numSamples)
            writtenSamples += track.write(samples, offset + writtenSamples, numSamples - writtenSamples);
    }

    @Override
    public void writeSamples(float[] samples, int offset, int numSamples) {
        if (buffer.length < samples.length)
            buffer = new short[samples.length];
        int bound = offset + numSamples;
        for (int i = offset, j = 0; i < bound; i++, j++) {
            float fValue = samples[i];
            if (fValue > 1)
                fValue = 1;
            if (fValue < -1)
                fValue = -1;
            short value = (short) (fValue * Short.MAX_VALUE);
            buffer[j] = value;
        }
        int writtenSamples = track.write(buffer, 0, numSamples);
        while (writtenSamples != numSamples)
            writtenSamples += track.write(buffer, writtenSamples, numSamples - writtenSamples);
    }

    @Override
    public int getLatency() {
        return latency;
    }

    @Override
    public void setVolume(float volume) {
        if (Build.VERSION.SDK_INT >= 21) {
            setVolume21(volume);
        } else {
            setVolumeBase(volume);
        }
    }

    @TargetApi(21)
    private final void setVolume21(float volume) {
        track.setVolume(volume);
    }

    @SuppressWarnings("deprecation")
    private final void setVolumeBase(float volume) {
        track.setStereoVolume(volume, volume);
    }

    @Override
    public void pause() {
        track.pause();
    }

    @Override
    public void resume() {
        track.play();
    }

}
