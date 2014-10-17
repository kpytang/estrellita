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
import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.customview.CalendarImageView.OnCellTouchListener;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CalendarPickerDialog extends Dialog {
	/**
	 * Defines the interface which defines the methods of the OnDateSetListener
	 */
	public interface OnDateSetListener {
		/**
		 * this method is called when a date was selected by the user
		 * @param view			the caller of the method
		 *
		 */
		public void onDateSet(CalendarImageView view, Calendar selectedDate);
	}

	public static final String TIME = "time";

	private Context mContext;
	private Calendar mCalendar;
	private LinearLayout mDialogLayout;
	private CalendarImageView mCalendarView;
	private TextView mNext, mPrev, mMonth;
	private Date mSelectedDate;
	private OnDateSetListener mOnDateSetListener;
	private OnDismissListener mDismissListener;

	public CalendarPickerDialog(Context context, OnDateSetListener listener, Calendar initialDate) {
		this(context, listener, null, initialDate);
	}

	public CalendarPickerDialog(Context context, OnDateSetListener listener, OnDismissListener dismissListener, Calendar initialDate) {
		super(context);
		mContext = context;
		mCalendar = initialDate;
		mOnDateSetListener = listener;
		mDismissListener = dismissListener;
		mSelectedDate = null;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDialogLayout = (LinearLayout) inflater.inflate(R.layout.calendar_picker_dialog, null);
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Dialog#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(mDialogLayout, new LayoutParams(445, LayoutParams.WRAP_CONTENT));

		setCanceledOnTouchOutside(true);
		setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mDismissListener != null) {
					mDismissListener.onDismiss(dialog);
				}
			}
		});

		mCalendarView = (CalendarImageView) mDialogLayout.findViewById(R.id.calendar);
		mCalendarView.setTimeInMillis(mCalendar.getTimeInMillis());
		mCalendarView.setOnCellTouchListener(new OnCellTouchListener() {
			public void onTouch(CalendarCell cell) {
				updateCalendar(mCalendarView.getYear(), mCalendarView.getMonth(), cell.getDayOfMonth());
			}
		});
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getOrientation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			mCalendarView.resetConstants(true);
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			mCalendarView.resetConstants(false);
			break;
		}

		mNext = (TextView) mDialogLayout.findViewById(R.id.next);
		mNext.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mCalendarView.nextMonth();
				checkAndSetHighlight();
				updateMonth();
			}
		});
		mPrev = (TextView) mDialogLayout.findViewById(R.id.prev);
		mPrev.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mCalendarView.previousMonth();
				checkAndSetHighlight();
				updateMonth();
			}
		});

		mMonth = (TextView) mDialogLayout.findViewById(R.id.month_name);
		updateMonth();

		Button okButton = (Button) mDialogLayout.findViewById(R.id.ok_button);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mSelectedDate != null) {
					mCalendar.setTime(mSelectedDate);
				}
				else {
					mCalendar.setTime(DateUtils.getTimestamp());
				}
				if (mOnDateSetListener != null) {
					mOnDateSetListener.onDateSet(mCalendarView, mCalendar);
				}
				dismiss();
			}
		});

		//		checkAndSetHighlight();
	}

	private void checkAndSetHighlight() {
		// we're back in the same month and year as the previously highlighted date, so let's highlight it again
		if (mSelectedDate != null) {
			if ( (mCalendarView.getMonth() == mSelectedDate.getMonth()) && (mCalendarView.getYear() == (mSelectedDate.getYear()+1900)) ) {
				mCalendarView.setHighlight(mSelectedDate.getDate());
			}
		}
	}

	private void updateMonth() {
		mMonth.setText(android.text.format.DateUtils.getMonthString(mCalendarView.getMonth(), android.text.format.DateUtils.LENGTH_SHORT).toUpperCase() + 
				" " + mCalendarView.getYear());
	}

	// gets called by OnTouch
	public void updateCalendar(int year, int month, int day) {
		mCalendar.set(year, month, day);
		mSelectedDate = mCalendar.getTime();
		mCalendarView.setTimeInMillis(mCalendar.getTimeInMillis());
	}

	// gets called by OnPrepare for dialog
	public void setTime(Calendar c, boolean drawHighlight) {
		updateCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mCalendarView.resetCalendarView();
		updateMonth();
		if (drawHighlight) {
			checkAndSetHighlight();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Dialog#onSaveInstanceState()
	 */
	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		if (mCalendarView != null) {
			state.putInt("month", mCalendarView.getMonth());
			state.putInt("year", mCalendarView.getYear());
		}
		if (mSelectedDate != null) {
			state.putLong("selected-date", mSelectedDate.getTime());
		}
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Dialog#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int month = savedInstanceState.getInt("month", -1);
		int year = savedInstanceState.getInt("year", -1);
		if ( (month != -1) && (year != -1)) {
			mCalendar.set(year, month, 1);
		}
		long selected = savedInstanceState.getLong("selected-date", -1);
		if (selected != -1) {
			mSelectedDate = new Date(selected);
		}
		mCalendarView.setTimeInMillis(mCalendar.getTime().getTime());
		mCalendarView.resetCalendarView();
		updateMonth();

		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getOrientation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			mCalendarView.resetConstants(true);
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			mCalendarView.resetConstants(false);
			break;
		}
	}
}