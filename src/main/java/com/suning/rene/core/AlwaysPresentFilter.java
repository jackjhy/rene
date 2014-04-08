package com.suning.rene.core;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by tiger on 14-3-29.
 */
public class AlwaysPresentFilter implements IFilter {
	public boolean isPresent(ByteBuffer key) {
		return true;
	}

	public void add(ByteBuffer key) {
	}

	public void clear() {
	}

	public void close() throws IOException {
	}

	public long serializedSize() {
		return 0;
	}

	@Override
	public boolean isPersisting() {
		return false;
	}

	@Override
	public void persisting(boolean b) {
		// throw new UnsupportedOperationException("");
	}
}
