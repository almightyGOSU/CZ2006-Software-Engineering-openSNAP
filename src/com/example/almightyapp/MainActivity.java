package com.example.almightyapp;

import com.example.almightyapp.R;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.almightyapp.classes.*;

public class MainActivity extends Activity {

	public static final String MAIN_USER_OBJECT = "com.example.almightyapp.MAIN_USER_OBJECT";
	public static final int EDIT_PROFILE_REQUEST = 111; // Request code
	
	private static final String PREF_USER_ID = "USER_ID";
	private static final String PREF_USER_EMAIL = "USER_EMAIL";
	private static final String PREF_USER_PASSWORD = "USER_PASSWORD";
	private static final String PREF_USER_NAME = "USER_NAME";
	private static final String PREF_USER_AGE = "USER_AGE";
	private static final String PREF_USER_GENDER = "USER_GENDER";
	
	private ActionBar _actionBar;
	private ActionBar.Tab _journalTab, _browseTab;
    private JournalFragment _journalFragment;
    private BrowseFragment _browseFragment;
    
    private User _user = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// If this activity is started by LoginActivity
		// Retrieve the User object passed here from LoginActivity
		Intent intent = getIntent();
		if(intent != null)
		{
			if(intent.hasExtra(MAIN_USER_OBJECT))
			{
				_user = (User) intent.getParcelableExtra(MAIN_USER_OBJECT);
			}
		}
		
	    // Setup action bar to allow tab navigation
	    _actionBar = getActionBar();
	    _actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    _actionBar.setDisplayShowTitleEnabled(true);
	    
	    // Instantiate the journal & browse fragments
	    _journalFragment = new JournalFragment();
	    _browseFragment = new BrowseFragment();
	    
	    // Set Tab Titles (Can change to icon also!)
	    _journalTab = _actionBar.newTab().setText(R.string.tab_journal);
	    _browseTab = _actionBar.newTab().setText(R.string.tab_browse);
 
        // Set Tab Listeners
	    _journalTab.setTabListener(new TabListener(_journalFragment));
	    _browseTab.setTabListener(new TabListener(_browseFragment));
 
        // Add tabs to actionbar
	    _actionBar.addTab(_journalTab);
	    _actionBar.addTab(_browseTab);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// If this activity is not started by LoginActivity
		// Restore user information using saved data
		if(_user == null)
		{
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			
			int userID = preferences.getInt(PREF_USER_ID, 0);
			String userEmail = preferences.getString(PREF_USER_EMAIL, "");
			String userPassword = preferences.getString(PREF_USER_PASSWORD, "");
			String userName = preferences.getString(PREF_USER_NAME, "");
			int userAge = preferences.getInt(PREF_USER_AGE, 0);
			char userGender = preferences.getString(PREF_USER_GENDER, "").charAt(0);
			
			_user = new User(userID, userEmail, userPassword,
					userName, userAge, userGender);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Retain user information here
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putInt(PREF_USER_ID, _user.getUserID());
		editor.putString(PREF_USER_EMAIL, _user.getEmail());
		editor.putString(PREF_USER_PASSWORD, _user.getPassword());
		editor.putString(PREF_USER_NAME, _user.getName());
		editor.putInt(PREF_USER_AGE, _user.getAge());
		editor.putString(PREF_USER_GENDER, String.valueOf(_user.getGender()));
  
		// Commit to storage
		editor.apply();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_main, menu);
		return true;
	}

	@Override
	// Handle action bar item clicks here
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.user_settings:
			Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
			editProfileIntent.putExtra(MainActivity.MAIN_USER_OBJECT, _user);
			
	        startActivityForResult(editProfileIntent, EDIT_PROFILE_REQUEST);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PROFILE_REQUEST) {
            if (resultCode == RESULT_OK) {
            	
            	// Retrieve updated user object
            	_user = (User) data.getParcelableExtra(MAIN_USER_OBJECT);
            }
        }
    }
	
	// For fragments to get hold of the user object
	public User getUser() {
		return _user;
	}
}