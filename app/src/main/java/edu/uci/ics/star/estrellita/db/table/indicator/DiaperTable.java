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

package edu.uci.ics.star.estrellita.db.table.indicator;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Diaper;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;

/**
 * 9 fields: 
 * diaper_id int(11) 
 * baby_id int(11) 
 * wet int(11) 
 * dirty int(11) 
 * both int(11) 
 * photo_filenames mediumtext 
 * created_at datetime 
 * inserted_at datetime
 * 
 */
public class DiaperTable extends IndicatorTable {
	public static final String WET = "wet";
	public static final String DIRTY = "dirty";
	public static final String BOTH = "both";
	public static final String PHOTO_FILENAMES = "photos";

	public DiaperTable() {
		super("diaper", 
				",'" + WET + "' int(11) NOT NULL," + 
				"'" + DIRTY + "' int(11) NOT NULL,"+ 
				"'" + BOTH + "' int(11) NOT NULL," +
				"'" + PHOTO_FILENAMES + "' var(255) NULL"
		);
		this.setDuration(DateUtils.DAY);
		this.setOptional(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ics.uci.edu.star.db.tables.IndicatorTable#addDataToCV(ics.uci.edu.objects
	 * .Indicator, android.content.ContentValues)
	 */
	public boolean addDataToCV(Indicator indicator, ContentValues cv)
	throws WrongIndicatorException {
		if (indicator instanceof Diaper) {
			Diaper d = (Diaper) indicator;

			cv.put(WET, d.getWet());
			cv.put(DIRTY, d.getDirty());
			cv.put(BOTH, d.getBoth());
			cv.put(PHOTO_FILENAMES, CollectionUtils.toString(d.getPhotoFilenames(), Diaper.FILENAME_DELIMITER));
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getCVFromJSON(org.json.JSONObject)
	 */
	protected ContentValues getCVFromJSON(JSONObject indicator) throws JSONException {
		ContentValues cv = super.getCVFromJSON(indicator);
		String url = cv.getAsString(PHOTO);
		cv.remove(PHOTO);

		if(url != null && !url.equals("")) {
			String androidFilename = null;
			if(indicator.has(IndicatorTable.ANDROID_FILENAME) && 
					indicator.getString(IndicatorTable.ANDROID_FILENAME) != null && 
					indicator.getString(IndicatorTable.ANDROID_FILENAME).length() > 0){

				androidFilename = indicator.getString(ANDROID_FILENAME);

				java.io.File file = new java.io.File(androidFilename);

				if (!file.exists()) {
					ImageUtils.saveBitmapToFile(ImageUtils.urlToBitmap(url), androidFilename);
				}

			} else {

			}
		}
		return cv;
	}


	public boolean existsFilename(String filename) {
		String sqlQuery = "SELECT " + ANDROID_FILENAME + " FROM " + this.getTableName() + " " + 
		WHERE + " " + ANDROID_FILENAME + "=\"" + filename + "\"";

		int count = -1;

		Cursor c = null;
		try{
			c = doRawQuery(sqlQuery);

			count = c.getCount();
		}
		finally {
			if(c != null)
				c.close();
		}
		if(count > 0)
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ics.uci.edu.star.db.table.IndicatorTable#getIndicatorArray(android.database
	 * .Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		Diaper[] results = new Diaper[cursor.getCount()];
		Diaper d;
		int i;
		for (int k = 0; k < cursor.getCount(); k++) {
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();

			d = new Diaper(this.getCommonData(cursor), cursor.getInt(i++), cursor.getInt(i++),
					cursor.getInt(i++), cursor.getString(i++));
			results[k] = d;
		}
		return results;
	}

	// idUser = baby_id for diaper table
	// only returns the most recent diapers row with the same timestamp as today
	// Note: each diaper row only has 1 bitmap photo, but it also has a column for a ';'-delimited list of filenames 
	// the number of filenames should match the number of diaper rows for that day (i.e., the number of bitmaps)
	public Diaper[] getDiapersForToday(int idBaby) {

		String where = " JOIN (SELECT " + BABY_ID + ", MAX("
		+ CREATED_AT + ") AS MaxDT FROM " + getTableName() + " WHERE " + BABY_ID + "=="
		+ idBaby + " AND DATE(" + CREATED_AT
		+ "/1000, \"unixepoch\", \"localtime\") = DATE(\"now\", \"localtime\") GROUP BY DATE(" + CREATED_AT
		+ "/1000, \"unixepoch\", \"localtime\")) AS curday ON " + getTableName() + "." + CREATED_AT
		+ " = curday.MaxDT";
		String orderBy = IndicatorTable.CREATED_AT + " DESC";

		Cursor cursor = getCursor(where, orderBy);
		Diaper[] indicators;
		try {
			indicators = (Diaper[]) getIndicatorArray(cursor);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			indicators = null;
		} finally {
			if(cursor != null)
				cursor.close();
		}
		if ((indicators == null) || (indicators.length == 0)) {
			return null;
		} else {
			return indicators;
		}
	}

	// idUser = baby_id for diaper table
	// returns diaper rows that have timestamps between the ranges
	public Diaper[] getDiapersForTimeRange(int idBaby, Date start, Date end) {
		String where = " JOIN (SELECT " + BABY_ID + ", MAX("
		+ CREATED_AT + ") AS MaxDT FROM " + getTableName() + " WHERE " + BABY_ID + "=="
		+ idBaby + " AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") >= date("
		+ start.getTime() / 1000 + ", \"unixepoch\", \"localtime\") " + "AND date(" + CREATED_AT
		+ "/1000, \"unixepoch\", \"localtime\") <= date(" + end.getTime() / 1000
		+ ", \"unixepoch\", \"localtime\") GROUP BY DATE(" + CREATED_AT
		+ "/1000, \"unixepoch\", \"localtime\")) AS curday ON " + getTableName() + "." + CREATED_AT
		+ " = curday.MaxDT" + " GROUP BY " + IndicatorTable.CREATED_AT;

		String orderBy = IndicatorTable.CREATED_AT + " ASC";

		Cursor cursor = getCursor(where, orderBy);
		Diaper[] indicators;
		try {
			indicators = (Diaper[]) getIndicatorArray(cursor);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			indicators = null;
		} finally {
			if(cursor != null)
				cursor.close();
		}

		if ((indicators == null) || (indicators.length == 0)) {
			return null;
		} else {
			return indicators;
		}
	}

	// idUser = baby_id for diaper table
	// only returns the most recent diaper row with the same timestamp as today
	public Diaper[] getAllDiapers(int idBaby) {

		String where = " JOIN (SELECT " + BABY_ID + ", MAX("
		+ CREATED_AT + ") AS MaxDT FROM " + getTableName() + " WHERE " + BABY_ID + "=="
		+ idBaby + " GROUP BY DATE(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\")) AS curday ON "
		+ getTableName() + "." + CREATED_AT + " = curday.MaxDT";

		String orderBy = IndicatorTable.CREATED_AT + " DESC";

		Cursor cursor = getCursor(where, orderBy);
		Diaper[] indicators;
		try {
			indicators = (Diaper[]) getIndicatorArray(cursor);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			indicators = null;
		} finally {
			if(cursor != null)
				cursor.close();
		}
		if ((indicators == null) || (indicators.length == 0)) {
			return null;
		} else {
			return indicators;
		}
	}

	public Class<?> getIndicatorClass() {
		return Diaper.class;
	}

}
