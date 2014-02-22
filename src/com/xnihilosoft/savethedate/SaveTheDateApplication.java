package com.xnihilosoft.savethedate;

import java.util.List;

import android.app.Application;

import com.facebook.model.GraphUser;
import com.xnihilosoft.savethedate.helper.WeddingDate;

public class SaveTheDateApplication extends Application {

	private List<GraphUser> recipientsList;
	private GraphUser significantOther;
	private WeddingDate weddingDate;

	public List<GraphUser> getRecipientsList() {
		return recipientsList;
	}

	public void setRecipientsList(List<GraphUser> recipientsList) {
		this.recipientsList = recipientsList;
	}

	public WeddingDate getWeddingDate() {
		return weddingDate;
	}

	public void setWeddingDate(WeddingDate weddingDate) {
		this.weddingDate = weddingDate;
	}

	public GraphUser getSignificantOther() {
		return significantOther;
	}

	public void setSignificantOther(GraphUser significantOther) {
		this.significantOther = significantOther;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	
}
