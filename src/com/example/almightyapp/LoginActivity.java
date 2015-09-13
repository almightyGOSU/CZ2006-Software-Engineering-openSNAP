package com.example.almightyapp;

import com.example.almightyapp.R;
import android.app.Activity;
import android.app.ActionBar;	
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.almightyapp.classes.*;
import com.example.almightyapp.http.HttpURLConnHelper;

public class LoginActivity extends Activity implements OnClickListener {
	
	public static final int REGISTRATION_REQUEST = 111; // Request code
	
	public static final String REGISTRATION_EMAIL = "com.example.almightyapp.EMAIL";
	
	private static final String PREF_USER_EMAIL = "USER_EMAIL";
	
	private ProgressBar _progBar_Login;
	private TextView _tv_Login_Email, _tv_Login_Password, _tv_Login_registerPrompt;
	private EditText _et_Login_Email, _et_Login_Password;
	private Button _btn_Login;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
	    // Setup action bar
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	    actionBar.setDisplayShowTitleEnabled(true);
	    
	    // Store reference to the different views within the layout
	    _progBar_Login = (ProgressBar) findViewById(R.id.progBar_Login);
	    
	    _tv_Login_Email = (TextView) findViewById(R.id.tv_Login_Email);
	    _tv_Login_Password = (TextView) findViewById(R.id.tv_Login_Password);
	    _tv_Login_registerPrompt = (TextView) findViewById(R.id.tv_Login_registerPrompt);
	    
	    _et_Login_Email = (EditText) findViewById(R.id.et_Login_Email);
	    _et_Login_Password = (EditText) findViewById(R.id.et_Login_Password);
	    
	    _btn_Login = (Button) findViewById(R.id.btn_Login);    
	    
	    // Implement onClick Listeners for clickable views
	    _btn_Login.setOnClickListener(this);
	    _tv_Login_registerPrompt.setOnClickListener(this);
	    _et_Login_Email.setOnClickListener(this);
	    _et_Login_Password.setOnClickListener(this);
	    
	    _et_Login_Email.setCursorVisible(false);
		_et_Login_Password.setCursorVisible(false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(_et_Login_Email.getText().toString().equals(""))
		{
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			String userEmail = preferences.getString(PREF_USER_EMAIL, "");
			
			if(!userEmail.equals(""))
			{
				_et_Login_Email.setText(userEmail);
				_et_Login_Password.requestFocus();
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Retain user login information here
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString(PREF_USER_EMAIL, _et_Login_Email.getText().toString());
  
		// Commit to storage
		editor.apply();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onClick(View v) {
        switch(v.getId())
        {
        // Handle onClick for login button
        case R.id.btn_Login:
        	String email = _et_Login_Email.getText().toString();
        	String password = _et_Login_Password.getText().toString();       	
        	
        	new LoginTask().execute(email, password);
        	break;
        
        // Handle onClick for registration prompt
        case R.id.tv_Login_registerPrompt:
        	// Launch registration activity
        	startActivityForResult(new Intent(this, RegistrationActivity.class), REGISTRATION_REQUEST);
        	break;
        	
        case R.id.et_Login_Email:
        	_et_Login_Email.setCursorVisible(true);
			break;
			
		case R.id.et_Login_Password:
			_et_Login_Password.setCursorVisible(true);
			break;
        }
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REGISTRATION_REQUEST) {
            if (resultCode == RESULT_OK) {
            	
            	// Retrieve email from registration page
            	String email = data.getStringExtra(REGISTRATION_EMAIL);
            	
            	// Auto-populate email field
            	((EditText) findViewById(R.id.et_Login_Email)).setText(email);
            	((EditText) findViewById(R.id.et_Login_Password)).setText("");
            }
        }
    }
	
	/**
	 * Asynchronous task used to query database for user information
	 */
	private class LoginTask extends AsyncTask<String, Void, User> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			hideSoftKeyboard();
			toggleViews(false);
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		/**
		 * Method used to query database for user information<br>
		 * based on email & password provided
		 * 
		 * <p> params[0]: email, params[1]: password
		 *
		 * @param params : String[]
		 * 
		 * @return <b>User</b> object if database query is successful
		 * <br><b>null</b> if fields are empty, or database query failed
		 */
		protected User doInBackground(String... params) {
			
			if(isConnected)
			{
				// Both email & password fields cannot be empty
				if(params[0].equals("") || params[1].equals(""))
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = "Email/Password fields cannot be empty";
							Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
						}
					});
					return null;
				}
				
				User user = HttpURLConnHelper.getHttpHelper().getUser(params);
				if(user == null)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = HttpURLConnHelper.getErrorMessage();
							Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
						}
					});
				}
				return user;
			}
			return null;
		}

		protected void onPostExecute(User user) {
			// Restores hidden views & hide progress bar
			toggleViews(true);
			
			if(user != null)
			{
				// Login was successful, Pass user object to main activity
				Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
				mainIntent.putExtra(MainActivity.MAIN_USER_OBJECT, user);
				
	        	startActivity(mainIntent);
			}
		}
	}
	
	/**
	 * Method used to set the visibility of on-screen views
	 * <p>
	 * if bVisible is true, all views except progress bar are shown<br>
	 * Else, hide all views but show progress bar
	 *
	 * @param bVisible : boolean
	 *
	 */
	private void toggleViews(boolean bVisible) {
		// Set progress bar visibility
		_progBar_Login.setVisibility(bVisible ? View.INVISIBLE : View.VISIBLE);
		
		// Set visibility of remaining views
		_tv_Login_Email.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	    _tv_Login_Password.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	    _tv_Login_registerPrompt.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	    
	    _et_Login_Email.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	    _et_Login_Password.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	    
	    _btn_Login.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	}
	
	/**
	 * Method used to hide the soft keyboard
	 */
	private void hideSoftKeyboard() {
	    InputMethodManager inputMethodManager;
	    inputMethodManager = (InputMethodManager)this.getSystemService(
	    		Activity.INPUT_METHOD_SERVICE);
	    
	    if(inputMethodManager != null)
	    {
	    	View focusedView = this.getCurrentFocus();
	    	if(focusedView != null)
	    	{
	    		inputMethodManager.hideSoftInputFromWindow(
	    				focusedView.getWindowToken(), 0);
	    	}
	    }
	}
}