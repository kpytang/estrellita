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

package edu.uci.ics.star.estrellita.chart;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import edu.uci.ics.star.estrellita.customview.TableCellTextView;

public class BaseTable extends View {
	public interface TableCellDialogInterface {
		public Dialog TableCellDialog (int row, int column);
	}
	Context mContext;
	List<String> mHeaders;
	List<List<String>> mDataRows;
	boolean mAddRowNumbers, mAlternateRowBackgroundColors;
	int mBorderColor, mTextColor, mHeaderBackgroundColor, mHeaderTextColor, mRowBackgroundColor, mSecondRowBackgroundColor;
	int mStretchedColumnIndex;

	TableLayout mTable;
	ScrollView mScrollView;
	List<TableCellDialogInterface> mOnClickDialogs;

	// the default implementation requires each view to have an id. Adding this means we don't get a
	// warning like this, "couldn't save which view has focus because the focused view X has no id".
	private static final int CHART_VIEW_ID = 123456789;

	public BaseTable(Context context) {
		super(context);

		mContext = context;
		mHeaders = new ArrayList<String>();
		mDataRows = new ArrayList<List<String>>();
		mAddRowNumbers = false;
		mAlternateRowBackgroundColors = false;
		mRowBackgroundColor = Color.WHITE;
		mTextColor = Color.BLACK;
		mBorderColor = Color.BLACK;
		mStretchedColumnIndex = -1;

		mScrollView = new ScrollView(context);
		mTable = new TableLayout(context);
		mTable.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
		mScrollView.addView(mTable);

		setId(CHART_VIEW_ID);
	}

	public View getScrollView() {
		return mScrollView;
	}

	public void setHeaderColors(int backgroundColor, int textColor) {
		mHeaderTextColor = textColor;
		mHeaderBackgroundColor = backgroundColor;
	}

	public void setAllStretchable(boolean flag) {
		mTable.setStretchAllColumns(true);
	}

	public void setStretchable(int columnIndex) {
		mStretchedColumnIndex = columnIndex;
		mTable.setColumnStretchable(columnIndex, true);
	}

	public void addRowNumbers(boolean flag) {
		mAddRowNumbers = flag;
	}

	public void setBorderColor(int color) {
		mBorderColor = color;
	}

	public void setTextColor(int color) {
		mTextColor = color;
	}

	public void setAlternateRowColors(int oddRowColor, int evenRowColor) {
		mAlternateRowBackgroundColors = true;
		mRowBackgroundColor = oddRowColor;
		mSecondRowBackgroundColor = evenRowColor;
	}

	public void setTableHeader(String... headerStrings) {
		if (mHeaders == null) {
			mHeaders = new ArrayList<String>();
		}
		for (int i=0; i<headerStrings.length; i++) {
			mHeaders.add(headerStrings[i]);
		}
	}

	public void setTableHeader(List<String> headerStrings) {
		mHeaders = headerStrings;
	}

	public void setTableData(List<List<String>> data) {
		mDataRows = data;
	}

	public void addDataRow(List<String> data) {
		if (mDataRows == null) {
			mDataRows = new ArrayList<List<String>>();
		}
		mDataRows.add(data);
	}

	public void addDataRow(String... data) {
		if (mDataRows == null) {
			mDataRows = new ArrayList<List<String>>();
		}
		mDataRows.add(new ArrayList<String>());
		for (int i=0; i<data.length; i++) {
			mDataRows.get(mDataRows.size()-1).add(data[i]);
		}
	}

	public void drawTableHeader() {
		TableRow tr = new TableRow(mContext);
		tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

		TableCellTextView tv;
		for(int i=0; i<mHeaders.size(); i++) {
			if (i != mStretchedColumnIndex) {
				tv = new TableCellTextView(mContext, mBorderColor);
			}
			else {
				tv = new TableCellTextView(mContext, mBorderColor, false);
			}
			tv.setText(mHeaders.get(i));
			tv.setTextColor(mHeaderTextColor);
			tv.setBackgroundColor(mHeaderBackgroundColor);
			tv.setTypeface(null, Typeface.BOLD);
			tr.addView(tv);
		}
		mTable.addView(tr);
	}

	public void drawTableData() {
		TableRow tr;
		TableCellTextView tv;
		for(int i=0; i<mDataRows.size(); i++) {
			tr = new TableRow(mContext);
			tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

			List<String> row = mDataRows.get(i);
			for (int j=0; j<row.size(); j++) {
				if (i != mStretchedColumnIndex) {
					tv = new TableCellTextView(mContext, mBorderColor);
				}
				else {
					tv = new TableCellTextView(mContext, mBorderColor, false);
				}

				tv.setTag(new TableCoordinates(i, j));
				tv.setText(row.get(j));
				tv.setTextColor(mTextColor);

				if (mAlternateRowBackgroundColors && (i%2==0)) {
					tv.setBackgroundColor(mSecondRowBackgroundColor);
				}
				else if (mAlternateRowBackgroundColors && (i%2==0)) {
					tv.setBackgroundColor(mRowBackgroundColor);
				}
				else {
					tv.setBackgroundColor(Color.WHITE);
				}

				if ((mOnClickDialogs != null) && (mOnClickDialogs.get(j) != null)) {
					tv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							TableCoordinates coords = (TableCoordinates) v.getTag();
							TableCellDialogInterface dialogInterface = mOnClickDialogs.get(coords.getColumn());
							Dialog d = dialogInterface.TableCellDialog(coords.getRow(), coords.getColumn());
							d.show();
						}
					});
				}

				tr.addView(tv);
			}
			mTable.addView(tr);
		}
	}

	protected void addTableCellDialogInterface(int columnIndex, TableCellDialogInterface dialog) {
		if (mOnClickDialogs == null) {
			mOnClickDialogs = new ArrayList<TableCellDialogInterface>();
		}
		// specified column index > size of listener array
		// then we fill in the in-between indexes with null
		if (columnIndex > mOnClickDialogs.size()) {
			for (int i=mOnClickDialogs.size(); i<columnIndex; i++) {
				mOnClickDialogs.add(null);
			}
			mOnClickDialogs.add(dialog);
		}
		// specified column index == size of listener array
		else if (columnIndex == mOnClickDialogs.size()) {
			mOnClickDialogs.add(dialog);
		}
		// specified column index < size of listener array
		// this may overwrite an existing listener
		else {
			mOnClickDialogs.remove(columnIndex);
			mOnClickDialogs.add(columnIndex, dialog);
		}
	}
}
