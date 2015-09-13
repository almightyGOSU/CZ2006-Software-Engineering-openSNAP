package com.example.almightyapp.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
	
	private int _userID;
	
	private String _email;
	private String _password;
	
	private String _name;
	private int _age;
	
	/**
	 * Character value used to represent gender <br>
	 * <b>M</b> for Male, <b>F</b> for Female, no exceptions
	 */
	private char _gender;
	
	/**
	 * Constructor for user trying to log in
	 *
	 * @param email : String
	 * @param password : String
	 * 
	 * @return User object consisting of only email and password
	 */
	public User(String email, String password) {
		
		_email = email;
		_password = password;
	}
	
	/**
	 * Constructor for user that has successfully logged in, 
	 * i.e. existing user
	 * <br> To be used for creating an instance of the existing
	 * user based on the results of a database query
	 *
	 * @param userID  Integer value, unique identifier for each existing user
	 * @param email  String value, unique login email for each existing user
	 * @param password  String value, used to authenticate login process
	 * @param name  String value, name of the user
	 * @param age  Integer value, age of the user
	 * @param gender  Character value, gender of the user
	 * 
	 * @return User object with all the details obtained from database query
	 */
	public User(int userID, String email, String password,
			String name, int age, char gender) {

		_userID = userID;
		
		_email = email;
		_password = password;

		_name = name;
		_age = age;
		_gender = gender;
	}
	
	public int getUserID() {
		return _userID;
	}
	
	public String getEmail() {
		return _email;
	}
	
	public void setEmail(String newEmail) {
		_email = newEmail;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public void setPassword(String newPassword) {
		_password = newPassword;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String newName) {
		_name = newName;
	}
	
	public int getAge() {
		return _age;
	}
	
	public void setAge(int newAge) {
		_age = newAge;
	}
	
	public char getGender() {
		return _gender;
	}
	
	public void setGender(char newGender) {
		_gender = newGender;
	}
	
	/* everything below here is for implementing Parcelable */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(_userID);
		
		dest.writeString(_email);
		dest.writeString(_password);
		
		dest.writeString(_name);
		dest.writeInt(_age);
		dest.writeString(String.valueOf(_gender));
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};

	private User(Parcel in) {
		_userID = in.readInt();
		
		_email = in.readString();
		_password = in.readString();

		_name = in.readString();
		_age = in.readInt();
		_gender = in.readString().charAt(0);
	}
}