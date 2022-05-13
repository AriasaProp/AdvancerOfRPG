package com.ariasaproject.advancerofrpg.utils;

import java.io.ByteArrayOutputStream;

public class DataBuffer extends DataOutput {
	private final OUTStream outStream;

	public DataBuffer() {
		this(32);
	}

	public DataBuffer(int initialSize) {
		super(new OUTStream(initialSize));
		outStream = (OUTStream) out;
	}

	public byte[] getBuffer() {
		return outStream.getBuffer();
	}

	public byte[] toArray() {
		return outStream.toByteArray();
	}

	private static class OUTStream extends ByteArrayOutputStream {
		public OUTStream(int size) {
			super(size);
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
}
