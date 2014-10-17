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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.Response;
import edu.uci.ics.star.estrellita.object.indicator.Survey;
import edu.uci.ics.star.estrellita.utils.DateUtils;


/**
 * 9 fields:
 * response_id	int(11)
 * user_id	int(11)
 * survey_id	int(11)
 * question_id	int(11)
 * response	text
 * flag enum('urgent','warning','value')
 * score varchar(80)
 * created_at	datetime
 * inserted_at datetime
 * @param <T>
 *
 *
 * Note: the survey_id is actually just the timestamp of when the indicator was inserted
 */
public abstract class SurveyTable extends IndicatorTable {

	public static final String SURVEY_TYPE = "survey_type";
	public static final String SURVEY_ID = "survey_id";
	public static final String QUESTION_ID = "question_id";
	public static final String RESPONSE_INDICES = "response_indices";
	public static final String RESPONSE_TEXT = "response_text";
	public static final String ANSWERED_AT = "answered_at";

	public static final String SCORE = "score";
	protected String mSurveyType;
	private boolean mIsGroupByDate = false;

	public SurveyTable(String surveyType) {
		super("response", 
				",'" + SURVEY_TYPE + "' varchar(100) NOT NULL," +
				"'" + SURVEY_ID + "' int(11) NOT NULL," +
				"'" + QUESTION_ID + "' int(11) NOT NULL," +
				"'" + RESPONSE_INDICES + "' int(11)," +
				"'" + RESPONSE_TEXT + "' varchar(255)," +
				"'" + ANSWERED_AT + "' date," +
				"'" + SCORE + "' int(11)"
		);
		this.setSurveyType(surveyType);
	}

	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof Survey){
			Survey s = ((Survey)indicator);
			// for each question in the survey, add a row for the response
			cv.put(SURVEY_TYPE, s.getSurveyType().name());
			switch(s.getSurveyType()) {
			case PRE_APPT:
			case POST_APPT:
				// this ID will be the same as the appointment Id!
				cv.put(SURVEY_ID, s.getSurveyId());
				break;
			default:
				cv.put(SURVEY_ID, s.getCommonData().getTimestamp().getTime());
				break;
			}
			cv.put(SCORE, s.getScore());
			return true;
		}
		throw new WrongIndicatorException();
	}

	public String andWhereTypeEquals(String otherComparer) {
		return super.andWhereTypeEquals(SURVEY_TYPE, otherComparer);
	}

	public String orWhereSurveyTypeEquals(String... other){
		return super.orWhereTypeEquals(SURVEY_TYPE, other);
	}

	public List<ContentValues> toCV(Indicator indicator) {
		List<ContentValues> cvs = new ArrayList<ContentValues>();
		ContentValues cv;
		int i = 0;

		Survey s = (Survey) indicator;
		Map<Integer, List<Response>> responses = s.getResponses();

		for (Integer question : responses.keySet()) {

			List<Response> responseSet = s.getResponseSet(question);
			
			for (Response response : responseSet) {
				try {
					cv = new ContentValues();
					this.addCommonToCV(indicator.getCommonData(), cv);
					this.addDataToCV(s, cv);
					cv.put(QUESTION_ID, question);
					cv.put(RESPONSE_INDICES, (Integer)response.getData());
					cv.put(RESPONSE_TEXT, response.getText());
					cv.put(ANSWERED_AT, response.getCommonData().getTimestamp().getTime());
					cv.put(ID, s.getId()-i);
					cv.put(LOCAL_ID, s.getLocalId()+i);
					cvs.add(cv);
					i++;
				} catch (WrongIndicatorException e) {
					e.printStackTrace();
				}
			}
		}

		return cvs;
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.estrellita.db.tables.IndicatorTable#getUnsynchedIndicators()
	 */
	public Indicator[] getUnsynchedIndicators() {
		Cursor cursor = getCursor(WHERE + SYNCED + " =0" + andWhereTypeEquals(getSurveyType()), -1, 0, IndicatorTable.LOCAL_ID + " ASC ");
		try {
			Indicator[] results = getIndicatorArray(cursor);
			return results;
		} catch(Exception e){
			return new Indicator[0];
		} finally{
			if(cursor != null)
				cursor.close();
		}
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.estrellita.db.tables.IndicatorTable#getSetOfIndicators(java.lang.String, int, int, java.lang.String)
	 */
	public Indicator[] getSetOfIndicators(String where, int offset, int limit, String orderBy) {
		//		SELECT * from survey join (select type, max(dateTime) as MaxDT from survey where type = "Mood Map" group by DATE(dateTime)) as DAY on survey.dateTime = Day.MaxDT order by dateTime desc;
		if(where == null){
			where = " " + WHERE;
		} else { 
			if(!where.startsWith(WHERE)) {
				where = WHERE + where;
			}
			where += " AND ";
		}
		String groupby = "";
		if(isGroupByDate()){
			groupby = " JOIN (SELECT " +
			SURVEY_TYPE +  ", MAX(" + CREATED_AT+  ") AS MaxDT FROM " + getTableName()+  
			where + getTableName()+ "."+SURVEY_TYPE +  "=='" + getSurveyType() + "' " +
			" GROUP BY DATE(" + CREATED_AT+  
			"/1000, \"unixepoch\", \"localtime\") " +" ORDER BY MaxDT DESC LIMIT " + offset + "," + limit+
			") AS curday ON " + getTableName() + "." + CREATED_AT+  
			" = curday.MaxDT ";
			limit = -1;
		}

		groupby += where + getTableName()+ "."+SURVEY_TYPE +  "=='" + getSurveyType() + "' ";

		if(orderBy == null){
			orderBy = IndicatorTable.CREATED_AT + " DESC";
		}

		Cursor cursor = getCursor(groupby, offset, limit, orderBy);

		try{
			Survey[] results = (Survey[]) getIndicatorArray(cursor);
			return results;
		} catch(Exception e){
			e.printStackTrace();
			return new Survey[0];
		} finally{
			if(cursor != null)
				cursor.close();
		}
	}

	public Survey[] getBabySurveysForToday(int idBaby) {
		Survey[] indicators;
		try{
			indicators = (Survey[])getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND " + IndicatorTable.CREATED_AT + " > " + DateUtils.getStartOfDay().getTime(), -1, -1, CREATED_AT + " DESC");
			return indicators;
		} catch(Exception e){
			e.printStackTrace();
			indicators = null;
		} 
		return indicators;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getLastBabyIndicator(int, java.lang.String)
	 */
	@Override
	public Survey getLastBabyIndicator(int babyId, String otherComparer) {
		String whereClause = " AND " + getTableName()+ "."+SURVEY_TYPE +  "=='" + getSurveyType() + "'";
		if (otherComparer != null) {
			whereClause += otherComparer;
		}
		return (Survey) super.getLastBabyIndicator(babyId, whereClause);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getLastParentIndicator(int, java.lang.String)
	 */
	@Override
	public Survey getLastParentIndicator(int userId, String otherComparer) {
		String whereClause = " AND " + getTableName()+ "."+SURVEY_TYPE +  "=='" + getSurveyType() + "'";
		if (otherComparer != null) {
			whereClause += otherComparer;
		}
		return (Survey) super.getLastParentIndicator(userId, whereClause);
	}
	
	// returns Survey rows that have timestamps between the ranges 
	public Survey[] getUserSurveysForTimeRange(int idUser, Date start, Date end){
		return getSurveysForTimeRange(USER_ID, idUser, start, end);
	}

	// returns all Survey rows for that user
	public Survey[] getAllSurveysForUser(int idUser){
		String whereClause = WHERE + USER_ID +  "==" + idUser ;
		return (Survey[])getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
	}

	// returns a particular survey for that user
	public Survey getSurveyForUser(int userId, int surveyId){
		String whereClause = WHERE + USER_ID +  "==" + userId + " AND " + SURVEY_ID + "==" + surveyId;
		Survey[] surveys = (Survey[]) getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
		if (surveys.length>0) {
			return surveys[0];
		}
		else {
			return null;
		}
	}

	// returns Survey rows that have timestamps between the ranges 
	public Survey[] getBabySurveysForTimeRange(int idBaby, Date start, Date end){
		return getSurveysForTimeRange(BABY_ID, idBaby, start, end);
	}
	
	public Survey[] getSurveysForTimeRange(String comparator, int idBaby, Date start, Date end){
		String whereClause =  WHERE + comparator +  "==" + idBaby + " AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") >= date(" + start.getTime()/1000 +  ", \"unixepoch\", \"localtime\") " +
		"AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") <= date(" + end.getTime()/1000 +  ", \"unixepoch\", \"localtime\")";
		return (Survey[])getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
	}

	// returns all Survey rows for that user
	public Survey[] getAllSurveysForBaby(int idBaby){
		String whereClause = WHERE + BABY_ID +  "==" + idBaby ;
		return (Survey[])getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
	}

	// returns a particular survey for that user
	public Survey getSurveyForBaby(int idBaby, int surveyId){
		String whereClause = WHERE + BABY_ID +  "==" + idBaby + " AND " + SURVEY_ID + "==" + surveyId;
		Survey[] surveys = (Survey[]) getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
		if (surveys.length>0) {
			return surveys[0];
		}
		else {
			return null;
		}
	}

	// returns a survey (response) for a user, for a particular question in a particular survey
	public List<Response> getSurveyResponseSetForBaby (int idBaby, int surveyId, int questionId){
		String whereClause = WHERE + BABY_ID +  "==" + idBaby + " AND " + SURVEY_ID + "==" + surveyId + " AND " + QUESTION_ID + "==" + questionId;
		Survey[] surveys = (Survey[]) getSetOfIndicators(whereClause, -1, -1, IndicatorTable.CREATED_AT +  " ASC");
		if (surveys.length>0) {
			Survey survey = surveys[0];
			return survey.getResponseSet(questionId);
		}
		else {
			return new ArrayList<Response>();
		}
	}

	public int getNumberOfCompletedSurveysForUser(int idParent, Date start, Date end) {
		String whereClause =  WHERE + USER_ID +  "==" + idParent + " AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") >= date(" + start.getTime()/1000 +  ", \"unixepoch\", \"localtime\") " +
		"AND date(" + CREATED_AT + "/1000, \"unixepoch\", \"localtime\") <= date(" + end.getTime()/1000 +  ", \"unixepoch\", \"localtime\")";
		whereClause += " AND " + getTableName() + "." + SURVEY_TYPE + "=='" + mSurveyType + "'";
		whereClause += " GROUP BY " + SURVEY_ID;
		Cursor cursor = getCursor(whereClause);
		try {
			Survey[] surveys = (Survey[]) getIndicatorArray(cursor);
			if (surveys != null) {
				return surveys.length;
			}
		} catch(Exception e){
			return 0;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.estrellita.db.tables.GenericTable#getIndicatorClass()
	 */
	public Class<?> getIndicatorClass() {
		return Survey.class;
	}

	/**
	 * @param mSurveyType the mSurveyType to set
	 */
	public void setSurveyType(String mSurveyType) {
		this.mSurveyType = mSurveyType;
	}

	/**
	 * @return the mSurveyType
	 */
	public String getSurveyType() {
		return mSurveyType;
	}

	/**
	 * @param groupByDate the groupByDate to set
	 */
	public void setGroupByDate(boolean groupByDate) {
		this.mIsGroupByDate = groupByDate;
	}

	/**
	 * @return the groupByDate
	 */
	public boolean isGroupByDate() {
		return mIsGroupByDate;
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Survey[] getIndicatorArray(Cursor cursor) {
		//		Survey[] results = new Survey[cursor.getCount()];
		int i;
		HashMap<Integer, Survey> surveys = new HashMap<Integer, Survey>();
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();

			// there are 7 fields: survey_type, survey_id, question_id, response, response_text, answer_at, score
//			",'" + SURVEY_TYPE + "' varchar(100) NOT NULL," +
//			"'" + SURVEY_ID + "' int(11) NOT NULL," +
//			"'" + QUESTION_ID + "' int(11) NOT NULL," +
//			"'" + RESPONSE_INDICES + "' int(11)," +
//			"'" + RESPONSE_TEXT + "' varchar(255)," +
//			"'" + ANSWERED_AT + "' date," +
//			"'" + SCORE + "' int(11)"
			
			CommonData commonData = this.getCommonData(cursor);
			String surveyType = cursor.getString(i++);

			int tempSurveyId = cursor.getInt(i++);
			int tempQuestionId = cursor.getInt(i++);
			int response = cursor.getInt(i++);
			String responseText = cursor.getString(i++);
			i++; // this is to skip the answer_at field
			int score = cursor.getInt(i++);

			try {
				Constructor<?> constructor = getIndicatorClass().getConstructor(CommonData.class, String.class, int.class, int.class);
				if(surveys.get(tempSurveyId) == null){
					Survey newInstance = (Survey)constructor.newInstance(commonData,surveyType, 
							tempSurveyId, score);
					surveys.put(tempSurveyId, newInstance);
				}
				Survey s = surveys.get(tempSurveyId);
				s.addResponse(tempQuestionId, new Response(commonData, response, responseText));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

		}
		List<Survey> values = new ArrayList<Survey>(surveys.values());
		Collections.sort(values);
		Collections.reverse(values);
		Object newArray = Array.newInstance(getIndicatorClass(), values.size());
		int j=0;
		for(Survey s: values){
			Array.set(newArray, j, s);
			j++;
		}

		return (Survey[])newArray;
	}
}

