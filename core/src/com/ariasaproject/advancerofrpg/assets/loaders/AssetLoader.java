package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.files.FileHandle;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;

/**
 * Abstract base class for asset loaders.
 *
 * @param <T> the class of the asset the loader supports
 * @param <P> the class of the loading parameters the loader supports.
 * @author mzechner
 */
public abstract class AssetLoader<T extends Disposable, P extends AssetLoaderParameters<T>> {
	/**
	 * {@link FileHandleResolver} used to map from plain asset names to
	 * {@link FileHandle} instances
	 **/
	private final FileHandleResolver resolver;

	/**
	 * Constructor, sets the {@link FileHandleResolver} to use to resolve the file
	 * associated with the asset name.
	 *
	 * @param resolver
	 */
	public AssetLoader(FileHandleResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * @param fileName file name to resolve
	 * @return handle to the file, as resolved by the {@link FileHandleResolver} set
	 *         on the loader
	 */
	public FileHandle resolve(String fileName) {
		return resolver.resolve(fileName);
	}

	/**
	 * Returns the assets this asset requires to be loaded first. This method may be
	 * called on a thread other than the GL thread.
	 *
	 * @param fileName  name of the asset to load
	 * @param file      the resolved file to load
	 * @param parameter parameters for loading the asset
	 * @return other assets that the asset depends on and need to be loaded first or
	 *         null if there are no dependencies.
	 */
	public abstract Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, P parameter);
}
