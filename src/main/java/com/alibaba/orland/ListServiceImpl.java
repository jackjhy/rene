/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

import com.alibaba.orland.dao.DeletedRecordsDAO;
import com.alibaba.orland.dao.ListsDAO;
import com.alibaba.orland.dao.RecordsDAO;
import com.alibaba.orland.dataobject.DeletedRecords;
import com.alibaba.orland.dataobject.Lists;
import com.alibaba.orland.dataobject.Records;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tiger
 */
public class ListServiceImpl implements ListService {

	private ListsDAO listsDao;
	private RecordsDAO recordsDao;
	private DeletedRecordsDAO drsDao;
	private MatchService matchService;

	public ListServiceImpl(ListsDAO listsDao, RecordsDAO recordsDao, DeletedRecordsDAO drsDao, MatchService matchService) {
		this.listsDao = listsDao;
		this.recordsDao = recordsDao;
		this.drsDao = drsDao;
		this.matchService = matchService;
	}

	public ListServiceImpl() {
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void createNewList(String listName, String catagory, List<String> combinatedGroup, List<String> importedList, List<String> extendsList) throws OrlandException {
		try {
			//check if this listname had been used
			Lists lls = listsDao.queryListsById(listName);
			if (lls != null) {
				throw new OrlandException("this list name had been used");
			}
			StringBuilder groups = new StringBuilder();
			groups.append(listName).append("_default");
			groups.append(",");
			if (combinatedGroup != null && combinatedGroup.size() > 0) {
				for (String _group : combinatedGroup) {
					if (_group != null && _group.trim().length() > 0) {
						groups.append(_group);
						groups.append(",");
					}
				}
			}
			//if importedList is not null, do importing first
			//a group id is composed with group name and created timestamp
			if (importedList != null && importedList.size() > 0) {
				long index = System.currentTimeMillis();
				for (String _id : importedList) {
					Lists _ls = listsDao.queryListsById(_id);
					if (_ls == null) {
						continue;
					}
					String _groups = _ls.getContent();
					String[] __groups = _groups.split(",");
					for (String _group : __groups) {
						if(_group.indexOf("_")<0)_group= _group+"_"+index;
						String __group = _group.substring(0, _group.lastIndexOf("_") + 1) + index;
						copyExistedGroupInto(_group, __group, listName);
						groups.append(__group);
						groups.append(",");
					}
				}
			}
			//prepared extends list
			if (extendsList != null && extendsList.size() > 0) {
				for (String _id : extendsList) {
					Lists _ls = listsDao.queryListsById(_id);
					if (_ls == null) {
						continue;
					}
					String _groups = _ls.getContent();
					groups.append(_groups);
					groups.append(",");
				}
			}

			String groupsString = groups.toString().trim();
			//insert new list into table lists
			lls = new Lists(listName, catagory, groupsString, null, null, null, null, null);
			listsDao.insertSelective(lls);
			//match implementation load this new list now
			matchService.loadNewList(listName);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void importExistedListInto(String listName, String existedList) throws OrlandException {
		try {
			//check if this listName is existed
			Lists lls = listsDao.queryListsById(listName);
			if (lls == null) {
				throw new OrlandException("list with this name can not been found in system");
			}
			StringBuilder groups = new StringBuilder();
			if (lls.getContent() != null && lls.getContent().trim().length() > 0) {
				groups.append(lls.getContent().trim());
				groups.append(",");
			}
			//check if this existedList had existed
			if (existedList != null && existedList.trim().length() > 0) {
				long index = System.currentTimeMillis();
				Lists _ls = listsDao.queryListsById(existedList);
				if (_ls != null) {
					String _groups = _ls.getContent();
					String[] __groups = _groups.split(",");
					for (String _group : __groups) {
						String __group = _group.substring(0, _group.lastIndexOf("_") + 1) + index;
						copyExistedGroupInto(_group, __group, listName);
						//load list records into matcher implementation
						matchService.loadNewGroupIntoList(listName, __group);
						groups.append(__group);
						groups.append(",");
					}
					//update lists
					String groupsString = groups.toString().trim();
					lls.setContent(groupsString);
					listsDao.updateLists(lls);
				}
			}

		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void reExtendedFromExistedList(String listName, String existedList) throws OrlandException {
		try {
			//check if this listName is existed
			Lists lls = listsDao.queryListsById(listName);
			if (lls == null) {
				throw new OrlandException("list with this name can not been found in system");
			}
			StringBuilder groups = new StringBuilder();
			if (lls.getContent() != null && lls.getContent().trim().length() > 0) {
				groups.append(lls.getContent().trim());
				groups.append(",");
			}
			//check if this existedList had existed
			if (existedList != null && existedList.trim().length() > 0) {
				Lists _ls = listsDao.queryListsById(existedList);
				if (_ls != null) {
					String _groups = _ls.getContent();
					groups.append(_groups);
					groups.append(",");
					//update lists
					String groupsString = groups.toString().trim();
					lls.setContent(groupsString);
					listsDao.updateLists(lls);
					//load list records into matcher implementation
					for (String __g : _groups.split(",")) {
						matchService.loadNewGroupIntoList(listName, __g);
					}
				}
			}
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void deleteList(String listName) throws OrlandException {
		try {
			listsDao.deleteLists(listName);
			matchService.removeLists(listName);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public String createRecordsGroup() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void deleteRecordsGroup(String group) throws OrlandException {
		try {
			recordsDao.deleteRecordsGroup(group);
			matchService.removeGroup(group);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void deleteRecordFromList(String list, String record) throws OrlandException {
		//get records first, check does it belong to this list
		//if yes, directly removed
		//else add a new deleted records into deleted records table
		try {
			List<Records> _lrs = recordsDao.queryRecordsWithValue(record);
			for (Records _r : _lrs) {
				if (_r.getCreatedList() == null || _r.getCreatedList().trim().length() == 0 || list.equals(_r.getCreatedList())) {
					recordsDao.deleteRecords(_r.getId());
				} else {
					DeletedRecords _dr = new DeletedRecords(_r.getId(), list, new Date());
					drsDao.insert(_dr);
				}
			}
			matchService.removeRecordFromLlist(record, list);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public boolean matchRecordInList(String list, String record) throws OrlandException {
		return matchService.matchRecordInList(list, record);
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void addRecordIntoGroup(String group, String record, String createdList, String category, Date expired) throws OrlandException {
		try {
			Records r = new Records();
			r.setGroupId(group);
			r.setValue(record);
			r.setCreatedList(createdList);
			r.setType(category);
			r.setExpired(expired);
			recordsDao.insertSelective(r);
			matchService.addNewRecordIntoGroup(record, group);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void importExistedGroupInto(String listName, String existedGroup) throws OrlandException {
		try {
			//check if this listName is existed
			Lists lls = listsDao.queryListsById(listName);
			if (lls == null) {
				throw new OrlandException("list with this name can not been found in system");
			}
			StringBuilder groups = new StringBuilder();
			if (lls.getContent() != null && lls.getContent().trim().length() > 0) {
				groups.append(lls.getContent().trim());
				groups.append(",");
			}
			//check if this existedList had existed
			long index = System.currentTimeMillis();
			String __group = existedGroup.substring(0, existedGroup.lastIndexOf("_") + 1) + index;
			copyExistedGroupInto(existedGroup, __group, listName);
			groups.append(__group);
			groups.append(",");
			//update lists
			String groupsString = groups.toString().trim();
			lls.setContent(groupsString);
			listsDao.updateLists(lls);
			matchService.loadNewGroupIntoList(listName, __group);
		} catch (SQLException ex) {
			Logger.getLogger(ListServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void reExtendedFromExistedGroup(String listName, String existedGroup) throws OrlandException {
		try {
			//check if this listName is existed
			Lists lls = listsDao.queryListsById(listName);
			if (lls == null) {
				throw new OrlandException("list with this name can not been found in system");
			}
			StringBuilder groups = new StringBuilder();
			if (lls.getContent() != null && lls.getContent().trim().length() > 0) {
				groups.append(lls.getContent().trim());
				groups.append(",");
			}
			groups.append(existedGroup);
			groups.append(",");
			//update lists
			String groupsString = groups.toString().trim();
			lls.setContent(groupsString);
			listsDao.updateLists(lls);
			matchService.loadNewGroupIntoList(listName, existedGroup);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when do db operation", ex);
		}
	}

	private void copyExistedGroupInto(String oldGroup, String newGroup, String listId) throws OrlandException {
		try {
			recordsDao.copyRecordsGroup(oldGroup, newGroup, listId);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when copy records group", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void addRecordIntoList(String list, String record, String category, Date expired) throws OrlandException {
		try {
			Lists _l = listsDao.queryListsById(list);
			if (_l == null) {
				throw new OrlandException("lists does not existed");
			}
			Records r = new Records();
			r.setGroupId(list + "_default");
			r.setValue(record);
			r.setCreatedList(list);
			r.setExpired(expired);
			if (category != null) {
				r.setType(category);
			} else {
				r.setType(_l.getCategory());
			}
			recordsDao.insertSelective(r);
			matchService.addNewRecordIntoList(record, list);
		} catch (SQLException ex) {
			throw new OrlandException("occur error when copy records group", ex);
		}
	}

	/**
	 * @return {@inheritDoc}
	 */
	public void addRecordIntoList(String list, List<String> record, String category, Date expired) throws OrlandException {
		try {
			//TODO 待优化
			Lists _l = listsDao.queryListsById(list);
			if (_l == null) {
				throw new OrlandException("lists does not existed");
			}
			for (String _s : record) {
				Records r = new Records();
				r.setGroupId(list + "_default");
				r.setValue(_s);
				r.setCreatedList(list);
				r.setExpired(expired);
				if (category != null) {
					r.setType(category);
				} else {
					r.setType(_l.getCategory());
				}
				recordsDao.insertSelective(r);
				matchService.addNewRecordIntoList(_s, list);
			}
		} catch (SQLException ex) {
			throw new OrlandException("occur error when copy records group", ex);
		}
	}
}
