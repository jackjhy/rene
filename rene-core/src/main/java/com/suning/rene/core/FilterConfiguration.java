package com.suning.rene.core;

import com.suning.rene.ServerDescriptor;
import com.suning.rene.utils.FileUtils;

import java.io.*;
import java.util.Properties;

/**
 * Created by tiger on 14-4-4.
 */
public class FilterConfiguration {

	int hashNumber;
	String name;
	long numberOfElement;
	boolean offHeap;
	boolean persisting = false;

	public static FilterConfiguration readConfiguration(String name)
			throws IOException {
		FilterConfiguration conf = new FilterConfiguration();
		conf.name = name;
		File f = new File(ServerDescriptor.getCommitLogLocation(name)
				+ File.separator + "." + name);
		if (f.exists()) {
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			conf.numberOfElement = Long.parseLong(p
					.getProperty("numberOfElement"));
			return conf;
		}
		return null;
	}

	public static FilterConfiguration readConfiguration(File f)
			throws IOException {
		FilterConfiguration conf = new FilterConfiguration();
		conf.name = f.getName().substring(1);
		if (f.exists()) {
			Properties p = new Properties();
			p.load(new FileInputStream(f));
			conf.numberOfElement = Long.parseLong(p
					.getProperty("numberOfElement"));
			return conf;
		}
		return null;
	}

	public void save(File f) throws ReneException {
		Properties p = new Properties();
		p.setProperty("numberOfElement", Long.toString(numberOfElement));
		try {
			FileOutputStream o = new FileOutputStream(f);
			p.store(o, null);
			o.close();
		} catch (IOException e) {
			throw new ReneException(
					"error when store filter configuration file", e);
		}

	}

	public FilterConfiguration(String name, long noe) {
		this.name = name;
		this.numberOfElement = noe;
	}

	public FilterConfiguration() {
	}
}
