package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public abstract class AsynchronousAssetLoader<T extends Disposable, P extends AssetLoaderParameters<T>> extends AssetLoader<T, P> {

	public AsynchronousAssetLoader(FileHandleResolver resolver) {
		super(resolver);
	}
	public abstract void loadAsync(AssetContainer manager, String fileName, FileHandle file, P parameter);
	public abstract T loadSync(AssetContainer manager, String fileName, FileHandle file, P parameter);
}
