package com.example.almightyapp.classes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Product class<br>
 * Implements Parcelable to allowing passing of Products between activities<br>
 * Implements Comparable to allow sorting
 * 
 * @author Jin Yao
 */
public class Product implements Parcelable, Comparable<Product> {
	
	private String _productID;
	private String _productName;
	
	private String _productManufacturer;
	private String _productPackagingType;
	
	private String _productImageUrl;
	private Bitmap _productImage;
	
	private int _numOfRaters;
	private double _avgRating;
	
	/**
	 * Constructor for a existing Product<br>
	 * Used to create a Product object based on results from a database query
	 *
	 * @param productID : String
	 * @param productName : String
	 * @param productManufacturer : String
	 * @param productPackagingType : String
	 * @param productImageUrl : String
	 * @param numOfRaters : int
	 * @param avgRating : double 
	 * 
	 * @return Product object with all the details obtained from database query
	 */
	public Product(String productID, String productName,
			String productManufacturer, String productPackagingType,
			String productImageUrl, int numOfRaters, double avgRating) {
		
		_productID = productID;
		_productName = productName;
		
		_productManufacturer = productManufacturer;
		_productPackagingType = productPackagingType;
		
		_productImageUrl = productImageUrl;
		_productImage = null;
		
		_numOfRaters = numOfRaters;
		_avgRating = avgRating;
	}
	
	public String getProductID() {
		return _productID;
	}
	
	public String getProductName() {
		return _productName;
	}
	
	public String getProductManufacturer() {
		return _productManufacturer;
	}
	
	public String getProductPackagingType() {
		return _productPackagingType;
	}
		
	public String getProductImageUrl() {
		return _productImageUrl;
	}
	
	public Bitmap getProductImage() {
		return _productImage;
	}
	
	public void setProductImage(Bitmap newProductImage) {
		_productImage = newProductImage;
	}
	
	public int getNumOfRaters() {
		return _numOfRaters;
	}
	
	public double getAvgRating() {
		return _avgRating;
	}
	
	/**
	 * Method to allow comparison between products<br>
	 * Used to implement sorting of products
	 */
	public int compareTo(Product otherProduct) {
		
		double thisProductAvgRating = getAvgRating();
		double otherProductAvgRating = otherProduct.getAvgRating();
 
		if (thisProductAvgRating > otherProductAvgRating) {
	        return -1;
	    } else if (thisProductAvgRating < otherProductAvgRating) {
	        return 1;
	    }
	    return 0; 
	}
	
	/* everything below here is for implementing Parcelable */

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeString(_productID);
		dest.writeString(_productName);
		
		dest.writeString(_productManufacturer);
		dest.writeString(_productPackagingType);
		
		dest.writeString(_productImageUrl);
		
		dest.writeInt(_numOfRaters);
		dest.writeDouble(_avgRating);		
	}
	
	public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
		public Product createFromParcel(Parcel in) {
			return new Product(in);
		}

		public Product[] newArray(int size) {
			return new Product[size];
		}
	};

	private Product(Parcel in) {
		
		_productID = in.readString();
		_productName = in.readString();
		
		_productManufacturer = in.readString();
		_productPackagingType = in.readString();
		
		_productImageUrl = in.readString();

		_numOfRaters = in.readInt();
		_avgRating = in.readDouble();
	}
}