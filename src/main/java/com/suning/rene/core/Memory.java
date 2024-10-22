package com.suning.rene.core;

import com.sun.jna.Native;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An off-heap region of memory that must be manually free'd when no longer
 * needed.
 */
public class Memory {
	private static final Unsafe unsafe = NativeAllocator.unsafe;
	// TODO should be replaced by configuration.allocator 's value
	private static final IAllocator allocator = new NativeAllocator();
	private static final long BYTE_ARRAY_BASE_OFFSET = unsafe
			.arrayBaseOffset(byte[].class);

	private static final boolean bigEndian = ByteOrder.nativeOrder().equals(
			ByteOrder.BIG_ENDIAN);
	private static final boolean unaligned;

	static {
		String arch = System.getProperty("os.arch");
		unaligned = arch.equals("i386") || arch.equals("x86")
				|| arch.equals("amd64") || arch.equals("x86_64");
	}

	protected long peer;
	// size of the memory region
	private final long size;

	protected Memory(long bytes) {
		size = bytes;
		peer = allocator.allocate(size);
	}

	public static Memory allocate(long bytes) {
		if (bytes < 0)
			throw new IllegalArgumentException();

		return new Memory(bytes);
	}

	public void setByte(long offset, byte b) {
		checkPosition(offset);
		unsafe.putByte(peer + offset, b);
	}

	public void setMemory(long offset, long bytes, byte b) {
		// check if the last element will fit into the memory
		checkPosition(offset + bytes - 1);
		unsafe.setMemory(peer + offset, bytes, b);
	}

	public void setLong(long offset, long l) {
		checkPosition(offset);
		if (unaligned) {
			unsafe.putLong(peer + offset, l);
		} else {
			putLongByByte(peer + offset, l);
		}
	}

	private void putLongByByte(long address, long value) {
		if (bigEndian) {
			unsafe.putByte(address, (byte) (value >> 56));
			unsafe.putByte(address + 1, (byte) (value >> 48));
			unsafe.putByte(address + 2, (byte) (value >> 40));
			unsafe.putByte(address + 3, (byte) (value >> 32));
			unsafe.putByte(address + 4, (byte) (value >> 24));
			unsafe.putByte(address + 5, (byte) (value >> 16));
			unsafe.putByte(address + 6, (byte) (value >> 8));
			unsafe.putByte(address + 7, (byte) (value));
		} else {
			unsafe.putByte(address + 7, (byte) (value >> 56));
			unsafe.putByte(address + 6, (byte) (value >> 48));
			unsafe.putByte(address + 5, (byte) (value >> 40));
			unsafe.putByte(address + 4, (byte) (value >> 32));
			unsafe.putByte(address + 3, (byte) (value >> 24));
			unsafe.putByte(address + 2, (byte) (value >> 16));
			unsafe.putByte(address + 1, (byte) (value >> 8));
			unsafe.putByte(address, (byte) (value));
		}
	}

	public void setInt(long offset, int l) {
		checkPosition(offset);
		if (unaligned) {
			unsafe.putInt(peer + offset, l);
		} else {
			putIntByByte(peer + offset, l);
		}
	}

	private void putIntByByte(long address, int value) {
		if (bigEndian) {
			unsafe.putByte(address, (byte) (value >> 24));
			unsafe.putByte(address + 1, (byte) (value >> 16));
			unsafe.putByte(address + 2, (byte) (value >> 8));
			unsafe.putByte(address + 3, (byte) (value));
		} else {
			unsafe.putByte(address + 3, (byte) (value >> 24));
			unsafe.putByte(address + 2, (byte) (value >> 16));
			unsafe.putByte(address + 1, (byte) (value >> 8));
			unsafe.putByte(address, (byte) (value));
		}
	}

	/**
	 * Transfers count bytes from buffer to Memory
	 * 
	 * @param memoryOffset
	 *            start offset in the memory
	 * @param buffer
	 *            the data buffer
	 * @param bufferOffset
	 *            start offset of the buffer
	 * @param count
	 *            number of bytes to transfer
	 */
	public void setBytes(long memoryOffset, byte[] buffer, int bufferOffset,
			int count) {
		if (buffer == null)
			throw new NullPointerException();
		else if (bufferOffset < 0 || count < 0
				|| bufferOffset + count > buffer.length)
			throw new IndexOutOfBoundsException();
		else if (count == 0)
			return;

		checkPosition(memoryOffset);
		long end = memoryOffset + count;
		checkPosition(end - 1);

		unsafe.copyMemory(buffer, BYTE_ARRAY_BASE_OFFSET + bufferOffset, null,
				peer + memoryOffset, count);
	}

	public byte getByte(long offset) {
		checkPosition(offset);
		return unsafe.getByte(peer + offset);
	}

	public long getLong(long offset) {
		checkPosition(offset);
		if (unaligned) {
			return unsafe.getLong(peer + offset);
		} else {
			return getLongByByte(peer + offset);
		}
	}

	private long getLongByByte(long address) {
		if (bigEndian) {
			return (((long) unsafe.getByte(address)) << 56)
					| (((long) unsafe.getByte(address + 1) & 0xff) << 48)
					| (((long) unsafe.getByte(address + 2) & 0xff) << 40)
					| (((long) unsafe.getByte(address + 3) & 0xff) << 32)
					| (((long) unsafe.getByte(address + 4) & 0xff) << 24)
					| (((long) unsafe.getByte(address + 5) & 0xff) << 16)
					| (((long) unsafe.getByte(address + 6) & 0xff) << 8)
					| (((long) unsafe.getByte(address + 7) & 0xff));
		} else {
			return (((long) unsafe.getByte(address + 7)) << 56)
					| (((long) unsafe.getByte(address + 6) & 0xff) << 48)
					| (((long) unsafe.getByte(address + 5) & 0xff) << 40)
					| (((long) unsafe.getByte(address + 4) & 0xff) << 32)
					| (((long) unsafe.getByte(address + 3) & 0xff) << 24)
					| (((long) unsafe.getByte(address + 2) & 0xff) << 16)
					| (((long) unsafe.getByte(address + 1) & 0xff) << 8)
					| (((long) unsafe.getByte(address) & 0xff));
		}
	}

	public int getInt(long offset) {
		checkPosition(offset);
		if (unaligned) {
			return unsafe.getInt(peer + offset);
		} else {
			return getIntByByte(peer + offset);
		}
	}

	private int getIntByByte(long address) {
		if (bigEndian) {
			return (((int) unsafe.getByte(address)) << 24)
					| (((int) unsafe.getByte(address + 1) & 0xff) << 16)
					| (((int) unsafe.getByte(address + 2) & 0xff) << 8)
					| (((int) unsafe.getByte(address + 3) & 0xff));
		} else {
			return (((int) unsafe.getByte(address + 3)) << 24)
					| (((int) unsafe.getByte(address + 2) & 0xff) << 16)
					| (((int) unsafe.getByte(address + 1) & 0xff) << 8)
					| (((int) unsafe.getByte(address) & 0xff));
		}
	}

	/**
	 * Transfers count bytes from Memory starting at memoryOffset to buffer
	 * starting at bufferOffset
	 * 
	 * @param memoryOffset
	 *            start offset in the memory
	 * @param buffer
	 *            the data buffer
	 * @param bufferOffset
	 *            start offset of the buffer
	 * @param count
	 *            number of bytes to transfer
	 */
	public void getBytes(long memoryOffset, byte[] buffer, int bufferOffset,
			int count) {
		if (buffer == null)
			throw new NullPointerException();
		else if (bufferOffset < 0 || count < 0
				|| count > buffer.length - bufferOffset)
			throw new IndexOutOfBoundsException();
		else if (count == 0)
			return;

		checkPosition(memoryOffset);
		long end = memoryOffset + count;
		checkPosition(end - 1);

		unsafe.copyMemory(null, peer + memoryOffset, buffer,
				BYTE_ARRAY_BASE_OFFSET + bufferOffset, count);
	}

	private void checkPosition(long offset) {
		assert peer != 0 : "Memory was freed";
		assert offset >= 0 && offset < size : "Illegal offset: " + offset
				+ ", size: " + size;
	}

	public void free() {
		assert peer != 0;
		allocator.free(peer);
		peer = 0;
	}

	public long size() {
		assert peer != 0;
		return size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Memory))
			return false;
		Memory b = (Memory) o;
		if (peer == b.peer && size == b.size)
			return true;
		return false;
	}

	public ByteBuffer[] asByteBuffers() {
		if (size() == 0)
			return new ByteBuffer[0];

		ByteBuffer[] result = new ByteBuffer[(int) (size() / Integer.MAX_VALUE) + 1];
		long offset = 0;
		int size = (int) (size() / result.length);
		for (int i = 0; i < result.length - 1; i++) {
			result[i] = Native.getDirectByteBuffer(peer + offset, size);
			offset += size;
		}
		result[result.length - 1] = Native.getDirectByteBuffer(peer + offset,
				(int) (size() - offset));
		return result;
	}
}
