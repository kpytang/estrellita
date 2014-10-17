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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.chart.TimeBasedLineChart;
import edu.uci.ics.star.estrellita.object.indicator.Weight;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;

public class WeightOverview extends TileOverviewActivity<Weight> {
	private static final int WEIGHT_FORM_ID = 0;

	List<List<Float>> mWeightChartLines;
	List<Integer> mWeightChartColors;

	TimeBasedLineChart mWeightChart;

	boolean mAlreadyReportedThisWeek;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.MONTH);

		// initialize header
		setActivityHeader("Weight", true, Tile.WEIGHT);
		setButtonHeader("update weight");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mUpdateWeight);

		mIndicators = new ArrayList<Weight>();
		setIndicatorType(Weight.class);
		mAlreadyReportedThisWeek = false;

		// by default should get weekly set - but we need to support other ranges
		// assumes we get a full set of data with the future dates appropriately set (to -1)

		// display chart data
		showChartData();
	}

	protected void showChartData() {
		// this calls getChartDataFromDB, formatLineChartData() and convertChartDataToVizData()
		super.getChartData(ChartType.LINE);

		mWeightChart = new TimeBasedLineChart(this, mWeightChartLines, mWeightChartColors, mCurrentTimeScale);
		mWeightChart.setOnChartTouchListener(mChartListener);

		if (mCurrentTimeScale == TimeScale.ALL) {
			mWeightChart.setXLabels(getMonthStrings());
		}
		else if (mCurrentTimeScale == TimeScale.MONTH) {
			mWeightChart.setXLabels(getWeekStrings().get(0), getWeekStrings().get(1));
		}

		updateChartView(mWeightChart);
	}

	/*
	 * grabs all the weights from the database
	 * by default, we'll get a month of weights
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#getChartDataFromDB()
	 */
	protected List<Weight> getChartDataFromDB() {
		List<Weight> dbWeights = new ArrayList<Weight>();
		Weight[] weights;
		switch(mCurrentTimeScale) {
		case MONTH:
			weights = mDatabase.getWeightTable().getWeightsForTimeRange(mBaby.getId(), mCurrentStartDate, mCurrentEndDate);
			if ( (weights != null) && (weights.length>0) ) {
				dbWeights = Arrays.asList(weights);
				// make sure these weights are sorted (by time)
				Collections.sort(dbWeights);
			}
			break;
		case ALL:
			weights = mDatabase.getWeightTable().getAllWeights(mBaby.getId());
			if ( (weights != null) && (weights.length>0) ) {
				dbWeights = Arrays.asList(weights);
				// make sure these weights are sorted (by time)
				Collections.sort(dbWeights);
				mCurrentStartDate = dbWeights.get(0).getDateTime();
				mCurrentEndDate = dbWeights.get(dbWeights.size()-1).getDateTime();
			}
			break;
		}
		// check to see if weight has already been reported this week
		if (mDatabase.getWeightTable().isNeglected(mBaby.getId(), this)) {
			mAlreadyReportedThisWeek = false;
		}
		else {
			mAlreadyReportedThisWeek = true;
		}
		return dbWeights;
	}

	// pulls out the different weights and initializes them as chart data
	protected void convertChartDataToVizData() {
		List<Float> data = new ArrayList<Float>();

		for (int i=0; i<mIndicators.size(); i++) {
			data.add(Double.valueOf(mIndicators.get(i).getWeight()).floatValue());
		}

		mWeightChartLines = new ArrayList<List<Float>>();
		mWeightChartLines.add(data);

		mWeightChartColors = new ArrayList<Integer>();
		mWeightChartColors.add(Color.RED);
	}

	View.OnClickListener mUpdateWeight = new OnClickListener() {
		public void onClick(View v) {
			// if you already entered a weight for this week, show a prompt that prevents them from entering any more data for this week
			if (mAlreadyReportedThisWeek) {
				Dialog d = ValidateFormDialog(WeightOverview.this);
				d.show();
			}
			else {
				Intent intent = new Intent(WeightOverview.this, WeightForm.class);
				intent.putExtra(TileActivity.BABY, mBaby);
				Weight weight = (Weight)mDatabase.getWeightTable().getLastBabyIndicator(mBaby.getId(), null);
				if(weight != null) {
					intent.putExtra("lbs", weight.getPounds());
					intent.putExtra("ozs", weight.getOunces());
				}
				startActivityForResult(intent, WEIGHT_FORM_ID);
			}
		}
	};

	// checks for results from 'update weight' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case WEIGHT_FORM_ID:
				// get diaper info from intent
				Weight weight = (Weight) data.getParcelableExtra("weight");
				String weightPhoto = data.getStringExtra("photo");
				// else, there are photos, so upload them one by one (all with the same count info)
				if (weightPhoto != null) {
					weight.setImage(ImageUtils.decodeFile(new File(weightPhoto)));
					weight.getCommonData().setAndroidFilename(weightPhoto);
				}

				openDatabase();
				mDatabase.insertSingle(weight);
				data.setClassName(this, this.getClass().getName());
				mDatabase.logInsert(data, weight);

				// update chart with newly inserted data
				showChartData();

				Weight[] previousWeights = (Weight[])mDatabase.getWeightTable().getLastNBabyIndicator(mBaby.getId(), null, 2);
				if(previousWeights != null){
					for(Weight previousWeight:previousWeights){
						if(previousWeight.getPounds() > weight.getPounds()  // if lbs is less than last time
								|| (previousWeight.getPounds() == weight.getPounds() // lbs is the same
										&& ((previousWeight.getOunces() > weight.getOunces())))) { // but ounces are less
							AlertDialog makeUnderweightDialog = makeWeightAlertDialog("Your baby's weight has dropped since last week. Please schedule an appointment with your pediatrician to discuss your baby's weight.");
							makeUnderweightDialog.show();
						}
						else if (previousWeight.getOunces() == weight.getOunces() // or weight and ouces are the same
									&& DateUtils.getDaysPast(previousWeight.getCommonData().getTimestamp()) > 12) { // and that record is two weeks old

							AlertDialog makeUnderweightDialog = makeWeightAlertDialog("Your baby's weight has not increased in 2 week. Please schedule an appointment with your pediatrician to discuss your baby's health.");
							makeUnderweightDialog.show();
						}
					}
				}



				Toast.makeText(WeightOverview.this, "updated chart: " + weight.getWeightString(), Toast.LENGTH_LONG).show();
				break;
			}
		}
	}
	private AlertDialog makeWeightAlertDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(WeightOverview.this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("Alert");
		builder.setMessage(message)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				Intent phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:9492642229"));
//				startActivity(phoneDialer);
				finish();
			}
		});
		AlertDialog create = builder.create();
		return create;
	}
	@Override
	protected void setHighlightBox(int index) {
		mWeightChart.setHighlightBox(index);
	}

	public AlertDialog ValidateFormDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle(R.string.tip);
		builder.setMessage(R.string.too_many_weight_reports)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
}
