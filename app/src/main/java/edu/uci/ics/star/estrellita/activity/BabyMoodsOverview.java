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
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.chart.TimeBasedLineChart;
import edu.uci.ics.star.estrellita.customview.BabyMoodsListViewDialog;
import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class BabyMoodsOverview extends TileOverviewActivity<BabyMoodSurvey> {
	private static final int BABYMOOD_FORM_ID = 0;

	// for charting
	List<List<Float>> mMoodChartLines;
	List<Integer> mMoodChartColors;

	TimeBasedLineChart mMoodsChart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.DAY);

		// initialize header
		setActivityHeader("Baby's Moods", true, Tile.BABYMOODS);
		setButtonHeader("update baby's mood");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mUpdateMood);

		mIndicators = new ArrayList<BabyMoodSurvey>();
		setIndicatorType(BabyMoodSurvey.class);

		// display chart data
		showChartData();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#showChartData()
	 */
	protected void showChartData() {
		// this calls getChartDataFromDB, formatLineChartData() and convertChartDataToVizData()
		super.getChartData(ChartType.LINE);

		mMoodsChart = new TimeBasedLineChart(this, mMoodChartLines, mMoodChartColors,mCurrentTimeScale, 10f);
		mMoodsChart.setOnChartTouchListener(mChartListener);

		if (mCurrentTimeScale == TimeScale.ALL) {
			mMoodsChart.setXLabels(getMonthStrings());
		}
		else if (mCurrentTimeScale == TimeScale.MONTH) {
			mMoodsChart.setXLabels(getWeekStrings().get(0), getWeekStrings().get(1));
		}
		updateChartView(mMoodsChart);
	}

	/*
	 * grabs all the moods from the database
	 * by default, we'll get a day of moods
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#getChartDataFromDB()
	 */
	protected List<BabyMoodSurvey> getChartDataFromDB() {
		List<BabyMoodSurvey> dbMoods = new ArrayList<BabyMoodSurvey>();
		BabyMoodSurvey[] moods;
		switch(mCurrentTimeScale) {
		case DAY:
		case WEEK:
		case MONTH:
			moods = (BabyMoodSurvey[])mDatabase.getBabyMoodTable().getBabySurveysForTimeRange(mBaby.getId(), mCurrentStartDate, mCurrentEndDate);
			if ( (moods != null) && (moods.length>0) ) {
				dbMoods = Arrays.asList(moods);
				// make sure these moods are sorted (by time)
				Collections.sort(dbMoods);
			}
			break;
		case ALL:
			moods = (BabyMoodSurvey[])mDatabase.getBabyMoodTable().getAllSurveysForBaby(mBaby.getId());
			if ( (moods != null) && (moods.length>0) ) {
				dbMoods = Arrays.asList(moods);
				// make sure these moods are sorted (by time)
				Collections.sort(dbMoods);
				mCurrentStartDate = dbMoods.get(0).getDateTime();
				mCurrentEndDate = dbMoods.get(dbMoods.size()-1).getDateTime();
			}
			break;
		}
		return dbMoods;
	}

	// pulls out the different moods and puts them in a form that is readable by the chart
	protected void convertChartDataToVizData() {
		List<Float> fussyList = new ArrayList<Float>();

		// read data in sorted order
		Collections.sort(mIndicators);

		for (int i = 0; i < mIndicators.size(); i++) {
			fussyList.add(mIndicators.get(i).getAverageFussyness());
		}
		mMoodChartLines = new ArrayList<List<Float>>();
		mMoodChartLines.add(fussyList);

		mMoodChartColors = new ArrayList<Integer>();
		mMoodChartColors.add(Color.RED);
	}

	View.OnClickListener mUpdateMood = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(BabyMoodsOverview.this, BabyMoodForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			startActivityForResult(intent, BABYMOOD_FORM_ID);
		}
	};

	// checks for results from the 'update mood' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BABYMOOD_FORM_ID:
				// get mood info from intent
				BabyMoodSurvey babymood = (BabyMoodSurvey) data.getParcelableExtra("babymood");
				openDatabase();
				mDatabase.insertSingle(babymood);
				data.setClassName(this, this.getClass().getName());
				mDatabase.logInsert(data, babymood);
				// update chart with newly inserted data
				showChartData();

				Toast.makeText(BabyMoodsOverview.this, "updated chart with baby mood (fussyness=" + babymood.getFussyness() + ")", Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#setHighlightBox(int)
	 */
	@Override
	protected void setHighlightBox(int index) {
		mMoodsChart.setHighlightBox(index);

		// get the moods for this index
		List<BabyMoodSurvey> babymoods = super.getDataAtIndex(index);
		boolean showDialog = false;
		if (babymoods.size() > 0) {
			for (int i=0; i<babymoods.size(); i++) {
				if (babymoods.get(i).getCommonData().hasNotes()) {
					showDialog = true;
					break;
				}
			}
			if (showDialog) {
				String title = "Notes for " + mBaby.getName() + "'s Moods \n";
				Date endDate;
				Date startDate = babymoods.get(0).getDateTime();
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
				Dialog d = new BabyMoodsListViewDialog(this, title, babymoods, mCurrentTimeScale);
				d.setCancelable(true);
				d.show();
			}
		}
	}
}
