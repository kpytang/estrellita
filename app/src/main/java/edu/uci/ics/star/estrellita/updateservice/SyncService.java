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

package edu.uci.ics.star.estrellita.updateservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.activity.SurveyForm;
import edu.uci.ics.star.estrellita.activity.TileActivity;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.db.table.indicator.LogTable;
import edu.uci.ics.star.estrellita.db.table.indicator.SurveyTable;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.SpecialReminder;
import edu.uci.ics.star.estrellita.object.SpecialReminder.ReminderType;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.Survey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.sharedprefs.api.PreferencesApi;
import edu.uci.ics.star.estrellita.sharedprefs.api.ReminderPreferences;
import edu.uci.ics.star.estrellita.sharedprefs.api.UserPreferences;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;
import edu.uci.ics.star.estrellita.utils.WebUtils;

public class SyncService extends WakefulIntentService {
	private static boolean isUpdating = false;
	private static double dbKey = -1;

	public SyncService() {
		super("AppService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		doUpdateWork();
	}

	public void doUpdateWork() {
		Database db = new Database(this);
		dbKey = db.open(dbKey);

		StringBuilder sb = new StringBuilder();
		sb.append("SyncService.doWakefulWork- db:");
		if(db == null || db.getDb() == null || !db.getDb().isOpen()){
			sb.append("closed");
		} else {
			sb.append("open, isOnline:");
			sb.append(WebUtils.isOnline(this));
			updateTables(this, db, sb);
			checkOccasionallyDoneIndicators(this, db);
			performSpecialReminders(this, db);
			Utilities.updateWidget(this);
		}
		if (PreferencesApi.getBoolean(this, PreferencesApi.NEED_TO_UPLOAD_ERROR_LOG)) {
			String weeklyLogFilename = Utilities.getWeeklyLogFilename();
			JSONObject json = new JSONObject();
			try {
				json.put("reportname", "errorlog");
				json.put("user_id", UserPreferences.getLastUserId(this));
				json.put("baby_id", UserPreferences.getKidIds(this).get(0));
				json.put("created_at", DateUtils.getTimeInMillis());
				json.put("error_file", "");
				WebUtils.postToServer(WebUtils.ESTRELLITA_SERVER_SUBMIT_URL,Utilities.getBytesFromFile(Utilities.ESTRELLITA_DIR + weeklyLogFilename), weeklyLogFilename, json);
				PreferencesApi.putBoolean(this, PreferencesApi.NEED_TO_UPLOAD_ERROR_LOG, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		db.close(dbKey);
	}

	public static void threadedUpdateTables(final Context c, final Database db, final StringBuilder sb) {
		// upload error log
		if (PreferencesApi.getBoolean(c, PreferencesApi.NEED_TO_UPLOAD_ERROR_LOG)) {
			String weeklyLogFilename = Utilities.getWeeklyLogFilename();
			JSONObject json = new JSONObject();
			try {
				json.put("reportname", "errorlog");
				json.put("user_id", UserPreferences.getLastUserId(c));
				json.put("baby_id", UserPreferences.getKidIds(c).get(0));
				json.put("created_at", DateUtils.getTimeInMillis());
				json.put("error_file", "");
				WebUtils.postToServer(WebUtils.ESTRELLITA_SERVER_SUBMIT_URL,Utilities.getBytesFromFile(Utilities.ESTRELLITA_DIR + weeklyLogFilename), weeklyLogFilename, json);
				PreferencesApi.putBoolean(c, PreferencesApi.NEED_TO_UPLOAD_ERROR_LOG, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Thread thread = new Thread(new Runnable (){
			public void run() {
				dbKey = db.open(dbKey);
				updateAllTablesButLog(c, db, sb);
				db.close(dbKey);
				Intent intent = new Intent(Utilities.ACTION_UPDATE_MAIN);
				c.sendBroadcast(intent);
			}
		});
		thread.start();
	} 

	public static void updateTables(Context c, Database db, StringBuilder sb) {
		if(!isUpdating) {
			isUpdating = true;
			try {
				for(Integer childId:UserPreferences.getKidIds(c)){
					boolean updatedResponses = false;
					if(WebUtils.isOnline(c)){

						ArrayList<IndicatorTable> tables = new ArrayList<IndicatorTable>(db.getTables());
						tables.add(db.getEpdsSurveyTable());
						tables.add(db.getStressSurveyTable());
						tables.add(db.getPreAppointmentSurveyTable());
						tables.add(db.getPostAppointmentSurveyTable());
						for (Iterator<IndicatorTable> iterator = tables.iterator(); iterator.hasNext();) {
							IndicatorTable t = iterator.next();
							try{
								if (t.isGetsUpdated()) {

									dbKey = db.open(dbKey);
									t.prepareTable(db.getDb());
									if((!(t instanceof SurveyTable) || !updatedResponses) && !(t instanceof LogTable)) {
										//get updates
										dbKey = db.open(dbKey);
										t.prepareTable(db.getDb());
										t.updateTable(t.getUpdateArray(UserPreferences.getLastUserId(c), childId, c));
										if(t instanceof SurveyTable) {
											updatedResponses = true;
										}
									}

									// upload unsynched
									dbKey = db.open(dbKey);
									t.prepareTable(db.getDb());
									db.upload(t, t.getUnSynchedCVs());

								}
							}
							catch(Exception e){
								Utilities.writeToWeeklyErrorLogFile(c, e);
								e.printStackTrace();
							}
						}
					}
					///
				}
				///
			} finally {
				isUpdating = false;
				Intent intent = new Intent(Utilities.ACTION_UPDATE_MAIN);
				c.sendBroadcast(intent);
			}
		}
	}

	public static void updateAllTablesButLog(Context c, Database db, StringBuilder sb) {
		if(!isUpdating) {
			isUpdating = true;
			try {
				for(Integer childId:UserPreferences.getKidIds(c)){
					boolean updatedResponses = false;
					if(WebUtils.isOnline(c)){

						ArrayList<IndicatorTable> tables = new ArrayList<IndicatorTable>(db.getTables());
						tables.add(db.getEpdsSurveyTable());
						tables.add(db.getStressSurveyTable());
						tables.add(db.getPreAppointmentSurveyTable());
						tables.add(db.getPostAppointmentSurveyTable());
						for (Iterator<IndicatorTable> iterator = tables.iterator(); iterator.hasNext();) {
							IndicatorTable t = iterator.next();

							try{
								if (t.isGetsUpdated() && !(t instanceof LogTable)) {
									dbKey = db.open(dbKey);
									t.prepareTable(db.getDb());

									if(!(t instanceof SurveyTable) || !updatedResponses) {
										//get updates
										dbKey = db.open(dbKey);
										t.prepareTable(db.getDb());
										t.updateTable(t.getUpdateArray(UserPreferences.getLastUserId(c), childId, c));

										updatedResponses = true;
									}

									// upload unsynched
									dbKey = db.open(dbKey);
									t.prepareTable(db.getDb());
									db.upload(t, t.getUnSynchedCVs());

								}
							}
							catch(Exception e){
								Utilities.writeToWeeklyErrorLogFile(c, e);
								e.printStackTrace();
							}
						}
					}
					///
				}
			} finally {
				isUpdating = false;
			}
		}
	}

	public static void checkOccasionallyDoneIndicators(Context context, final Database d) {
		Date startOfDay = DateUtils.getStartOfDay();

		long lastAlertedTime = UserPreferences.getLastAlertedTime(context);
		boolean hasAlertedYet = lastAlertedTime>=startOfDay.getTime();

		int currentHour = DateUtils.getHourOfDay();
		boolean isTimeYet = currentHour >= ReminderPreferences.getAlertTime(context);

		int quietStart = ReminderPreferences.getQuietTimeStart(context);
		int quietEnd = ReminderPreferences.getQuietTimeEnd(context);
		boolean isDuringQuietHours = ( (currentHour >= quietStart) && (currentHour <= quietEnd) );

		HashMap<Integer, ArrayList<String>> output = new HashMap<Integer, ArrayList<String>>();
		ArrayList<String> parentArrayString = new ArrayList<String>();
		for(Integer childId:UserPreferences.getKidIds(context)){
			String contentTitle =  "please update ";
			try {
				contentTitle += getBabyName(d, context,childId);
			} catch (NoUserException e1) {
				e1.printStackTrace();
			}
			if(isTimeYet && !isDuringQuietHours){

				ArrayList<String> kidArrayString = new ArrayList<String>();
				String[] tileNames = context.getResources().getStringArray(R.array.tile_names);    	
				IndicatorTable t;
				for(int i=0; i<tileNames.length; i++) {

					int targetId = childId;

					if (tileNames[i].equals(Tile.APPOINTMENTS)) {
						t = d.getAppointmentTable();
					}
					else if (tileNames[i].equals(Tile.DIAPERS)) {
						t = d.getDiaperTable();
					}
					else if (tileNames[i].equals(Tile.BONDING)) {
						t = d.getBondingTable();
					}
					else if (tileNames[i].equals(Tile.BABYMOODS)) {
						t = d.getBabyMoodTable();
					}
					else if (tileNames[i].equals(Tile.CUSTOMCHARTS)) {
						t = d.getGenericIndicatorTable();
					}
					else if (tileNames[i].equals(Tile.WEIGHT)) {
						t = d.getWeightTable();
					}
					else if (tileNames[i].equals(Tile.MYMOODS)) {
						t = d.getMoodMapTable();
						targetId = UserPreferences.getLastUserId(context);
					}
					else if (tileNames[i].equals(Tile.SURVEYS)) {
						t = d.getEpdsSurveyTable();
					} else {
						t = null;
					}
					try{
						if(t != null && t.isNeglected(targetId, context)){
							if(t.isParentLevel()){
								parentArrayString.add(tileNames[i]);
							} else {
								kidArrayString.add(tileNames[i]);
							}
						}
					}
					catch(Exception e){
						Utilities.writeToWeeklyErrorLogFile(context, e);
						e.printStackTrace();
					}
				}

				if(!kidArrayString.isEmpty()){
					showNotification(context, contentTitle, kidArrayString, childId, childId, !hasAlertedYet, !hasAlertedYet);
					UserPreferences.setLastAlertedTime(context);
				} else {
					cancelNotification(context, contentTitle, childId);

				}
			} else {
				cancelNotification(context, contentTitle, childId);
			}
		}

		String contentTitle = "Please update parent indicators";
		if(!parentArrayString.isEmpty()){
			output.put(UserPreferences.getLastUserId(context), parentArrayString);

			showNotification(context, contentTitle, parentArrayString, -1, UserPreferences.getLastUserId(context), !hasAlertedYet, !hasAlertedYet);
			UserPreferences.setLastAlertedTime(context);
		} else {
			cancelNotification(context, contentTitle, UserPreferences.getLastUserId(context));
		}

	}

	public static void performSpecialReminders(Context context, final Database d) {
		// special reminders
		ArrayList<SpecialReminder> srs = getSpecialReminders(d);

		// go through the special reminders
		for(Integer childId:UserPreferences.getKidIds(context)){
			for (SpecialReminder specialReminder : srs) {
				if(specialReminder.meetsCondition(context, childId)){
					List<Indicator> indicators = specialReminder.getIndicators();
					for (int i = 0; i < indicators.size(); i++) {
						makeNotification(context, specialReminder.getTitles().get(i), indicators.get(i).getId(), true, true, specialReminder.getTexts().get(i), specialReminder.getIntents().get(i));
					}
				}
			}
		}
	}

	public static ArrayList<SpecialReminder> getSpecialReminders(final Database d) {
		ArrayList<SpecialReminder> srs = new ArrayList<SpecialReminder>();


		// pre appointment reminder
		srs.add(new SpecialReminder(ReminderType.PRE_APPT_SURVEY){

			@Override
			public boolean evaluate(Context context, Integer childId) {

				int currentHour = DateUtils.getHourOfDay();

				int quietStart = ReminderPreferences.getQuietTimeStart(context);
				int quietEnd = ReminderPreferences.getQuietTimeEnd(context);
				boolean isDuringQuietHours = ( (currentHour >= quietStart) && (currentHour <= quietEnd) );

				if(!isDuringQuietHours) {
					int preAppointmentReminder = ReminderPreferences.getPreAppointmentReminder(context);
					Appointment[] nextAppointments = d.getAppointmentTable().getNextAppointments(childId, preAppointmentReminder);
					String title = context.getString(R.string.pre_appt_reminder);
					try {
						title += " for " + StringUtils.capitalize(getBabyName(d, context, childId));
					} catch (NoUserException nue) {
					}

					for(Appointment nextAppointment: nextAppointments) {
						if(nextAppointment != null) {
							// make sure you haven't already completed the survey
							Survey s = d.getPreAppointmentSurveyTable().getSurveyForBaby(childId, nextAppointment.getId());
							if (s == null) {
								Log l = new Log(new CommonData(UserPreferences.getLastUserId(context),childId), SurveyType.PRE_APPT.name(), title, nextAppointment.getId().toString());

								Indicator lastLogForLog = d.getLogTable().getLastLogForLog(l);

								Date combineDateAndTime = DateUtils.combineDateAndTime(nextAppointment.getDate(), nextAppointment.getStartTime());
								int hoursUntil = DateUtils.getHoursUntil(combineDateAndTime);

								if(lastLogForLog == null && hoursUntil <= preAppointmentReminder){

									try {
										Intent intent = new Intent(context, SurveyForm.class);
										intent.putExtra(SurveyForm.TITLE, "Pre-Appt Survey");
										intent.putExtra(TileActivity.BABY, d.getBabyTable().getBaby(childId, context));
										intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.PRE_APPT.name());
										intent.putExtra(SurveyForm.INDICATOR_ID, nextAppointment.getId());
										intent.setAction(Utilities.ACTION_NOTIFY);
										intent.addCategory(Intent.CATEGORY_DEFAULT);
										intent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);
										d.insertSingle(l, false, false);

										this.addText("Please answer some questions about " + StringUtils.capitalize(d.getBabyTable().getBaby(childId, context).getName()) + "'s upcoming appointment");
										this.addTitle(title);
										this.addIndicator(nextAppointment);
										this.addIntent(intent);
									} catch (NoUserException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
					if(this.getIndicators().size() > 0){
						return true;
					}
				}
				return false;
			}
		});

		// post appointment reminder
		srs.add(new SpecialReminder(ReminderType.POST_APPT_SURVEY){
			@Override
			public boolean evaluate(Context context, Integer childId) {

				int currentHour = DateUtils.getHourOfDay();

				int quietStart = ReminderPreferences.getQuietTimeStart(context);
				int quietEnd = ReminderPreferences.getQuietTimeEnd(context);
				boolean isDuringQuietHours = ( (currentHour >= quietStart) && (currentHour <= quietEnd) );

				if(!isDuringQuietHours) {
					int postAppointmentReminder = -1*ReminderPreferences.getPostAppointmentReminderStart(context);
					int maxWindow = ReminderPreferences.getPostAppointmentReminderStop(context);
					Appointment[] nextAppointments = d.getAppointmentTable().getNextAppointments(childId, postAppointmentReminder);
					String title = context.getString(R.string.post_appt_reminder);
					try {
						title += " for " + StringUtils.capitalize(getBabyName(d, context, childId));
					} catch (NoUserException nue) {
					}

					for(Appointment nextAppointment: nextAppointments) {
						if(nextAppointment != null) {
							// make sure you haven't already completed the survey
							Survey s = d.getPreAppointmentSurveyTable().getSurveyForBaby(childId, nextAppointment.getId());
							if (s == null) {
								Log l = new Log(new CommonData(UserPreferences.getLastUserId(context),childId), SurveyType.POST_APPT.name(), title, nextAppointment.getId().toString());

								Indicator lastLogForLog = d.getLogTable().getLastLogForLog(l);

								Date combineDateAndTime = DateUtils.combineDateAndTime(nextAppointment.getDate(), nextAppointment.getStartTime());
								int hoursUntil = DateUtils.getHoursUntil(combineDateAndTime);

								if(lastLogForLog == null && ((hoursUntil <= postAppointmentReminder) && (postAppointmentReminder <= maxWindow))) {

									try {
										Intent intent = new Intent(context, SurveyForm.class);
										intent.putExtra(SurveyForm.TITLE, "Post-Appt Survey");
										intent.putExtra(TileActivity.BABY, d.getBabyTable().getBaby(childId, context));
										intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.POST_APPT.name());
										intent.putExtra(SurveyForm.INDICATOR_ID, nextAppointment.getId());
										intent.setAction(Utilities.ACTION_NOTIFY);
										intent.addCategory(Intent.CATEGORY_DEFAULT);
										intent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);
										d.insertSingle(l, false, false);

										this.addText("Please answer some questions about " + StringUtils.capitalize(d.getBabyTable().getBaby(childId, context).getName()) + "'s recent appointment");
										this.addTitle(title);
										this.addIndicator(nextAppointment);
										this.addIntent(intent);
									} catch (NoUserException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
					if(this.getIndicators().size() > 0){
						return true;
					}
				}
				return false;
			}
		});

		// appointment reminder
		srs.add(new SpecialReminder(ReminderType.APPOINTMENT){
			@Override
			public boolean evaluate(Context context, Integer childId) {
				Appointment nextAppointment = d.getAppointmentTable().getNextAppointment(childId);
				if(nextAppointment != null) {
					String title = context.getString(R.string.appt_reminder);
					try {
						title += " for " + StringUtils.capitalize(getBabyName(d, context, childId));
					} catch (NoUserException nue) {
					}

					// check to see if it's within two hours of now
					Date apptDateTime = DateUtils.combineDateAndTime(nextAppointment.getDate(), nextAppointment.getStartTime());
					int appointmentReminder = -1*ReminderPreferences.getAppointmentReminder(context);
					int hoursUntil = DateUtils.getHoursUntil(apptDateTime);

					Log l = new Log(new CommonData(UserPreferences.getLastUserId(context),childId), Tile.APPOINTMENTS, title, nextAppointment.getId().toString());

					if (hoursUntil <= appointmentReminder) {
						try {
							Intent intent = Utilities.getIntentForContentTitle(context, Tile.APPOINTMENTS, childId);
							d.insertSingle(l, false, false);

							this.addText(StringUtils.capitalize(getBabyName(d, context, childId)) + " has an appt today at " + DateUtils.formatTimeAsAMPM(nextAppointment.getStartTime()));
							this.addTitle(title);
							this.addIndicator(nextAppointment);
							this.addIntent(intent);
						} catch (NoUserException e) {
							e.printStackTrace();
						}
					}
					if(this.getIndicators().size() > 0){
						return true;
					}
				}
				return false;
			}
		});

		// bonding ignored reminder
		srs.add(new SpecialReminder(ReminderType.BONDING){
			@Override
			public boolean evaluate(Context context, Integer childId) {

				int currentHour = DateUtils.getHourOfDay();
				boolean isTimeYet = DateUtils.getHourOfDay() >= ReminderPreferences.getAlertTime(context);

				int quietStart = ReminderPreferences.getQuietTimeStart(context);
				int quietEnd = ReminderPreferences.getQuietTimeEnd(context);
				boolean isDuringQuietHours = ( (currentHour >= quietStart) && (currentHour <= quietEnd) );

				if(isTimeYet && !isDuringQuietHours) {
					int postAppointmentReminder = -1*ReminderPreferences.getBondingNeglectedTime(context);
					BondingSurvey bonding = (BondingSurvey)d.getBondingTable().getLastBabyIndicator(childId, null);
					String title = "Bonding Reminder";
					try {
						title += " for " + getBabyName(d, context, childId);
					} catch (NoUserException nue) {
					}
					if (bonding != null) {
						Log l = new Log(new CommonData(UserPreferences.getLastUserId(context),childId), SurveyType.BONDING.name(), title, bonding.getId().toString());

						Indicator lastLogForLog = d.getLogTable().getLastLogForLog(l);

						int hoursUntil = DateUtils.getHoursUntil(bonding.getDateTime());

						if(lastLogForLog == null && hoursUntil <= postAppointmentReminder){

							try {
								Intent intent = Utilities.getIntentForContentTitle(context, Tile.BONDING, childId);

								d.insertSingle(l, false, false);
								this.addText("You have not recorded bonding with " + StringUtils.capitalize(getBabyName(d, context, childId)) + " in "  + -1*postAppointmentReminder + " hours.");

								this.addTitle(title);
								this.addIndicator(bonding);
								this.addIntent(intent);
							} catch (NoUserException e) {
								e.printStackTrace();
							}
						}

						if(this.getIndicators().size() > 0){
							return true;
						}
					}
				}
				return false;
			}
		});

		return srs;
	}

	private static String getBabyName(final Database d, Context context,
			Integer childId) throws NoUserException {
		return StringUtils.capitalize(d.getBabyTable().getBaby(childId, context).getName());
	}

	/**
	 * Show a notification while this service is running.
	 */
	public static void showNotification(Context context, String contentTitle,
			ArrayList<String> listOfTitles, int childId, int notificationId, boolean shouldMakeNoise, boolean shouldVibrate) {
		StringBuilder sb = new StringBuilder();
		for(String s: listOfTitles){
			sb.append(s);
			sb.append(",");
		}
		sb.setCharAt(sb.length()-1, '.');

		String text = sb.toString();

		Intent intent = Utilities.getIntentForContentTitle(context, listOfTitles.get(0), childId);

		makeNotification(context, contentTitle, childId, shouldMakeNoise,
				shouldVibrate, text, intent);
	}

	private static void makeNotification(Context context, String contentTitle,
			int notificationId, boolean shouldMakeNoise, boolean shouldVibrate,
			String text, Intent intent) {
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, contentTitle, text, contentIntent);
		notification.defaults = Notification.DEFAULT_LIGHTS;
		if(shouldMakeNoise){
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if(shouldVibrate){
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		notification.ledARGB = 0xFFff0000;
		notification.ledOffMS = 300;
		notification.ledOnMS = 300; 
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// Send the notification.
		// We use a layout id because it is a unique number.  We use it later to cancel.
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(contentTitle, notificationId, notification);
	}

	public static void cancelNotification(Context context, String tag, int notificationId) {
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(tag, notificationId);
	}
}