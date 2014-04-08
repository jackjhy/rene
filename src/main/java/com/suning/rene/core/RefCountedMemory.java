package com.suning.rene.core;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Created by tiger on 14-3-20.
 */
public class RefCountedMemory extends Memory {
	private volatile int references = 1;
	private static final AtomicIntegerFieldUpdater<RefCountedMemory> UPDATER = AtomicIntegerFieldUpdater
			.newUpdater(RefCountedMemory.class, "references");

	public RefCountedMemory(long size) {
		super(size);
	}

	/**
	 * @return true if we succeed in referencing before the reference count
	 *         reaches zero. (A FreeableMemory object is created with a
	 *         reference count of one.)
	 */
	public boolean reference() {
		while (true) {
			int n = UPDATER.get(this);
			if (n <= 0)
				return false;
			if (UPDATER.compareAndSet(this, n, n + 1))
				return true;
		}
	}

	/** decrement reference count. if count reaches zero, the object is freed. */
	public void unreference() {
		if (UPDATER.decrementAndGet(this) == 0)
			free();
	}
}
