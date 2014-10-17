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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.object.indicator.GenericSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.sharedprefs.api.ReminderPreferences;
import edu.uci.ics.star.estrellita.sharedprefs.api.UserPreferences;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class SurveysOverview extends TileActivity<GenericSurvey>  {

	public static final int SURVEY_FORM_ID = 0;
	public static final int MAX_NUMBER_OF_SURVEYS = 5;

	private List<Date> mSurveyDates;

	private LinearLayout mLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.surveys_overview);

		// initialize header
		setActivityHeader("Surveys", false, Tile.SURVEYS);

		// figure out what dates the surveys are to be taken on
		getSurveyDates();

		// populate expandable listview
		mLayout = (LinearLayout) this.findViewById(R.id.notes_layout);
		mLayout.addView(new SurveyListView(this));
	}

	private void getSurveyDates() {
		mSurveyDates = new ArrayList<Date>();
		Date initialDate = ReminderPreferences.getInitialSurveyDate(SurveysOverview.this);
		for (int i=0; i<MAX_NUMBER_OF_SURVEYS; i++) {
			mSurveyDates.add(DateUtils.addToDate(initialDate, Calendar.MONTH, (i)));
		}
	}

	private class SurveyListView extends ExpandableListView {
		private static final String CATEGORY = "CATEGORY";
		private static final String NUMBER_OF_SURVEYS = "NUMBER_OF_SURVEYS";
		private static final String TITLE = "TITLE";
		private static final String TIME_INFO = "TIME_INFO";

		private List<Map<String, String>> mListViewGroups;
		private List<List<Map<String, String>>> mListViewChildren;

		private List<Map<String, String>> mUpcomingSurveys, mPastSurveys;

		public SurveyListView(Context context) {
			super(context);

			// at index 0: upcoming surveys (since we want these listed first)
			// at index 1: past surveys (since we want these listed second)
			mListViewChildren = new ArrayList<List<Map<String, String>>>();
			initializeSurveys();
			mListViewChildren.add(mUpcomingSurveys);
			mListViewChildren.add(mPastSurveys);

			mListViewGroups = new ArrayList<Map<String, String>>();
			Map<String, String> groupLabels = new HashMap<String, String>();
			groupLabels.put(CATEGORY, "Upcoming Surveys");
			groupLabels.put(NUMBER_OF_SURVEYS, getGroupSubHeaderString(mListViewChildren.get(0).size(), "upcoming"));
			mListViewGroups.add(groupLabels);
			groupLabels = new HashMap<String, String>();
			groupLabels.put(CATEGORY, "Past Surveys");
			groupLabels.put(NUMBER_OF_SURVEYS, getGroupSubHeaderString(mListViewChildren.get(1).size(), "past"));
			mListViewGroups.add(groupLabels);

			// Set up our adapter
			SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
					this.getContext(),
					mListViewGroups,
					android.R.layout.simple_expandable_list_item_2,
					new String[] { CATEGORY, NUMBER_OF_SURVEYS },
					new int[] { android.R.id.text1, android.R.id.text2 },
					mListViewChildren,
					android.R.layout.simple_expandable_list_item_2,
					new String[] { TITLE, TIME_INFO },
					new int[] { android.R.id.text1, android.R.id.text2 }
			);
			setAdapter(adapter);

			setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					// the first group will be for UPCOMING surveys
					if (groupPosition == 0) {
						String title = mListViewChildren.get(groupPosition).get(childPosition).get(TITLE);
						String surveyNumber = title.substring(title.length()-1, title.length());

						Date expectedDate = mSurveyDates.get(Integer.parseInt(surveyNumber) - 1);
						// if the CURRENT time is ON or AFTER the survey/expected timestamp, then start up the survey
						if (DateUtils.isDateOnOrAfter(DateUtils.getTimestamp(), expectedDate)) {
							Intent intent = new Intent(SurveysOverview.this, SurveyForm.class);
							intent.putExtra(SurveyForm.TITLE, title);
							intent.putExtra(SurveyForm.SURVEY_TYPE, SurveyType.STRESS.name());
							intent.putExtra(TileActivity.BABY, mBaby);
							startActivityForResult(intent, SURVEY_FORM_ID);
							return false;
						}
						// else the current timestamp is before survey/expected timestamp, then pop up the window to tell user to wait
						else {
							Dialog d = WithholdSurveyDialog(expectedDate);
							d.show();
							return false;
						}
					}
					return false;
				}
			});

			// expands the Upcoming Surveys section
			expandGroup(0);
		}

		private void initializeSurveys() {
			mUpcomingSurveys = new ArrayList<Map<String, String>>();
			mPastSurveys = new ArrayList<Map<String, String>>();
			Map<String, String> childLabels;

			int lastUserId = UserPreferences.getLastUserId(SurveysOverview.this);
			openDatabase();
			// we'll only pull one type of survey since they should be taken together & we make sure that the oldest one is first
			List<GenericSurvey> takenSurveys = Arrays.asList((GenericSurvey[]) mDatabase.getEpdsSurveyTable().getAllSurveysForUser(lastUserId));

			// for each survey, we're going to check if you've already taken them, or the survey has expired, or if the survey has not yet been taken
			boolean categorized = false;
			for (int i=0; i<MAX_NUMBER_OF_SURVEYS; i++) {
				childLabels = new HashMap<String, String>();
				childLabels.put(TITLE, "Survey #" + (i+1));

				// check to see if you've taken this yet
				for (int j=0; (!categorized) && (j<takenSurveys.size()); j++) {
					Date actualDate = takenSurveys.get(j).getCommonData().getTimestamp();
					// we use this data point for comparing the survey dates
					// we add 8 because the DateUtils.isBetween does not include the end date
					Date weekAfterScheduledDate = DateUtils.addToDate(mSurveyDates.get(i), Calendar.DAY_OF_YEAR, 8);
					if (DateUtils.isDateBetween(actualDate, mSurveyDates.get(i), weekAfterScheduledDate)) {
						childLabels.put(TIME_INFO, "completed on " + DateUtils.getDateAsString(actualDate, DateUtils.DATE_ONLY_FORMAT));
						mPastSurveys.add(childLabels);
						categorized = true;
					}
				}

				// if it hasn't been taken, check to see if it's expired 
				if (!categorized) {
					Date surveyToBeTakenByDate = DateUtils.addToDate(mSurveyDates.get(i), Calendar.DAY_OF_YEAR, 7);
					if (DateUtils.isDateOnOrAfter(DateUtils.getTimestamp(), surveyToBeTakenByDate)) {
						childLabels.put(TIME_INFO, "expired on " + DateUtils.getDateAsString(mSurveyDates.get(i), DateUtils.DATE_ONLY_FORMAT));
						mPastSurveys.add(childLabels);
					}
					else {
						childLabels.put(TIME_INFO, "to be taken on " + DateUtils.getDateAsString(mSurveyDates.get(i), DateUtils.DATE_ONLY_FORMAT));
						mUpcomingSurveys.add(childLabels);
					}
				}
				categorized = false;
			}
		}

		private String getGroupSubHeaderString(int count, String type) {
			if (count > 0) {
				return "[" + StringUtils.pluralize(count, "survey", "surveys") + "]";
			}
			else if (type.toLowerCase().equals("upcoming")) {
				return "[you have no upcoming surveys] " + ReminderPreferences.getInitialSurveyDate(SurveysOverview.this);
			}
			else if (type.toLowerCase().equals("past")) {
				return "[you have not completed any surveys yet]";
			}
			return "";
		}

		public AlertDialog WithholdSurveyDialog(Date d) {
			AlertDialog.Builder builder = new AlertDialog.Builder(SurveysOverview.this);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(R.string.tip);
			builder.setMessage(getString(R.string.too_early_for_survey) + " " + 
					DateUtils.getDateAsString(d, DateUtils.DATE_ONLY_FORMAT))
					.setCancelable(true)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			return builder.create();
		}
	}

	// checks for results from taking the survey
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SURVEY_FORM_ID:
				// as long as we get a RESULT_OK from the Survey Form, then it means we finished the survey
				// so we should update the listview
				if (mLayout != null) {
					mLayout.removeViewAt(mLayout.getChildCount()-1);
					mLayout.addView(new SurveyListView(this));
				}
				break;
			}
		}
	}
}
