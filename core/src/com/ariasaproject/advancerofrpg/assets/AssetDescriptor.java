package com.ariasaproject.advancerofrpg.assets;

import com.ariasaproject.advancerofrpg.files.FileHandle;
import com.ariasaproject.advancerofrpg.utils.Disposable;

public class AssetDescriptor<T extends Disposable> {
	public final String fullPath;
	public final Class<T> type;
	public final AssetLoaderParameters<T> params;
	public FileHandle file;

	public AssetDescriptor(String fullPath, Class<T> assetType) {
		this(fullPath, assetType, null);
	}

	public AssetDescriptor(FileHandle file, Class<T> assetType) {
		this(file, assetType, null);
	}

	public AssetDescriptor(FileHandle file, Class<T> assetType, AssetLoaderParameters<T> params) {
		this(file.path(), assetType, params);
		this.file = file;
	}
	public AssetDescriptor(String fullPath, Class<T> assetType, AssetLoaderParameters<T> params) {
		this.fullPath = fullPath.replaceAll("\\\\", "/");
		this.type = assetType;
		this.params = params;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof AssetDescriptor)) return false;
		AssetDescriptor o = (AssetDescriptor) obj;

		return this.fullPath.equals(o.fullPath) && this.type.equals(o.type) && this.params.equals(o.params) || this.file.equals(o.file);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(fullPath);
		sb.append(", ");
		sb.append(type.getName());
		return sb.toString();
	}
}
