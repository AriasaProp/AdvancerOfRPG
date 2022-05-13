package com.ariasaproject.advancerofrpg.assets.loaders;

import com.ariasaproject.advancerofrpg.files.Files.FileHandle;

public interface FileHandleResolver {
	FileHandle resolve(String fileName);
}
