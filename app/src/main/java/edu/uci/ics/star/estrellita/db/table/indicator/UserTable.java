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

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.utils.WebUtils;



public class UserTable extends IndicatorTable {
	private static final String USER_TABLE = "user";
	private static final String FIRST_NAME = "first_name";
	private static final String LAST_NAME = "last_name";	
	private static final String USERNAME = "username";
	private static final String PASSWD = "passwd";
	private static final String PHOTO = "photo";
	private static final String USER_TYPE = "user_type";
	private static final String EMAIL = "email";
	
	private HashMap<Integer, User> users;

	public UserTable() {
		super(USER_TABLE, 
				",'" + FIRST_NAME + "' varchar(100)" +
				",'" + LAST_NAME + "' varchar(100)" +
				",'" + USERNAME + "' varchar(100)" +
				",'" + PASSWD + "' varchar(100)" +
				",'" + PHOTO + "' varchar(200)" +
				",'" + USER_TYPE + "' varchar(100)" +
				",'" + EMAIL + "' varchar(200)");
		users = new HashMap<Integer, User>();
		getsUpdated = false;
	}
	
	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof User){
			User user = (User) indicator;
			cv.put(FIRST_NAME, user.getFirstName());
			cv.put(LAST_NAME, user.getLastName());
			cv.put(USERNAME, user.getUsername());
			cv.put(PASSWD, user.getPassword());
			cv.put(PHOTO, user.getPhotoURL());
			cv.put(USER_TYPE, user.getUserType().toString());
			cv.put(EMAIL, user.getEmail());
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		User[] results = new User[cursor.getCount()];
		User d;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();
			// 7 fields: first_name, last_name, username, passwd, photo, user_type, email
			d = new User(this.getCommonData(cursor), 
					cursor.getString(i++), cursor.getString(i++), cursor.getString(i++)
					, cursor.getString(i++), cursor.getString(i++), cursor.getString(i++), cursor.getString(i++));
			results[k] = d;
		}
		return results;
	}

	public Class<?> getIndicatorClass() {
		return User.class;
	}

	public User getUser(int id, Context context) throws NoUserException {
		// check if we already have the user info in memory
		User u = (User) users.get(id);
		// if it's not (e.g., it's the first time you're logging in)
		if(u == null){
			try{
				u =  ((User[]) getSetOfIndicators(WHERE + "user_id=" + id,0,1))[0];
				users.put(id, u);
			} catch(ArrayIndexOutOfBoundsException aiobe){
				// this will occur when there is nothing in the cursor (User u is null)
				try{
					// so we check the server for the user's info 
					JSONObject ju = WebUtils.getUserFromServer(id);
					u = new User(getCommonData(ju.getJSONObject(IndicatorTable.COMMON_DATA), context), 
							ju.getString(FIRST_NAME), ju.getString(LAST_NAME), ju.getString(USERNAME),
							ju.getString(PASSWD), ju.getString(PHOTO), ju.getString(USER_TYPE),
							ju.getString(EMAIL));
					// now we'll add this to the cursor
					insert(toCV(u));
					users.put(id, u);
				} catch(Exception e){
					e.printStackTrace();
					throw new NoUserException();
				}
			}
		}
		return u;
	}

	public User getUserAndKids(int id, List<Integer> kidIds, Context context, BabyTable bt) throws NoUserException {
		if(id == -1){
			throw new NoUserException();
		}
		User parent = (User) getUser(id, context);
		parent.getKids().clear();
		for(Integer i: kidIds){
			parent.addKid(bt.getBaby(i,context));
		}
		return parent;
	}

	public String getCurrentKidWhereString(User kid) {
		return "user_id==\'"+ kid.getId()+ "\'";
	}

}
