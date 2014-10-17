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
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;

public abstract class ThreeLineListAdapter<T> extends ArrayAdapter<T> {
	protected List<String> mFirstRows;
	protected List<String> mSecondRows;
	protected List<String> mThirdRows;
	protected List<Boolean> mImageFlags;
	protected List<String> mTags;
	protected List<T> mItems;

	protected int mListItemResource;
	protected Bitmap mIcon;

	public ThreeLineListAdapter(Context context, List<T> items) {
		this(context, R.layout.three_line_list_item_with_icon, -1, items);
	}
	
	public ThreeLineListAdapter(Context context, int iconDrawableId, List<T> items) {
		this(context, R.layout.three_line_list_item_with_icon, iconDrawableId, items);
	}

	public ThreeLineListAdapter(Context context, int listItemResourceId, int iconDrawableId, List<T> items) {
		super(context, listItemResourceId, items);
		mFirstRows = new ArrayList<String>();
		mSecondRows = new ArrayList<String>();
		mThirdRows = new ArrayList<String>();
		mTags = new ArrayList<String>();
		mListItemResource = listItemResourceId;
		if (iconDrawableId != -1) {
			mImageFlags = new ArrayList<Boolean>();
			// icons bound to (some of) the rows (those with notes)
			mIcon = BitmapFactory.decodeResource(context.getResources(), iconDrawableId);
		}
		mItems = items;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(mListItemResource, null);
		}
		TextView tv;
		// set top text view (the main text)
		tv = (TextView) view.findViewById(R.id.row0);
		tv.setText(mFirstRows.get(position).toString());
		if (mTags.size()>0) {
			tv.setTag(mTags.get(position).toString());
		}
		else {
			tv.setTag(position);
		}
		// set middle text view (the secondary text)
		tv = (TextView) view.findViewById(R.id.row1);
		if (mSecondRows.get(position) != null) {
			tv.setText(mSecondRows.get(position).toString());
			tv.setVisibility(View.VISIBLE);
			if (mTags.size()>0) {
				tv.setTag(mTags.get(position).toString());
			}
		}
		else {
			tv.setVisibility(View.GONE);
		}
		// set bottom text view (the tertiary text)
		tv = (TextView) view.findViewById(R.id.row1);
		if (mThirdRows.get(position) != null) {
			tv.setText(mThirdRows.get(position).toString());
			tv.setVisibility(View.VISIBLE);
			if (mTags.size()>0) {
				tv.setTag(mTags.get(position).toString());
			}
		}
		else {
			tv.setVisibility(View.GONE);
		}
		// set the icon
		ImageView iv = (ImageView) view.findViewById(R.id.icon);
		if ( (iv != null) && (mImageFlags != null) && (mImageFlags.get(position)) && (mIcon != null)) {
			iv.setImageBitmap(mIcon);
			iv.setVisibility(View.VISIBLE);
		}
		else if (iv != null) {
			iv.setVisibility(View.GONE);
		}
		return view;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mFirstRows.size();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getItem(int)
	 */
	@Override
	public T getItem(int position) {
		return mItems.get(position);
	}
}
