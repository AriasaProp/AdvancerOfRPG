package com.ariasaproject.advancerofrpg;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import com.ariasaproject.advancerofrpg.files.Files.FileHandle;
import com.ariasaproject.advancerofrpg.files.Files.FileType;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

public class AndroidFileHandle extends FileHandle {
	final private AssetManager assets;

	AndroidFileHandle(AssetManager assets, String fileName, FileType type) {
		super(fileName.replace('\\', '/'), type);
		this.assets = assets;
	}

	AndroidFileHandle(AssetManager assets, File file, FileType type) {
		super(file, type);
		this.assets = assets;
	}

	@Override
	public FileHandle child(String name) {
		name = name.replace('\\', '/');
		if (file.getPath().length() == 0)
			return new AndroidFileHandle(assets, new File(name), type);
		return new AndroidFileHandle(assets, new File(file, name), type);
	}

	@Override
	public FileHandle sibling(String name) {
		name = name.replace('\\', '/');
		if (file.getPath().length() == 0)
			throw new RuntimeException("Cannot get the sibling of the root.");
		return GraphFunc.app.getFiles().getFileHandle(new File(file.getParent(), name).getPath(), type);
		// this way we can find the sibling even if it's inside the obb
	}

	@Override
	public FileHandle parent() {
		File parent = file.getParentFile();
		if (parent == null) {
			if (type == FileType.Absolute)
				parent = new File("/");
			else
				parent = new File("");
		}
		return new AndroidFileHandle(assets, parent, type);
	}

	@Override
	public InputStream read() {
		if (type == FileType.Internal) {
			try {
				return assets.open(file.getPath());
			} catch (IOException ex) {
				throw new RuntimeException("Error reading file: " + file + " (" + type + ")", ex);
			}
		}
		return super.read();
	}

	@Override
	public ByteBuffer map(FileChannel.MapMode mode) {
		if (type == FileType.Internal) {
			FileInputStream input = null;
			try {
				AssetFileDescriptor fd = getAssetFileDescriptor();
				long startOffset = fd.getStartOffset();
				long declaredLength = fd.getDeclaredLength();
				input = new FileInputStream(fd.getFileDescriptor());
				ByteBuffer map = input.getChannel().map(mode, startOffset, declaredLength);
				map.order(ByteOrder.nativeOrder());
				return map;
			} catch (Exception ex) {
				throw new RuntimeException("Error memory mapping file: " + this + " (" + type + ")", ex);
			} finally {
				try {
					input.close();
				} catch (Throwable ignored) {
				}
			}
		}
		return super.map(mode);
	}

	@Override
	public FileHandle[] list() {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				for (int i = 0, n = handles.length; i < n; i++)
					handles[i] = new AndroidFileHandle(assets, new File(file, relativePaths[i]), type);
				return handles;
			} catch (Exception ex) {
				throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list();
	}

	@Override
	public FileHandle[] list(FileFilter filter) {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				int count = 0;
				for (int i = 0, n = handles.length; i < n; i++) {
					String path = relativePaths[i];
					FileHandle child = new AndroidFileHandle(assets, new File(file, path), type);
					if (!filter.accept(child.file()))
						continue;
					handles[count] = child;
					count++;
				}
				if (count < relativePaths.length) {
					FileHandle[] newHandles = new FileHandle[count];
					System.arraycopy(handles, 0, newHandles, 0, count);
					handles = newHandles;
				}
				return handles;
			} catch (Exception ex) {
				throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list(filter);
	}

	@Override
	public FileHandle[] list(FilenameFilter filter) {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				int count = 0;
				for (int i = 0, n = handles.length; i < n; i++) {
					String path = relativePaths[i];
					if (!filter.accept(file, path))
						continue;
					handles[count] = new AndroidFileHandle(assets, new File(file, path), type);
					count++;
				}
				if (count < relativePaths.length) {
					FileHandle[] newHandles = new FileHandle[count];
					System.arraycopy(handles, 0, newHandles, 0, count);
					handles = newHandles;
				}
				return handles;
			} catch (Exception ex) {
				throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list(filter);
	}

	@Override
	public FileHandle[] list(String suffix) {
		if (type == FileType.Internal) {
			try {
				String[] relativePaths = assets.list(file.getPath());
				FileHandle[] handles = new FileHandle[relativePaths.length];
				int count = 0;
				for (int i = 0, n = handles.length; i < n; i++) {
					String path = relativePaths[i];
					if (!path.endsWith(suffix))
						continue;
					handles[count] = new AndroidFileHandle(assets, new File(file, path), type);
					count++;
				}
				if (count < relativePaths.length) {
					FileHandle[] newHandles = new FileHandle[count];
					System.arraycopy(handles, 0, newHandles, 0, count);
					handles = newHandles;
				}
				return handles;
			} catch (Exception ex) {
				throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
			}
		}
		return super.list(suffix);
	}

	@Override
	public boolean isDirectory() {
		if (type == FileType.Internal) {
			try {
				return assets.list(file.getPath()).length > 0;
			} catch (IOException ex) {
				return false;
			}
		}
		return super.isDirectory();
	}

	@Override
	public boolean exists() {
		if (type == FileType.Internal) {
			String fileName = file.getPath();
			try {
				assets.open(fileName).close(); // Check if file exists.
				return true;
			} catch (Exception ex) {
				// This is SUPER slow! but we need it for directories.
				try {
					return assets.list(fileName).length > 0;
				} catch (Exception ignored) {
				}
				return false;
			}
		}
		return super.exists();
	}

	@Override
	public long length() {
		if (type == FileType.Internal) {
			AssetFileDescriptor fileDescriptor = null;
			try {
				fileDescriptor = assets.openFd(file.getPath());
				return fileDescriptor.getLength();
			} catch (IOException ignored) {
			} finally {
				if (fileDescriptor != null) {
					try {
						fileDescriptor.close();
					} catch (IOException e) {
					}
					;
				}
			}
		}
		return super.length();
	}

	@Override
	public long lastModified() {
		return super.lastModified();
	}

	@Override
	public File file() {
		if (type == FileType.Local)
			return new File(GraphFunc.app.getFiles().getLocalStoragePath(), file.getPath());
		return super.file();
	}

	/**
	 * @return an AssetFileDescriptor for this file or null if the file is not of
	 *         type Internal
	 * @throws IOException - thrown by AssetManager.openFd()
	 */
	public AssetFileDescriptor getAssetFileDescriptor() throws IOException {
		return assets != null ? assets.openFd(path()) : null;
	}
}
