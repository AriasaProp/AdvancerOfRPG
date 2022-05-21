package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.audio.Sound;
import com.ariasaproject.advancerofrpg.utils.Array;

public class SoundLoader extends AsynchronousAssetLoader<Sound, SoundLoader.SoundParameter> {
    private Sound sound;

    public SoundLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    protected Sound getLoadedSound() {
        return sound;
    }

    @Override
    public void loadAsync(AssetContainer manager, String fileName, FileHandle file, SoundParameter parameter) {
        sound = GraphFunc.app.getAudio().newSound(file);
    }

    @Override
    public Sound loadSync(AssetContainer manager, String fileName, FileHandle file, SoundParameter parameter) {
        Sound sound = this.sound;
        this.sound = null;
        return sound;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SoundParameter parameter) {
        return null;
    }

    static public class SoundParameter extends AssetLoaderParameters<Sound> {
    }

}
