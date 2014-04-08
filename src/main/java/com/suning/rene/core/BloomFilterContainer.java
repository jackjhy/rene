package com.suning.rene.core;

import com.google.common.io.PatternFilenameFilter;
import com.suning.rene.ServerDescriptor;
import com.suning.rene.commitlog.CommitLog;
import com.suning.rene.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tiger on 14-3-21.
 */
public class BloomFilterContainer implements IMCCommand {
	Logger logger = LoggerFactory.getLogger(BloomFilterContainer.class);
	private static Map<String, IFilter> addedFiltersMap = new ConcurrentHashMap<String, IFilter>();
	private static Map<String, IFilter> deletedFiltersMap = new ConcurrentHashMap<String, IFilter>();

    public static Map<String, FilterConfiguration> getFilterConfigurationMap() {
        return filterConfigurationMap;
    }

    private static Map<String, FilterConfiguration> filterConfigurationMap = new ConcurrentHashMap<String, FilterConfiguration>();
	private final static Charset US_ASCII = Charset.forName("us-ascii");

	public static BloomFilterContainer instance = new BloomFilterContainer();
	private CommitLog commitLog = null;

	private BloomFilterContainer() {
		File[] ls = FileUtils.listDirectory(new File(ServerDescriptor
				.getRootPath()));
		if (ls != null && ls.length > 0) {
			for (File lf : ls) {
				String nl = lf.getName();
				File[] flag = lf.listFiles(new PatternFilenameFilter("." + nl));
				if (flag == null || flag.length == 0)
					continue;
				try {
					FilterConfiguration fc = FilterConfiguration
							.readConfiguration(flag[0]);
					filterConfigurationMap.put(nl, fc);
				} catch (IOException e) {
					logger.warn("error when read filter configuration :" + nl);
					continue;
				}
				File[] filteFiles = FileUtils.listBloomFilter(lf);
				if (filteFiles != null && filteFiles.length > 0) {
					for (File blf : filteFiles) {
						if (blf.getName().startsWith("pos"))
							try {
								addedFiltersMap
										.put(nl,
												FilterFactory
														.deserialize(
																new DataInputStream(
																		new FileInputStream(
																				blf)),
																true));
							} catch (Exception e) {
								logger.warn("");
							}
						if (blf.getName().startsWith("neg"))
							try {
								deletedFiltersMap
										.put(nl,
												FilterFactory
														.deserialize(
																new DataInputStream(
																		new FileInputStream(
																				blf)),
																true));
							} catch (Exception e) {
								logger.warn("");
							}
					}
				} else {
					logger.warn("Filters file are not exist or uncorrected number");
				}
				File[] commitlogs = FileUtils.listCommitLog(lf);
				if (commitlogs != null && commitlogs.length > 0) {
					if (addedFiltersMap.containsKey(nl)
							&& addedFiltersMap.get(nl) != null) {
						IFilter nf = deletedFiltersMap.get(nl);
						if (nf == null) {
							nf = FilterFactory
									.getFilter(
											filterConfigurationMap.get(nl).numberOfElement / 10,
											true);
						}
						deletedFiltersMap.put(nl, nf);
						CommitLog.recover(nl, addedFiltersMap.get(nl), nf);
						// persist bf
						persist(nl, new IFilter[]{addedFiltersMap.get(nl), nf});
						// clear commitlogs
						for (File cf : commitlogs)
							FileUtils.deleteWithConfirm(cf);
					}
				}
				commitLog = new CommitLog();
			}

			new Timer(true).scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					for (FilterConfiguration f : filterConfigurationMap
							.values()) {
						File[] commitlogs = FileUtils.listCommitLog(new File(
								ServerDescriptor.getCommitLogLocation(f.name)));
						commitLog.newInstance(f.name);
						persist(f.name,
								new IFilter[]{addedFiltersMap.get(f.name),
										deletedFiltersMap.get(f.name)});
						for (File ff : commitlogs)
							FileUtils.deleteWithConfirm(ff);
					}
				}
			}, ServerDescriptor.getPersistTime(), 24 * 60 * 60 * 1000);
		}
	}

	/**
	 * 
	 * @param key
	 *            name/size/needPersist
	 * @return void
	 * @throws Exception
	 */
	@Override
	public void add(String key) throws ReneException {
		String[] params = key.split("/");
		if (params.length != 3)
			throw new ReneException("parameters are unvalidated", null);
		if (addedFiltersMap.containsKey(params[0].trim())) {
			throw new ReneException("listing name should be unique", null);
		}
		filterConfigurationMap.put(params[0], new FilterConfiguration(
				params[0], Long.parseLong(params[1])));
		IFilter pf = FilterFactory.getFilter(Integer.parseInt(params[1]),
				FilterFactory.DEFAULT_FALSE_POS_PROBABILITY, true);
		IFilter nf = FilterFactory.getFilter(Integer.parseInt(params[1]) / 10,
				FilterFactory.DEFAULT_FALSE_POS_PROBABILITY, true);
		if (params[2].trim().equalsIgnoreCase("1")) {
			String path = ServerDescriptor.getCommitLogLocation(params[0]
					.trim());
			filterConfigurationMap.get(params[0]).save(
					new File(path + File.separator + "." + params[0]));
			persist(params[0].trim(), new IFilter[]{pf, nf});
		}
	}

	private void persist(String nl, IFilter[] filters) {
		File f = null;
		DataOutputStream dof = null;
		if (filters[0] != null) {
			try {
				f = new File(ServerDescriptor.getCommitLogLocation(nl)
						+ File.separator + "pos.bf");
				filters[0].persisting(true);
				dof = new DataOutputStream(new FileOutputStream(f));
				FilterFactory.serialize(filters[0], dof);
			} catch (IOException e) {
			} finally {
				if (dof != null)
					try {
						dof.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				filters[0].persisting(false);
			}
		}
		if (filters[1] != null) {
			try {
				f = new File(ServerDescriptor.getCommitLogLocation(nl)
						+ File.separator + "neg.bf");
				filters[1].persisting(true);
				dof = new DataOutputStream(new FileOutputStream(f));
				FilterFactory.serialize(filters[1], dof);
			} catch (IOException e) {
			} finally {
				if (dof != null)
					try {
						dof.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				filters[1].persisting(false);
			}
		}

	}

	@Override
	public void set(String keySubKey) throws ReneException {
		int index = keySubKey.indexOf('/');
		String ln = keySubKey.substring(0, index);
		String key = keySubKey.substring(index + 1);
		if (!addedFiltersMap.containsKey(ln))
			throw new ReneException("this listing does not exist", null);
		if (addedFiltersMap.get(ln).isPersisting())
			throw new ReneException("persisting,plz try later", null);
		try {
			commitLog.add(ln, key);
		} catch (IOException e) {
			logger.error("commitlog write error", e);
			throw new ReneException("commitlog error", e);
		}
		addedFiltersMap.get(ln).add(ByteBuffer.wrap(key.getBytes(US_ASCII)));
	}

	@Override
	public boolean get(String keySubKey) throws ReneException {
		int index = keySubKey.indexOf('/');
		String ln = keySubKey.substring(0, index);
		String key = keySubKey.substring(index + 1);
		if (!addedFiltersMap.containsKey(ln))
			throw new ReneException("this listing does not exist", null);
		if (addedFiltersMap.get(ln).isPersisting())
			throw new ReneException("persisting,plz try later", null);
		ByteBuffer v = ByteBuffer.wrap(key.getBytes(US_ASCII));
		return addedFiltersMap.get(ln).isPresent(v)
				&& (deletedFiltersMap.containsKey(ln) ? !deletedFiltersMap.get(
						ln).isPresent(v) : true);
	}

	@Override
	public boolean del(String key) throws ReneException {
		if (!addedFiltersMap.containsKey(key))
			throw new ReneException("this listing does not exist", null);
		try {
			addedFiltersMap.get(key).close();
			if (deletedFiltersMap.containsKey(key)) {
				deletedFiltersMap.get(key).close();
				deletedFiltersMap.remove(key);
			}
			addedFiltersMap.remove(key);
			FileUtils.deleteWithConfirm(ServerDescriptor
					.getCommitLogLocation(key) + File.separator + "." + key);
		} catch (IOException e) {
			logger.error("error when close bitset", e);
			throw new ReneException("error when close bitset", e);
		}
		return false;
	}

	@Override
	public boolean replace(String keySubKey) throws ReneException {
		int index = keySubKey.indexOf('/');
		String ln = keySubKey.substring(0, index);
		String key = keySubKey.substring(index + 1);
		if (!addedFiltersMap.containsKey(ln))
			throw new ReneException("this listing does not exist", null);
		if (!deletedFiltersMap.containsKey(ln)) {
			FilterConfiguration fc = filterConfigurationMap.get(ln);
			IFilter f = FilterFactory.getFilter(fc == null
					? 1000
					: (fc.numberOfElement / 10),
					FilterFactory.DEFAULT_FALSE_POS_PROBABILITY, true);
			deletedFiltersMap.put(ln, f);
			persist(ln, new IFilter[]{null, f});
		}
		deletedFiltersMap.get(key).add(ByteBuffer.wrap(key.getBytes(US_ASCII)));
		return false;
	}

	@Override
	public String stats() throws ReneException {
		throw new ReneException("not supported this operation", null);
	}
}
