package com.xnihilosoft.savethedate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.xnihilosoft.savethedate.SelectionFragment.OnSelectionFragmentChangeListener;
import com.xnihilosoft.savethedate.helper.WeddingDate;

public class MainActivity extends FragmentActivity {

	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int POST_SELECTION = 2;
	private static final int LOGOUT = 3;
	private static final int FRAGMENT_COUNT = 4;
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private MenuItem logout;
	private MenuItem updateEvent;
	private MenuItem countdown;
	
	private String eventId = "";
	
	private boolean isResumed = false;
	private boolean isActivityResult = false;
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private OnSelectionFragmentChangeListener onSelectionFragmentChangeListener = new OnSelectionFragmentChangeListener() {
		@Override
		public void onEventUpdated(WeddingDate weddingDate) {
			onUpdatedEvent(weddingDate);
		}
		
		@Override
		public void onEventCreated(String eventId, WeddingDate weddingDate) {
			onCreatedEvent(eventId, weddingDate);
		}

		@Override
		public void onActivityResult() {
			isActivityResult = true;
		}
	};
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    loadData();
	    
	    uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.main);

	    FragmentManager fm = getSupportFragmentManager();
	    fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
	    fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
	    fragments[POST_SELECTION] = fm.findFragmentById(R.id.postSelectionFragment);
	    fragments[LOGOUT] = fm.findFragmentById(R.id.logoutFragment);
	    
	    FragmentTransaction transaction = fm.beginTransaction();
	    for(int i = 0; i < fragments.length; i++) {
	        transaction.hide(fragments[i]);
	    }
	    transaction.commit();
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	    isResumed = true;
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	    isResumed = false;
	    saveData();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data); 
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	private void showFragment(int fragmentIndex, boolean addToBackStack) {
	    FragmentManager fm = getSupportFragmentManager();
	    FragmentTransaction transaction = fm.beginTransaction();
	    for (int i = 0; i < fragments.length; i++) {
	        if (i == fragmentIndex) {
	            transaction.show(fragments[i]);
	        } else {
	            transaction.hide(fragments[i]);
	        }
	    }
	    if (addToBackStack) {
	        transaction.addToBackStack(null);
	    }
	    transaction.commit();
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    // Only make changes if the activity is visible
	    if (isResumed) {
	        FragmentManager manager = getSupportFragmentManager();
	        // Get the number of entries in the back stack
	        int backStackSize = manager.getBackStackEntryCount();
	        // Clear the back stack
	        for (int i = 0; i < backStackSize; i++) {
	            manager.popBackStack();
	        }
	        if (state.isOpened()) {
	            // If the session state is open:
	            // Show the authenticated fragment
	        	if (eventId.isEmpty()) {
		    		showFragment(SELECTION, false);
		    	} else {
		    		showFragment(POST_SELECTION, false);
		    	}
	        } else if (state.isClosed()) {
	            // If the session state is closed:
	            // Show the login fragment
	            showFragment(SPLASH, false);
	        }
	    }
	}
	
	private void loadData() {
		SharedPreferences data = this.getPreferences(Context.MODE_PRIVATE);
		eventId = data.getString(getString(R.string.saved_event_id), "");
	}
	
	@SuppressLint("CommitPrefEdits")
	private void saveData() {
		SharedPreferences data = this.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = data.edit();
		editor.putString(getString(R.string.saved_event_id), eventId);
	}
	
	@Override
	protected void onResumeFragments() {
	    super.onResumeFragments();
	    Session session = Session.getActiveSession();
	   
	    if (session != null && session.isOpened()) {
	        // if the session is already open,
	        // try to show the selection fragment
	    	if (eventId.isEmpty()) {
	    		showFragment(SELECTION, false);
	    	} else {
	    		
	    		if (isActivityResult) {
	    			showFragment(SELECTION, false);
	    		} else {
	    			showFragment(POST_SELECTION, false);
	    		}
	    	
	    	}
	    } else {
	        // otherwise present the splash screen
	        // and ask the person to login.
	        showFragment(SPLASH, false);
	    }
	    isActivityResult = false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    // only add the menu when the selection fragment is showing
	    if (fragments[SELECTION].isVisible()) {
	    	if (menu.size() == 0) {
	        	logout = menu.add(R.string.logout);
	        	countdown = menu.add("View Countdown");
	        } else {
	        	menu.clear();
	    		logout = menu.add(R.string.logout);
	    		countdown = menu.add("View Countdown");
	    		updateEvent = null;
	        }
	    	return true;
	    } else if (fragments[POST_SELECTION].isVisible()) {
	    	if (menu.size() == 0 ) {
	    		logout = menu.add(R.string.logout);
	    		updateEvent = menu.add("Update Notice");
	    	} else {
	    		menu.clear();
	    		logout = menu.add(R.string.logout);
	    		updateEvent = menu.add("Update Notice");
	    		countdown = null;
	    	}
        	return true;
	    } else {
	        menu.clear();
	        logout = null;
	        updateEvent = null;
	        countdown = null;
	    }
	    return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.equals(logout)) {
	        showFragment(LOGOUT, false);
	        return true;
	    } else if (item.equals(updateEvent)) {
	    	showFragment(SELECTION, false);
	    	return true;
	    } else if (item.equals(countdown)) {
	    	showFragment(POST_SELECTION, false);
	    	return true;
	    }
	    return false;
	}
	
	
	public OnSelectionFragmentChangeListener getOnSelectionFragmentChangeListener() {
		return onSelectionFragmentChangeListener;
	}
	
	private void onUpdatedEvent(WeddingDate weddingDate) {
		PostSelectionFragment postSelectionFragment = (PostSelectionFragment) fragments[POST_SELECTION];
		postSelectionFragment.updateWeddingDateView(weddingDate);
		postSelectionFragment.updateWeddingDayCountView(weddingDate);
		showFragment(POST_SELECTION, false);
	}
	
	private void onCreatedEvent(String eventId, WeddingDate weddingDate) {
		this.eventId = eventId;
		PostSelectionFragment postSelectionFragment = (PostSelectionFragment) fragments[POST_SELECTION];
		postSelectionFragment.updateWeddingDateView(weddingDate);
		postSelectionFragment.updateWeddingDayCountView(weddingDate);
		showFragment(POST_SELECTION, false);
	}
	
}
