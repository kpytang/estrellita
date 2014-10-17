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

package edu.uci.ics.star.estrellita;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.exception.IncorrectLoginException;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.sharedprefs.api.UserPreferences;
import edu.uci.ics.star.estrellita.utils.Utilities;
import edu.uci.ics.star.estrellita.utils.WebUtils;

public class LoginActivity extends Activity {
	private LoginTask mTask;
	private static final int DIALOG_FIRST_RUN = 1;
	private static final int DIALOG_LOGIN_ERROR = 2;
	private static final int DIALOG_NETWORK_ERROR = 3;
	private static final int DIALOG_LOGIN_PROGRESS = 4;
	private EditText mUsernameEdit, mPasswordEdit;
	private Button mLoginCancelButton, mLoginOkButton;
	private Database db;
	private double dbKey = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mLoginOkButton = (Button) findViewById(R.id.login_ok_button);
		mLoginCancelButton = (Button) findViewById(R.id.login_cancel_button);

		mUsernameEdit = (EditText) findViewById(R.id.login_username); 
		mPasswordEdit = (EditText) findViewById(R.id.login_password);
		if(!WebUtils.isOnline(this)){
			Toast.makeText(this, "Cannot proceed without Internet",
					Toast.LENGTH_LONG).show();
		}
		mLoginOkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doLogin();
			}
		});		
		mLoginCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mTask != null) {
			mTask.setActivity(null);
			return mTask;
		}
		return null;
	}

	private void doLogin() {
		String username = mUsernameEdit.getText().toString().trim().toLowerCase();
		String password = mPasswordEdit.getText().toString().trim().toLowerCase();

		mTask = new LoginTask(LoginActivity.this);
		mTask.execute(username, password);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = super.onCreateDialog(id);
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_FIRST_RUN:
			dialogBuilder.setTitle("Welcome")
			.setMessage("Disclaimer/Agreement stuff goes here.")
			.setCancelable(false)
			.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					LoginActivity.this.finish();
				}
			});
			dialog = dialogBuilder.create();
			break;

		case DIALOG_LOGIN_ERROR:
			dialogBuilder.setTitle("Error")
			.setMessage("Unable to authenticate. Please check username and re-enter password.")
			.setCancelable(true)
			.setPositiveButton("OK", null);
			dialog = dialogBuilder.create();        	
			break;

		case DIALOG_NETWORK_ERROR:
			dialogBuilder.setTitle("Error")
			.setMessage("Unable to communicate with server. Please try again later.")
			.setCancelable(true)
			.setPositiveButton("OK", null);
			dialog = dialogBuilder.create();
			break;

		case DIALOG_LOGIN_PROGRESS:
			ProgressDialog pDialog = new ProgressDialog(this);
			pDialog.setMessage("Authenticating with " + getString(R.string.app_name) + " servers...");
			pDialog.setCancelable(false);
			dialog = pDialog;
			break;
		}

		return dialog;
	}

	/**
	 * @param username
	 * @param password
	 * @throws NoUserException 
	 * @throws IncorrectLoginException 
	 */
	private User login(String username, String password) throws NoUserException, IncorrectLoginException {
		JSONObject loginResponse;
		try {
			loginResponse = WebUtils.login(username,password);
		} catch (Exception e1) {
			loginResponse = null;
			Utilities.writeToWeeklyErrorLogFile(LoginActivity.this, e1);
			e1.printStackTrace();
		}

		// if we don't get anything back from the server, then the user doesn't exist 
		if(loginResponse == null){
			throw new IncorrectLoginException();
		} 
		else {
			JSONArray ja;
			try {
				// get array of baby_ids
				ja = loginResponse.getJSONArray("kids");
				if(db == null){
					db = new Database(this);
					//					OnBootReceiver.startAlarm(this);
				}
				dbKey = db.open(dbKey);
				User u = db.getUserTable().getUser(loginResponse.getInt("id"), this);
				u.getKids().clear();
				for (int i = 0; i < ja.length(); i++) {
					try{
						dbKey = db.open(dbKey);

						u.addKid(db.getBabyTable().getBaby(ja.getInt(i), this));
					} catch(NoUserException nue){
						throw new NoUserException();
					}
				}
				db.close(dbKey);
				return u;
			} catch (JSONException e) {
				throw new IncorrectLoginException();
			}
		}
	}

	private class LoginTask extends AsyncTask<String, Void, Boolean>{

		private LoginActivity mActivity;
		private boolean mIsDone = false;
		private String mUsername;
		private String mPassword;
		private Boolean user = null;

		private LoginTask(LoginActivity activity) {
			this.mActivity = activity;
		}

		public void setActivity(LoginActivity activity) {
			this.mActivity = activity;
			if (mIsDone) {
				notifyTaskDone();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mActivity.showDialog(DIALOG_LOGIN_PROGRESS);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			mUsername = params[0];
			mPassword = params[1];

			try {
				// this does the login & also stores the login info (if successful)
				UserPreferences.storeLastUser(LoginActivity.this, login(mUsername, mPassword));
				return true;
			} catch (IncorrectLoginException e) {
				e.printStackTrace();
			} catch (NoUserException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean user) {
			super.onPostExecute(user);
			this.user = user;
			mIsDone = true;
			notifyTaskDone();			
		}

		private void notifyTaskDone() {
			if (mActivity != null) {
				try {
					dismissDialog(DIALOG_LOGIN_PROGRESS);
				} catch (IllegalArgumentException e) {
//					Utilities.println(e);
				}
			}
			if(user){
				//we've already saved credentials, so we can just restart the main menu 
				setResult(Activity.RESULT_OK);
				startActivity(new Intent(LoginActivity.this, EstrellitaTiles.class));		

				//close this activity
				finish();
			} 
			else {
				showDialog(DIALOG_LOGIN_ERROR);
			}
		}
	}
}
