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

public class ProductsAdapter extends BaseAdapter {

    private Context _context;
    private List<Product> _products;
    
    // A list of product images being downloaded
    private List<String> _downloading_productsID;
    
    // Default placeholder product image
    private Drawable _placeholderBitmap;

    public ProductsAdapter(Context context, List<Product> products) {
        _context = context;
        _products = products;
        
        _downloading_productsID = new ArrayList<String>();
        
        // Get placeholder product image
        _placeholderBitmap = context.getResources().getDrawable(
        		R.drawable.journal_def_product_img);
    }

    @Override
    public int getCount() {
        return _products.size();
    }

    @Override
    public Product getItem(int position) {
        return _products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return _products.indexOf(getItem(position));
    }

    @SuppressLint({ "InflateParams", "DefaultLocale" })
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null)
        {
            LayoutInflater mInflater = (LayoutInflater) _context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.browse_list_item, null);
        }

        TextView tv_productName = (TextView) convertView.findViewById(
        		R.id.tv_BrowseItem_productName);
        TextView tv_numOfRaters = (TextView) convertView.findViewById(
        		R.id.tv_BrowseItem_numOfRaters);
        TextView tv_avgRating = (TextView) convertView.findViewById(
        		R.id.tv_BrowseItem_avgRating);       
        ImageView productImage = (ImageView) convertView.findViewById(
        		R.id.iv_BrowseItem_productImage);
        
        // The view is recycled
        if(convertView != null)
        {
        	// Set the image view back to the placeholder image
        	productImage.setImageDrawable(_placeholderBitmap);
        }
        
        Product product = getItem(position);
        
        // Set product name
        tv_productName.setText(product.getProductName());
        
        // Set number of raters & average rating
        String numOfRaters = "No. of Raters: ";
        numOfRaters += String.valueOf(product.getNumOfRaters());
        tv_numOfRaters.setText(numOfRaters);
        
        String avgRating = "Average Rating: ";
        avgRating += new DecimalFormat("#.00").format(
        		product.getAvgRating());
        tv_avgRating.setText(avgRating);
        
        // Download/Set product image
        if(product.getProductImage() == null)
        {
        	// Ensures that request to download product image
        	// is only submitted once
        	if(_downloading_productsID.indexOf(product.getProductID()) == -1)
        	{
        		new DownloadImageTask(product).execute(
        				product.getProductImageUrl());
        		_downloading_productsID.add(product.getProductID());
        	}
        }
        else
        {
        	productImage.setImageBitmap(product.getProductImage());
        }

        return convertView;
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private Product _product;
		
		public DownloadImageTask(Product product)
		{
			super();
			_product = product;
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
				_product.setProductImage(productImage);    	
	        	notifyDataSetChanged();
			}
			else
			{
				// Download failed, remove from list of downloading images
				// Will re-download when this item comes into view again
				_downloading_productsID.remove(_product.getProductID());
			}
		}
	}
}