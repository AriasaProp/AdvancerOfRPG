package com.ariasaproject.advancerofrpg.assets;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.ariasaproject.advancerofrpg.assets.loaders.AssetLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.AsynchronousAssetLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.SynchronousAssetLoader;
import com.ariasaproject.advancerofrpg.files.Files.FileHandle;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;

class AssetLoadingTask implements Callable<Void> {
	final AssetDescriptor assetDesc;
	final AssetLoader loader;
	final ExecutorService executor;
	final long startTime;
	AssetContainer manager;
	volatile boolean asyncDone = false;
	volatile boolean dependenciesLoaded = false;
	volatile Array<AssetDescriptor> dependencies;
	volatile Future<Void> depsFuture = null;
	volatile Future<Void> loadFuture = null;
	volatile Disposable asset = null;

	int ticks = 0;
	volatile boolean cancel = false;

	public AssetLoadingTask(AssetContainer manager, AssetDescriptor assetDesc, AssetLoader loader,
							ExecutorService threadPool) {
		this.manager = manager;
		this.assetDesc = assetDesc;
		this.loader = loader;
		this.executor = threadPool;
		startTime = 0;
	}

	/**
	 * Loads parts of the asset asynchronously if the loader is an
	 * {@link AsynchronousAssetLoader}.
	 */
	@Override
	public Void call() throws Exception {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader) loader;
		if (!dependenciesLoaded) {
			dependencies = asyncLoader.getDependencies(assetDesc.fullPath, resolve(loader, assetDesc),
					assetDesc.params);
			if (dependencies != null) {
				removeDuplicates(dependencies);
				manager.injectDependencies(assetDesc.fullPath, dependencies);
			} else {
				// if we have no dependencies, we load the async part of the task immediately.
				asyncLoader.loadAsync(manager, assetDesc.fullPath, resolve(loader, assetDesc), assetDesc.params);
				asyncDone = true;
			}
		} else {
			asyncLoader.loadAsync(manager, assetDesc.fullPath, resolve(loader, assetDesc), assetDesc.params);
		}
		return null;
	}

	/**
	 * Updates the loading of the asset. In case the asset is loaded with an
	 * {@link AsynchronousAssetLoader}, the loaders
	 * {@link AsynchronousAssetLoader#loadAsync(AssetManager, String, FileHandle, AssetLoaderParameters)}
	 * method is first called on a worker thread. Once this method returns, the rest
	 * of the asset is loaded on the rendering thread via
	 * {@link AsynchronousAssetLoader#loadSync(AssetManager, String, FileHandle, AssetLoaderParameters)}.
	 *
	 * @return true in case the asset was fully loaded, false otherwise
	 * @throws RuntimeException
	 */
	public boolean update() {
		ticks++;
		if (loader instanceof SynchronousAssetLoader) {
			handleSyncLoader();
		} else {
			handleAsyncLoader();
		}
		return asset != null;
	}

	private void handleSyncLoader() {
		SynchronousAssetLoader syncLoader = (SynchronousAssetLoader) loader;
		if (!dependenciesLoaded) {
			dependenciesLoaded = true;
			dependencies = syncLoader.getDependencies(assetDesc.fullPath, resolve(loader, assetDesc), assetDesc.params);
			if (dependencies == null) {
				asset = syncLoader.load(manager, assetDesc.fullPath, resolve(loader, assetDesc), assetDesc.params);
				return;
			}
			removeDuplicates(dependencies);
			manager.injectDependencies(assetDesc.fullPath, dependencies);
		} else {
			asset = syncLoader.load(manager, assetDesc.fullPath, resolve(loader, assetDesc), assetDesc.params);
		}
	}

	private void handleAsyncLoader() {
		AsynchronousAssetLoader asyncLoader = (AsynchronousAssetLoader) loader;
		if (!dependenciesLoaded) {
			if (depsFuture == null) {
				depsFuture = executor.submit(this);
			} else {
				if (depsFuture.isDone()) {
					try {
						depsFuture.get();
					} catch (Exception e) {
						throw new RuntimeException("Couldn't load dependencies of asset: " + assetDesc.fullPath, e);
					}
					dependenciesLoaded = true;
					if (asyncDone) {
						asset = asyncLoader.loadSync(manager, assetDesc.fullPath, resolve(loader, assetDesc),
								assetDesc.params);
					}
				}
			}
		} else {
			if (loadFuture == null && !asyncDone) {
				loadFuture = executor.submit(this);
			} else {
				if (asyncDone) {
					asset = asyncLoader.loadSync(manager, assetDesc.fullPath, resolve(loader, assetDesc),
							assetDesc.params);
				} else if (loadFuture.isDone()) {
					try {
						loadFuture.get();
					} catch (Exception e) {
						throw new RuntimeException("Couldn't load asset: " + assetDesc.fullPath, e);
					}
					asset = asyncLoader.loadSync(manager, assetDesc.fullPath, resolve(loader, assetDesc),
							assetDesc.params);
				}
			}
		}
	}

	private FileHandle resolve(AssetLoader loader, AssetDescriptor assetDesc) {
		if (assetDesc.file == null)
			assetDesc.file = loader.resolve(assetDesc.fullPath);
		return assetDesc.file;
	}

	public Disposable getAsset() {
		return asset;
	}

	private void removeDuplicates(Array<AssetDescriptor> array) {
		boolean ordered = array.ordered;
		array.ordered = true;
		for (int i = 0; i < array.size; ++i) {
			final String fn = array.get(i).fullPath;
			final Class type = array.get(i).type;
			for (int j = array.size - 1; j > i; --j) {
				if (type == array.get(j).type && fn.equals(array.get(j).fullPath))
					array.removeIndex(j);
			}
		}
		array.ordered = ordered;
	}
}
