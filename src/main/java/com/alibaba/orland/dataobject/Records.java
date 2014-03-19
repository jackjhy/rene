package com.alibaba.orland.dataobject;

import java.util.Date;

public class Records {

	private Integer id;
	private String value;
	private String type;
	private String groupId;
	private Date expired;
	private String createdList;

	public Records() {
	}

	public Records(Integer id, String value, String type, String groupId, Date expired, String createdList) {
		this.id = id;
		this.value = value;
		this.type = type;
		this.groupId = groupId;
		this.expired = expired;
		this.createdList = createdList;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Date getExpired() {
		return expired;
	}

	public void setExpired(Date expired) {
		this.expired = expired;
	}

	public String getCreatedList() {
		return createdList;
	}

	public void setCreatedList(String createdList) {
		this.createdList = createdList;
	}
}