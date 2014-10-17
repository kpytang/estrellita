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
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class EditTextWithDoneSoftAction extends EditText {
	
	public EditTextWithDoneSoftAction(Context context) {
		super(context);
	}
	public EditTextWithDoneSoftAction(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public EditTextWithDoneSoftAction(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	    InputConnection connection = super.onCreateInputConnection(outAttrs);
	    int imeActions = outAttrs.imeOptions&EditorInfo.IME_MASK_ACTION;
	    if ((imeActions&EditorInfo.IME_ACTION_DONE) != 0) {
	        // clear the existing action
	        outAttrs.imeOptions ^= imeActions;
	        // set the DONE action
	        outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
	    }
	    if ((outAttrs.imeOptions&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
	        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
	    }
	    return connection;
	}
}
