package com.xnihilosoft.savethedate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.xnihilosoft.savethedate.helper.WeddingDate;

public class PostSelectionFragment extends Fragment {

	@SuppressWarnings("unused")
	private static final String TAG = "PostSelectionFragment";

	private WeddingDate weddingDate = null;
	
	private TextView weddingDayCountView;
	private TextView weddingDateView;
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        //onSessionStateChange(session, state, exception);
	    }
	};
	
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
	
	public WeddingDate getWeddingDate() {
		return this.weddingDate;
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
}
	

