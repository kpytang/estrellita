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

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class CallReminderForm extends GenericActivity {
	public static final String TITLE = "reminder";
	public static final String OPTIONS = "options";
	public static final String PHONE_NUMBERS = "phonenumbers";
	public static final String LEFT_BUTTON = "leftbutton";
	public static final String RIGHT_BUTTON = "rightbutton";
	
	private List<String> mReminderOptions, mReminderPhoneNumbers;
	private String mLeftButtonName;
	private String mRightButtonName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.reminder_form);

		setActivityHeader(CallReminderForm.TITLE, false, Tile.REMINDER);
		mReminderOptions = getIntent().getStringArrayListExtra(CallReminderForm.OPTIONS);
		mReminderPhoneNumbers = getIntent().getStringArrayListExtra(CallReminderForm.PHONE_NUMBERS);
		mLeftButtonName = getIntent().getStringExtra(CallReminderForm.LEFT_BUTTON);
		mRightButtonName = getIntent().getStringExtra(CallReminderForm.RIGHT_BUTTON);
		
		updateReminderView();
	}

	public View.OnClickListener mAlreadyCalledListener = new View.OnClickListener() {
		public void onClick(View v) {
			// set the flag in the log table
			Log log = new Log(new CommonData(EstrellitaTiles.getParentId(CallReminderForm.this), mBaby.getId()), SurveyType.EPDS.name(), SurveyForm.ALREADY_CALLED_TAG, "-1");
			mDatabase.insertSingle(log, false, false);
			// return to the previous screen
			finish();
		}
	};

	public View.OnClickListener mCallLaterListener = new View.OnClickListener() {
		public void onClick(View v) {
			// return to the previous screen
			finish();
		}
	};

	private void updateReminderView() {
		LinearLayout mainLayout = (LinearLayout) this.findViewById(R.id.options_layout);
		if (mainLayout.getChildCount()>2) {
			mainLayout.removeViews(2, mainLayout.getChildCount()-2);
		}
		
		// show the call options header text
		TextView tv = new TextView(this);
		tv.setText(R.string.stjoseph_call_reminder_prompt);
		tv.setTextSize(18);
		tv.setTypeface(null, Typeface.ITALIC);
		mainLayout.addView(tv);
		mainLayout.addView(CustomViews.createSeparator(this, 1));

		// show the call options
		ImageView iv;
		View v = null;
		for(int i=0; i<mReminderOptions.size(); i++) {
			// create a textview for each option
			v = (LinearLayout) this.getLayoutInflater().inflate(R.layout.list_item_with_icon, null);
			tv = (TextView) v.findViewById(R.id.text);
			tv.setText(mReminderOptions.get(i));
			tv.setTextSize(getResources().getDimension(R.dimen.font_small));
			tv.setTag(mReminderPhoneNumbers.get(i));
			iv = (ImageView) v.findViewById(R.id.icon);
			iv.setImageResource(R.drawable.phone);
			
			// now add the actions for each option
			tv.setClickable(true);
			tv.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// clicking an option will set a call reminder flag
					Log log = new Log(new CommonData(EstrellitaTiles.getParentId(CallReminderForm.this), mBaby.getId()), SurveyType.EPDS.name(), SurveyForm.ALREADY_CALLED_TAG, "-1");
					mDatabase.insertSingle(log, false, false);
					
					String phoneNumber = (((TextView) v).getTag()).toString();
					// if it starts with -1, then this will just launch the address book
					if (phoneNumber.startsWith("-1")) {
						Cursor cursor =  managedQuery(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
						// only go to the address book if there are people listed there
						if (cursor.getCount() > 0) {
							cursor.close();
							// show the address book
							Intent contacts = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);  
							startActivity(contacts); 
						}
						else {
							// show a dialog warning them that they have no contacts in their phone
							AlertDialog.Builder builder = new AlertDialog.Builder(CallReminderForm.this);
							builder.setIcon(android.R.drawable.ic_dialog_alert);
							builder.setMessage("Your address book is empty.")
							       .setCancelable(true)
							       .setPositiveButton("Go back", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							                dialog.cancel();
							           }
							       });
							AlertDialog alert = builder.create();
							alert.show();
						}
					}
					// else, we'll assume it's a valid phone number: we'll clean it up for display, and then launch the dialer
					else {
						phoneNumber = StringUtils.cleanUpPhoneNumber(phoneNumber);
						Intent phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + phoneNumber));
						startActivity(phoneDialer);
					}
				}
			});
			
			mainLayout.addView(v);
			mainLayout.addView(CustomViews.createSeparator(this, 1));
		}

		// initialize footer
		LinearLayout layout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.button_footer, null);
		mainLayout.addView(layout);
		setButtonFooter(mRightButtonName, mCallLaterListener, mLeftButtonName, mAlreadyCalledListener);
	}
}