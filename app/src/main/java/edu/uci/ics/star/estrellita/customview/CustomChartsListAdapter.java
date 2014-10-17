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

import java.util.Collections;
import java.util.List;

import android.content.Context;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CustomChartsListAdapter extends TwoLineListAdapter<GenericIndicator> {
	private List<GenericIndicator> mCharts;

	public CustomChartsListAdapter(Context context, List<GenericIndicator> charts) {
		super(context, R.layout.two_line_list_item, charts);
		mCharts = charts;
		createList();
	}

	public void createList() {
		mFirstRows.clear();
		mSecondRows.clear();
		mTags.clear();

		String s;
		for(int i=0; i<mCharts.size(); i++) {
			GenericIndicator gi = mCharts.get(i);
			mFirstRows.add(gi.getTitle());
			// a 0-length generic indicator data means that it's actually null
			// and this only occurs when the user has just created a new chart (but hasn't added any data to it yet)
			if (gi.getDataAsString().length() > 0) {
				s = "last updated: " + DateUtils.getDateAsString(gi.getDateTime(), DateUtils.APPOINTMENT_SHORT_DATE_FORMAT) + 
				" with value: " + gi.getDataAsString();
			}
			else {
				s = "created on: " + DateUtils.getDateAsString(gi.getDateTime(), DateUtils.APPOINTMENT_SHORT_DATE_FORMAT);
			}
			mSecondRows.add(s);
			mTags.add(gi.getTitle());
		}
	}

	public void addChild(GenericIndicator gi) {
		mCharts.add(gi);
		Collections.sort(mCharts);
		Collections.reverse(mCharts);
		createList();
		super.notifyDataSetChanged();
	}

	public void updateList(GenericIndicator gi) {
		// if this chart is already in the list, then just update the second row
		if (mTags.contains(gi.getTitle())) {
			int index = mTags.indexOf(gi.getTitle());
			mFirstRows.remove(index);
			mSecondRows.remove(index);
			mTags.remove(index);
			mCharts.remove(index);
			addChild(gi);
		}
	}

	public boolean contains(GenericIndicator gi) {
		if (mTags.contains(gi.getTitle())) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public GenericIndicator getItem(int position) {
		return mCharts.get(position);
	}
}