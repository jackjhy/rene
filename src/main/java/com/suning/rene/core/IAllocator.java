package com.suning.rene.core;

/**
 * Created by tiger on 14-3-20.
 */
public interface IAllocator {

	long allocate(long size);

	void free(long peer);
}
