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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.chart.BaseChart.ChartType;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.chart.HeatMap;
import edu.uci.ics.star.estrellita.chart.HeatMap.OnHeatMapTouchListener;
import edu.uci.ics.star.estrellita.customview.ChartDialogListAdapter;
import edu.uci.ics.star.estrellita.object.indicator.MoodMapSurvey;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class MyMoodsOverview extends TileOverviewActivity<MoodMapSurvey> {
	private static final int MYMOOD_FORM_ID = 0;
	private static final int NEAREST_MOODS_DIALOG_ID = 1;

	List<PointF> mMoodCoords;

	HeatMap mMoodMapChart;

	List<Integer> mHighlightedMoodsIndices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, TimeScale.WEEK, R.layout.mymoods_overview, R.id.mymoods_heatmap);

		// initialize header
		setActivityHeader("My Moods", false, Tile.MYMOODS);
		setButtonHeader("update my mood");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mUpdateMyMoods);

		mIndicators = new ArrayList<MoodMapSurvey>();
		setIndicatorType(MoodMapSurvey.class);

		// display heatmap data
		showChartData();

		mHighlightedMoodsIndices = null;
	}

	protected void showChartData() {
		// this calls getChartDataFromDB, formatLineChartData() and convertChartDataToVizData()
		super.getChartData(ChartType.HEATMAP);

		mMoodMapChart = new HeatMap(this, mMoodCoords);
		mMoodMapChart.setOnHeatMapTouchListener(mHeatMapListener);

		updateChartView(mMoodMapChart);
	}

	protected List<MoodMapSurvey> getChartDataFromDB() {
		// grab all the moods from the database
		List<MoodMapSurvey> dbMoods = new ArrayList<MoodMapSurvey>();
		MoodMapSurvey[] moods;
		switch(mCurrentTimeScale) {
		case WEEK:
		case MONTH:
			moods = (MoodMapSurvey[])mDatabase.getMoodMapTable().getUserSurveysForTimeRange(EstrellitaTiles.getParentId(this), mCurrentStartDate, mCurrentEndDate);
			if ( (moods != null) && (moods.length>0) ) {
				dbMoods = Arrays.asList(moods);
			}
			break;
		case ALL:
			moods = (MoodMapSurvey[])mDatabase.getMoodMapTable().getAllSurveysForUser(EstrellitaTiles.getParentId(this));
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

	// pulls out the different moods and initializes them as chart data
	// by default should get weekly set 
	protected void convertChartDataToVizData() {
		mMoodCoords = new ArrayList<PointF>();

		for(int i=0; i<mIndicators.size(); i++) {
			MoodMapSurvey mood = mIndicators.get(i);
			PointF p = new PointF(mood.getResponseSet(0).get(0).getData(),mood.getResponseSet(1).get(0).getData());
			mMoodCoords.add(p);
		}
	}

	/**
	 * Eliminates color banding
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	View.OnClickListener mUpdateMyMoods = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(MyMoodsOverview.this, MyMoodForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			startActivityForResult(intent, MYMOOD_FORM_ID);
		}
	};


	// checks for results from the 'update mood' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case MYMOOD_FORM_ID:
				// get diaper info from intent
				MoodMapSurvey mood = (MoodMapSurvey) data.getParcelableExtra("mood");
				openDatabase();
				mDatabase.insertSingle(mood);
				data.setClassName(this, this.getClass().getName());
				mDatabase.logInsert(data, mood);
				// update chart with newly inserted data
				showChartData();

				Toast.makeText(MyMoodsOverview.this, "updated mood map", Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	protected OnHeatMapTouchListener mHeatMapListener = new OnHeatMapTouchListener() {
		@Override
		public void OnHeatMapTouched(View v, List<Integer> closestIndices) {
			mHighlightedMoodsIndices = closestIndices;
			showDialog(NEAREST_MOODS_DIALOG_ID);
		}
	};

	/*
	 * will create the dialog that shows up when user clicks on the heatmap
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NEAREST_MOODS_DIALOG_ID:
			if ( (mHighlightedMoodsIndices != null) && (mHighlightedMoodsIndices.size()>0) ) {
				List<String> dataSnippet = new ArrayList<String>();
				List<String> notesSnippet = new ArrayList<String>();
				List<Integer> indexesSnippet = new ArrayList<Integer>();

				for (int i=0; i<mHighlightedMoodsIndices.size(); i++) {
					MoodMapSurvey mood = mIndicators.get(mHighlightedMoodsIndices.get(i));
					dataSnippet.add(DateUtils.getDateAsString(mood.getDateTime(), DateUtils.DATE_AND_TIME_FORMAT_FULL));
					if (mood.getCommonData().hasNotes()) {
						notesSnippet.add(mood.getCommonData().getNotes());
					}
					else {
						notesSnippet.add(null);
					}
					indexesSnippet.add(mHighlightedMoodsIndices.get(i));
				}
				if (mHighlightedMoodsIndices.size() > 0) {
					return NearestMoodsDialog(this, dataSnippet, notesSnippet, indexesSnippet);
				}
			}
			break;
		}
		return null;
	}

	protected AlertDialog NearestMoodsDialog(Context context, List<String> dataStrings, List<String> notesString, List<Integer> indexes) {	
		View dialog = this.getLayoutInflater().inflate(R.layout.dialog_listview, (ViewGroup) findViewById(R.id.dialog_layout));
		ListView listview = (ListView) dialog.findViewById(R.id.data_listview);
		listview.setAdapter(new ChartDialogListAdapter(this, dataStrings, notesString, indexes));
		listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				int dataIndex = Integer.parseInt(parent.getItemAtPosition(position).toString());
				mMoodMapChart.setHighlightCircle(dataIndex);
				dismissDialog(NEAREST_MOODS_DIALOG_ID);
				mHighlightedMoodsIndices = null;
				removeDialog(NEAREST_MOODS_DIALOG_ID);
			}
		});

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(dialog);
		builder.setTitle("Your past data");
		builder.setCancelable(true);
		return builder.create();
	}
}
