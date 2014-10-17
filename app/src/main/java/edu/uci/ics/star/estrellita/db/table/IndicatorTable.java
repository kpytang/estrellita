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

package edu.uci.ics.star.estrellita.db.table;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import edu.uci.ics.star.estrellita.db.table.indicator.AppointmentTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Flag;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;
import edu.uci.ics.star.estrellita.utils.WebUtils;

public abstract class IndicatorTable {
	public static final String ANDROID_FILENAME = "android_filename";

	public static final String IS_DELETED = "is_deleted";

	public static final String FOR_DELETE = "for_delete";

	public static final String FOR_UPDATE = "for_update";

	public static final int NUMBER_OF_COMMONDATA_FIELDS = 13;

	public static final String WHERE = " WHERE ";

	public static final String GROUP_BY = " GROUP BY ";

	public static final String LOCAL_ID = "localId";

	public static final String USER_ID = "user_id";

	public static final String BABY_ID = "baby_id";

	public static final String CREATED_AT = "created_at";

	public static final String FILE = "file";

	public static final String FLAG = "flag";

	public static final String NOTES = "notes";

	public static final String PHOTO = "photo";

	public static final String IS_DELETED_STRING = " AND (" + IS_DELETED + " = 0 OR "+ IS_DELETED + " = 'FALSE' OR "+ IS_DELETED +" ISNULL ) ";



	// + AccelerometerLogDB.ACCELEROMETER_TIMESTAMP
	// + " DATETIME PRIMARY KEY, "
	// + AccelerometerLogDB.ACCELEROMETER_TIMESTAMP_MILLI
	// + " INTEGER, " + AccelerometerLogDB.ACCELEROMETER_X
	// + " DOUBLE, " + AccelerometerLogDB.ACCELEROMETER_Y
	// + " DOUBLE, " + AccelerometerLogDB.ACCELEROMETER_Z
	// + " DOUBLE)";

	public static final String SYNCED = "synced";
	public static final String ID = "id";

	private static final String CREATE_INDICATOR_TABLE_BASE = "'" + ID + "' INTEGER PRIMARY KEY," 
	+ "'" + LOCAL_ID + "' INTEGER,"
	+ "'" + USER_ID + "' INTEGER,"
	+ "'" + BABY_ID + "' INTEGER,"
	//				+ "'key' varchar(40)," 
	//				+ "'personId' varchar(40) NOT NULL,"
	//				+ "'recordId' varchar(40)," 
	+ "'" + CREATED_AT + "' date NOT NULL,"
	+ "'" + FILE + "' BLOB," 
	+ "'" + ANDROID_FILENAME + "' varchar(255)," 
	+ "'" + FLAG + "' varchar(20),"
	+ "'" + NOTES + "' varchar(255), "
	+ "'" + FOR_UPDATE + "' boolean DEFAULT 0, "
	+ "'" + FOR_DELETE + "' boolean DEFAULT 0, "
	+ "'" + IS_DELETED + "' boolean DEFAULT 0, "
	+ "'" + SYNCED + "' boolean DEFAULT 0";

	public static final String COMMON_DATA = "common_data";
	private boolean isOptional;
	private int duration;
	private boolean isParentLevel;
	protected boolean getsUpdated;
	private String tableName;
	private String createTableStringstart;
	private String createTableString;
	private SQLiteDatabase db;

	private Cursor rawQuery = null;

	public IndicatorTable(String tableName, String createTableStringAppend) {
		this.tableName = tableName;
		createTableStringstart = "CREATE TABLE " + tableName + " (";
		this.createTableString = makeCreateTableString(tableName, CREATE_INDICATOR_TABLE_BASE, createTableStringAppend);
		isOptional = true;
		isParentLevel = false;
		setDuration(Utilities.NO_LIMIT);
		getsUpdated = true;
	}

	public String makeCreateTableString(String tableName, String createTableString, String createTableStringAppend){
		return createTableStringstart + createTableString + createTableStringAppend
		+ ")";
	}

	public String formatAsColumnName(String s) {
		return s.replaceAll("\\w", "_").toLowerCase();
	}

	public String getCreateTableStringstart() {
		return createTableStringstart;
	}

	public void setCreateTableStringstart(String createTableStringstart) {
		this.createTableStringstart = createTableStringstart;
	}

	public String getCreateTableString() {
		return createTableString;
	}

	public void setCreateTableString(String createTableString) {
		this.createTableString = createTableString;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String setCreateTable(String appendedSQL) {
		return createTableStringstart + appendedSQL + ")";
	}

	public String getTableName() {
		return tableName;
	}

	public IndicatorTable prepareTable(SQLiteDatabase db) {
		this.db = db;
		return this;
	}

	public SQLiteDatabase getDb() {
		return db;
	}

	public void appendBoolean(boolean b, StringBuilder sb) {
		int i = 0;
		if (b) {
			i = 1;
		}
		appendLiteral(i, sb);
	}

	public static void appendString(Object object, StringBuilder sb) {
		if (object == null) {
			appendLiteral(object, sb);
		} else {
			appendLiteral("\'" + object + "\'", sb);
		}
	}

	public static void appendLiteral(Object object, StringBuilder sb) {
		sb.append("," + object);
	}

	/**
	 * empty a table
	 * @return the number of affected rows
	 */
	public int empty() {
		return getDb().delete(getTableName(), null, null);
	}

	public Cursor getCursor(String where) {
		return getCursor(where, -1, -1, null);
	}

	public Cursor getCursor(String where, String orderBy) {
		return getCursor(where, -1, -1, orderBy);
	}

	public Cursor getCursor(String where, int offset, int limit, 
			String orderBy) {
		String sqlQuery = "SELECT * FROM " + this.getTableName();
		if(where != null){
			sqlQuery += where;
		}

		if(orderBy != null){
			sqlQuery += " ORDER BY " + orderBy;
		} else {
			sqlQuery += " ORDER BY " + IndicatorTable.CREATED_AT + " ASC";
		}
		if(offset >= 0){
			sqlQuery +=  " LIMIT " + offset + "," + limit;
		}
		return doRawQuery(sqlQuery);
	}

	public int getHighestIndex() {
		Cursor cursor = getCursor(null, 0, 1, "id DESC");
		int index = 0;

		try{
			if(cursor.moveToFirst()){
				index = cursor.getInt(cursor.getColumnIndex("id"));
			}
		} catch (Exception e){
			// ignore for now
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return index;
	}

	public int getHighestBabyIndex(int idBaby) {
		Cursor cursor = getCursor(WHERE + IndicatorTable.BABY_ID + " == " + idBaby, 0, 1, "id DESC");
		int index = -1;

		try{
			if(cursor.moveToFirst()){
				index = cursor.getInt(cursor.getColumnIndex("id"));
			}
		} catch (Exception e){
			// ignore for now
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return index;
	}

	public int getHighestParentIndex(int idUser) {
		Cursor cursor = getCursor(WHERE + IndicatorTable.USER_ID + " == " + idUser, 0, 1, "id DESC");
		int index = -1;

		try{
			if(cursor.moveToFirst()){
				index = cursor.getInt(cursor.getColumnIndex("id"));
			}
		} catch (Exception e){
			// ignore for now
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return index;
	}

	public int getLowestIndex() {
		Cursor cursor = getCursor(null, 0, 1, "id ASC");
		int index = 0;
		try{
			if(cursor.moveToFirst()){
				index = cursor.getInt(cursor.getColumnIndex("id"));
			}
		} catch (Exception e){
			// ignore for now
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return index;
	}

	public Integer getHighestLocalId() {
		Cursor cursor = getCursor(null, 0, 1, "localId DESC");
		int index = 0;
		try {
			if(cursor.moveToFirst()){
				index = cursor.getInt(cursor.getColumnIndex("localId"));
			}
		} catch (Exception e){
			// ignore for now
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return index;
	}

	public Integer getLowestLocalId() {
		Cursor cursor = getCursor(null, 0, 1, "localId ASC");
		int index = 0;
		try {
			if(cursor.moveToFirst()){
				index = cursor.getInt(cursor.getColumnIndex("localId"));
			}
		} catch (Exception e){
			// ignore for now
		} finally {
			if(cursor != null)
				cursor.close();
		}
		return index;
	}

	public int count(String where){
		String sqlQuery = "SELECT " + ID + " FROM " + this.getTableName() + " ";
		if(where != null){
			sqlQuery += where;
		}
		int returnvalue = -1;
		Cursor c = doRawQuery(sqlQuery);

		try{
			returnvalue = c.getCount();
		} catch (Exception e){
			// ignore for now
		} finally {
			c.close();
		}
		return returnvalue;
	}

	protected synchronized Cursor doRawQuery(String sqlQuery) {
		rawQuery = getDb().rawQuery(sqlQuery, null);
		return rawQuery;
	}

	public long insert(ContentValues cv){
		return getDb().insert(this.getTableName(), null, cv);
	}

	public void insert(List<ContentValues> cv){
		for (ContentValues contentValues : cv) {
			insert(contentValues);
		}
	}

	public abstract boolean addDataToCV(Indicator indicator, ContentValues cv)
	throws WrongIndicatorException;

	public abstract Class<?> getIndicatorClass();

	/**
	 * updats the table from the server for a given babyId
	 * @param babyId
	 */
	public void updateTable(final int userId, final int babyId, Context c) {
		try {
			JSONArray indicators = getUpdateArray(userId, babyId, c);

			updateTable(indicators);
		} catch (JSONException e) {
		} catch (Exception e) {
			Utilities.writeToWeeklyErrorLogFile(c, e);
		}
	}

	public void updateTable(JSONArray indicators) throws Exception{
		//		try{
		//			getDb().beginTransaction();
		for (int i = 0; i < indicators.length(); i++) {
			insertIndicatorFromJSON(indicators.getJSONObject(i));
		}
		//			getDb().setTransactionSuccessful();
		//		} catch (SQLException e) {
		//			e.printStackTrace();
		//		} finally {
		//			getDb().endTransaction();
		//		}
	}

	public JSONArray getUpdateArray(final int userId, final int babyId, Context c) throws JSONException{
		JSONObject json = WebUtils.getUpdatesFromServer(getTableName(), userId, babyId, getHighestBabyIndex(babyId), c);

		if(json != null) {
			return json.getJSONArray("set");
		}
		return new JSONArray();
	}

	public void insertIndicatorFromJSON(JSONObject indicator) throws Exception {
		try {
			ContentValues cv;
			cv = getCVFromJSON(indicator);
			insert(cv);
		} catch (Exception	e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	protected ContentValues getCVFromJSON(JSONObject indicator)
	throws JSONException {
		ContentValues cv;
		cv = new ContentValues();
		for (Iterator<String> iterator = indicator.keys(); iterator.hasNext();) {
			String key = iterator.next();
			if(indicator.getString(key).equalsIgnoreCase("null") || indicator.getString(key).equals("")){
				cv.putNull(key);
			} else if(key.equals(IndicatorTable.CREATED_AT)){
				try{
					cv.put(key, DateUtils.mysqlStringToDate(indicator.getString(key)).getTime());
				} catch(Exception e){
					cv.put(key, DateUtils.getTimeInMillis());
				}
			} else if(key.equals(AppointmentTable.DATE)){
				cv.put(key, DateUtils.formatDateAsSimpleDate(DateUtils.mysqlStringToDate(indicator.getString(key))));
			} else {
				cv.put(key, indicator.getString(key));
			}
		}
		return cv;
	}

	public List<ContentValues> toCV(Indicator indicator) {
		List<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv = new ContentValues();
		try {
			this.addCommonToCV(indicator.getCommonData(), cv);
			this.addDataToCV(indicator, cv);
		} catch (WrongIndicatorException e) {
//			Utilities.println(e);
		}
		cvs.add(cv);
		return cvs;
	}

	public void addCommonToCV(CommonData common, ContentValues cv) {
		cv.put(ID, common.getId());
		cv.put(LOCAL_ID, common.getLocalId());
		cv.put(USER_ID, common.getIdUser());
		cv.put(BABY_ID, common.getIdBaby());
		cv.put(CREATED_AT, common.getTimestamp().getTime());
		cv.put(FILE, ImageUtils.getBitmapAsByteArray(common.getImage()));
		cv.put(ANDROID_FILENAME, common.getAndroidFilename());
		cv.put(FLAG, common.getFlagString());
		cv.put(NOTES, common.getNotes());
		cv.put(FOR_UPDATE, common.isForUpdate()?1:0);
		cv.put(FOR_DELETE, common.isForDelete()?1:0);
		cv.put(IS_DELETED, common.isDeleted()?1:0);
		cv.put(SYNCED, 0);
	}

	/**
	 * updates a row in the database with the new id from the server
	 * @param cv
	 * @param oldId
	 */
	public Integer update(ContentValues cv){
		String targetColumn = IndicatorTable.LOCAL_ID;
		Integer id = cv.getAsInteger(IndicatorTable.LOCAL_ID);
		if(id == 0) {
			id = cv.getAsInteger(IndicatorTable.ID);
			targetColumn = IndicatorTable.ID;
		}

		getDb().update(this.getTableName(), cv, targetColumn+"=?", new String[]{ id + ""});
//		Utilities.println("UPDATED ROWS: " + getDb().update(this.getTableName(), cv, targetColumn+"=?",
//				new String[]{ id + ""}));
		return id;
	}

	public void delete(ContentValues cv) {
		cv.put(IS_DELETED, true);
		update(cv);
//		Utilities.println("DELETED (by updating IS_DELETED flag" + update(cv));
	}

	public void delete(List<ContentValues> cv){
		for (ContentValues contentValues : cv) {
			delete(contentValues);
		}
	}

	public String andWhereTypeEquals(String comparer, String otherComparer) {
		String operator = "AND";
		return getConditionalString(comparer, otherComparer, operator);
	}

	private String getConditionalString(String comparer, String otherComparer, String operator) {
		if (otherComparer != null && operator != null) {
			otherComparer = " " + operator + " " + comparer + "=\"" + otherComparer + "\""; 
		} else {
			otherComparer = "";
		}
		return otherComparer;
	}

	public String orWhereTypeEquals(String comparer, String... other){
		String operator = "OR";
		if(other.length == 1){
			return getConditionalString(comparer, other[0], operator);
		} else if(other.length > 1){
			StringBuilder sb = new StringBuilder();
			sb.append(" AND (" + comparer + "=\"" + other[0] + "\"");

			for (int i = 1; i < other.length; i++) {
				sb.append(getConditionalString(comparer, other[i], operator));
			}
			sb.append(") ");

			return sb.toString();
		}
		return null;
	}

	/**
	 * if rank is zero of above it will add a limit
	 * starting at a rank and return count lines after that point
	 * ordered by dateTime
	 * @param where
	 * @param limit
	 * @param offset
	 * @return
	 */
	public Indicator[] getSetOfIndicators(String where, int limit, int offset) {
		return getSetOfIndicators(where, limit, offset, null);
	}

	@SuppressWarnings("unchecked")
	public List<ContentValues>[] getUnSynchedCVs() {
		Indicator[] indicators = getUnsynchedIndicators();

		List<ContentValues>[] cvs = new ArrayList[getUnsynchedIndicators().length];
		for(int i = 0; i < cvs.length; i ++){
			cvs[i] = toCV(indicators[i]);
		}
		return cvs;
	}

	public Indicator[] getUnsynchedIndicators() {
		return getSetOfIndicators(WHERE + FOR_UPDATE + " = 1 OR " + FOR_DELETE + " = 1 OR " + SYNCED + " = 0 ", -1, 0, IndicatorTable.LOCAL_ID + " ASC ");
	}

	/**
	 * @param where
	 * @param rank
	 * @param count
	 * @param orderBy
	 * @return
	 */
	public Indicator[] getSetOfIndicators(String where, int rank, int count, String orderBy) {
		Cursor cursor = getCursor(where, rank, count, orderBy);
		try{
			Indicator[] results = getIndicatorArray(cursor);
			return results;
		} catch(Exception e){
			return new Indicator[0];
		} finally{
			if(cursor != null)
				cursor.close();
		}
	}

	public Indicator getLastBabyIndicator(int babyId, String otherComparer){
		try {
			return getLastNBabyIndicator(babyId, otherComparer, 1)[0];
		} catch (Exception e) {
			return null;
		}
	}

	public Indicator[] getLastNBabyIndicator(int babyId, String otherComparer, int n){
		if (otherComparer == null) {
			otherComparer = "";
		}
		Cursor cursor = getCursor(WHERE + BABY_ID + "==" + babyId + " " + otherComparer + IS_DELETED_STRING, 0, n, CREATED_AT + " DESC");

		Indicator[] indicators = null;
		try{
			indicators = getIndicatorArray(cursor);
		} finally{
			if(cursor != null)
				cursor.close();
		}
		return indicators;
	}

	public Indicator getLastParentIndicator(int userid, String otherComparer){
		try {
			return getLastNParentIndicator(userid, otherComparer, 1)[0];
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * Gets the last N indicators for the given parent
	 * @param userid
	 * @param otherComparer
	 * @param n
	 * @return
	 */
	public Indicator[] getLastNParentIndicator(int userid, String otherComparer, int n){
		if (otherComparer == null) {
			otherComparer = "";
		}
		Cursor cursor = getCursor(WHERE + USER_ID + "==" + userid + otherComparer + IS_DELETED_STRING, 0, n, CREATED_AT + " DESC");
		Indicator[] indicators = new Indicator[n];
		try{
			indicators = getIndicatorArray(cursor);
		} finally{
			if(cursor != null)
				cursor.close();
		}
		return indicators;
	}

	public CommonData getCommonData(Cursor cursor) {
		// (int id, int localId, String user_id,  Timestamp created_at,
		// Bitmap image, Flag flag)
		int i = 0;
		return new CommonData(cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++), new Timestamp(cursor.getLong(i++)), 
				ImageUtils.toBitmap(cursor.getBlob(i++)), cursor.getString(i++), Flag.getValue(cursor.getString(i++)), cursor.getString(i++),
				cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++));
	}

	/**
	 * This marks the end of the index of the common data + 1 to signify when
	 * the rest of the data starts, skipping the synched flag
	 * 
	 * @return
	 */
	public int getStartUncommonData() {
		return NUMBER_OF_COMMONDATA_FIELDS;
	}

	/**
	 * this is only used for baby and user
	 * @param jsonObject
	 * @param context
	 * @return
	 */
	public CommonData getCommonData(JSONObject jsonObject, Context context) {
		try {
			Integer babyId = null;
			try {
				babyId = jsonObject.getInt(BABY_ID);
			} catch (JSONException e1){

			}

			Bitmap bitmap = ImageUtils.urlToBitmap(jsonObject.getString(FILE));
			String androidFilename = null;
			androidFilename = this.getTableName()  + jsonObject.getInt(ID)+ "photo.png";
			java.io.File file = new java.io.File(ImageUtils.getImageFilePath(), androidFilename);
			file.getAbsolutePath();
			if(bitmap != null){
				ImageUtils.saveBitmapToPNGFile(bitmap, file);
				androidFilename = file.getAbsolutePath();
			} else {
				androidFilename = null;
			}
			bitmap = null;
			Timestamp createdAt = DateUtils.getTimestamp();

			try{
				if(jsonObject.has(IndicatorTable.CREATED_AT) && 
						jsonObject.getString(IndicatorTable.CREATED_AT) != null && 
						jsonObject.getString(IndicatorTable.CREATED_AT).length() > 0){
					createdAt = new Timestamp(DateUtils.mysqlStringToDate(jsonObject.getString(CREATED_AT)).getTime());
				}
			} catch(Exception e){
				Utilities.writeToWeeklyErrorLogFile(context, e);
			}

			return new CommonData(jsonObject.getInt(ID), null, jsonObject.getInt(USER_ID), babyId, 
					createdAt, bitmap, androidFilename,
					Flag.getValue(jsonObject.getString(FLAG)), jsonObject.getString(NOTES));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param context 
	 * @param IndicatorTable
	 */
	public boolean isNeglected(int kidId, Context context) {
		int daysPast = getDaysPast(kidId);

		// timePast was not null, and it is less than the target duration.
		// examples: timePast = 5, 
		// 
		if (isOptional() || (getDuration() == Utilities.NO_LIMIT || daysPast >= 0 && daysPast < getDuration())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 
	 * @param mCurrentBabyID
	 * @return
	 */
	public String getKidWhereString(int currentBabyID) {
		return " " + BABY_ID + "=" + currentBabyID;
	}

	/**
	 * 
	 * Takes in the id for a kid and returns the days that have passed since
	 * an indicator was last entered. 
	 * Note that DateUtils.getDaysPast(Date) counts a day as crossing midnight.
	 * so one min to midnight and one min after midnight would return "1".
	 * @param kidId
	 * @return
	 */
	public int getDaysPast(int kidId){
		int daysPast;
		try{
			Date dateTime;
			//get the last time an indicator was entered for a the current kid
			dateTime = getLastBabyIndicator(kidId, null).getDateTime();

			// how long ago was that entered
			daysPast = DateUtils.getDaysPast(dateTime);
		} catch(NullPointerException npe){
			// this happens when there was no last time
			daysPast = -1;
		}
		return daysPast;
	}

	/**
	 * 
	 * Takes in the id for a kid and returns the days that have passed since
	 * an indicator was last entered. 
	 * Note that DateUtils.getDaysPast(Date) counts a day as crossing midnight.
	 * so one min to midnight and one min after midnight would return "1".
	 * @param kidId
	 * @return
	 */
	public int getDaysPastParent(int userId){
		int daysPast;
		try{
			Date dateTime;
			//get the last time an indicator was entered for a the current kid
			dateTime = getLastParentIndicator(userId, null).getDateTime();

			// how long ago was that entered
			daysPast = DateUtils.getDaysPast(dateTime);
		} catch(NullPointerException npe){
			// this happens when there was no last time
			daysPast = -1;
		}
		return daysPast;
	}

	/**
	 * Meant to be overridden by classes that extend this
	 * 
	 * @param cursor
	 * @return
	 */
	public abstract Indicator[] getIndicatorArray(Cursor cursor);

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public void setParentLevel(boolean isParentLevel) {
		this.isParentLevel = isParentLevel;
	}

	public boolean isParentLevel() {
		return isParentLevel;
	}

	public void setGetsUpdated(boolean getsUpdated) {
		this.getsUpdated = getsUpdated;
	}

	public boolean isGetsUpdated() {
		return getsUpdated;
	}

}
