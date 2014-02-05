package com.xnihilosoft.savethedate;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.model.GraphUser;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;

public class PickerActivity extends FragmentActivity {

	public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");
	public static final String IS_MULTI_SELECT = "is_multi_select";
	public static final String TITLE_TEXT = "title_text";
	public static final String REQUEST_CODE = "request_code";
	private boolean isMultiSelect = true;
	private String titleText = "Choose Friends";
	private int requestCode = 0;
	
	private FriendPickerFragment friendPickerFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Intent extras = this.getIntent();
	    
	    if (extras != null) {
	    	setMultiSelect(extras.getBooleanExtra(PickerActivity.IS_MULTI_SELECT, isMultiSelect));
	    	setTitleText(extras.getStringExtra(TITLE_TEXT));
	    	setRequestCode(extras.getIntExtra(REQUEST_CODE, requestCode));
	    }
	    
	    setContentView(R.layout.pickers);

	    Bundle args = getIntent().getExtras();
	    FragmentManager manager = getSupportFragmentManager();
	    Fragment fragmentToShow = null;
	    Uri intentUri = getIntent().getData();

	    if (FRIEND_PICKER.equals(intentUri)) {
	        if (savedInstanceState == null) {
	            friendPickerFragment = new FriendPickerFragment(args);
	            friendPickerFragment.setMultiSelect(isMultiSelect);
	            friendPickerFragment.setTitleText(titleText);
	            
	            if (getRequestCode() == SelectionFragment.SIGNIFICANT_OTHER) {
	            	GraphUser significantOther = ((SaveTheDateApplication) getApplication()).getSignificantOther();
	            	if (significantOther != null) {
	            		friendPickerFragment.setSelection(significantOther);
	            	}
	            } else if (getRequestCode() == SelectionFragment.RECIPIENTS_COUNT_CODE) {
	            	List<GraphUser> recipientsList = ((SaveTheDateApplication) getApplication()).getRecipientsList();
	            	if (recipientsList != null && recipientsList.size() > 0) {
	            		friendPickerFragment.setSelection(recipientsList);
	            	}
	            }
	            
	        } else {
	            friendPickerFragment = 
	                (FriendPickerFragment) manager.findFragmentById(R.id.picker_fragment);
	        }
	        // Set the listener to handle errors
	        friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
	            @Override
	            public void onError(PickerFragment<?> fragment,
	                                FacebookException error) {
	                PickerActivity.this.onError(error);
	            }
	        });
	        // Set the listener to handle button clicks
	        friendPickerFragment.setOnDoneButtonClickedListener(
	                new PickerFragment.OnDoneButtonClickedListener() {
	            @Override
	            public void onDoneButtonClicked(PickerFragment<?> fragment) {
	                finishActivity();
	            }
	        });
	        fragmentToShow = friendPickerFragment;

	    } else {
	        // Nothing to do, finish
	        setResult(RESULT_CANCELED);
	        finish();
	        return;
	    }

	    manager.beginTransaction().replace(R.id.picker_fragment, fragmentToShow).commit();
	}
	
	@Override
	protected void onStart() {
	    super.onStart();
	    if (FRIEND_PICKER.equals(getIntent().getData())) {
	        try {
	            friendPickerFragment.loadData(false);
	        } catch (Exception ex) {
	            onError(ex);
	        }
	    }
	}

	private void onError(Exception error) {
		String text = error.getMessage();
	    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
	    toast.show();
	}

	private void onError(String error, final boolean finishActivity) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.error_dialog_title).
	            setMessage(error).
	            setPositiveButton(R.string.error_dialog_button_text, 
	               new DialogInterface.OnClickListener() {
	                @Override
	                public void onClick(DialogInterface dialogInterface, int i) {
	                    if (finishActivity) {
	                        finishActivity();
	                    }
	                }
	            });
	    builder.show();
	}

	private void finishActivity() {
		SaveTheDateApplication app = (SaveTheDateApplication) getApplication();
		if (FRIEND_PICKER.equals(getIntent().getData())) {
		    if (friendPickerFragment != null) {
		    	if (friendPickerFragment.getSelection() != null && friendPickerFragment.getSelection().size() > 0 ) {
			    	if (getRequestCode() == SelectionFragment.RECIPIENTS_COUNT_CODE) {
			    		app.setRecipientsList(friendPickerFragment.getSelection());
			    	} else if (getRequestCode() == SelectionFragment.SIGNIFICANT_OTHER) {
			    		app.setSignificantOther(friendPickerFragment.getSelection().get(0));
			    	}
		    	}
		    }   
		}  
		setResult(RESULT_OK, null);
	    finish();
	}
	
	public void setMultiSelect(boolean isMultiSelect) {
		this.isMultiSelect = isMultiSelect;
	}

	public boolean isMultiSelect() {
		return this.isMultiSelect;
	}
	
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	
	public String getTitleText() {
		return this.titleText;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
}
