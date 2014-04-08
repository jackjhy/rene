package com.suning.rene.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by tiger on 14-3-29.
 */
abstract class BloomFilterSerializer {
	public void serialize(BloomFilter bf, DataOutput out) throws IOException {
		out.writeInt(bf.hashCount);
		bf.bitset.serialize(out);
	}

	public BloomFilter deserialize(DataInput in) throws IOException {
		return deserialize(in, true);
	}

	public BloomFilter deserialize(DataInput in, boolean offheap)
			throws IOException {
		int hashes = in.readInt();
		IBitSet bs = offheap ? OffHeapBitSet.deserialize(in) : OpenBitSet
				.deserialize(in);
		return createFilter(hashes, bs);
	}

	protected abstract BloomFilter createFilter(int hashes, IBitSet bs);

}
