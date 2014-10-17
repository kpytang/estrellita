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

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class ReminderPreferences extends PreferencesApi {

	public static void setIntialValues(Context context) {
		setInitialWeightDate(context);
		setInitialApptReminderBuffer(context);
		setInitialBondingNeglectedReminderBuffer(context);
		setInitialAlertTime(context);
		setInitialQuietTimes(context);
	}

	public static void setInitialWeightDate(Context context) {
		if(PreferencesApi.getInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_START_PREFERENCE) == -1){
			PreferencesApi.putInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_START_PREFERENCE, Calendar.SUNDAY);
			PreferencesApi.putInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_END_PREFERENCE, Calendar.THURSDAY);
		}
	}

	public static int getStartWeightDate(Context context) {
		return PreferencesApi.getInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_START_PREFERENCE);
	}

	public static int getEndWeightDate(Context context) {
		return PreferencesApi.getInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_END_PREFERENCE);
	}

	public static void setStartWeightDate(Context context, int dayOfWeek) {
		PreferencesApi.putInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_START_PREFERENCE, dayOfWeek);
	}

	public static void setEndWeightDate(Context context, int dayOfWeek) {
		PreferencesApi.putInt(context, PreferencesApi.WEIGHT_DAYOFWEEK_END_PREFERENCE, dayOfWeek);
	}

	public static Date getInitialInstallDate(Context context) {
		User user = EstrellitaTiles.getParentUser(context);
		if (user != null) {
			return user.getCommonData().getTimestamp();
		}
		else {
			return DateUtils.getEarliestDate();
		}
	}

	public static void setInitialSurveyDate(Context context) {
		if(PreferencesApi.getLong(context, PreferencesApi.SURVEY_DAYOFMONTH_PREFERENCE) == -1){
			PreferencesApi.putLong(context, PreferencesApi.SURVEY_DAYOFMONTH_PREFERENCE, ReminderPreferences.getInitialInstallDate(context).getTime());
		}
	}

	public static Date getInitialSurveyDate(Context context) {
		long millis = PreferencesApi.getLong(context, PreferencesApi.SURVEY_DAYOFMONTH_PREFERENCE);
		return new Date(millis);
	}

	public static void setInitialApptReminderBuffer(Context context) {
		if(PreferencesApi.getInt(context, PreferencesApi.APPOINTMENT_REMINDER_HOURS_BEFORE_PREFERENCE) == -1){
			PreferencesApi.putInt(context, PreferencesApi.APPOINTMENT_REMINDER_HOURS_BEFORE_PREFERENCE, 48);
			PreferencesApi.putInt(context, PreferencesApi.APPOINTMENT_REMINDER_HOURS_AFTER_PREFERENCE_START, 6);
			PreferencesApi.putInt(context, PreferencesApi.APPOINTMENT_REMINDER_HOURS_AFTER_PREFERENCE_STOP, 168);
			PreferencesApi.putInt(context, PreferencesApi.APPOINTMENT_REMINDER_PREFERENCE, 2);	
		}
	}

	public static int getPreAppointmentReminder(Context context) {
		return PreferencesApi.getInt(context,  PreferencesApi.APPOINTMENT_REMINDER_HOURS_BEFORE_PREFERENCE);
	}

	public static int getPostAppointmentReminderStart(Context context) {
		return PreferencesApi.getInt(context,  PreferencesApi.APPOINTMENT_REMINDER_HOURS_AFTER_PREFERENCE_START);
	}

	public static int getPostAppointmentReminderStop(Context context) {
		return PreferencesApi.getInt(context,  PreferencesApi.APPOINTMENT_REMINDER_HOURS_AFTER_PREFERENCE_STOP);
	}

	public static int getAppointmentReminder(Context context) {
		return PreferencesApi.getInt(context,  PreferencesApi.APPOINTMENT_REMINDER_PREFERENCE);
	}

	public static void setInitialBondingNeglectedReminderBuffer(Context context) {
		if(PreferencesApi.getInt(context, PreferencesApi.BONDING_NEGLECTED_HOURS_PREFERENCE) == -1){
			PreferencesApi.putInt(context, PreferencesApi.BONDING_NEGLECTED_HOURS_PREFERENCE, 48);
		}
	}

	public static int getBondingNeglectedTime(Context context) {
		return PreferencesApi.getInt(context, PreferencesApi.APPOINTMENT_REMINDER_HOURS_BEFORE_PREFERENCE);
	}

	public static int getAlertTime(Context context) {
		return PreferencesApi.getInt(context, PreferencesApi.SYNC_SERVICE_ALERT_TIME);
	}

	public static void setInitialAlertTime(Context context) {
		if(PreferencesApi.getInt(context, PreferencesApi.SYNC_SERVICE_ALERT_TIME) == -1){
			PreferencesApi.putInt(context, PreferencesApi.SYNC_SERVICE_ALERT_TIME, 20);
		}
	}
	
	public static void setInitialQuietTimes(Context context) {
		if(PreferencesApi.getInt(context, PreferencesApi.SYNC_SERVICE_QUIET_TIME_START) == -1){
			PreferencesApi.putInt(context, PreferencesApi.SYNC_SERVICE_QUIET_TIME_START, 22);
			PreferencesApi.putInt(context, PreferencesApi.SYNC_SERVICE_QUIET_TIME_END, 8);
		}
	}
	
	public static int getQuietTimeStart(Context context) {
		return PreferencesApi.getInt(context, PreferencesApi.SYNC_SERVICE_QUIET_TIME_START);
	}
	
	public static int getQuietTimeEnd(Context context) {
		return PreferencesApi.getInt(context, PreferencesApi.SYNC_SERVICE_QUIET_TIME_END);
	}
}