package com.ariasaproject.advancerofrpg.assets.loaders.resolvers;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;

public class LocalFileHandleResolver implements FileHandleResolver {
    @Override
    public FileHandle resolve(String fileName) {
        return GraphFunc.app.getFiles().local(fileName);
    }
}
