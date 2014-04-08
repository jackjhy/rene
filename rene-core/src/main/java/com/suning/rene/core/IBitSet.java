package com.suning.rene.core;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * a abstract description for java.util.BitSet Created by tiger on 14-3-20.
 */
public interface IBitSet extends Closeable {
	@Override
	void close() throws IOException;

	public long capacity();

	/**
	 * Return true or false for the specified bit index. The index should be
	 * less than the capacity
	 * 
	 * @param index
	 * @return
	 */
	public boolean get(long index);

	/**
	 * Sets the bit at the specified position. The index should be less than the
	 * capacity
	 * 
	 * @param index
	 */
	public void set(long index);

	/**
	 * clear the bit. The index should be less than capacity.
	 * 
	 * @param index
	 */
	public void clear(long index);

	public void serialize(DataOutput out) throws IOException;

	public long serializedSize();

	public void clear();
}
