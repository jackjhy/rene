package com.suning.rene.commitlog;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by tiger on 14-3-27.
 */
public interface ICommitLogExecutorService {
	/**
	 * Get the number of completed tasks
	 */
	public long getCompletedTasks();

	/**
	 * Get the number of tasks waiting to be executed
	 */
	public long getPendingTasks();

	public <T> Future<T> submit(Callable<T> task);

	/** shuts down the CommitLogExecutor in an orderly fashion */
	public void shutdown();

	/** Blocks until shutdown is complete. */
	public void awaitTermination() throws InterruptedException;
}
