package com.example.almightyapp.classes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.almightyapp.R;
import com.example.almightyapp.http.HttpURLConnHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntriesAdapter extends BaseAdapter {

    private Context _context;
    private List<Entry> _entries;
    
    // A list of product images being downloaded
    private List<Integer> _downloading_entriesID;
    
    // Default placeholder product image
    private Drawable _placeholderBitmap;

    public EntriesAdapter(Context context, List<Entry> entries) {
        _context = context;
        _entries = entries;
        
        _downloading_entriesID = new ArrayList<Integer>();
        
        // Get placeholder product image
        _placeholderBitmap = context.getResources().getDrawable(
        		R.drawable.journal_def_product_img);
    }

    @Override
    public int getCount() {
        return _entries.size();
    }

    @Override
    public Entry getItem(int position) {
        return _entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return _entries.indexOf(getItem(position));
    }

    @SuppressLint({ "InflateParams", "DefaultLocale" })
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater mInflater = (LayoutInflater) _context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.journal_list_item, null);
        }

        TextView productName = (TextView) convertView.findViewById(
        		R.id.tv_listItem_productName);
        TextView overallRating = (TextView) convertView.findViewById(
        		R.id.tv_listItem_overallRating);           
        ImageView productImage = (ImageView) convertView.findViewById(
        		R.id.iv_listItem_productImage);
        
        // The view is recycled
        if(convertView != null)
        {
        	// Set the image view back to the placeholder image
        	productImage.setImageDrawable(_placeholderBitmap);
        }
        
        Entry entry = getItem(position);
        
        // Set product name & overall rating
        productName.setText(entry.getProductName());
        
        String productRating = "Overall Rating: ";
        productRating += new DecimalFormat("#.00").format(
        		entry.getOverallRating());
        overallRating.setText(productRating);
        
        // Download/Set product image
        if(entry.getProductImage() == null)
        {
        	// Ensures that request to download product image
        	// is only submitted once
        	if(_downloading_entriesID.indexOf(entry.getEntryID()) == -1)
        	{
        		new DownloadImageTask(entry).execute(
        				entry.getProductImageUrl());
        		_downloading_entriesID.add(entry.getEntryID());
        	}
        }
        else
        {
        	productImage.setImageBitmap(entry.getProductImage());
        }

        return convertView;
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private Entry _entry;
		
		public DownloadImageTask(Entry entry)
		{
			super();
			_entry = entry;
		}
		
		protected void onPreExecute()
		{
			
		}
		
		// Write things to do for the download process inside here
		// params[0]: url of image to be downloaded
		protected Bitmap doInBackground(String... params) {
			
			Bitmap productImage = HttpURLConnHelper.getHttpHelper().getImage(params[0]);
			return productImage;
		}

		protected void onPostExecute(Bitmap productImage) {
			if(productImage != null)
			{				
	        	_entry.setProductImage(productImage);        	
	        	notifyDataSetChanged();
			}
			else
			{
				// Download failed, remove from list of downloading images
				// Will re-download when this item comes into view again
				_downloading_entriesID.remove(_entry.getEntryID());
			}
		}
	}
}