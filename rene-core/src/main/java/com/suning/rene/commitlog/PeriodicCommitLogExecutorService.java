package com.suning.rene.commitlog;

import com.google.common.util.concurrent.Uninterruptibles;
import com.suning.rene.ServerDescriptor;
import com.suning.rene.utils.SNUtilities;
import com.suning.rene.utils.WrappedRunnable;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by tiger on 14-3-27.
 */
public class PeriodicCommitLogExecutorService
		implements
			ICommitLogExecutorService {
	private final BlockingQueue<Runnable> queue;
	protected volatile long completedTaskCount = 0;
	private final Thread appendingThread;
	private volatile boolean run = true;

	public PeriodicCommitLogExecutorService(final CommitLog commitLog) {
		queue = new LinkedBlockingQueue<Runnable>(
				ServerDescriptor.getCommitLogPeriodicQueueSize());
		Runnable runnable = new WrappedRunnable() {
			public void runMayThrow() throws Exception {
				while (run) {
					Runnable r = queue.poll(100, TimeUnit.MILLISECONDS);
					if (r == null)
						continue;
					r.run();
					completedTaskCount++;
				}
				commitLog.sync();
			}
		};
		appendingThread = new Thread(runnable, "COMMIT-LOG-WRITER");
		appendingThread.start();

		final Callable syncer = new Callable() {
			public Object call() throws Exception {
				commitLog.sync();
				return null;
			}
		};

		new Thread(new Runnable() {
			public void run() {
				while (run) {
					SNUtilities.waitOnFuture(submit(syncer));
					Uninterruptibles.sleepUninterruptibly(
							ServerDescriptor.getCommitLogSyncPeriod(),
							TimeUnit.MILLISECONDS);
				}
			}
		}, "PERIODIC-COMMIT-LOG-SYNCER").start();

	}

	public <T> Future<T> submit(Callable<T> task) {
		FutureTask<T> ft = new FutureTask<T>(task);
		try {
			queue.put(ft);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return ft;
	}

	public void shutdown() {
		new Thread(new WrappedRunnable() {
			public void runMayThrow() throws InterruptedException, IOException {
				while (!queue.isEmpty())
					Thread.sleep(100);
				run = false;
				appendingThread.join();
			}
		}, "Commitlog Shutdown").start();
	}

	public void awaitTermination() throws InterruptedException {
		appendingThread.join();
	}

	public long getPendingTasks() {
		return queue.size();
	}

	public long getCompletedTasks() {
		return completedTaskCount;
	}
}
