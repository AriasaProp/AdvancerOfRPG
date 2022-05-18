package com.ariasaproject.advancerofrpg;

import android.content.res.AssetFileDescriptor;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.Files.FileType;
import com.ariasaproject.advancerofrpg.ZipResourceFile.ZipEntryRO;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

public class AndroidZipFileHandle extends AndroidFileHandle {
    private boolean hasAssetFd;
    private long fdLength;
    private ZipResourceFile expansionFile;
    private String path;

    public AndroidZipFileHandle(String fileName) {
        super(null, fileName, FileType.Internal);
        initialize();
    }

    public AndroidZipFileHandle(File file, FileType type) {
        super(null, file, type);
        initialize();
    }

    private void initialize() {
        path = file.getPath().replace('\\', '/');
        expansionFile = ((AndroidFiles) GraphFunc.app.getFiles()).getExpansionFile();
        AssetFileDescriptor assetFd = expansionFile.getAssetFileDescriptor(getPath());
        if (assetFd != null) {
            hasAssetFd = true;
            fdLength = assetFd.getLength();
            try {
                assetFd.close();
            } catch (IOException e) {
            }
        } else {
            hasAssetFd = false;
        }

        // needed for listing entries and exists() of directories
        if (isDirectory())
            path += "/";
    }

    @Override
    public AssetFileDescriptor getAssetFileDescriptor() throws IOException {
        return expansionFile.getAssetFileDescriptor(getPath());
    }

    private String getPath() {
        return path;
    }

    @Override
    public InputStream read() {
        InputStream input = null;

        try {
            input = expansionFile.getInputStream(getPath());
        } catch (IOException ex) {
            throw new RuntimeException("Error reading file: " + file + " (ZipResourceFile)", ex);
        }
        return input;
    }

    @Override
    public FileHandle child(String name) {
        if (file.getPath().length() == 0)
            return new AndroidZipFileHandle(new File(name), type);
        return new AndroidZipFileHandle(new File(file, name), type);
    }

    @Override
    public FileHandle sibling(String name) {
        if (file.getPath().length() == 0)
            throw new RuntimeException("Cannot get the sibling of the root.");
        return GraphFunc.app.getFiles().getFileHandle(new File(file.getParent(), name).getPath(), type); // this way we
        // can find
        // the
        // sibling
        // even
        // if it's
        // not
        // inside
        // the obb
    }

    @Override
    public FileHandle parent() {
        File parent = file.getParentFile();
        if (parent == null)
            parent = new File("");
        return new AndroidZipFileHandle(parent.getPath());
    }

    @Override
    public FileHandle[] list() {
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        FileHandle[] handles = new FileHandle[zipEntries.length - 1];
        int count = 0;
        for (int i = 0, n = zipEntries.length; i < n; i++) {
            if (zipEntries[i].mFileName.length() == getPath().length()) // Don't include the directory itself
                continue;
            handles[count++] = new AndroidZipFileHandle(zipEntries[i].mFileName);
        }
        return handles;
    }

    @Override
    public FileHandle[] list(FileFilter filter) {
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        FileHandle[] handles = new FileHandle[zipEntries.length - 1];
        int count = 0;
        for (int i = 0, n = zipEntries.length; i < n; i++) {
            if (zipEntries[i].mFileName.length() == getPath().length()) // Don't include the directory itself
                continue;
            FileHandle child = new AndroidZipFileHandle(zipEntries[i].mFileName);
            if (!filter.accept(child.file()))
                continue;
            handles[count] = child;
            count++;
        }
        if (count < handles.length) {
            FileHandle[] newHandles = new FileHandle[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    @Override
    public FileHandle[] list(FilenameFilter filter) {
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        FileHandle[] handles = new FileHandle[zipEntries.length - 1];
        int count = 0;
        for (int i = 0, n = zipEntries.length; i < n; i++) {
            if (zipEntries[i].mFileName.length() == getPath().length()) // Don't include the directory itself
                continue;
            String path = zipEntries[i].mFileName;
            if (!filter.accept(file, path))
                continue;
            handles[count] = new AndroidZipFileHandle(path);
            count++;
        }
        if (count < handles.length) {
            FileHandle[] newHandles = new FileHandle[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    @Override
    public FileHandle[] list(String suffix) {
        ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
        FileHandle[] handles = new FileHandle[zipEntries.length - 1];
        int count = 0;
        for (int i = 0, n = zipEntries.length; i < n; i++) {
            if (zipEntries[i].mFileName.length() == getPath().length()) // Don't include the directory itself
                continue;
            String path = zipEntries[i].mFileName;
            if (!path.endsWith(suffix))
                continue;
            handles[count] = new AndroidZipFileHandle(path);
            count++;
        }
        if (count < handles.length) {
            FileHandle[] newHandles = new FileHandle[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            handles = newHandles;
        }
        return handles;
    }

    @Override
    public boolean isDirectory() {
        return !hasAssetFd;
    }

    @Override
    public long length() {
        return hasAssetFd ? fdLength : 0;
    }

    @Override
    public boolean exists() {
        return hasAssetFd || expansionFile.getEntriesAt(getPath()).length != 0;
    }
}
