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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.chart.BarChart;
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.OnChartTouchListener;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey.BondingActivity;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class BondingOverview extends TileOverviewActivity<BondingSurvey> {
	private static final int BONDING_FORM_ID = 0;

	List<Float> mBondingCounts;
	List<String> mBondingCategories;

	BarChart mBondingChart;

	AlertDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.WEEK);

		// initialize header
		setActivityHeader("Bonding", true, Tile.BONDING);
		setButtonHeader("update bonding activities");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mUpdateBonding);

		// the bonding categories - abbreviated one of the names so that it shows up on the chart
		mBondingCategories = BondingSurvey.getBondingActivityNames();
		mBondingCategories.remove(3);
		mBondingCategories.add(3, "Walking");

		mBarChartBins = BondingActivity.getSize();
		mIndicators = new ArrayList<BondingSurvey>();
		setIndicatorType(BondingSurvey.class);

		// grab chart data
		showChartData();
	}

	protected void showChartData() {
		// this calls getChartDataFromDB, getBarChartDataTable() and convertChartDataToVizData()
		super.getChartData(ChartType.BAR);

		mBondingChart = new BarChart(this, mBondingCategories, mBondingCounts);
		// mBondingChart.setOnChartTouchListener(mChartListener);

		updateChartView(mBondingChart);
	}

	/*
	 * grabs all the bondings from the database
	 * by default, we'll get a week of bondings
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#getChartDataFromDB()
	 */
	@Override
	protected List<BondingSurvey> getChartDataFromDB() {
		// first grab all the bonding surveys from the database
		List<BondingSurvey> dbBondings = new ArrayList<BondingSurvey>();
		BondingSurvey[] bondings;
		switch(mCurrentTimeScale) {
		case WEEK:
		case MONTH:
			bondings = (BondingSurvey[]) mDatabase.getBondingTable().getBabySurveysForTimeRange(mBaby.getId(), mCurrentStartDate, mCurrentEndDate);
			if ( (bondings != null) && (bondings.length>0) ) {
				dbBondings = Arrays.asList(bondings);
				// make sure these bondings are sorted (by time)
				Collections.sort(dbBondings);
			}
			break;
		case ALL:
			bondings = (BondingSurvey[]) mDatabase.getBondingTable().getAllSurveysForBaby(mBaby.getId());
			if ( (bondings != null) && (bondings.length>0) ) {
				dbBondings = Arrays.asList(bondings);
				// make sure these bondings are sorted (by time)
				Collections.sort(dbBondings);
				mCurrentStartDate = dbBondings.get(0).getDateTime();
				mCurrentEndDate = dbBondings.get(dbBondings.size()-1).getDateTime();
			}
			break;
		}
		return dbBondings;
	}

	// this chart is for the main overview chart
	protected void convertChartDataToVizData() {
		// initialize array
		Float[] counts = new Float[BondingActivity.getSize()];
		for (int i=0; i<counts.length; i++) {
			counts[i] = 0f;
		}

		for (int i=0; i<mIndicators.size(); i++) {
			if (mIndicators.get(i).hasCompletedTalking()) {
				counts[BondingActivity.TALKING.getActivityID()]++;
			}
			if (mIndicators.get(i).hasCompletedReading()) {
				counts[BondingActivity.READING.getActivityID()]++;
			}
			if (mIndicators.get(i).hasCompletedWalking()) {
				counts[BondingActivity.TAKE_BABY_WALKING.getActivityID()]++;
			}
			if (mIndicators.get(i).hasCompletedTummyTime()) {
				counts[BondingActivity.TUMMY_TIME.getActivityID()]++;
			}
			if (mIndicators.get(i).hasCompletedSinging()) {
				counts[BondingActivity.SINGING.getActivityID()]++;
			}
			if (mIndicators.get(i).isClearAll()) {
				counts[BondingActivity.TALKING.getActivityID()]--;
				counts[BondingActivity.READING.getActivityID()]--;
				counts[BondingActivity.TAKE_BABY_WALKING.getActivityID()]--;
				counts[BondingActivity.TUMMY_TIME.getActivityID()]--;
				counts[BondingActivity.SINGING.getActivityID()]--;
			}
		}
		mBondingCounts = Arrays.asList(counts);
	}

	/**
	 * when touching the bar graph, we display a table of the bonding activities
	 */
	protected OnChartTouchListener mChartListener = new OnChartTouchListener() {
		@Override
		public void OnChartTouched(View v, int closestIndex) {
			List<Date> bondingActivityDates = CollectionUtils.getSetAsSortedList(mIndicatorCountsByDate.keySet());
			List<List<String>> bondingFlags = new ArrayList<List<String>>(bondingActivityDates.size());
			for(int i=0; i<bondingActivityDates.size(); i++) {
				bondingFlags.add(mIndicatorCountsByDate.get(bondingActivityDates.get(i)));
			}
			mDialog = BondingDialog(BondingOverview.this, BondingSurvey.getBondingActivityValues(), bondingFlags);
			mDialog.show();
		}
	};

	private AlertDialog BondingDialog(Context context, List<BondingActivity> activities, List<List<String>> activityFlags) {	
		View dialog = this.getLayoutInflater().inflate(R.layout.dialog_chart_table_view_data, (ViewGroup) findViewById(R.id.dialog_layout));    

		TableLayout table = (TableLayout) dialog.findViewById(R.id.data_table);
		TableRow row;
		TextView tv;

		// create the table header, leave first column empty 
		row = new TableRow(context);
		tv = new TextView(context);
		row.addView(tv);
		ImageView iv;
		for (int i=0; i<activities.size(); i++) {
			iv = new ImageView(context);
			iv.setScaleType(ScaleType.FIT_CENTER);
			iv.setAdjustViewBounds(true);
			iv.setMaxHeight(60);
			iv.setMaxWidth(60);
			switch(activities.get(i)) {
			case TALKING:
				iv.setImageResource(R.drawable.talking_icon);
				break;
			case READING:
				iv.setImageResource(R.drawable.reading_icon);
				break;
			case TAKE_BABY_WALKING:
				iv.setImageResource(R.drawable.walking_icon);
				break;
			case TUMMY_TIME:
				iv.setImageResource(R.drawable.tummytime_icon);
				break;
			case SINGING:
				iv.setImageResource(R.drawable.singing_icon);
				break;
			}
			row.addView(iv);
		}
		table.addView(row);

		// create data rows
		for(int i=0; i<activityFlags.size(); i++) {
			row = new TableRow(context);
			tv = new TextView(context);
			switch (mCurrentTimeScale) {
			case WEEK:
				tv.setText(DateUtils.DAYS_OF_WEEK[i]);
				break;
			case MONTH:
				tv.setText("Week" + i);
				break;
			case ALL:
				tv.setText("Month" + i);
				break;
			}
			tv.setTextSize(20);
			row.addView(tv);
			for (int j=0; j<BondingActivity.getSize(); j++) {
				tv = new TextView(context);
				tv.setText(activityFlags.get(i).get(j));
				tv.setTextSize(20);
				tv.setTypeface(null, Typeface.BOLD);
				tv.setGravity(Gravity.CENTER);
				row.addView(tv);
			}
			table.addView(row);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (activityFlags.size() > 0) {
			builder.setView(dialog);
		}
		else {
			builder.setMessage("<no past data>");
		}
		builder.setTitle("Past Bonding Data");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mDialog.dismiss();
			}
		});
		return builder.create();
	}

	View.OnClickListener mUpdateBonding = new OnClickListener() {
		public void onClick(View v) {
			openDatabase();
			BondingSurvey[] bondings = (BondingSurvey[]) mDatabase.getBondingTable().getBabySurveysForToday(mBaby.getId());
			BondingSurvey b = BondingSurvey.flattenBondingArray(bondings);
			if (b == null) {
				b = new BondingSurvey();
			}
			Intent intent = new Intent(BondingOverview.this, BondingForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			intent.putExtra("bonding", b);
			startActivityForResult(intent, BONDING_FORM_ID);
		}
	};

	// checks for results from 'updating bonding' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BONDING_FORM_ID:
				// get bonding info from intent
				BondingSurvey bonding = (BondingSurvey) data.getParcelableExtra("bonding");
				openDatabase();
				mDatabase.insertSingle(bonding);
				data.setClassName(this, this.getClass().getName());
				mDatabase.logInsert(data, bonding);
				if (bonding.getResponseSet(0).size()==0) {
					Toast.makeText(BondingOverview.this, "updated chart: no bonding activities", Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(BondingOverview.this, "updated chart: " + bonding.toString(), Toast.LENGTH_LONG).show();
				}
				// update the chart with the newly inserted data
				showChartData();

				break;
			}
		}
	}
}
