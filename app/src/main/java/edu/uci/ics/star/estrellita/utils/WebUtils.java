/*
 * Copyright (C) 2012 Karen P. Tang, Sen Hirano
 * 
 * This file is part of the Estrellita project.
 * 
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of 
 * the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. If not, see
 * 				
 * 				http://www.gnu.org/licenses/
 * 
 */

/**
 * @author Karen P. Tang
 * @author Sen Hirano
 * 
 */

package edu.uci.ics.star.estrellita.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;

public class WebUtils {
	// sirius = 128.195.58.94
	// pulsar = 128.195.58.164
	public static final String ESTRELLITA_SERVER_SUBMIT_URL = "https://128.195.58.94:4443/submit/Karen.aspx";
	private static final String ESTRELLITA_SERVER_RECEIVE_URL = "https://128.195.58.94:4443/receive/Karen.aspx";
	public static final String ACTION_UPLOAD_FINISHED = "edu.uci.ics.star.estrellita.ACTION_UPLOAD_FINISHED";

	/**
	 * check if there is internet connectivity
	 * at some point I might want to check ifBackgroundData is enabled
	 * @param c
	 * @return
	 */
	public static boolean isOnline(Context c) {
		try {
			ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null) {
				return netInfo.isConnectedOrConnecting();
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static void uploadSingleToServer(final ContentValues cv, final IndicatorTable t, final Context context, final Database db){

		Runnable mTask = new Runnable() {
			public void run() {

				double dbKey = -1;
				dbKey = db.open(dbKey);
				uploadSingleToServerNonThreaded(cv, t, context, db);
				db.close(dbKey);
			}
		};
		Thread thr = new Thread(null, mTask, "UploadIndicatorToServer");
		thr.start();
	}

	public static void uploadSingleToServerNonThreaded(final ContentValues cv, final IndicatorTable t, final Context context, final Database db){
		JSONObject json;
		json = tryUpload(cv, t.getTableName(), context);
		try {
			double dbKey = -1;
			dbKey = db.open(dbKey);
			if(json != null){
				cv.put(IndicatorTable.SYNCED, 1);

				cv.put(IndicatorTable.FOR_UPDATE, 0);

				cv.put(IndicatorTable.FOR_DELETE, 0);

				cv.put(IndicatorTable.ID, json.getInt(IndicatorTable.ID));
				if(json.has(IndicatorTable.ANDROID_FILENAME) && 
						json.getString(IndicatorTable.ANDROID_FILENAME) != null && 
						json.getString(IndicatorTable.ANDROID_FILENAME).length() > 0){
					// null the file in the db to save space
					cv.remove(IndicatorTable.FILE);
					cv.putNull(IndicatorTable.FILE);
				} else if(cv.containsKey(IndicatorTable.FILE) && 
						cv.getAsString(IndicatorTable.FILE) != null &&
						cv.getAsString(IndicatorTable.FILE).length() > 0){
					cv.remove(IndicatorTable.FILE);
					cv.putNull(IndicatorTable.FILE);
				}
				t.prepareTable(db.getDb());
				t.update(cv);
			}
			db.close(dbKey);
		} catch (Exception e) {
			Utilities.writeToWeeklyErrorLogFile(context, e);
			e.printStackTrace();
		}
	}
	
	public static void uploadSingleToServerWithBroadcast(final ContentValues cv, final IndicatorTable t, final Context context, final Database db) {

		Runnable mTask = new Runnable() {
			public void run() {
				uploadSingleToServerNonThreaded(cv, t, context, db);
				Intent intent = new Intent(ACTION_UPLOAD_FINISHED);
				context.sendBroadcast(intent);
			}
		};
		Thread thr = new Thread(null, mTask, "UploadIndicatorToServerWithBroadcast");
		thr.start();
		
	}

	/**
	 * @param insert
	 * @param reportName
	 * @param c
	 */
	public static JSONObject tryUpload(ContentValues insert, String reportName, Context c) {
		if(isOnline(c)){
			JSONObject jsonInput = contentValuesToJSON(insert, c);
			
			String filename = null;
			try {
				jsonInput.put("reportname", reportName);
				if(jsonInput.has(IndicatorTable.ANDROID_FILENAME) && 
						jsonInput.getString(IndicatorTable.ANDROID_FILENAME) != null && 
						jsonInput.getString(IndicatorTable.ANDROID_FILENAME).length() > 0){
					filename = jsonInput.getString(IndicatorTable.ANDROID_FILENAME);
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			
			byte[] fileBytes = insert.getAsByteArray("file");
			
			JSONObject jsonOutput;
			try {
				jsonOutput = postToServer(ESTRELLITA_SERVER_SUBMIT_URL,fileBytes, filename, jsonInput);
			}
			catch (Exception e1) {
				jsonOutput = null;
				Utilities.writeToWeeklyErrorLogFile(c, e1);
				e1.printStackTrace();
			}

			if(jsonOutput != null){
				try {
//					Utilities.println("GETTING ID FROM JSON: " + jsonOutput.get("id"));
					return jsonOutput;
				} catch (Exception e) {
					Utilities.writeToWeeklyErrorLogFile(c, e);
//					Utilities.println("JSON ERROR");
				}
			}
		}
		return null;
	}

	public static JSONObject contentValuesToJSON(ContentValues cv, Context c) {
		Set<Entry<String, Object>> valueSet = cv.valueSet();
		String key;
		String value;
		JSONObject jsonInput = new JSONObject();
		for (Entry<String, Object> entry : valueSet) {
			key = entry.getKey();
			if(!key.equalsIgnoreCase("file")){
				value = cv.getAsString(key);
				if(value != null){
					try {
						jsonInput.put(key, URLEncoder.encode(value));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return jsonInput;
	}



	public static JSONObject getUpdatesFromServer(final String tableName,
			int parentPid, int babyId, final int lastIndex, Context c) {

		JSONObject json = new JSONObject();
		try {
			json.put("personId", Integer.toString(parentPid));
			json.put("childId", Integer.toString(babyId));
			json.put("reportname", tableName);
			json.put("lastindex", Integer.toString(lastIndex));
			try {
				return postToServer(ESTRELLITA_SERVER_RECEIVE_URL, json);
			} catch (Exception e1) {
				Utilities.writeToWeeklyErrorLogFile(c, e1);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}


	public static JSONObject login(String username, String password) throws Exception {
		JSONObject json = new JSONObject();
		try {
			json.put("username", username);
			json.put("password", password);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return postToServer(ESTRELLITA_SERVER_RECEIVE_URL, json);
	}

	public static List<String> extractUrls(String input) {
		List<String> result = new ArrayList<String>();

		Pattern pattern = Pattern.compile(
				"(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|"+
				"[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\"+
				"([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))"+
		"*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?]))");

		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			result.add(matcher.group());
		}

		return result;
	}

	public static void openUrlInBrowser(String url, Context context) {
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			if(url.startsWith("Http://") || url.startsWith("Https://")) {
				url = url.replaceFirst("Http", "http");
			} else {
				url = "http://" + url;
			}
		}


		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(browserIntent);
	}

	public static JSONObject getUserFromServer(int id) throws Exception {
		JSONObject json = new JSONObject();
		try {
			json.put("getUser", Integer.toString(id));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return postToServer(ESTRELLITA_SERVER_RECEIVE_URL, json);
	}

	public static JSONObject getBabyFromServer(int id) throws Exception {
		JSONObject json = new JSONObject();
		try {
			json.put("getBaby", Integer.toString(id));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return postToServer(ESTRELLITA_SERVER_RECEIVE_URL, json);
	}

	public static JSONObject getCommentTargetFor(int id) throws Exception {
		JSONObject json = new JSONObject();
		try {
			json.put("comment", Integer.toString(id));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return postToServer(ESTRELLITA_SERVER_RECEIVE_URL, json);
	}


	public static JSONObject postToServer(String url, JSONObject json) throws Exception {
		
		return postToServer(url, null, null, json);
	}
	
	/**
	 * Takes a byte array & a hashtable of paramaters and posts to the given url
	 * @param url
	 * @param fileBytes
	 * @param filename
     * @param json
	 * @return
	 */
	public static JSONObject postToServer(String url, byte[] fileBytes, String filename, JSONObject json) throws Exception {
		HttpMultipartRequest req;
		try {
			Hashtable<String, String> params = new Hashtable<String, String>();
			params.put("json", json.toString());
			if(fileBytes == null){
				req = new HttpMultipartRequest(url,params);
			} 
			else{
				if(filename == null) {
					filename = "pc_android_pic.png";
				}
				req = new HttpMultipartRequest(url, params, "file", 
						filename, "image/png", fileBytes);
			}

			byte[] response = req.send();
			JSONObject jObject = null;

			String jsonString;

			if(response == null){
				jsonString = "NULL, RESPONSE FAILED";
			} 
			else {
				jsonString = new String(response);
				try {
					jsonString = URLDecoder.decode(jsonString);
					jObject = new JSONObject(jsonString);
				} 
				catch(JSONException jse){
				}
			}

			return jObject;
		} catch (Exception e) {
			throw e;
		}	 
	}
}
