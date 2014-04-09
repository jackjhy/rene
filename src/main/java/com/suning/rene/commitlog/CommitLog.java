package com.suning.rene.commitlog;

import com.google.common.io.PatternFilenameFilter;
import com.suning.rene.ServerDescriptor;
import com.suning.rene.core.IFilter;
import com.suning.rene.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by tiger on 14-3-25.
 */
public class CommitLog {
	public static int END_OF_SEGMENT_MARKER = 1;

	private HashMap<String, CommitLogSegment> cls = new HashMap<String, CommitLogSegment>();

	private final ICommitLogExecutorService executor;

	public static void recover(String nl, IFilter abf, IFilter dbf) {
		File f = new File(ServerDescriptor.getCommitLogLocation(nl));
		if (f.exists()) {
			File[] commitLogs = FileUtils.listCommitLog(f);
			if (commitLogs != null && commitLogs.length > 0) {
				RandomAccessFile raf;
				byte[] b = new byte[1024];
				for (File cl : commitLogs) {
					try {
						raf = new RandomAccessFile(cl, "r");
						while (true) {
							int flag = raf.readInt();
							if (flag <= 0)
								break;
							raf.read(b, 0, flag);
							// if start with '+' add it into added filter
							// or add it into deleted filter
							if (b[0] == 43)
								abf.add(ByteBuffer.wrap(b, 1, flag - 1));
							else
								dbf.add(ByteBuffer.wrap(b, 1, flag - 1));
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public CommitLog() {
		File[] nls = FileUtils.listDirectory(new File(ServerDescriptor
				.getDataPath()));
		if (nls != null && nls.length > 0) {
			for (File f : nls) {
				File[] flag = f.listFiles(new PatternFilenameFilter("."
						+ f.getName()));
				if (flag == null || flag.length == 0)
					continue;
				cls.put(f.getName(), new CommitLogSegment(f.getName(), null));
			}
		}
		executor = new PeriodicCommitLogExecutorService(this);
	}

	public CommitLogSegment newInstance(String nl) {
		if (cls.containsKey(nl)) {
			cls.get(nl).sync();
			cls.get(nl).close();
		}
		cls.put(nl, new CommitLogSegment(nl, null));
		return cls.get(nl);
	}

	public void add(String nl, String value) throws IOException {
		if (!cls.containsKey(nl))
			cls.put(nl, new CommitLogSegment(nl, null));
		if (!cls.get(nl).write(value))
			newInstance(nl).write(value);
	}

	public void sync() {
		for (CommitLogSegment c : cls.values())
			c.sync();
	}

	public void shutdownBlocking() throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination();
		for (CommitLogSegment c : cls.values())
			c.close();
	}

}
