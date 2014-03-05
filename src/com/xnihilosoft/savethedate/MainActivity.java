package com.xnihilosoft.savethedate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;

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
	
	private ArrayAdapter<String> arrayAdapter;
	private ActionBar actionBar;
	
	private List<String> navigationItems;
	private static final int UPDATE_NOTICE = 0;
	private static final int COUNTDOWN = 1;

	private MenuItem shareItem, logoutItem;
		
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
	    displayOverflowActionIcon();
	    createActionBar();
	   	    
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
	    invalidateOptionsMenu();
	    updateActionBar(fragmentIndex);
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

	/**
	 * This is needed to make sure the action overflow icon appears on devices that have a menu button
	 */
	private void displayOverflowActionIcon() {
		try {
	    	  ViewConfiguration config = ViewConfiguration.get(this);
	    	  Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

	    	  if (menuKeyField != null) {
	    	    menuKeyField.setAccessible(true);
	    	    menuKeyField.setBoolean(config, false);
	    	  }
	    	}
	    catch (Exception e) {
	    	 Log.e("MAIN", "Failed to force overflow icon on the Action Bar.");
	   	}
	}

	private void createActionBar() {
		navigationItems = new ArrayList<String>();
	    navigationItems.add(getString(R.string.update_notice));
	    navigationItems.add(getString(R.string.view_countdown));
	    
	    arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, navigationItems);
	    
        //Enabling dropdown list navigation for the action bar
	    actionBar = getActionBar();
	    
	    if (!eventId.isEmpty()) {
	    	updateNavigationDisplay(ActionBar.NAVIGATION_MODE_LIST, false);
	    } else {
	    	updateNavigationDisplay(ActionBar.NAVIGATION_MODE_STANDARD, true);
	    }
 
        ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (itemPosition == UPDATE_NOTICE) {
                	showFragment(SELECTION, false);
                } else if (itemPosition == COUNTDOWN) {
                	showFragment(POST_SELECTION, false);
                }
                return true;
            }
        };
 
        //Setting dropdown items and item navigation listener for the actionbar
        actionBar.setListNavigationCallbacks(arrayAdapter, navigationListener);
	}
	
	private void updateActionBar(int fragmentIndex) {
		if (!eventId.isEmpty()) {
		    if (fragmentIndex == SELECTION) {
		    	updateNavigationDisplay(ActionBar.NAVIGATION_MODE_LIST, false);
		    	actionBar.setSelectedNavigationItem(UPDATE_NOTICE);
		    } else if (fragmentIndex == POST_SELECTION) {
		    	updateNavigationDisplay(ActionBar.NAVIGATION_MODE_LIST, false);
		    	actionBar.setSelectedNavigationItem(COUNTDOWN);
		    } else {
		    	updateNavigationDisplay(ActionBar.NAVIGATION_MODE_STANDARD, true);
		    }
	    } else {
	    	updateNavigationDisplay(ActionBar.NAVIGATION_MODE_STANDARD, true);
	    }
	}
	
	private void updateNavigationDisplay(int navigationMode, boolean isTitleEnabled) {
		actionBar.setNavigationMode(navigationMode);
    	actionBar.setDisplayShowTitleEnabled(isTitleEnabled);
	}
		
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    if (fragments[SELECTION].isVisible()) {
	    	updateMenu(menu, SELECTION);
	    	return true;
	    } else if (fragments[POST_SELECTION].isVisible()) {
	    	updateMenu(menu, POST_SELECTION);
        	return true;
	    } else {
	        menu.clear();
	        logoutItem = null;
	        shareItem = null;
	    }
	    return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.equals(logoutItem)) {
	        showFragment(LOGOUT, false);
	        return true;
	    } else if (item.equals(shareItem)) {
	    	Intent share = new Intent(Intent.ACTION_SEND);
	    	if (fragments[POST_SELECTION].isVisible()) {
	    		PostSelectionFragment postSelectionFragment = (PostSelectionFragment) fragments[POST_SELECTION];
	    		WeddingDate wd = postSelectionFragment.getWeddingDate();
	    		int daysTillWedding = wd.getDaysUntilWedding();
	    		share.setType("text/plain");
	    		share.putExtra(Intent.EXTRA_SUBJECT, "Days until Wedding");
	    		share.putExtra(Intent.EXTRA_TEXT, "I get married in " + daysTillWedding + " days!");
	    	}
	    	showShareDialog(share);
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private void updateMenu(Menu menu, int fragmentId) {
		if (menu.size() != 0 ) {
			menu.clear();
    	}
		
		if (fragmentId == SELECTION) {
			logoutItem = menu.add(R.string.logout);
			shareItem = null;
		} else if (fragmentId == POST_SELECTION) {
			shareItem = menu.add(R.id.action_share);
			shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			shareItem.setIcon(R.drawable.ic_action_share);
			shareItem.setTitle(R.string.share);
			logoutItem = menu.add(R.string.logout);
		} 
	}
	
	private void showShareDialog(Intent intent) {
		startActivity(Intent.createChooser(intent, "Share via"));
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
