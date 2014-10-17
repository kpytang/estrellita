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

import java.sql.Timestamp;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Weight;
import edu.uci.ics.star.estrellita.sharedprefs.api.ReminderPreferences;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;


public class WeightTable extends IndicatorTable {

	private static final String OZS = "ozs";
	private static final String LBS = "lbs";

	/*
	CREATE TABLE `weight` (
	  `idBaby` int(11) NOT NULL,
	  `dateTime` date NOT NULL,
	  `lbs` int(11) NOT NULL,
	  `ozs` int(11) NOT NULL,
	  `photo` varchar(200) NOT NULL,
	  `flag` enum('urgent','warning','value') NOT NULL
	) ENGINE=MyISAM DEFAULT CHARSET=latin1;
	 */
	public WeightTable() {
		super("weight", "," +
				"'" + LBS + "' int(11) NOT NULL," +
				"'" + OZS + "' int(11) NOT NULL"
		);
		this.setDuration(DateUtils.WEEK);
		this.setOptional(false);
	}

	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof Weight){
			Weight w = (Weight) indicator;
			cv.put(LBS, w.getPounds());
			cv.put(OZS, w.getOunces());
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		Weight[] results = new Weight[cursor.getCount()];
		Weight w;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();

			w = new Weight(this.getCommonData(cursor), 
					cursor.getInt(i++), cursor.getInt(i++)
			);
			results[k] = w;
		}
		return results;
	}

	// idUser = baby_id for weight table
	// returns weight rows that have timestamps between the ranges 
	public Weight[] getWeightsForTimeRange(int idBaby, Date start, Date end){
		String where = " JOIN (SELECT " +
		BABY_ID +  ", MAX(" + CREATED_AT+  ") AS MaxDT FROM " + getTableName()+  
		" WHERE " + BABY_ID+  "==" + idBaby + " AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") >= date(" + start.getTime()/1000 +  ", \"unixepoch\", \"localtime\") " +
		"AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") <= date(" + end.getTime()/1000 +  ", \"unixepoch\", \"localtime\") GROUP BY DATE(" + CREATED_AT+  
		"/1000, \"unixepoch\", \"localtime\")) AS curday ON " + getTableName()+ "." + CREATED_AT+  
		" = curday.MaxDT" + GROUP_BY + IndicatorTable.CREATED_AT;

		Cursor cursor = getCursor(where);
		Weight[] indicators;
		try{
			indicators = (Weight[]) getIndicatorArray(cursor);
		} catch(ArrayIndexOutOfBoundsException aioobe){
			indicators = null;
		}
		finally {
			if(cursor != null)
				cursor.close();
		}

		return indicators;
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

	// idUser = baby_id for weight table
	// only returns the most recent weight row with the same timestamp as today
	public Weight[] getAllWeights(int idBaby){

		String where = " JOIN (SELECT " +
		BABY_ID +  ", MAX(" + CREATED_AT+  ") AS MaxDT FROM " + getTableName()+  
		" WHERE " + BABY_ID+  "==" + idBaby + " GROUP BY DATE(" + CREATED_AT+  
		"/1000, \"unixepoch\", \"localtime\")) AS curday ON " + getTableName()+ "." + CREATED_AT+  
		" = curday.MaxDT";
		String orderBy = IndicatorTable.CREATED_AT+  " DESC";

		Cursor cursor = getCursor(where, orderBy);
		Weight[] indicators;
		try{
			indicators = (Weight[]) getIndicatorArray(cursor);
		} catch(ArrayIndexOutOfBoundsException aioobe){
			indicators = null;
		}
		finally {
			if(cursor != null)
				cursor.close();
		}
		if (indicators == null) {
			return null;
		}
		else if (indicators.length == 0) {
			return new Weight[0];
		}
		else {
			return indicators;
		}
	}


	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#isNeglected(int, android.content.Context)
	 */
	public boolean isNeglected(int kidId, Context context) {
		Weight w = (Weight) getLastBabyIndicator(kidId, null);

		Timestamp timestamp = DateUtils.getTimestamp();
		if (w != null) {
			if (DateUtils.isSameWeek(w.getDateTime(), timestamp) ) {
				return false; // not neglected: weight this week already
			}
			// you entered a weight that's from an earlier week. so just make sure you fall on the right days
			else {
				if ( ((timestamp.getDay()+1) >=  ReminderPreferences.getStartWeightDate(context)) && 
						((timestamp.getDay()+1) <= ReminderPreferences.getEndWeightDate(context)) ) {

					return true;	// neglected: no weight this week & it's on the right days
				}
				return false; // not neglected: earlier weight, and it's not on the right days
			}
		}
		// there's no previous weight
		else {
			// so just make sure you fall on the right days
			if ( ((timestamp.getDay()+1) >=  ReminderPreferences.getStartWeightDate(context)) && 
					((timestamp.getDay()+1) <= ReminderPreferences.getEndWeightDate(context)) ) {
				return true; // neglected: no weight, and it's on the right days
			}
			else {
				return false; // not neglected: no weight, and it's not on the right days
			}
		}
	}

	@Override
	public Class<?> getIndicatorClass() {
		return Weight.class;
	}

}
