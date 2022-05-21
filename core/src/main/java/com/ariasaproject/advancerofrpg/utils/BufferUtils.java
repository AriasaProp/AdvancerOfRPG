package com.ariasaproject.advancerofrpg.utils;

import com.ariasaproject.advancerofrpg.math.Matrix3;
import com.ariasaproject.advancerofrpg.math.Matrix4;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public final class BufferUtils {

    public static void copy(float[] src, Buffer dst, int numFloats, int offset) {
        if (dst instanceof ByteBuffer)
            dst.limit(numFloats << 2);
        else if (dst instanceof FloatBuffer)
            dst.limit(numFloats);
        copyJni(src, dst, numFloats, offset);
        dst.position(0);
    }

    public static void copy(byte[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements);
    }

    public static void copy(short[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements << 1));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
    }

    public static void copy(char[] src, int srcOffset, int numElements, Buffer dst) {
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
    }

    public static void copy(int[] src, int srcOffset, int numElements, Buffer dst) {
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
    }

    public static void copy(long[] src, int srcOffset, int numElements, Buffer dst) {
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
    }

    public static void copy(float[] src, int srcOffset, int numElements, Buffer dst) {
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
    }

    public static void copy(double[] src, int srcOffset, int numElements, Buffer dst) {
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
    }

    public static void copy(char[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements << 1));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
    }

    public static void copy(int[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements << 2));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
    }

    public static void copy(long[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements << 3));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
    }

    public static void copy(float[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements << 2));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
    }

    public static void copy(double[] src, int srcOffset, Buffer dst, int numElements) {
        dst.limit(dst.position() + bytesToElements(dst, numElements << 3));
        copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
    }

    public static void copy(Buffer src, Buffer dst, int numElements) {
        int numBytes = elementsToBytes(src, numElements);
        dst.limit(dst.position() + bytesToElements(dst, numBytes));
        copyJni(src, positionInBytes(src), dst, positionInBytes(dst), numBytes);
    }

    public static void transform(Buffer data, int dimensions, int strideInBytes, int count, Matrix4 matrix) {
        transform(data, dimensions, strideInBytes, count, matrix, 0);
    }

    public static void transform(float[] data, int dimensions, int strideInBytes, int count, Matrix4 matrix) {
        transform(data, dimensions, strideInBytes, count, matrix, 0);
    }

    public static void transform(Buffer data, int dimensions, int strideInBytes, int count, Matrix4 matrix, int offset) {
        switch (dimensions) {
            case 4:
                transformV4M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
                break;
            case 3:
                transformV3M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
                break;
            case 2:
                transformV2M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void transform(float[] data, int dimensions, int strideInBytes, int count, Matrix4 matrix, int offset) {
        switch (dimensions) {
            case 4:
                transformV4M4Jni(data, strideInBytes, count, matrix.val, offset);
                break;
            case 3:
                transformV3M4Jni(data, strideInBytes, count, matrix.val, offset);
                break;
            case 2:
                transformV2M4Jni(data, strideInBytes, count, matrix.val, offset);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void transform(Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix) {
        transform(data, dimensions, strideInBytes, count, matrix, 0);
    }

    public static void transform(float[] data, int dimensions, int strideInBytes, int count, Matrix3 matrix) {
        transform(data, dimensions, strideInBytes, count, matrix, 0);
    }

    public static void transform(Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix, int offset) {
        switch (dimensions) {
            case 3:
                transformV3M3Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
                break;
            case 2:
                transformV2M3Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void transform(float[] data, int dimensions, int strideInBytes, int count, Matrix3 matrix, int offset) {
        switch (dimensions) {
            case 3:
                transformV3M3Jni(data, strideInBytes, count, matrix.val, offset);
                break;
            case 2:
                transformV2M3Jni(data, strideInBytes, count, matrix.val, offset);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static long findFloats(Buffer vertex, int strideInBytes, Buffer vertices, int numVertices) {
        return find(vertex, positionInBytes(vertex), strideInBytes, vertices, positionInBytes(vertices), numVertices);
    }

    public static long findFloats(float[] vertex, int strideInBytes, Buffer vertices, int numVertices) {
        return find(vertex, 0, strideInBytes, vertices, positionInBytes(vertices), numVertices);
    }

    public static long findFloats(Buffer vertex, int strideInBytes, float[] vertices, int numVertices) {
        return find(vertex, positionInBytes(vertex), strideInBytes, vertices, 0, numVertices);
    }

    public static long findFloats(float[] vertex, int strideInBytes, float[] vertices, int numVertices) {
        return find(vertex, 0, strideInBytes, vertices, 0, numVertices);
    }

    public static long findFloats(Buffer vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon) {
        return find(vertex, positionInBytes(vertex), strideInBytes, vertices, positionInBytes(vertices), numVertices, epsilon);
    }

    public static long findFloats(float[] vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon) {
        return find(vertex, 0, strideInBytes, vertices, positionInBytes(vertices), numVertices, epsilon);
    }

    public static long findFloats(Buffer vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon) {
        return find(vertex, positionInBytes(vertex), strideInBytes, vertices, 0, numVertices, epsilon);
    }

    public static long findFloats(float[] vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon) {
        return find(vertex, 0, strideInBytes, vertices, 0, numVertices, epsilon);
    }

    private static int positionInBytes(Buffer dst) {
        if (dst instanceof ByteBuffer)
            return dst.position();
        else if (dst instanceof ShortBuffer)
            return dst.position() << 1;
        else if (dst instanceof CharBuffer)
            return dst.position() << 1;
        else if (dst instanceof IntBuffer)
            return dst.position() << 2;
        else if (dst instanceof LongBuffer)
            return dst.position() << 3;
        else if (dst instanceof FloatBuffer)
            return dst.position() << 2;
        else if (dst instanceof DoubleBuffer)
            return dst.position() << 3;
        else
            throw new RuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
    }

    private static int bytesToElements(Buffer dst, int bytes) {
        if (dst instanceof ByteBuffer)
            return bytes;
        else if (dst instanceof ShortBuffer)
            return bytes >>> 1;
        else if (dst instanceof CharBuffer)
            return bytes >>> 1;
        else if (dst instanceof IntBuffer)
            return bytes >>> 2;
        else if (dst instanceof LongBuffer)
            return bytes >>> 3;
        else if (dst instanceof FloatBuffer)
            return bytes >>> 2;
        else if (dst instanceof DoubleBuffer)
            return bytes >>> 3;
        else
            throw new RuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
    }

    private static int elementsToBytes(Buffer dst, int elements) {
        if (dst instanceof ByteBuffer)
            return elements;
        else if (dst instanceof ShortBuffer)
            return elements << 1;
        else if (dst instanceof CharBuffer)
            return elements << 1;
        else if (dst instanceof IntBuffer)
            return elements << 2;
        else if (dst instanceof LongBuffer)
            return elements << 3;
        else if (dst instanceof FloatBuffer)
            return elements << 2;
        else if (dst instanceof DoubleBuffer)
            return elements << 3;
        else
            throw new RuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
    }

    public static ByteBuffer newByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    public static IntBuffer newIntBuffer(int size) {
        return newByteBuffer(size * 4).asIntBuffer();
    }

    public static FloatBuffer newFloatBuffer(int size) {
        return newByteBuffer(4 * size).asFloatBuffer();
    }

    public static long getUnsafeBufferAddress(Buffer buffer) {
        return getBufferAddress(buffer) + buffer.position();
    }

    public static native void freeMemory(Buffer buffer);

    public static native Buffer newDisposableByteBuffer(int numBytes);

    private static native long getBufferAddress(Buffer buffer);

    public static native void clear(ByteBuffer buffer, int numBytes);

    private native static void copyJni(float[] src, Buffer dst, int numFloats, int offset);

    private native static void copyJni(byte[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(char[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(short[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(int[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(long[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(float[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(double[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void copyJni(Buffer src, int srcOffset, Buffer dst, int dstOffset, int numBytes);

    private native static void transformV4M4Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV4M4Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV3M4Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV3M4Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV2M4Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV2M4Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV3M3Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV3M3Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV2M3Jni(Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static void transformV2M3Jni(float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes);

    private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices);

    private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices);

    private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices);

    private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices);

    private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon);

    private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon);

    private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon);

    private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon);
}
