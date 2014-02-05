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
	    
	    List<String> permissions = new ArrayList<String>();
	    permissions.add("user_relationship_details");
	    permissions.add("user_friends");
	    permissions.add("user_relationships");
	    
	    LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
	    loginButton.setReadPermissions(permissions);
	    
	    return view;
	}

}
