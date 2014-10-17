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

package edu.uci.ics.star.estrellita.sharedprefs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.updateservice.SyncService;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class UpdateWidgetPreference extends Preference {
	
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private Database mDatabase;
	private double dbKey = -1;
	
	public UpdateWidgetPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
	}

	/*
	 * (non-Javadoc)
	 * @see android.preference.Preference#onClick()
	 */
	@Override
	protected void onClick() {
		new UpdateTask().execute();
	}
	
	private class UpdateTask extends AsyncTask<Void, Void, Void> {

	    protected void onPreExecute() {
	        mProgressDialog = ProgressDialog.show(getContext(), "Please wait", "Updating widgets and reminders...", true);
	    }

	    protected Void doInBackground(Void... unused) {
			return (null);
	    }

	    protected void onPostExecute(Void unused) {
	    	Utilities.updateWidget(mContext);

			mDatabase = new Database(mContext);
			dbKey = mDatabase.open(dbKey);
			SyncService.checkOccasionallyDoneIndicators(mContext, mDatabase);
			SyncService.performSpecialReminders(mContext, mDatabase);
			
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
	    	
			if (mDatabase != null) {
	    		mDatabase.close(dbKey);
	    	}
	    }
	  }
}
