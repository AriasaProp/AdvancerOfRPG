package com.ariasaproject.advancerofrpg.files;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.ariasaproject.advancerofrpg.files.Files.FileType;

public abstract class FileHandleStream extends FileHandle {
	/** Create an {@link FileType#Absolute} file at the given location. */
	public FileHandleStream(String path) {
		super(new File(path), FileType.Absolute);
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public FileHandle child(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileHandle sibling(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileHandle parent() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream read() {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream write(boolean overwrite) {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileHandle[] list() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mkdirs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean deleteDirectory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void copyTo(FileHandle dest) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void moveTo(FileHandle dest) {
		throw new UnsupportedOperationException();
	}
}
