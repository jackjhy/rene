package com.suning.rene.io;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An implementation of the DataOutputStream interface using a
 * FastByteArrayOutputStream and exposing its buffer so copies can be avoided.
 * 
 * This class is completely thread unsafe.
 */
public final class DataOutputBuffer extends DataOutputStream {
	public DataOutputBuffer() {
		this(128);
	}

	public DataOutputBuffer(int size) {
		super(new FastByteArrayOutputStream(size));
	}

	@Override
	public void write(int b) {
		try {
			super.write(b);
		} catch (IOException e) {
			throw new AssertionError(e); // FBOS does not throw IOE
		}
	}

	@Override
	public void write(byte[] b, int off, int len) {
		try {
			super.write(b, off, len);
		} catch (IOException e) {
			throw new AssertionError(e); // FBOS does not throw IOE
		}
	}

	/**
	 * Returns the current contents of the buffer. Data is only valid to
	 * {@link #getLength()}.
	 */
	public byte[] getData() {
		return ((FastByteArrayOutputStream) out).buf;
	}

	/** Returns the length of the valid data currently in the buffer. */
	public int getLength() {
		return ((FastByteArrayOutputStream) out).count;
	}
}
