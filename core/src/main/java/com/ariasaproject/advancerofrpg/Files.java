package com.ariasaproject.advancerofrpg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public abstract class Files implements LifecycleListener {
    protected static final byte[] byteBuffer = new byte[1024];
    protected static final char[] charBuffer = new char[512];

    public final static InputStream readClasspath(String path) {
        InputStream r = Files.class.getResourceAsStream("/" + path.replace('\\', '/'));
        if (r == null) throw new RuntimeException("no file classpath in " + path);
        return r;
    }

    public final static String readClasspathString(String path) {
        BufferedReader read = new BufferedReader(new InputStreamReader(readClasspath(path)));
        try {
            StringBuilder b = new StringBuilder();
            int len;
            while ((len = read.read(charBuffer)) != -1) {
                b.append(charBuffer, 0, len);
            }
            return b.toString();
        } catch (IOException e) {
            throw new RuntimeException("no file classpath in " + path);
        } finally {
            try {
                read.close();
            } catch (Throwable ignore) {

            }
        }
    }

    public final static byte[] readClasspathBytes(String path) {
        InputStream r = readClasspath(path);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            if (r == null) throw new RuntimeException("no file classpath in " + path);
            int len;
            while ((len = r.read(byteBuffer)) != -1) {
                b.write(byteBuffer, 0, len);
            }
            return b.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("no file classpath in " + path);
        } finally {
            try {
                r.close();
                b.close();
            } catch (Throwable ignore) {

            }
        }
    }

    public abstract FileHandle getFileHandle(String path, FileType type);

    public abstract FileHandle internal(String path);

    public abstract FileHandle external(String path);

    public abstract FileHandle absolute(String path);

    public abstract FileHandle local(String path);

    public abstract String getExternalStoragePath();

    public abstract boolean isExternalStorageAvailable();

    public abstract String getLocalStoragePath();

    public abstract boolean isLocalStorageAvailable();

    public static enum FileType {
        Internal, External, Absolute, Local;
    }

    public static class FileHandle {
        protected File file;
        protected FileType type;

        protected FileHandle() {
        }

        public FileHandle(String fileName) {
            this.file = new File(fileName);
            this.type = FileType.Absolute;
        }

        public FileHandle(File file) {
            this.file = file;
            this.type = FileType.Absolute;
        }

        protected FileHandle(String fileName, FileType type) {
            this.type = type;
            file = new File(fileName);
        }

        protected FileHandle(File file, FileType type) {
            this.file = file;
            this.type = type;
        }

        static public FileHandle tempFile(String prefix) {
            try {
                return new FileHandle(File.createTempFile(prefix, null));
            } catch (IOException ex) {
                throw new RuntimeException("Unable to create temp file.", ex);
            }
        }

        static public FileHandle tempDirectory(String prefix) {
            try {
                File file = File.createTempFile(prefix, null);
                if (!file.delete())
                    throw new IOException("Unable to delete temp file: " + file);
                if (!file.mkdir())
                    throw new IOException("Unable to create temp directory: " + file);
                return new FileHandle(file);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to create temp file.", ex);
            }
        }

        static private void emptyDirectory(File file, boolean preserveTree) {
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0, n = files.length; i < n; i++) {
                        if (!files[i].isDirectory())
                            files[i].delete();
                        else if (preserveTree)
                            emptyDirectory(files[i], true);
                        else
                            deleteDirectory(files[i]);
                    }
                }
            }
        }

        static private boolean deleteDirectory(File file) {
            emptyDirectory(file, false);
            return file.delete();
        }

        static private void copyFile(FileHandle source, FileHandle dest) {
            try {
                dest.write(source.read(), false);
            } catch (Exception ex) {
                throw new RuntimeException("Error copying source file: " + source.file + " (" + source.type + ")\n" //
                        + "To destination: " + dest.file + " (" + dest.type + ")", ex);
            }
        }

        static private void copyDirectory(FileHandle sourceDir, FileHandle destDir) {
            destDir.mkdirs();
            FileHandle[] files = sourceDir.list();
            for (int i = 0, n = files.length; i < n; i++) {
                FileHandle srcFile = files[i];
                FileHandle destFile = destDir.child(srcFile.name());
                if (srcFile.isDirectory())
                    copyDirectory(srcFile, destFile);
                else
                    copyFile(srcFile, destFile);
            }
        }

        public String path() {
            return file.getPath().replace('\\', '/');
        }

        public String name() {
            return file.getName();
        }

        public String extension() {
            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex == -1)
                return "";
            return name.substring(dotIndex + 1);
        }

        public String nameWithoutExtension() {
            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex == -1)
                return name;
            return name.substring(0, dotIndex);
        }

        public String pathWithoutExtension() {
            String path = file.getPath().replace('\\', '/');
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex == -1)
                return path;
            return path.substring(0, dotIndex);
        }

        public FileType type() {
            return type;
        }

        public File file() {
            switch (type) {
                case External:
                    return new File(GraphFunc.app.getFiles().getExternalStoragePath(), file.getPath());
                case Local:
                    return new File(GraphFunc.app.getFiles().getLocalStoragePath(), file.getPath());

                default:
                    return file;
            }
        }

        public InputStream read() {
            if ((type == FileType.Internal || type == FileType.Local) && !file().exists()) {
                InputStream input = FileHandle.class.getResourceAsStream("/" + file.getPath().replace('\\', '/'));
                if (input == null)
                    throw new RuntimeException("File not found: " + file + " (" + type + ")");
                return input;
            }

            try {
                return new FileInputStream(file());
            } catch (Exception ex) {
                if (file().isDirectory())
                    throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
                throw new RuntimeException("Error reading file: " + file + " (" + type + ")", ex);
            }
        }

        public BufferedInputStream read(int bufferSize) {
            return new BufferedInputStream(read(), bufferSize);
        }

        public Reader reader() {
            return new InputStreamReader(read());
        }

        public Reader reader(String charset) {
            InputStream stream = read();
            try {
                return new InputStreamReader(stream, charset);
            } catch (UnsupportedEncodingException ex) {
                try {
                    stream.close();
                } catch (Throwable ignore) {

                }
                throw new RuntimeException("Error reading file: " + this, ex);
            }
        }

        public BufferedReader reader(int bufferSize) {
            return new BufferedReader(new InputStreamReader(read()), bufferSize);
        }

        public BufferedReader reader(int bufferSize, String charset) {
            try {
                return new BufferedReader(new InputStreamReader(read(), charset), bufferSize);
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Error reading file: " + this, ex);
            }
        }

        public String readString() {
            return readString(null);
        }

        public String readString(String charset) {
            StringBuilder output = new StringBuilder(estimateLength());
            InputStreamReader reader = null;
            try {
                if (charset == null)
                    reader = new InputStreamReader(read());
                else
                    reader = new InputStreamReader(read(), charset);
                char[] buffer = new char[256];
                while (true) {
                    int length = reader.read(buffer);
                    if (length == -1)
                        break;
                    output.append(buffer, 0, length);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error reading layout file: " + this, ex);
            } finally {
                try {
                    reader.close();
                } catch (Throwable ignore) {
                }
            }
            return output.toString();
        }

        public byte[] readBytes() {
            InputStream input = read();
            ByteArrayOutputStream baos = new OptimizedByteArrayOutputStream(estimateLength());
            try {
                int bytesRead;
                while ((bytesRead = input.read(byteBuffer)) != -1) {
                    baos.write(byteBuffer, 0, bytesRead);
                }
                return baos.toByteArray();
            } catch (IOException ex) {
                throw new RuntimeException("Error reading file: " + this, ex);
            } finally {
                try {
                    input.close();
                    baos.close();
                } catch (Throwable ignore) {

                }
            }
        }

        private int estimateLength() {
            int length = (int) length();
            return length != 0 ? length : 512;
        }

        public int readBytes(byte[] bytes, int offset, int size) {
            InputStream input = read();
            int position = 0;
            try {
                while (true) {
                    int count = input.read(bytes, offset + position, size - position);
                    if (count <= 0)
                        break;
                    position += count;
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error reading file: " + this, ex);
            } finally {
                try {
                    input.close();
                } catch (Throwable ignore) {

                }
            }
            return position - offset;
        }

        public ByteBuffer map() {
            return map(MapMode.READ_ONLY);
        }

        public ByteBuffer map(FileChannel.MapMode mode) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, mode == MapMode.READ_ONLY ? "r" : "rw");
                FileChannel fileChannel = raf.getChannel();
                ByteBuffer map = fileChannel.map(mode, 0, file.length());
                map.order(ByteOrder.nativeOrder());
                return map;
            } catch (Exception ex) {
                throw new RuntimeException("Error memory mapping file: " + this + " (" + type + ")", ex);
            } finally {
                try {
                    raf.close();
                } catch (Throwable ignore) {

                }
            }
        }

        public OutputStream write(boolean append) {
            if (type == FileType.Internal)
                throw new RuntimeException("Cannot write to an internal file: " + file);
            parent().mkdirs();
            try {
                return new FileOutputStream(file(), append);
            } catch (Exception ex) {
                if (file().isDirectory())
                    throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
                throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
            }
        }

        public OutputStream write(boolean append, int bufferSize) {
            return new BufferedOutputStream(write(append), bufferSize);
        }

        public void write(InputStream input, boolean append) {
            OutputStream output = null;
            try {
                output = write(append);
                int bytesRead;
                while ((bytesRead = input.read(byteBuffer)) != -1) {
                    output.write(byteBuffer, 0, bytesRead);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Error stream writing to file: " + file + " (" + type + ")", ex);
            } finally {
                try {
                    input.close();
                    output.close();
                } catch (Throwable ignore) {

                }
            }

        }

        public Writer writer(boolean append) {
            return writer(append, null);
        }

        public Writer writer(boolean append, String charset) {
            if (type == FileType.Internal)
                throw new RuntimeException("Cannot write to an internal file: " + file);
            parent().mkdirs();
            try {
                FileOutputStream output = new FileOutputStream(file(), append);
                if (charset == null)
                    return new OutputStreamWriter(output);
                else
                    return new OutputStreamWriter(output, charset);
            } catch (IOException ex) {
                if (file().isDirectory())
                    throw new RuntimeException("Cannot open a stream to a directory: " + file + " (" + type + ")", ex);
                throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
            }
        }

        public void writeString(String string, boolean append) {
            writeString(string, append, null);
        }

        public void writeString(String string, boolean append, String charset) {
            Writer writer = null;
            try {
                writer = writer(append, charset);
                writer.write(string);
            } catch (Exception ex) {
                throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
            } finally {
                try {
                    writer.close();
                } catch (Throwable ignore) {

                }
            }
        }

        public void writeBytes(byte[] bytes, boolean append) {
            OutputStream output = write(append);
            try {
                output.write(bytes);
            } catch (IOException ex) {
                throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
            } finally {
                try {
                    output.close();
                } catch (Throwable ignore) {
                }
            }
        }

        public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
            OutputStream output = write(append);
            try {
                output.write(bytes, offset, length);
            } catch (IOException ex) {
                throw new RuntimeException("Error writing file: " + file + " (" + type + ")", ex);
            } finally {
                try {
                    output.close();
                } catch (Throwable ignore) {
                }
            }
        }

        public FileHandle[] list() {
            String[] relativePaths = file().list();
            if (relativePaths == null)
                return new FileHandle[0];
            FileHandle[] handles = new FileHandle[relativePaths.length];
            for (int i = 0, n = relativePaths.length; i < n; i++)
                handles[i] = child(relativePaths[i]);
            return handles;
        }

        public FileHandle[] list(FileFilter filter) {
            File file = file();
            String[] relativePaths = file.list();
            if (relativePaths == null)
                return new FileHandle[0];
            FileHandle[] handles = new FileHandle[relativePaths.length];
            int count = 0;
            for (int i = 0, n = relativePaths.length; i < n; i++) {
                String path = relativePaths[i];
                FileHandle child = child(path);
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
        }

        public FileHandle[] list(FilenameFilter filter) {
            File file = file();
            String[] relativePaths = file.list();
            if (relativePaths == null)
                return new FileHandle[0];
            FileHandle[] handles = new FileHandle[relativePaths.length];
            int count = 0;
            for (int i = 0, n = relativePaths.length; i < n; i++) {
                String path = relativePaths[i];
                if (!filter.accept(file, path))
                    continue;
                handles[count] = child(path);
                count++;
            }
            if (count < relativePaths.length) {
                FileHandle[] newHandles = new FileHandle[count];
                System.arraycopy(handles, 0, newHandles, 0, count);
                handles = newHandles;
            }
            return handles;
        }

        public FileHandle[] list(String suffix) {
            String[] relativePaths = file().list();
            if (relativePaths == null)
                return new FileHandle[0];
            FileHandle[] handles = new FileHandle[relativePaths.length];
            int count = 0;
            for (int i = 0, n = relativePaths.length; i < n; i++) {
                String path = relativePaths[i];
                if (!path.endsWith(suffix))
                    continue;
                handles[count] = child(path);
                count++;
            }
            if (count < relativePaths.length) {
                FileHandle[] newHandles = new FileHandle[count];
                System.arraycopy(handles, 0, newHandles, 0, count);
                handles = newHandles;
            }
            return handles;
        }

        public boolean isDirectory() {
            return file().isDirectory();
        }

        public FileHandle child(String name) {
            if (file.getPath().length() == 0)
                return new FileHandle(new File(name), type);
            return new FileHandle(new File(file, name), type);
        }

        public FileHandle sibling(String name) {
            if (file.getPath().length() == 0)
                throw new RuntimeException("Cannot get the sibling of the root.");
            return new FileHandle(new File(file.getParent(), name), type);
        }

        public FileHandle parent() {
            File parent = file.getParentFile();
            if (parent == null) {
                if (type == FileType.Absolute)
                    parent = new File("/");
                else
                    parent = new File("");
            }
            return new FileHandle(parent, type);
        }

        public void mkdirs() {
            if (type == FileType.Internal)
                throw new RuntimeException("Cannot mkdirs with an internal file: " + file);
            file().mkdirs();
        }

        public boolean exists() {
            if (type == FileType.Internal) {
                if (file().exists())
                    return true;
                return FileHandle.class.getResource("/" + file.getPath().replace('\\', '/')) != null;
            }
            return file().exists();
        }

        public boolean delete() {
            if (type == FileType.Internal)
                throw new RuntimeException("Cannot delete an internal file: " + file);
            return file().delete();
        }

        public boolean deleteDirectory() {
            if (type == FileType.Internal)
                throw new RuntimeException("Cannot delete an internal file: " + file);
            return deleteDirectory(file());
        }

        public void emptyDirectory() {
            emptyDirectory(false);
        }

        public void emptyDirectory(boolean preserveTree) {
            if (type == FileType.Internal)
                throw new RuntimeException("Cannot delete an internal file: " + file);
            emptyDirectory(file(), preserveTree);
        }

        public void copyTo(FileHandle dest) {
            if (!isDirectory()) {
                if (dest.isDirectory())
                    dest = dest.child(name());
                copyFile(this, dest);
                return;
            }
            if (dest.exists()) {
                if (!dest.isDirectory())
                    throw new RuntimeException("Destination exists but is not a directory: " + dest);
            } else {
                dest.mkdirs();
                if (!dest.isDirectory())
                    throw new RuntimeException("Destination directory cannot be created: " + dest);
            }
            copyDirectory(this, dest.child(name()));
        }

        public void moveTo(FileHandle dest) {
            switch (type) {
                default:
                    throw new RuntimeException("file type is null for " + file);
                case Internal:
                    throw new RuntimeException("Cannot move an internal file: " + file);
                case Absolute:
                case External:
                    // Try rename for efficiency and to change case on
                    // case-insensitive file
                    // systems.
                    if (file().renameTo(dest.file()))
                        return;
                case Local:
                    break;
            }
            copyTo(dest);
            delete();
            if (exists() && isDirectory())
                deleteDirectory();
        }

        public long length() {
            if (type == FileType.Internal && !file.exists()) {
                InputStream input = read();
                try {
                    return input.available();
                } catch (Exception ignored) {
                } finally {
                    try {
                        input.close();
                    } catch (Throwable ignore) {

                    }
                }
                return 0;
            }
            return file().length();
        }

        public long lastModified() {
            return file().lastModified();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FileHandle))
                return false;
            FileHandle other = (FileHandle) obj;
            return type == other.type && path().equals(other.path());
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = hash * 37 + type.hashCode();
            hash = hash * 67 + path().hashCode();
            return hash;
        }

        @Override
        public String toString() {
            return file.getPath().replace('\\', '/');
        }

    }

    static public class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
        public OptimizedByteArrayOutputStream(int initialSize) {
            super(initialSize);
        }

        @Override
        public synchronized byte[] toByteArray() {
            if (count == buf.length)
                return buf;
            return super.toByteArray();
        }

        public byte[] getBuffer() {
            return buf;
        }
    }

    public static abstract class FileHandleStream extends FileHandle {
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
}
