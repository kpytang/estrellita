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

package edu.uci.ics.star.estrellita.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.db.table.indicator.AppointmentTable;
import edu.uci.ics.star.estrellita.db.table.indicator.BabyMoodSurveyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.BabyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.BondingSurveyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.DiaperTable;
import edu.uci.ics.star.estrellita.db.table.indicator.GenericIndicatorTable;
import edu.uci.ics.star.estrellita.db.table.indicator.GenericSurveyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.LogTable;
import edu.uci.ics.star.estrellita.db.table.indicator.MoodMapSurveyTable;
import edu.uci.ics.star.estrellita.db.table.indicator.PostTable;
import edu.uci.ics.star.estrellita.db.table.indicator.UserTable;
import edu.uci.ics.star.estrellita.db.table.indicator.WeightTable;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Diaper;
import edu.uci.ics.star.estrellita.object.indicator.GenericSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.MoodMapSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Post;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.object.indicator.Weight;
import edu.uci.ics.star.estrellita.updateservice.SyncService;
import edu.uci.ics.star.estrellita.utils.ImageUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;
import edu.uci.ics.star.estrellita.utils.WebUtils;

public class Database {
	private static final String DATABASE_NAME    = "estrellita.db";
	private static final String DIRECTORY_NAME    = "estrellita";
	private static final String SD_DIRECTORY = "/sdcard/"+DIRECTORY_NAME;

	private static final int    DATABASE_VERSION = 65;

	private static SQLiteDatabase      db;

	private static Object lock = new Object();
	private HashMap<String, IndicatorTable> tables;

	private DBOpenHelper openHelper;
	private Context mContext;

	private static HashSet<Double> openDbKeys = new HashSet<Double>();

	public Database(Context context) {
		mContext = context;
		tables = new HashMap<String, IndicatorTable>();
		tables.put(BondingSurvey.class.getSimpleName(), new BondingSurveyTable());
		tables.put(Diaper.class.getSimpleName(), new DiaperTable());
		tables.put(Weight.class.getSimpleName(), new WeightTable());
		tables.put(MoodMapSurvey.class.getSimpleName(), new MoodMapSurveyTable());
		tables.put(Appointment.class.getSimpleName(), new AppointmentTable());
		tables.put(Post.class.getSimpleName(), new PostTable());
		tables.put(User.class.getSimpleName(), new UserTable());
		tables.put(Baby.class.getSimpleName(), new BabyTable());
		tables.put(BabyMoodSurvey.class.getSimpleName(), new BabyMoodSurveyTable());
		tables.put(GenericIndicator.class.getSimpleName(), new GenericIndicatorTable());
		tables.put(Log.class.getSimpleName(), new LogTable());
		openHelper = new DBOpenHelper(mContext, DATABASE_NAME, DATABASE_VERSION, tables.values());
	}

	public Collection<IndicatorTable> getTables() {
		return tables.values();
	}

	public void updateTables(final int parentId, final int babyId){
		Runnable mTask = new Runnable() {
			public void run() {
				for(IndicatorTable t: tables.values()){
					t.updateTable(parentId, babyId, mContext);
				}
			}
		};
		Thread thr = new Thread(null, mTask, "DownloadIndicatorsFromServer");
		thr.start();
	}

	public IndicatorTable getTableForIndicator(Indicator indicator) {
		if(indicator instanceof GenericSurvey) {
			return getSurveyTableByType(((GenericSurvey)indicator).getSurveyType());
		} 
		return getTableForClass(indicator.getClass());
	}

	public IndicatorTable getTableForClass(Class<?> objectClass) {
		return tables.get(objectClass.getSimpleName()).prepareTable(getDb());
	}

	public IndicatorTable getTableForClass(String objectClass) {
		return tables.get(objectClass).prepareTable(getDb());
	}

	public IndicatorTable getSurveyTableByType(SurveyType type) {
		return new GenericSurveyTable(type).prepareTable(getDb());
	}

	public void insertSingle(Indicator indicator) {
		insertSingle(indicator, true, true);
	}

	public void insertSingle(Indicator indicator, boolean upload, boolean sync) {
		//		open();

		IndicatorTable t = getTableForIndicator(indicator);
		if(indicator.getId() == null){
			indicator.setId(t.getLowestIndex()-1);
		}
		if(indicator.getLocalId() == null){
			indicator.setLocalid(t.getHighestLocalId()+1);
		}
		List<ContentValues> cvs = t.toCV(indicator);
		for (ContentValues cv : cvs) {
			long newId = t.insert(cv);
		}
		if(sync) {
			SyncService.threadedUpdateTables(mContext, this, null);
			//			SyncService.updateTables(mContext, this, null);
			Utilities.updateWidget(mContext);
		}
		//		SyncService.checkOccasionallyDoneIndicators(mContext, this);
	}

	public Log log(Integer userId, Integer babyId, Intent intent, String action) {
		Log log = new Log(userId, babyId, intent, action);
		insertSingle(log, false, false);
		return log;
	}

	public Log logInsert(Intent intent, Indicator indicator) {
		return log(intent, indicator, "INSERTING: ");
	}

	public Log logUpdate(Intent intent, Indicator indicator) {
		return log(intent, indicator, "UPDATING: ");
	}

	public Log logDelete(Intent intent, Indicator indicator) {
		return log(intent, indicator, "DELETING: ");
	}

	public Log log(Intent intent, Indicator indicator, String tag) {
		Log log = new Log(indicator.getCommonData().getIdUser(), indicator.getCommonData().getIdBaby(), intent, tag + indicator.toString());
		insertSingle(log, false, false);
		return log;
	}

	public Log heartbeat(Integer userId, Integer babyId, Intent intent) {
		Log log = new Log(userId, babyId, intent, Log.HEART_BEAT);
		insertSingle(log, true, false);
		return log;
	}

	public void update(Indicator indicator){
		update(indicator, true);
	}

	public void update(Indicator indicator, boolean sync){
		IndicatorTable t = getTableForClass(indicator.getClass().getSimpleName());

		indicator.common.setForUpdate(true);
		List<ContentValues> cvs = t.toCV(indicator);

		for (ContentValues cv : cvs) {
			t.update(cv);
		}

		if(indicator instanceof Baby){
			for(ContentValues cv: cvs){
				if(indicator instanceof Baby || indicator instanceof User){
					try{
						cv.put(IndicatorTable.FILE, ImageUtils.getBitmapAsByteArray(ImageUtils.decodeFile(cv.getAsString(IndicatorTable.ANDROID_FILENAME))));
						WebUtils.uploadSingleToServerWithBroadcast(cv, t, mContext, this);
					} catch (Exception e){
						Utilities.writeToWeeklyErrorLogFile(mContext, e);
						e.printStackTrace();
					}
				}
			}
		} else {
			if(sync) {
				SyncService.threadedUpdateTables(mContext, this, null);
			}
		}
		Utilities.updateWidget(mContext);
	}

	public void delete(Indicator indicator) {
		delete(indicator, true);
	}

	public void delete(Indicator indicator, boolean sync) {
		IndicatorTable t = getTableForClass(indicator.getClass().getSimpleName());

		indicator.common.setForDelete(true);
		List<ContentValues> cvs = t.toCV(indicator);

		for (ContentValues cv : cvs) {
			t.delete(cv);
		}

		if(sync) {
			SyncService.threadedUpdateTables(mContext, this, null);
		}

		Utilities.updateWidget(mContext);
	}

	public void upload(IndicatorTable t, ContentValues cv) {
		WebUtils.uploadSingleToServerNonThreaded(cv, t, mContext, this);
	}

	public void upload(IndicatorTable t, List<ContentValues> cvs) {
		for (ContentValues cv:cvs) {
			upload(t, cv);
		}
	}

	public void upload(IndicatorTable tableForClass, List<ContentValues>[] unSynchedIndictors) {
		for (List<ContentValues> list : unSynchedIndictors) {
			for (ContentValues cv : list) {
				upload(tableForClass, cv);
			}
		}
	}

	public Indicator[] getLastFiveIndicatorsForUser(String indicator, User kid){
		return getSetOfIndicators(indicator,kid, 0,5);
	}

	public Indicator[] getSetOfIndicators(String indicator, User kid, int rank, int count) {
		return getSetOfIndicators(indicator, IndicatorTable.WHERE + getUserTable().getCurrentKidWhereString(kid),rank,count);
	}
	public Indicator[] getSetOfIndicators(String indicator, String where, int rank, int count) {
		return getSetOfIndicators(indicator, where, rank, count, IndicatorTable.CREATED_AT + " DESC");
	}
	public Indicator[] getSetOfIndicators(String indicator, String where, int rank, int count, String orderBy) {
		return ((IndicatorTable)getTableForClass(indicator)).getSetOfIndicators(where,rank,count,orderBy);
	}

	public synchronized void close(double dbKey) {
		synchronized(lock){
			openDbKeys.remove(dbKey);

			if(openDbKeys.size() == 0){
				if(Database.db != null && Database.db.isOpen()) {
					Database.db.close();
				}
				Database.db = null;
			}
			dbKey = -1;
		}
	}

	/**
	 * decided to make get db and open do the same thing, it should try to open the database if it is not open
	 * @return
	 */
	public double open(double key) {
		synchronized(lock){
			if(Database.db != null && !Database.db.isOpen()){
				Database.db = null;
				openHelper = new DBOpenHelper(mContext, DATABASE_NAME, DATABASE_VERSION, tables.values());
			}
	
			if(Database.db == null){
				setDb(openHelper.getWritableDatabase());
				double random;
				do{
					random = Math.random();
				}
				while(openDbKeys.contains(random));
				openDbKeys.add(random);
				return random;
			}
			if(openDbKeys.contains(key)){
				return key;
			} else {
				double random;
				do{
					random = Math.random();
				}
				while(openDbKeys.contains(random));

				openDbKeys.add(random);
				return random;
			}
		}
	}

	public void setDb(SQLiteDatabase db) {
		Database.db = db;
	}

	/**
	 * decided to make get db and open do the same thing, it should try to open the database if it is not open
	 * @return
	 */
	public SQLiteDatabase getDb() {
		return Database.db;
	}

	public void sendDBtoSDCard() throws IOException {
		File sdDirectory = new File(SD_DIRECTORY);
		sdDirectory.mkdir();
		File data = Environment.getDataDirectory();

		if (sdDirectory.canWrite()) {
			String packageName = "data/edu.uci.ics.star.estrellita";//getClass().getPackage().getName();
			String currentDBPath = packageName + "/databases/" + DATABASE_NAME;
			String backupDBFileName = "backup"+DATABASE_NAME;

			File currentDB = new File(data, currentDBPath);
			File backupDB = new File(sdDirectory, backupDBFileName);

			if (currentDB.exists()) {
				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(backupDB).getChannel();

				long bytesTransferred = dst.transferFrom(src, 0, src.size());

				src.close();
				dst.close();
			}
		}
	}

	public boolean restoreDBFromSDCard() {
		try {

			File sdDirectory = new File(SD_DIRECTORY);
			String packageName = "data/edu.uci.ics.star.estrellita";//getClass().getPackage().getName();
			String currentDBPath = packageName + "/databases/" + DATABASE_NAME;
			String backupDBFileName = "backup"+DATABASE_NAME;


			File data = Environment.getDataDirectory();
			File backedUpDB = new File(sdDirectory, backupDBFileName);

			File currentDB = new File(data, currentDBPath);

			if (backedUpDB.exists() && currentDB.exists()) 
			{
				FileChannel src = new FileInputStream(backedUpDB).getChannel();
				FileChannel dst = new FileOutputStream(currentDB).getChannel();

				long bytesTransferred = dst.transferFrom(src, 0, src.size());

				src.close();
				dst.close();
				return true;
			}
			return false;

		} catch (Exception e) {
			Utilities.writeToWeeklyErrorLogFile(mContext, e);
//			Utilities.println("restoreDBFromSDCard() Failed: " + e.toString());
			return false;
		}
	}

	public UserTable getUserTable(){
		return (UserTable) getTableForClass(User.class);
	}

	public BabyTable getBabyTable(){
		return (BabyTable) getTableForClass(Baby.class);
	}

	public PostTable getPostTable(){
		return (PostTable) getTableForClass(Post.class);
	}

	public WeightTable getWeightTable() {
		return (WeightTable) getTableForClass(Weight.class);
	}

	public BondingSurveyTable getBondingTable() {
		return (BondingSurveyTable) getTableForClass(BondingSurvey.class);
	}

	public DiaperTable getDiaperTable() {
		return (DiaperTable) getTableForClass(Diaper.class);
	}

	public AppointmentTable getAppointmentTable() {
		return (AppointmentTable) getTableForClass(Appointment.class);
	}

	public BabyMoodSurveyTable getBabyMoodTable() {
		return (BabyMoodSurveyTable) getTableForClass(BabyMoodSurvey.class);
	}

	public MoodMapSurveyTable getMoodMapTable() {
		return (MoodMapSurveyTable) getTableForClass(MoodMapSurvey.class);
	}

	public GenericIndicatorTable getGenericIndicatorTable() {
		return (GenericIndicatorTable) getTableForClass(GenericIndicator.class);
	}

	public GenericSurveyTable getEpdsSurveyTable() {
		return (GenericSurveyTable) getSurveyTableByType(SurveyType.EPDS);
	}

	public GenericSurveyTable getStressSurveyTable() {
		return (GenericSurveyTable) getSurveyTableByType(SurveyType.STRESS);
	}

	public GenericSurveyTable getPreAppointmentSurveyTable() {
		return (GenericSurveyTable) getSurveyTableByType(SurveyType.PRE_APPT);
	}

	public GenericSurveyTable getPostAppointmentSurveyTable() {
		return (GenericSurveyTable) getSurveyTableByType(SurveyType.POST_APPT);
	}

	public LogTable getLogTable() {
		return (LogTable) getTableForClass(Log.class);
	}

}
