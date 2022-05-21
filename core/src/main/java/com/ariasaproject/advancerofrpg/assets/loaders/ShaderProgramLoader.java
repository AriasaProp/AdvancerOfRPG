package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.utils.Array;

public class ShaderProgramLoader extends AsynchronousAssetLoader<ShaderProgram, AssetLoaderParameters<ShaderProgram>> {

    public ShaderProgramLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<ShaderProgram> parameter) {
        return null;
    }

    @Override
    public void loadAsync(AssetContainer manager, String fileName, FileHandle file, AssetLoaderParameters<ShaderProgram> parameter) {
    }

    @Override
    public ShaderProgram loadSync(AssetContainer manager, String fileName, FileHandle file, AssetLoaderParameters<ShaderProgram> parameter) {
        try {
            return new ShaderProgram(file);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("ShaderProgram " + fileName + " failed to compile! cause " + e);
        }
    }
}
