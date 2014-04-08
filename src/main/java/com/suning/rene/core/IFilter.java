package com.suning.rene.core;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * Created by tiger on 14-3-29.
 */
public interface IFilter extends Closeable {
	void add(ByteBuffer key);

	boolean isPresent(ByteBuffer key);

	void clear();

	long serializedSize();

	boolean isPersisting();

	void persisting(boolean b);

}
