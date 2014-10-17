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

import android.content.ContentValues;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.Indicator;

public class GenericIndicatorTable extends IndicatorTable {
	public enum DataType {NUMERIC, STRING};

	public static final String TITLE = "title";
	public static final String UNITS = "units";
	public static final String NUMERIC = "data_type";
	public static final String DATA = "data";

	public GenericIndicatorTable() {
		super("genericindicator", 
				",'" + TITLE + "' varchar(100) NOT NULL," +
				"'" + UNITS + "' varchar(100)," + 
				"'" + DATA + "' varchar(100)"
		);
	}

	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof GenericIndicator){
			GenericIndicator gi = ((GenericIndicator)indicator);
			cv.put(TITLE, gi.getTitle());
			cv.put(UNITS, gi.getUnits());
			cv.put(DATA, gi.getDataAsString());
			return true;
		}
		throw new WrongIndicatorException();
	}

	
	/* (non-Javadoc)
	 * @see ics.uci.edu.star.estrellita.db.tables.IndicatorTable#getSetOfIndicators(java.lang.String, int, int, java.lang.String)
	 */
	public Indicator[] getSetOfIndicators(String where, int rank, int count, String orderBy) {
		return super.getSetOfIndicators(where + IS_DELETED_STRING, rank, count, orderBy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getLastIndicator(int, java.lang.String)
	 */
	public Indicator getLastBabyIndicator(int userid, String otherComparer){
		if (otherComparer == null) {
			otherComparer = "";
		}
		Indicator indicator = null;
		try{
			indicator = getSetOfIndicators( WHERE + BABY_ID + "=" + userid + otherComparer, -1, -1, CREATED_AT + " DESC")[0];
		} catch(ArrayIndexOutOfBoundsException aioobe){
			// ignore for now
		}
		return indicator;
	}

	// idUser = baby_id 
	// returns all generic indicators rows for that user with a specific title
	public GenericIndicator[] getGenericIndicatorsForTimeRange(int idBaby, Date start, Date end){
		String whereClause = WHERE + BABY_ID +  "==" + idBaby + " AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") >= date(" + start.getTime()/1000 +  ", \"unixepoch\", \"localtime\") " +
		"AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") <= date(" + end.getTime()/1000 +  ", \"unixepoch\", \"localtime\")";
		return (GenericIndicator[])getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
	}

	// idUser = baby_id 
	// returns all generic indicators rows for that user 
	public GenericIndicator[] getAllGenericIndicators(int idBaby){
		String whereClause =  WHERE + BABY_ID +  "==" + idBaby ;
		return (GenericIndicator[])getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
	}
	
	// idUser = baby_id 
	// returns all generic indicators rows for that user with a specific title
	public GenericIndicator[] getAllGenericIndicatorsByTitle(int idBaby, String title){
		String whereClause =  WHERE + BABY_ID +  "==" + idBaby + " AND UPPER(" + TITLE + ") == UPPER('" + title.trim() + "')";
		return (GenericIndicator[])getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
	}
	
	// idUser = baby_id 
	// returns all generic indicators rows for that user with a specific title
	public GenericIndicator getFirstGenericIndicatorByTitle(int idBaby, String title){
		String whereClause =  WHERE + BABY_ID +  "==" + idBaby + " AND UPPER(" + TITLE + ") == UPPER('" + title.trim() + "')";
		return (GenericIndicator)getSetOfIndicators(whereClause, 0, 1, IndicatorTable.CREATED_AT +  " ASC")[0];
	}
	
	// idUser = baby_id 
	// returns all generic indicators rows for that user with a specific title for a specific time range
	public GenericIndicator[] getGenericIndicatorsByTitleForTimeRange(int idBaby, String title, Date start, Date end){
		String whereClause = WHERE + BABY_ID +  "==" + idBaby + " AND UPPER(" + TITLE + ") == UPPER('" + title.trim() + "')";
		whereClause += " AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") >= date(" + start.getTime()/1000 +  ", \"unixepoch\", \"localtime\") " +
		"AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") <= date(" + end.getTime()/1000 +  ", \"unixepoch\", \"localtime\")";
		return (GenericIndicator[])getSetOfIndicators(whereClause, -1, -1, null);
	}
	
	// idUser = parent_id for Survey in response table
	// returns all Survey rows for that user
	public GenericIndicator[] getLatestGenericIndicators(int idBaby){
		String groupby = " JOIN (SELECT " +
			TITLE +  " as curTitle, MAX(" + CREATED_AT+  ") AS MaxDT FROM " + getTableName() +  
			WHERE + BABY_ID +  "==" + idBaby +  " GROUP BY curTitle)" +
			" AS curEntry ON " + getTableName() + "." + CREATED_AT+  
			" = curEntry.maxDT ";
		String where = groupby + WHERE + BABY_ID +  "==" + idBaby ;
		return (GenericIndicator[])getSetOfIndicators(where, -1, -1, null);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getIndicatorClass()
	 */
	public Class<?> getIndicatorClass() {
		return GenericIndicator.class;
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		GenericIndicator[] results = new GenericIndicator[cursor.getCount()];
		GenericIndicator gi;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();
			
			gi = new GenericIndicator(this.getCommonData(cursor),cursor.getString(i++), cursor.getString(i++),
					cursor.getString(i++)
				);
			results[k] = gi;
		}
		return results;
	}


}

