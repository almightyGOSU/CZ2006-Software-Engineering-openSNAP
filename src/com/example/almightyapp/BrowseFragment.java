package com.example.almightyapp;

import java.util.ArrayList;
import java.util.List;

import com.example.almightyapp.R;
import com.example.almightyapp.classes.*;
import com.example.almightyapp.http.HttpURLConnHelper;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class BrowseFragment extends ListFragment implements 
		OnItemSelectedListener, OnClickListener {
	
	public static final String PREF_RELOAD_BROWSE = "RELOAD_BROWSE";
	
	private ProgressDialog _progDialog_Browse;
	
	private static boolean _reloadBrowse = true;
	
	private List<Product> _products;
	private ProductsAdapter _productsAdapter;
	
	private String _selectedProductManufacturer, _selectedPackagingType;
	
	private Spinner _spinner_Browse_productManufacturer;
	private Spinner _spinner_Browse_packagingType;
	private ImageView _iv_Browse_reload;

	public BrowseFragment() {
		_reloadBrowse = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_browse,
				container, false);
		
		// Allow access to options menu from fragment
		setHasOptionsMenu(true);
		return rootView;
	}
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        findViews(view);
        
        Context appContext = getActivity().getApplicationContext();
        
        // Product Manufacturer Spinner
        ArrayAdapter<CharSequence> manufacturersAdapter =
        		ArrayAdapter.createFromResource(appContext,
        		R.array.dd_manufacturers_array, R.layout.spinner_layout);
        manufacturersAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        _spinner_Browse_productManufacturer.setAdapter(manufacturersAdapter);
        _spinner_Browse_productManufacturer.setOnItemSelectedListener(this);

        // Product Packaging Type Spinner
        ArrayAdapter<CharSequence> packagingTypeAdapter =
        		ArrayAdapter.createFromResource(appContext,
        		R.array.dd_packaging_type_array, R.layout.spinner_layout);
        packagingTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        _spinner_Browse_packagingType.setAdapter(packagingTypeAdapter);
        _spinner_Browse_packagingType.setOnItemSelectedListener(this);
        
        _iv_Browse_reload.setOnClickListener(this);
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
	    menu.findItem(R.id.journal_new_entry).setVisible(false);
	    menu.findItem(R.id.journal_new_entry).setEnabled(false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Only check when "reloadBrowse" is false
		if(!_reloadBrowse)
		{
			SharedPreferences preferences = getActivity().getPreferences(
					Activity.MODE_PRIVATE);
			_reloadBrowse = preferences.getBoolean(PREF_RELOAD_BROWSE, true);
		}
		
        if(_reloadBrowse)
        {
        	_reloadBrowse = false;
        
        	reloadEntries();
        }
        else
        {
        	if(_productsAdapter != null)
        	{
        		setListAdapter(_productsAdapter);
        	}
        }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Retain journal information here
		SharedPreferences preferences = getActivity().getPreferences(
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
				
		editor.putBoolean(PREF_RELOAD_BROWSE, _reloadBrowse);

		// Commit to storage
		editor.apply();
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch(parent.getId())
		{
		case R.id.spinner_Browse_productManufacturer:
			_selectedProductManufacturer = parent.getSelectedItem().toString();
			break;
			
		case R.id.spinner_Browse_packagingType:
			_selectedPackagingType = parent.getSelectedItem().toString();
			break;
		}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Do nothing in this case
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.iv_Browse_reload:
			reloadEntries();
			break;
		}
	}
	
	private void findViews(View v) {
		// Store reference to the different views within the layout		
		_spinner_Browse_productManufacturer = (Spinner)
				v.findViewById(R.id.spinner_Browse_productManufacturer);
		_spinner_Browse_packagingType = (Spinner)
				v.findViewById(R.id.spinner_Browse_packagingType);
		
		_iv_Browse_reload = (ImageView)
				v.findViewById(R.id.iv_Browse_reload);
	}
	
	private void reloadEntries() {
		_selectedProductManufacturer =
				_spinner_Browse_productManufacturer.getSelectedItem().toString();
		_selectedPackagingType =
				_spinner_Browse_packagingType.getSelectedItem().toString();
		
		new LoadProductsTask().execute(_selectedProductManufacturer,
				_selectedPackagingType);
	}
	
	private class LoadProductsTask extends AsyncTask<String, Void, List<Product>> {
		private boolean isConnected;
		
		protected void onPreExecute() {
			_progDialog_Browse = new ProgressDialog(getActivity());
			_progDialog_Browse.setTitle(R.string.load_products);
			_progDialog_Browse.setMessage("Loading products..");
			_progDialog_Browse.setCancelable(false);
			_progDialog_Browse.setCanceledOnTouchOutside(false);
			_progDialog_Browse.show();
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(
					getActivity().getApplicationContext());
		}
		
		// Write things to do for loading products
		// params[0]: selectedProductManufacturer
		// params[1]: selectedPackagingType
		protected List<Product> doInBackground(String... params) {
			
			if(isConnected)
			{
				// Go query Product db using selectedProductManufacturer &
				// selectedPackagingType
				List<Product> products =
						HttpURLConnHelper.getHttpHelper().browseProducts(params);
				return products;
			}
			
			// Not connected, return null by default
			return null;
		}

		protected void onPostExecute(List<Product> products) {
			
			if(products != null)
			{
				_products = products;		
				_productsAdapter = new ProductsAdapter(
						getActivity().getApplicationContext(), _products);
				setListAdapter(_productsAdapter);
			}
			else
			{
				if(HttpURLConnHelper.getErrorMessage().equals(""))
				{
					String toastText = "No such product found";
					Toast.makeText(getActivity().getApplicationContext(),
							toastText, Toast.LENGTH_SHORT).show();
					
					// No product with selected product manufacturer &
					// packaging type
					_products = new ArrayList<Product>();
					_productsAdapter = new ProductsAdapter(
							getActivity().getApplicationContext(), _products);
					setListAdapter(_productsAdapter);
					
				}
				else
				{
					// Error getting information from db
					String toastText = HttpURLConnHelper.getErrorMessage();
					Toast.makeText(getActivity().getApplicationContext(),
							toastText, Toast.LENGTH_SHORT).show();
				}
			}
			
			_progDialog_Browse.dismiss();
		}
	}
}