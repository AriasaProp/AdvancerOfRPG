package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.files.FileHandle;
import com.ariasaproject.advancerofrpg.files.Files;

public final class LwjglFiles implements Files {
	static public final String externalPath = System.getProperty("user.home") + "/";

	@Override
	public FileHandle getFileHandle (String fileName, FileType type) {
		return new LwjglFileHandle(fileName, type);
	}

	@Override
	public FileHandle internal (String path) {
		return new LwjglFileHandle(path, FileType.Internal);
	}

	@Override
	public FileHandle external (String path) {
		return new LwjglFileHandle(path, FileType.External);
	}

	@Override
	public FileHandle absolute (String path) {
		return new LwjglFileHandle(path, FileType.Absolute);
	}

	@Override
	public FileHandle local (String path) {
		return new LwjglFileHandle(path, FileType.Local);
	}

	@Override
	public String getExternalStoragePath () {
		return externalPath;
	}

	@Override
	public boolean isExternalStorageAvailable () {
		return true;
	}

	@Override
	public String getLocalStoragePath () {
		return "";
	}

	@Override
	public boolean isLocalStorageAvailable () {
		return true;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
