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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ExpandableListView;
import edu.uci.ics.star.estrellita.object.Indicator;

public abstract class DateBasedList<T extends Indicator> extends ExpandableListView {
	public interface OnDeleteSelectedActionListener<T extends Indicator> {
		public void OnContextItemSelectedAction(T indicator);
	}

	private static final int MENU_DELETE = 1;

	protected List<Boolean> mExpandedFlags;
	protected DateBasedExpandableListAdapter<T> mListAdapter;
	protected OnDeleteSelectedActionListener<T> mOnDeleteSelectedActionListener;

	public DateBasedList(Context context) {
		super(context);

		setOnCreateContextMenuListener(mOnCreateDeleteContextMenuListener);
	}

	public void expandAllGroups() {
		for (int i=0; i<getMapSize(); i++) {
			this.expandGroup(i);
		}
	}
	
	public T getIndicator(int groupPosition, int childPosition) {
		return (T) mListAdapter.getChild(groupPosition, childPosition);
	}
	
	public int getMapSize() {
		return mListAdapter.getGroupCount();
	}
	
	private OnCreateContextMenuListener mOnCreateDeleteContextMenuListener =
		new OnCreateContextMenuListener() {
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
			int type = getPackedPositionType(info.packedPosition);
			if (type == 1) {
				menu.setHeaderTitle("What would you like to do?");
				MenuItem item = menu.add(0, MENU_DELETE, 0, "Delete this");
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();
						int groupPos = 0, childPos = 0;
						int type = ExpandableListView.getPackedPositionType(info.packedPosition);
						if (type == PACKED_POSITION_TYPE_CHILD) {
							groupPos = getPackedPositionGroup(info.packedPosition);
							childPos = getPackedPositionChild(info.packedPosition);
						}
						T indicator = getIndicator(groupPos, childPos);
						((OnDeleteSelectedActionListener<T>) mOnDeleteSelectedActionListener).OnContextItemSelectedAction(indicator);
						return false;
					}
				});
			}
		}
	};

	public void setOnDeleteSelectedListener(OnDeleteSelectedActionListener<T> listener) {
		mOnDeleteSelectedActionListener = listener;
	}
}