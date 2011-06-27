package com.sqt001.ipcall.util;

public class SoftObj {
	public SoftObj(String id, String title, String message, String url) {
		this.setId(id);
		this.setTitle(title);
		this.setMessage(message);
		this.setUrl(url);
	}
	
	private String id = "";
	private String title = "";
	private String message = "";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	private String url = "";
}
