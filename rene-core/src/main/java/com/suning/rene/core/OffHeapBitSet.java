package com.suning.rene.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Off-heap bitset, file compatible with OpeBitSet
 * 
 * Created by tiger on 14-3-20.
 */
public class OffHeapBitSet implements IBitSet {
	private final Memory bytes;

	public OffHeapBitSet(long numBits) {
		// OpenBitSet.bits2words calculation is there for backward
		// compatibility.
		long wordCount = OpenBitSet.bits2words(numBits);
		if (wordCount > Integer.MAX_VALUE)
			throw new UnsupportedOperationException(
					"Bloom filter size is > 16GB, reduce the bloom_filter_fp_chance");
		try {
			long byteCount = wordCount * 8L;
			bytes = RefCountedMemory.allocate(byteCount);
		} catch (OutOfMemoryError e) {
			throw new RuntimeException(
					"Out of native memory occured, You can avoid it by increasing the system ram space or by increasing bloom_filter_fp_chance.");
		}
		// flush/clear the existing memory.
		clear();
	}

	private OffHeapBitSet(Memory bytes) {
		this.bytes = bytes;
	}

	public long capacity() {
		return bytes.size() * 8;
	}

	public boolean get(long index) {
		long i = index >> 3;
		long bit = index & 0x7;
		int bitmask = 0x1 << bit;
		return (bytes.getByte(i) & bitmask) != 0;
	}

	public void set(long index) {
		long i = index >> 3;
		long bit = index & 0x7;
		int bitmask = 0x1 << bit;
		bytes.setByte(i, (byte) (bitmask | bytes.getByte(i)));
	}

	public void set(long offset, byte b) {
		bytes.setByte(offset, b);
	}

	public void clear(long index) {
		long i = index >> 3;
		long bit = index & 0x7;
		int bitmask = 0x1 << bit;
		int nativeByte = (bytes.getByte(i) & 0xFF);
		nativeByte &= ~bitmask;
		bytes.setByte(i, (byte) nativeByte);
	}

	public void clear() {
		bytes.setMemory(0, bytes.size(), (byte) 0);
	}

	public void serialize(DataOutput out) throws IOException {
		out.writeInt((int) (bytes.size() / 8));
		for (long i = 0; i < bytes.size();) {
			long value = ((bytes.getByte(i++) & 0xff) << 0)
					+ ((bytes.getByte(i++) & 0xff) << 8)
					+ ((bytes.getByte(i++) & 0xff) << 16)
					+ ((long) (bytes.getByte(i++) & 0xff) << 24)
					+ ((long) (bytes.getByte(i++) & 0xff) << 32)
					+ ((long) (bytes.getByte(i++) & 0xff) << 40)
					+ ((long) (bytes.getByte(i++) & 0xff) << 48)
					+ ((long) bytes.getByte(i++) << 56);
			out.writeLong(value);
		}
	}

	public long serializedSize() {
		return 1;
		// return type.sizeof((int) bytes.size()) + bytes.size();
	}

	public static OffHeapBitSet deserialize(DataInput in) throws IOException {
		long byteCount = in.readInt() * 8L;
		Memory memory = RefCountedMemory.allocate(byteCount);
		for (long i = 0; i < byteCount;) {
			long v = in.readLong();
			memory.setByte(i++, (byte) (v >>> 0));
			memory.setByte(i++, (byte) (v >>> 8));
			memory.setByte(i++, (byte) (v >>> 16));
			memory.setByte(i++, (byte) (v >>> 24));
			memory.setByte(i++, (byte) (v >>> 32));
			memory.setByte(i++, (byte) (v >>> 40));
			memory.setByte(i++, (byte) (v >>> 48));
			memory.setByte(i++, (byte) (v >>> 56));
		}
		return new OffHeapBitSet(memory);
	}

	public void close() throws IOException {
		bytes.free();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OffHeapBitSet))
			return false;
		OffHeapBitSet b = (OffHeapBitSet) o;
		return bytes.equals(b.bytes);
	}

	@Override
	public int hashCode() {
		// Similar to open bitset.
		long h = 0;
		for (long i = bytes.size(); --i >= 0;) {
			h ^= bytes.getByte(i);
			h = (h << 1) | (h >>> 63); // rotate left
		}
		return (int) ((h >> 32) ^ h) + 0x98761234;
	}
}