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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TextDiaperNumberPicker extends TextNumberPicker {
	private static final int ORIGINAL_VALUE_COLOR = Color.BLACK;
	private static final int NEW_VALUE_COLOR = Color.DKGRAY;
	
	private int mOriginalValue;

	public TextDiaperNumberPicker(Context context) {
		this(context, null);
	}
	
	public TextDiaperNumberPicker(Context context, AttributeSet attr) {
		super(context, attr, 0);
		this.setStart(0);
		this.setEnd(20);
		mOriginalValue = 0;
		
		// get rid of the of the orange highlight around the text box
		mText.setFocusable(false);
	}

	// original version wraps around - we won't do that here
	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.customview.TextNumberPicker#changeCurrent(int)
	 */
	@Override
	public void changeCurrent(int current) {
        if (current > getEnd()) {
            current = getEnd();
        } else if (current < getStart()) {
            current = getStart();
        }
    	mPrevious = mCurrent;
        mCurrent = current;

        if (mCurrent != mPrevious) {
	        notifyChange();
	        updateView();
        }

    }
	
	public void setStartingValue(int value) {
		mOriginalValue = value;
		mPrevious = 0;
        mCurrent = value;
        notifyChange();
        updateView();
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.customview.NumberPicker#updateView()
	 */
	@Override
	public void updateView() {
		super.updateView();
		if (mCurrent == mOriginalValue) {
			mText.setTextColor(ORIGINAL_VALUE_COLOR);
			mText.setTypeface(null, Typeface.BOLD);
		}
		else {
			mText.setTextColor(NEW_VALUE_COLOR);
			mText.setTypeface(null, Typeface.NORMAL);
		}
	}
}
