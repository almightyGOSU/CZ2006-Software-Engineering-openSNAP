package com.example.almightyapp;

import com.example.almightyapp.R;
import com.example.almightyapp.classes.User;
import com.example.almightyapp.http.HttpURLConnHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class EditProfileActivity extends Activity implements OnClickListener {
	
	private User _user;
	
	private ProgressBar _progBar_EditProfile;
	
	private EditText _et_EditProfile_Email;
	private EditText _et_EditProfile_Password1, _et_EditProfile_Password2;
	private EditText _et_EditProfile_Name, _et_EditProfile_Age;
	
	private RadioGroup _radioGroup_EditProfile_Gender;
	
	private Button _btn_EditProfile_Update;
	private Button _btn_EditProfile_Cancel;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);
		
		Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        
        // Store reference to the different views within the layout
        _progBar_EditProfile = (ProgressBar) findViewById(R.id.progBar_EditProfile);
	    
        _et_EditProfile_Email = (EditText) findViewById(R.id.et_EditProfile_Email);
        _et_EditProfile_Password1 = (EditText) findViewById(R.id.et_EditProfile_Password1);
        _et_EditProfile_Password2 = (EditText) findViewById(R.id.et_EditProfile_Password2);
	    
        _et_EditProfile_Name = (EditText) findViewById(R.id.et_EditProfile_Name);
        _et_EditProfile_Age = (EditText) findViewById(R.id.et_EditProfile_Age);
	    
	    _radioGroup_EditProfile_Gender = (RadioGroup)
	    		findViewById(R.id.radioGroup_EditProfile_Gender);
	    
	    _btn_EditProfile_Update = (Button) findViewById(R.id.btn_EditProfile_Update);
	    _btn_EditProfile_Cancel = (Button) findViewById(R.id.btn_EditProfile_Cancel);
	    
	    // Implement onClickListeners for clickable views
	    _btn_EditProfile_Update.setOnClickListener(this);
	    _btn_EditProfile_Cancel.setOnClickListener(this);
        
        Intent intent = getIntent();
		if(intent != null)
		{
			if(intent.hasExtra(MainActivity.MAIN_USER_OBJECT))
			{
				_user = (User) intent.getParcelableExtra(MainActivity.MAIN_USER_OBJECT);
				updateFields();
			}
		}
	}
	
	@Override
    public void onClick(View v) {
        switch(v.getId())
        {
        case R.id.btn_EditProfile_Update:
        	// Handle onClick for Update button
        	
        	// Get all the required information from the text fields
        	String email = _et_EditProfile_Email.getText().toString();
        	String password1 = _et_EditProfile_Password1.getText().toString();
			String password2 = _et_EditProfile_Password2.getText().toString();
			
			String name = _et_EditProfile_Name.getText().toString();
			String age = _et_EditProfile_Age.getText().toString();
			
			int selectedRadioBtnIndex =
					_radioGroup_EditProfile_Gender.getCheckedRadioButtonId();
			RadioButton selectedRadioBtn = (RadioButton)
					findViewById(selectedRadioBtnIndex);
			String gender = selectedRadioBtn.getText().toString();
			
			new UpdateProfileTask().execute(email, _user.getPassword(),
					password1, name, age, gender,
					String.valueOf(_user.getUserID()), password2);
        	break;
        	
        case R.id.btn_EditProfile_Cancel:
        	// Handle onClick for Cancel button
        	String toastText = "Unsaved profile changes discarded";
			Toast.makeText(getApplicationContext(),
					toastText, Toast.LENGTH_SHORT).show();
        	
        	finish();
        	break;
        }
    }
	
	@Override
    public void onBackPressed() {
		// Do nothing here
		// Just prevents back button from dismissing this activity
    }
	
	private void updateFields() {
		if(_user != null)
		{
			_et_EditProfile_Email.setText(_user.getEmail());
			_et_EditProfile_Email.setEnabled(false);
			
			_et_EditProfile_Password1.setText(_user.getPassword());
			_et_EditProfile_Password2.setText(_user.getPassword());
			
			_et_EditProfile_Name.setText(_user.getName());
			_et_EditProfile_Age.setText(String.valueOf(_user.getAge()));
			
			char gender = _user.getGender();
			_radioGroup_EditProfile_Gender.check(
					(gender == 'M') ? R.id.radio_btn_Male :
						R.id.radio_btn_Female);
			
			_et_EditProfile_Email.clearFocus();
			_et_EditProfile_Name.clearFocus();
			_et_EditProfile_Password1.requestFocus();
		}
	}
	
	private class UpdateProfileTask extends AsyncTask<String, Void, Boolean> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			hideSoftKeyboard();
			toggleViews(false);
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// params[0]: email, params[1]: oldPassword, params[2]: newPassword,
		// params[3]: name, params[4]: age, params[5]: gender,
		// params[6]: userID, params[7]: newPassword2
		protected Boolean doInBackground(String... params) {
			if(isConnected)
			{
				// If any of the fields are empty, updating profile fails
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
				
				// If both passwords do not match, updating profile fails
				if(!params[2].equals(params[7]))
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = "Passwords do not match";
							Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
						}
					});
					return false;
				}
				
				// Returns true if user profile is updated successfully
				HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
				boolean registrationResult = httpHelper.updateUserProfile(params);
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
			return true;
		}

		protected void onPostExecute(Boolean result) {
			// Restores hidden views & hide progress bar
			toggleViews(true);
			
			if(result)
			{
				// Successfully registered for new account
				String toastText = "User profile updated successfully";
				Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
				
				// Update user object with all the successfully updated fields
				_user.setPassword(_et_EditProfile_Password1.getText().toString());
				
				_user.setName(_et_EditProfile_Name.getText().toString());
				_user.setAge(Integer.valueOf(_et_EditProfile_Age.getText().toString()));
				
				int selectedRadioBtnIndex =
						_radioGroup_EditProfile_Gender.getCheckedRadioButtonId();
				RadioButton selectedRadioBtn = (RadioButton)
						findViewById(selectedRadioBtnIndex);
				char gender = selectedRadioBtn.getText().toString().charAt(0);
				_user.setGender(gender);
				
				// Create intent, updated user object inside intent
	        	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
	        	
	        	intent.putExtra(MainActivity.MAIN_USER_OBJECT, _user);
	        	
	        	// Set result & return to calling activity
	        	setResult(Activity.RESULT_OK, intent);
	        	finish();
			}
		}
	}
	
	// If bVisible is true, hide progress bar & show buttons 
	// Else, show progress bar & hide buttons
	private void toggleViews(boolean bVisible) {
		// Set progress bar visibility
		_progBar_EditProfile.setVisibility(bVisible ? View.INVISIBLE : View.VISIBLE);
		
		// Set button visibility
		_btn_EditProfile_Update.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
		_btn_EditProfile_Cancel.setVisibility(bVisible ? View.VISIBLE : View.INVISIBLE);
		
		// Set fields interactivity
		_et_EditProfile_Password1.setEnabled(bVisible ? true : false);
		_et_EditProfile_Password2.setEnabled(bVisible ? true : false);
		
		_et_EditProfile_Name.setEnabled(bVisible ? true : false);
		_et_EditProfile_Age.setEnabled(bVisible ? true : false);
		_radioGroup_EditProfile_Gender.setEnabled(bVisible ? true : false);
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