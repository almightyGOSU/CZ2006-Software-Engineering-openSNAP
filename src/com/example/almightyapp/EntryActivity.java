package com.example.almightyapp;

import java.io.File;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.example.almightyapp.classes.*;
import com.example.almightyapp.http.HttpURLConnHelper;
import com.example.almightyapp.R;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class EntryActivity extends Activity implements
		RatingBar.OnRatingBarChangeListener, OnClickListener,
		OnEditorActionListener, OnItemSelectedListener {
	
	public final static String ENTRY_USER_ID = "com.example.almightyapp.USER_ID";
	public final static String ENTRY_PRODUCT_ID = "com.example.almightyapp.PRODUCT_ID";
	
	// Unique Request Codes
	public final static int ENTRY_IMAGE_TAKE_PHOTO = 111;
	public final static int ENTRY_IMAGE_GALLERY_PICK = 222;
	public final static int ENTRY_IMAGE_CROP_IMAGE = 333;

	// All the view objects within the layout
	private ProgressDialog _progDialog_Entry;
	
	private ImageView _iv_Entry_productImage;
	private EditText _et_Entry_productName;
	
	private Spinner _spinner_Entry_productManufacturer;
	private Spinner _spinner_Entry_packagingType;
	
	private RatingBar _rb_Entry_overallRating;
	
	private LinearLayout _grp_Entry_easeRating,
			_grp_Entry_safetyRating,
			_grp_Entry_resealRating;
	
	private RatingBar _rb_Entry_easeRating,
			_rb_Entry_safetyRating,
			_rb_Entry_resealRating;
	
	private EditText _et_Entry_comment;
	
	private String _userID;
	private String _productID;
	
	private Entry _entry;
	
	// Used to restore original values when user cancels editing of entry
	private Entry _originalEntry;
	
	private boolean _bEditMode = false;
	
	public static boolean _reloadJournal = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayShowTitleEnabled(false);
	    
	    // Store reference to the different views within the layout
	    findViews();
		
		// Get the userID & entryID from main activity
	    Intent intent = getIntent();
	    _userID = intent.getStringExtra(ENTRY_USER_ID);
	    _productID = intent.getStringExtra(ENTRY_PRODUCT_ID);
	    
	    // Add rating changed listeners for rating bars
	    _rb_Entry_easeRating.setOnRatingBarChangeListener(this);
	    _rb_Entry_safetyRating.setOnRatingBarChangeListener(this);
	    _rb_Entry_resealRating.setOnRatingBarChangeListener(this);
	    
	    // Add on click listener for product image & edit text
	    _iv_Entry_productImage.setOnClickListener(this);
	    _et_Entry_productName.setOnClickListener(this);
	    _et_Entry_comment.setOnClickListener(this);
	    
	    // Add on focus changed listener for the edit text
	    _et_Entry_productName.setOnEditorActionListener(this);
	    _et_Entry_comment.setOnEditorActionListener(this);
	    
	    // Override the colors of the rating bar stars
	    int colorValue = Color.YELLOW;
	    android.graphics.PorterDuff.Mode mode = android.graphics.PorterDuff.Mode.SRC_ATOP;
	    LayerDrawable stars = (LayerDrawable) _rb_Entry_overallRating.getProgressDrawable();
	    stars.getDrawable(2).setColorFilter(colorValue, mode);
	    stars = (LayerDrawable) _rb_Entry_easeRating.getProgressDrawable();
	    stars.getDrawable(2).setColorFilter(colorValue, mode);
	    stars = (LayerDrawable) _rb_Entry_safetyRating.getProgressDrawable();
	    stars.getDrawable(2).setColorFilter(colorValue, mode);
	    stars = (LayerDrawable) _rb_Entry_resealRating.getProgressDrawable();
	    stars.getDrawable(2).setColorFilter(colorValue, mode);
	    
	    // Product Manufacturer Spinner
	    ArrayAdapter<CharSequence> manufacturersAdapter = ArrayAdapter.createFromResource(this,
	    		R.array.dd_manufacturers_array, R.layout.spinner_layout);
	    manufacturersAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
	    _spinner_Entry_productManufacturer.setAdapter(manufacturersAdapter);
	    _spinner_Entry_productManufacturer.setOnItemSelectedListener(this);
	    
	    // Product Packaging Type Spinner
	    ArrayAdapter<CharSequence> packagingTypeAdapter = ArrayAdapter.createFromResource(this,
	    		R.array.dd_packaging_type_array, R.layout.spinner_layout);
	    packagingTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
	    _spinner_Entry_packagingType.setAdapter(packagingTypeAdapter);
	    _spinner_Entry_packagingType.setOnItemSelectedListener(this);
	    
	    
	    new LoadEntryTask().execute(_userID, _productID);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Retain journal information here
		SharedPreferences preferences = getPreferences(
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
				
		editor.putBoolean(JournalFragment.PREF_RELOAD_JOURNAL,
				_reloadJournal);

		// Commit to storage
		editor.apply();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_entry, menu);
		
		menu.findItem(R.id.entry_action_accept).setVisible(_bEditMode ? true : false);
		menu.findItem(R.id.entry_action_accept).setEnabled(_bEditMode ? true : false);

		menu.findItem(R.id.entry_action_cancel).setVisible(_bEditMode ? true : false);
		menu.findItem(R.id.entry_action_cancel).setEnabled(_bEditMode ? true : false);

		menu.findItem(R.id.entry_action_share).setVisible(_bEditMode ? false : true);
		menu.findItem(R.id.entry_action_share).setEnabled(_bEditMode ? false : true);

		menu.findItem(R.id.entry_action_edit).setVisible(_bEditMode ? false : true);
		menu.findItem(R.id.entry_action_edit).setEnabled(_bEditMode ? false : true);

		menu.findItem(R.id.entry_action_discard).setVisible(_bEditMode ? false : true);
		menu.findItem(R.id.entry_action_discard).setEnabled(_bEditMode ? false : true);
		    
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.entry_action_accept:
			hideSoftKeyboard();
			
			// Calls updateEntry to initiate the uploading process
			updateEntry();
			
			JournalFragment.reloadJournal();
			break;
			
		case R.id.entry_action_cancel:
			toggleMode(false);
			
			if(_entry.getEntryID() == 0)
			{
				// User cancelled creation of new entry
				// Navigate back to journal view
				NavUtils.navigateUpFromSameTask(this);
			}
			else
			{
				// User cancelled editing of existing entry
				Toast.makeText(this, "Unsaved changes discarded",
						Toast.LENGTH_SHORT).show();
				
				// Discard unsaved changes by restoring the original entry
				_entry = new Entry(_originalEntry);
				
				_iv_Entry_productImage.setImageBitmap(_entry.getProductImage());
				displayEntry();
			}
			break;
			
		case R.id.entry_action_share:
			createShareIntent();
			break;
			
		case R.id.entry_action_edit:
			// Clone the orignal entry
			_originalEntry = new Entry(_entry);
			
			toggleMode(true);
			
			break;
			
		case R.id.entry_action_discard:
			deleteEntry();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		if(fromUser)
		{
			// Changed because of user's input
			// Not because it has been changed programatically
			switch(ratingBar.getId())
			{
			case R.id.rb_Entry_easeRating:
				_entry.setEaseRating((int) rating);
				_rb_Entry_overallRating.setRating((float)_entry.getOverallRating());
				break;
				
			case R.id.rb_Entry_safetyRating:
				_entry.setSafetyRating((int) rating);
				_rb_Entry_overallRating.setRating((float)_entry.getOverallRating());
				break;
				
			case R.id.rb_Entry_resealRating:
				_entry.setResealRating((int) rating);
				_rb_Entry_overallRating.setRating((float)_entry.getOverallRating());
				break;
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.iv_Entry_productImage:
			selectImage();
			break;
		
		case R.id.et_Entry_productName:
			_et_Entry_productName.setCursorVisible(true);
			break;
			
		case R.id.et_Entry_comment:
			_et_Entry_comment.setCursorVisible(true);
			break;
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch(parent.getId())
		{
		case R.id.spinner_Entry_productManufacturer:
			String selectedProductManufacturer;
			selectedProductManufacturer = parent.getSelectedItem().toString();
			if(_entry != null)
				_entry.setProductManufacturer(selectedProductManufacturer);
			break;
			
		case R.id.spinner_Entry_packagingType:
			String selectedPackagingType;
			selectedPackagingType = parent.getSelectedItem().toString();
			if(_entry != null)
				_entry.setProductPackagingType(selectedPackagingType);
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing in this case
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_DONE)
		{
			if (v.getId() == R.id.et_Entry_productName) {
				_et_Entry_productName.clearFocus();
				_et_Entry_productName.setCursorVisible(false);
			} else if (v.getId() == R.id.et_Entry_comment) {
				_et_Entry_comment.clearFocus();
				_et_Entry_comment.setCursorVisible(false);
			}
		}
		return false;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK)
		{
			if (requestCode == ENTRY_IMAGE_TAKE_PHOTO)
			{
				File fileDirectory = new File(Environment
						.getExternalStorageDirectory().toString());
				String imageFileName = _entry.getProductID() + ".jpg";
				File photo = null;

				// Search for the photo using the given image file name
				for (File file : fileDirectory.listFiles()) {
					if (file.getName().equals(imageFileName)) {
						photo = file;
						break;
					}
				}
				
				Uri capturedPhotoUri = Uri.fromFile(photo);		
				cropImage(capturedPhotoUri);
			}
			else if (requestCode == ENTRY_IMAGE_GALLERY_PICK)
			{
				Uri selectedImageUri = data.getData();			
				cropImage(selectedImageUri);
			}
			else if (requestCode == ENTRY_IMAGE_CROP_IMAGE)
			{
				// Get the returned data
				Bundle extras = data.getExtras();
				
				// Get the cropped image
				Bitmap croppedImage = extras.getParcelable("data");
				
				if (croppedImage != null)
				{
					_iv_Entry_productImage.setImageBitmap(croppedImage);
					_entry.setProductImage(croppedImage);
				}
			}
		}
	}
	
	private void findViews() {
		// Store reference to the different views within the layout
		_iv_Entry_productImage = (ImageView) findViewById(R.id.iv_Entry_productImage);
		_et_Entry_productName = (EditText) findViewById(R.id.et_Entry_productName);
		
		_spinner_Entry_productManufacturer = (Spinner)
				findViewById(R.id.spinner_Entry_productManufacturer);
		_spinner_Entry_packagingType = (Spinner)
				findViewById(R.id.spinner_Entry_packagingType);
		
		_rb_Entry_overallRating = (RatingBar) findViewById(R.id.rb_Entry_overallRating);
		
		_grp_Entry_easeRating = (LinearLayout) findViewById(R.id.grp_Entry_easeRating);
		_rb_Entry_easeRating = (RatingBar) findViewById(R.id.rb_Entry_easeRating);
		
		_grp_Entry_safetyRating = (LinearLayout) findViewById(R.id.grp_Entry_safetyRating);
		_rb_Entry_safetyRating = (RatingBar) findViewById(R.id.rb_Entry_safetyRating);
		
		_grp_Entry_resealRating = (LinearLayout) findViewById(R.id.grp_Entry_resealRating);
		_rb_Entry_resealRating = (RatingBar) findViewById(R.id.rb_Entry_resealRating);
		
		_et_Entry_comment = (EditText) findViewById(R.id.et_Entry_comment);
	}
	
	// If bEditable is true, allow editable fields to be edited
	// and show appropriate action bar icons
	// Else, disable editable fields and show appropriate action bar icons
	private void toggleMode(boolean bEditable) {
		// Enable/disable product image view
		_iv_Entry_productImage.setEnabled(bEditable ? true : false);
		
		// Enable/disable product name text field
		_et_Entry_productName.setEnabled(bEditable ? true : false);
		
		// Enable/disable product manufacturers and packaging
		// type spinners
		_spinner_Entry_productManufacturer.setEnabled(bEditable ? true : false);
		_spinner_Entry_packagingType.setEnabled(bEditable ? true : false);
		
		// Enable/disable rating bars
		_rb_Entry_easeRating.setIsIndicator(bEditable ? false : true);
		_rb_Entry_safetyRating.setIsIndicator(bEditable ? false : true);
		_rb_Entry_resealRating.setIsIndicator(bEditable ? false : true);
		
		// Enable/disable product review text field
		_et_Entry_comment.setEnabled(bEditable ? true : false);
		
		// Change background color values of editable fields
		int drawableResId = bEditable ? R.drawable.background_dark :
				R.drawable.background_light;
		_iv_Entry_productImage.setBackgroundResource(drawableResId);
		_et_Entry_productName.setBackgroundResource(drawableResId);
		_spinner_Entry_productManufacturer.setBackgroundResource(drawableResId);
		_spinner_Entry_packagingType.setBackgroundResource(drawableResId);
		_grp_Entry_easeRating.setBackgroundResource(drawableResId);
		_grp_Entry_safetyRating.setBackgroundResource(drawableResId);
		_grp_Entry_resealRating.setBackgroundResource(drawableResId);
		_et_Entry_comment.setBackgroundResource(drawableResId);
		
		_bEditMode = bEditable;
		this.invalidateOptionsMenu();
	}
	
	private void displayEntry() {
		if(_entry.getEntryID() != 0)
		{	
			// Existing entry - display it
			_et_Entry_productName.setText(_entry.getProductName());
			
			// Product manufacturer spinner
			String productManufacturer;
			productManufacturer = _entry.getProductManufacturer();
			
			if(productManufacturer.equals("Kraft"))
				_spinner_Entry_productManufacturer.setSelection(0);
	    	else if(productManufacturer.equals("Marigold"))
	    		_spinner_Entry_productManufacturer.setSelection(1);
	    	else if(productManufacturer.equals("Meiji"))
	    		_spinner_Entry_productManufacturer.setSelection(2);
	    	else if(productManufacturer.equals("Nestle"))
	    		_spinner_Entry_productManufacturer.setSelection(3);
	    	else if(productManufacturer.equals("Quaker"))
	    		_spinner_Entry_productManufacturer.setSelection(4);
			
			// Packaging type spinner
			String packagingType;
			packagingType = _entry.getProductPackagingType();
			
			if(packagingType.equals("Bag"))
				_spinner_Entry_packagingType.setSelection(0);
	    	else if(packagingType.equals("Bottle"))
	    		_spinner_Entry_packagingType.setSelection(1);
	    	else if(packagingType.equals("Box"))
	    		_spinner_Entry_packagingType.setSelection(2);
	    	else if(packagingType.equals("Can"))
	    		_spinner_Entry_packagingType.setSelection(3);
	    	else if(packagingType.equals("Carton"))
	    		_spinner_Entry_packagingType.setSelection(4);
	    	else if(packagingType.equals("Tub"))
	    		_spinner_Entry_packagingType.setSelection(5);
	    	else if(packagingType.equals("Wrapper"))
	    		_spinner_Entry_packagingType.setSelection(6);
			
			// Ratings
			_rb_Entry_easeRating.setRating(_entry.getEaseRating());
			_rb_Entry_safetyRating.setRating(_entry.getSafetyRating());
			_rb_Entry_resealRating.setRating(_entry.getResealRating());
			_rb_Entry_overallRating.setRating((float)_entry.getOverallRating());
			
			// Packaging Review
			_et_Entry_comment.setText(_entry.getComment());
		}
		else
		{
			// New Entry, allow editing
			toggleMode(true);
		}
	}
	
	private void updateEntry() {
		
		if(_et_Entry_productName.getText().toString().equals("") ||
			_et_Entry_comment.getText().toString().equals(""))
		{
			Toast.makeText(this, "Please fill all required fields",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(_entry.getProductImage() == null)
		{
			Toast.makeText(this, "Please provide an product image",
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		_entry.setProductName(_et_Entry_productName.getText().toString());
		
		String productManufacturer, packagingType;
		productManufacturer = _spinner_Entry_productManufacturer.getSelectedItem().toString();
		packagingType = _spinner_Entry_packagingType.getSelectedItem().toString();
		_entry.setProductManufacturer(productManufacturer);
		_entry.setProductPackagingType(packagingType);
		
		_entry.setEaseRating((int)_rb_Entry_easeRating.getRating());
		_entry.setSafetyRating((int)_rb_Entry_safetyRating.getRating());
		_entry.setResealRating((int)_rb_Entry_resealRating.getRating());
		
		_entry.setComment(_et_Entry_comment.getText().toString());
		
		// Calls async UploadEntryTask
		new UploadEntryTask().execute(_entry);
	}
	
	private void deleteEntry() {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Delete Entry");
        dialog.setMessage("Do you really wish to delete this entry?");
        dialog.setCancelable(false);
        
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick (DialogInterface dialog, int buttonId) {
            	JournalFragment.reloadJournal();
            	
                // Start an async task to do the deletion
            	new DeleteEntryTask().execute(_entry);
            }
        });
        
        dialog.setNegativeButton("No",
                new DialogInterface.OnClickListener () {
            public void onClick (DialogInterface dialog, int buttonId) {
                dialog.dismiss();
            }
        });
        
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
	}
	
	private void createShareIntent() {
		
		Bitmap icon = _entry.getProductImage();
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");

		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, "title");
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		
		Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
				values);
		OutputStream outstream;
		
		try {
			outstream = getContentResolver().openOutputStream(uri);
			icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
			outstream.close();
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		
		String message = "Check out my review for \"" +
				_entry.getProductName() + "\" on openSNAP!";
		String productRating = new DecimalFormat("#.00").format(
				_entry.getOverallRating());
		message += "\nOverall Rating: " + productRating +
				"\nPackaging Review: " + _entry.getComment();
		
		share.putExtra(Intent.EXTRA_STREAM, uri);
		share.putExtra(Intent.EXTRA_TEXT, message);
		
		startActivity(Intent.createChooser(share, "Share Product Review"));
	}
	
	private void selectImage() {
		final CharSequence[] options = {"Take Photo", "Choose from Gallery",
				"Cancel"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Update Product Image");
		builder.setItems(options, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (options[item].equals("Take Photo"))
				{
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					String imageFileName = _entry.getProductID() + ".jpg";
					File file = new File(Environment.getExternalStorageDirectory(),
							imageFileName);

					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
					startActivityForResult(intent, ENTRY_IMAGE_TAKE_PHOTO);

				}
				else if (options[item].equals("Choose from Gallery"))
				{
					Intent intent = new Intent(Intent.ACTION_PICK,
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, ENTRY_IMAGE_GALLERY_PICK);
				}
				else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}
	
	private void cropImage(final Uri originalImageUri) {
	    final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

	    Intent intent = new Intent("com.android.camera.action.CROP");
	    intent.setType("image/*");

	    List<ResolveInfo> list = getPackageManager().queryIntentActivities(
	            intent, 0);

	    int size = list.size();

	    if (size == 0)
	    {
	        Toast.makeText(this, "No application available for cropping",
	                Toast.LENGTH_SHORT).show();
	        return;
	    }
	    else
	    {
	        intent.setDataAndType(originalImageUri, "image/*");

	        intent.putExtra("outputX", 400);
	        intent.putExtra("outputY", 400);
	        intent.putExtra("aspectX", 1);
	        intent.putExtra("aspectY", 1);
	        intent.putExtra("scale", true);
	        intent.putExtra("return-data", true);

	        if (size == 1) {
	            Intent imageCropIntent = new Intent(intent);
	            ResolveInfo res = list.get(0);

	            imageCropIntent.setComponent(new ComponentName(res.activityInfo.packageName,
	                    res.activityInfo.name));

	            startActivityForResult(imageCropIntent, ENTRY_IMAGE_CROP_IMAGE);
	        } else {
	            for (ResolveInfo res : list) {
	                final CropOption co = new CropOption();

	                co.title = getPackageManager().getApplicationLabel(
	                        res.activityInfo.applicationInfo);
	                co.icon = getPackageManager().getApplicationIcon(
	                        res.activityInfo.applicationInfo);
	                co.appIntent = new Intent(intent);

	                co.appIntent.setComponent(new ComponentName(
	                		res.activityInfo.packageName,
	                		res.activityInfo.name));
	                
	                cropOptions.add(co);
	            }

	            CropOptionAdapter adapter = new CropOptionAdapter(
	                    getApplicationContext(), cropOptions);

	            AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            builder.setTitle("Choose application for cropping");
	            builder.setAdapter(adapter,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int item) {
	                            startActivityForResult(
	                                    cropOptions.get(item).appIntent,
	                                    ENTRY_IMAGE_CROP_IMAGE);
	                        }
	                    });

	            AlertDialog alert = builder.create();
	            alert.show();
	        }
	    }
	}
	
	private class CropOption {
	    public CharSequence title;
	    public Drawable icon;
	    public Intent appIntent;
	}
	
	private class CropOptionAdapter extends ArrayAdapter<CropOption> {
		private ArrayList<CropOption> mOptions;
		private LayoutInflater mInflater;
		
		public CropOptionAdapter(Context context, ArrayList<CropOption> options) {
			super(context, R.layout.crop_selector, options);		
			mOptions 	= options;
			mInflater	= LayoutInflater.from(context);
		}
		
		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.crop_selector, null);
			
			CropOption item = mOptions.get(position);
			
			if (item != null) {
				((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
				((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);
				
				return convertView;
			}			
			return null;
		}
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
	
	private class LoadEntryTask extends AsyncTask<String, Void, Entry> {
		private boolean isConnected;
		
		protected void onPreExecute() {
			_progDialog_Entry = new ProgressDialog(EntryActivity.this);
			_progDialog_Entry.setTitle(R.string.load_entry);
			_progDialog_Entry.setMessage("Loading entry information..");
			_progDialog_Entry.setCancelable(false);
			_progDialog_Entry.setCanceledOnTouchOutside(false);
			_progDialog_Entry.show();
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// Write things to do for checking existing entry
		// params[0]: userID, params[1]: productID
		protected Entry doInBackground(String... params) {
			if(isConnected)
			{
				// Go query Entry db using userID & productID
				Entry entry = HttpURLConnHelper.getHttpHelper().getEntry(params);
				return entry;
			}
			
			// Not connected, return a new entry by default
			return new Entry(Integer.valueOf(params[0]), params[1]);
		}

		protected void onPostExecute(Entry result) {
			_entry = result;
			
			if(_entry.getEntryID() == 0)
			{	
				if(HttpURLConnHelper.getErrorMessage().equals(""))
				{
					// New Entry
					String toastText = "New Entry";
					Toast.makeText(getApplicationContext(),
							toastText, Toast.LENGTH_SHORT).show();
				}
				else
				{
					// Error getting information from db
					String toastText = HttpURLConnHelper.getErrorMessage();
					Toast.makeText(getApplicationContext(),
							toastText, Toast.LENGTH_SHORT).show();
				}
				
				_progDialog_Entry.dismiss();
				displayEntry();
			}
			else
			{
				// Existing entry
				// Calls async DownloadImageTask to download product image
				new DownloadImageTask().execute(_entry.getProductImageUrl());
			}
		}
	}
	
	private class UploadEntryTask extends AsyncTask<Entry, Void, Integer> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			// Show an updating screen
			hideSoftKeyboard();
			_progDialog_Entry = new ProgressDialog(EntryActivity.this);
			_progDialog_Entry.setTitle(R.string.update_entry);
			_progDialog_Entry.setMessage("Updating product information..");
			_progDialog_Entry.setCancelable(false);
			_progDialog_Entry.setCanceledOnTouchOutside(false);
			_progDialog_Entry.show();
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// entry[0]: the entry to be uploaded
		protected Integer doInBackground(Entry... entry) {
			if(isConnected)
			{	
				int entryID = HttpURLConnHelper.getHttpHelper().uploadEntry(entry[0]);
				if(entryID != 0)
				{
					_entry.setEntryID(entryID);
				}
				else
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = HttpURLConnHelper.getErrorMessage();
							Toast.makeText(getApplicationContext(), toastText,
									Toast.LENGTH_LONG).show();
						}
					});
				}
				return entryID;
			}
			return 0;
		}

		protected void onPostExecute(Integer entryID) {
			if(entryID != 0)
			{
				new UploadImageTask().execute(_entry);		
			}
			else
			{
				// Failed to upload entry information
				_progDialog_Entry.dismiss();
			}
		}
	}
	
	private class DeleteEntryTask extends AsyncTask<Entry, Void, Boolean> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			// Show an deleting in progress screen
			_progDialog_Entry = new ProgressDialog(EntryActivity.this);
			_progDialog_Entry.setTitle(R.string.delete_entry);
			_progDialog_Entry.setMessage("Deleting entry..");
			_progDialog_Entry.setCancelable(false);
			_progDialog_Entry.setCanceledOnTouchOutside(false);
			_progDialog_Entry.show();
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// entry[0]: the entry to be deleted
		protected Boolean doInBackground(Entry... entry) {
			if(isConnected)
			{
				int entryID = entry[0].getEntryID();
				String productID = entry[0].getProductID();
				return HttpURLConnHelper.getHttpHelper().deleteEntry(
						entryID, productID);
			}
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if(result)
			{
				String toastText = "Successfully deleted entry";
				Toast.makeText(getApplicationContext(), toastText,
						Toast.LENGTH_SHORT).show();
				
				_reloadJournal = true;
				
				_progDialog_Entry.dismiss();
				
				// Return back to journal view since current entry
				// has been deleted successfully
				NavUtils.navigateUpFromSameTask(EntryActivity.this);
			}
			else
			{
				String toastText = HttpURLConnHelper.getErrorMessage();
				Toast.makeText(getApplicationContext(), toastText,
						Toast.LENGTH_SHORT).show();
				
				_progDialog_Entry.dismiss();
			}
		}
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			_progDialog_Entry.setMessage("Loading product image..");
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// Write things to do for the download process inside here
		// params[0]: url of image to be downloaded
		protected Bitmap doInBackground(String... params) {
			if(isConnected)
			{	
				Bitmap productImage = HttpURLConnHelper.getHttpHelper().getImage(params[0]);
				if(productImage == null)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = HttpURLConnHelper.getErrorMessage();
							Toast.makeText(getApplicationContext(), toastText,
									Toast.LENGTH_LONG).show();
						}
					});
				}
				return productImage;
			}
			return null;
		}

		protected void onPostExecute(Bitmap productImage) {
			if(productImage != null)
			{
				_entry.setProductImage(productImage);
				_iv_Entry_productImage.setImageBitmap(productImage);
			}
			
			_progDialog_Entry.dismiss();
			toggleMode(false);
			
			displayEntry();
		}
	}
	
	private class UploadImageTask extends AsyncTask<Entry, Void, Boolean> {
		private boolean isConnected;
		
		protected void onPreExecute()
		{
			_progDialog_Entry.setMessage("Updating product image..");
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(getApplicationContext());
		}
		
		// Write things to do for the upload process inside here
		// entry[0]: the entry to be uploaded
		protected Boolean doInBackground(Entry... entry) {
			if(isConnected)
			{	
				boolean result = HttpURLConnHelper.getHttpHelper().uploadImage(entry[0]);
				if(result == false)
				{
					runOnUiThread(new Runnable() {
						public void run() {
							String toastText = HttpURLConnHelper.getErrorMessage();
							Toast.makeText(getApplicationContext(), toastText,
									Toast.LENGTH_LONG).show();
						}
					});
				}
				return result;
			}
			return false;
		}

		protected void onPostExecute(Boolean result) {
			if(result)
			{
				// Successfully uploaded both entry information
				// & product image
				String toastText = "Entry updated successfully";
				Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
				
				_progDialog_Entry.dismiss();
				toggleMode(false);
				
				_reloadJournal = true;
			}
			else
			{
				// Failed to upload product image
				_progDialog_Entry.dismiss();
			}
		}
	}
}