package com.ariasaproject.advancerofrpg;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.audio.AudioDevice;
import com.ariasaproject.advancerofrpg.audio.AudioRecorder;
import com.ariasaproject.advancerofrpg.audio.Music;
import com.ariasaproject.advancerofrpg.audio.Sound;
import com.ariasaproject.advancerofrpg.files.Files.FileHandle;
import com.ariasaproject.advancerofrpg.files.Files.FileType;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

public final class AndroidAudio implements Audio {
	private final SoundPool soundPool;
	private final AudioManager manager;
	private final List<AndroidMusic> musics = new ArrayList<AndroidMusic>();
	boolean isPaused;

	public AndroidAudio(AndroidApplication context) {
		if (Build.VERSION.SDK_INT >= 21) {
			soundPool = Pool21();
		} else {
			// srcQuality: the sample-rate converter quality. Currently has no effect. Use 0
			// for the default.
			soundPool = PoolBase();
		}
		manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		context.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@SuppressWarnings("deprecation")
	private final SoundPool PoolBase() {
		return new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	}

	@TargetApi(21)
	private final SoundPool Pool21() {
		AudioAttributes audioAttrib = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
		return new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(1).build();
	}

	public void onPause() {
		if (isPaused)
			return;
		synchronized (musics) {
			for (AndroidMusic music : musics) {
				if (music.isPlaying()) {
					music.pause();
					music.wasPlaying = true;
				} else
					music.wasPlaying = false;
			}
		}
		this.soundPool.autoPause();
		isPaused = true;
	}

	public void onResume() {
		if (!isPaused)
			return;
		synchronized (musics) {
			for (int i = 0; i < musics.size(); i++) {
				if (musics.get(i).wasPlaying)
					musics.get(i).play();
			}
		}
		this.soundPool.autoResume();
		isPaused = false;
	}

	@Override
	public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
		return new AndroidAudioDevice(samplingRate, isMono);
	}

	@Override
	public Music newMusic(FileHandle file) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		if (file.type() == FileType.Internal) {
			try {
				AssetFileDescriptor descriptor = ((AndroidFileHandle) file).getAssetFileDescriptor();
				mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(),
						descriptor.getLength());
				descriptor.close();
				mediaPlayer.prepare();
				AndroidMusic music = new AndroidMusic(this, mediaPlayer);
				synchronized (musics) {
					musics.add(music);
				}
				return music;
			} catch (Exception ex) {
				throw new RuntimeException("Error loading audio file: " + file
						+ "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {
				mediaPlayer.setDataSource(file.file().getPath());
				mediaPlayer.prepare();
				AndroidMusic music = new AndroidMusic(this, mediaPlayer);
				synchronized (musics) {
					musics.add(music);
				}
				return music;
			} catch (Exception ex) {
				throw new RuntimeException("Error loading audio file: " + file, ex);
			}
		}

	}

	public Music newMusic(FileDescriptor fd) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(fd);
			mediaPlayer.prepare();
			AndroidMusic music = new AndroidMusic(this, mediaPlayer);
			synchronized (musics) {
				musics.add(music);
			}
			return music;
		} catch (Exception ex) {
			throw new RuntimeException("Error loading audio from FileDescriptor", ex);
		}
	}

	@Override
	public Sound newSound(FileHandle file) {
		if (file.type() == FileType.Internal) {
			try {
				AssetFileDescriptor descriptor = ((AndroidFileHandle) file).getAssetFileDescriptor();
				Sound sound = new AndroidSound(soundPool, manager, soundPool.load(descriptor, 1));
				descriptor.close();
				return sound;
			} catch (IOException ex) {
				throw new RuntimeException("Error loading audio file: " + file
						+ "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {
				return new AndroidSound(soundPool, manager, soundPool.load(file.file().getPath(), 1));
			} catch (Exception ex) {
				throw new RuntimeException("Error loading audio file: " + file, ex);
			}
		}
	}

	@Override
	public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
		return new AndroidAudioRecorder(samplingRate, isMono);
	}

	public void onDestroy() {
		synchronized (musics) {
			ArrayList<Music> musicsCopy = new ArrayList<Music>(musics);
			for (Music music : musicsCopy) {
				music.dispose();
			}
		}
		soundPool.release();
	}

	public void notifyMusicDisposed(Music music) {
		synchronized (musics) {
			musics.remove(music);
		}
	}
}
