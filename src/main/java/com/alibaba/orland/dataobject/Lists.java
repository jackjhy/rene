package com.alibaba.orland.dataobject;

public class Lists {

	private String id;
	private String category;
	private String content;
	private Integer action;
	private String roleScope;
	private String appScope;
	private String partnerScope;
	private String descs;

	public Lists() {
	}

	public Lists(String id, String category, String content, Integer action, String roleScope, String appScope, String partnerScope, String descs) {
		this.id = id;
		this.category = category;
		this.content = content;
		this.action = action;
		this.roleScope = roleScope;
		this.appScope = appScope;
		this.partnerScope = partnerScope;
		this.descs = descs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getAction() {
		return action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}

	public String getRoleScope() {
		return roleScope;
	}

	public void setRoleScope(String roleScope) {
		this.roleScope = roleScope;
	}

	public String getAppScope() {
		return appScope;
	}

	public void setAppScope(String appScope) {
		this.appScope = appScope;
	}

	public String getPartnerScope() {
		return partnerScope;
	}

	public void setPartnerScope(String partnerScope) {
		this.partnerScope = partnerScope;
	}

	public String getDescs() {
		return descs;
	}

	public void setDescs(String descs) {
		this.descs = descs;
	}
}