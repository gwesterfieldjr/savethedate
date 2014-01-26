package com.xnihilosoft.savethedate;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
	    
	    // Find the list view
	    listView = (ListView) view.findViewById(R.id.selection_person_list);
	    
	    // Set up the list view items, based on a list of
	    // CoupleListElement items
	    personListElements = new ArrayList<PersonListElement>();
	    
	    // Add an item for the current user
	    personListElements.add(new PersonListElement(USER) {
			@Override
			protected OnClickListener getOnClickListener() {
				return null;
			}
		});
	    
	    // Add item for the spouse to be friend picker
	    personListElements.add(new PersonListElement(SPOUSE_TO_BE) {
			@Override
			protected OnClickListener getOnClickListener() {
				
				return null;
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
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    // Set the id for the ProfilePictureView
	                    // view that in turn displays the profile picture.
	                	personListElements.get(USER).setProfilePictureId(user.getId());
	                    // Set the Textview's text to the user's name.
	                	personListElements.get(USER).setName(user.getName());
	                	
	                    // Set the userType TextView text to bride or groom
	                    String gender = user.asMap().get("gender").toString();
	                    if (gender.contains("female")) {
	                    	personListElements.get(USER).setType("Bride");
	                    } else if (gender.contains("male")) {
	                    	personListElements.get(USER).setType("Groom");
	                    } else {
	                    	personListElements.get(USER).setType("Other");
	                    }
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
	    }
	}
	
	private class ActionListAdapter extends ArrayAdapter<PersonListElement> {
	    private List<PersonListElement> listElements;

	    public ActionListAdapter(Context context, int resourceId, 
	                             List<PersonListElement> listElements) {
	        super(context, resourceId, listElements);
	        this.listElements = listElements;
	        // Set up as an observer for list item changes to
	        // refresh the view.
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
	            if (userType != null) {
	            	userName.setText(listElement.getName());
	            }
	        }
	        return view;
	    }

	}
	
}
