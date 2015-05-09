package com.ithaha.mobilesafe.domain;

import android.graphics.drawable.Drawable;

/**
 * App信息的业务bean
 * 
 * @author hello
 * 
 */
public class AppInfo {

	private Drawable icon;
	private String name;
	private String packname;
	private boolean inRom;
	private boolean userApp;
	private int uid;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackname() {
		return packname;
	}

	public void setPackname(String packname) {
		this.packname = packname;
	}

	public boolean isInRom() {
		return inRom;
	}

	public void setInRom(boolean inRom) {
		this.inRom = inRom;
	}

	public boolean isUserApp() {
		return userApp;
	}

	public void setUserApp(boolean userApp) {
		this.userApp = userApp;
	}

	@Override
	public String toString() {
		return "AppInfo [icon=" + icon + ", name=" + name + ", packname="
				+ packname + ", inRom=" + inRom + ", userApp=" + userApp + "]";
	}

}
