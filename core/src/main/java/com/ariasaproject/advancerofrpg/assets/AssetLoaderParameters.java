package com.ariasaproject.advancerofrpg.assets;

public class AssetLoaderParameters<T> {

	public LoadedCallback loadedCallback;

	/**
	 * Callback interface that will be invoked when the {@link AssetManager} loaded
	 * an asset.
	 *
	 * @author mzechner
	 */
	public interface LoadedCallback {
		void finishedLoading(AssetContainer assetManager, String fileName, Class type);
	}
}
