package com.suning.rene.commitlog;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * Created by tiger on 14-3-25.
 */
public class ChecksummedOutputStream extends OutputStream {
	private final OutputStream out;
	private final Checksum checksum;

	public ChecksummedOutputStream(OutputStream out, Checksum checksum) {
		this.out = out;
		this.checksum = checksum;
	}

	public void resetChecksum() {
		checksum.reset();
	}

	public void write(int b) throws IOException {
		out.write(b);
		checksum.update(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		checksum.update(b, off, len);
	}
}