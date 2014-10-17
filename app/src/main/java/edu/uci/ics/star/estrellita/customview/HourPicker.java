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

import android.content.Context;
import android.widget.TimePicker;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class HourPicker extends TimePicker {
	int mInitialHourOfDay;
	boolean mIs24HourView;

	public HourPicker(Context context) {
		this (context, DateUtils.getTimestamp().getHours(), false);
	}
	
	public HourPicker(Context context, int  hourOfDay) {
		this (context, hourOfDay, false);
	}

	/**
	 * @param context Parent.
	 * @param callBack How parent is notified.
	 * @param hourOfDay The initial hour.
	 * @param minute The initial minute.
	 * @param is24HourView Whether this is a 24 hour view, or AM/PM.
	 */
	public HourPicker(Context context, int hourOfDay, boolean is24HourView) {
		super(context);
		mInitialHourOfDay = hourOfDay;
		mIs24HourView = is24HourView;

		// initialize state
		setCurrentHour(mInitialHourOfDay);
		setCurrentMinute(0);
		setIs24HourView(mIs24HourView);
		setOnTimeChangedListener(mOnTimeChangedListener);
	}

	private OnTimeChangedListener mOnTimeChangedListener = new OnTimeChangedListener() {
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			setOnTimeChangedListener(mNullOnTimeChangedListener);

			setCurrentHour(hourOfDay);
			setCurrentMinute(0);

			setOnTimeChangedListener(mOnTimeChangedListener);
		}
	};

	private OnTimeChangedListener mNullOnTimeChangedListener = new OnTimeChangedListener() {
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		}
	};
}
