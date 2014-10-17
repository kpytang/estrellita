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

package edu.uci.ics.star.estrellita.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.VersionInfo;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class ContactInfo extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dialog_listview);
		
		LinearLayout mainLayout = new LinearLayout(this);
		mainLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		
		// generic header text
		TextView tv = new TextView(this);
		tv.setText("You can contact the study coordinators by email or phone");
		tv.setTextSize(20);
		tv.setPadding(10,5,10,5);
		tv.setBackgroundColor(Color.WHITE);
		tv.setTextColor(Color.BLACK);
		
		mainLayout.addView(tv);
		mainLayout.addView(CustomViews.createSeparator(this, 1));
		
		// contact info - email
		LinearLayout llayout = new LinearLayout(this);
		llayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		llayout.setOrientation(LinearLayout.VERTICAL);
		llayout.setPadding(10, 5, 5, 5);
		
		tv = new TextView(this);
		tv.setText("Send Email to Study Coordinators");
		tv.setTextSize(20);
		tv.setTextColor(Color.WHITE);
		tv.setTypeface(null, Typeface.BOLD);
		llayout.addView(tv);
		tv = new TextView(this);
		tv.setText(getResources().getString(R.string.baby_uci_email));
		tv.setTextSize(15);
		tv.setTextColor(Color.WHITE);
		tv.setTypeface(null, Typeface.NORMAL);
		
		llayout.addView(tv);
		llayout.setClickable(true);
		llayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
	    		Intent sendIntent = new Intent(Intent.ACTION_SEND);
	    		// need to add this or the send email intent isn't recognized
	    		sendIntent.setType("jpeg/image");
	    	    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {
	    	    		getResources().getString(R.string.baby_uci_email), 
						getResources().getString(R.string.baby_gmail_email)});
	    	    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.contact_study_coordinators_subject));
	    	    Float megaBytesFree = Utilities.megabytesAvailable(Environment.getExternalStorageDirectory().getPath());
	    	    String emailBody = "\n\n\n---------\n"
	    	    	+ "Parent ID: " + EstrellitaTiles.getParentId(ContactInfo.this) + ", "
	    	    	+ "Baby ID: " + EstrellitaTiles.getCurrentBabyId() + ", "
	    	    	+ "Version: " + VersionInfo.VERSION + ", "
	    	    	+ "sdcard MB free space: " + megaBytesFree.toString();
	    	    sendIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
	    	    startActivity(Intent.createChooser(sendIntent, "Email:"));
			}
		});
		mainLayout.addView(llayout);
		mainLayout.addView(CustomViews.createSeparator(this, 1));

		// contact info - call
		llayout = new LinearLayout(this);
		llayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		llayout.setOrientation(LinearLayout.VERTICAL);
		llayout.setPadding(10, 5, 5, 5);
		
		tv = new TextView(this);
		tv.setText("Call Study Coordinators");
		tv.setTextSize(20);
		tv.setTypeface(null, Typeface.BOLD);
		tv.setTextColor(Color.WHITE);
		llayout.addView(tv);
		tv = new TextView(this);
		tv.setText(getResources().getString(R.string.baby_google_voice));
		tv.setTextSize(15);
		tv.setTextColor(Color.WHITE);
		tv.setTypeface(null, Typeface.NORMAL);
		
		llayout.addView(tv);
		llayout.setClickable(true);
		llayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = StringUtils.extractOnlyNumbers(getResources().getString(R.string.baby_google_voice));
				Intent phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + phoneNumber));
				startActivity(phoneDialer);
			}
		});
		mainLayout.addView(llayout);
		mainLayout.addView(CustomViews.createSeparator(this, 1));
		
		setContentView(mainLayout);
	}
}
