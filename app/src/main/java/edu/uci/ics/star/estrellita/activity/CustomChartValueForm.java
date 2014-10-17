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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.TextboxDialog;
import edu.uci.ics.star.estrellita.customview.TextboxDialog.OnDialogResult;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.GenericIndicator.IndicatorType;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CustomChartValueForm extends TileActivity<GenericIndicator> {
	private static final int TEXTBOX_DIALOG_ID = 1;
	private static final int TIME_DIALOG_ID = 7;

	EditText mChartValueView;
	TextView mChartNotesView;
	String mChartTitle, mChartUnits;
	IndicatorType mChartType;

	private TextView mDateTextView;
	private Calendar mDisplayDate;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_chart_value_form);

		// if this is being called from the CustomChartOverview, then there will be a title in the intent
		// otherwise, this will be null because it's being created by CustomChartsOverview 
		mChartTitle = getIntent().getStringExtra("title");
		mChartUnits = getIntent().getStringExtra("units");

		String s = getIntent().getStringExtra("type");
		if (s != null) {
			mChartType = IndicatorType.valueOf(s);
		}
		else {
			mChartType = IndicatorType.TEXT;
		}

		setActivityHeader("add a value", true, Tile.CUSTOMCHARTS);
		setButtonFooter("add", mSaveClickListener, null, null);

		TextView tv = (TextView) findViewById(R.id.chart_title_header);
		tv.setText("Chart Title: " + mChartTitle);

		// put in the units, only if it is provided
		tv = (TextView) findViewById(R.id.chart_units_header);
		if ((mChartUnits != null) && (mChartUnits.length() > 0)) {
			tv.setText("(measured in " + mChartUnits + ")");
		}
		else {
			mChartUnits = "";
			tv.setVisibility(View.GONE);
		}

		// check to see which type of input data this should be
		mChartValueView = (EditText) findViewById(R.id.chart_value);
		if(mChartType == IndicatorType.NUMERIC) {
			mChartValueView.setKeyListener(new DigitsKeyListener(false,true));
		}

		mChartNotesView = (TextView) findViewById(R.id.chart_notes);
		mChartNotesView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(TEXTBOX_DIALOG_ID);
			}
		});
		mChartNotesView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showDialog(TEXTBOX_DIALOG_ID);
				}
			}
		});

		// initialize the date
		LinearLayout dateButton = (LinearLayout) this.findViewById(R.id.date_header);
		dateButton.setClickable(true);
		dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}       
		});
		mDateTextView = (TextView) this.findViewById(R.id.date_header_text);
		mDisplayDate = Calendar.getInstance();
		updateDateTime(mDisplayDate.getTime());
		
		restoreIndicator();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case TEXTBOX_DIALOG_ID:
			return new TextboxDialog.Builder(this)
			.setTitle("Add a note for this value")
			.setText(mChartNotesView.getText().toString().trim())
			.setPositiveButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
					if (text.length() > 0) {
						mChartNotesView.setText(text);
					}
				}
			})
			.setNegativeButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
				}
			})
			.create();
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mDisplayDate.get(Calendar.HOUR_OF_DAY), mDisplayDate.get(Calendar.MINUTE), false);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case TEXTBOX_DIALOG_ID:
			((TextboxDialog) dialog).setText(mChartNotesView.getText().toString().trim());
			break;
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mDisplayDate.get(Calendar.HOUR_OF_DAY), mDisplayDate.get(Calendar.MINUTE));
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}

	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mDisplayDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mDisplayDate.set(Calendar.MINUTE, minute);
			updateDateTime(mDisplayDate.getTime());
		}
	};

	private void updateDateTime(Date d) {
		mDateTextView.setText("for "+ DateUtils.getDateAsString(d, DateUtils.DATE_HEADER_FORMAT) + "\n" 
				+ "at " + DateUtils.getDateAsString(d, DateUtils.AMPM_TIME_FORMAT));
	}

	public AlertDialog ValidateFormDialog(String title, String message) {
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

	public boolean isMissingChartValue() {
		if ((mChartValueView.getText() == null) || (mChartValueView.getText().toString().trim().length()==0)) {
			return true;
		}
		return false;	
	}

	public boolean isValueNotNumeric() {
		String value = mChartValueView.getText().toString().trim();
		try {
			Float.parseFloat(value);
			return false;
		}
		catch (Exception e) {
			return true;
		}
	}

	// grabs all the reported info and sends it back to the overview activity
	public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (isMissingChartValue()) {
				Dialog d = ValidateFormDialog("Missing required info", "Please fill in a value for this chart.");
				d.show();
			}
			else if (isValueNotNumeric() && (mChartType == IndicatorType.NUMERIC)) {
				Dialog d = ValidateFormDialog("Incorrect data type", "You can only add numeric values to this chart.");
				d.show();
			}
			else {
				GenericIndicator gi = new GenericIndicator();
				gi.getCommonData().setIdUser(EstrellitaTiles.getParentId(CustomChartValueForm.this));
				gi.getCommonData().setIdBaby(mBaby.getId());
				gi.getCommonData().setTimestamp(new Timestamp(mDisplayDate.getTimeInMillis()));
				gi.setTitle(mChartTitle);
				gi.setUnits(mChartUnits);
				gi.setData(mChartValueView.getText().toString().trim());
				gi.getCommonData().setNotes(mChartNotesView.getText().toString().trim());

				Intent intent = new Intent();
				intent.putExtra("indicator", gi);
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
		mIndicator = new GenericIndicator();
		// we must put this earlier because we will manually change the timestamp of the indicator 
		// (which is set in the super.saveIndicator())
		super.saveIndicator();
		mIndicator.getCommonData().setTimestamp(new Timestamp(mDisplayDate.getTimeInMillis()));
	}

	private void restoreIndicator() {
		mIndicator = null; 
		if (getLastNonConfigurationInstance() != null) { 
			mIndicator = (GenericIndicator) getLastNonConfigurationInstance();
			mDisplayDate.setTime(mIndicator.getDateTime());
			updateDateTime(mDisplayDate.getTime());
		}
	}
}
