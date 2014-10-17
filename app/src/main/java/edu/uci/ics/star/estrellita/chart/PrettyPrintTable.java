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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class PrettyPrintTable extends BaseTable {
	Map<String, Integer> mFrequencyTable;

	public PrettyPrintTable(Context context, List<String> headers, List<List<String>> data) {
		this(context, headers, data, null);
	}
	
	public PrettyPrintTable(Context context, List<String> headers, List<List<String>> data, List<Integer> frequencyColumnIndexes) {
		super(context);

		// initialize look & feel
		setAlternateRowColors(Color.WHITE, Color.GRAY);
		setHeaderColors(Color.BLACK, Color.WHITE);

		// send in the data
		setTableHeader(headers);
		setTableData(data);
		if ( (frequencyColumnIndexes != null) && (frequencyColumnIndexes.size() > 0)) {
			computeFrequencyCounts();
			for (int i=0; i<frequencyColumnIndexes.size(); i++) {
				addFrequencyCounts(frequencyColumnIndexes.get(i));
			}
		}

		drawTableHeader();
		drawTableData();
	}

	public void addFrequencyCounts(int columnIndex) {
		if ((mDataRows.size() > 0) && (columnIndex < mDataRows.get(0).size())) {
			addTableCellDialogInterface(columnIndex, mFrequencyCountsDialog);
		}
	}

	private void computeFrequencyCounts() {
		mFrequencyTable = new HashMap<String, Integer>();
		for (int i=0; i<mDataRows.size(); i++) {
			List<String> row = mDataRows.get(i);
			for (int j=0; j<row.size(); j++) {
				String s = row.get(j);
				if (!mFrequencyTable.containsKey(s)) {
					mFrequencyTable.put(s, 0);
				}
				int count = mFrequencyTable.get(s);
				mFrequencyTable.put(s, ++count);
			}
		}
	}

	protected TableCellDialogInterface mFrequencyCountsDialog = new TableCellDialogInterface() {
		@Override
		public AlertDialog TableCellDialog(int row, int column) {
			String s = mDataRows.get(row).get(column);
			Integer count = 0;
			if (mFrequencyTable.containsKey(s)) {
				count = mFrequencyTable.get(s);
			}
			TextView tv = new TextView(mContext);
			tv.setText(count.toString() + " times");
			tv.setTextSize(20);
			tv.setGravity(Gravity.CENTER);

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle("'" + s + "' has been recorded");
			builder.setView(tv)
			.setCancelable(true)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			return builder.create();
		}
	};
}
