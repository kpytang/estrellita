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
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.chart.PrettyPrintTable;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.GenericIndicator.IndicatorType;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CustomTableOverview extends TileOverviewActivity<GenericIndicator> {
	private static final int CUSTOMCHART_ADDVALUE_FORM_ID = 0;
	
	String mChartTitle, mChartUnits;
	PrettyPrintTable mTable;
	
	List<String> mHeaders;
	List<List<String>> mData;
	GenericIndicator mNewGenericIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.DAY);

		// get chart title from intent
		mChartTitle = getIntent().getStringExtra("title");
		mChartUnits = getIntent().getStringExtra("units");
		
		// initialize header
		setActivityHeader(mChartTitle, true, Tile.CUSTOMCHARTS);
		setButtonHeader("add data to this table");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mAddNewData);
		
		mIndicators = new ArrayList<GenericIndicator>();
		setIndicatorType(GenericIndicator.class);
		
		// display chart data
		showChartData();
	}
	
	@Override
	protected void showChartData() {
		// this calls getChartDataFromDB, formatLineChartData() and convertChartDataToVizData()
		// convertChartDataToVizData() will create the table
		super.getChartData(ChartType.TABLE);
		
		List<Integer> indexes = new ArrayList<Integer>();
		indexes.add(2);
		mTable = new PrettyPrintTable(this, mHeaders, mData, indexes);
		mTable.setStretchable(2);
		updateChartView(mTable.getScrollView());
	}

	/*
	 * grabs all the indicators from the database
	 * by default, we'll get a week of indicators
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#getChartDataFromDB()
	 */
	@Override
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
				if (dbIndicators.get(i).getDataAsString().length() == 0) {
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
				if (dbIndicators.get(i).getDataAsString().length() == 0) {
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

	/*
	 * create the table in this screen
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#convertChartDataToVizData()
	 */
	@Override
	protected void convertChartDataToVizData() {
		mHeaders = new ArrayList<String>();
		mHeaders.add("Date");
		mHeaders.add("Time");
		mHeaders.add("Value");
		
		// make sure the indicators are sorted with most recent first
		Collections.sort(mIndicators);
		Collections.reverse(mIndicators);
		
		mData = new ArrayList<List<String>>();
		for(int i=0; i<mIndicators.size(); i++) {
			GenericIndicator indicator = (GenericIndicator) mIndicators.get(i);
			
			mData.add(new ArrayList<String>());
			mData.get(i).add(DateUtils.getDateAsString(indicator.getCommonData().getTimestamp(), DateUtils.APPOINTMENT_SHORT_DATE_FORMAT));
			mData.get(i).add(DateUtils.getDateAsString(indicator.getCommonData().getTimestamp(), DateUtils.APPOINTMENT_TIME_FORMAT));
			mData.get(i).add(indicator.getDataAsString());
		}
	}
	
	View.OnClickListener mAddNewData = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(CustomTableOverview.this, CustomChartValueForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			intent.putExtra("title", mChartTitle);
			intent.putExtra("units", mChartUnits);
			intent.putExtra("type", IndicatorType.TEXT.name());
			startActivityForResult(intent, CUSTOMCHART_ADDVALUE_FORM_ID);
		}
	};
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("indicator", mNewGenericIndicator);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}

	// checks for results from the 'update mood' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CUSTOMCHART_ADDVALUE_FORM_ID:
				// get indicator info from intent
				mNewGenericIndicator = (GenericIndicator) data.getParcelableExtra("indicator");
				
				openDatabase();
				// insert new data into database
				mDatabase.insertSingle(mNewGenericIndicator);
				data.setClassName(this, this.getClass().getName());
				mDatabase.logInsert(data, mNewGenericIndicator);
				// update chart with newly inserted data
				showChartData();

				Toast.makeText(CustomTableOverview.this, "updated table: " + mNewGenericIndicator.getIndicatorString(), Toast.LENGTH_LONG).show();
				break;
			}
		}
	}
}
