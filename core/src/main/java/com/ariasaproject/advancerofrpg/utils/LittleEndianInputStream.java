package com.ariasaproject.advancerofrpg.utils;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Taken from http://www.javafaq.nu/java-example-code-1079.html
 *
 * @author mzechner
 */
public class LittleEndianInputStream extends FilterInputStream implements DataInput {

    private final DataInputStream din;

    public LittleEndianInputStream(InputStream in) {
        super(in);
        din = new DataInputStream(in);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        din.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        din.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return din.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return din.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return din.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return din.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        int low = din.read();
        int high = din.read();
        return (short) ((high << 8) | (low & 0xff));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        int low = din.read();
        int high = din.read();
        return ((high & 0xff) << 8) | (low & 0xff);
    }

    @Override
    public char readChar() throws IOException {
        return din.readChar();
    }

    @Override
    public int readInt() throws IOException {
        int[] res = new int[4];
        for (int i = 3; i >= 0; i--)
            res[i] = din.read();
        return ((res[0] & 0xff) << 24) | ((res[1] & 0xff) << 16) | ((res[2] & 0xff) << 8) | (res[3] & 0xff);
    }

    @Override
    public long readLong() throws IOException {
        int[] res = new int[8];
        for (int i = 7; i >= 0; i--)
            res[i] = din.read();
        return (((long) (res[0] & 0xff) << 56) | ((long) (res[1] & 0xff) << 48) | ((long) (res[2] & 0xff) << 40) | ((long) (res[3] & 0xff) << 32) | ((long) (res[4] & 0xff) << 24) | ((long) (res[5] & 0xff) << 16) | ((long) (res[6] & 0xff) << 8) | (res[7] & 0xff));
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public final String readLine() throws IOException {
        return din.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return din.readUTF();
    }
}
