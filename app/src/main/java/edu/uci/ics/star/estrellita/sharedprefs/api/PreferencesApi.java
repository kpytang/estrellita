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

package edu.uci.ics.star.estrellita.sharedprefs.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesApi {
	public static final String CONTENT_TABLE_NAMES = "contentTableNames";
	public static final String CONTENT_TITLES = "contentTitles";
	public static final String COMMENT_ID = "comment_id";
	public static final String COMMENT_POST = "comment_post";
	public static final String COMMENT_AUTHOR = "comment_author";
	public static final String COMMENT_ID_LOCAL = "comment_id_local";
	
	public static final String LAST_USER = "lastUser";
	public static final String LAST_USER_KIDS = "lastUserKids";
	public static final String LAST_ALERTED_TIME = "lastAlertedTime";
	public static final String WIDGET_PREF_PREFIX_KEY = "widget_kid_";

	public static final String WIDGET_TYPE_PREFIX_KEY = "widget_style_";
	public static final String MY_TIME_PREFERENCE = "my_time_preference";
	
	public static final String WEIGHT_DAYOFWEEK_START_PREFERENCE = "weight_dayofweek_start_preference";
	public static final String WEIGHT_DAYOFWEEK_END_PREFERENCE = "weight_dayofweek_end_preference";
	public static final String SURVEY_DAYOFMONTH_PREFERENCE = "survey_dayofmonth_preference";
	public static final String APPOINTMENT_REMINDER_PREFERENCE = "appointment_reminder";
	public static final String APPOINTMENT_REMINDER_HOURS_BEFORE_PREFERENCE = "appointment_reminder_hours_before";
	public static final String APPOINTMENT_REMINDER_HOURS_AFTER_PREFERENCE_START = "appointment_reminder_hours_after_start";
	public static final String APPOINTMENT_REMINDER_HOURS_AFTER_PREFERENCE_STOP = "appointment_reminder_hours_after_stop";
	public static final String BONDING_NEGLECTED_HOURS_PREFERENCE = "bonding_neglected_hours_preference";

	public static final String SYNC_SERVICE_ALERT_TIME = "sync_service_alert_time";
	public static final String SYNC_SERVICE_QUIET_TIME_START = "sync_service_quiet_time_start";
	public static final String SYNC_SERVICE_QUIET_TIME_END = "sync_service_quiet_time_end";
	
	public static final String NEED_TO_UPLOAD_ERROR_LOG = "need_to_upload_error_log";

	
	public static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static SharedPreferences.Editor getPrefsForEdit(Context context) {
		return getPrefs(context).edit();
	}
	
	public static boolean getBoolean(Context context, String key){
		return getPrefs(context).getBoolean(key, false);
	}
	
	public static float getFloat(Context context, String key){
		return getPrefs(context).getFloat(key, -1);
	}
	
	public static int getInt(Context context, String key){
		return getPrefs(context).getInt(key, -1);
	}
	
	public static long getLong(Context context, String key){
		return getPrefs(context).getLong(key, -1);
	}
	
	public static String getString(Context context, String key){
		return getPrefs(context).getString(key, null);
	}
	
	public static void putInt(Context context, String key, int i){
		getPrefsForEdit(context).putInt(key, i).commit();
	}

	public static void putBoolean(Context context, String key, boolean i){
		getPrefsForEdit(context).putBoolean(key, i).commit();
	}
	
	public static void putFloat(Context context, String key, float i){
		getPrefsForEdit(context).putFloat(key, i).commit();
	}
	
	public static void putLong(Context context, String key, long i){
		getPrefsForEdit(context).putLong(key, i).commit();
	}
	
	public static void putString(Context context, String key, String i){
		getPrefsForEdit(context).putString(key, i).commit();
	}
	
	public static boolean removePref(Context context, String key) {
		return getPrefsForEdit(context).remove(key).commit();
	}
}