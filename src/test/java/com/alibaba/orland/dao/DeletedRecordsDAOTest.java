/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland.dao;

import com.alibaba.orland.dataobject.DeletedRecords;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author tiger
 */
public class DeletedRecordsDAOTest {

	DeletedRecordsDAO dao;

	@BeforeClass
	public void setUp() {
		SqlMapClient client = SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/alibaba/orland/ibatis/sqlmap.xml")));
		dao = new DeletedRecordsDAOImpl(client);
	}

	@Test
	public void aTest() {
		System.out.println("Test");
	}

//	@Test
	public void insertTest() throws SQLException {
		dao.insert(new DeletedRecords(3, "flist", new Date()));
		assert dao.queryByIdAndListsId(3, "flist").size() == 1;
		dao.deleteByIdAndListid(3, "flist");
	}

	@AfterClass
	public void cleanUp() {
		// code that will be invoked after this test ends
	}
}
