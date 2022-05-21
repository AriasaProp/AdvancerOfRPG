package com.ariasaproject.advancerofrpg;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;

import com.ariasaproject.advancerofrpg.ZipResourceFile.ZipEntryRO;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class AndroidFiles extends Files {
    protected final String sdcard;
    protected final String localpath;
    protected final AssetManager assets;
    private ZipResourceFile expansionFile = null;

    public AndroidFiles(Context co) {
        this.sdcard = co.getExternalFilesDir("/").getAbsolutePath();
        String lp = co.getFilesDir().getAbsolutePath();
        this.localpath = lp.endsWith("/") ? lp : lp + "/";
        this.assets = co.getAssets();
    }

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        switch (type) {
            case Internal:
                FileHandle f = internal(path);
                if (expansionFile != null && type == FileType.Internal)
                    f = getZipFileHandleIfExists(f, path);
                return f;
            case Absolute:
                return absolute(path);
            case Local:
                return local(path);
            case External:
                return external(path);
            default:
                return null;

        }
    }

    private FileHandle getZipFileHandleIfExists(FileHandle handle, String path) {
        try {
            assets.open(path).close(); // Check if file exists.
            return handle;
        } catch (Exception ex) {
            // try APK expansion instead
            FileHandle zipHandle = new InternalExpansionFileHandle(getExpansionFile(), path);
            if (!zipHandle.isDirectory())
                return zipHandle;
            else if (zipHandle.exists())
                return zipHandle;
        }
        return handle;
    }

    @Override
    public FileHandle internal(final String path) {
        final ZipResourceFile expansionFile = getExpansionFile();
        FileHandle handle = new InternalFileHandle(assets, path);
        if (expansionFile != null)
            handle = getZipFileHandleIfExists(handle, path);
        return handle;
    }

    @Override
    public FileHandle external(String path) {
        return new FileHandle(path.replace('\\', '/'), FileType.External);
    }

    @Override
    public FileHandle absolute(String path) {
        return new FileHandle(path.replace('\\', '/'), FileType.Absolute) {
            @Override
            public FileHandle child(String name) {
                name = name.replace('\\', '/');
                if (file.getPath().length() == 0)
                    return new FileHandle(new File(name), type);
                return new FileHandle(new File(file, name), type);
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
                if (parent == null)
                    parent = new File("/");
                return new FileHandle(parent, type);
            }
        };
    }

    @Override
    public FileHandle local(String path) {
        return new FileHandle(path.replace('\\', '/'), FileType.Local) {
            @Override
            public FileHandle child(String name) {
                name = name.replace('\\', '/');
                if (file.getPath().length() == 0)
                    return new FileHandle(new File(name), type);
                return new FileHandle(new File(file, name), type);
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
                if (parent == null)
                    parent = new File("");
                return new FileHandle(parent, type);
            }
        };
    }

    @Override
    public String getExternalStoragePath() {
        return sdcard;
    }

    @Override
    public boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public String getLocalStoragePath() {
        return localpath;
    }

    @Override
    public boolean isLocalStorageAvailable() {
        return true;
    }

    public boolean setAPKExpansion(int mainVersion, int patchVersion) {
        try {
            Context context;
            if (GraphFunc.app instanceof Activity) {
                context = ((Activity) GraphFunc.app).getBaseContext();
            } else {
                throw new RuntimeException("APK expansion not supported for application type");
            }
            expansionFile = APKExpansionSupport.getAPKExpansionZipFile(context, mainVersion, patchVersion);
        } catch (IOException ex) {
            throw new RuntimeException("APK expansion main version " + mainVersion + " or patch version " + patchVersion
                    + " couldn't be opened!");
        }
        return expansionFile != null;
    }

    public ZipResourceFile getExpansionFile() {
        return expansionFile;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void dispose() {
    }

    public AssetFileDescriptor descriptor(FileHandle handle) throws IOException {
        if (handle instanceof InternalFileHandle) {
            return ((InternalExpansionFileHandle) handle).getAssetFileDescriptor();
        } else if (handle instanceof InternalFileHandle) {
            return ((InternalFileHandle) handle).getAssetFileDescriptor();
        } else {
            throw new IllegalStateException("file handle type doesn't support file descriptor");
        }

    }

    static class InternalExpansionFileHandle extends FileHandle {
        private final boolean hasAssetFd;
        private final long fdLength;
        private final ZipResourceFile expansionFile;
        private final String path;

        InternalExpansionFileHandle(ZipResourceFile expansionFile, String fileName) {
            super(fileName.replace('\\', '/'), FileType.Internal);
            this.expansionFile = expansionFile;
            AssetFileDescriptor assetFd = expansionFile.getAssetFileDescriptor(getPath());
            if (assetFd != null) {
                hasAssetFd = true;
                fdLength = assetFd.getLength();
                try {
                    assetFd.close();
                } catch (IOException e) {
                }
                path = file.getPath().replace('\\', '/');
            } else {
                hasAssetFd = false;
                fdLength = 0;
                path = file.getPath().replace('\\', '/') + "/";
            }
        }

        InternalExpansionFileHandle(ZipResourceFile expansionFile, File file) {
            super(file, FileType.Internal);
            this.expansionFile = expansionFile;
            AssetFileDescriptor assetFd = expansionFile.getAssetFileDescriptor(getPath());
            if (assetFd != null) {
                hasAssetFd = true;
                fdLength = assetFd.getLength();
                try {
                    assetFd.close();
                } catch (IOException e) {
                }
                path = file.getPath().replace('\\', '/');
            } else {
                hasAssetFd = false;
                fdLength = 0;
                path = file.getPath().replace('\\', '/') + "/";
            }
        }

        public AssetFileDescriptor getAssetFileDescriptor() throws IOException {
            return expansionFile.getAssetFileDescriptor(getPath());
        }

        @Override
        public ByteBuffer map(FileChannel.MapMode mode) {
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
                return new InternalExpansionFileHandle(expansionFile, new File(name));
            return new InternalExpansionFileHandle(expansionFile, new File(file, name));
        }

        @Override
        public FileHandle sibling(String name) {
            if (file.getPath().length() == 0)
                throw new RuntimeException("Cannot get the sibling of the root.");
            return GraphFunc.app.getFiles().getFileHandle(new File(file.getParent(), name).getPath(), type); // this way we
            // can find the sibling even if it's not inside the obb
        }

        @Override
        public FileHandle parent() {
            File parent = file.getParentFile();
            if (parent == null)
                parent = new File("");
            return new InternalExpansionFileHandle(expansionFile, parent.getPath());
        }

        @Override
        public FileHandle[] list() {
            ZipEntryRO[] zipEntries = expansionFile.getEntriesAt(getPath());
            FileHandle[] handles = new FileHandle[zipEntries.length - 1];
            int count = 0;
            for (int i = 0, n = zipEntries.length; i < n; i++) {
                if (zipEntries[i].mFileName.length() == getPath().length()) // Don't include the directory itself
                    continue;
                handles[count++] = new InternalExpansionFileHandle(expansionFile, zipEntries[i].mFileName);
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
                FileHandle child = new InternalExpansionFileHandle(expansionFile, zipEntries[i].mFileName);
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
                handles[count] = new InternalExpansionFileHandle(expansionFile, path);
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
                handles[count] = new InternalExpansionFileHandle(expansionFile, path);
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

    static class InternalFileHandle extends FileHandle {
        final private AssetManager assets;

        InternalFileHandle(AssetManager assets, String fileName) {
            super(fileName.replace('\\', '/'), FileType.Internal);
            this.assets = assets;
        }

        InternalFileHandle(AssetManager assets, File file) {
            super(file, FileType.Internal);
            this.assets = assets;
        }

        @Override
        public FileHandle child(String name) {
            name = name.replace('\\', '/');
            if (file.getPath().length() == 0)
                return new InternalFileHandle(assets, new File(name));
            return new InternalFileHandle(assets, new File(file, name));
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
            return new InternalFileHandle(assets, parent);
        }

        @Override
        public InputStream read() {
            try {
                return assets.open(file.getPath());
            } catch (IOException ex) {
                throw new RuntimeException("Error reading file: " + file + " (" + type + ")", ex);
            }
        }

        @Override
        public ByteBuffer map(FileChannel.MapMode mode) {
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

        @Override
        public FileHandle[] list() {
            try {
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                for (int i = 0, n = handles.length; i < n; i++)
                    handles[i] = new InternalFileHandle(assets, new File(file, relativePaths[i]));
                return handles;
            } catch (Exception ex) {
                throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }

        @Override
        public FileHandle[] list(FileFilter filter) {
            try {
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                int count = 0;
                for (int i = 0, n = handles.length; i < n; i++) {
                    String path = relativePaths[i];
                    FileHandle child = new InternalFileHandle(assets, new File(file, path));
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

        @Override
        public FileHandle[] list(FilenameFilter filter) {
            try {
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                int count = 0;
                for (int i = 0, n = handles.length; i < n; i++) {
                    String path = relativePaths[i];
                    if (!filter.accept(file, path))
                        continue;
                    handles[count] = new InternalFileHandle(assets, new File(file, path));
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

        @Override
        public FileHandle[] list(String suffix) {
            try {
                String[] relativePaths = assets.list(file.getPath());
                FileHandle[] handles = new FileHandle[relativePaths.length];
                int count = 0;
                for (int i = 0, n = handles.length; i < n; i++) {
                    String path = relativePaths[i];
                    if (!path.endsWith(suffix))
                        continue;
                    handles[count] = new InternalFileHandle(assets, new File(file, path));
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

        @Override
        public boolean isDirectory() {
            try {
                return assets.list(file.getPath()).length > 0;
            } catch (IOException ex) {
                return false;
            }

        }

        @Override
        public boolean exists() {
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

        @Override
        public long length() {
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
            return 0;
        }

        public AssetFileDescriptor getAssetFileDescriptor() throws IOException {
            return assets.openFd(path());
        }
    }

    static class AbsoluteFileHandle extends FileHandle {
        AbsoluteFileHandle(File file) {
            super(file, FileType.Absolute);
        }

        AbsoluteFileHandle(String path) {
            super(path, FileType.Absolute);
        }

        @Override
        public FileHandle parent() {
            File parent = file.getParentFile();
            if (parent == null) {
                parent = new File("/");
            }
            return new AbsoluteFileHandle(parent);
        }

    }

    static class LocalFileHandle extends FileHandle {
        LocalFileHandle(File file) {
            super(file, FileType.Local);
        }

        LocalFileHandle(String path) {
            super(path, FileType.Local);
        }

        @Override
        public File file() {
            return new File(GraphFunc.app.getFiles().getLocalStoragePath(), file.getPath());
        }

    }

    static class ExternalFileHandle extends FileHandle {
        ExternalFileHandle(File file) {
            super(file, FileType.External);
        }

        ExternalFileHandle(String path) {
            super(path, FileType.External);
        }
    }
}
