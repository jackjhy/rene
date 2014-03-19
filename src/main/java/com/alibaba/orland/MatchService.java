/*
 * Copyright 1999-2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.orland;

/**
 *
 * @author tiger
 */
public interface MatchService {

	boolean matchRecordInList(String list, String record) throws OrlandException;

	void addNewRecordIntoList(String record,String list) throws OrlandException;
	
	void addNewRecordIntoGroup(String record,String group) throws OrlandException;

	void removeRecordFromLlist(String record,String list) throws OrlandException;

	void removeLists(String list) throws OrlandException;

	void loadNewList(String list) throws OrlandException;

	void loadNewGroupIntoList(String list,String group) throws OrlandException;

	void removeGroup(String group) throws OrlandException;

}
