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
import com.alibaba.orland.dataobject.DeletedRecords;
import com.alibaba.orland.dataobject.Lists;
import com.alibaba.orland.dataobject.Records;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author tiger
 */
public class MatchServiceImplOnMysql implements MatchService {

	static Map<String, String> listGroupMap = new ConcurrentHashMap<String, String>();
	private ListsDAO listsDao;
	private RecordsDAO recordsDao;
	private DeletedRecordsDAO drsDao;

	public MatchServiceImplOnMysql(ListsDAO listsDao, RecordsDAO recordsDao, DeletedRecordsDAO drsDao) {
		this.listsDao = listsDao;
		this.recordsDao = recordsDao;
		this.drsDao = drsDao;
	}

	public MatchServiceImplOnMysql() {
		init();
	}

	public boolean matchRecordInList(String list, String record) throws OrlandException {
		try {
			String _gs = listGroupMap.get(list);
			if (_gs == null || _gs.trim().length() == 0) {
				Lists _l = listsDao.queryListsById(list);
				if(_l==null) {
					//TODO
					new OrlandException("lists is not existed,or had been deleted");
					return false;
				}
				_gs = addSingleQuotationMarks(_l.getContent());
				if (_gs == null || _gs.trim().length() == 0) {
					//TODO
					new OrlandException("lists' group should not be null or blank");
					return false;
				}
			}
			List<Records> _rl = recordsDao.queryByKeyAndInGroup(record, _gs);
			if (_rl == null || _rl.isEmpty()) {
				return false;
			}
			boolean f = false;
			for (Records _r : _rl) {
				if (_r.getCreatedList() == null || _r.getCreatedList().equals(list)) {
					return true;
				} else {
					f = true;
					List<DeletedRecords> _drl = drsDao.queryByIdAndListsId(_r.getId(), list);
					if (_drl != null && !_drl.isEmpty()) {
						f = false;
					} else {
						return true;
					}
				}
			}
			return f;
		} catch (SQLException ex) {
			//TODO deal exception
			throw new OrlandException("", ex);
		}
	}

	public void addNewRecordIntoList(String record, String list) {
	}

	public void removeRecordFromLlist(String record, String list) {
	}

	public void loadNewList(String list) throws OrlandException{
		try {
			Lists _l = listsDao.queryListsById(list);
			if(_l!=null&&_l.getContent().trim().length()>0)listGroupMap.put(list, addSingleQuotationMarks(_l.getContent().trim()));
		} catch (SQLException ex) {
			throw new OrlandException("error when load new lists",ex);
		}
	}

	private void init() {
		try {
			List<Lists> allLists = listsDao.getAll();
			for (Lists _l : allLists) {
				listGroupMap.put(_l.getId(), addSingleQuotationMarks(_l.getContent()));
			}
		} catch (SQLException ex) {
			//TODO add loger fatal
		}
		//TODO add logger for init finished
		new Thread(){
			@Override
			@SuppressWarnings("SleepWhileInLoop")
			public void run() {
				while (true) {					
					try {
						Thread.sleep(1000*60*60);
						listGroupMap = groupIdCache();
					} catch (InterruptedException ex) {
						//TODO
					}
				}
			}
		}.start();
	}

	public void loadNewGroupIntoList(String list, String group) {
		String _gs = listGroupMap.get(list);
		if(_gs==null) _gs = addSingleQuotationMarks(group);
		else _gs = _gs+","+ addSingleQuotationMarks(group);
		listGroupMap.put(list, _gs);
	}

	public void removeLists(String list) {
		listGroupMap.remove(list);
	}

	public void removeGroup(String group) {
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

	private synchronized void flushCache() throws OrlandException {
		listGroupMap = groupIdCache();
	}

	private Map<String, String> groupIdCache() {
		Map<String, String> _m = new HashMap<String, String>();
		try {
			List<Lists> allLists = listsDao.getAll();
			for (Lists _l : allLists) {
				_m.put(_l.getId(), addSingleQuotationMarks(_l.getContent()));
			}
			return _m;
		} catch (SQLException ex) {
			//TODO
			return null;
		}

	}

	public void addNewRecordIntoGroup(String record, String group) throws OrlandException {
	}
}
