package com.alibaba.orland.dao;

import com.alibaba.orland.dataobject.Lists;
import java.sql.SQLException;
import java.util.List;

public interface ListsDAO {

	void insert(Lists record) throws SQLException;

	void insertSelective(Lists record) throws SQLException;

	Lists queryListsById(String id) throws SQLException;

	void updateLists(Lists record) throws SQLException;

	void deleteLists(String id) throws SQLException;

	List<Lists> getAll() throws SQLException;

	List<Lists> getAllWithLikeGroup(String group) throws SQLException;
}