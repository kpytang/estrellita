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

package edu.uci.ics.star.estrellita.customview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.activity.CallReminderForm;
import edu.uci.ics.star.estrellita.activity.SurveyForm;
import edu.uci.ics.star.estrellita.activity.TileActivity;
import edu.uci.ics.star.estrellita.activity.ViewAppointment;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.PromptedSpecialReminder;
import edu.uci.ics.star.estrellita.object.SpecialReminder.ReminderType;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class AppointmentRemindersDialog extends Dialog  {
	private static final int NOTES_ICON_RESOURCE_ID = R.drawable.notes_icon;

	Map<Baby, List<PromptedSpecialReminder>> mRemindersMap;

	Context mContext;
	String mTitle;

	public AppointmentRemindersDialog(Context context, Map<Baby, List<PromptedSpecialReminder>> apptReminders) {
		super(context);
		mContext = context;
		mRemindersMap = apptReminders;

		setCanceledOnTouchOutside(true);
	}

	// Called when the dialog is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ScrollView scrollView = new ScrollView(mContext);
		LinearLayout layout = new LinearLayout(mContext);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		scrollView.addView(layout);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		TextView tv;
		RelativeLayout relativeLayout = null;
		LinearLayout linearLayout = null;
		Bitmap notesBitmap = null, surveyBitmap = null, phoneBitmap = null;
		ImageView iv;
		List<PromptedSpecialReminder> reminders;

		// if there are appointments then show this section
		if (hasAppointments()) {
			// show the appointment reminders header
			linearLayout = new LinearLayout(mContext);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			tv = new TextView(mContext);
			tv.setText("Appointments");
			tv.setTextSize(25);
			tv.setTypeface(null, Typeface.BOLD);
			tv.setTextColor(Color.BLACK);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			linearLayout.addView(tv);
			tv = new TextView(mContext);
			tv.setText("(click to get info about upcoming appts)");
			tv.setTextSize(15);
			tv.setTypeface(null, Typeface.NORMAL);
			tv.setTextColor(Color.BLACK);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			linearLayout.addView(tv);
			linearLayout.setBackgroundColor(Color.WHITE);
			layout.addView(linearLayout);
			layout.addView(CustomViews.createSeparator(mContext, 1));

			// go through each appointment and show them individually as three-line listview item
			for (Baby baby: mRemindersMap.keySet()) {
				reminders = mRemindersMap.get(baby);
				for (int i=0; i<reminders.size(); i++) {
					PromptedSpecialReminder reminder = reminders.get(i);
					if (reminder.getType() == ReminderType.APPOINTMENT) {
						Appointment a = reminder.getAppointment();
						relativeLayout = (RelativeLayout) inflater.inflate(R.layout.three_line_list_item_with_icon, null);
						relativeLayout = (RelativeLayout) inflater.inflate(R.layout.three_line_list_item_with_icon, null);
						// appt time
						tv = (TextView) relativeLayout.findViewById(R.id.row0);
						tv.setText("Today @ " + DateUtils.getDateAsString(a.getStartTime(), DateUtils.APPOINTMENT_TIME_FORMAT));
						// dr's name and type
						tv = (TextView) relativeLayout.findViewById(R.id.row1);
						tv.setText(a.getDoctorInfo());
						// appt concerns
						tv = (TextView) relativeLayout.findViewById(R.id.row2);
						String concernsString = "";
						if (a.isConcernedAboutDiapers()) {
							concernsString = StringUtils.addToCommaDelimitedList("diapers", concernsString);
						}
						if (a.isConcernedAboutBabyMoods()) {
							concernsString = StringUtils.addToCommaDelimitedList("baby moods", concernsString);
						}
						if (a.isConcernedAboutCharts()) {
							concernsString = StringUtils.addToCommaDelimitedList("charts", concernsString);
						}
						if (a.isConcernedAboutWeight()) {
							concernsString = StringUtils.addToCommaDelimitedList("weight", concernsString);
						}
						if (concernsString.length()>0) {
							tv.setText("concerns: " + concernsString);
							tv.setVisibility(View.VISIBLE);
						}
						else {
							tv.setVisibility(View.GONE);
						}
						// apt notes
						iv = (ImageView) relativeLayout.findViewById(R.id.icon);
						if (a.getCommonData().hasNotes()) {
							if (notesBitmap == null) {
								notesBitmap = BitmapFactory.decodeResource(mContext.getResources(), NOTES_ICON_RESOURCE_ID);
							}
							iv.setImageBitmap(notesBitmap);
							iv.setVisibility(View.VISIBLE);
						}
						else {
							iv.setVisibility(View.GONE);
						}
						// build the intent
						Intent intent = new Intent(mContext, ViewAppointment.class);
						intent.putExtra(TileActivity.BABY, baby);
						intent.putExtra("appointment", a);
						relativeLayout.setTag(intent);
						// add onclick behavior which will launch intent
						relativeLayout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = (Intent) v.getTag();
								mContext.startActivity(intent);
								dismiss();
							}
						});
						layout.addView(relativeLayout);
						layout.addView(CustomViews.createSeparator(mContext, 1));
					}
				}
			}
		}

		// if there are any surveys then show this section
		if (hasPreAppointmentSurveys() || hasPostAppointmentSurveys()) {
			// show the surveys  header
			linearLayout = new LinearLayout(mContext);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			tv = new TextView(mContext);
			tv.setText("Survey Reminders");
			tv.setTextSize(25);
			tv.setTypeface(null, Typeface.BOLD);
			tv.setTextColor(Color.BLACK);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			linearLayout.addView(tv);
			tv = new TextView(mContext);
			tv.setText("(you haven't taken these surveys yet)");
			tv.setTextSize(15);
			tv.setTypeface(null, Typeface.NORMAL);
			tv.setTextColor(Color.BLACK);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			linearLayout.addView(tv);
			linearLayout.setBackgroundColor(Color.WHITE);
			layout.addView(linearLayout);
			layout.addView(CustomViews.createSeparator(mContext, 1));

			// show the pre-surveys first
			for (Baby baby: mRemindersMap.keySet()) {
				reminders = mRemindersMap.get(baby);
				for (int i=0; i<reminders.size(); i++) {
					PromptedSpecialReminder reminder = reminders.get(i);
					if (reminder.getType() == ReminderType.PRE_APPT_SURVEY) {
						Appointment a = reminder.getAppointment();
						linearLayout = (LinearLayout) inflater.inflate(R.layout.list_item_with_icon, null);
						tv = (TextView) linearLayout.findViewById(R.id.text);
						tv.setText(reminder.getSurveyReminderString());
						iv = (ImageView) linearLayout.findViewById(R.id.icon);
						if (surveyBitmap == null) {
							surveyBitmap = BitmapFactory.decodeResource(mContext.getResources(), Tile.SURVEYS_ICON);
						}
						iv.setImageBitmap(surveyBitmap);
						linearLayout.setTag(a);
						// add onclick behavior which will launch intent
						linearLayout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Appointment a = (Appointment) v.getTag(); 
								Intent intent = new Intent(mContext, SurveyForm.class);
								intent.putExtra(SurveyForm.TITLE, "Pre-Appt Survey");
								intent.putExtra(TileActivity.BABY, EstrellitaTiles.getBaby(a.getCommonData().getIdBaby()));
								intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.PRE_APPT.name());
								intent.putExtra(SurveyForm.INDICATOR_ID, a.getId());
								mContext.startActivity(intent);
								dismiss();
							}
						});
						layout.addView(linearLayout);
						layout.addView(CustomViews.createSeparator(mContext, 1));
					}
				}
			}

			// show the post-surveys next
			for (Baby baby: mRemindersMap.keySet()) {
				reminders = mRemindersMap.get(baby);
				for (int i=0; i<reminders.size(); i++) {
					PromptedSpecialReminder reminder = reminders.get(i);
					if (reminder.getType() == ReminderType.POST_APPT_SURVEY) {
						Appointment a = reminder.getAppointment();
						linearLayout = (LinearLayout) inflater.inflate(R.layout.list_item_with_icon, null);
						tv = (TextView) linearLayout.findViewById(R.id.text);
						tv.setText(reminder.getSurveyReminderString());
						iv = (ImageView) linearLayout.findViewById(R.id.icon);
						if (surveyBitmap == null) {
							surveyBitmap = BitmapFactory.decodeResource(mContext.getResources(), Tile.SURVEYS_ICON);
						}
						iv.setImageBitmap(surveyBitmap);
						linearLayout.setTag(a);
						// add onclick behavior which will launch intent
						linearLayout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Appointment a = (Appointment) v.getTag(); 
								Intent intent = new Intent(mContext, SurveyForm.class);
								intent.putExtra(SurveyForm.TITLE, "Post-Appt Survey");
								intent.putExtra(TileActivity.BABY, EstrellitaTiles.getBaby(a.getCommonData().getIdBaby()));
								intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.POST_APPT.name());
								intent.putExtra(SurveyForm.INDICATOR_ID, a.getId());
								mContext.startActivity(intent);
								dismiss();
							}
						});
						layout.addView(linearLayout);
						layout.addView(CustomViews.createSeparator(mContext, 1));
					}
				}
			}
		}

		// if there are phone call reminders to show
		if (hasPhoneCallReminders()) {
			// show the phone call reminders header
			linearLayout = new LinearLayout(mContext);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			tv = new TextView(mContext);
			tv.setText("Reminders to Call");
			tv.setTextSize(25);
			tv.setTypeface(null, Typeface.BOLD);
			tv.setTextColor(Color.BLACK);
			tv.setGravity(Gravity.CENTER_HORIZONTAL);
			linearLayout.addView(tv);
			linearLayout.setBackgroundColor(Color.WHITE);
			layout.addView(linearLayout);
			layout.addView(CustomViews.createSeparator(mContext, 1));

			// show the st joseph phone calls first using a one-line list item
			for (Baby baby: mRemindersMap.keySet()) {
				reminders = mRemindersMap.get(baby);
				for (int i=0; i<reminders.size(); i++) {
					PromptedSpecialReminder reminder = reminders.get(i);
					if (reminder.getType() == ReminderType.CALL_ST_JOSEPH) {
						linearLayout = (LinearLayout) inflater.inflate(R.layout.list_item_with_icon, null);
						tv = (TextView) linearLayout.findViewById(R.id.text);
						tv.setText(reminder.getSurveyReminderString());
						iv = (ImageView) linearLayout.findViewById(R.id.icon);
						if (phoneBitmap == null) {
							phoneBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.phone);
						}
						iv.setImageBitmap(phoneBitmap);
						linearLayout.setTag(mContext.getResources().getString(R.string.st_josephs_phone_number));
						// add onclick behavior which will launch intent
						// for st joseph reminder it will launch the ReminderForm activity
						linearLayout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {								
								Intent intent = new Intent(mContext, CallReminderForm.class);
								intent.putExtra(TileActivity.BABY, EstrellitaTiles.getBaby(EstrellitaTiles.getCurrentBabyId()));
								ArrayList<String> callOptions = new ArrayList<String>();
								callOptions.add((String) mContext.getResources().getText(R.string.stjoseph_call_reminder_option1));
								callOptions.add((String) mContext.getResources().getText(R.string.stjoseph_call_reminder_option2));
								ArrayList<String> phoneNumbers = new ArrayList<String>();
								phoneNumbers.add((String) mContext.getResources().getText(R.string.st_josephs_phone_number));
								phoneNumbers.add("-1");
								intent.putStringArrayListExtra(CallReminderForm.OPTIONS, callOptions);
								intent.putStringArrayListExtra(CallReminderForm.PHONE_NUMBERS, phoneNumbers);
								intent.putExtra(CallReminderForm.LEFT_BUTTON, (String) mContext.getResources().getText(R.string.stjoseph_call_reminder_option3));
								intent.putExtra(CallReminderForm.RIGHT_BUTTON, (String) mContext.getResources().getText(R.string.stjoseph_call_reminder_option4));
								mContext.startActivity(intent);
								dismiss();
							}
						});
						layout.addView(linearLayout);
						layout.addView(CustomViews.createSeparator(mContext, 1));
					}
				}
			}

			// show the reschedule appointments phone calls first using a one-line list item
			for (Baby baby: mRemindersMap.keySet()) {
				reminders = mRemindersMap.get(baby);
				for (int i=0; i<reminders.size(); i++) {
					PromptedSpecialReminder reminder = reminders.get(i);
					if (reminder.getType() == ReminderType.CALL_DOCTOR) {
						linearLayout = (LinearLayout) inflater.inflate(R.layout.list_item_with_icon, null);
						tv = (TextView) linearLayout.findViewById(R.id.text);
						tv.setText(reminder.getSurveyReminderString());
						iv = (ImageView) linearLayout.findViewById(R.id.icon);
						if (phoneBitmap == null) {
							phoneBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.phone);
						}
						iv.setImageBitmap(phoneBitmap);
						Appointment a = reminder.getAppointment();
						String phoneNumber = "";
						if ( (a != null) && (a.getPhone() != null) && (a.getPhone().length()>0) ) {
							phoneNumber = a.getPhone();
						}
						linearLayout.setTag(phoneNumber);
						// add onclick behavior which will launch intent
						linearLayout.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								String phoneNumber = (String) v.getTag();
								Intent phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + phoneNumber));
								mContext.startActivity(phoneDialer);
								dismiss();
							}
						});
						layout.addView(linearLayout);
						layout.addView(CustomViews.createSeparator(mContext, 1));
					}
				}
			}
		}

		// it crashed here due to a null pointer
		// we might need to delve into this more, since it shouldn't have thrown a null pointer
		// maybe we need to do that on addOnGlobalLayoutListener thing here
		try {
			// remove last view - which should be a separator
			layout.removeViewAt(layout.getChildCount()-1);
		} catch(Exception e) {

		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(scrollView);
	}

	private boolean hasAppointments() {
		for (Baby baby: mRemindersMap.keySet()) {
			List<PromptedSpecialReminder> reminders = mRemindersMap.get(baby);
			for (int i=0; i<reminders.size(); i++) {
				if (reminders.get(i).getType() == ReminderType.APPOINTMENT) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasPreAppointmentSurveys() {
		for (Baby baby: mRemindersMap.keySet()) {
			List<PromptedSpecialReminder> reminders = mRemindersMap.get(baby);
			for (int i=0; i<reminders.size(); i++) {
				if (reminders.get(i).getType() == ReminderType.PRE_APPT_SURVEY) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasPostAppointmentSurveys() {
		for (Baby baby: mRemindersMap.keySet()) {
			List<PromptedSpecialReminder> reminders = mRemindersMap.get(baby);
			for (int i=0; i<reminders.size(); i++) {
				if (reminders.get(i).getType() == ReminderType.POST_APPT_SURVEY) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasPhoneCallReminders() {
		for (Baby baby: mRemindersMap.keySet()) {
			List<PromptedSpecialReminder> reminders = mRemindersMap.get(baby);
			for (int i=0; i<reminders.size(); i++) {
				switch (reminders.get(i).getType()) {
				case CALL_DOCTOR:
				case CALL_ST_JOSEPH:
					return true;
				}
			}
		}
		return false;
	}
}
