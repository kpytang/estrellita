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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.WebUtils;



/*
CREATE TABLE IF NOT EXISTS `baby` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `person_id` varchar(40) NOT NULL,
  `record_id` varchar(40) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `photo` varchar(200) NOT NULL,
  `actual_age` int(11) NOT NULL,
  `adjusted_age` int(11) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=13 ;

 */
public class BabyTable extends IndicatorTable {
	private static final String USER_TABLE = "baby";
	private static final String NAME = "name";
	private static final String PHOTO = "photo";
	private static final String ACTUAL_BIRTHDAY = "actual_birthday";
	private static final String EXPECTED_BIRTHDAY = "expected_birthday";
	private static final String GENDER = "gender";

	private Map<Integer, Baby> mBabies;

	public BabyTable() {
		super(USER_TABLE, 
				",'" + NAME + "' varchar(100)" +
				",'" + PHOTO + "' varchar(200)" +
				",'" + ACTUAL_BIRTHDAY + "' date" +
				",'" + EXPECTED_BIRTHDAY + "' date" +
				",'" + GENDER + "' varchar(100)");
		mBabies = new HashMap<Integer, Baby>();
		getsUpdated = false;

	}

	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof Baby){
			Baby baby = (Baby) indicator;
			cv.put(NAME, baby.getName());
			cv.put(PHOTO, baby.getPhotoURL());
			cv.put(ACTUAL_BIRTHDAY, DateUtils.formatDateAsSimpleDate(baby.getActualBirthday()));
			cv.put(EXPECTED_BIRTHDAY, DateUtils.formatDateAsSimpleDate(baby.getExpectedBirthday()));
			cv.put(GENDER, baby.getGender().toString());
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		Baby[] results = new Baby[cursor.getCount()];
		Baby d;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();
			// 5 fields: name, photo, actual_age, adjusted_age, gender
			d = new Baby(this.getCommonData(cursor), 
					cursor.getString(i++), cursor.getString(i++), DateUtils.stringToDate(cursor.getString(i++)), DateUtils.stringToDate(cursor.getString(i++)), cursor.getString(i++));
			results[k] = d;
		}
		return results;
	}

	public Class<?> getIndicatorClass() {
		return Baby.class;
	}

	public Baby getBaby(int id, Context context) throws NoUserException {
		// check if we already have the baby info in memory
		if(id == -1){
			id = EstrellitaTiles.getCurrentBabyId();
			if (id == -1) {
				throw new NoUserException();
			}
		}
		Baby baby = (Baby) mBabies.get(id);
		// if it's not (e.g., it's the first time you're logging in)
		if(baby == null){
			try{
				baby =  ((Baby[]) getSetOfIndicators(WHERE + BABY_ID + "=" + id,0,1))[0];
				baby.setImage(null);
				mBabies.put(id, baby);
			} catch(ArrayIndexOutOfBoundsException aiobe){
				// this will occur when there is nothing in the cursor (baby is null)
				try{
					// so we check the server for the user's info 
					JSONObject ju = WebUtils.getBabyFromServer(id);
					baby = new Baby(getCommonData(ju.getJSONObject(IndicatorTable.COMMON_DATA), context), 
							ju.getString(NAME), ju.getString(PHOTO), 
							DateUtils.mysqlStringToDate(ju.getString(ACTUAL_BIRTHDAY)), DateUtils.mysqlStringToDate(ju.getString(EXPECTED_BIRTHDAY)), ju.getString(GENDER));
					// now we'll add this to the cursor
					insert(toCV(baby));
					mBabies.put(id, baby);
				} catch(Exception e){
					e.printStackTrace();
					throw new NoUserException();
				}
			}
		}
		return baby;
	}

	public List<Baby> getBabies(List<Integer> ids, Context context) {
		List<Baby> babies = new ArrayList<Baby>();
		for (int id: ids) {
			if (mBabies.containsKey(id)) {
				babies.add(mBabies.get(id));
			}
			else {
				try {
					babies.add(getBaby(id, context));
				} catch (NoUserException e) {
					e.printStackTrace();
				}
			}
		}
		return babies;
	}
}
