/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

import java.util.Date;
import java.util.List;

/**
 *
 * @author tiger
 */
public interface ListService {

	/**
	 * 建新名单，必须指定名单名称（此名称全局唯一），不能为空，其余可以为空，全部为空，就是空白名单；
	 * 实际操作为lists表建新记录，表示名单，records表更新关联record group数据
	 * @param listName  list's unique identify
	 * @param combinatedGroup groups that should be combinated into this list
	 * @param importedList list ids those should be imported
	 * @param extendsList list ids those will be extends
	 */
	void createNewList(String listName, String catagory, List<String> combinatedGroup, List<String> importedList, List<String> extendsList) throws OrlandException;

	/**
	 * 从现存的名单中导入数据进新名单，实际为调用导入group
	 * @param listName
	 * @param existedList 
	 */
	void importExistedListInto(String listName, String existedList) throws OrlandException;

	/**
	 * 导入现存的record group数据到新名单,实现逻辑为建新的group，并copy原group数据
	 * @param listName
	 * @param group 
	 */
	void importExistedGroupInto(String listName, String existedGroup) throws OrlandException;

	/**
	 * 继承现有的list，业务逻辑：lists表中，增加原list的groups到新list的groups，进去重文本append
	 * @param listName
	 * @param existedList 
	 */
	void reExtendedFromExistedList(String listName, String existedList) throws OrlandException;

	/**
	 * 同 from list
	 * @param listName
	 * @param existedGroup 
	 */
	void reExtendedFromExistedGroup(String listName, String existedGroup) throws OrlandException;

	/**
	 * 仅删除list记录，实际名单记录（group）不做删除
	 * @param listName 
	 */
	void deleteList(String listName) throws OrlandException;

	/**
	 * 
	 * @return 
	 */
	String createRecordsGroup() throws OrlandException;

	/**
	 * 实际删除名单内容，可能造成空白名单，该方法不负责检查
	 * @param group 
	 */
	void deleteRecordsGroup(String group) throws OrlandException;

	/**
	 * 增加新名单数据，默认进入名单创建的group，如不存在或者多余一个，则抛出异常
	 * @param list
	 * @param record 
	 */
	void addRecordIntoList(String list, String record, String category, Date expired) throws OrlandException;

	/**
	 * 批量增加新名单数据，默认进入名单创建的group，如不存在或者多余一个，则抛出异常
	 * @param list
	 * @param record 
	 */
	void addRecordIntoList(String list, List<String> record,String category, Date expired) throws OrlandException;

	/**
	 * 该名单数据创建list将会为空，后续任何有关数据创建者的权限检查直接通过，有可能造成子名单删除父名单数据；
	 * 一般情况，不建议使用
	 * @param group
	 * @param record 
	 */
	void addRecordIntoGroup(String group, String record,String createdList,String category,Date expired) throws OrlandException;

	/**
	 * 删除名单记录
	 * @param list
	 * @param record 
	 */
	void deleteRecordFromList(String list, String record) throws OrlandException;

	/**
	 * 匹配操作
	 * @param list
	 * @param record
	 * @return 
	 */
	boolean matchRecordInList(String list, String record) throws OrlandException;

}
