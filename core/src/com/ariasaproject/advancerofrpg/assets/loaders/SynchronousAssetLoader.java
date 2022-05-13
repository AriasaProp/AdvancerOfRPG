package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.files.Files.FileHandle;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public abstract class SynchronousAssetLoader<T extends Disposable, P extends AssetLoaderParameters<T>> extends AssetLoader<T, P> {
	public SynchronousAssetLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public abstract T load(AssetContainer assetManager, String fileName, FileHandle file, P parameter);
}
