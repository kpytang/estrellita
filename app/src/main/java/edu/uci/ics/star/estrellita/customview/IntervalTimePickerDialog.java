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

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import edu.uci.ics.star.estrellita.R;

public class IntervalTimePickerDialog extends AlertDialog implements OnClickListener {
	/**
	 * The callback interface used to indicate the user is done filling in
	 * the time (they clicked on the 'Set' button).
	 */
	public interface OnTimeSetListener {
		/**
		 * @param view The view associated with this listener.
		 * @param hourOfDay The hour that was set.
		 * @param minute The minute that was set.
		 */
		void onTimeSet(TimePicker view, int hourOfDay, int minute);
	}	

	private static final String HOUR = "hour";
	private static final String MINUTE = "minute";
	private static final String IS_24_HOUR = "is24hour";
	private static final int INTERVAL_SIZE = 15;

	private final TimePicker mTimePicker;
	private final OnTimeSetListener mCallback;
	private final Calendar mCalendar;
	private final java.text.DateFormat mDateFormat;

	int mInitialHourOfDay;
	int mInitialMinute;
	boolean mIs24HourView;
	int mPrevMinute;
	int[] mIntervals = {0, 15, 30, 45};

	public IntervalTimePickerDialog(Context context, int  hourOfDay, int minute) {
		this (context, null, hourOfDay, minute, false);
	}

	/**
	 * @param context Parent.
	 * @param callBack How parent is notified.
	 * @param hourOfDay The initial hour.
	 * @param minute The initial minute.
	 * @param is24HourView Whether this is a 24 hour view, or AM/PM.
	 */
	public IntervalTimePickerDialog(Context context, OnTimeSetListener callBack,
			int hourOfDay, int minute, boolean is24HourView) {
		super(context);
		mCallback = callBack;
		mInitialHourOfDay = hourOfDay;
		mInitialMinute = minute;
		mIs24HourView = is24HourView;
		mPrevMinute = mInitialMinute;

		mDateFormat = DateFormat.getTimeFormat(context);
		mCalendar = Calendar.getInstance();
		updateTitle(mInitialHourOfDay, mInitialMinute);

		setCanceledOnTouchOutside(true);
		setButton(BUTTON_POSITIVE, "Set", this);
		setButton(BUTTON_NEGATIVE, "Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});
		setIcon(R.drawable.ic_dialog_time);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.time_picker_dialog, null);
		setView(view);

		mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);

		// initialize state
		mTimePicker.setCurrentHour(mInitialHourOfDay);
		mTimePicker.setCurrentMinute(mInitialMinute);
		mTimePicker.setIs24HourView(mIs24HourView);
		mTimePicker.setOnTimeChangedListener(mOnTimeChangedListener);
	}

	public void onClick(DialogInterface dialog, int which) {
		if (mCallback != null) {
			mTimePicker.clearFocus();
			mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
		}
	}

	public void updateTime(int hourOfDay, int minuteOfHour) {
		mTimePicker.setCurrentHour(hourOfDay);
		mTimePicker.setCurrentMinute(minuteOfHour);
	}

	private void updateTitle(int hour, int minute) {
		mCalendar.set(Calendar.HOUR_OF_DAY, hour);
		mCalendar.set(Calendar.MINUTE, minute);
		setTitle(mDateFormat.format(mCalendar.getTime()));
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Dialog#onSaveInstanceState()
	 */
	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt(HOUR, mTimePicker.getCurrentHour());
		state.putInt(MINUTE, mTimePicker.getCurrentMinute());
		state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Dialog#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int hour = savedInstanceState.getInt(HOUR);
		int minute = savedInstanceState.getInt(MINUTE);
		mTimePicker.setCurrentHour(hour);
		mTimePicker.setCurrentMinute(minute);
		mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
		updateTitle(hour, minute);
	}

	private OnTimeChangedListener mOnTimeChangedListener = new OnTimeChangedListener() {
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			if (minute == 59) {
				mPrevMinute = 60;
			}
			if (minute > mPrevMinute) {
				minute += INTERVAL_SIZE;
			}
			int bucket = minute/INTERVAL_SIZE;
			int nextMinute = mIntervals[bucket%(mIntervals.length)];

			mTimePicker.setOnTimeChangedListener(mNullOnTimeChangedListener);

			mTimePicker.setCurrentHour(hourOfDay);
			mTimePicker.setCurrentMinute(nextMinute);
			mPrevMinute = nextMinute;
			updateTitle(hourOfDay, nextMinute);

			mTimePicker.setOnTimeChangedListener(mOnTimeChangedListener);
		}
	};

	private OnTimeChangedListener mNullOnTimeChangedListener = new OnTimeChangedListener() {
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		}
	};
}
