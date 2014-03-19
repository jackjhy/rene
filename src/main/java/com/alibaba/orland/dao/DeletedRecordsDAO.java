package com.alibaba.orland.dao;

import com.alibaba.orland.dataobject.DeletedRecords;
import java.sql.SQLException;
import java.util.List;

public interface DeletedRecordsDAO {

	void insert(DeletedRecords record) throws SQLException;

	void insertSelective(DeletedRecords record) throws SQLException;

	List<DeletedRecords> queryByIdAndListsId(Integer id, String listsid) throws SQLException;

	void deleteByIdAndListid(int id,String listid) throws SQLException;
}