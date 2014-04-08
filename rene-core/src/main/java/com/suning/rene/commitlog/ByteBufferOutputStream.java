package com.suning.rene.commitlog;

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by tiger on 14-3-25.
 */

public class ByteBufferOutputStream extends OutputStream {
	private final ByteBuffer buffer;

	public ByteBufferOutputStream(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public void write(int b) {
		buffer.put((byte) b);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		buffer.put(b, off, len);
	}
}