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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.chart.BaseChart;
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.chart.TimeBasedLineChart;
import edu.uci.ics.star.estrellita.customview.GenericIndicatorsListViewDialog;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.GenericIndicator.IndicatorType;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CustomChartOverview extends TileOverviewActivity<GenericIndicator> {
	private static final int CUSTOMCHART_ADDVALUE_FORM_ID = 0;

	List<List<Float>> mChartLines;
	List<Integer> mChartColors;
	String mChartTitle, mChartUnits;

	TimeBasedLineChart mChart;
	GenericIndicator mNewGenericIndicator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.WEEK);

		// get chart title from intent
		mChartTitle = getIntent().getStringExtra("title");
		mChartUnits = getIntent().getStringExtra("units");

		// initialize header
		setActivityHeader(mChartTitle, true, Tile.CUSTOMCHARTS);
		setButtonHeader("add data to this chart");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mAddNewData);

		mIndicators = new ArrayList<GenericIndicator>();
		setIndicatorType(GenericIndicator.class);
		setTimeScaleOptions(BaseChart.DAY_TIME_SCALES, 1);

		// display chart data
		showChartData();
	}

	protected void showChartData() {
		// this calls getChartDataFromDB, formatLineChartData() and convertChartDataToVizData()
		super.getChartData(ChartType.LINE);

		mChart = new TimeBasedLineChart(this, mChartLines, mChartColors, mCurrentTimeScale);
		mChart.setOnChartTouchListener(mChartListener);

		if (mCurrentTimeScale == TimeScale.ALL) {
			mChart.setXLabels(getMonthStrings());
		}
		else if (mCurrentTimeScale == TimeScale.MONTH) {
			mChart.setXLabels(getWeekStrings().get(0), getWeekStrings().get(1));
		}
		
		updateChartView(mChart);
	}

	/*
	 * grabs all the generic indicators from the database
	 * by default, we'll get a week of indicators
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#getChartDataFromDB()
	 */
	protected List<GenericIndicator> getChartDataFromDB() {
		List<GenericIndicator> dbIndicators = new ArrayList<GenericIndicator>();
		List<GenericIndicator> temp = null;
		switch(mCurrentTimeScale) {
		case DAY:
		case WEEK:
		case MONTH:
			dbIndicators = Arrays.asList(mDatabase.getGenericIndicatorTable().getGenericIndicatorsByTitleForTimeRange(mBaby.getId(), mChartTitle, mCurrentStartDate, mCurrentEndDate));
			temp = new ArrayList<GenericIndicator>(dbIndicators);
			// remove indicators that have no data (these were the null one added when the chart was first created)
			for (int i=0; i<dbIndicators.size(); i++) {
				if (dbIndicators.get(i).getDataAsFloat() == null) {
					temp.remove(dbIndicators.get(i));
				}
			}
			// make sure these are sorted (by time)
			dbIndicators = temp;
			Collections.sort(dbIndicators);
			break;
		case ALL:
			dbIndicators = Arrays.asList(mDatabase.getGenericIndicatorTable().getAllGenericIndicatorsByTitle(mBaby.getId(), mChartTitle));
			temp = new ArrayList<GenericIndicator>(dbIndicators);
			// remove indicators that have no data (these were the null one added when the chart was first created)
			for (int i=0; i<dbIndicators.size(); i++) {
				if (dbIndicators.get(i).getDataAsFloat() == null) {
					temp.remove(dbIndicators.get(i));
				}
			}
			// make sure these are sorted (by time)
			dbIndicators = temp;
			Collections.sort(dbIndicators);
			mCurrentStartDate = dbIndicators.get(0).getDateTime();
			mCurrentEndDate = dbIndicators.get(dbIndicators.size()-1).getDateTime();
			break;
		}
		return dbIndicators;
	}

	// pulls out the different indicators and initializes them as chart data
	protected void convertChartDataToVizData() {
		List<Float> data = new ArrayList<Float>();

		// read data in sorted order
		Collections.sort(mIndicators);

		for (int i = 0; i < mIndicators.size(); i++) {
			Float f = mIndicators.get(i).getDataAsFloat();
			if (f != null) {
				data.add(f);
			}
			else {
				data.add(-1f);
			}
		}

		mChartLines = new ArrayList<List<Float>>();
		mChartLines.add(data);

		mChartColors = new ArrayList<Integer>();
		mChartColors.add(Color.RED);
	}

	View.OnClickListener mAddNewData = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(CustomChartOverview.this, CustomChartValueForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			intent.putExtra("title", mChartTitle);
			intent.putExtra("units", mChartUnits);
			intent.putExtra("type", IndicatorType.NUMERIC.name());
			startActivityForResult(intent, CUSTOMCHART_ADDVALUE_FORM_ID);
		}
	};

	// checks for results from 'update chart' form (you're getting an extra data point back)
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CUSTOMCHART_ADDVALUE_FORM_ID:
				// get indicator info from intent
				mNewGenericIndicator = (GenericIndicator) data.getParcelableExtra("indicator");
				// insert new data into database
				openDatabase();
				mDatabase.insertSingle(mNewGenericIndicator);
				data.setClassName(this, this.getClass().getName());
				mDatabase.logInsert(data, mNewGenericIndicator);
				// update chart with newly inserted data
				showChartData();

				Toast.makeText(CustomChartOverview.this, "updated chart: " + mNewGenericIndicator.getIndicatorString(), Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("indicator", mNewGenericIndicator);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}

	@Override
	protected void setHighlightBox(int index) {
		mChart.setHighlightBox(index);
		
		// get the moods for this index
		List<GenericIndicator> indicators = super.getDataAtIndex(index);
		boolean showDialog = false;
		if (indicators.size() > 0) {
			for (int i=0; i<indicators.size(); i++) {
				if (indicators.get(i).getCommonData().hasNotes()) {
					showDialog = true;
					break;
				}
			}
			if (showDialog) {
				String title = "Notes\n";
				Date endDate;
				Date startDate = indicators.get(0).getDateTime();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startDate);
				switch(mCurrentTimeScale) {
				case DAY:
					endDate = DateUtils.addToDate(startDate, Calendar.HOUR, 1);
					title += DateUtils.getDateAsString(startDate, DateUtils.APPOINTMENT_SHORT_DATE_FORMAT);
					title += ", (" + DateUtils.getDateAsString(startDate, DateUtils.HOUR_ONLY);
					title += "-" + DateUtils.getDateAsString(endDate, DateUtils.HOUR_ONLY) + ")";
					break;
				case WEEK:
					title += DateUtils.getDateAsString(startDate, DateUtils.DATE_HEADER_FORMAT);
					break;
				case MONTH:
					startDate = DateUtils.getNthDayOfWeek(calendar, Calendar.SUNDAY);
					title += DateUtils.getDateAsString(startDate, DateUtils.MONTH_AND_DAY_ONLY);
					endDate = DateUtils.getNthDayOfWeek(calendar, Calendar.SATURDAY);
					title += "-" + DateUtils.getDateAsString(endDate, DateUtils.DAY_AND_YEAR_ENDING);
					break;
				case ALL:
					title += DateUtils.getDateAsString(startDate, DateUtils.MONTH_AND_YEAR);
					break;
				}
				Dialog d = new GenericIndicatorsListViewDialog(this, title, indicators, mCurrentTimeScale);
				d.setCancelable(true);
				d.show();
			}
		}
	}
}