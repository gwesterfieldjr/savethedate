package com.xnihilosoft.savethedate;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.widget.LoginButton;

public class SplashFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.splash, container, false);
	    
	    List<String> readPermissions = new ArrayList<String>();
	    readPermissions.add("user_relationship_details");
	    readPermissions.add("user_friends");
	    readPermissions.add("user_relationships");
	    readPermissions.add("user_events");
	    readPermissions.add("friends_events");
	    
	    List<String> publishPermissions = new ArrayList<String>();
	    publishPermissions.add("create_event");
	    publishPermissions.add("rsvp_event");
	    publishPermissions.add("publish_actions");
	    
	    LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
	    loginButton.setReadPermissions(readPermissions);
	    loginButton.clearPermissions();
	    loginButton.setPublishPermissions(publishPermissions);
	    
	    return view;
	}

}
