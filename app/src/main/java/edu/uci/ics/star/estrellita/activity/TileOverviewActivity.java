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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.chart.BaseChart;
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.OnChartTouchListener;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.customview.ChartDialogListAdapter;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey.BondingActivity;
import edu.uci.ics.star.estrellita.object.indicator.Diaper;
import edu.uci.ics.star.estrellita.object.indicator.Weight;
import edu.uci.ics.star.estrellita.sharedprefs.api.ReminderPreferences;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public abstract class TileOverviewActivity<T extends Indicator> extends TileActivity<T> {
	private static final int CHART_LAYOUT_RESOURCE = R.layout.chart_overview;
	private static final int CHART_ID_RESOURCE = R.id.chart;

	private static final String NO_INDICATOR_REPORTED_MARKER = "-";
	private static final String INDICATOR_REPORTED_MARKER = "X";

	LinearLayout mChartLayout;
	LinearLayout mChartTimeScaleLayout;
	Button mChartTimeScalePrevButton, mChartTimeScaleNextButton;

	// for the chart data
	Class<T> mIndicatorType;
	List<T> mIndicators, mDbIndicators;
	Map<Date, List<String>> mIndicatorCountsByDate;
	Integer mBarChartBins;

	// default time scale = "this week"
	TimeScale mCurrentTimeScale, mDefaultTimeScale;
	int mCurrentTimeScaleOption = 0;
	String[] mTimeScaleOptions;
	Date mCurrentStartDate, mCurrentEndDate, mEarliestDate;

	// for chart dialog
	List<String> mDialogDates;
	int mNearestIndicatorsDialogId = -1;

	protected void onCreate(Bundle savedInstanceState, TimeScale scale) {
		onCreate(savedInstanceState, scale, CHART_LAYOUT_RESOURCE, CHART_ID_RESOURCE);
	}

	protected void onCreate(Bundle savedInstanceState, TimeScale scale, int layoutResource, int vizId) {
		super.onCreate(savedInstanceState);
		setContentView(layoutResource);

		// initialize database & chart layout
		openDatabase();
		mEarliestDate = ReminderPreferences.getInitialInstallDate(this);

		// initialize time scales
		// if it's a monthly chart: we'll load this month's data by default
		// if it's a weekly chart: we'll load this week's data by default
		// if it's a daily chart: we'll load today's data by default
		mCurrentStartDate = DateUtils.getTimestamp();
		mDefaultTimeScale = scale;
		switch (mDefaultTimeScale) {
		case DAY:
			mTimeScaleOptions = BaseChart.DAY_TIME_SCALES;
			mCurrentTimeScale = TimeScale.DAY;
			break;
		case WEEK:
			mTimeScaleOptions = BaseChart.WEEK_TIME_SCALES;
			mCurrentTimeScale = TimeScale.WEEK;
			break;
		case MONTH:
			mTimeScaleOptions = BaseChart.MONTH_TIME_SCALES;
			mCurrentTimeScale = TimeScale.MONTH;
			break;
		}
		getTimeRange();

		mChartLayout = (LinearLayout)this.findViewById(vizId);
		mChartTimeScaleLayout = (LinearLayout) this.findViewById(R.id.chart_time_scale);
		mChartTimeScaleLayout.setClickable(true);
		mChartTimeScaleLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Dialog d = TimeScaleDialog(v.getContext());
				d.show();
			}       
		});
		mChartTimeScalePrevButton = (Button) this.findViewById(R.id.chart_time_scale_prev);
		updatePrevButton();
		mChartTimeScaleNextButton = (Button) this.findViewById(R.id.chart_time_scale_next);
		updateNextButton();

		// show the right dates in the chart
		setChartHeader();
	}

	public void updateTimeScale(int stepSize) {
		int field = Calendar.DAY_OF_WEEK;
		switch (mCurrentTimeScale) {
		case DAY:
			field = Calendar.DAY_OF_WEEK;
			break;
		case WEEK:
			field = Calendar.WEEK_OF_MONTH;
			break;
		case MONTH:
			field = Calendar.MONTH;
			break;
		}
		mCurrentStartDate = DateUtils.addToDate(mCurrentStartDate, field, stepSize);
		getTimeRange();

		setChartHeader();
		showChartData();
	}

	public void setIndicatorType(Class<T> type) {
		mIndicatorType = type;
	}

	public void setTimeScaleOptions(String[] options, int index) {
		mTimeScaleOptions = options;
		mCurrentTimeScaleOption = index;
		setChartHeader();
	}

	private void setChartHeader() {
		TextView tv = (TextView) mChartTimeScaleLayout.findViewById(R.id.chart_time_scale_label);
		int nUnitsSince = DateUtils.getNumberOfUnitsSinceToday(mCurrentStartDate, mCurrentTimeScale);
		switch(mCurrentTimeScale) {
		case DAY:
			if (nUnitsSince == 0) {
				tv.setText("for today");
			}
			else if (nUnitsSince == 1) {
				tv.setText("for yesterday");
			}
			else {
				tv.setText("for a previous day");
			}
			break;
		case WEEK:
			if (nUnitsSince == 0) {
				tv.setText("for this week");
			}
			else if (nUnitsSince == 1) {
				tv.setText("for last week");
			}
			else {
				tv.setText("for a previous week");
			}
			break;
		case MONTH:
			if (nUnitsSince == 0) {
				tv.setText("for this month");
			}
			else if (nUnitsSince == 1) {
				tv.setText("for last month");
			}
			else {
				tv.setText("for a previous month");
			}
			break;
		case ALL:
			tv.setText("for all");
			break;
		}

		tv = (TextView) mChartTimeScaleLayout.findViewById(R.id.chart_time_scale_dates);
		switch(mCurrentTimeScale) {
		case DAY:
			tv.setText("(" + DateUtils.getDateAsString(mCurrentStartDate, DateUtils.APPOINTMENT_SHORT_DATE_FORMAT) + ")");
			break;
		case WEEK:
			tv.setText("(" + DateUtils.getDateAsString(mCurrentStartDate, DateUtils.APPOINTMENT_SHORT_DATE_FORMAT) + " - " + 
					DateUtils.getDateAsString(mCurrentEndDate, DateUtils.APPOINTMENT_SHORT_DATE_FORMAT) + ")");
			break;
		case MONTH:
			tv.setText("(" + DateUtils.getDateAsString(mCurrentStartDate, DateUtils.MONTH_AND_YEAR) + ")");
			break;
		case ALL:
			tv.setText("(since " + DateUtils.getDateAsString(ReminderPreferences.getInitialInstallDate(this), DateUtils.MONTH_AND_YEAR) + ")");
			break;
		}
		updatePrevButton();
		updateNextButton();
	}

	private void updatePrevButton() {
		switch(mCurrentTimeScale) {
		case ALL:
			mChartTimeScalePrevButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.previous_disabled));
			mChartTimeScalePrevButton.setClickable(false);
			mChartTimeScalePrevButton.setOnClickListener(null);
			break;
		default:
			mChartTimeScalePrevButton.setClickable(true);
			mChartTimeScalePrevButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// allow users to update the time scale to infinity
					updateTimeScale(-1);
					int nUnitsSince = DateUtils.getNumberOfUnitsSinceToday(mCurrentStartDate, mCurrentTimeScale);
					if (nUnitsSince > 0) {
						mChartTimeScaleNextButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.next));
						mChartTimeScaleNextButton.setClickable(true);
						mChartTimeScaleNextButton.setOnClickListener(mChartTimeScaleNextOnClickListener);
					}
				}
			});
			break;
		}
	}

	private void updateNextButton() {
		switch(mCurrentTimeScale) {
		case ALL:
			mChartTimeScaleNextButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.next_disabled));
			mChartTimeScaleNextButton.setClickable(false);
			mChartTimeScaleNextButton.setOnClickListener(null);
			break;
		default:
			int nUnitsSince = DateUtils.getNumberOfUnitsSinceToday(mCurrentStartDate, mCurrentTimeScale);
			if (nUnitsSince < 1) {
				mChartTimeScaleNextButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.next_disabled));
				mChartTimeScaleNextButton.setClickable(false);
				mChartTimeScaleNextButton.setOnClickListener(null);
			}
			else {
				mChartTimeScaleNextButton.setClickable(true);
				mChartTimeScaleNextButton.setClickable(true);
				mChartTimeScaleNextButton.setOnClickListener(mChartTimeScaleNextOnClickListener);
			}
			break;
		}
	}

	private View.OnClickListener mChartTimeScaleNextOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int nUnitsSince = DateUtils.getNumberOfUnitsSinceToday(mCurrentStartDate, mCurrentTimeScale);
			if (nUnitsSince > 0) {
				updateTimeScale(1);
			}
			nUnitsSince = DateUtils.getNumberOfUnitsSinceToday(mCurrentStartDate, mCurrentTimeScale);
			if (nUnitsSince < 1) {
				mChartTimeScaleNextButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.next_disabled));
				mChartTimeScaleNextButton.setClickable(false);
			}
		}
	};

	protected void getChartData(ChartType chartType) {
		getTimeRange();
		openDatabase();
		mDbIndicators = getChartDataFromDB();
		switch(chartType) {
		case LINE:
			formatLineChartData();
			break;
		case BAR:
			mIndicators = mDbIndicators;
			getBarChartDataTable();
			break;
		case HEATMAP:
		case TABLE:
			mIndicators = mDbIndicators;
			break;
		}

		convertChartDataToVizData();
	}

	// WEEK_TIME_SCALES = { "this week", "this month", "last week", "last month", "all dates" };
	// adjust start & end dates based on the time scale
	private void getTimeRange() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mCurrentStartDate);
		switch(mCurrentTimeScale) {
		case DAY:
			mCurrentEndDate = DateUtils.getEndOfDay(mCurrentStartDate);
			mCurrentStartDate = DateUtils.getStartOfDay(mCurrentStartDate);
			break;
		case WEEK:
			mCurrentStartDate = DateUtils.getNthDayOfWeek(calendar, Calendar.SUNDAY);
			mCurrentEndDate = DateUtils.getNthDayOfWeek(calendar, Calendar.SATURDAY);
			break;
		case MONTH:
		case ALL:
			mCurrentStartDate = DateUtils.getNthDayOfMonth(calendar, 1); 
			mCurrentEndDate = DateUtils.getNthDayOfMonth(calendar, DateUtils.getNumberOfDaysInMonth(mCurrentStartDate));
			break;
		}
	}

	abstract void showChartData();

	abstract List<T> getChartDataFromDB();

	protected void formatLineChartData() {
		List<List<T>> runningAverageBins = new ArrayList<List<T>>();
		switch(mCurrentTimeScale) {
		// if we're plotting for a day, we need 24 data points (one for each hr)
		case DAY:
			for (int i=0; i<24; i++) {
				runningAverageBins.add(new ArrayList<T>());
			}
			// go through each db indicator and figure out which bin it should go in (based on which week it falls in)
			for (int i=0; i<mDbIndicators.size(); i++) {
				int bin = DateUtils.getHourOfDay(mDbIndicators.get(i).getDateTime());
				runningAverageBins.get(bin).add(mDbIndicators.get(i));
			}
			break;
			// if we're plotting for a week, we need 7 data points (one for each day)
		case WEEK:
			for (int i=0; i<7; i++) {
				runningAverageBins.add(new ArrayList<T>());
			}
			// go through each db indicator and figure out which bin it should go in (based on which week it falls in)
			for (int i=0; i<mDbIndicators.size(); i++) {
				int bin = DateUtils.getDayOfWeek(mDbIndicators.get(i).getDateTime());
				// subtract 1 because counting starts from 1, not 0
				runningAverageBins.get(bin-1).add(mDbIndicators.get(i));
			}
			break;
			// if we're plotting for a month, we need 4 or 5 data points (one for each week)
		case MONTH:
			int size = DateUtils.getNumberOfWeeksInMonth(mCurrentStartDate);
			for (int i=0; i<size; i++) {
				runningAverageBins.add(new ArrayList<T>());
			}
			// go through each db indicator and figure out which bin it should go in (based on which week it falls in)
			for (int i=0; i<mDbIndicators.size(); i++) {
				int bin = DateUtils.getWeekOfMonth(mDbIndicators.get(i).getDateTime());
				// subtract 1 because counting starts from 1, not 0
				runningAverageBins.get(bin-1).add(mDbIndicators.get(i));
			}
			break;
			// if we're plotting everything, we need to first figure out what the total range between the oldest point and today is
			// then we need to figure out how many bins there are, based on the number of months between then and now
			// if there are no points, then we just have an empty indicator list for the chart
		case ALL:
			// create bins for each month of the year to start with
			runningAverageBins = new ArrayList<List<T>>();
			int range = DateUtils.getMonthDiff(mCurrentStartDate, mCurrentEndDate);
			for (int i=0; i<=range; i++) {
				runningAverageBins.add(new ArrayList<T>());
			}
			if (mDbIndicators.size()>0) {
				// go through each db indicator and figure out which bin it should go in (based on which month it's in)
				for (int i=0; i<mDbIndicators.size(); i++) {
					int bin = DateUtils.getMonthDiff(mCurrentStartDate, mDbIndicators.get(i).getDateTime());
					runningAverageBins.get(bin).add(mDbIndicators.get(i));
				}
			}
			break;
		}

		int start = 0;
		int end = runningAverageBins.size();

		mDialogDates = new ArrayList<String>();
		List<T> chartData = new ArrayList<T>();

		String runningAverageDates = "";
		// compute averages for each bin
		for (int i=start; i<end; i++) {
			List<T> list = runningAverageBins.get(i);
			// if there's nothing in this bin, then it means there was nothing reported during this week
			// so we create a filler diaper with -1's
			if (list.size() == 0) {
				T filler = getFillerIndicator();
				if (mCurrentTimeScale == TimeScale.ALL) {
					filler.getCommonData().setTimestamp(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i-start));
				}
				else if (mCurrentTimeScale == TimeScale.MONTH) {
					filler.getCommonData().setTimestamp(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i));
				}
				else {
					filler.getCommonData().setTimestamp(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i-1));
				}
				chartData.add(filler);
				mDialogDates.add(DateUtils.getDateAsString(filler.getDateTime(), DateUtils.DATE_AND_TIME_FORMAT_FULL));
			}
			// else there's at least one value here, so we can compute the average for this week and use that as the chart data
			else {
				if (list.size()>1) {
					runningAverageDates = "averaged across ";
					switch(mCurrentTimeScale) {
					case DAY:
						runningAverageDates += "an hour";
						break;
					case WEEK:
						runningAverageDates += "a day";
						break;
					case MONTH:
						runningAverageDates += "a week";
						break;
					case ALL:
						runningAverageDates += "this study";
						break;
					}
					runningAverageDates += ":\n";
				}
				List<T> runningAverageIndicators = new ArrayList<T>();
				for (int j=0; j<list.size(); j++) {
					runningAverageIndicators.add(list.get(j));
					if (mCurrentTimeScale == mDefaultTimeScale) {
						runningAverageDates += DateUtils.getDateAsString(list.get(j).getDateTime(), DateUtils.DATE_AND_TIME_FORMAT_FULL);
					} 
					else {
						runningAverageDates += DateUtils.getDateAsString(list.get(j).getDateTime(), DateUtils.DATE_AND_TIME_FORMAT_FULL) + " (" + getChartString(list.get(j)) + ")";
					}
					if (list.get(j).getCommonData().hasNotes()) {
						runningAverageDates += "*";
					}
					else if (list.get(j).getCommonData().hasPhotos()) {
						runningAverageDates += "*";
					}
					runningAverageDates += "\n";
				}
				T avgIndicator;
				avgIndicator = getAverageIndicator(runningAverageIndicators);
				if (mCurrentTimeScale == TimeScale.ALL) {
					avgIndicator.getCommonData().setTimestamp(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i-start));
				}
				else if (mCurrentTimeScale == TimeScale.MONTH) {
					avgIndicator.getCommonData().setTimestamp(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i));
				}
				else {
					avgIndicator.getCommonData().setTimestamp(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i-1));
				}

				chartData.add(avgIndicator);
				mDialogDates.add(runningAverageDates);
				runningAverageDates = "";
			}
		}

		mIndicators = chartData;
	}

	private Timestamp getNewTimestamp(Date startDate, TimeScale timeScale, int offset) {
		switch(timeScale) {
		case DAY:
			return new Timestamp(DateUtils.addToDate(startDate, Calendar.HOUR, offset).getTime());
		case WEEK:
			return new Timestamp(DateUtils.addToDate(startDate, Calendar.DAY_OF_MONTH, offset).getTime());
		case MONTH:
			return new Timestamp(DateUtils.addToDate(startDate, Calendar.WEEK_OF_MONTH, offset).getTime());
		case ALL:
			return new Timestamp(DateUtils.addToDate(startDate, Calendar.MONTH, offset).getTime());
		default:
			// the offset is actually the month of the new point, so let's calculate the actual offset here
			int startIndex = DateUtils.getMonthOfYear(startDate);
			offset = (offset + 1) - startIndex; // compensating for the -1 that's being passed in
			return new Timestamp(DateUtils.addToDate(startDate, Calendar.MONTH, offset).getTime());
		}
	}

	@SuppressWarnings("unchecked")
	private T getFillerIndicator() {
		if (mIndicatorType.equals(Diaper.class)) {
			Diaper filler = new Diaper();
			filler.setWet(-1);
			filler.setDirty(-1);
			return (T) filler;
		}
		else if (mIndicatorType.equals(BabyMoodSurvey.class)) {
			BabyMoodSurvey filler = new BabyMoodSurvey();
			filler.addFussyReport(-1);
			return (T) filler;
		}
		else if (mIndicatorType.equals(Weight.class)) {
			Weight filler = new Weight();
			filler.setWeight(-1);
			return (T) filler;
		}
		else if (mIndicatorType.equals(GenericIndicator.class)) {
			GenericIndicator filler = new GenericIndicator();
			filler.setData("-1");
			return (T) filler;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private T getAverageIndicator(List<T> indicators) {
		if (indicators.size() > 0) {
			T indicator = indicators.get(0);
			if (indicator instanceof Diaper) {
				Diaper avgDiaper = new Diaper();
				avgDiaper.setWet(0);
				avgDiaper.setDirty(0);
				for (int i=0; i<indicators.size(); i++) {
					Diaper diaper = (Diaper) indicators.get(i);
					avgDiaper.setWet(avgDiaper.getWet() + diaper.getWet());
					avgDiaper.setDirty(avgDiaper.getDirty() + diaper.getDirty());
				}
				avgDiaper.setWet(avgDiaper.getWet()/indicators.size());
				avgDiaper.setDirty(avgDiaper.getDirty()/indicators.size());
				return (T) avgDiaper;
			}
			else if (indicator instanceof BabyMoodSurvey) {
				BabyMoodSurvey avgMood = new BabyMoodSurvey();
				avgMood.addFussyReport(0);
				for (int i=0; i<indicators.size(); i++) {
					BabyMoodSurvey mood = (BabyMoodSurvey) indicators.get(i);
					avgMood.addFussyReport(avgMood.getFussyness() + mood.getFussyness());
				}
				float average = (float)avgMood.getFussyness()/(float)indicators.size();
				avgMood.addAverageFussyReport(average);
				return (T) avgMood;
			}
			else if (indicator instanceof Weight) {
				Weight avgWeight = new Weight();
				avgWeight.setPounds(0);
				avgWeight.setOunces(0);
				for (int i=0; i<indicators.size(); i++) {
					Weight weight = (Weight) indicators.get(i);
					avgWeight.setPounds(avgWeight.getPounds() + weight.getPounds());
					avgWeight.setOunces(avgWeight.getOunces() + weight.getOunces());
				}
				avgWeight.setPounds(avgWeight.getPounds()/indicators.size());
				avgWeight.setOunces(avgWeight.getOunces()/indicators.size());
				return (T) avgWeight;
			}
			// for generic indicators, things get tricky because there are two types of indicators: a chartable indicator and a table indicator
			// chartable indicators have numbers, so we'll do the averages mathematically.
			// table indicators only have strings, so the "average" indicator will just have a comma-delimited list of all the individual strings
			else if (indicator instanceof GenericIndicator) {
				boolean chartable = ((GenericIndicator) indicator).isDataNumeric();
				String s = "";
				Float f = 0f;
				for (int i=0; i<indicators.size(); i++) {
					GenericIndicator gi = (GenericIndicator)indicators.get(i);
					if (chartable) {
						f += gi.getDataAsFloat();
					}
					else {
						s += gi.getDataAsString() + GenericIndicator.LIST_DELIMITER;
					}
				}

				GenericIndicator avgIndicator = new GenericIndicator();
				if (chartable) {
					f /= (float)indicators.size();
					avgIndicator.setData(new DecimalFormat("#.##").format(f));
				}
				else {
					s = s.substring(0, s.length()-1);
					avgIndicator = new GenericIndicator();
					avgIndicator.setData(s);
				}
				return (T) avgIndicator;
			}
		}
		return null;
	}

	protected void getBarChartDataTable() {
		List<List<T>> runningCounts = new ArrayList<List<T>>();
		// now we need to put db indicator into bins (for each day, week, or month) & figure out the totals for each bin
		switch(mCurrentTimeScale) {
		// if we're plotting for a week, we need 7 data points (one for each day) & we just need to keep track if bonding was done or not (no count is needed)
		case WEEK:
			// put each of db indicator into the right bin
			for (int i=0; i<7; i++) {
				runningCounts.add(new ArrayList<T>());
			}
			// go through each db indicator and figure out which bin it should go in (based on which week it falls in)
			for (int i=0; i<mDbIndicators.size(); i++) {
				int bin = DateUtils.getDayOfWeek(mDbIndicators.get(i).getDateTime());
				// subtract 1 because counting starts from 1, not 0
				runningCounts.get(bin-1).add(mDbIndicators.get(i));
			}
			break;
			// if we're plotting for a month, we only need 4 or 5 data points (depending on the number of weeks in that month)
			// and for each data point, we'll show the total times each indicator was completed
		case MONTH:
			// create bins for each week of the month
			int size = DateUtils.getNumberOfWeeksInMonth(mCurrentStartDate);
			for (int i=0; i<size; i++) {
				runningCounts.add(new ArrayList<T>());
			}
			// go through each db indicator and figure out which bin it should go in (based on which week it falls in)
			for (int i=0; i<mDbIndicators.size(); i++) {
				int bin = DateUtils.getWeekOfMonth(mDbIndicators.get(i).getDateTime());
				// subtract 1 because counting starts from 1, not 0
				runningCounts.get(bin-1).add(mDbIndicators.get(i));
			}
			break;
			// if we're plotting everything, we need to first figure out what the total range between the oldest point and today is
			// then we need to figure out how many bins there are, based on the number of months between then and now
			// if there are no points, then we just have an empty indicators list for the chart
		case ALL:
			if (mDbIndicators.size()>0) {
				// create bins for each month of the year to start with
				for (int i=0; i<12; i++) {
					runningCounts.add(new ArrayList<T>());
				}
				// go through each db indicator and figure out which bin it should go in (based on which month it's in)
				for (int i=0; i<mDbIndicators.size(); i++) {
					int bin = DateUtils.getMonthOfYear(mDbIndicators.get(i).getDateTime());
					runningCounts.get(bin).add(mDbIndicators.get(i));
				}
			}
			break;
		}

		// tally up the counts for each bin and only look at months between first & last db indicator
		int start = 0;
		if (mCurrentTimeScale.equals(TimeScale.ALL)) {
			start = DateUtils.getMonthOfYear(mCurrentStartDate);
		}
		int end = runningCounts.size();
		// we want to include the last month, so we add the +1 (like the difference between last char & size of an array)
		if (mCurrentTimeScale.equals(TimeScale.ALL)) {
			end = DateUtils.getMonthOfYear(mCurrentEndDate) + 1; 
		}
		mIndicatorCountsByDate = new HashMap<Date, List<String>>();
		List<String> flags;
		for (int i=start; i<end; i++) {
			flags = getARowOfBarChartDataTable(runningCounts.get(i), mCurrentTimeScale);
			mIndicatorCountsByDate.put(getNewTimestamp(mCurrentStartDate, mCurrentTimeScale, i), flags);
		}
	}

	private List<String> getARowOfBarChartDataTable(List<T> indicators, TimeScale scale) {
		Integer counts[] = new Integer[mBarChartBins];
		for (int i=0; i<counts.length; i++) {
			counts[i] = 0;
		}
		List<String> flags = new ArrayList<String>();
		// if there's nothing in this bin, then it means there were no indicators reported 
		// so we set the strings to all be 0's
		if (indicators.size() == 0) {
			for (int i=0; i<mBarChartBins; i++) {
				flags.add(NO_INDICATOR_REPORTED_MARKER);
			}
		}
		// else there's at least one indicator reported, so we should step through each one and 
		// add up the number of reports for each indicator
		else {
			if (indicators.get(0) instanceof BondingSurvey) {
				for (int i=0; i<indicators.size(); i++) {
					BondingSurvey bonding = (BondingSurvey) indicators.get(i);
					if (bonding.hasCompletedReading()) {
						counts[BondingActivity.READING.getActivityID()]++;
					}
					if (bonding.hasCompletedSinging()) {
						counts[BondingActivity.SINGING.getActivityID()]++;
					}
					if (bonding.hasCompletedTalking()) {
						counts[BondingActivity.TALKING.getActivityID()]++;
					}
					if (bonding.hasCompletedWalking()) {
						counts[BondingActivity.TAKE_BABY_WALKING.getActivityID()]++;
					}
					if (bonding.hasCompletedTummyTime()) {
						counts[BondingActivity.TUMMY_TIME.getActivityID()]++;
					}
				}
				for (int i=0; i<BondingActivity.getSize(); i++) {
					if (scale.equals(TimeScale.WEEK)) {
						if (counts[i] == 0) {
							flags.add(NO_INDICATOR_REPORTED_MARKER);
						}
						else {
							flags.add(INDICATOR_REPORTED_MARKER);
						}
					}
					flags.add(counts[i].toString());
				}
			}
		}
		return flags;
	}

	abstract void convertChartDataToVizData();

	protected void updateChartView(View chart) {
		mChartLayout.removeAllViews();
		mChartLayout.addView(chart);
	}

	// WEEK_TIME_SCALES = { "this week", "this month", "last week", "last month", "all" };
	protected AlertDialog TimeScaleDialog(Context context) {    	
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("change time scale to show:");
		builder.setSingleChoiceItems(mTimeScaleOptions, mCurrentTimeScaleOption, new DialogInterface.OnClickListener()  {
			public void onClick(DialogInterface dialog, int item) {
				mCurrentTimeScaleOption = item;
				// WEEK_TIME_SCALES = { "this week", "this month", "last week", "last month", "all dates" };
				if (mTimeScaleOptions == BaseChart.WEEK_TIME_SCALES) {
					switch (mCurrentTimeScaleOption) {
					case 0:
						mCurrentStartDate = DateUtils.getTimestamp();
						mCurrentTimeScale = TimeScale.WEEK;
						break;
					case 1:
						mCurrentStartDate = DateUtils.getTimestamp();
						mCurrentTimeScale = TimeScale.MONTH;
						break;
					case 2:
						mCurrentStartDate = DateUtils.getLastWeekDate();
						mCurrentTimeScale = TimeScale.WEEK;
						break;
					case 3:
						mCurrentStartDate = DateUtils.getLastMonthDate();
						mCurrentTimeScale = TimeScale.MONTH;
						break;
					case 4:
						mCurrentStartDate = mEarliestDate;
						mCurrentTimeScale = TimeScale.ALL;
						break;
					}
				}
				// DAY_TIME_SCALES = { "today", "this week", "this month", "yesterday", "last week", "last month", "all dates" };
				else if (mTimeScaleOptions == BaseChart.DAY_TIME_SCALES) {
					switch (mCurrentTimeScaleOption) {
					case 0:
						mCurrentStartDate = DateUtils.getTimestamp();
						mCurrentTimeScale = TimeScale.DAY;
						break;
					case 1:
						mCurrentStartDate = DateUtils.getTimestamp();
						mCurrentTimeScale = TimeScale.WEEK;
						break;
					case 2:
						mCurrentStartDate = DateUtils.getTimestamp();
						mCurrentTimeScale = TimeScale.MONTH;
						break;
					case 3:
						mCurrentStartDate = DateUtils.getYesterdayDate();
						mCurrentTimeScale = TimeScale.DAY;
						break;
					case 4:
						mCurrentStartDate = DateUtils.getLastWeekDate();
						mCurrentTimeScale = TimeScale.WEEK;
						break;
					case 5:
						mCurrentStartDate = DateUtils.getLastMonthDate();
						mCurrentTimeScale = TimeScale.MONTH;
						break;
					case 6:
						mCurrentStartDate = mEarliestDate;
						mCurrentTimeScale = TimeScale.ALL;
						break;
					}
				}
				// MONTH_TIME_SCALES = { "this month", "last month", "all dates" };
				else if (mTimeScaleOptions == BaseChart.MONTH_TIME_SCALES) {
					switch (mCurrentTimeScaleOption) {
					case 0:
						mCurrentStartDate = DateUtils.getTimestamp();
						mCurrentTimeScale = TimeScale.MONTH;
						break;
					case 1:
						mCurrentStartDate = DateUtils.getLastMonthDate();
						mCurrentTimeScale = TimeScale.MONTH;
						break;
					case 2:
						mCurrentStartDate = mEarliestDate;
						mCurrentTimeScale = TimeScale.ALL;
						break;
					}
				}
				showChartData();
				setChartHeader();
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	protected void onStop() {
		super.onStop();
	}

	/*
	 * will create the dialog that shows up when user clicks on the chart
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int closestIndex) {
		List<String> dataSnippet = new ArrayList<String>();
		List<String> datesSnippet = new ArrayList<String>();
		List<Integer> indexesSnippet = new ArrayList<Integer>();

		// see if we need to look before closestIndex
		// don't need to look back if: it's the first data point, or if the point before is (-1) or (empty)
		// do need to look back if: we're not the first data point AND the point before is not empty (not -1)
		if ((closestIndex > 0) && !(isEmptyIndicator(mIndicators.get(closestIndex-1)))) {			
			dataSnippet.add(getChartString(mIndicators.get(closestIndex-1)));
			datesSnippet.add(mDialogDates.get(closestIndex-1));
			indexesSnippet.add(closestIndex-1);
		}

		// we definitely have to add the closestIndex (which is guaranteed not to be null)
		if (!(isEmptyIndicator(mIndicators.get(closestIndex)))) {
			dataSnippet.add(getChartString(mIndicators.get(closestIndex)));
			datesSnippet.add(mDialogDates.get(closestIndex));
			indexesSnippet.add(closestIndex);
		}

		// see if we need to look after closestIndex
		// don't need to look ahead if: it's the last data point, or if the point after is (-1) or (empty)
		// do need to look ahead if: we're not the last data point AND the point after is not empty (not -1)
		if ((closestIndex < (mIndicators.size()-1)) && !(isEmptyIndicator(mIndicators.get(closestIndex+1)))) {			
			dataSnippet.add(getChartString(mIndicators.get(closestIndex+1)));
			datesSnippet.add(mDialogDates.get(closestIndex+1));
			indexesSnippet.add(closestIndex+1);
		}

		// set the selected (best guess) index: there will be no more than 3 items in indexesSnippet
		// if there's 1 item, then select that
		// if there are two items, select the first
		// if there are three items, select the middle one
		// REMOVED: since we're using a listview, this isn't needed, though it might be nice to have this sometime later
		// int selectedIndex = indexesSnippet.size()-1;

		if (dataSnippet.size() > 0) {
			return NearestIndicatorsDialog(this, dataSnippet, datesSnippet, indexesSnippet);
		}
		else {
			return null;
		}
	}

	private boolean isEmptyIndicator(T indicator) {
		if (indicator instanceof Diaper) {
			Diaper d = (Diaper) indicator;
			return d.isEmpty();
		}
		else if (indicator instanceof BabyMoodSurvey) {
			BabyMoodSurvey survey = (BabyMoodSurvey) indicator;
			return survey.isEmpty();
		}
		else if (indicator instanceof Weight) {
			Weight w = (Weight) indicator;
			return w.isEmpty();
		}
		else if (indicator instanceof GenericIndicator) {
			GenericIndicator gi = (GenericIndicator) indicator;
			return gi.isEmpty();
		}
		return false;
	}

	private String getChartString(T indicator) {
		if (indicator instanceof Diaper) {
			Diaper d = (Diaper) indicator;
			return d.toChartString();
		}
		else if (indicator instanceof BabyMoodSurvey) {
			BabyMoodSurvey survey = (BabyMoodSurvey) indicator; 
			if (survey.getAverageFussyness() != -1) {
				return survey.toAverageChartString();
			}
			return survey.toChartString();
		}
		else if (indicator instanceof Weight) {
			Weight w = (Weight) indicator;
			return w.toChartString();
		}
		else if (indicator instanceof GenericIndicator) {
			GenericIndicator gi = (GenericIndicator) indicator;
			return gi.toChartString();
		}
		return "";
	}

	protected OnChartTouchListener mChartListener = new OnChartTouchListener() {
		@Override
		public void OnChartTouched(View v, int closestIndex) {
			// this calls onCreateDialog
			mNearestIndicatorsDialogId = closestIndex;
			showDialog(closestIndex);
		}
	};

	protected AlertDialog NearestIndicatorsDialog(Context context, 
			List<String> dataStrings, List<String> dateStrings, List<Integer> indexes) {	
		View dialog = this.getLayoutInflater().inflate(R.layout.dialog_listview, (ViewGroup) findViewById(R.id.dialog_layout));
		ListView listview = (ListView) dialog.findViewById(R.id.data_listview);
		listview.setAdapter(new ChartDialogListAdapter(this, dataStrings, dateStrings, indexes));
		listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				setHighlightBox(Integer.parseInt(parent.getItemAtPosition(position).toString()));
				dismissDialog(mNearestIndicatorsDialogId);
				removeDialog(mNearestIndicatorsDialogId);
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(dialog);
		builder.setTitle("Your past data");
		return builder.create();
	}

	// will highlight the chart at the right location
	// default action will be to do nothing (i.e., highlight is optional)
	protected void setHighlightBox(int index) {
	}

	protected List<T> getDataAtIndex(int index) {
		List<T> data = new ArrayList<T>();
		int bin;
		switch(mCurrentTimeScale) {
		case DAY:
			for (int i=0; i<mDbIndicators.size(); i++) {
				bin = DateUtils.getHourOfDay(mDbIndicators.get(i).getDateTime());
				if (bin == index) {
					data.add(mDbIndicators.get(i));
				}
			}
			break;
		case WEEK:
			for (int i=0; i<mDbIndicators.size(); i++) {
				bin = DateUtils.getDayOfWeek(mDbIndicators.get(i).getDateTime());
				// subtract 1 because counting starts from 1, not 0
				if ((bin-1) == index) {
					data.add(mDbIndicators.get(i));
				}
			}
			break;
		case MONTH:
			for (int i=0; i<mDbIndicators.size(); i++) {
				bin = DateUtils.getWeekOfMonth(mDbIndicators.get(i).getDateTime());
				// subtract 1 because counting starts from 1, not 0
				if ((bin-1) == index) {
					data.add(mDbIndicators.get(i));
				}
			}
			break;
		case ALL:
			for (int i=0; i<mDbIndicators.size(); i++) {
				bin = DateUtils.getMonthOfYear(mDbIndicators.get(i).getDateTime());
				int startingMonth = DateUtils.getMonthOfYear(ReminderPreferences.getInitialSurveyDate(this));
				if (bin == (index + startingMonth)) {
					data.add(mDbIndicators.get(i));
				}
			}
			break;
		}
		return data;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileActivity#onPause()
	 */
	@Override
	protected void onPause() {
		if (mNearestIndicatorsDialogId != -1) {
			try {
				dismissDialog(mNearestIndicatorsDialogId);
				removeDialog(mNearestIndicatorsDialogId);
			}
			catch (Exception e) {
			}
		}
		super.onPause();
	}

	protected List<String> getMonthStrings() {
		List<String> monthStrings = new ArrayList<String>();
		for (int i=0; i<mIndicators.size(); i++) {
			monthStrings.add(DateUtils.getDateAsString(mIndicators.get(i).getDateTime(), DateUtils.MONTH_ONLY));
		}
		return monthStrings;
	}
	
	protected List<List<String>> getWeekStrings() {
		List<List<String>> weekStrings = new ArrayList<List<String>>();
		// this will hold the prefix "Week of"
		weekStrings.add(new ArrayList<String>());
		// this will hold the start dates of the week
		weekStrings.add(new ArrayList<String>());
		for (int i=0; i<mIndicators.size(); i++) {
			weekStrings.get(0).add("Week of");
			Date d = mIndicators.get(i).getCommonData().getTimestamp();
			weekStrings.get(1).add(DateUtils.getDateAsString(DateUtils.getDateOfPreviousSunday(d), DateUtils.WEEK_START_DATE));
		}
		return weekStrings;
	}
}
