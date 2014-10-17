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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import edu.uci.ics.star.estrellita.R;

public abstract class ListViewDialog<T> extends AlertDialog {

	protected Context mContext;
	protected ListView mListView;
	protected Intent mIntent;
	protected List<T> mItems;
	protected LinearLayout mDialogLayout;
	
	public ListViewDialog(Context context, String title, List<T> items) {
		this(context, R.layout.dialog_listview, title, items);
	}
	
	public ListViewDialog(Context context, View titleView, List<T> items) {
		this(context, R.layout.dialog_listview, titleView, items);
	}
	
	public ListViewDialog(Context context, int contentViewResourceId, String title, List<T> items) {
		super(context);
		mContext = context;
		mItems = items;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDialogLayout = (LinearLayout) inflater.inflate(contentViewResourceId, null);
		setView(mDialogLayout);
		setTitle(title);
		
		setCanceledOnTouchOutside(true);
	}
	
	public ListViewDialog(Context context, int contentViewResourceId, View titleView, List<T> items) {
		super(context);
		mContext = context;
		mItems = items;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDialogLayout = (LinearLayout) inflater.inflate(contentViewResourceId, null);
		setView(mDialogLayout);
		setCustomTitle(titleView);
	}
	
	public void setNoHighlighting() {
		mListView.setSelector(R.drawable.list_item_no_orange);
	}
	
	public void setIntent(Intent intent) {
		mIntent = intent;
	}
	
	protected void startActivity(Intent intent) {
		mContext.startActivity(intent);
	}
}