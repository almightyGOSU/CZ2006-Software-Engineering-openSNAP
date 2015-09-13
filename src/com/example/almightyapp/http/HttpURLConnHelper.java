package com.example.almightyapp.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.almightyapp.classes.Entry;
import com.example.almightyapp.classes.Product;
import com.example.almightyapp.classes.User;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class HttpURLConnHelper {
	
	private static HttpURLConnHelper _httpURLConnHelper = null;	
	private static String _errorMessage = "";
	
	private HttpURLConnHelper() {}
	
	public static HttpURLConnHelper getHttpHelper()
	{
		if(_httpURLConnHelper == null)
		{
			_httpURLConnHelper = new HttpURLConnHelper();
		}
		
		return _httpURLConnHelper;
	}
	
	public boolean checkNetworkConn(Context context)
	{
		ConnectivityManager connMgr = (ConnectivityManager) 
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		if((networkInfo == null) || (networkInfo.isConnected() == false))
			Toast.makeText(context, "Please check your network settings", Toast.LENGTH_LONG).show();
		
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	public static String getErrorMessage()
	{
		return _errorMessage;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// params[0]: email, params[1]: password
	public User getUser(String... params) {
		_errorMessage = "";
		InputStream inputStream = null;
		
		try {
			URL url = new URL("http://php54-opensnap.rhcloud.com/checklogin/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("email", params[0]));
			paramsList.add(new BasicNameValuePair("password", params[1]));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(paramsList));
			writer.flush();
			writer.close();
			os.close();

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK
					|| response == HttpURLConnection.HTTP_CREATED
					|| response == HttpURLConnection.HTTP_ACCEPTED)
			{
				inputStream = conn.getInputStream();
				String queryResult = convertStreamToString(inputStream);
				
				JSONObject jsonObject = new JSONObject(queryResult);
				int userID = Integer.valueOf(jsonObject.getString("user_id"));
				String userName = jsonObject.getString("name");
				char userGender = (jsonObject.getInt("gender") == 0) ? 'M' : 'F';
				int userAge = Integer.valueOf(jsonObject.getInt("age"));
				
				return new User(userID, params[0], params[1], userName, userAge, userGender);
			}
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);
				JSONObject jsonObject = new JSONObject(queryResult);

				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");			
					return null;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(SocketTimeoutException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
		}
		
		_errorMessage = "Login failed, please try again";
		return null;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// params[0]: email, params[1]: password1, params[2]: password2
	// params[3]: name, params[4]: age, params[5]: gender
	public Boolean createNewUser(String... params) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {
			URL url = new URL("http://php54-opensnap.rhcloud.com/users/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("email", params[0]));
			paramsList.add(new BasicNameValuePair("password", params[1]));
			paramsList.add(new BasicNameValuePair("name", params[3]));
			paramsList.add(new BasicNameValuePair("age", params[4]));
			String gender = (params[5].charAt(0) == 'M') ? "0" : "1";
			paramsList.add(new BasicNameValuePair("gender", gender));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(paramsList));
			writer.flush();
			writer.close();
			os.close();

			// Starts the query
			conn.connect();
			
			int response = conn.getResponseCode();
			
			if(response == HttpURLConnection.HTTP_OK ||
					response == HttpURLConnection.HTTP_CREATED ||
					response == HttpURLConnection.HTTP_ACCEPTED)
				return true;
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);
				
				JSONObject jsonObject = new JSONObject(queryResult);
				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");
					return false;
				}
			}	
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		_errorMessage = "Registration failed, please try again";
		return false;
	}
	
	// params[0]: email, params[1]: oldPassword, params[2]: newPassword,
	// params[3]: name, params[4]: age, params[5]: gender,
	// params[6]: userID
	public Boolean updateUserProfile(String... params) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {
			String baseUrl = "http://php54-opensnap.rhcloud.com/users/";
			baseUrl += params[6];
			URL url = new URL(baseUrl);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("email", params[0]));
			paramsList.add(new BasicNameValuePair("old_password", params[1]));
			paramsList.add(new BasicNameValuePair("password", params[2]));
			paramsList.add(new BasicNameValuePair("name", params[3]));
			paramsList.add(new BasicNameValuePair("age", params[4]));
			String gender = (params[5].charAt(0) == 'M') ? "0" : "1";
			paramsList.add(new BasicNameValuePair("gender", gender));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					os, "UTF-8"));
			writer.write(getQuery(paramsList));
			writer.flush();
			writer.close();
			os.close();

			// Starts the query
			conn.connect();

			int response = conn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK
					|| response == HttpURLConnection.HTTP_CREATED
					|| response == HttpURLConnection.HTTP_ACCEPTED)
				return true;
			else {
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);

				JSONObject jsonObject = new JSONObject(queryResult);
				if (jsonObject.has("error")) {
					_errorMessage = jsonObject.getString("error");
					return false;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		_errorMessage = "Failed to update user profile\nPlease try again";
		return false;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// params[0]: userID, params[1]: productID
	public Entry getEntry(String... params) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("user_id", params[0]));
			paramsList.add(new BasicNameValuePair("product_id", params[1]));
			
			String baseEntryUrl = "http://php54-opensnap.rhcloud.com/entries/";
			String fullUrl = baseEntryUrl + "?" + getQuery(paramsList);
			URL url = new URL(fullUrl);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK
					|| response == HttpURLConnection.HTTP_CREATED
					|| response == HttpURLConnection.HTTP_ACCEPTED)
			{
				inputStream = conn.getInputStream();
				String queryResult = convertStreamToString(inputStream);

				JSONObject jsonObject = new JSONObject(queryResult);
				
				/*{"entry_id":"50","user_id":"1","product_id":"0","timestamp":"2014-09-29 05:41:00",
				 * "image":null,"entry_name":"","rating_ease":"0","rating_safety":"0",
				 * "rating_reseal":"0","rating_overall":"0","comment":""}
				 */
				
				/*public Entry(int userID, int entryID, String productID,
					String productName, String productImageUrl,
					long timestamp, int overallRating, int easeRating,
					int safetyRating, int resealRating, String comment)*/
				
				if(jsonObject.has("entry_id"))
				{
					if(jsonObject.getString("entry_id").equals(""))
					{
						return new Entry(Integer.valueOf(params[0]), params[1]);
					}
				}
				
				int userID = jsonObject.getInt("user_id");
				int entryID = jsonObject.getInt("entry_id");
				
				String productID = String.valueOf(jsonObject.getLong("product_id"));
				String productName = jsonObject.getString("entry_name");
				
				String productManufacturer, productPackagingType;
				productManufacturer = jsonObject.getString("manufacturer");
				productPackagingType = jsonObject.getString("packaging_type");
				
				String productImageUrl = jsonObject.getString("image");			
				String timestamp = jsonObject.getString("timestamp");
				
				double overallRating = jsonObject.getDouble("rating_overall");
				int easeRating = jsonObject.getInt("rating_ease");
				int safetyRating = jsonObject.getInt("rating_safety");
				int resealRating = jsonObject.getInt("rating_reseal");
				String comment = jsonObject.getString("comment");

				return new Entry(userID, entryID, productID, productName,
						productManufacturer, productPackagingType,
						productImageUrl, timestamp, overallRating, easeRating,
						safetyRating, resealRating, comment);
			}
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);
				JSONObject jsonObject = new JSONObject(queryResult);

				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");		
					return new Entry(Integer.valueOf(params[0]), params[1]);
				}
			}
		} catch (MalformedURLException e) {
			_errorMessage = "MalformedURLException";
			e.printStackTrace();
		} catch(SocketTimeoutException e) {
			_errorMessage = "SocketTimeoutException";
			e.printStackTrace();
		} catch (ProtocolException e) {
			_errorMessage = "ProtocolException";
			e.printStackTrace();
		} catch (IOException e) {
			_errorMessage = "IOException";
			e.printStackTrace();
		} catch (JSONException e) {
			_errorMessage = "JSONException";
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					_errorMessage = "IOException - inputStream.close()";
					e.printStackTrace();
				}
			} 
		}

		//_errorMessage = "Cannot retrieve entry, please try again";
		return new Entry(Integer.valueOf(params[0]), params[1]);
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// Parameter: entry to be uploaded
	// Returns: entryID of uploaded entry
	public int uploadEntry(Entry entry) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {
			URL url = new URL("http://php54-opensnap.rhcloud.com/entries/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			// for new entry: user_id, product_id, entry_name, rating_ease,
			// rating_safety, rating_reseal, rating_overall, comment
			
			// for updating entry: entry_id, entry_name, rating_ease,
			// rating_safety, rating_reseal, rating_overall, comment 

			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			
			if(entry.getEntryID() == 0)
			{
				// New Entry - CREATE
				String userID = String.valueOf(entry.getUserID());
				String productID = entry.getProductID();
				
				paramsList.add(new BasicNameValuePair("user_id", userID));
				paramsList.add(new BasicNameValuePair("product_id", productID));
			}
			else
			{
				// Existing Entry - UPDATE
				String entryID = String.valueOf(entry.getEntryID());
				String productID = entry.getProductID();
				
				paramsList.add(new BasicNameValuePair("entry_id", entryID));
				paramsList.add(new BasicNameValuePair("product_id", productID));
			}
			
			// Include entry name
			String entryName = entry.getProductName();
			paramsList.add(new BasicNameValuePair("entry_name", entryName));
			
			// Include product manufacturer & packaging type
			String productManufacturer, productPackagingType;
			productManufacturer = entry.getProductManufacturer();
			productPackagingType = entry.getProductPackagingType();
			
			paramsList.add(new BasicNameValuePair("manufacturer",
					productManufacturer));
			paramsList.add(new BasicNameValuePair("packaging_type",
					productPackagingType));
			
			// Include the ratings
			String easeRating = String.valueOf(entry.getEaseRating());
			String safetyRating = String.valueOf(entry.getSafetyRating());
			String resealRating = String.valueOf(entry.getResealRating());
			String overallRating = String.valueOf(entry.getOverallRating());
			
			paramsList.add(new BasicNameValuePair("rating_ease", easeRating));
			paramsList.add(new BasicNameValuePair("rating_safety", safetyRating));
			paramsList.add(new BasicNameValuePair("rating_reseal", resealRating));
			paramsList.add(new BasicNameValuePair("rating_overall", overallRating));
			
			// Include product review (comment)
			paramsList.add(new BasicNameValuePair("comment", entry.getComment()));

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(paramsList));
			writer.flush();
			writer.close();
			os.close();

			// Starts the query
			conn.connect();

			int response = conn.getResponseCode();

			if(response == HttpURLConnection.HTTP_OK ||
					response == HttpURLConnection.HTTP_CREATED ||
					response == HttpURLConnection.HTTP_ACCEPTED)
			{
				inputStream = conn.getInputStream();
				String queryResult = convertStreamToString(inputStream);

				JSONObject jsonObject = new JSONObject(queryResult);
				
				int entryID = jsonObject.getInt("entry_id");
				return entryID;
			}
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);

				JSONObject jsonObject = new JSONObject(queryResult);
				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");
					return 0;
				}
			}
		} catch (MalformedURLException e) {
			//_errorMessage = "MalformedURLException";
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			//_errorMessage = "SocketTimeoutException";
			e.printStackTrace();
		} catch (ProtocolException e) {
			//_errorMessage = "ProtocolException";
			e.printStackTrace();
		} catch (IOException e) {
			//_errorMessage = "IOException";
			e.printStackTrace();
		} catch (JSONException e) {
			//_errorMessage = "JSONException";
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					//_errorMessage = "IOException - inputStream.close";
					e.printStackTrace();
				}
			}
		}

		_errorMessage = "Upload failed, please try again";
		return 0;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// Parameter: entryID & productID to be deleted
	// Returns: success/failure of delete operation
	public boolean deleteEntry(int entryID, String productID) {
		_errorMessage = "";

		try {
			String URL = "http://php54-opensnap.rhcloud.com/entries/";
			URL += String.valueOf(entryID) + "/delete/";
			URL url = new URL(URL);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			
			// for deleting entry: product_id
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("product_id", productID));
			
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(paramsList));
			writer.flush();
			writer.close();
			os.close();

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();

			if(response == HttpURLConnection.HTTP_OK ||
					response == HttpURLConnection.HTTP_CREATED ||
					response == HttpURLConnection.HTTP_ACCEPTED)
			{
				return true;
			}
			else
			{
				_errorMessage = "Failed to delete entry, please try again";
				return false;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		_errorMessage = "Failed to delete entry, please try again";
		return false;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	public Bitmap getImage(String imageUrl) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {
			String baseUrl = "http://php54-opensnap.rhcloud.com/";
			String fullUrl = baseUrl + imageUrl;
			URL url = new URL(fullUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK
					|| response == HttpURLConnection.HTTP_CREATED
					|| response == HttpURLConnection.HTTP_ACCEPTED)
			{
				inputStream = conn.getInputStream();
				Bitmap productImage = BitmapFactory.decodeStream(inputStream);
				
				if(productImage == null)
				{
					_errorMessage = "Cannot retrieve product image,";
					_errorMessage += "please try again";
				}
				
		        return productImage;
			}
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);
				JSONObject jsonObject = new JSONObject(queryResult);

				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");		
					return null;
				}
			}
		} catch (MalformedURLException e) {
			_errorMessage = "MalformedURLException";
			e.printStackTrace();
		} catch(SocketTimeoutException e) {
			_errorMessage = "SocketTimeoutException";
			e.printStackTrace();
		} catch (ProtocolException e) {
			_errorMessage = "ProtocolException";
			e.printStackTrace();
		} catch (IOException e) {
			_errorMessage = "IOException";
			e.printStackTrace();
		} catch (JSONException e) {
			_errorMessage = "JSONException";
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					_errorMessage = "IOException - inputStream.close()";
					e.printStackTrace();
				}
			} 
		}

		//_errorMessage = "Cannot retrieve product image, please try again";
		return null;
	}
	
	public boolean uploadImage(Entry entry)
	{
		String delimiter = "--";
	    String boundary =  "GOSU";
	    boundary += String.valueOf(System.currentTimeMillis()) + "GOSU";
	    
	    String entryID = String.valueOf(entry.getEntryID());
		Bitmap productImage = entry.getProductImage();
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		productImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		
		_errorMessage = "";
		
		try {
			URL url = new URL("http://php54-opensnap.rhcloud.com/entries/" +
					entryID + "/image/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			
			conn.connect();
			OutputStream outputStream = conn.getOutputStream();
			
			outputStream.write( (delimiter + boundary + "\r\n").getBytes());
			outputStream.write( ("Content-Disposition: form-data; name=\"" +
					"image_file" +  "\"; filename=\"" + (entryID + ".jpg") +
					"\"\r\n").getBytes());
			outputStream.write( ("Content-Type: application/octet-stream\r\n").getBytes());
			outputStream.write( ("Content-Transfer-Encoding: binary\r\n").getBytes());
			outputStream.write("\r\n".getBytes());
	   
			outputStream.write(byteArray);		
			outputStream.write("\r\n".getBytes());
			
			outputStream.write( (delimiter + boundary + delimiter + "\r\n").getBytes());
			
			int response = conn.getResponseCode();		
			outputStream.close();
			
			if(response == HttpURLConnection.HTTP_OK ||
					response == HttpURLConnection.HTTP_CREATED ||
					response == HttpURLConnection.HTTP_ACCEPTED)
			{
				return true;
			}
			else
			{
				InputStream inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);

				JSONObject jsonObject = new JSONObject(queryResult);
				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");
					return false;
				}
				
				inputStream.close();
			}
			
		} catch (MalformedURLException e) {
			//_errorMessage = "MalformedURLException";
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			//_errorMessage = "SocketTimeoutException";
			e.printStackTrace();
		} catch (ProtocolException e) {
			//_errorMessage = "ProtocolException";
			e.printStackTrace();
		} catch (IOException e) {
			//_errorMessage = "IOException";
			e.printStackTrace();
		} catch(JSONException e) {
			//_errorMessage = "JSONException";
			e.printStackTrace();
		}
		
		_errorMessage = "Upload failed, please try again";
		return false;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// params[0]: userID
	public List<Entry> getJournal(String... params) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {
			String baseEntryUrl = "http://php54-opensnap.rhcloud.com/users/";
			String fullUrl = baseEntryUrl + params[0] + "/entries/";
			URL url = new URL(fullUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK
					|| response == HttpURLConnection.HTTP_CREATED
					|| response == HttpURLConnection.HTTP_ACCEPTED)
			{
				inputStream = conn.getInputStream();
				String queryResult = convertStreamToString(inputStream);
				queryResult = queryResult.trim();
				
				// Empty Journal
				if(queryResult.equals("\"\""))
					return null;

				JSONArray jsonArray = new JSONArray(queryResult);
				List<Entry> entries = new ArrayList<Entry>();

				/*{"entry_id":"2","user_id":"2","product_id":"8801062636358",
				 * "timestamp":"2014-10-02 03:52:16","image":"upload\/2_1412236336.jpg",
				 * "name":"Random Alvin","manufacturer":"Kraft","packaging_type":"Bag",
				 * "rating_ease":"3","rating_safety":"4","rating_reseal":"2",
				 * "rating_overall":"3","comment":"Alvin very bad!!!"}*/
				
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					
					int entryID = jsonObject.getInt("entry_id");
					int userID = jsonObject.getInt("user_id");
					
					String productID = String.valueOf(jsonObject.getLong("product_id"));
					String timestamp = jsonObject.getString("timestamp");
					String productImageUrl = jsonObject.getString("image");			
					
					String productName = jsonObject.getString("name");
					String productManufacturer, productPackagingType;
					productManufacturer = jsonObject.getString("manufacturer");
					productPackagingType = jsonObject.getString("packaging_type");
					
					double overallRating = jsonObject.getDouble("rating_overall");
					int easeRating = jsonObject.getInt("rating_ease");
					int safetyRating = jsonObject.getInt("rating_safety");
					int resealRating = jsonObject.getInt("rating_reseal");
					String comment = jsonObject.getString("comment");
					
					entries.add(new Entry(userID, entryID, productID, productName,
						productManufacturer, productPackagingType,
						productImageUrl, timestamp, overallRating, easeRating,
						safetyRating, resealRating, comment));
				}
				
				return entries;
			}
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);
				JSONObject jsonObject = new JSONObject(queryResult);

				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");		
					return null;
				}
			}
		} catch (MalformedURLException e) {
			_errorMessage = "MalformedURLException";
			e.printStackTrace();
		} catch(SocketTimeoutException e) {
			_errorMessage = "SocketTimeoutException";
			e.printStackTrace();
		} catch (ProtocolException e) {
			_errorMessage = "ProtocolException";
			e.printStackTrace();
		} catch (IOException e) {
			_errorMessage = "IOException";
			e.printStackTrace();
		} catch (JSONException e) {
			_errorMessage = "JSONException";
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					_errorMessage = "IOException - inputStream.close()";
					e.printStackTrace();
				}
			} 
		}

		//_errorMessage = "Cannot retrieve journal, please try again";
		return null;
	}
	
	// Can take very long, ONLY use within an AsyncTask
	// params[0]: productManufacturer,
	// params[1]: productPackagingType
	public List<Product> browseProducts(String... params) {
		_errorMessage = "";
		InputStream inputStream = null;

		try {		
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("manufacturer", params[0]));
			paramsList.add(new BasicNameValuePair("packaging_type", params[1]));
			
			String baseBrowseUrl = "http://php54-opensnap.rhcloud.com/browse/";
			String fullUrl = baseBrowseUrl + "?" + getQuery(paramsList);
			URL url = new URL(fullUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK
					|| response == HttpURLConnection.HTTP_CREATED
					|| response == HttpURLConnection.HTTP_ACCEPTED)
			{
				inputStream = conn.getInputStream();
				String queryResult = convertStreamToString(inputStream);
				queryResult = queryResult.trim();

				// No product with given manufacturer & packaging type
				if(queryResult.equals("\"\""))
					return null;

				JSONArray jsonArray = new JSONArray(queryResult);
				List<Product> products = new ArrayList<Product>();

				/*{"product_id":"881802267699","name":"peppero",
				 * "manufacturer":"Kraft","packaging_type":"Bag",
				 * "image":"upload\/19_1412862982.jpg","no_of_raters":"1",
				 * "avg_rating":"3.3333333333333335"}*/

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);

					String productID = String.valueOf(jsonObject.getLong("product_id"));
					String productName = jsonObject.getString("name");
					
					String productManufacturer, productPackagingType;
					productManufacturer = jsonObject.getString("manufacturer");
					productPackagingType = jsonObject.getString("packaging_type");
					
					String productImageUrl = jsonObject.getString("image");

					int numOfRaters = jsonObject.getInt("no_of_raters");
					double avgRating = jsonObject.getDouble("avg_rating");

					products.add(new Product(productID, productName,
							productManufacturer, productPackagingType,
							productImageUrl, numOfRaters, avgRating));
				}
				
				// Sorts the products by average rating (in descending order)
				Collections.sort(products);				
				return products;
			}
			else
			{
				inputStream = conn.getErrorStream();
				String queryResult = convertStreamToString(inputStream);
				JSONObject jsonObject = new JSONObject(queryResult);

				if (jsonObject.has("error"))
				{
					_errorMessage = jsonObject.getString("error");		
					return null;
				}
			}
		} catch (MalformedURLException e) {
			_errorMessage = "MalformedURLException";
			e.printStackTrace();
		} catch(SocketTimeoutException e) {
			_errorMessage = "SocketTimeoutException";
			e.printStackTrace();
		} catch (ProtocolException e) {
			_errorMessage = "ProtocolException";
			e.printStackTrace();
		} catch (IOException e) {
			_errorMessage = "IOException";
			e.printStackTrace();
		} catch (JSONException e) {
			_errorMessage = "JSONException";
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					_errorMessage = "IOException - inputStream.close()";
					e.printStackTrace();
				}
			} 
		}

		//_errorMessage = "Cannot retrieve products, please try again";
		return null;
	}
	
	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
	
	private String convertStreamToString(InputStream inputStream) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append(line);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	        	inputStream.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}
}