package com.xnihilosoft.savethedate;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class SelectionFragment extends Fragment {

	private static final String TAG = "SelectionFragment";
	private static final int REAUTH_ACTIVITY_CODE = 100;
	private static final int USER = 0;
	private static final int SPOUSE_TO_BE = 1;
	
	private Person user, spouseToBe;
	
	private EditText noticeMessageView;
	private ListView listView;
	private List<PersonListElement> personListElements;
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
	    
	    user = new Person();
	    spouseToBe = new Person();
	    
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
	    personListElements.add(new PersonListElement(SPOUSE_TO_BE) {
			@Override
			protected OnClickListener getOnClickListener() {
				return new View.OnClickListener() {
		            @Override
		            public void onClick(View view) {
		            	startPickerActivity(PickerActivity.FRIEND_PICKER, getRequestCode());
		            }
		        };
			}
		});
	    
	    // Set the list view adapter
	    listView.setAdapter(new ActionListAdapter(getActivity(), R.id.selection_person_list, personListElements));
	    
	    // Find the notice message edit text area
	    noticeMessageView = (EditText) view.findViewById(R.id.selection_notice_message);
	    
	    // Check for an open session
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	        // Get the user's data
	        makeMeRequest(session);
	    }
	     
	    return view;
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
	                	user.setId(graphUser.getId());
	                	user.setName(graphUser.getName());
	                	user.setGender(graphUser.asMap().get("gender").toString());
	                	
	                	updatePersonListElements();
	                }
	            }
	            if (response.getError() != null) {
	                // Handle errors, will do so later.
	            }
	        }
	    });
	    request.executeAsync();
	}
	
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
	    if (session != null && session.isOpened()) {
	        // Get the user's data.
	        makeMeRequest(session);
	    }
	}
	
	private void updatePersonListElements() {
		personListElements.get(USER).setProfilePictureId(user.getId());
		
		personListElements.get(USER).setName(user.getName());
		
		String defaultSpouseToBeName = getActivity().getResources().getString(R.string.default_spouse_to_be_name);
		personListElements.get(SPOUSE_TO_BE).setName(defaultSpouseToBeName);
		
		String gender = user.getGender();
		String groomType = getActivity().getResources().getString(R.string.person_type_groom);
		String brideType = getActivity().getResources().getString(R.string.person_type_bride);
		String otherType = getActivity().getResources().getString(R.string.person_type_other);
		
		if (gender.equals("female")) {
			personListElements.get(USER).setType(brideType);
			personListElements.get(SPOUSE_TO_BE).setType(groomType);
		} else if (gender.equals("male")) {
			personListElements.get(USER).setType(groomType);
			personListElements.get(SPOUSE_TO_BE).setType(brideType);
		} else {
			personListElements.get(USER).setType(otherType);
			personListElements.get(SPOUSE_TO_BE).setType(otherType);
		}
		
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
	    uiHelper.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REAUTH_ACTIVITY_CODE) {
	      uiHelper.onActivityResult(requestCode, resultCode, data);
	    } else if (resultCode == Activity.RESULT_OK) {
	        // Do nothing for now
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
	            	userName.setText(listElement.getName());
	            	if (userName.getText().equals(getString(R.string.default_spouse_to_be_name))) {
	            		userName.setTextColor(Color.parseColor("#6699CC"));
	            	}
	            }
	        }
	        return view;
	    }

	}
	
	private void startPickerActivity(Uri data, int requestCode) {
	     Intent intent = new Intent();
	     intent.setData(data);
	     intent.setClass(getActivity(), PickerActivity.class);
	     startActivityForResult(intent, requestCode);
	 }
	
	
	
}
