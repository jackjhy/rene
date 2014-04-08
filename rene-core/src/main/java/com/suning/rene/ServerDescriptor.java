package com.suning.rene;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import com.suning.rene.core.ReneException;
import com.suning.rene.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Created by tiger on 14-3-25.
 */
public class ServerDescriptor {

	private static final Logger logger = LoggerFactory
			.getLogger(ServerDescriptor.class);

	static final String RENE_HOME_PATH = "RENE_HOME";
	static final String RENE_HOME_PATH_1 = "rene.home";
	static String rootPath;

	static Properties p = null;

	public static String getCommitLogLocation(String nl) {
		String path = getRootPath() + File.separatorChar + nl;
		FileUtils.createDirectory(path);
		return path;
	}

	public static String getRootPath() {
		if (rootPath != null)
			return rootPath;
		String home = System.getenv(RENE_HOME_PATH);
		if (home == null || home.trim().length() == 0)
			home = System.getProperty(RENE_HOME_PATH_1);
		if (home == null || home.trim().length() == 0)
			logger.error("can not find rene home, plz check again");
		return "/home/tiger/rene_test";
	}

	public static long getCommitLogSegmentSize() {
		if (p == null)
			p = loadProp();
		long size = 1024 * 1024;
		try {
			size = Long.parseLong(p.getProperty("com.suning.rene.css"));
		} catch (NumberFormatException e) {
		}
		return size;
	}

	public static int getCommitLogPeriodicQueueSize() {
		if (p == null)
			p = loadProp();
		int size = 50;
		try {
			size = Integer.parseInt(p.getProperty("com.suning.rene.cpqs"));
		} catch (NumberFormatException e) {
		}
		return size;
	}

	public static long getCommitLogSyncPeriod() {
		if (p == null)
			p = loadProp();
		int size = 10;
		try {
			size = Integer.parseInt(p.getProperty("com.suning.rene.csp"));
		} catch (NumberFormatException e) {
		}
		return size;
	}

	public static Date getPersistTime() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 2);
        cal.add(Calendar.HOUR,24);
		return cal.getTime();
	}

	private static Properties loadProp() {
		Properties p = new Properties();
		File file = new File(getRootPath() + File.separator + "conf.properties");
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				p.load(fis);
			} catch (IOException e) {
				logger.error("error when loading config", e);
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
					}
			}
		}
		return p;
	}

	public static int getAsciiPort() {
		if (p == null)
			p = loadProp();
		int asciiPort = 3434;
		try {
			asciiPort = Integer.parseInt(p
					.getProperty("com.suning.rene.ascii.port"));
		} catch (NumberFormatException e) {
			logger.warn("got error when read ascii port from conf,use default 3434");
		}
		return asciiPort;
	}

	public static int getWorkerThreadNumber() {
		int size = 0;
		try {
			size = Integer.parseInt(p
					.getProperty("com.suning.rene.worker.size"));
		} catch (NumberFormatException e) {
		}
		return size;
	}
}