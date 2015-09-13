package com.example.almightyapp;

import com.example.almightyapp.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.almightyapp.http.HttpURLConnHelper;

public class RegistrationActivity extends Activity implements OnClickListener {
	
	private ProgressBar _progBar_Registration;
	
	private EditText _et_Registration_Email;
	private EditText _et_Registration_Password1, _et_Registration_Password2;
	private EditText _et_Registration_Name, _et_Registration_Age;
	
	private RadioGroup _radioGroup_Gender;
	
	private Button _btn_Registration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		
	    // Setup action bar
	    ActionBar actionBar = getActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	    actionBar.setTitle(R.string.registration);
	    actionBar.setDisplayShowTitleEnabled(true);
	    
	    // Store reference to the different views within the layout
	    _progBar_Registration = (ProgressBar) findViewById(R.id.progBar_Registration);
	    
	    _et_Registration_Email = (EditText) findViewById(R.id.et_Registration_Email);
	    _et_Registration_Password1 = (EditText) findViewById(R.id.et_Registration_Password1);
	    _et_Registration_Password2 = (EditText) findViewById(R.id.et_Registration_Password2);
	    
	    _et_Registration_Name = (EditText) findViewById(R.id.et_Registration_Name);
	    _et_Registration_Age = (EditText) findViewById(R.id.et_Registration_Age);
	    
	    _radioGroup_Gender = (RadioGroup) findViewById(R.id.radioGroup_Gender);
	    
	    _btn_Registration = (Button) findViewById(R.id.btn_Registration_Register);
	    
	    // Implement onClickListeners for clickable views
	    _btn_Registration.setOnClickListener(this);
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
        case R.id.btn_Registration_Register:
        	// Handle onClick for register button
        	
        	// Get all the required information from the text fields
        	String email = _et_Registration_Email.getText().toString();
        	String password1 = _et_Registration_Password1.getText().toString();
			String password2 = _et_Registration_Password2.getText().toString();
			
			String name = _et_Registration_Name.getText().toString();
			String age = _et_Registration_Age.getText().toString();
			
			int selectedRadioBtnIndex = _radioGroup_Gender.getCheckedRadioButtonId();
			RadioButton selectedRadioBtn = (RadioButton) findViewById(selectedRadioBtnIndex);
			String gender = selectedRadioBtn.getText().toString();
			
			new RegistrationTask().execute(email, password1, password2, name, age, gender);     	
        	break;
        }
    }
	
	private class RegistrationTask extends AsyncTask<String, Void, Boolean> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			hideSoftKeyboard();
			toggleViews(false);
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// Write things to do for the registration process inside here
		// params[0]: email, params[1]: password1, params[2]: password2
		// params[3]: name, params[4]: age, params[5]: gender
		protected Boolean doInBackground(String... params) {
			if(isConnected)
			{
				// If any of the fields are empty, registration fails
				for(String param: params)
				{
					if(param.equals(""))
					{
						runOnUiThread(new Runnable() {
							public void run() {
								String toastText = "Please fill all required fields";
								Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
							}
						});
						return false;
					}
				}
				
				// If both passwords do not match, registration fails
				if(!params[1].equals(params[2]))
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = "Passwords do not match";
							Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
						}
					});
					return false;
				}
				
				// Returns true if account is created successfully
				HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
				boolean registrationResult = httpHelper.createNewUser(params);
				if(registrationResult == false)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = HttpURLConnHelper.getErrorMessage();
							Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
						}
					});
				}
				return registrationResult;
			}	
			return false;
		}

		protected void onPostExecute(Boolean result) {
			// Restores hidden views & hide progress bar
			toggleViews(true);
			
			if(result)
			{
				// Successfully registered for new account
				String toastText = "New account successfully created";
				Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
				
				// Create intent, store email & password inside intent
	        	Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
	        	
	        	String email = _et_Registration_Email.getText().toString();
	        	intent.putExtra(LoginActivity.REGISTRATION_EMAIL, email);
	        	
	        	// Set result & return to calling activity
	        	setResult(Activity.RESULT_OK, intent);
	        	finish();
			}
		}
	}
	
	// If bVisible is true, hide progress bar & show registration button 
	// Else, show progress bar & hide registration button
	private void toggleViews(boolean bVisible) {
		// Set progress bar visibility
		_progBar_Registration.setVisibility(bVisible ? View.INVISIBLE : View.VISIBLE);
		
		// Set registration button visibility
		_btn_Registration.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
	}
	
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