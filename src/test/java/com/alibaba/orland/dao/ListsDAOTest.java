/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland.dao;

import com.alibaba.orland.dataobject.Lists;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Properties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author tiger
 */
public class ListsDAOTest {

	ListsDAO dao = null;

	@BeforeClass
	public void setUp() {
		Properties p = new Properties();
		p.put("driver", "com.mysql.jdbc.Driver");
		p.put("url", "jdbc:mysql://127.0.0.1:3306/orland");
		p.put("username", "root");
		p.put("password", "Fh280112");
		SqlMapClient client = SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/alibaba/orland/ibatis/sqlmap.xml")),p);
		dao = new ListsDAOImpl(client);
	}

	@Test
	public void aTest() {
		System.out.println("Test");
	}

	@Test
	public void insertTest() throws SQLException {
		dao.insert(new Lists("flistt", "ip", "123_1232,123_1223", 0, "", "", "", "test lists"));
		assert dao.queryListsById("flistt").getCategory().equals("ip");
		dao.deleteLists("flistt");
	}

//	@Test
	public void updateListsTest() throws SQLException {
		dao.insert(new Lists("flistt", "ip", "123_1232,123_1223", 0, "", "", "", "test lists"));
		dao.updateLists(new Lists("flistt", null, null, 3, "", "", "", null));
		assert dao.queryListsById("flistt").getAction() == 3;
	}

//	@Test
	public void deleteListsTest() throws SQLException {
		dao.insert(new Lists("flistt", "ip", "123_1232,123_1223", 0, "", "", "", "test lists"));
		dao.deleteLists("flistt");
		assert dao.queryListsById("flistt") == null;
	}

//	@Test
	public void getAllTest() throws SQLException {
		int i = (dao.getAll() == null || dao.getAll().isEmpty()) ? 0 : dao.getAll().size();
		dao.insert(new Lists("flistt", "ip", "123_1232,123_1223", 0, "", "", "", "test lists"));
		assert dao.getAll().size() == i + 1;
		dao.deleteLists("flistt");
	}

//	@Test
	public void getAllWithLikeGroupTest() throws SQLException{
		dao.insert(new Lists("flistt", "ip", "123_1232,123_1223,123_1332", 0,"", "", "", "test lists"));
		dao.insert(new Lists("flist1", "ip", "123_1223", 0, "", "", "", "test lists"));
		assert dao.getAllWithLikeGroup("123_1223").size()==2;
		assert dao.getAllWithLikeGroup("123_1232").size()==1;
		assert dao.getAllWithLikeGroup("123_1332").size()==1;
		dao.deleteLists("flistt");
		dao.deleteLists("flist1");
	}

	@AfterClass
	public void cleanUp() {
		// code that will be invoked after this test ends
	}
}
