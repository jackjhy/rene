package com.alibaba.orland.dao;

import com.alibaba.orland.dataobject.Lists;
import com.ibatis.sqlmap.client.SqlMapClient;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListsDAOImpl implements ListsDAO {

	private SqlMapClient sqlMapClient;

	public ListsDAOImpl(SqlMapClient sqlMapClient) {
		super();
		this.sqlMapClient = sqlMapClient;
	}

	public void insert(Lists record) throws SQLException {
		sqlMapClient.insert("lists.insert", record);
	}

	public void insertSelective(Lists record) throws SQLException {
		sqlMapClient.insert("lists.insertSelective", record);
	}

	public Lists queryListsById(String id) throws SQLException {
		return (Lists)sqlMapClient.queryForObject("lists.queryById",id);
	}

	public void updateLists(Lists l) throws SQLException{
		sqlMapClient.update("lists.updateById",l);
	}

	public void deleteLists(String id) throws SQLException {
		sqlMapClient.delete("lists.deleteById",id);
	}

	public List<Lists> getAll() throws SQLException {
		return sqlMapClient.queryForList("lists.getAll");
	}

	public List<Lists> getAllWithLikeGroup(String group) throws SQLException {
		List<Lists> result = new ArrayList<Lists>();
		result.addAll(sqlMapClient.queryForList("lists.getAllWithLikeGroup",group));
		result.addAll(sqlMapClient.queryForList("lists.getAllWithLikeGroup",group.concat(",%")));
		result.addAll(sqlMapClient.queryForList("lists.getAllWithLikeGroup","%,".concat(group)));
		result.addAll(sqlMapClient.queryForList("lists.getAllWithLikeGroup","%,".concat(group).concat(",%")));
		return result;
	}
}