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
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author tiger
 */
public class ListServiceTest {

	ListService service;

	@BeforeClass
	public void setUp() {
		SqlMapClient client = new SqlMapClientManager(null, "jdbc:mysql://localhost:3306/orland", "root", "Fh280112").getSqlMapClient();
		ListsDAO ld = new ListsDAOImpl(client);
		RecordsDAO rd = new RecordsDAOImpl(client);
		DeletedRecordsDAO dd = new DeletedRecordsDAOImpl(client);
		MatchService ms = new MatchServiceImplOnBloomFilter(0.01, 100000, ld, rd, dd);
//		MatchService ms = new MatchServiceImplOnMysql(ld, rd, dd);
		service = new ListServiceImpl(ld, rd, dd, ms);
	}

	@Test
	public void aTest() {
		System.out.println("Test");
	}

	@Test
	public void createNewListTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoList("test1", "t1", null, null);
		service.addRecordIntoGroup("tg_1", "tt", null, null, null);
		assert service.matchRecordInList("test1", "t1");
		assert service.matchRecordInList("test1", "tt");
		service.createNewList("test2", "name", l, null, null);
		assert service.matchRecordInList("test2", "tt");
		l = new ArrayList<String>();
		l.add("test1");
		service.createNewList("test3", "name", null, l, null);
		assert service.matchRecordInList("test3", "t1");
		service.createNewList("test4", "name", null, null, l);
		assert service.matchRecordInList("test4", "t1");
		service.deleteList("test1");
		service.deleteList("test4");
		service.deleteList("test2");
		service.deleteList("test3");
		service.deleteRecordsGroup("tg_1");
		service.deleteRecordsGroup("test1_default");
		service.deleteRecordsGroup("test2_default");
		service.deleteRecordsGroup("test3_default");
		service.deleteRecordsGroup("test4_default");
	}

//	@Test
	public void importExistedListIntoTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoGroup("tg_1", "t1", null, null, null);
		l = new ArrayList<String>();
		l.add("tg_2");
		service.createNewList("test5", "name", l, null, null);
		service.importExistedListInto("test5", "test1");
		assert service.matchRecordInList("test5", "t1");
		service.deleteList("test1");
		service.deleteList("test5");
		service.deleteRecordsGroup("tg_1");
		service.deleteRecordsGroup("tg_2");
		service.deleteRecordsGroup("test1_default");
		service.deleteRecordsGroup("test5_default");
	}

//	@Test
	public void importExistedGroupIntoTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoGroup("tg_1", "t1", null, null, null);
		l = new ArrayList<String>();
		l.add("tg_2");
		service.createNewList("test5", "name", l, null, null);
		service.importExistedGroupInto("test5", "tg_1");
		assert service.matchRecordInList("test5", "t1");
		service.deleteList("test1");
		service.deleteList("test5");
		service.deleteRecordsGroup("tg_1");
		service.deleteRecordsGroup("tg_2");
		service.deleteRecordsGroup("test1_default");
		service.deleteRecordsGroup("test5_default");
	}

//	@Test
	public void reExtendedFromExistedListTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoGroup("tg_1", "t1", null, null, null);
		l = new ArrayList<String>();
		l.add("tg_2");
		service.createNewList("test5", "name", l, null, null);
		service.reExtendedFromExistedList("test5", "test1");
		assert service.matchRecordInList("test5", "t1");
		service.deleteList("test1");
		service.deleteList("test5");
		service.deleteRecordsGroup("tg_1");
		service.deleteRecordsGroup("tg_2");
		service.deleteRecordsGroup("test1_default");
		service.deleteRecordsGroup("test5_default");
	}

//	@Test
	public void reExtendedFromExistedGroupTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoGroup("tg_1", "t1", null, null, null);
		l = new ArrayList<String>();
		l.add("tg_2");
		service.createNewList("test5", "name", l, null, null);
		service.reExtendedFromExistedGroup("test5", "tg_1");
		assert service.matchRecordInList("test5", "t1");
		service.deleteList("test1");
		service.deleteList("test5");
		service.deleteRecordsGroup("tg_1");
		service.deleteRecordsGroup("tg_2");
		service.deleteRecordsGroup("test1_default");
		service.deleteRecordsGroup("test5_default");
	}

//	@Test
	public void deleteListTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoGroup("tg_1", "t1", null, null, null);
		service.deleteList("test1");
		assert !service.matchRecordInList("test1", "t1");
		service.deleteRecordsGroup("tg_1");
		service.deleteRecordsGroup("test1_default");
	}

//	@Test
	public void deleteRecordsGroupTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoGroup("tg_1", "t1", null, null, null);
		service.deleteRecordsGroup("tg_1");
		assert !service.matchRecordInList("test1", "t1");
		service.deleteList("test1");
		service.deleteRecordsGroup("test1_default");
	}

//	@Test
	public void addRecordIntoListTest() throws OrlandException {
		service.createNewList("test1", "name", null, null, null);
		service.addRecordIntoList("test1", "t1", null, null);
		assert service.matchRecordInList("test1", "t1");
		service.addRecordIntoList("test1", "t2", null, null);
		assert service.matchRecordInList("test1", "t2");
		service.deleteList("test1");
		service.deleteRecordsGroup("test1_default");
	}

//	@Test
	public void deleteRecordFromListTest() throws OrlandException {
		List<String> l = new ArrayList<String>();
		l.add("tg_1");
		service.createNewList("test1", "name", l, null, null);
		service.addRecordIntoList("test1", "t1", null, null);
		service.addRecordIntoGroup("tg_1", "tt", null, null, null);
		service.addRecordIntoGroup("tg_1", "ttt", "test1", null, null);
		service.deleteRecordFromList("test1", "t1");
		assert !service.matchRecordInList("test1", "t1");
		service.createNewList("test2", "name", l, null, null);
		service.deleteRecordFromList("test2", "tt");
		assert service.matchRecordInList("test1", "ttt");
		assert !service.matchRecordInList("test1", "tt");
		assert !service.matchRecordInList("test2", "tt");
		service.deleteRecordFromList("test1", "tt");
		assert !service.matchRecordInList("test1", "tt");
		assert !service.matchRecordInList("test2", "tt");
		service.deleteList("test1");
		service.deleteList("test2");
		service.deleteRecordsGroup("test1_default");
		service.deleteRecordsGroup("test2_default");
		service.deleteRecordsGroup("tg_1");
	}

	@AfterClass
	public void cleanUp() {
		// code that will be invoked after this test ends
	}

	public static void main(String[] args) {
		System.out.println("_test".indexOf("_"));
	}
}
