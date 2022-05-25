package com.ariasaproject.advancerofrpg.audio;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.assets.loaders.AsynchronousAssetLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.utils.Array;

public class MusicLoader extends AsynchronousAssetLoader<Music, MusicLoader.MusicParameter> {

    private Music music;

    public MusicLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    /**
     * Returns the {@link Music} instance currently loaded by this
     * {@link MusicLoader}.
     *
     * @return the currently loaded {@link Music}, otherwise {@code null} if no
     * {@link Music} has been loaded yet.
     */
    protected Music getLoadedMusic() {
        return music;
    }

    @Override
    public void loadAsync(AssetContainer manager, String fileName, FileHandle file, MusicParameter parameter) {
        music = GraphFunc.app.getAudio().newMusic(file);
    }

    @Override
    public Music loadSync(AssetContainer manager, String fileName, FileHandle file, MusicParameter parameter) {
        Music music = this.music;
        this.music = null;
        return music;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MusicParameter parameter) {
        return null;
    }

    static public class MusicParameter extends AssetLoaderParameters<Music> {
    }

}
