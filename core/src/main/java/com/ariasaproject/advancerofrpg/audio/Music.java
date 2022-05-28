package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.Files;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.assets.loaders.AsynchronousAssetLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public interface Music extends Disposable {
    public void play();

    public void pause();

    public void stop();

    public boolean isPlaying();

    public boolean isLooping();

    public void setLooping(boolean isLooping);

    public float getVolume();

    public void setVolume(float volume);

    public void setPan(float pan, float volume);

    public float getPosition();

    public void setPosition(float position);

    @Override
    public void dispose();

    public void setOnCompletionListener(Runnable listener);

    static public class MusicLoader extends AsynchronousAssetLoader<Music, MusicParameter> {

        private Music music;

        public MusicLoader(FileHandleResolver resolver) {
            super(resolver);
        }
        protected Music getLoadedMusic() {
            return music;
        }

        @Override
        public void loadAsync(AssetContainer manager, String fileName, Files.FileHandle file, MusicParameter parameter) {
            music = GraphFunc.app.getAudio().newMusic(file);
        }

        @Override
        public Music loadSync(AssetContainer manager, String fileName, Files.FileHandle file, MusicParameter parameter) {
            Music music = this.music;
            this.music = null;
            return music;
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, Files.FileHandle file, MusicParameter parameter) {
            return null;
        }
    }
    static public class MusicParameter extends AssetLoaderParameters<Music> {
    }
}
