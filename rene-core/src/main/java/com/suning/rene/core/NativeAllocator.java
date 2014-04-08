package com.suning.rene.core;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by tiger on 14-3-20.
 */
public class NativeAllocator implements IAllocator {
	static final Unsafe unsafe;
	static {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (sun.misc.Unsafe) field.get(null);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public long allocate(long size) {
		return unsafe.allocateMemory(size);
	}

	@Override
	public void free(long peer) {
		unsafe.freeMemory(peer);
	}
}
