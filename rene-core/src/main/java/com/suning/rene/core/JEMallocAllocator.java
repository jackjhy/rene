package com.suning.rene.core;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Created by tiger on 14-3-20.
 */
public class JEMallocAllocator implements IAllocator {
	public interface JEMLibrary extends Library {
		long malloc(long size);

		void free(long pointer);
	}

	private final JEMLibrary library;

	public JEMallocAllocator() {
		library = (JEMLibrary) Native.loadLibrary("jemalloc", JEMLibrary.class);
	}

	public long allocate(long size) {
		return library.malloc(size);
	}

	public void free(long peer) {
		library.free(peer);
	}
}
