package com.alibaba.orland.dao;

import com.alibaba.orland.CallBack;
import com.alibaba.orland.dataobject.Lists;
import com.alibaba.orland.dataobject.Records;
import com.ibatis.sqlmap.client.SqlMapClient;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsDAOImpl implements RecordsDAO {

	private SqlMapClient sqlMapClient;

	public RecordsDAOImpl(SqlMapClient sqlMapClient) {
		super();
		this.sqlMapClient = sqlMapClient;
	}

	public void insert(Records record) throws SQLException {
		sqlMapClient.insert("records.insert", record);
	}

	public void insertSelective(Records record) throws SQLException {
		sqlMapClient.insert("records.insertSelective", record);
	}

	public void copyRecordsGroup(String oldGroup, String newGroup, String newlist) throws SQLException {
		HashMap<String, String> m = new HashMap<String, String>();
		m.put("newGroup", newGroup);
		m.put("oldGroup", oldGroup);
		m.put("list", newlist);
		sqlMapClient.insert("records.copyRecordGroup", m);
	}

	public void deleteRecordsGroup(String group) throws SQLException {
		sqlMapClient.delete("records.deleteGroupByGroupId", group);
	}

	public List<Records> queryRecordsWithValue(String record) throws SQLException {
		return sqlMapClient.queryForList("records.queryRecordsWithValue", record);
	}

	public void deleteRecords(Integer id) throws SQLException {
		sqlMapClient.delete("records.deleteRecords", id);
	}

	public List<Records> queryByKeyAndInGroup(String v, String groups) throws SQLException {
		HashMap<String, String> m = new HashMap<String, String>();
		m.put("value", v);
		m.put("groups", groups);
		return sqlMapClient.queryForList("records.queryByKeyAndInGroup", m);
	}

	public void loadAllInGroups(String list, String groups, CallBack callback) throws SQLException {
		int limitSize = 1000;
		int beginIndex = 0;
		while (true) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("list", list);
			m.put("groups", groups);
			m.put("limitSize", limitSize);
			m.put("beginIndex", beginIndex);
			List<Records> _l = sqlMapClient.queryForList("records.batchQueryInGroup", m);
			if (_l == null || _l.isEmpty()) {
				break;
			}
			for (Records _r : _l) {
				callback.callback(_r);
			}
			beginIndex = _l.get(_l.size() - 1).getId() + 1;
		}
	}

	public List<Records> loadPageInGroups(Lists list, Integer lastMaxId, Integer pageCap) throws SQLException {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("list", list.getId());
		m.put("groups", addSingleQuotationMarks(list.getContent()));
		m.put("limitSize", pageCap);
		m.put("beginIndex", lastMaxId + 1);
		return sqlMapClient.queryForList("records.batchQueryInGroup", m);
	}

	public Integer getCapWithList(Lists list) throws SQLException {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("list", list.getId());
		m.put("groups", addSingleQuotationMarks(list.getContent()));
		return (Integer) (sqlMapClient.queryForObject("records.countList", m));
	}

	public Integer getCapWithListAndGroup(String list, String group) throws SQLException {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("list", list);
		m.put("group", group);
		return (Integer) (sqlMapClient.queryForObject("records.countGroupInList", m));
	}

	private String addSingleQuotationMarks(String s) {
		StringBuilder sb = new StringBuilder();
		for (String _s : s.split(",")) {
			sb.append("'");
			sb.append(_s);
			sb.append("'");
			sb.append(",");
		}
		String result = sb.toString().trim();
		if (result.length() > 0) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

}