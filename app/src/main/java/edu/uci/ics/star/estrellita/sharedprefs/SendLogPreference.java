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

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.preference.Preference;
import android.util.AttributeSet;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.VersionInfo;

public class SendLogPreference extends Preference {

	public SendLogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * (non-Javadoc)
	 * @see android.preference.Preference#onClick()
	 */
	@Override
	protected void onClick() {
		// check to see if there is an alogcat directory
		String path = Environment.getExternalStorageDirectory() + "/alogcat";
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			// look for the most recent alogcat file
			File[] logFiles = file.listFiles();
			long latestDate = Long.MIN_VALUE;
			int latestFileIndex = -1;
			for (int i=0; i<logFiles.length; i++) {
				if (logFiles[i].lastModified() > latestDate) {
					latestDate = logFiles[i].lastModified();
					latestFileIndex = i;
				}
			}
			if (latestFileIndex != -1) {
				String logFile = "file://" + logFiles[latestFileIndex].getPath();
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("jpeg/image");
				sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {
						getContext().getResources().getString(R.string.baby_uci_email), 
						getContext().getResources().getString(R.string.baby_gmail_email)});
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, getContext().getResources().getString(R.string.alogcat_attachment_subject));
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(logFile));
				String emailBody = "\n\n\n---------\n" 
					+ "Parent ID: " + EstrellitaTiles.getParentId(getContext()) + ", "
					+ "Baby ID: " + EstrellitaTiles.getCurrentBabyId() + ", "
	    	    	+ "Version: " + VersionInfo.VERSION;
				sendIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
				getContext().startActivity(Intent.createChooser(sendIntent, "Email:"));
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage("could not find any alogcat files");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
				builder.show();
			}
		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setMessage("could find alogcat directory");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			builder.show();
		}
	}
}
