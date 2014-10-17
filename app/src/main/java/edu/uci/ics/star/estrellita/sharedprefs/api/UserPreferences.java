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

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class UserPreferences extends PreferencesApi{

	public static void storeLastUser(Context context, User user) {

		Editor editor = PreferencesApi.getPrefsForEdit(context);
		editor.putInt(PreferencesApi.LAST_USER, user.getCommonData().getId());
		StringBuilder sb = new StringBuilder();
		for(Baby k:user.getKids()){
			sb.append(k.getId());
			sb.append(Utilities.DEFAULT_DELIMITER);
		}
		editor.putString(PreferencesApi.LAST_USER_KIDS, sb.toString());
		editor.commit();
	}


	public static int getLastUserId(Context context){
		return getPrefs(context).getInt(PreferencesApi.LAST_USER, -1);
	}

	public static boolean isFirstRun(Context context){
		return (getPrefs(context).getInt(PreferencesApi.LAST_USER, -1) == -1);
	}

	public static long getLastAlertedTime(Context context){
		return getPrefs(context).getLong(PreferencesApi.LAST_ALERTED_TIME, 0);
	}

	public static void setLastAlertedTime(Context context) {
		long timeInMillis = DateUtils.getTimeInMillis();
		PreferencesApi.putLong(context, PreferencesApi.LAST_ALERTED_TIME, timeInMillis);
	}

	public static ArrayList<Integer> getKidIds(Context context){
		ArrayList<Integer> kidIds = new ArrayList<Integer>();

		String string = getPrefs(context).getString(PreferencesApi.LAST_USER_KIDS, null);
		if(string != null){
			String[] ssplit = string.split(Utilities.DEFAULT_DELIMITER);

			for(String s: ssplit){
				kidIds.add(Integer.parseInt(s));
			}
		}
		return kidIds;
	}

	public static void saveWidgetKidPref(Context context, int appWidgetId, int kidId) {
		PreferencesApi.putInt(context, PreferencesApi.WIDGET_PREF_PREFIX_KEY + appWidgetId, kidId);
	}

	// Read the prefix from the SharedPreferences object for this widget.
	// If there is no preference saved, get the default from a resource
	public static int loadWidgetKidPref(Context context, int appWidgetId) {
		return getPrefs(context).getInt(PreferencesApi.WIDGET_PREF_PREFIX_KEY + appWidgetId, -1);
	}
	
	public static boolean removeWidgetKidPref(Context context, int appWidgetId) {
		return PreferencesApi.removePref(context, PreferencesApi.WIDGET_PREF_PREFIX_KEY + appWidgetId);
	}
	
	public static void saveWidgetTypePref(Context context, int appWidgetId, String type) {
		PreferencesApi.putString(context, PreferencesApi.WIDGET_TYPE_PREFIX_KEY + appWidgetId, type);
	}

	public static String loadWidgetTypePref(Context context, int appWidgetId) {
		return getPrefs(context).getString(PreferencesApi.WIDGET_TYPE_PREFIX_KEY + appWidgetId, "");
	}
	
	public static boolean removeWidgetTypePref(Context context, int appWidgetId) {
		return PreferencesApi.removePref(context, PreferencesApi.WIDGET_TYPE_PREFIX_KEY + appWidgetId);
	}
	
	

}
