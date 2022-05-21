package com.ariasaproject.advancerofrpg.openal;

import com.ariasaproject.advancerofrpg.files.FileHandle;

import java.io.ByteArrayOutputStream;

public class Ogg {
    static public class Music extends OpenALMusic {
        private OggInputStream input;

        public Music(OpenALAudio audio, FileHandle file) {
            super(audio, file);
            if (audio.noDevice) return;
            input = new OggInputStream(file.read());
            setup(input.getChannels(), input.getSampleRate());
        }

        @Override
        public int read(byte[] buffer) {
            if (input == null) {
                input = new OggInputStream(file.read());
                setup(input.getChannels(), input.getSampleRate());
            }
            return input.read(buffer);
        }

        @Override
        public void reset() {
            if (input == null) return;
            input.close();
            input = null;
        }

        @Override
        public float getVolume() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setPan(float pan, float volume) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPosition(float position) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setOnCompletionListener(Runnable listener) {
            // TODO Auto-generated method stub

        }
    }

    static public class Sound extends OpenALSound {
        public Sound(OpenALAudio audio, FileHandle file) {
            super(audio);
            if (audio.noDevice) return;
            OggInputStream input = new OggInputStream(file.read());
            ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            byte[] buffer = new byte[2048];
            while (!input.atEnd()) {
                int length = input.read(buffer);
                if (length == -1) break;
                output.write(buffer, 0, length);
            }
            setup(output.toByteArray(), input.getChannels(), input.getSampleRate());
        }
    }
}
