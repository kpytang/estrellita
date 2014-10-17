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

package edu.uci.ics.star.estrellita.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.MoodMapBaseCanvas;
import edu.uci.ics.star.estrellita.customview.MoodMapClickableCanvas;
import edu.uci.ics.star.estrellita.customview.TextboxDialog;
import edu.uci.ics.star.estrellita.customview.TextboxDialog.OnDialogResult;
import edu.uci.ics.star.estrellita.object.indicator.MoodMapSurvey;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class MyMoodForm extends TileActivity<MoodMapSurvey> {
	private static final int NOTES_DIALOG_ID = 0;
	private static final int LAYOUT_WIDTH = MoodMapBaseCanvas.MOODMAP_WIDTH + 15;
	private static final int LAYOUT_HEIGHT = MoodMapBaseCanvas.MOODMAP_HEIGHT + 15;

	private MoodMapClickableCanvas mMoodMapCanvas;
	private String mNotes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mymood_form);

		// initialize header & footer
		setActivityHeader("update my mood", false, Tile.MYMOODS);
		setButtonFooter("update my mood", mSaveClickListener, "add a note", mNotesClickListener);

		// initialize the date
		TextView tv = (TextView) this.findViewById(R.id.date_header);
		tv.setText("for "+ DateUtils.getNowString(DateUtils.DATE_HEADER_FORMAT));

		LinearLayout chartLayout = (LinearLayout)this.findViewById(R.id.mymood);
		mMoodMapCanvas = new MoodMapClickableCanvas(this);
		chartLayout.addView(mMoodMapCanvas);
		mMoodMapCanvas.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			chartLayout.setLayoutParams(new RelativeLayout.LayoutParams(LAYOUT_WIDTH, LAYOUT_HEIGHT));
		}
		else {
			chartLayout.setLayoutParams(new LinearLayout.LayoutParams(LAYOUT_WIDTH, LAYOUT_HEIGHT));
		}

		mNotes = "";

		restoreIndicator();
	}
	
	private OnClickListener mNotesClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			showDialog(NOTES_DIALOG_ID);
		}
	};
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NOTES_DIALOG_ID:
			return new TextboxDialog.Builder(this)
				.setTitle("Add a note for this mood")
				.setText(mNotes)
				.setHintText("Jot down why you're feeling this way")
				.setPositiveButton(new OnDialogResult() {
					@Override
					public void finish(String text) {
						if (text.length() > 0) {
							mNotes = text;
						}
					}
				})
				.setNegativeButton(new OnDialogResult() {
					@Override
					public void finish(String text) {
					}
				})
				.create();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		dialog.setCanceledOnTouchOutside(true);
		switch (id) {
		case NOTES_DIALOG_ID:
			((TextboxDialog) dialog).setText(mNotes);
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}
	
	public AlertDialog ValidateFormDialog(Context context, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
	
	public boolean isMissingValue() {
		if ((mMoodMapCanvas.getX() == -Integer.MAX_VALUE) || (mMoodMapCanvas.getY() == -Integer.MAX_VALUE)) {
			return true;
		}
		return false;	
	}

	// grabs all the reported info and sends it back to the overview activity
	public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (isMissingValue()) {
				Dialog d = ValidateFormDialog(MyMoodForm.this, "Missing required info", "Please indicate your mood by clicking on the mood map.");
				d.show();
			}
			else {
				saveIndicator();
				Intent intent = new Intent();
				intent.putExtra("mood", mIndicator);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileActivity#saveIndicator()
	 */
	@Override
	protected void saveIndicator() {
		mIndicator = new MoodMapSurvey();
		if (!isMissingValue()) {
			mIndicator.addMoodReport((int)mMoodMapCanvas.getX(), (int)mMoodMapCanvas.getY());
		}
		mIndicator.getCommonData().setNotes(mNotes);
		super.saveIndicator();
		
	}

	private void restoreIndicator() {
		mIndicator = null; 
		if (getLastNonConfigurationInstance() != null) { 
			mIndicator = (MoodMapSurvey) getLastNonConfigurationInstance();
			if (mIndicator.getMoodReport() != null) {
				ViewTreeObserver vto = mMoodMapCanvas.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mMoodMapCanvas.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
						mMoodMapCanvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				});
				mMoodMapCanvas.setMood(mIndicator.getMoodReport());
			}
			String s = mIndicator.getCommonData().getNotes();
			if ((s != null) && (s.length()>0)) {
				mNotes = s;
			}
		}
	}
}
