package com.alibaba.orland.dao;

import com.alibaba.orland.CallBack;
import com.alibaba.orland.dataobject.Lists;
import com.alibaba.orland.dataobject.Records;
import java.sql.SQLException;
import java.util.List;

public interface RecordsDAO {

	void insert(Records record) throws SQLException;

	void insertSelective(Records record) throws SQLException;

	void copyRecordsGroup(String oldGroup, String newGroup, String newlist) throws SQLException;

	void deleteRecordsGroup(String group) throws SQLException;

	List<Records> queryRecordsWithValue(String record) throws SQLException;

	void deleteRecords(Integer id) throws SQLException;

	List<Records> queryByKeyAndInGroup(String v, String groups) throws SQLException;

	void loadAllInGroups(String list, String groups, CallBack callback) throws SQLException;

	public List<Records> loadPageInGroups(Lists list, Integer lastMaxId, Integer pageCap) throws SQLException;

	public Integer getCapWithList(Lists list) throws SQLException;

	public Integer getCapWithListAndGroup(String list, String group) throws SQLException;
}