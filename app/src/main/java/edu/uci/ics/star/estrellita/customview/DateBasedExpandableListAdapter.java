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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public abstract class DateBasedExpandableListAdapter<T extends Indicator> extends BaseExpandableListAdapter {
	protected static final String DATE_FORMAT_STRING = DateUtils.CALENDAR_DATEPICKER_FORMAT;
	
	public enum SortingOrder {ASCENDING, DESCENDING};

	protected LayoutInflater mInflater;
	protected Typeface mBold, mRegular;

	protected Map<Date, List<T>> mDateBasedMap;
	protected String[] mDateLabels;

	protected SortingOrder mGroupOrder, mChildrenOrder;

	public DateBasedExpandableListAdapter(Context context, Map<Date, List<T>> mapData, SortingOrder groupOrder, SortingOrder childrenOrder) {
		super();

		mDateBasedMap = mapData;
		mGroupOrder = groupOrder;
		mChildrenOrder = childrenOrder;
		getGroupLabels();

		// cache the LayoutInflate to avoid asking for a new one each time.
		mInflater = LayoutInflater.from(context);
		mBold = Typeface.createFromAsset(context.getAssets(),"fonts/MyriadPro-Bold.otf"); 
		mRegular = Typeface.createFromAsset(context.getAssets(),"fonts/MyriadPro-Regular.otf");
	}

	private void getGroupLabels() {
		switch(mGroupOrder) {
		case DESCENDING:
			mDateLabels = getDateListWithNewestFirst();
			break;
		case ASCENDING:
		default:
			mDateLabels = getDateListWithOldestFirst();
			break;
		}
	}

	public T getChild(int groupPosition, int childPosition) {
		Date d = DateUtils.parseString(mDateLabels[groupPosition], DATE_FORMAT_STRING);
		if (mDateBasedMap.containsKey(d)) {
			return ((List<T>) mDateBasedMap.get(d)).get(childPosition);
		}
		return null;
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		Date d = DateUtils.parseString(mDateLabels[groupPosition], DATE_FORMAT_STRING);
		if (mDateBasedMap.containsKey(d)) {
			return mDateBasedMap.get(d).size();
		}
		else {
			return 0;
		}
	}

	public abstract View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent);

	public Date getGroup(int groupPosition) {
		Date d = DateUtils.parseString(mDateLabels[groupPosition], DATE_FORMAT_STRING);
		if (mDateBasedMap.containsKey(d)) {
			return d;
		}
		else  {
			return null;
		}
	}

	public int getGroupCount() {
		return mDateBasedMap.keySet().size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.date_list_item, null);
		} 

		Date d = (Date)getGroup(groupPosition);
		TextView tv = (TextView) convertView.findViewById(R.id.date);
		tv.setText(DateUtils.getDateAsString(d, DATE_FORMAT_STRING));
		return convertView;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

	public void addChild(Date d, T indicator) {
		if (!mDateBasedMap.containsKey(d)) {
			mDateBasedMap.put(d, new ArrayList<T>());
		}
		mDateBasedMap.get(d).add(indicator);
		sortChildren(d);
		getGroupLabels();
		this.notifyDataSetChanged();
	}

	public void removeChild(Date d, T indicator) {
		if (mDateBasedMap.containsKey(d)) {
			boolean remove = mDateBasedMap.get(d).remove(indicator);

			if (remove && (mDateBasedMap.get(d).size() == 0)) {
				mDateBasedMap.remove(d);
			}
			getGroupLabels();
			this.notifyDataSetChanged();
		} 
		else {
		}
	}

	private void sortChildren(Date d) {
		List<T> indicators = mDateBasedMap.get(d);
		if ((indicators != null) && (indicators.size()>0)) {
			Collections.sort(indicators);
			if (mChildrenOrder == SortingOrder.DESCENDING) {
				Collections.reverse(indicators);
			}
			mDateBasedMap.remove(d);
			mDateBasedMap.put(d, indicators);
		}
	}

	public String[] getDateListWithNewestFirst() {
		List<Date> dates = CollectionUtils.getSetAsSortedList(mDateBasedMap.keySet());
		Collections.reverse(dates);
		String[] labels = new String[dates.size()];
		for(int i=0; i<labels.length; i++) {
			labels[i] = DateUtils.getDateAsString(dates.get(i), DATE_FORMAT_STRING);
		}
		return labels;
	}

	public String[] getDateListWithOldestFirst() {
		List<Date> dates = CollectionUtils.getSetAsSortedList(mDateBasedMap.keySet());
		String[] labels = new String[dates.size()];
		for(int i=0; i<labels.length; i++) {
			labels[i] = DateUtils.getDateAsString(dates.get(i), DATE_FORMAT_STRING);
		}
		return labels;	
	}

	public Map<Date, List<T>> getMapData() {
		return mDateBasedMap;
	}

	public List<Date> getMapKeys() {
		if (mDateBasedMap != null) {
			return CollectionUtils.getSetAsSortedList(mDateBasedMap.keySet());
		}
		return new ArrayList<Date>();
	}
}
