package com.xnihilosoft.savethedate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xnihilosoft.savethedate.helper.WeddingDate;

public class PostSelectionFragment extends Fragment {

	private static final String TAG = "PostSelectionFragment";

	private WeddingDate weddingDate = null;
	
	private TextView weddingDayCountView;
	private TextView weddingDateView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.postselection, container, false);
		
		loadData();
		
		weddingDayCountView = (TextView) view.findViewById(R.id.postselection_days_till_weddding_count);
		
		weddingDateView = (TextView) view.findViewById(R.id.postselection_days_till_wedding);
		
		if (weddingDate != null) {
			weddingDateView.setText(weddingDate.toString());
			weddingDayCountView.setText(String.valueOf(weddingDate.getDaysUntilWedding()));
		}
		
		return view;
	}
	
	protected void updateWeddingDateView(WeddingDate weddingDate) {
		setWeddingDate(weddingDate);
		weddingDateView.setText(weddingDate.toString());
	}
	
	protected void updateWeddingDayCountView(WeddingDate weddingDate) {
		setWeddingDate(weddingDate);
		weddingDayCountView.setText(String.valueOf(weddingDate.getDaysUntilWedding()));
	}
	
	protected void setWeddingDate(WeddingDate weddingDate) {
		this.weddingDate = weddingDate;
	}
		
	private void loadData() {
		SharedPreferences data = getActivity().getPreferences(Context.MODE_PRIVATE);
		int day = data.getInt(getString(R.string.saved_wedding_day), 0);
		int month = data.getInt(getString(R.string.saved_wedding_month), 0);
		int year = data.getInt(getString(R.string.saved_wedding_year), 0);
		if (day != 0 || month != 0 || year != 0) {
			weddingDate = new WeddingDate(year, month, day);
		}
	}
	
}
	

