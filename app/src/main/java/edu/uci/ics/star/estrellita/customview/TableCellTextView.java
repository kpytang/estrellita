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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;

public class TableCellTextView extends TextView {
	private static final float PREFIX_MARGIN_BUFFER = 5;
	private static final int PADDING = 20;
	private static final float BORDER_THICKNESS = 2;

	private Paint mBorderPaint;
	private String mPrefix;
	private Paint mTextPaint;
	private int mBackgroundColor;
	
	public TableCellTextView(Context context) {
		this(context, Color.DKGRAY, true);
	}
	
	public TableCellTextView(Context context, int borderColor) {
		this(context, borderColor, true);
	}
	
	public TableCellTextView(Context context, int borderColor, boolean addPadding) {
		super(context);
		
		mTextPaint = new Paint();
		mBorderPaint = new Paint(borderColor);
		mBorderPaint.setStrokeWidth(BORDER_THICKNESS);
		mPrefix = null;
		
		// padding for left, top, right, bottom
		if (addPadding) {
			setPadding(PADDING,0,PADDING,0);
		}
	}
	
	public void setBorderThickness(float width) {
		mBorderPaint.setStrokeWidth(width);
	}
	
	public void setPrefix(String s) {
		mPrefix = s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.TextView#setTextColor(int)
	 */
	public void setTextColor(int color) {
		super.setTextColor(color);
		mTextPaint = new Paint(color);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#setBackgroundColor(int)
	 */
	public void setBackgroundColor(int color) {
		super.setBackgroundColor(color);
		mBackgroundColor = color;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		// set the background color for the item
		canvas.drawColor(mBackgroundColor);	
		// draw the horizontal line at the bottom of the item
		canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), mBorderPaint);
		// if we have a prefix, then we need to draw that before drawing the textview
		if ((mPrefix != null) && (mPrefix.length() > 0)) {
			// draw the prefix text to the left of the margin
			canvas.drawText(mPrefix, 25, getMeasuredHeight(), mTextPaint);
			// draw the vertical borders (first one is to the right of the prefix, second one is after the prefix
			canvas.drawLine(2, 0, 2, getMeasuredHeight(), mBorderPaint);
			float offset = mTextPaint.measureText(mPrefix) + PREFIX_MARGIN_BUFFER;
			canvas.drawLine(offset, 0, offset, getMeasuredHeight(), mBorderPaint);
			// move the text across from the margin and up 10 from the line so that it isn't flush with the border line
			canvas.save();
			canvas.translate(offset, 10);
		}
		// if we don't have a prefix, we should still draw a vertical to the right of the text
		else {
			canvas.drawLine(2, 0, 2, getMeasuredHeight(), mBorderPaint);
			// move the text a little to the right and up 10 from the line so that it isn't flush with the border line
			canvas.save();
			canvas.translate(PREFIX_MARGIN_BUFFER, 0);
		}
		// use the TextView to render the text.
		super.onDraw(canvas);
		canvas.restore();
	}
}
