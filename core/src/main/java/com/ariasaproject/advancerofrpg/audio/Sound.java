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

public interface Sound extends Disposable {
    public long play();

    public long play(float volume);

    public long play(float volume, float pitch, float pan);

    public long loop();

    public long loop(float volume);

    public long loop(float volume, float pitch, float pan);

    public void stop();

    public void pause();

    public void resume();

    @Override
    public void dispose();

    public void stop(long soundId);

    public void pause(long soundId);

    public void resume(long soundId);

    public void setLooping(long soundId, boolean looping);

    public void setPitch(long soundId, float pitch);

    public void setVolume(long soundId, float volume);

    public void setPan(long soundId, float pan, float volume);
    public class SoundLoader extends AsynchronousAssetLoader<Sound, SoundParameter> {
        private Sound sound;

        public SoundLoader(FileHandleResolver resolver) {
            super(resolver);
        }

        protected Sound getLoadedSound() {
            return sound;
        }

        @Override
        public void loadAsync(AssetContainer manager, String fileName, Files.FileHandle file, SoundParameter parameter) {
            sound = GraphFunc.app.getAudio().newSound(file);
        }

        @Override
        public Sound loadSync(AssetContainer manager, String fileName, Files.FileHandle file, SoundParameter parameter) {
            Sound sound = this.sound;
            this.sound = null;
            return sound;
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, Files.FileHandle file, SoundParameter parameter) {
            return null;
        }
    }
    static public class SoundParameter extends AssetLoaderParameters<Sound> {
    }
}
