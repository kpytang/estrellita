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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.CustomChartsListAdapter;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.GenericIndicator.IndicatorType;

public class CustomChartsOverview extends TileActivity<GenericIndicator> {
	private static final int CUSTOMCHART_OVERVIEW_ID = 0;
	private static final int CUSTOMCHART_FORM_ID = 1;

	ChartsList mChartsList;
	ArrayList<GenericIndicator> mCharts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_charts_overview);

		// initialize header
		setActivityHeader("Custom Charts", true, Tile.CUSTOMCHARTS);
		setButtonHeader("add new chart");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mAddNewChart);

		// initialize listview
		getCustomChartsFromDatabase();
		LinearLayout mainLayout = (LinearLayout) this.findViewById(R.id.custom_charts_layout);
		mChartsList = new ChartsList(this, mCharts);
		mainLayout.addView(mChartsList);

		// set up the view for when there is an empty list
		TextView tv = new TextView(this);
		tv.setText("you have no custom charts");
		tv.setTypeface(null, Typeface.ITALIC);
		tv.setVisibility(View.GONE);
		mChartsList.setEmptyView(tv);
		((ViewGroup) mChartsList.getParent()).addView(tv);
	}

	private void getCustomChartsFromDatabase() {
		openDatabase();
		mCharts = new ArrayList<GenericIndicator>(Arrays.asList((GenericIndicator[])mDatabase.getGenericIndicatorTable().getLatestGenericIndicators(mBaby.getId())));
	}

	View.OnClickListener mAddNewChart = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(CustomChartsOverview.this, CustomChartForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			startActivityForResult(intent, CUSTOMCHART_FORM_ID);
		}
	};

	private class ChartsList extends ListView {
		CustomChartsListAdapter mListAdapter;
		GenericIndicator mSelectedIndicator;

		public ChartsList(Context context, List<GenericIndicator> charts) {
			super(context);

			// we sort them chronologically and make sure the most recent one is at the top
			Collections.sort(charts);
			Collections.reverse(charts);

			mListAdapter = new CustomChartsListAdapter(this.getContext(), charts);
			setAdapter(mListAdapter);

			setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int childPosition, long id) {
					mSelectedIndicator = (GenericIndicator) mListAdapter.getItem(childPosition);
					openDatabase();
					GenericIndicator firstIndicator = ((GenericIndicator)mDatabase.getGenericIndicatorTable().getFirstGenericIndicatorByTitle(mBaby.getId(), mSelectedIndicator.getTitle()));

					Intent intent;
					String s = "";
					if (firstIndicator != null) {
						s = firstIndicator.getCommonData().getNotes().trim().toUpperCase();
					}
					// determine if this is a chart or table by looking at the notes attached to the first chart
					try {
//						if (IndicatorType.valueOf(s) == IndicatorType.NUMERIC) {
						if(firstIndicator.getChartType() == IndicatorType.NUMERIC) {
							intent = new Intent(CustomChartsOverview.this, CustomChartOverview.class);
						}
						// otherwise, it's a string, so load the TableOverview
						else {
							intent = new Intent(CustomChartsOverview.this, CustomTableOverview.class);
						}
					}
					catch (Exception e) {
						intent = new Intent(CustomChartsOverview.this, CustomTableOverview.class);
					}
					intent.putExtra(TileActivity.BABY, mBaby);
					intent.putExtra("title", mSelectedIndicator.getTitle());
					intent.putExtra("units", mSelectedIndicator.getUnits());
					startActivityForResult(intent, CUSTOMCHART_OVERVIEW_ID);
				}
			});

			setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View v, int childPosition, long id) {
					mSelectedIndicator = (GenericIndicator) mListAdapter.getItem(childPosition);

					Dialog d = DeleteDialog();
					d.show();
					return false;
				}
			});

		}

		public void addToListView(GenericIndicator gi) {
			mListAdapter.addChild(gi);
		}

		public void removeFromListView(GenericIndicator gi) {
			mListAdapter.remove(gi);
			mListAdapter.notifyDataSetChanged();
		}

		public void updateListView(GenericIndicator gi) {
			mListAdapter.updateList(gi);
		}

		public boolean contains(GenericIndicator gi) {
			return mListAdapter.contains(gi);
		}

		public AlertDialog DeleteDialog() {
			String[] actions = new String[1];
			if (mSelectedIndicator.getDataAsString().length() > 0) {
				actions[0] = "Delete this chart and all of its data";
			}
			else {
				actions[0] = "Delete this chart";
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(CustomChartsOverview.this);
			builder.setTitle(R.string.what_to_do);
			builder.setCancelable(true);
			builder.setItems(actions, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item) {
					// this meant that you clicked on the first (only) option: delete
					case 0:
						new AlertDialog.Builder(CustomChartsOverview.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.delete_this)
						.setMessage(R.string.really_delete)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								openDatabase();
								// since we are deleting entire charts, we should delete all the indicators with the same title ( & log it)
								List<GenericIndicator> indicators = Arrays.asList(mDatabase.getGenericIndicatorTable().getAllGenericIndicatorsByTitle(mBaby.getId(), mSelectedIndicator.getTitle()));
								for (int i=0; i<indicators.size(); i++) {
									mDatabase.delete(indicators.get(i));
									mDatabase.logDelete(getIntent(), indicators.get(i));
								}
								removeFromListView(mSelectedIndicator);

								// this will refresh the listview
								startActivity(getIntent());
								finish();
							}

						})
						.setNegativeButton(R.string.no, null)
						.show();
						break;
					}
				}
			});
			return builder.create();
		}
	}

	// checks for results from the 'update custom chart' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			GenericIndicator gi;
			switch (requestCode) {
			case CUSTOMCHART_FORM_ID:
			case CUSTOMCHART_OVERVIEW_ID:
				// get chart info from intent
				gi = (GenericIndicator) data.getParcelableExtra("indicator");
				// if the indicator is null, the none of the charts have changed, so we don't consider that case
				if (gi != null) {
					// if you're already in the list, then just update it
					if (((ChartsList) mChartsList).contains(gi)) {
						((ChartsList) mChartsList).updateListView(gi);
					}
					// it's a new chart, so add it to the database and the list
					else {
						openDatabase();
						mDatabase.insertSingle(gi);
						// add chart to listview
						((ChartsList) mChartsList).addToListView(gi);
					}
				}
				break;
			}
		}
	}

}