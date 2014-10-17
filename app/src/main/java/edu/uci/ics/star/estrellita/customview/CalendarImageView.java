/*
 * Copyright (C) 2011 Chris Gao <chris@exina.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


package edu.uci.ics.star.estrellita.customview;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.widget.ImageView;
import edu.uci.ics.star.estrellita.R;

public class CalendarImageView extends ImageView {
	private static int WEEK_TOP_MARGIN = 0; //74
	private static int WEEK_LEFT_MARGIN = 40;
	private static int CELL_WIDTH = 61;
	private static int CELL_HEIGHT = 54;
	private static int CELL_MARGIN_TOP = 18; //92
	private static int CELL_MARGIN_LEFT = 39;
	private static float CELL_TEXT_SIZE = 26;
	private static int HIGHLIGHT_Y_BUFFER = 2;
	private static int HIGHLIGHT_WIDTH = 4;

	//	private static final String TAG = "CalendarView"; 
	private Calendar mRightNow = null;
	private Drawable mWeekTitle = null;
	private CalendarCell mToday = null;
	private CalendarCell[][] mCells = new CalendarCell[6][7];
	private OnCellTouchListener mOnCellTouchListener = null;
	private MonthDisplayHelper mHelper;
	private Context mContext;
	private Rect mHightlightBounds = null;
	private Rect mTodayBounds = null;

	public interface OnCellTouchListener {
		public void onTouch(CalendarCell cell);
	}

	public CalendarImageView(Context context) {
		this(context, null);
	}

	public CalendarImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalendarImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;

		initCalendarView();
	}

	private void initCalendarView() {
		mRightNow = Calendar.getInstance();

		setImageResource(R.drawable.calendar_background);
		mWeekTitle = mContext.getResources().getDrawable(R.drawable.calendar_week);

		mHelper = new MonthDisplayHelper(mRightNow.get(Calendar.YEAR), mRightNow.get(Calendar.MONTH));
	}

	private void initCells() {
		class _calendar {
			public int day;
			public boolean thisMonth;
			public _calendar(int d, boolean b) {
				day = d;
				thisMonth = b;
			}
			public _calendar(int d) {
				this(d, false);
			}
		};
		_calendar tmp[][] = new _calendar[6][7];

		for(int i=0; i<tmp.length; i++) {
			int n[] = mHelper.getDigitsForRow(i);
			for(int d=0; d<n.length; d++) {
				if(mHelper.isWithinCurrentMonth(i,d))
					tmp[i][d] = new _calendar(n[d], true);
				else
					tmp[i][d] = new _calendar(n[d]);

			}
		}

		Calendar today = Calendar.getInstance();
		int thisDay = 0;
		mToday = null;
		if(mHelper.getYear()==today.get(Calendar.YEAR) && mHelper.getMonth()==today.get(Calendar.MONTH)) {
			thisDay = today.get(Calendar.DAY_OF_MONTH);
		}
		// build cells
		Rect Bound = new Rect(CELL_MARGIN_LEFT, CELL_MARGIN_TOP, CELL_WIDTH+CELL_MARGIN_LEFT, CELL_HEIGHT+CELL_MARGIN_TOP);
		for(int week=0; week<mCells.length; week++) {
			for(int day=0; day<mCells[week].length; day++) {
				if(tmp[week][day].thisMonth) {
					if(day==0 || day==6 ) {
						mCells[week][day] = new RedCell(tmp[week][day].day, new Rect(Bound), CELL_TEXT_SIZE);
					}
					else {
						mCells[week][day] = new CalendarCell(tmp[week][day].day, new Rect(Bound), CELL_TEXT_SIZE);
					}
				}
				else {
					mCells[week][day] = new GrayCell(tmp[week][day].day, new Rect(Bound), CELL_TEXT_SIZE);
				}

				Bound.offset(CELL_WIDTH, 0); // move to next column 

				// get today
				if(tmp[week][day].day==thisDay && tmp[week][day].thisMonth) {
					mToday = mCells[week][day];
					mTodayBounds = new Rect(
							mToday.getBound().left, 
							mToday.getBound().top + HIGHLIGHT_Y_BUFFER, 
							mToday.getBound().right, 
							mToday.getBound().bottom + HIGHLIGHT_Y_BUFFER);
				}
			}
			Bound.offset(0, CELL_HEIGHT); // move to next row and first column
			Bound.left = CELL_MARGIN_LEFT;
			Bound.right = CELL_MARGIN_LEFT+CELL_WIDTH;
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ImageView#setFrame(int, int, int, int)
	 */
	@Override
	protected boolean setFrame (int l, int t, int r, int b) {
		Rect re = getDrawable().getBounds();
		WEEK_LEFT_MARGIN = CELL_MARGIN_LEFT = (r-l - re.width()) / 2;
		mWeekTitle.setBounds(WEEK_LEFT_MARGIN, WEEK_TOP_MARGIN, WEEK_LEFT_MARGIN+mWeekTitle.getMinimumWidth(), WEEK_TOP_MARGIN+mWeekTitle.getMinimumHeight());
		initCells();
		return super.setFrame(l, t, r, b);
	}

	public void setTimeInMillis(long milliseconds) {
		mRightNow.setTimeInMillis(milliseconds);
		initCells();
		this.invalidate();
	}
	
	public void resetCalendarView() {
		mHelper = new MonthDisplayHelper(mRightNow.get(Calendar.YEAR), mRightNow.get(Calendar.MONTH));
	}

	public int getYear() {
		return mHelper.getYear();
	}

	public int getMonth() {
		return mHelper.getMonth();
	}
	
	public void nextMonth() {
		mHelper.nextMonth();
		mHightlightBounds = null;
		initCells();
		invalidate();
	}

	public void previousMonth() {
		mHelper.previousMonth();
		mHightlightBounds = null;
		initCells();
		invalidate();
	}

	public boolean firstDay(int day) {
		return day==1;
	}

	public boolean lastDay(int day) {
		return mHelper.getNumberOfDaysInMonth()==day;
	}

	public void goToday() {
		Calendar cal = Calendar.getInstance();
		mHelper = new MonthDisplayHelper(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
		initCells();
		invalidate();
	}

	public Calendar getDate() {
		return mRightNow;
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		CalendarCell selectedDay = null;
		mHightlightBounds = null;
		boolean redraw = false;
		for(CalendarCell[] week : mCells) {
			for(CalendarCell day : week) {
				if (day.hitTest((int) event.getX(), (int) event.getY())) { // check if we've clicked on a day
					selectedDay = day;
					mHightlightBounds = new Rect(
							day.getBound().left, 
							day.getBound().top + HIGHLIGHT_Y_BUFFER, 
							day.getBound().right, 
							day.getBound().bottom + HIGHLIGHT_Y_BUFFER);
					redraw = true;
					break;
				}
			}
		}
		if (redraw) {
			invalidate();
		}
		if ( (mOnCellTouchListener != null) && (selectedDay != null) ){
			mOnCellTouchListener.onTouch(selectedDay);
		}
		return super.onTouchEvent(event);
	}

	public void setOnCellTouchListener(OnCellTouchListener p) {
		mOnCellTouchListener = p;
	}

	public void setHighlight(int dayOfMonth) {
		CalendarCell day = mCells[mHelper.getRowOf(dayOfMonth)][mHelper.getColumnOf(dayOfMonth)];
		if ( !(day instanceof GrayCell) ) { 
			mHightlightBounds = new Rect(
					day.getBound().left, 
					day.getBound().top + HIGHLIGHT_Y_BUFFER, 
					day.getBound().right, 
					day.getBound().bottom + HIGHLIGHT_Y_BUFFER);
			invalidate();
		}
	}

	public void setHighlight(Rect bounds) {
		mHightlightBounds = bounds;
	}

	public Rect getHighlight() {
		return mHightlightBounds;
	}

	public void resetConstants(boolean portrait) {
		if (portrait) {
			CELL_HEIGHT = 53;
			CELL_MARGIN_TOP = 18; //92
		}
		else {
			CELL_HEIGHT = 50;
			CELL_MARGIN_TOP = 15; //92
		}
		initCells();
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw background
		super.onDraw(canvas);
		mWeekTitle.draw(canvas);

		if (mHightlightBounds != null) {
			Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			highlightPaint.setColor(Color.BLUE);
			highlightPaint.setStyle(Paint.Style.STROKE);
			highlightPaint.setStrokeWidth(HIGHLIGHT_WIDTH);
			canvas.drawRect(mHightlightBounds, highlightPaint);
		}

		// draw today
		if(mTodayBounds!=null && mToday!=null) {
			Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			highlightPaint.setColor(Color.YELLOW);
			highlightPaint.setStyle(Paint.Style.FILL);
			canvas.drawRect(mTodayBounds, highlightPaint);
		}

		// draw cells
		for(CalendarCell[] week : mCells) {
			for(CalendarCell day : week) {
				day.draw(canvas);			
			}
		}
	}	

	private class GrayCell extends CalendarCell {
		public GrayCell(int dayOfMon, Rect rect, float s) {
			super(dayOfMon, rect, s);
			mTextPaint.setColor(Color.LTGRAY);
		}			
	}

	private class RedCell extends CalendarCell {
		public RedCell(int dayOfMon, Rect rect, float s) {
			super(dayOfMon, rect, s);
			mTextPaint.setColor(0xdddd0000);
		}			
	}
}
