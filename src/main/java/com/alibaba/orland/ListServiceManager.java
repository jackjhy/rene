/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

import com.alibaba.orland.dao.DeletedRecordsDAO;
import com.alibaba.orland.dao.DeletedRecordsDAOImpl;
import com.alibaba.orland.dao.ListsDAO;
import com.alibaba.orland.dao.ListsDAOImpl;
import com.alibaba.orland.dao.RecordsDAO;
import com.alibaba.orland.dao.RecordsDAOImpl;
import com.alibaba.orland.dao.ibatis.SqlMapClientManager;
import com.alibaba.orland.dataobject.Lists;
import com.ibatis.sqlmap.client.SqlMapClient;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tiger
 */
public class ListServiceManager {

	private SqlMapClientManager smcm;

	public ListServiceManager() {
	}

	public void init(String deiver, String url, String user, String password) {
		smcm = new SqlMapClientManager(deiver, url, user, password);
	}

	public ListService getBaseListService() {
		SqlMapClient client = smcm.getSqlMapClient();
		ListsDAO ld = new ListsDAOImpl(client);
		RecordsDAO rd = new RecordsDAOImpl(client);
		DeletedRecordsDAO dd = new DeletedRecordsDAOImpl(client);
		MatchService ms = new MatchServiceImplOnMysql(ld, rd, dd);
		return new ListServiceImpl(ld, rd, dd, ms);
	}

	public ListService getBloomFilterListService(int number) {
		SqlMapClient client = smcm.getSqlMapClient();
		ListsDAO ld = new ListsDAOImpl(client);
		RecordsDAO rd = new RecordsDAOImpl(client);
		DeletedRecordsDAO dd = new DeletedRecordsDAOImpl(client);
		MatchService ms = new MatchServiceImplOnBloomFilter(0.01, number, ld, rd, dd);
		ListService service = new ListServiceImpl(ld, rd, dd, ms);
		List<Lists> lists;
		try {
			lists = ld.getAll();
			for (Lists l : lists) {
				ms.loadNewList(l.getId());
			}
		} catch (SQLException ex) {
			Logger.getLogger(ListServiceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (OrlandException ex) {
			Logger.getLogger(ListServiceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return service;
	}
}
