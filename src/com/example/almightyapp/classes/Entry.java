package com.example.almightyapp.classes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Entry implements Parcelable {

	private int _userID;
	private int _entryID;
	
	private String _productID;
	private String _productName;
	
	private String _productManufacturer;
	private String _productPackagingType;
	
	private String _timestamp;
	
	private String _productImageUrl;
	private Bitmap _productImage;
	
	private double _overallRating;
	private int _easeRating;
	private int _safetyRating;
	private int _resealRating;
	private String _comment;
	
	/**
	 * Constructor for a new Entry object
	 *
	 * @param userID the userID of the user creating this Entry
	 * @param productID the productID of the product being reviewed
	 */
	public Entry(int userID, String productID) {
		
		_entryID = 0;
		_userID = userID;
		
		_productID = productID;
		_productName = "";
		
		_productManufacturer = "";
		_productPackagingType = "";
		
		_timestamp = "";
		
		_productImageUrl = "";
		_productImage = null;
		
		_easeRating = 1;
		_safetyRating = 1;
		_resealRating = 1;
		_comment = "";
		
		calculateOverallRating();
	}
	
	/**
	 * Constructor for an existing Entry <br>
	 * Used to create an Entry object based on results from a database query
	 * 
	 * @param userID the userID of the user who created this entry
	 * @param entryID the entryID of this entry
	 * @param productID the productID of the product for this entry
	 * @param productName the name of the product
	 * @param productManufacturer the manufacturer of the product
	 * @param productPackagingType the packaging type of the product
	 * @param productImageUrl the image url for this product image
	 * @param timestamp the timestamp for this entry
	 * @param overallRating overall rating for this entry
	 * @param easeRating ease rating for this entry
	 * @param safetyRating safety rating for this entry
	 * @param resealRating reseal rating for this entry
	 * @param comment the packaging review for this entry
	 */
	public Entry(int userID, int entryID, String productID,
			String productName, String productManufacturer,
			String productPackagingType, String productImageUrl,
			String timestamp, double overallRating, int easeRating,
			int safetyRating, int resealRating, String comment) {
		
		_userID = userID;
		_entryID = entryID;
		
		_productID = productID;
		_productName = productName;
		
		_productManufacturer = productManufacturer;
		_productPackagingType = productPackagingType;
		
		_productImageUrl = productImageUrl;
		_productImage = null;
		
		_timestamp = timestamp;
		
		_overallRating = overallRating;
		_easeRating = easeRating;
		_safetyRating = safetyRating;
		_resealRating = resealRating;
		_comment = comment;
	}
	
	/**
	 * Copy constructor for an existing Entry object <br>
	 * Used to clone an existing Entry
	 *
	 * @param originalEntry the entry to be cloned
	 */
	public Entry(Entry originalEntry) {
		
		_userID = originalEntry.getUserID();
		_entryID = originalEntry.getEntryID();
		
		_productID = originalEntry.getProductID();
		_productName = originalEntry.getProductName();
		
		_productManufacturer = originalEntry.getProductManufacturer();
		_productPackagingType = originalEntry.getProductPackagingType();
		
		_productImageUrl = originalEntry.getProductImageUrl();
		_productImage = originalEntry.getProductImage();
		
		_timestamp = originalEntry.getTimestamp();
		
		_overallRating = originalEntry.getOverallRating();
		_easeRating = originalEntry.getEaseRating();
		_safetyRating = originalEntry.getSafetyRating();
		_resealRating = originalEntry.getResealRating();
		_comment = originalEntry.getComment();
	}
	
	/**
	 * Method used to calculate overall rating for food product packaging<br>
	 * based on 3 other ratings, easeRating, safetyRating & resealRating
	 */
	public void calculateOverallRating() {
		_overallRating = (_easeRating + _safetyRating + _resealRating);
		_overallRating /= 3.0;
	}
	
	/**
	 * @return the userID of the user who created this entry
	 */
	public int getUserID() {
		return _userID;
	}
	
	/**
	 * @return the entryID for this entry
	 */
	public int getEntryID() {
		return _entryID;
	}
	
	/**
	 * Changes the entryID to the new value given
	 * 
	 * @param newEntryID the new entryID for this entry
	 */
	public void setEntryID(int newEntryID) {
		_entryID = newEntryID;
	}
	
	/**
	 * Gets the productID of the product for this entry
	 * 
	 * @return the productID of the product for this entry
	 */
	public String getProductID() {
		return _productID;
	}
	
	/**
	 * 
	 * @return the product name of the product for this entry
	 */
	public String getProductName() {
		return _productName;
	}
	
	public void setProductName(String newProductName) {
		_productName = newProductName;
	}
	
	public String getProductManufacturer() {
		return _productManufacturer;
	}
	
	public void setProductManufacturer(String newProductManufacturer) {
		_productManufacturer = newProductManufacturer;
	}
	
	public String getProductPackagingType() {
		return _productPackagingType;
	}
	
	public void setProductPackagingType(String newProductPackagingType) {
		_productPackagingType = newProductPackagingType;
	}
	
	public String getTimestamp() {
		return _timestamp;
	}
	
	public String getProductImageUrl() {
		return _productImageUrl;
	}
	
	public void setProductImageUrl(String newProductImageUrl) {
		_productImageUrl = newProductImageUrl;
	}
	
	public Bitmap getProductImage() {
		return _productImage;
	}
	
	public void setProductImage(Bitmap newProductImage) {
		_productImage = newProductImage;
	}
	
	public double getOverallRating() {
		return _overallRating;
	}
	
	public int getEaseRating() {
		return _easeRating;
	}
	
	public void setEaseRating(int newEaseRating) {
		_easeRating = newEaseRating;
		calculateOverallRating();
	}
	
	public int getSafetyRating() {
		return _safetyRating;
	}
	
	public void setSafetyRating(int newSafetyRating) {
		_safetyRating = newSafetyRating;
		calculateOverallRating();
	}
	
	public int getResealRating() {
		return _resealRating;
	}
	
	public void setResealRating(int newResealRating) {
		_resealRating = newResealRating;
		calculateOverallRating();
	}
	
	public String getComment() {
		return _comment;
	}
	
	public void setComment(String newComment) {
		_comment = newComment;
	}
	
	/* everything below here is for implementing Parcelable */

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_userID);
		dest.writeInt(_entryID);
		
		dest.writeString(_productID);
		dest.writeString(_productName);
		
		dest.writeString(_productManufacturer);
		dest.writeString(_productPackagingType);
		
		dest.writeString(_timestamp);
		dest.writeString(_productImageUrl);
		
		dest.writeDouble(_overallRating);
		dest.writeInt(_easeRating);
		dest.writeInt(_safetyRating);
		dest.writeInt(_resealRating);
		dest.writeString(_comment);		
	}
	
	public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
		public Entry createFromParcel(Parcel in) {
			return new Entry(in);
		}

		public Entry[] newArray(int size) {
			return new Entry[size];
		}
	};

	private Entry(Parcel in) {
		_userID = in.readInt();
		_entryID = in.readInt();
		
		_productID = in.readString();
		_productName = in.readString();
		
		_productManufacturer = in.readString();
		_productPackagingType = in.readString();
		
		_timestamp = in.readString();
		_productImageUrl = in.readString();

		_overallRating = in.readDouble();
		_easeRating = in.readInt();
		_safetyRating = in.readInt();
		_resealRating = in.readInt();
		_comment = in.readString();
	}
}