package com.xyproto.archfriend.model;

public class Maintainer {

	private String username;
	private String fullName;

	public Maintainer(String username, String fullName) {
		super();
		this.username = username;
		this.fullName = fullName;
	}

	public String getUsername() {
		return username;
	}

	public String getFullName() {
		return fullName;
	}

	@Override
	public String toString() {
		return fullName;
	}

}
