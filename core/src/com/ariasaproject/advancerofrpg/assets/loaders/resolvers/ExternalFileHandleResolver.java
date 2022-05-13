package com.ariasaproject.advancerofrpg.assets.loaders.resolvers;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.files.Files.FileHandle;

public class ExternalFileHandleResolver implements FileHandleResolver {
	@Override
	public FileHandle resolve(String fileName) {
		return GraphFunc.app.getFiles().external(fileName);
	}
}
