package com.ariasaproject.advancerofrpg.assets.loaders.resolvers;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;

public class InternalFileHandleResolver implements FileHandleResolver {
	@Override
	public FileHandle resolve(String fileName) {
		return GraphFunc.app.getFiles().internal(fileName);
	}
}
