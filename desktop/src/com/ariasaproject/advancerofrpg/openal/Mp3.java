package com.ariasaproject.advancerofrpg.openal;

import java.io.ByteArrayOutputStream;

import com.ariasaproject.advancerofrpg.files.FileHandle;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.MP3Decoder;
import javazoom.jl.decoder.OutputBuffer;

public class Mp3 {
	static public class Music extends OpenALMusic {
		private Bitstream bitstream;
		private OutputBuffer outputBuffer;
		private MP3Decoder decoder;

		public Music (OpenALAudio audio, FileHandle file) {
			super(audio, file);
			if (audio.noDevice) return;
			bitstream = new Bitstream(file.read());
			decoder = new MP3Decoder();
			try {
				Header header = bitstream.readFrame();
				if (header == null) throw new RuntimeException("empty ogg");
				int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
				outputBuffer = new OutputBuffer(channels, false);
				decoder.setOutputBuffer(outputBuffer);
				setup(channels, header.getSampleRate());
			} catch (BitstreamException e) {
				throw new RuntimeException("error while preloading mp3", e);
			}
		}

		@Override
		public int read (byte[] buffer) {
			try {
				boolean setup = bitstream == null;
				if (setup) {
					bitstream = new Bitstream(file.read());
					decoder = new MP3Decoder();
				}

				int totalLength = 0;
				int minRequiredLength = buffer.length - OutputBuffer.BUFFERSIZE * 2;
				while (totalLength <= minRequiredLength) {
					Header header = bitstream.readFrame();
					if (header == null) break;
					if (setup) {
						int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
						outputBuffer = new OutputBuffer(channels, false);
						decoder.setOutputBuffer(outputBuffer);
						setup(channels, header.getSampleRate());
						setup = false;
					}
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
						// JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
					}
					bitstream.closeFrame();

					int length = outputBuffer.reset();
					System.arraycopy(outputBuffer.getBuffer(), 0, buffer, totalLength, length);
					totalLength += length;
				}
				return totalLength;
			} catch (Throwable ex) {
				reset();
				throw new RuntimeException("Error reading audio data.", ex);
			}
		}

		@Override
		public void reset () {
			if (bitstream == null) return;
			try {
				bitstream.close();
			} catch (BitstreamException ignored) {
			}
			bitstream = null;
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
		// Note: This uses a slightly modified version of JLayer.

		public Sound (OpenALAudio audio, FileHandle file) {
			super(audio);
			if (audio.noDevice) return;
			ByteArrayOutputStream output = new ByteArrayOutputStream(4096);

			Bitstream bitstream = new Bitstream(file.read());
			MP3Decoder decoder = new MP3Decoder();

			try {
				OutputBuffer outputBuffer = null;
				int sampleRate = -1, channels = -1;
				while (true) {
					Header header = bitstream.readFrame();
					if (header == null) break;
					if (outputBuffer == null) {
						channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
						outputBuffer = new OutputBuffer(channels, false);
						decoder.setOutputBuffer(outputBuffer);
						sampleRate = header.getSampleRate();
					}
					try {
						decoder.decodeFrame(header, bitstream);
					} catch (Exception ignored) {
						// JLayer's decoder throws ArrayIndexOutOfBoundsException sometimes!?
					}
					bitstream.closeFrame();
					output.write(outputBuffer.getBuffer(), 0, outputBuffer.reset());
				}
				bitstream.close();
				setup(output.toByteArray(), channels, sampleRate);
			} catch (Throwable ex) {
				throw new RuntimeException("Error reading audio data.", ex);
			}
		}
	}
}
