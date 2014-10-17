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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class CalendarCell {
//	private static final String TAG = "Cell";
	protected Rect mBound = null;
	protected int mDayOfMonth = 1;	// from 1 to 31
	protected Paint mTextPaint = new Paint(Paint.SUBPIXEL_TEXT_FLAG|Paint.ANTI_ALIAS_FLAG);
	protected Paint mHighlightPaint = null;
	protected boolean mTouched = false;
	int dx, dy;
	
	public CalendarCell(int dayOfMon, Rect rect, float textSize, boolean bold) {
		mDayOfMonth = dayOfMon;
		mBound = rect;
		mTextPaint.setTextSize(textSize/*26f*/);
		mTextPaint.setColor(Color.BLACK);
		if(bold) mTextPaint.setFakeBoldText(true);
		
		dx = (int) mTextPaint.measureText(String.valueOf(mDayOfMonth)) / 2;
		dy = (int) (-mTextPaint.ascent() + mTextPaint.descent()) / 2;
	}
	
	public CalendarCell(int dayOfMon, Rect rect, float textSize) {
		this(dayOfMon, rect, textSize, false);
	}
	
	protected void draw(Canvas canvas) {
		if (mHighlightPaint != null) {
			canvas.drawRect(mBound, mHighlightPaint);
		}
		canvas.drawText(String.valueOf(mDayOfMonth), mBound.centerX() - dx, mBound.centerY() + dy, mTextPaint);
	}
	
	public int getDayOfMonth() {
		return mDayOfMonth;
	}
	
	public boolean hitTest(int x, int y) {
		return mBound.contains(x, y); 
	}
	
	public Rect getBound() {
		return mBound;
	}
	
	public String toString() {
		return String.valueOf(mDayOfMonth)+"("+mBound.toString()+")";
	}
	
	public void setTouchedFlag(boolean flag) {
		mTouched = flag;
	}
}

