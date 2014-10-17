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

import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.AppointmentsListViewDialog;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.object.Response;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.GenericSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Appointment.AttendedState;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.sharedprefs.api.ReminderPreferences;
import edu.uci.ics.star.estrellita.updateservice.SyncService;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class ViewAppointment extends TileActivity<Appointment> {
	private static final int DIAPERS_ID = 0;
	private static final int BABYMOODS_ID = 1;
	private static final int WEIGHT_ID = 2;
	private static final int GENERIC_CHARTS_ID = 3;
	private static final int EDIT_APPOINTMENT_FORM_ID = 4;
	private static final int FOLLOWUP_APPOINTMENT_FORM_ID = 5;
	private static final int PRE_APPT_SURVEY = 10;
	private static final int POST_APPT_SURVEY = 11;
	private static final int UPDATE_PROGRESS_DIALOG_ID = 12;
	private static final int APPOINTMENT_LIST_DIALOG_ID = 13;
	private static final int NOTES_DIALOG_ID = 14;

	private static final float CHOC_LATITUDE = 33.78052f;
	private static final float CHOC_LONGITUDE = -117.8667f;

	// refers to the old/original appointment information, before things have been edited/updated
	public static long staticDate = -1;
	public static Appointment staticOrigAppointment;
	
	private UpdateReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appointment_view_form);

		setActivityHeader("appointment", true, Tile.APPOINTMENTS);
		mIndicator = (Appointment) getIntent().getParcelableExtra("appointment"); 

		updateAppointmentView();
	}

	public View.OnClickListener mEditClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			// first save the current appointment as the static appointment
			staticOrigAppointment = mIndicator;

			Intent intent = new Intent(ViewAppointment.this, AppointmentForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			intent.putExtra("appointment", mIndicator);
			startActivityForResult(intent, EDIT_APPOINTMENT_FORM_ID);
		}
	};

	public View.OnClickListener mFollowupClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			mIndicator.getCommonData().setNotes(null);
			mIndicator.clearAllConcerns();
			mIndicator.setDate(DateUtils.getTimestamp());
			mIndicator.setStartTime(new Time(DateUtils.getTimeInMillis()));
			mIndicator.setAttended(AttendedState.UNKNOWN);	// clearing the attended state
			Intent intent = new Intent(ViewAppointment.this, AppointmentForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			intent.putExtra("appointment", mIndicator);
			// this will trigger appointment listview to just insert a new appointment
			staticDate = (long)-1;
			startActivityForResult(intent, FOLLOWUP_APPOINTMENT_FORM_ID);
		}
	};

	public void launchConcernTile(int index, boolean turnOn) {
		ImageView iv = null;
		View.OnClickListener listener = null;
		switch(index) {
		// diapers
		case 0:
			iv = (ImageView) this.findViewById(R.id.diaper_concern);
			listener = new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), DiapersOverview.class); 
					intent.putExtra(TileActivity.BABY, mBaby);
					startActivityForResult(intent, DIAPERS_ID);
				}
			};
			break;
			// baby moods
		case 1:
			iv = (ImageView) this.findViewById(R.id.babymood_concern);
			listener = new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), BabyMoodsOverview.class); 
					intent.putExtra(TileActivity.BABY, mBaby);
					startActivityForResult(intent, BABYMOODS_ID);
				}
			};
			break;
			// custom charts
		case 2:
			iv = (ImageView) this.findViewById(R.id.charts_concern);
			listener = new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), CustomChartsOverview.class); 
					intent.putExtra(TileActivity.BABY, mBaby);
					startActivityForResult(intent, GENERIC_CHARTS_ID);
				}
			};
			break;
			// weight
		case 3:
			iv = (ImageView) this.findViewById(R.id.weight_concern);
			listener = new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), WeightOverview.class); 
					intent.putExtra(TileActivity.BABY, mBaby);
					startActivityForResult(intent, WEIGHT_ID);
				}
			};
			break;
		}
		if (iv != null) {
			if (turnOn) {
				iv.setAlpha(255);
				iv.setClickable(true);
				iv.setOnClickListener(listener);
			}
			else {
				iv.setAlpha(70);
				iv.setClickable(false);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case APPOINTMENT_LIST_DIALOG_ID:
			openDatabase();
			List<Appointment> appointments = Arrays.asList(mDatabase.getAppointmentTable().getAppointmentsByDoctor(mBaby.getId(), mIndicator.getDoctorName()));
			return new AppointmentsListViewDialog(ViewAppointment.this, 
				"Appointments with\nDr. " + mIndicator.getDoctorName(), 
				appointments, 
				getIntent());
		case NOTES_DIALOG_ID:
			return new AlertDialog.Builder(ViewAppointment.this)
			.setTitle("Notes for this appointment")
			.setMessage(mIndicator.getCommonData().getNotes())
			.setCancelable(true)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).create();
		case UPDATE_PROGRESS_DIALOG_ID:
			ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("updating appointment...");
            dialog.setIndeterminate(true);
            return dialog;
		}
		return null;
	}
	
	// checks for results when coming back from the 'edit new appointment' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			TextView tv;
			Appointment newAppointment;
			switch (requestCode) {
			case FOLLOWUP_APPOINTMENT_FORM_ID:
				newAppointment = (Appointment) data.getParcelableExtra("appointment");
				// make sure that this new appointment gets a new id
				newAppointment.getCommonData().setId(null);
				newAppointment.getCommonData().setLocalId(null);
				Intent intent = new Intent();
				intent.putExtra("appointment", newAppointment);
				// we're setting a result canceled as a hack to signal to AppointmentTabs that this is a new (follow-up) appointment
				setResult(RESULT_CANCELED, intent);
				staticDate = (long) -1;
				super.onBackPressed();
				finish();
				break;
			case EDIT_APPOINTMENT_FORM_ID:
				newAppointment = (Appointment) data.getParcelableExtra("appointment");
				openDatabase();
				// static appointment should hold the original appointment, before it was edited
				Date oldDate = DateUtils.combineDateAndTime(staticOrigAppointment.getDate(), staticOrigAppointment.getStartTime());
				Date newDate = DateUtils.combineDateAndTime(newAppointment.getDate(), newAppointment.getStartTime());
				// if the dates are the same, just do an update (it means people updated the notes, locations, concerns, phone)
				if (oldDate.equals(newDate)) {
					mDatabase.update(newAppointment);
					data.setClassName(this, this.getClass().getName());
					mDatabase.logUpdate(data, newAppointment);
					mIndicator = newAppointment;
				}
				// the dates aren't the same, so they rescheduled it, so we need to delete it and then insert it again
				// so that we generate a new id in the database and can re-trigger the pre-appointment survey
				else {
					mDatabase.delete(staticOrigAppointment);
					// we should also delete any notifications associated with this old appointment
					try {
						String s = getResources().getString(R.string.pre_appt_reminder) + " for " + mBaby.getName();
						SyncService.cancelNotification(this, s, staticOrigAppointment.getId());
						s = getResources().getString(R.string.post_appt_reminder) + " for " + mBaby.getName();
						SyncService.cancelNotification(this, s, staticOrigAppointment.getId());
					}
					catch (Exception e) {
						// not sure if there's an error for canceling a notification that doesn't exist
					}
					data.setClassName(this, this.getClass().getName());
					mDatabase.logDelete(data, staticOrigAppointment);
					// we need to rest the id's so that the new appointment shows up as a new one
					newAppointment.getCommonData().setId(null);
					newAppointment.getCommonData().setLocalId(null);
					mDatabase.insertSingle(newAppointment);
					mDatabase.logInsert(data, newAppointment);
					mIndicator = newAppointment;
				}
				staticDate = oldDate.getTime();
				mReceiver = new UpdateReceiver();
				registerReceiver(mReceiver, new IntentFilter(Utilities.ACTION_UPDATE_MAIN));
				showDialog(UPDATE_PROGRESS_DIALOG_ID);
				break;
			case PRE_APPT_SURVEY:
				tv = (TextView) findViewById(R.id.pre_survey_appt);
				tv.setVisibility(View.GONE);
				break;
			case POST_APPT_SURVEY:
				tv = (TextView) findViewById(R.id.post_survey_appt);
				tv.setVisibility(View.GONE);
				break;
			}
		}
		else {
			TextView tv;
			switch (requestCode) {
			case PRE_APPT_SURVEY:
				tv = (TextView) findViewById(R.id.pre_survey_appt);
				tv.setVisibility(View.VISIBLE);
				break;
			case POST_APPT_SURVEY:
				tv = (TextView) findViewById(R.id.pre_survey_appt);
				tv.setVisibility(View.VISIBLE);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("appointment", mIndicator);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
		finish();
	}

	private boolean showPreApptSurvey() {
		// check to see if it's within reminder period before this appointment
		int reminder = ReminderPreferences.getPreAppointmentReminder(this);
		Date apptDate = DateUtils.combineDateAndTime(mIndicator.getDate(), mIndicator.getStartTime());
		int hoursUntil = DateUtils.getHoursUntil(apptDate);
		// the appointment is in the future and the appointment is within the reminder window, then look for existing pre-appt surveys
		if ( (apptDate.after(DateUtils.getTimestamp())) && (hoursUntil <= reminder) ) {
			openDatabase();
			GenericSurvey survey = (GenericSurvey) mDatabase.getPreAppointmentSurveyTable().getSurveyForBaby(mBaby.getId(), mIndicator.getId());
			// we didn't find a completed pre-appt survey, so we should show the button
			if (survey == null) {
				return true;
			}
		}
		return false;
	}

	// we will always show post-appointment surveys, as long as: 
	// it hasn't been completed before AND
	// the user said that they would attend the appointment in their pre-appt survey OR they didn't complete a pre-appt survey
	private boolean showPostApptSurvey(){
		// only show this option if the appointment has occurred before today
		if (DateUtils.combineDateAndTime(mIndicator.getDate(), mIndicator.getStartTime()).before(DateUtils.getTimestamp())) {
			openDatabase();
			GenericSurvey survey = (GenericSurvey) mDatabase.getPostAppointmentSurveyTable().getSurveyForBaby(mBaby.getId(), mIndicator.getId());
			// we didn't find a completed post-appt survey
			if (survey == null) {
				// so now we check if the user didn't complete a pre-appt survey
				survey = (GenericSurvey) mDatabase.getPreAppointmentSurveyTable().getSurveyForBaby(mBaby.getId(), mIndicator.getId());
				// they didn't complete pre-survey, so we should still show the post-survey
				if (survey == null) {
					return true;
				}
				// else they did complete a pre-appt survey, so we need to check if they said that they'd come to the appt
				else {
					List<Response> responses = mDatabase.getPreAppointmentSurveyTable().getSurveyResponseSetForBaby(mBaby.getId(), mIndicator.getId(), 0);
					if (responses.size() > 0) {
						Response r = responses.get(0);
						// if the response is a YES (coded as a 0), then that means that they said they'd come to the appt
						// so we can show them the post-appt survey
						if (r.getData() == 0) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private void updateAppointmentView() {
		// ideally i'd want to create a list, but we can't create a listview inside a scrollview
		// so instead we'll create multiple relativelayouts/textviews inside a linearlayout
		LinearLayout mainLayout = (LinearLayout) this.findViewById(R.id.details_layout);
		if (mainLayout.getChildCount()>5) {
			mainLayout.removeViews(5, mainLayout.getChildCount()-5);
		}

		// if there's a pre/post appointment that needs to be taken, then show it
		// there can never be both pre & post surveys so we'll use an if/else if
		TextView tv = (TextView) findViewById(R.id.pre_survey_appt);

		if (showPreApptSurvey()) {
			tv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ViewAppointment.this, SurveyForm.class);
					intent.putExtra(SurveyForm.TITLE, "Pre-Appt Survey");
					intent.putExtra(TileActivity.BABY, mBaby);
					intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.PRE_APPT.name());
					intent.putExtra(SurveyForm.INDICATOR_ID, mIndicator.getId());
					startActivityForResult(intent, PRE_APPT_SURVEY);
				}
			});
			tv.setTextSize(20);
			tv.setVisibility(View.VISIBLE);
			// since we're showing the pre-appt survey, then we'll hide the post-appt survey
			tv = (TextView) findViewById(R.id.post_survey_appt);
			tv.setVisibility(View.GONE);
		}
		else {
			// we shouldn't show pre-appt survey
			tv.setVisibility(View.GONE);
			// so now check for post-appointment survey
			tv = (TextView) findViewById(R.id.post_survey_appt);
			tv.setTextSize(20);
			if (showPostApptSurvey()) {
				tv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ViewAppointment.this, SurveyForm.class);
						intent.putExtra(SurveyForm.TITLE, "Post-Appt Survey");
						intent.putExtra(TileActivity.BABY, mBaby);
						intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.POST_APPT.name());
						intent.putExtra(SurveyForm.INDICATOR_ID, mIndicator.getId());
						startActivityForResult(intent, POST_APPT_SURVEY);
					}
				});
				tv.setVisibility(View.VISIBLE);
			}
			else {
				tv.setVisibility(View.GONE);
			}
		}
		
		// dr name & type & appt date/time
		RelativeLayout layout = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.three_line_list_item_with_icon, null);
		tv = (TextView) layout.findViewById(R.id.row0);
		tv.setText("Dr. " + mIndicator.getDoctorName());
		tv = (TextView) layout.findViewById(R.id.row1);
		tv.setText("Specialty: " + mIndicator.getDoctorTypeAsString());
		tv = (TextView) layout.findViewById(R.id.row2);
		String dateString = DateUtils.getDateAsString(mIndicator.getDate(), DateUtils.APPOINTMENT_SHORT_DATE_FORMAT) + " @ " + 
		DateUtils.getDateAsString(mIndicator.getStartTime(), DateUtils.APPOINTMENT_TIME_FORMAT);
		if (DateUtils.isDateOnOrAfter(mIndicator.getDate(), DateUtils.getTimestamp())) {
			tv.setText("Next visit on: " + dateString);
		}
		else {
			tv.setText("Past visit on: " + dateString);
		}
		ImageView iv = (ImageView) layout.findViewById(R.id.icon);
		iv.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.doctor));
		iv.setVisibility(View.VISIBLE);
		layout.setClickable(true);
		layout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(APPOINTMENT_LIST_DIALOG_ID);
			}
		});
		mainLayout.addView(layout);
		mainLayout.addView(CustomViews.createSeparator(this, 1));

		// call office @ given phone number (if a phone number is entered)
		View v = null;
		if ((mIndicator.getPhone() != null) && (mIndicator.getPhone().length() > 0)) {
			v = (LinearLayout) this.getLayoutInflater().inflate(R.layout.list_item_with_icon, null);
			tv = (TextView) v.findViewById(R.id.text);
			tv.setText("Call " + StringUtils.cleanUpPhoneNumber(mIndicator.getPhone()));
			tv.setTextSize(getResources().getDimension(R.dimen.font_small));
			iv = (ImageView) v.findViewById(R.id.icon);
			iv.setImageResource(R.drawable.phone);
		}
		else {
			tv = (TextView) this.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
			tv.setText("Call Office");
		}
		tv.setClickable(true);
		tv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String phoneNumber = (((TextView) v).getText()).toString();
				Intent phoneDialer;
				if (phoneNumber.contains("Office")) {
					phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"));
				}
				else {
					phoneNumber = phoneNumber.substring(5); // get rid of the "Call " prefix
					phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + phoneNumber));
				}
				startActivity(phoneDialer);
			}
		});
		if (v != null) {
			mainLayout.addView(v);
		}
		else {
			mainLayout.addView(tv);
		}
		mainLayout.addView(CustomViews.createSeparator(this, 1));

		// address/location (if it's given)
		if ((mIndicator.getLocation() != null) && (mIndicator.getLocation().length() > 0)) {
			layout = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.two_line_list_item_with_icon, null);
			tv = (TextView) layout.findViewById(R.id.row0);
			tv.setText("View Map");
			tv = (TextView) layout.findViewById(R.id.row1);
			tv.setText(mIndicator.getLocation());
			layout.setClickable(true);
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String geoUriString = "geo:"+ CHOC_LATITUDE + "," + CHOC_LONGITUDE + "?z=7&q=" + mIndicator.getLocation();
					Intent mapCall = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString)); 
					startActivity(mapCall);
				}
			});
			iv = (ImageView) layout.findViewById(R.id.icon);
			iv.setImageResource(R.drawable.map);
			mainLayout.addView(layout);
			mainLayout.addView(CustomViews.createSeparator(this, 1));
		}

		// notes
		if (mIndicator.getCommonData().hasNotes()) {
			v = (LinearLayout) this.getLayoutInflater().inflate(R.layout.list_item_with_icon, null);
			tv = (TextView) v.findViewById(R.id.text);
			tv.setText("View Notes");
			iv = (ImageView) v.findViewById(R.id.icon);
			iv.setImageResource(R.drawable.notes_icon);
			v.setClickable(true);
			v.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					showDialog(NOTES_DIALOG_ID);
				}
			});
			mainLayout.addView(v);
			mainLayout.addView(CustomViews.createSeparator(this, 1));
		}

		LinearLayout llayout;
		// concerns
		if (!mIndicator.isNoConcerns()) {
			llayout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.concerns_wrapper, null);
			mainLayout.addView(llayout);
			boolean[] concerns = mIndicator.getAllConcerns();
			for (int i=0; i<4; i++) {
				launchConcernTile(i, concerns[i]);
			}
		}

		// initialize footer
		llayout = (LinearLayout) this.getLayoutInflater().inflate(R.layout.button_footer, null);
		mainLayout.addView(llayout);
		setButtonFooter("edit/update", mEditClickListener, "add a follow-up", mFollowupClickListener);
	}
	
	// this is for catching when the updates are done
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateHandler.sendEmptyMessage(0);
		}
	}
	
	// define the Handler that receives messages from the thread and update the progress
	final Handler updateHandler = new Handler() {
		public void handleMessage(Message msg) {
			try{
				removeDialog(UPDATE_PROGRESS_DIALOG_ID);
				mIndicator = mDatabase.getAppointmentTable().getAppointmentByProperties(mIndicator);
				updateAppointmentView();
				unregisterReceiver(mReceiver);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
}