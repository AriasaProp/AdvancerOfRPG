package com.ariasaproject.advancerofrpg.openal;

import com.ariasaproject.advancerofrpg.files.FileHandle;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;

public class Wav {
    static public class Music extends OpenALMusic {
        private WavInputStream input;

        public Music(OpenALAudio audio, FileHandle file) {
            super(audio, file);
            input = new WavInputStream(file);
            if (audio.noDevice) return;
            setup(input.channels, input.sampleRate);
        }

        @Override
        public int read(byte[] buffer) {
            if (input == null) {
                input = new WavInputStream(file);
                setup(input.channels, input.sampleRate);
            }
            try {
                return input.readData(buffer);
            } catch (IOException ex) {
                throw new RuntimeException("Error reading WAV file: " + file, ex);
            }
        }

        @Override
        public void reset() {
            if (input == null) return;
            try {
                input.close();
            } catch (IOException ignored) {
            }
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

            WavInputStream input = new WavInputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            try {
                byte[] buffer = new byte[2048];
                while (true) {
                    int length = input.readData(buffer);
                    if (length == -1) break;
                    output.write(buffer, 0, length);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error reading WAV file: " + file, ex);
            }
            setup(output.toByteArray(), input.channels, input.sampleRate);
        }
    }

    /**
     * @author Nathan Sweet
     */
    static private class WavInputStream extends FilterInputStream {
        int channels, sampleRate, dataRemaining;

        WavInputStream(FileHandle file) {
            super(file.read());
            try {
                if (read() != 'R' || read() != 'I' || read() != 'F' || read() != 'F')
                    throw new RuntimeException("RIFF header not found: " + file);

                skipFully(4);

                if (read() != 'W' || read() != 'A' || read() != 'V' || read() != 'E')
                    throw new RuntimeException("Invalid wave file header: " + file);

                int fmtChunkLength = seekToChunk('f', 'm', 't', ' ');

                int type = read() & 0xff | (read() & 0xff) << 8;
                if (type != 1) throw new RuntimeException("WAV files must be PCM: " + type);

                channels = read() & 0xff | (read() & 0xff) << 8;
                if (channels != 1 && channels != 2)
                    throw new RuntimeException("WAV files must have 1 or 2 channels: " + channels);

                sampleRate = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

                skipFully(6);

                int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
                if (bitsPerSample != 16)
                    throw new RuntimeException("WAV files must have 16 bits per sample: " + bitsPerSample);

                skipFully(fmtChunkLength - 16);

                dataRemaining = seekToChunk('d', 'a', 't', 'a');
            } catch (Throwable ex) {
                try {
                    close();
                } catch (IOException ignored) {
                }
                throw new RuntimeException("Error reading WAV file: " + file, ex);
            }
        }

        private int seekToChunk(char c1, char c2, char c3, char c4) throws IOException {
            while (true) {
                boolean found = read() == c1;
                found &= read() == c2;
                found &= read() == c3;
                found &= read() == c4;
                int chunkLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
                if (chunkLength == -1)
                    throw new IOException("Chunk not found: " + c1 + c2 + c3 + c4);
                if (found) return chunkLength;
                skipFully(chunkLength);
            }
        }

        private void skipFully(int count) throws IOException {
            while (count > 0) {
                long skipped = in.skip(count);
                if (skipped <= 0) throw new EOFException("Unable to skip.");
                count -= skipped;
            }
        }

        public int readData(byte[] buffer) throws IOException {
            if (dataRemaining == 0) return -1;
            int length = Math.min(read(buffer), dataRemaining);
            if (length == -1) return -1;
            dataRemaining -= length;
            return length;
        }
    }
}
