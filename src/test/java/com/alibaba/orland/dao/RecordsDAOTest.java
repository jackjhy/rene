/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland.dao;

import com.alibaba.orland.CallBack;
import com.alibaba.orland.dataobject.Lists;
import com.alibaba.orland.dataobject.Records;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author tiger
 */
public class RecordsDAOTest {

	RecordsDAO dao = null;
	ListsDAO ldao = null;

	@BeforeClass
	public void setUp() {
		SqlMapClient client = SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/alibaba/orland/ibatis/sqlmap.xml")));
		dao = new RecordsDAOImpl(client);
		ldao = new ListsDAOImpl(client);
	}

	@Test
	public void aTest() {
		System.out.println("Test");
	}

//	@Test
	public void insertTest() throws SQLException {
		dao.insert(new Records(10, "129.168.1.1", "ip", "123_122343323", new Date(), "123"));
		assert dao.queryRecordsWithValue("129.168.1.1").size() == 1;
		assert dao.queryRecordsWithValue("129.168.1.1").get(0).getType().equalsIgnoreCase("ip");
		dao.deleteRecords(10);
	}

//	@Test
	public void copyGroupTest() throws SQLException {
		dao.insert(new Records(10, "129.168.1.1", "ip", "123_122343323", new Date(), "123"));
		dao.copyRecordsGroup("123_122343323", "123_122343324", "newlist");
		assert dao.queryRecordsWithValue("129.168.1.1").size() == 2;
		dao.deleteRecordsGroup("123_122343323");
		dao.deleteRecordsGroup("123_122343324");
	}

//	@Test
	public void queryByKeyAndInGroupTest() throws SQLException {
		dao.insert(new Records(10, "129.168.1.1", "ip", "123_122343323", new Date(), "123"));
		assert dao.queryByKeyAndInGroup("129.168.1.1", "'123_122343323','123_22333'").size() == 1;
		dao.deleteRecords(10);
	}

//	@Test
	public void deleteRecordsGroupTest() throws SQLException {
		dao.insert(new Records(10, "129.168.1.1", "ip", "123_122343323", new Date(), "123"));
		dao.copyRecordsGroup("123_122343323", "123_122343324", "newlist");
		dao.deleteRecordsGroup("123_122343324");
		assert dao.queryRecordsWithValue("129.168.1.1").size() == 1;
		dao.deleteRecords(10);
	}

//	@Test
	public void deleteRecordsTest() throws SQLException {
		dao.insert(new Records(10, "129.168.1.1", "ip", "123_122343323", new Date(), "123"));
		dao.deleteRecords(10);
		List<Records> l = dao.queryRecordsWithValue("129.168.1.1");
		assert l == null || l.isEmpty();
	}

	@Test
	public void loadAllInGroupsTest() throws SQLException{
		dao.insertSelective(new Records(null, "sss", "", "g1", null, null));
		dao.insertSelective(new Records(null, "aaa", "", "g2", null, null));
		final AtomicInteger i = new AtomicInteger(0);
		dao.loadAllInGroups("test1", "'g1','g2'", new CallBack() {
			public void callback(Records r) {
				i.addAndGet(1);
			}
		});
		assert i.get()==2;
		dao.deleteRecordsGroup("g1");
		dao.deleteRecordsGroup("g2");
	}

	@AfterClass
	public void cleanUp() {
		// code that will be invoked after this test ends
	}
}
