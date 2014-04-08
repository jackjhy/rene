package com.suning.rene.core;

import com.suning.rene.utils.SNUtilities;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by tiger on 14-3-21.
 */
public abstract class BloomFilter implements IFilter {
	private static final ThreadLocal<long[]> reusableIndexes = new ThreadLocal<long[]>() {
		protected long[] initialValue() {
			return new long[21];
		}
	};

	public final IBitSet bitset;
	public final int hashCount;

	private boolean persisting = false;

	@Override
	public boolean isPersisting() {
		return persisting;
	}

	@Override
	public void persisting(boolean b) {
		persisting = b;
	}

	BloomFilter(int hashes, IBitSet bitset) {
		this.hashCount = hashes;
		this.bitset = bitset;
	}

	// Murmur is faster than an SHA-based approach and provides as-good
	// collision
	// resistance. The combinatorial generation approach described in
	// http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf
	// does prove to work in actual tests, and is obviously faster
	// than performing further iterations of murmur.
	protected abstract void hash(ByteBuffer b, int position, int remaining,
			long seed, long[] result);

	// note that this method uses the threadLocal that may be longer than
	// hashCount
	// to avoid generating a lot of garbage since stack allocation currently
	// does not support stores
	// (CASSANDRA-6609). it returns the array so that the caller does not need
	// to perform
	// a second threadlocal lookup.
	private long[] indexes(ByteBuffer key) {
		// we use the same array both for storing the hash result, and for
		// storing the indexes we return,
		// so that we do not need to allocate two arrays.
		long[] indexes = reusableIndexes.get();
		hash(key, key.position(), key.remaining(), 0L, indexes);
		setIndexes(indexes[0], indexes[1], hashCount, bitset.capacity(),
				indexes);
		return indexes;
	}

	private void setIndexes(long base, long inc, int count, long max,
			long[] results) {
		for (int i = 0; i < count; i++) {
			// todo we should test another algorithm from harvard--
			// Gi(x)=H1(x)+iH2(x), h1 and h2 means dividing one 128 hash digest
			// into two 64 digest
			// i means the number of hash
			results[i] = SNUtilities.abs(base % max);
			base += inc;
		}
	}

	public void add(ByteBuffer key) {
		long[] indexes = indexes(key);
		for (int i = 0; i < hashCount; i++) {
			bitset.set(indexes[i]);
		}
	}

	public final boolean isPresent(ByteBuffer key) {
		long[] indexes = indexes(key);
		for (int i = 0; i < hashCount; i++) {
			if (!bitset.get(indexes[i])) {
				return false;
			}
		}
		return true;
	}

	public void clear() {
		bitset.clear();
	}

	public void close() throws IOException {
		bitset.close();
	}

}
