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

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.Preference;
import android.util.AttributeSet;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.VersionInfo;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class SendDBPreference extends Preference {
	
    public SendDBPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * (non-Javadoc)
     * @see android.preference.Preference#onClick()
     */
    @Override
    protected void onClick() {
    	Database db = null;
		double dbKey = -2;
    	try {
    		db = new Database(getContext());
			dbKey = db.open(dbKey);
    		db.sendDBtoSDCard();
    		
    		String file = "file:///sdcard/estrellita/backupestrellita.db";
    		Intent sendIntent = new Intent(Intent.ACTION_SEND);
    	    sendIntent.setType("jpeg/image");
    	    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {
					getContext().getResources().getString(R.string.baby_uci_email), 
					getContext().getResources().getString(R.string.baby_gmail_email)});
    	    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.estrellita_db_attachment_subject));
    	    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file));
    	    Float megaBytesFree = Utilities.megabytesAvailable(Environment.getExternalStorageDirectory().getPath());
    	    String emailBody = "\n\n\n---------\n"
    	    	+ "Parent ID: " + EstrellitaTiles.getParentId(getContext()) + ", "
    	    	+ "Baby ID: " + EstrellitaTiles.getCurrentBabyId() + ", "
    	    	+ "Version: " + VersionInfo.VERSION + ", "
    	    	+ "sdcard MB free space: " + megaBytesFree.toString();
    	    sendIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
    	    getContext().startActivity(Intent.createChooser(sendIntent, "Email:"));
		}
    	catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    		if (db != null) {
    			db.close(dbKey);
    		}
    	}
    }
}
