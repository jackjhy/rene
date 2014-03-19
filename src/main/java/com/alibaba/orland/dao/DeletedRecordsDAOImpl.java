package com.alibaba.orland.dao;

import com.alibaba.orland.dataobject.DeletedRecords;
import com.ibatis.sqlmap.client.SqlMapClient;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DeletedRecordsDAOImpl implements DeletedRecordsDAO {

	private SqlMapClient sqlMapClient;

	public DeletedRecordsDAOImpl(SqlMapClient sqlMapClient) {
		super();
		this.sqlMapClient = sqlMapClient;
	}

	public void insert(DeletedRecords record) throws SQLException {
		sqlMapClient.insert("records_deleted.insert", record);
	}

	public void insertSelective(DeletedRecords record) throws SQLException {
		sqlMapClient.insert("records_deleted.insertSelective", record);
	}

	public List<DeletedRecords> queryByIdAndListsId(Integer id, String listsid) throws SQLException {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("listsid", listsid);
		return sqlMapClient.queryForList("records_deleted.queryByIdAndListsId", m);
	}

	public void deleteByIdAndListid(int id, String listid) throws SQLException {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("id", id);
		m.put("listsid", listid);
		sqlMapClient.delete("records_deleted.deleteByIdAndListid", m);
	}
}