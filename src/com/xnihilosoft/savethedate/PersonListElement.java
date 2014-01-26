package com.xnihilosoft.savethedate;

import android.view.View;
import android.widget.BaseAdapter;

public abstract class PersonListElement {
	
	private String profilePictureId;
	private String type;
	private String name;
	private int requestCode;
	
	private BaseAdapter adapter;
	
	public PersonListElement(int requestCode) {
		super();
		this.requestCode = requestCode;
	}
	
	public void setAdapter(BaseAdapter adapter) {
	    this.adapter = adapter;
	}
	
	public String getProfilePictureId() {
		return profilePictureId;
	}
	
	public void setProfilePictureId(String profilePictureId) {
		this.profilePictureId = profilePictureId;
		
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
		
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		
		if (adapter != null) {
		    adapter.notifyDataSetChanged();
		}
	}
	
	public int getRequestCode() {
		return requestCode;
	}
	
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
	protected abstract View.OnClickListener getOnClickListener();
	
}
