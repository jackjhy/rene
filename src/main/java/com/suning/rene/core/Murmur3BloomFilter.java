package com.suning.rene.core;

import java.nio.ByteBuffer;

/**
 * Created by tiger on 14-3-21.
 */
public class Murmur3BloomFilter extends BloomFilter {

	public static final Murmur3BloomFilterSerializer serializer = new Murmur3BloomFilterSerializer();

	public Murmur3BloomFilter(int hashes, IBitSet bs) {
		super(hashes, bs);
	}

	@Override
	protected void hash(ByteBuffer b, int position, int remaining, long seed,
			long[] result) {
		MurmurHash.hash3_x64_128(b, b.position(), b.remaining(), seed, result);
	}

	@Override
	public long serializedSize() {
		// todo
		throw new UnsupportedOperationException();
	}

	public static class Murmur3BloomFilterSerializer
			extends
				BloomFilterSerializer {
		protected BloomFilter createFilter(int hashes, IBitSet bs) {
			return new Murmur3BloomFilter(hashes, bs);
		}
	}
}
