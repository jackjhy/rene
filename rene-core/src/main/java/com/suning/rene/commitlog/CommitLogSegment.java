package com.suning.rene.commitlog;

import com.suning.rene.ServerDescriptor;
import com.suning.rene.io.FSWriteError;
import com.suning.rene.utils.FileUtils;
import com.suning.rene.utils.PureJavaCrc32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Checksum;

/**
 * Created by tiger on 14-3-25.
 */

public class CommitLogSegment {
	private static final Logger logger = LoggerFactory
			.getLogger(CommitLogSegment.class);

	private final static long idBase = System.currentTimeMillis();
	private final static AtomicInteger nextId = new AtomicInteger(1);

	private static Charset UTF8 = Charset.forName("utf-8");

	// The commit log entry overhead in bytes (int: length + long: head checksum
	// + long: tail checksum)
	static final int ENTRY_OVERHEAD_SIZE = 4 + 8 + 8;

	static final int RETURN = '\n';

	public final long id;

	private final File logFile;
	private final RandomAccessFile logFileAccessor;

	private boolean needsSync = false;

	private final MappedByteBuffer buffer;
	private final DataOutputStream bufferStream;
	private boolean closed;

	private String nl;

	public final CommitLogDescriptor descriptor;

	public static long getNextId() {
		return idBase + nextId.getAndIncrement();
	}
	/**
	 * Constructs a new segment file.
	 * 
	 * @param filePath
	 *            if not null, recycles the existing file by renaming it and
	 *            truncating it to CommitLog.SEGMENT_SIZE.
	 */
	CommitLogSegment(String nl, String filePath) {
		this.nl = nl;
		id = getNextId();
		descriptor = new CommitLogDescriptor(id);
		logFile = new File(ServerDescriptor.getCommitLogLocation(nl),
				descriptor.fileName());
		boolean isCreating = true;

		try {
			if (filePath != null) {
				File oldFile = new File(filePath);

				if (oldFile.exists()) {
					logger.debug(
							"Re-using discarded CommitLog segment for {} from {}",
							id, filePath);
					if (!oldFile.renameTo(logFile))
						throw new IOException("Rename from " + filePath
								+ " to " + id + " failed");
					isCreating = false;
				}
			}

			// Open the initial the segment file
			logFileAccessor = new RandomAccessFile(logFile, "rw");

			if (isCreating)
				logger.debug("Creating new commit log segment {}",
						logFile.getPath());

			// Map the segment, extending or truncating it to the standard
			// segment size
			logFileAccessor.setLength(ServerDescriptor
					.getCommitLogSegmentSize());

			buffer = logFileAccessor.getChannel().map(
					FileChannel.MapMode.READ_WRITE, 0,
					ServerDescriptor.getCommitLogSegmentSize());
			bufferStream = new DataOutputStream(new ByteBufferOutputStream(
					buffer));
			buffer.putInt(CommitLog.END_OF_SEGMENT_MARKER);
			buffer.position(0);

			needsSync = true;
		} catch (IOException e) {
			throw new FSWriteError(e, logFile);
		}
	}

	/**
	 * Completely discards a segment file by deleting it. (Potentially blocking
	 * operation)
	 */
	public void discard(boolean deleteFile) {
		// TODO shouldn't we close the file when we're done writing to it, which
		// comes (potentially) much earlier than it's eligible for recyling?
		close();
		if (deleteFile)
			FileUtils.deleteWithConfirm(logFile);
	}

	/**
	 * Recycle processes an unneeded segment file for reuse.
	 * 
	 * @return a new CommitLogSegment representing the newly reusable segment.
	 */
	public CommitLogSegment recycle() {
		// writes an end-of-segment marker at the very beginning of the file and
		// closes it
		buffer.position(0);
		buffer.putInt(CommitLog.END_OF_SEGMENT_MARKER);
		buffer.position(0);

		try {
			sync();
		} catch (FSWriteError e) {
			logger.error("I/O error flushing " + this + " " + e);
			throw e;
		}

		close();

		return new CommitLogSegment(nl, getPath());
	}

	/**
	 * @return true if there is room to write() @param size to this segment
	 */
	public boolean hasCapacityFor(long size) {
		return size <= buffer.remaining();
	}

	/**
	 * Forces a disk flush for this segment file.
	 */
	public void sync() {
		if (needsSync) {
			try {
				buffer.force();
			} catch (Exception e) // MappedByteBuffer.force() does not declare
									// IOException but can actually throw it
			{
				throw new FSWriteError(e, getPath());
			}
			needsSync = false;
		}
	}

	/**
	 * @return the file path to this segment
	 */
	public String getPath() {
		return logFile.getPath();
	}

	/**
	 * @return the file name of this segment
	 */
	public String getName() {
		return logFile.getName();
	}

	/**
	 * Close the segment file.
	 */
	public void close() {
		if (closed)
			return;

		try {
			FileUtils.clean(buffer);
			logFileAccessor.close();
			closed = true;
		} catch (IOException e) {
			throw new FSWriteError(e, getPath());
		}
	}

	@Override
	public String toString() {
		return "CommitLogSegment(" + getPath() + ')';
	}

	public int position() {
		return buffer.position();
	}

	public boolean write(String value) throws IOException {
		assert !closed;
		int length = value.getBytes(UTF8).length;
		if (!hasCapacityFor(4 + length))
			return false;
		bufferStream.writeInt(length);;
		bufferStream.write(value.getBytes(UTF8), 0, length);
		needsSync = true;
		return true;
	}

}