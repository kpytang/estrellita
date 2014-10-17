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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.R;

public class CustomViews {
	public enum ToastMessage {OTHER, PHOTO_TIP};
	
	private static final String NOTES_HELP_DIALOG_TITLE = "How to use the Notes field";
	
	public static Toast createCenteredToast(Context context, ToastMessage type) {
		switch(type) {
		case PHOTO_TIP:
			return createCenteredToast(context, R.string.photo_tip, Toast.LENGTH_LONG);
		}
		return null;
	}
	
	public static Toast createCenteredToast(Context context, int message, int length) {
		Toast toast = Toast.makeText(context, message, length);
		toast.setGravity(Gravity.CENTER, 0, 0);
		return toast;
	}
	
	public static Toast createCenteredToast(Context context, String message, int length) {
		Toast toast = Toast.makeText(context, message, length);
		toast.setGravity(Gravity.CENTER, 0, 0);
		return toast;
	}
	
	public static AlertDialog NotesTipDialog(Context context, String message) {
		AlertDialog d = new AlertDialog.Builder(context)
			.setTitle(NOTES_HELP_DIALOG_TITLE)
			.setMessage(message)
			.setCancelable(true)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			})
			.create();
		d.setCanceledOnTouchOutside(true);
		return d;
	}
	
	public static View createSeparator(Context context, int thickness) {
		View separator = new View(context);
		separator.setBackgroundColor(Color.GRAY);
		separator.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, thickness));
		return separator;
	}
	
	public static View createDialogTitleView(Context context, String title) {
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView tv = new TextView(context);
		tv.setText(title);
		tv.setGravity(Gravity.CENTER);
		tv.setTextAppearance(context, android.R.style.TextAppearance_Large);
		layout.addView(tv);
		layout.addView(CustomViews.createSeparator(context, 1));
		
		return layout;
	}
}
