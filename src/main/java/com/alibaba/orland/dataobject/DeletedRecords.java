package com.alibaba.orland.dataobject;

import java.util.Date;

public class DeletedRecords {

	private Integer id;
	private String listId;
	private Date deletedTime;

	public DeletedRecords() {
	}

	public DeletedRecords(Integer id, String listId, Date deletedTime) {
		this.id = id;
		this.listId = listId;
		this.deletedTime = deletedTime;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public Date getDeletedTime() {
		return deletedTime;
	}

	public void setDeletedTime(Date deletedTime) {
		this.deletedTime = deletedTime;
	}
}