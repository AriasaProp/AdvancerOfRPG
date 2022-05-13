package com.ariasaproject.advancerofrpg;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class AndroidFiles extends Files {
	protected final String sdcard;
	protected final String localpath;
	protected final AssetManager assets;
	private ZipResourceFile expansionFile = null;

	public AndroidFiles(AssetManager assets, String sdcard) {
		this.sdcard = sdcard;
		this.assets = assets;
		localpath = sdcard;
	}

	public AndroidFiles(AssetManager assets, String localpath, String sdcard) {
		this.sdcard = sdcard;
		this.assets = assets;
		this.localpath = localpath.endsWith("/") ? localpath : localpath + "/";
	}

	@Override
	public FileHandle getFileHandle(String path, FileType type) {
		FileHandle handle = new AndroidFileHandle(type == FileType.Internal ? assets : null, path, type);
		if (expansionFile != null && type == FileType.Internal)
			handle = getZipFileHandleIfExists(handle, path);
		return handle;
	}

	private FileHandle getZipFileHandleIfExists(FileHandle handle, String path) {
		try {
			assets.open(path).close(); // Check if file exists.
			return handle;
		} catch (Exception ex) {
			// try APK expansion instead
			FileHandle zipHandle = new AndroidZipFileHandle(path);
			if (!zipHandle.isDirectory())
				return zipHandle;
			else if (zipHandle.exists())
				return zipHandle;
		}
		return handle;
	}

	@Override
	public FileHandle internal(String path) {
		FileHandle handle = new AndroidFileHandle(assets, path, FileType.Internal);
		if (expansionFile != null)
			handle = getZipFileHandleIfExists(handle, path);
		return handle;
	}

	@Override
	public FileHandle external(String path) {
		return new AndroidFileHandle(null, path, FileType.External);
	}

	@Override
	public FileHandle absolute(String path) {
		return new AndroidFileHandle(null, path, FileType.Absolute);
	}

	@Override
	public FileHandle local(String path) {
		return new AndroidFileHandle(null, path, FileType.Local);
	}

	@Override
	public String getExternalStoragePath() {
		return sdcard;
	}

	@Override
	public boolean isExternalStorageAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	@Override
	public String getLocalStoragePath() {
		return localpath;
	}

	@Override
	public boolean isLocalStorageAvailable() {
		return true;
	}

	public boolean setAPKExpansion(int mainVersion, int patchVersion) {
		try {
			Context context;
			if (GraphFunc.app instanceof Activity) {
				context = ((Activity) GraphFunc.app).getBaseContext();
			} else {
				throw new RuntimeException("APK expansion not supported for application type");
			}
			expansionFile = APKExpansionSupport.getAPKExpansionZipFile(context, mainVersion, patchVersion);
		} catch (IOException ex) {
			throw new RuntimeException("APK expansion main version " + mainVersion + " or patch version " + patchVersion
					+ " couldn't be opened!");
		}
		return expansionFile != null;
	}

	public ZipResourceFile getExpansionFile() {
		return expansionFile;
	}

	@Override
	public void resume() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void dispose() {
	}
}
