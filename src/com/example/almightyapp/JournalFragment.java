package com.example.almightyapp;

import java.util.List;
import com.example.almightyapp.R;
import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import com.example.almightyapp.classes.*;
import com.example.almightyapp.http.HttpURLConnHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

//@SuppressWarnings("unused")
public class JournalFragment extends ListFragment implements OnItemClickListener {
	
	public static final String PREF_RELOAD_JOURNAL = "RELOAD_JOURNAL";
	
	private ProgressDialog _progDialog_Journal;
	
	private static boolean _reloadJournal = true;
	
	private List<Entry> _entries;
	private EntriesAdapter _entriesAdapter;
	
	
	public JournalFragment() {
		_reloadJournal = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_journal,
				container, false);
		
		// Allow access to options menu from fragment
		setHasOptionsMenu(true);
		return rootView;
	}
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Only check when "reloadJournal" is false
		if(!_reloadJournal)
		{
			SharedPreferences preferences = getActivity().getPreferences(
					Activity.MODE_PRIVATE);
			_reloadJournal = preferences.getBoolean(PREF_RELOAD_JOURNAL, true);
		}
		
        if(_reloadJournal)
        {
        	_reloadJournal = false;
        
        	User user = ((MainActivity) getActivity()).getUser();		
        	String userID = String.valueOf(user.getUserID());
			
        	new LoadJournalTask().execute(userID);
        }
        else
        {
        	if(_entriesAdapter != null)
        	{
        		setListAdapter(_entriesAdapter);
        		getListView().setOnItemClickListener(JournalFragment.this);
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
				
		editor.putBoolean(PREF_RELOAD_JOURNAL, _reloadJournal);

		// Commit to storage
		editor.apply();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    
	    menu.findItem(R.id.journal_new_entry).setVisible(true);
	    menu.findItem(R.id.journal_new_entry).setEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here.
		switch(item.getItemId())
		{
		case R.id.journal_new_entry:
			// Start barcode scanner here
			new IntentIntegrator(this).initiateScan();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		// Pass userID & productID to EntryActivity	
		User user = ((MainActivity) getActivity()).getUser();		
		String userID = String.valueOf(user.getUserID());
		String productID = _entriesAdapter.getItem(position).getProductID();
		
		Intent entryIntent = new Intent(getActivity(), EntryActivity.class);
		
		entryIntent.putExtra(EntryActivity.ENTRY_USER_ID, userID);
		entryIntent.putExtra(EntryActivity.ENTRY_PRODUCT_ID, productID);
		
	    startActivity(entryIntent);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Retrieve scan result
		IntentResult scanningResult;
		scanningResult = IntentIntegrator.parseActivityResult(requestCode,
				resultCode, intent);
		
		if (scanningResult != null) {
			String productID = scanningResult.getContents();
			
			if(productID != null)
			{	
				User user = ((MainActivity) getActivity()).getUser();		
				String userID = String.valueOf(user.getUserID());
				
				Intent entryIntent = new Intent(getActivity(), EntryActivity.class);
				
				// Journal has to be reloaded after new entry has been created
				_reloadJournal = true;
				
				entryIntent.putExtra(EntryActivity.ENTRY_USER_ID, userID);
				entryIntent.putExtra(EntryActivity.ENTRY_PRODUCT_ID, productID);
			    startActivity(entryIntent);
			}
			else
			{
				// Displayed only when barcode scanning fails or gets cancelled by user
				String toastText = "Scan product barcode to create a new entry";
				Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			// Displayed only when barcode scanning fails or gets cancelled by user
			String toastText = "Scan product barcode to create a new entry";
			Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void reloadJournal()
	{
		_reloadJournal = true;
	}
	
	private class LoadJournalTask extends AsyncTask<String, Void, List<Entry>> {
		private boolean isConnected;
		
		protected void onPreExecute() {
			_progDialog_Journal = new ProgressDialog(getActivity());
			_progDialog_Journal.setTitle(R.string.load_journal);
			_progDialog_Journal.setMessage("Loading journal entries..");
			_progDialog_Journal.setCancelable(false);
			_progDialog_Journal.setCanceledOnTouchOutside(false);
			_progDialog_Journal.show();
			
			// Check network connectivity
			HttpURLConnHelper httpHelper = HttpURLConnHelper.getHttpHelper();
			isConnected = httpHelper.checkNetworkConn(
					getActivity().getApplicationContext());
		}
		
		// Write things to do for loading journal entries
		// params[0]: userID
		protected List<Entry> doInBackground(String... params) {
			
			if(isConnected)
			{
				// Go query Entry db using userID & productID
				List<Entry> entries = HttpURLConnHelper.getHttpHelper().getJournal(params);
				return entries;
			}
			
			// Not connected, return null by default
			return null;
		}

		protected void onPostExecute(List<Entry> entries) {
			
			if(entries != null)
			{
				_entries = entries;		
				_entriesAdapter = new EntriesAdapter(
						getActivity().getApplicationContext(), _entries);
				setListAdapter(_entriesAdapter);
		        getListView().setOnItemClickListener(JournalFragment.this);
			}
			else
			{
				if(HttpURLConnHelper.getErrorMessage().equals(""))
				{
					String toastText = "No existing journal entries";
					Toast.makeText(getActivity().getApplicationContext(),
							toastText, Toast.LENGTH_SHORT).show();
				}
				else
				{
					// Error getting information from db
					String toastText = HttpURLConnHelper.getErrorMessage();
					Toast.makeText(getActivity().getApplicationContext(),
							toastText, Toast.LENGTH_SHORT).show();
				}
			}
			
			_progDialog_Journal.dismiss();
		}
	}
}