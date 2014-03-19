/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland.dao.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 *
 * @author tiger
 */
public class SqlMapClientManager {

	private Properties p = new Properties();

	public SqlMapClientManager(String driver, String url, String user, String password) {
		p.put("driver", "com.mysql.jdbc.Driver");
		if (driver != null && driver.length() != 0) {
			p.put("driver", driver);
		}
		if (url != null && url.length() != 0) {
			p.put("url", url);
		}
		if (user != null && user.length() != 0) {
			p.put("username", user);
		}
		if (password != null && password.length() != 0) {
			p.put("password", password);
		}
	}

	public SqlMapClient getSqlMapClient() {
		return SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("com/alibaba/orland/ibatis/sqlmap.xml")), p);
	}
}
