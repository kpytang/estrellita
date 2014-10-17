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


import java.util.Collection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;

public class DBOpenHelper extends SQLiteOpenHelper {

	private Collection<IndicatorTable> tables;
	private final int databaseVersion;

	DBOpenHelper(Context context, String databaseName, int databaseVersion, Collection<IndicatorTable> collection) {
		super(context, databaseName, null, databaseVersion);
		this.databaseVersion = databaseVersion;
		this.tables = collection;
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		for (IndicatorTable table : tables) {

			try{
				db.execSQL(table.getCreateTableString());
			} catch (SQLiteException se){
				// ignore				
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (IndicatorTable table : tables) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ table.getTableName());
		}
		onCreate(db);
	}

}
