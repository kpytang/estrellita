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
import edu.uci.ics.star.estrellita.object.indicator.Diaper;
import edu.uci.ics.star.estrellita.utils.ImageUtils;

public class DiapersOverview extends TileOverviewActivity<Diaper> {
	private static final int DIAPER_FORM_ID = 0;

	// for charting
	List<List<Float>> mDiaperChartLines;
	List<Integer> mDiaperChartColors;
	List<String> mDiaperChartLabels;

	TimeBasedLineChart mDiapersChart;

	View mPhotoFlipperView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.WEEK);
		// initialize header
		setActivityHeader("Diapers", true, Tile.DIAPERS);
		setButtonHeader("update diaper count");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mUpdateDiaperCount);

		mIndicators = new ArrayList<Diaper>();
		setIndicatorType(Diaper.class);

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

		mDiapersChart = new TimeBasedLineChart(this, mDiaperChartLines, mDiaperChartColors, mCurrentTimeScale);
		mDiapersChart.addLegend(mDiaperChartLabels, mDiaperChartColors);
		mDiapersChart.setOnChartTouchListener(mChartListener);

		if (mCurrentTimeScale == TimeScale.ALL) {
			mDiapersChart.setXLabels(getMonthStrings());
		}
		else if (mCurrentTimeScale == TimeScale.MONTH) {
			mDiapersChart.setXLabels(getWeekStrings().get(0), getWeekStrings().get(1));
		}

		updateChartView(mDiapersChart);
	}

	/*
	 * grabs all the diapers from the database
	 * by default, we'll get a week of diapers
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#getChartDataFromDB()
	 */
	@Override
	protected List<Diaper> getChartDataFromDB() {
		List<Diaper> dbDiapers = new ArrayList<Diaper>();
		Diaper[] diapers;
		switch(mCurrentTimeScale) {
		case WEEK:
		case MONTH:
			diapers = mDatabase.getDiaperTable().getDiapersForTimeRange(mBaby.getId(), mCurrentStartDate, mCurrentEndDate);
			if ( (diapers != null) && (diapers.length>0) ) {
				dbDiapers = Arrays.asList(diapers);
				// make sure these diapers are sorted (by time)
				Collections.sort(dbDiapers);
			}
			break;
		case ALL:
			diapers = mDatabase.getDiaperTable().getAllDiapers(mBaby.getId());
			if ( (diapers != null) && (diapers.length>0) ) {
				dbDiapers = Arrays.asList(diapers);
				// make sure these diapers are sorted (by time)
				Collections.sort(dbDiapers);
				mCurrentStartDate = dbDiapers.get(0).getDateTime();
				mCurrentEndDate = dbDiapers.get(dbDiapers.size()-1).getDateTime();
			}
			break;
		}
		return dbDiapers;
	}

	/*
	 * pulls out the different diaper counts and initializes them as chart data
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#convertChartDataToVizData()
	 */
	@Override
	protected void convertChartDataToVizData() {
		List<Float> wetList = new ArrayList<Float>();
		List<Float> dirtyList = new ArrayList<Float>();

		for (int i=0; i<mIndicators.size(); i++) {
			wetList.add(Integer.valueOf(mIndicators.get(i).getWet()).floatValue());
			dirtyList.add(Integer.valueOf(mIndicators.get(i).getDirty()).floatValue());
		}

		mDiaperChartLines = new ArrayList<List<Float>>();
		mDiaperChartLines.add(wetList);
		mDiaperChartLines.add(dirtyList);

		mDiaperChartColors = new ArrayList<Integer>();
		mDiaperChartColors.add(Color.RED);
		mDiaperChartColors.add(Color.BLUE);

		mDiaperChartLabels = new ArrayList<String>();
		mDiaperChartLabels.add("wet");
		mDiaperChartLabels.add("dirty");
	}

	// listener for: updating diaper counts
	View.OnClickListener mUpdateDiaperCount = new OnClickListener() {
		public void onClick(View v) {
			// we're not passing in the diaper list because we're going to do it based on the date inside the diaper form
			Intent intent = new Intent(DiapersOverview.this, DiaperForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			startActivityForResult(intent, DIAPER_FORM_ID);
		}
	};

	// checks for results from 'update diaper' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String toastMessage = "";
			switch (requestCode) {
			case DIAPER_FORM_ID:
				// get diaper info from intent
				Diaper d = (Diaper) data.getParcelableExtra("diaper");
				openDatabase();
				// if there are no photos, just upload the count info
				List<String> photoFileNames = d.getPhotoFilenames();
				toastMessage += "updated chart: " + d.getWet() + " wet, " + d.getDirty() + " poopy";
				if ((photoFileNames == null) || (photoFileNames.size() == 0)) {
					mDatabase.insertSingle(d);
					data.setClassName(this, this.getClass().getName());
					mDatabase.logInsert(data, d);
					Toast.makeText(DiapersOverview.this, toastMessage, Toast.LENGTH_LONG).show();
				}
				// else, there are photos, so upload them one by one (all with the same count info)
				// this is so that we can upload the actual bitmaps, in addition to the list of filenames
				else {
					boolean hasUploadedImage = false;
					for (int i = 0; i < photoFileNames.size(); i++) {
						d.getCommonData().setId(null);
						d.getCommonData().setLocalId(null);
						String filename = photoFileNames.get(i);

						if(!mDatabase.getDiaperTable().existsFilename(filename)){
							d.getCommonData().setImage(ImageUtils.decodeFile(new File(filename)));
							d.getCommonData().setAndroidFilename(filename);
							mDatabase.insertSingle(d);
							data.setClassName(this, this.getClass().getName());
							mDatabase.logInsert(data, d);
							hasUploadedImage = true;
						}
					}
					if(!hasUploadedImage){
						d.getCommonData().setImage(null);
						d.getCommonData().setAndroidFilename(null);
						mDatabase.insertSingle(d);
						data.setClassName(this, this.getClass().getName());
						mDatabase.logInsert(data, d);
					}
					toastMessage += ", " + photoFileNames.size() + " photos";
					Toast.makeText(DiapersOverview.this, toastMessage, Toast.LENGTH_LONG).show();
				}
				// update chart with newly inserted data
				showChartData();

				break;
			}
		}
	}

	/*
	 * we'll also pop up another window showing all the photos for the data points
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileOverviewActivity#setHighlightBox(int)
	 */
	@Override
	protected void setHighlightBox(int index) {
		mDiapersChart.setHighlightBox(index);

		// get the diapers for this index
		List<Diaper> diapers = super.getDataAtIndex(index);
		List<String> filenames = new ArrayList<String>();
		for (int i=0; i<diapers.size(); i++) {
			filenames.addAll(diapers.get(i).getPhotoFilenames());
		}

		// check to make sure each filename actually exists
		// if it doesn't, then look at the image in commondata and save that file using the original filename
		Diaper.checkAndCreatePhotoFileNames(filenames, diapers);

		if (filenames.size() > 0) {
			Dialog d = ImageUtils.createPhotoFlipper(this, mPhotoFlipperView, filenames, true);
			d.show();
		}
	}
}
