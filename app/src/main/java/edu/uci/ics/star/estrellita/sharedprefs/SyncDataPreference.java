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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.AttributeSet;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.updateservice.SyncService;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class SyncDataPreference extends Preference {
	
	private UpdateReceiver mReceiver;
	private ProgressDialog mProgressDialog;
	private Database mDatabase;
	private Context mContext;
	private double dbKey = -1;
    
    public SyncDataPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mContext = context;
    }

    /*
     * (non-Javadoc)
     * @see android.preference.Preference#onClick()
     */
    @Override
    protected void onClick() {
        mDatabase = new Database(mContext);
        // don't need to open it
    	SyncService.threadedUpdateTables(mContext, mDatabase, null);
    	mReceiver = new UpdateReceiver();
    	mContext.registerReceiver(mReceiver, new IntentFilter(Utilities.ACTION_UPDATE_MAIN));
    	mProgressDialog = new ProgressDialog(mContext);
    	mProgressDialog.setTitle("Please wait");
    	mProgressDialog.setMessage("syncing Estrellita data...");
    	mProgressDialog.setIndeterminate(true);
    	mProgressDialog.show();
    }    
    
    // this is for catching when the updates are done
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateHandler.sendEmptyMessage(0);
		}
	}
	
	// define the Handler that receives messages from the thread and updates the progress
	final Handler updateHandler = new Handler() {
		public void handleMessage(Message msg) {
			try{
				mProgressDialog.dismiss();
				mContext.unregisterReceiver(mReceiver);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
}
