package com.suning.rene.utils;

import com.google.common.io.PatternFilenameFilter;
import com.suning.rene.io.FSReadError;
import com.suning.rene.io.FSWriteError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.text.DecimalFormat;

/**
 * Created by tiger on 14-3-25.
 */
public class FileUtils {
	private static final Logger logger = LoggerFactory
			.getLogger(FileUtils.class);
	private static final double KB = 1024d;
	private static final double MB = 1024 * 1024d;
	private static final double GB = 1024 * 1024 * 1024d;
	private static final double TB = 1024 * 1024 * 1024 * 1024d;

	private static final DecimalFormat df = new DecimalFormat("#.##");

	private static final Method cleanerMethod;

	static {
		Method m;
		try {
			m = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
		} catch (Exception e) {
			// Perhaps a non-sun-derived JVM - contributions welcome
			logger.info("Cannot initialize un-mmaper.  (Are you using a non-SUN JVM?)  Compacted data files will not be removed promptly.  Consider using a SUN JVM or using standard disk access mode");
			m = null;
		}
		cleanerMethod = m;
	}

	public static void deleteWithConfirm(String file) {
		deleteWithConfirm(new File(file));
	}

	public static void deleteWithConfirm(File file) {
		assert file.exists() : "attempted to delete non-existing file "
				+ file.getName();
		if (logger.isDebugEnabled())
			logger.debug("Deleting " + file.getName());
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			throw new FSWriteError(e, file);
		}
	}

	public static void clean(MappedByteBuffer buffer) {
		try {
			Object cleaner = cleanerMethod.invoke(buffer);
			cleaner.getClass().getMethod("clean").invoke(cleaner);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createDirectory(String directory) {
		createDirectory(new File(directory));
	}

	public static void createDirectory(File directory) {
		if (!directory.exists()) {
			if (!directory.mkdirs())
				throw new FSWriteError(new IOException("Failed to mkdirs "
						+ directory), directory);
		}
	}

	public static File[] listDirectory(File directory) {
		if (directory.isDirectory()) {
			return directory.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
		}
		throw new FSReadError(new IOException(directory.getName()
				+ " is not a directory"), directory);
	}

	public static File[] listCommitLog(File nl) {
		if (nl.exists()) {
			return nl.listFiles(new PatternFilenameFilter(
					"CommitLog-[23]-\\d{13}.log"));
		}
		throw new FSReadError(
				new IOException(nl.getName() + " does not exist"), nl);
	}

	public static File[] listBloomFilter(File nl) {
		if (nl.exists()) {
			return nl.listFiles(new PatternFilenameFilter("((pos)|(neg)).bf"));
		}
		throw new FSReadError(
				new IOException(nl.getName() + " does not exist"), nl);
	}

}
