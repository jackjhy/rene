package com.suning.rene.utils;

import com.google.common.base.Throwables;

/**
 * Created by tiger on 14-3-27.
 */
public abstract class WrappedRunnable implements Runnable {
	public final void run() {
		try {
			runMayThrow();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	abstract protected void runMayThrow() throws Exception;
}
