package com.suning.rene.utils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Created by tiger on 14-3-21.
 */
public class SNUtilities {
	public static long abs(long index) {
		long negbit = index >> 63;
		return (index ^ negbit) - negbit;
	}

	public static void waitOnFutures(Iterable<Future<?>> futures) {
		for (Future f : futures)
			waitOnFuture(f);
	}

	public static <T> T waitOnFuture(Future<T> future) {
		try {
			return future.get();
		} catch (ExecutionException ee) {
			throw new RuntimeException(ee);
		} catch (InterruptedException ie) {
			throw new AssertionError(ie);
		}
	}

}
