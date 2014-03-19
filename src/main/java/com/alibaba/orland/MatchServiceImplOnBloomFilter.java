/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

import com.alibaba.orland.dao.DeletedRecordsDAO;
import com.alibaba.orland.dao.ListsDAO;
import com.alibaba.orland.dao.RecordsDAO;
import com.alibaba.orland.dataobject.Lists;
import com.alibaba.orland.dataobject.Records;
import com.alibaba.orland.util.BloomFilter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * lists name and record value are composed with '-'
 * @author tiger
 */
public class MatchServiceImplOnBloomFilter implements MatchService {

	private BloomFilter<String> targetList;
	private BloomFilter<String> deletedList;
	private double falsePositiveProbability = 0.01;
	private int expectedNumberOfElements = 100000;
	private HashMap<String, Boolean> listsMap = new HashMap<String, Boolean>();
	private ListsDAO listsDao;
	private RecordsDAO recordsDao;
	private DeletedRecordsDAO drsDao;

	public MatchServiceImplOnBloomFilter(double _falsePositiveProbability, int _expectedNumberOfElements, ListsDAO ld, RecordsDAO rd, DeletedRecordsDAO drd) {
		if(_falsePositiveProbability>=0||_falsePositiveProbability<1) falsePositiveProbability = _falsePositiveProbability;
		if(expectedNumberOfElements>10000)expectedNumberOfElements = _expectedNumberOfElements;
		targetList = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements);
		deletedList = new BloomFilter<String>(falsePositiveProbability, expectedNumberOfElements/100);
		listsDao = ld;
		recordsDao = rd;
		drsDao = drd;
	}

	public boolean matchRecordInList(String list, String record) throws OrlandException {
		String _v = list.concat("-").concat(record);
		if (targetList.contains(_v)) {
			if ((listsMap.get(list) != null && !listsMap.get(list)) || deletedList.contains(list.concat("-")) || deletedList.contains(_v)) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public void addNewRecordIntoList(String record, String list) throws OrlandException {
		targetList.add(list.concat("-").concat(record));
	}

	public void addNewRecordIntoGroup(String record, String group) throws OrlandException {
		try {
			//get all list name for this group
			//and then use inner channel to invoke add records into correct list node
			List<Lists> lss = listsDao.getAllWithLikeGroup(group);
			for (Lists _l : lss) {
				addNewRecordIntoList(record, _l.getId());
			}
		} catch (SQLException ex) {
			throw new OrlandException("failed when query data from db");
		}
	}

	public void removeRecordFromLlist(String record, String list) throws OrlandException {
		deletedList.add(list.concat("-").concat(record));
	}

	public void removeLists(String list) throws OrlandException {
		listsMap.put(list, Boolean.FALSE);
		deletedList.add(list.concat("-"));
	}

	public void loadNewList(final String list) throws OrlandException {
		try {
			listsMap.put(list, Boolean.TRUE);
			Lists l = listsDao.queryListsById(list);
			if (l == null) {
				throw new OrlandException("this list does not exsit");
			}
			String group = addSingleQuotationMarks(l.getContent());
			recordsDao.loadAllInGroups(list,group, new CallBack() {

				public void callback(Records r) {
					targetList.add(list.concat("-").concat(r.getValue()));
				}
			});
		} catch (SQLException ex) {
			throw new OrlandException("failed when in db operations");
		}

	}

	public void loadNewGroupIntoList(final String list, String group) throws OrlandException {
		try {
			if(group==null||group.trim().length()==0)throw new OrlandException("group should not be blank");
			String _group = "'"+group.trim()+"'";
			recordsDao.loadAllInGroups(list,_group, new CallBack() {
				public void callback(Records r) {
					targetList.add(list.concat("-").concat(r.getValue()));
				}
			});
		} catch (SQLException ex) {
			throw new OrlandException("failed when in db operations");
		}
	}

	public void removeGroup(String group) throws OrlandException {
		//TODO do not support runtime remove from in group
//		throw new UnsupportedOperationException("Not supported yet.");
	}

	private String addSingleQuotationMarks(String s) {
		StringBuilder sb = new StringBuilder();
		for (String _s : s.split(",")) {
			sb.append("'");
			sb.append(_s);
			sb.append("'");
			sb.append(",");
		}
		String result = sb.toString().trim();
		if (result.length() > 0) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
}
