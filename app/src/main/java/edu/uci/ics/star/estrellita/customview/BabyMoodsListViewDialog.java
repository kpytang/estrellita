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

package edu.uci.ics.star.estrellita.customview;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;
import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class BabyMoodsListViewDialog extends ListViewDialog<BabyMoodSurvey> {
	private TimeScale mTimeScale;

	public BabyMoodsListViewDialog(Context context, String title, List<BabyMoodSurvey> moods, TimeScale timeScale) {
		super(context, CustomViews.createDialogTitleView(context, title), moods);
		
		mTimeScale = timeScale;
	}
	
	// Called when the dialog is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mListView = (ListView) mDialogLayout.findViewById(R.id.data_listview);
		mListView.setAdapter(new BabyMoodsTwoLineListAdapter(mContext, mItems, mTimeScale));
		setNoHighlighting();
		super.onCreate(savedInstanceState);
	}

	private class BabyMoodsTwoLineListAdapter extends TwoLineListAdapter<BabyMoodSurvey> {

		public BabyMoodsTwoLineListAdapter(Context context, List<BabyMoodSurvey> moods, TimeScale timeScale) {
			super(context, R.layout.two_line_list_item, moods);
			String dateString = "";
			for (int i=0; i<moods.size(); i++) {
				BabyMoodSurvey mood = moods.get(i);
				if (mood.getCommonData().hasNotes()) {
					switch(timeScale) {
					case DAY:
						dateString = DateUtils.getDateAsString(mood.getCommonData().getTimestamp(), DateUtils.TILE_HOUR_FORMAT);
						break;
					case WEEK:
						dateString = DateUtils.getDateAsString(mood.getCommonData().getTimestamp(), DateUtils.TILE_HOUR_FORMAT);
						break;
					case MONTH:
						dateString = DateUtils.getDateAsString(mood.getCommonData().getTimestamp(), DateUtils.DATE_AND_TIME_FORMAT_FULL);
						break;
					case ALL:
						dateString = DateUtils.getDateAsString(mood.getCommonData().getTimestamp(), DateUtils.DATE_AND_TIME_FORMAT_FULL);
						break;
					}
					mFirstRows.add("Fussyness: " + mood.getFussyness() + " (" + dateString + ")");
					mSecondRows.add(mood.getCommonData().getNotes().trim());
				}
				mTags.add(mood.getId().toString());
			}
		}
	}

}