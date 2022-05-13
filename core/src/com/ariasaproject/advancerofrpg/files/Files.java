package com.ariasaproject.advancerofrpg.files;

import com.ariasaproject.advancerofrpg.LifecycleListener;

public interface Files extends LifecycleListener {
	public static enum FileType {
		Internal, External, Absolute, Local;
	}

	public abstract FileHandle getFileHandle(String path, FileType type);

	public abstract FileHandle internal(String path);

	public abstract FileHandle external(String path);

	public abstract FileHandle absolute(String path);

	public abstract FileHandle local(String path);

	public abstract String getExternalStoragePath();

	public abstract boolean isExternalStorageAvailable();

	public abstract String getLocalStoragePath();

	public abstract boolean isLocalStorageAvailable();
}
