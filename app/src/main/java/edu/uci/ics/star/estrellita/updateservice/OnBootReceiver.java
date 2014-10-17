/***
	Copyright (c) 2009-10 CommonsWare, LLC

	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class OnBootReceiver extends BroadcastReceiver {
	private static final int PERIOD=1800000; 	// 30 mins
	private static boolean isAlarmSet = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// do a test to see if we want to start the add the alarm
		// e.g. if setting is set to do this
		if(!isAlarmSet) {
			startAlarm(context);
		}
	}

	/**
	 * @param context
	 */
	public static void startAlarm(Context context){
		AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i=new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0,
				i, 0);
		
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				//SystemClock.elapsedRealtime()+60000,
				SystemClock.elapsedRealtime()+1000,
				PERIOD,
				pi);
		isAlarmSet = true;
	}

	/**
	 * @param context
	 */
	public static void cancelAlarm(Context context){
		AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i=new Intent(context, OnAlarmReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(context, 0,
				i, 0);

		mgr.cancel(pi);
		isAlarmSet = false;
	}
}
