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

package edu.uci.ics.star.estrellita.widget;

import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.db.table.indicator.AppointmentTable;
import edu.uci.ics.star.estrellita.db.table.indicator.BabyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.BondingSurveyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.PostTable;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.Baby.Gender;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.Post;
import edu.uci.ics.star.estrellita.sharedprefs.api.UserPreferences;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class MainWidgetProviderWide extends AppWidgetProvider {
	private static final int PINK = 0xfff9c3d2;
	private static final int BLUE = 0xff00aacc;
	private static final int MAX_CHARACTER_LENGTH_FOR_STATUS = 170;

	private PostTable pt;
	private BondingSurveyTable bt;
	private AppointmentTable at;
	private BabyTable babyTable;
	private double dbKey = -1;

	public MainWidgetProviderWide(){
		super();
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	public void onReceive(Context context, Intent intent){

		/*
		 * There is a bug in android 1.5 that the onDeleted method is not called. 
		 * The code below placed in the onRecive fixes the problem.	
		 */
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt
			(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else if (Utilities.ACTION_UPDATE_WIDE.equals(action)){
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			int[] a = manager.getAppWidgetIds(new ComponentName(Utilities.EDU_UCI_ICS_STAR_ESTRELLITA, Utilities.EDU_UCI_ICS_STAR_ESTRELLITA_WIDGET_MAIN_WIDGET_PROVIDER_WIDE));
			if(a.length > 0){
				onUpdate(context, manager, a);
			}
		} else {
			super.onReceive(context, intent);
		}
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onDeleted(android.content.Context, int[])
	 */
	public void onDeleted(Context context, int[] appWidgetIds) {
		//		timer.cancel();
		super.onDeleted(context, appWidgetIds);
		for(int appWidgetId: appWidgetIds){
			UserPreferences.removeWidgetTypePref(context, appWidgetId);
			UserPreferences.removeWidgetKidPref(context, appWidgetId);
		}
	}

	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		Database db = new Database(context);
		for (int appWidgetId:appWidgetIds) {
			if(UserPreferences.loadWidgetTypePref(context, appWidgetId).equals("4x2")){
				updateAppWidget(context, appWidgetManager, appWidgetId, db);
			}
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}


	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId, Database db) {


		PendingIntent pendingIntent = getPendingContentIntent(context, "last", appWidgetId);

		// attach on-click listeners to the buttons
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.appwidget_provider_layout_wide);
		views.setOnClickPendingIntent(R.id.widget_body, pendingIntent);
		views.setOnClickPendingIntent(R.id.widget_appt_area, 
				getPendingContentIntent(context, Tile.APPOINTMENTS, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_bottom_area,
				getPendingContentIntent(context, Tile.BONDING, appWidgetId));

		views.setOnClickPendingIntent(R.id.widget_appt_inbox,
				getPendingContentIntent(context, Tile.WALL, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_hotspot_diaper,
				getPendingContentIntent(context, Tile.DIAPERS, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_hotspot_babymood,
				getPendingContentIntent(context, Tile.BABYMOODS, appWidgetId));
		views.setOnClickPendingIntent(R.id.widget_hotspot_mymood,
				getPendingContentIntent(context, Tile.MYMOODS, appWidgetId));
		
		updateFromDB(context, views, appWidgetId, db);

		// Tell the AppWidgetManager to perform an update on the current App Widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
		
	}

	private void updateFromDB(Context context, RemoteViews views, int appWidgetId, Database db) {

		int numComments = 0;
		String newStatus = "xx";

		int loadWidgetKidPref = UserPreferences.loadWidgetKidPref(context, appWidgetId);

		try{
			dbKey = db.open(dbKey);
			pt = db.getPostTable();
			
			babyTable = db.getBabyTable();
			//			db.updateTables(loadWidgetKidPref);
			Post lastPost = pt.getMostRecentPost(loadWidgetKidPref);
			if(lastPost != null){
				numComments = pt.getNumCommentsForPost(lastPost.getId());
				newStatus = lastPost.getModifiedMsg(babyTable.getBaby(lastPost.getCommonData().getIdBaby(), context));
			}
			views.setTextViewText(R.id.widget_name_tag, StringUtils.capitalize(babyTable.getBaby(loadWidgetKidPref, context).getName()));
			if (babyTable.getBaby(loadWidgetKidPref, context).getGender() == Gender.FEMALE) {
				views.setImageViewBitmap(R.id.widget_bgcolor, ImageUtils.getBackground(PINK));
			}
			else {
				views.setImageViewBitmap(R.id.widget_bgcolor, ImageUtils.getBackground(BLUE));
			}

			views.setTextViewText( R.id.widget_last_post, StringUtils.shortenStringWithEllipsis(newStatus, MAX_CHARACTER_LENGTH_FOR_STATUS));

			Log log = (Log) db.getLogTable().getLastLogForPost(babyTable.getBaby(loadWidgetKidPref, context).getId());
			Date d = DateUtils.getEarliestDate();
			if (log != null) {
				d = log.getCommonData().getTimestamp();
			}
			Integer postCount = pt.getPostCountSince(babyTable.getBaby(loadWidgetKidPref, context).getId(), d);
			if (postCount == 0) {
				views.setViewVisibility(R.id.widget_inbox_count_holder, View.GONE);
				views.setViewVisibility(R.id.widget_inbox_exclaim, View.GONE);
			}
			else {
				views.setViewVisibility(R.id.widget_inbox_count_holder, View.VISIBLE);
				views.setTextViewText( R.id.widget_inbox_count,  postCount.toString());
				views.setViewVisibility(R.id.widget_inbox_exclaim, View.VISIBLE);
			}

		} catch(ArrayIndexOutOfBoundsException aioobe){

		} catch (NoUserException e) {
			e.printStackTrace();
		}
		dbKey = db.open(dbKey);
		at = db.getAppointmentTable();
		String nextAppt;
		try{
			Appointment nextAppointment = at.getNextAppointment(loadWidgetKidPref);
			int daysTillApt = -1*DateUtils.getDaysPast(nextAppointment.getDate());
			if(daysTillApt == 0){
				int hrs = DateUtils.getHoursUntil(DateUtils.combineDateAndTime(nextAppointment.getDate(),nextAppointment.getStartTime()));
				if (hrs == 0) {
					int  mins = DateUtils.getMinutesUntil(DateUtils.combineDateAndTime(nextAppointment.getDate(),nextAppointment.getStartTime()));
					nextAppt = " in " + StringUtils.pluralize(mins, "min", "mins");
				}
				else {
					nextAppt = " in " + StringUtils.pluralize(hrs, "hr", "hrs");
				}
			} else if(daysTillApt < 0){
				nextAppt = " None";
			} else {
				nextAppt = " in " + StringUtils.pluralize(daysTillApt, "day", "days");
			}
		} catch(NullPointerException npe){
			nextAppt = "None";
		}

		views.setTextViewText( R.id.widget_next_appt_time,nextAppt);
		views.setTextViewText( R.id.widget_reply_count, Integer.toString(numComments));

		dbKey = db.open(dbKey);
		bt = db.getBondingTable();
		BondingSurvey bonding = (BondingSurvey)bt.getLastBabyIndicatorForToday(loadWidgetKidPref);
		db.close(dbKey);
		dbKey = -1;

		if(bonding != null && bonding.hasCompletedReading()){
			views.setImageViewResource(R.id.widget_icon_reading, R.drawable.reading_icon);
			views.setImageViewResource(R.id.widget_icon_reading_back, R.drawable.yellowfull);
		} else {
			views.setImageViewResource(R.id.widget_icon_reading, R.drawable.reading_icon_disabled);
			views.setImageViewResource(R.id.widget_icon_reading_back, R.drawable.grayfull);
		}
		if(bonding != null && bonding.hasCompletedSinging()){
			views.setImageViewResource(R.id.widget_icon_singing, R.drawable.singing_icon);
			views.setImageViewResource(R.id.widget_icon_singing_back, R.drawable.yellowfull);
		} else {
			views.setImageViewResource(R.id.widget_icon_singing, R.drawable.singing_icon_disabled);
			views.setImageViewResource(R.id.widget_icon_singing_back, R.drawable.grayfull);
		}
		if(bonding != null && bonding.hasCompletedTalking()){
			views.setImageViewResource(R.id.widget_icon_talking, R.drawable.talking_icon);
			views.setImageViewResource(R.id.widget_icon_talking_back, R.drawable.yellowfull);
		} else {
			views.setImageViewResource(R.id.widget_icon_talking, R.drawable.talking_icon_disabled);
			views.setImageViewResource(R.id.widget_icon_talking_back, R.drawable.grayfull);
		}
		if(bonding != null && bonding.hasCompletedTummyTime()){
			views.setImageViewResource(R.id.widget_icon_tummytime, R.drawable.tummytime_icon);
			views.setImageViewResource(R.id.widget_icon_tummytime_back, R.drawable.yellowfull);
		} else {
			views.setImageViewResource(R.id.widget_icon_tummytime, R.drawable.tummytime_icon_disabled);
			views.setImageViewResource(R.id.widget_icon_tummytime_back, R.drawable.grayfull);
		}
		if(bonding != null && bonding.hasCompletedWalking()){
			views.setImageViewResource(R.id.widget_icon_walking, R.drawable.walking_icon);
			views.setImageViewResource(R.id.widget_icon_walking_back, R.drawable.yellowfull);
		} else {
			views.setImageViewResource(R.id.widget_icon_walking, R.drawable.walking_icon_disabled);
			views.setImageViewResource(R.id.widget_icon_walking_back, R.drawable.grayfull);
		}
	}

	private static PendingIntent getPendingContentIntent(Context context, String title, int appWidgetId){
		Intent intent = Utilities.getIntentForContentTitle(context, title, 
				UserPreferences.loadWidgetKidPref(context, appWidgetId));

		return PendingIntent.getActivity(context, 0, intent, 0);		
	}

}