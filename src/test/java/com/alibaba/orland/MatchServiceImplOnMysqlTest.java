/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

import com.alibaba.orland.dao.DeletedRecordsDAOImpl;
import com.alibaba.orland.dao.ListsDAOImpl;
import com.alibaba.orland.dao.RecordsDAOImpl;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.InputStreamReader;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author tiger
 */
public class MatchServiceImplOnMysqlTest {

	MatchServiceImplOnMysql ms = null;

	@BeforeClass
	public void setUp() {
		SqlMapClient client = SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/alibaba/orland/ibatis/sqlmap.xml")));
		ms = new MatchServiceImplOnMysql(new ListsDAOImpl(client), new RecordsDAOImpl(client), new DeletedRecordsDAOImpl(client));
	}

	@Test
	public void aTest() {
		System.out.println("Test");
	}

//	@Test
	public void matchRecordInListTest() throws OrlandException {
		assert ms.matchRecordInList("flist", "192.168");
		assert !ms.matchRecordInList("flist1", "192.168.*");
		assert ms.matchRecordInList("flist", "192.168.*");
	}

	@AfterClass
	public void cleanUp() {
		// code that will be invoked after this test ends
	}
}
