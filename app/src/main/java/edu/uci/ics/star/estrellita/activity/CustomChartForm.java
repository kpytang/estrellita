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
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.GenericIndicator.IndicatorType;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CustomChartForm extends TileActivity<GenericIndicator> {
	private static final int VALIDATE_DIALOG_ID = 0;

	EditText mChartTitleView, mChartUnitsView;
	RadioButton mChartGraph, mChartTable;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_chart_form);

		// initialize header & footer
		setActivityHeader("create new chart", true, Tile.CUSTOMCHARTS);
		setButtonFooter("create", mSaveClickListener, null, null);
		
		mChartTitleView = (EditText) findViewById(R.id.chart_title);
		mChartUnitsView = (EditText) findViewById(R.id.chart_units);
		mChartGraph = (RadioButton) findViewById(R.id.chart_type_graph);
		mChartTable = (RadioButton) findViewById(R.id.chart_type_table);
	}

	public AlertDialog ValidateFormDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Missing required info");
		String prompt = "";
		if (isMissingChartTitle()) {
			prompt += "a title ";
		}
		if (isMissingChartType()) {
			if (prompt.length() > 0) {
				prompt += "and ";
			}
			prompt += " select a chart type ";
		}
		prompt = "Please provide " + prompt + "for this chart.";
		builder.setMessage(prompt)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}

	public boolean isMissingChartTitle() {
		if ((mChartTitleView.getText() == null) || (mChartTitleView.getText().toString().trim().length()==0)) {
			return true;
		}
		return false;	
	}
	
	public boolean isMissingChartType() {
		if (!mChartGraph.isChecked() && !mChartTable.isChecked()) {
			return true;
		}
		return false;	
	}

	// grabs all the reported info and sends it back to the overview activity
	public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (isMissingChartTitle()) {
				Dialog d = onCreateDialog(VALIDATE_DIALOG_ID);
				d.show();
			}
			else {
				GenericIndicator gi = new GenericIndicator();
				gi.getCommonData().setIdUser(EstrellitaTiles.getParentId(CustomChartForm.this));
				gi.getCommonData().setIdBaby(mBaby.getId());
				gi.getCommonData().setTimestamp(DateUtils.getTimestamp());
				gi.setTitle(mChartTitleView.getText().toString().trim());
				gi.setUnits(mChartUnitsView.getText().toString().trim());
				if (mChartGraph.isChecked()) {
					gi.getCommonData().setNotes(IndicatorType.NUMERIC.name());
				}
				else {
					gi.getCommonData().setNotes(IndicatorType.TEXT.name());
				}

				Intent intent = new Intent();
				intent.putExtra("indicator", gi);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case VALIDATE_DIALOG_ID:
			return ValidateFormDialog(this);
		}
		return null;
	}
}
