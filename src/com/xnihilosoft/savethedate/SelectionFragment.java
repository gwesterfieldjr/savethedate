package com.xnihilosoft.savethedate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.xnihilosoft.savethedate.utils.ObjectSerializer;

public class SelectionFragment extends Fragment {
	// tag for testing purposes
	private static final String TAG = "SelectionFragment";
	
	// Request Codes
	private static final int REAUTH_ACTIVITY_CODE = 100;
	public static final int RECIPIENTS_COUNT_CODE = 200;
	private static final int USER = 0;
	public static final int SIGNIFICANT_OTHER = 1;
	
	// savedInstanceState keys
	private static final String RECIPIENTS_KEY = "recipients";
	
	// Data objects
	private WeddingDate weddingDate;
	private GraphUser user = null; 
	private GraphUser significantOther = null;
	private List<GraphUser> selectedRecipientsList = null;
	private String noticeMessage = "";
	
	// UI stuff: views, fragments
	private ListView listView;
	private List<PersonListElement> personListElements;
	@SuppressWarnings("unused")
	private EditText noticeMessageView;
	@SuppressWarnings("unused")
	private TextView dateView, weddingDateView, recipientsView, recipientsCountView;
	private DatePickerFragment datePickerFragment;
	
	// Facebook login handle objects
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(final Session session, final SessionState state, final Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection, container, false);

	    loadData();
	    
	    // Find the list view
	    listView = (ListView) view.findViewById(R.id.selection_person_list);
	    
	    // Set up the list view items, based on a list of
	    // PersonListElement items
	    personListElements = new ArrayList<PersonListElement>();
	    
	    // Add an item for the current user
	    personListElements.add(new PersonListElement(USER) {
			@Override
			protected OnClickListener getOnClickListener() {
				// Do nothing..
				return null;
			}
		});
	    
	    // Add item for the spouse to be friend picker
	    personListElements.add(new PersonListElement(SIGNIFICANT_OTHER) {
			@Override
			protected void onActivityResult(Intent data) {
				super.onActivityResult(data);
				significantOther = ((SaveTheDateApplication) getActivity().getApplication()).getSignificantOther();
			    if (significantOther != null) {
					notifyDataChanged();
					setName(significantOther.getName());
					setProfilePictureId(significantOther.getId());
			    }
			}

			@Override
			protected OnClickListener getOnClickListener() {
				return new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		            	String titleText = new StringBuilder().append("Find your ").append(personListElements.get(SIGNIFICANT_OTHER).getType()).toString();
		            	startFriendPickerActivity(PickerActivity.FRIEND_PICKER, getRequestCode(), titleText, false);
		            }
		        };
			}
		});
	    
	    // Set the list view adapter
	    listView.setAdapter(new ActionListAdapter(getActivity(), R.id.selection_person_list, personListElements));
	    
	    // Find the notice message edit text area
	    noticeMessageView = (EditText) view.findViewById(R.id.selection_notice_message);
	    noticeMessageView.setText(noticeMessage);
	    
	    // Find the date text view
	    dateView = (TextView) view.findViewById(R.id.selection_date);
		
	    // Find the wedding date text view
	    weddingDateView = (TextView) view.findViewById(R.id.selection_wedding_date);
		// Initially set to current day
	    weddingDateView.setText(weddingDate.getWeddingDate());
	    
	    weddingDateView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				datePickerFragment = new DatePickerFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						return new DatePickerDialog(getActivity(), this, weddingDate.getYear(), weddingDate.getMonth(), weddingDate.getDayOfMonth());
					}

					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						super.onDateSet(view, year, monthOfYear, dayOfMonth);
						updateWeddingDateView(year, monthOfYear, dayOfMonth);
					}
					
				};
				datePickerFragment.show(getActivity().getSupportFragmentManager());
			}
		});
	    
	    recipientsView = (TextView) view.findViewById(R.id.selection_recipients);
	    
	    recipientsCountView = (TextView) view.findViewById(R.id.selection_recipients_count);
	    recipientsCountView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
            	startFriendPickerActivity(PickerActivity.FRIEND_PICKER, RECIPIENTS_COUNT_CODE, "Select your Recipients", true);
			}
		});
	    	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	        // Get the user's data
	    	if (user == null || significantOther == null) {
	    		makeMeRequest(session);
	    	} else {
	    		updatePersonListElements();
	    	}
	    }
	    
	    if (selectedRecipientsList != null) {
	    	updateRecipientsCountView();
	    }
	   
	    return view;
	}
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    saveData();
	    uiHelper.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REAUTH_ACTIVITY_CODE) {
	      uiHelper.onActivityResult(requestCode, resultCode, data);
	    } else if (resultCode == Activity.RESULT_OK) {
	        if (requestCode == RECIPIENTS_COUNT_CODE) {
	        	selectedRecipientsList = ((SaveTheDateApplication) getActivity().getApplication()).getRecipientsList();
	        	updateRecipientsCountView();
	        } else if (requestCode == SIGNIFICANT_OTHER) {
	        	personListElements.get(SIGNIFICANT_OTHER).onActivityResult(data);
	        }
	    }
	}

	private void makeMeRequest(final Session session) {
	    // Make an API call to get user data and define a 
	    // new callback to handle the response.
	    Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser graphUser, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (graphUser != null) {
	                	user = graphUser;
	                	JSONObject significantOtherObject = (JSONObject) graphUser.asMap().get("significant_other");
	         	        if (significantOtherObject != null) {
	                		significantOther = GraphObject.Factory.create(significantOtherObject, GraphUser.class);
	                		SaveTheDateApplication app = (SaveTheDateApplication) getActivity().getApplication();
	                		app.setSignificantOther(significantOther);
	                	}
	                }
	            }
	            if (response.getError() != null) {
	                // Handle errors, will do so later.
	            }
	            updatePersonListElements();
	        }
	    });
	    request.executeAsync();
	}
	
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
	    if (session != null && session.isOpened()) {
	    	// Get the user's data
	    	if (user == null || significantOther == null) {
	    		makeMeRequest(session);
	    	} else {
	    		updatePersonListElements();
	    	}
	    }
	}
		
	private void updatePersonListElements() {
		personListElements.get(USER).setProfilePictureId(user.getId());
		personListElements.get(USER).setName(user.getName());
		
		if (significantOther.getId() != null) {
			personListElements.get(SIGNIFICANT_OTHER).setProfilePictureId(significantOther.getId());
		}
	
		if (significantOther.getName() == null) {
			String defaultSpouseToBeName = getActivity().getResources().getString(R.string.default_spouse_to_be_name);
			personListElements.get(SIGNIFICANT_OTHER).setName(defaultSpouseToBeName);
		} else {
			personListElements.get(SIGNIFICANT_OTHER).setName(significantOther.getName());
		}
		
		String gender = user.asMap().get("gender").toString();
		String groomType = getActivity().getResources().getString(R.string.person_type_groom);
		String brideType = getActivity().getResources().getString(R.string.person_type_bride);
		String otherType = getActivity().getResources().getString(R.string.person_type_other);
		
		if (gender.equals("female")) {
			personListElements.get(USER).setType(brideType);
			personListElements.get(SIGNIFICANT_OTHER).setType(groomType);
		} else if (gender.equals("male")) {
			personListElements.get(USER).setType(groomType);
			personListElements.get(SIGNIFICANT_OTHER).setType(brideType);
		} else {
			personListElements.get(USER).setType(otherType);
			personListElements.get(SIGNIFICANT_OTHER).setType(otherType);
		}
	}
	
	private void updateWeddingDateView(int year, int monthOfYear, int dayOfMonth) {
		weddingDate.setYear(year);
		weddingDate.setMonth(monthOfYear);
		weddingDate.setDayOfMonth(dayOfMonth);
		weddingDateView.setText(weddingDate.getWeddingDate());
	}
	
	private void updateRecipientsCountView() {
		String text = null;
	    if ( selectedRecipientsList != null) {
	            // If there is one friend
	        if (selectedRecipientsList.size() == 1) {
	            text = String.format(getResources().getString(R.string.single_user_selected), selectedRecipientsList.get(0).getName());
	        } else if (selectedRecipientsList.size() == 2) {
	            // If there are two friends 
	            text = String.format(getResources().getString(R.string.two_users_selected),
	                    selectedRecipientsList.get(0).getName(), selectedRecipientsList.get(1).getName());
	        } else if (selectedRecipientsList.size() > 2) {
	            // If there are more than two friends 
	            text = String.format(getResources().getString(R.string.multiple_users_selected), 
	            		selectedRecipientsList.get(0).getName(), (selectedRecipientsList.size() - 1));
	        }   
	    }   
	    if (text == null) {
	        // If no text, use the placeholder text
	        text = getResources().getString(R.string.default_recipients_count);
	    }   
	    // Set the textview
	    recipientsCountView.setText(text);  
	}
	
	private void startFriendPickerActivity(Uri data, int requestCode, String titleText, boolean isMultiSelect) {
		 Intent intent = new Intent();
	     intent.setData(data);
	     intent.putExtra(PickerActivity.IS_MULTI_SELECT, isMultiSelect);
	     intent.putExtra(PickerActivity.TITLE_TEXT, titleText);
	     intent.putExtra(PickerActivity.REQUEST_CODE, requestCode);
	     intent.setClass(getActivity(), PickerActivity.class);
	     startActivityForResult(intent, requestCode);
	 }
	
	private String getGraphUserString(GraphUser user) {
		return user.getInnerJSONObject().toString();
	}
	
	private GraphUser restoreGraphUser(String user) {
		try {
			return GraphObject.Factory.create(new JSONObject(user), GraphUser.class);
		} catch (JSONException e) {
			Log.e(TAG, "Unable to deserialize GraphUser.", e); 
			return null;
		}
	}
	
	private List<String> getGraphUserList(List<GraphUser> users) {
		List<String> graphUserList = new ArrayList<String>();
		for (GraphUser user : users) {
			graphUserList.add(getGraphUserString(user));
		}
		return graphUserList;
	}
	
	private List<GraphUser> restoreGraphUserList(List<String> users) {
		List<GraphUser> graphUserList = new ArrayList<GraphUser>();
		for (String user : users) {
			graphUserList.add(restoreGraphUser(user));
		}
		return graphUserList;
	}
	
	@SuppressWarnings("rawtypes")
	private void saveData() {
		SharedPreferences data = getActivity().getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = data.edit();
		// WeddingDate information
		editor.putInt(getString(R.string.saved_wedding_day), weddingDate.getDayOfMonth());
		editor.putInt(getString(R.string.saved_wedding_month), weddingDate.getMonth());
		editor.putInt(getString(R.string.saved_wedding_year), weddingDate.getYear());
		
		// Notice message string
		editor.putString(getString(R.string.saved_notice_message), noticeMessageView.getText().toString());
		
		// The user
		editor.putString(getString(R.string.saved_user), getGraphUserString(user));
		
		// The significant other
		editor.putString(getString(R.string.saved_significant_other), getGraphUserString(significantOther));
		
		// The recipients list
		try {
			editor.putString(getString(R.string.saved_recipients_list), ObjectSerializer.serialize((ArrayList<String>) getGraphUserList(selectedRecipientsList)));
		} catch (IOException e) {
			Log.e(TAG, "Unable to serialize recipients list: " + e.getMessage());
		}
		editor.commit();
	}
	
	@SuppressWarnings("unchecked")
	private void loadData() {
		SharedPreferences data = getActivity().getPreferences(Context.MODE_PRIVATE);
		int day = data.getInt(getString(R.string.saved_wedding_day), 0);
		int month = data.getInt(getString(R.string.saved_wedding_month), 0);
		int year = data.getInt(getString(R.string.saved_wedding_year), 0);
		
		if (day != 0 || month != 0 || year != 0) {
			weddingDate = new WeddingDate(year, month, day);
		} else {
			weddingDate = new WeddingDate();
		}
		
		noticeMessage = data.getString(getString(R.string.saved_notice_message), "");
		
		String savedUser = data.getString(getString(R.string.saved_user), null);
		if (savedUser != null) {
			user = restoreGraphUser(savedUser);
		}
		
		String savedSignificantOther = data.getString(getString(R.string.saved_significant_other), null);
		if (savedSignificantOther != null) {
			significantOther = restoreGraphUser(savedSignificantOther);
		}
		
		String savedRecipientsList = data.getString(getString(R.string.saved_recipients_list), null);
		if (savedRecipientsList != null) {
			List<String> recipientsList = null;
			try {
				recipientsList = (ArrayList<String>) ObjectSerializer.deserialize(savedRecipientsList);
			} catch (IOException e) {
				Log.e(TAG, "Unable to deserialize recipients list: " + e.getMessage());
			}
			if (recipientsList != null) {
				selectedRecipientsList = restoreGraphUserList(recipientsList);
			}
		}
		
	}
	
	private class ActionListAdapter extends ArrayAdapter<PersonListElement> {
	    private List<PersonListElement> listElements;

	    public ActionListAdapter(Context context, int resourceId, List<PersonListElement> listElements) {
	        super(context, resourceId, listElements);
	        this.listElements = listElements;
	        // Set up as an observer for list item changes to refresh the view.
	        for (int i = 0; i < listElements.size(); i++) {
	            listElements.get(i).setAdapter(this);
	        }
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View view = convertView;
	        if (view == null) {
	            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(R.layout.personlistitem, null);
	        }

	        PersonListElement listElement = listElements.get(position);
	        if (listElement != null) {
	            view.setOnClickListener(listElement.getOnClickListener());
	            ProfilePictureView profilePicture = (ProfilePictureView) view.findViewById(R.id.person_profile_pic);
	            profilePicture.setCropped(true);
	            TextView userType = (TextView) view.findViewById(R.id.person_type);
	            TextView userName = (TextView) view.findViewById(R.id.person_name);
	            if (profilePicture != null) {
	            	profilePicture.setProfileId(listElement.getProfilePictureId());
	            }
	            if (userType != null) {
	            	userType.setText(listElement.getType());
	            }
	            if (userName != null) {
	            	if (listElement.getRequestCode() == USER) {
	            		userName.setTextColor(Color.parseColor("#333333"));    
	            	} else if (listElement.getRequestCode() == SIGNIFICANT_OTHER) {
	            		userName.setTextColor(Color.parseColor("#3333CC"));   
	            	}
	            	userName.setText(listElement.getName());
	            }
	        }
	        return view;
	    }
	}
	
	public abstract class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		
		public static final String DATE_PICKER = "datePicker";
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return super.onCreateDialog(savedInstanceState);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		}

		public void show(FragmentManager manager) {
			super.show(manager, DATE_PICKER);
		}
	}
	
	public class WeddingDate  {
				
		private Calendar calendar;
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
		
		public WeddingDate() {
			this.calendar = Calendar.getInstance();
		}
		
		public WeddingDate(int year, int month, int dayOfMonth) {
			this.calendar = Calendar.getInstance();
			calendar.set(year, month, dayOfMonth);
		}

		public int getYear() {
			return calendar.get(Calendar.YEAR);
		}

		public void setYear(int year) {
			calendar.set(Calendar.YEAR, year);
		}

		public int getMonth() {
			return calendar.get(Calendar.MONTH);
		}

		public void setMonth(int month) {
			calendar.set(Calendar.MONTH, month);
		}

		public int getDayOfMonth() {
			return calendar.get(Calendar.DAY_OF_MONTH);
		}

		public void setDayOfMonth(int dayOfMonth) {
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		}
		
		public String getWeddingDate() {
			return dateFormat.format(calendar.getTime());
		}
		
	}
	

}