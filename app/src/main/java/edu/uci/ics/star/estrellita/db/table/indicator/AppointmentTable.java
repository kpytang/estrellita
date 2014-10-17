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

import java.sql.Time;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.Appointment.DoctorType;
import edu.uci.ics.star.estrellita.utils.DateUtils;


/**
 * 15 fields:
 * appointment_id	int(11)
 * baby_id	int(11)
 * doctor_name	varchar(200)
 * doctor_type	enum('pediatrician','cardiac','endocrinology','gi','hematology','metabolic','neurology','neurosurgery','opthalmology','pulmonary','other')
 * date	date
 * start_time	time
 * end_time	time
 * location	varchar(200)
 * phone	varchar(100)
 * attended	tinyint(1) [aka boolean]
 * notes	mediumtext
 * diaper_concern	tinyint(1) [aka boolean]
 * mood_concern	tinyint(1) [aka boolean]
 * note_concern	tinyint(1) [aka boolean]
 * weight_concern	tinyint(1) [aka boolean]
 *
 */
/**
 * @author User
 *
 */
public class AppointmentTable extends IndicatorTable {

	public static final String DOCTOR_NAME = "doctor_name";
	public static final String DOCTOR_TYPE = "doctor_type";
	public static final String DATE = "date";
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String LOCATION = "location";
	public static final String PHONE = "phone";
	public static final String ATTENDED = "attended";
	public static final String DIAPER_CONCERN = "diaper_concern";
	public static final String MOOD_CONCERN = "mood_concern";
	public static final String NOTE_CONCERN = "note_concern";
	public static final String WEIGHT_CONCERN = "weight_concern";

	public AppointmentTable() {
		super("appointment", 
				",'" + DOCTOR_NAME + "' varchar(100)" + 
				",'" + DOCTOR_TYPE + "' varchar(100)" + 
				",'" + DATE + "' date" +
				",'" + START_TIME + "' date" +
				",'" + END_TIME + "' date" +
				",'" + LOCATION + "' varchar(100)" +
				",'" + PHONE + "' varchar(100)" +
				",'" + ATTENDED + "' int(1)" +
				",'" + DIAPER_CONCERN + "' int(1)" +
				",'" + MOOD_CONCERN + "' int(1)" +
				",'" + NOTE_CONCERN + "' int(1)" +
				",'" + WEIGHT_CONCERN + "' int(1)"
		);
		this.setDuration(DateUtils.MONTH);
		setOptional(true);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#addDataToCV(edu.uci.ics.star.estrellita.object.Indicator, android.content.ContentValues)
	 */
	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof Appointment){
			Appointment a = ((Appointment)indicator);
			cv.put(DOCTOR_NAME, a.getDoctorName());
			cv.put(DOCTOR_TYPE, a.getDoctorType().name().toString());
			cv.put(DATE, DateUtils.formatDateAsSimpleDate(a.getDate()));
			cv.put(START_TIME, DateUtils.formatDateAsTime(a.getStartTime()));
			cv.put(END_TIME, DateUtils.formatDateAsTime(a.getStartTime()));
			cv.put(LOCATION, a.getLocation());
			cv.put(PHONE, a.getPhone());
			cv.put(ATTENDED, (a.getAttendedState().ordinal()));
			cv.put(DIAPER_CONCERN, (a.isConcernedAboutDiapers()? 1: 0));
			cv.put(MOOD_CONCERN, (a.isConcernedAboutBabyMoods()? 1: 0));
			cv.put(NOTE_CONCERN, (a.isConcernedAboutCharts()? 1: 0));
			cv.put(WEIGHT_CONCERN, (a.isConcernedAboutWeight()? 1: 0));
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.IndicatorTable#getCVFromJSON(org.json.JSONObject)
	 */
	@Override
	protected ContentValues getCVFromJSON(JSONObject indicator) throws JSONException {
		ContentValues cv = super.getCVFromJSON(indicator);
		boolean b = Boolean.valueOf(cv.getAsString(ATTENDED));
		cv.put(ATTENDED, (b ? 1: 0));
		return cv;
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.estrellita.db.tables.IndicatorTable#getSetOfIndicators(java.lang.String, int, int, java.lang.String)
	 */
	@Override
	public Indicator[] getSetOfIndicators(String where, int rank, int count, String orderBy) {
		return super.getSetOfIndicators(where + IS_DELETED_STRING, rank, count, DATE +" DESC,"+START_TIME + " DESC");
	}

	/**
	 * @see edu.uci.ics.star.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	@Override
	public Indicator[] getIndicatorArray(Cursor cursor) {
		Appointment[] results = new Appointment[cursor.getCount()];
		Appointment a;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();
			// 12 fields: 
			// doctor_name, doctor_type, 
			// date, start_time, end_time, 
			// location, phone, 
			// attended, 
			// diaper_concern, mood_concern, note_concern, weight_concern

			// check for badly saved appointments (which have null doctor types)
			if (cursor.isNull(i+1)) {
				int j = i;
				i += 2;
				a = new Appointment(this.getCommonData(cursor),
						cursor.getString(j), DoctorType.OTHER.name(), 
						DateUtils.stringToDate(cursor.getString(i++)), toTime(cursor.getString(i++)),toTime(cursor.getString(i++)),
						cursor.getString(i++), cursor.getString(i++), 
						cursor.getInt(i++),
						cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++));
			}
			else {
				a = new Appointment(this.getCommonData(cursor),
						cursor.getString(i++), cursor.getString(i++), 
						DateUtils.stringToDate(cursor.getString(i++)), toTime(cursor.getString(i++)),toTime(cursor.getString(i++)),
						cursor.getString(i++), cursor.getString(i++), 
						cursor.getInt(i++),
						cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++), cursor.getInt(i++));
			}
			results[k] = a;
		}
		return results;
	}

	public Appointment getNextAppointment(int idBaby){
		Cursor cursor = getCursor(WHERE + BABY_ID + "==" + idBaby + IS_DELETED_STRING + " AND  datetime(" + DATE + " || " + START_TIME + " )  > datetime(\"now\", \"localtime\")"// > \'" 
				//				+ DateUtils.formatDateAsSimpleDate(DateUtils.getTimestamp()) + "\'"
				//+ " AND " + START_TIME +" > \'" + DateUtils.formatDateAsTime(DateUtils.getTimestamp()) + "\'"
				, 0, 1, DATE +" ASC,"+START_TIME + " ASC");

		Appointment indicator;
		try{
			indicator = (Appointment) getIndicatorArray(cursor)[0];
		} catch(ArrayIndexOutOfBoundsException aioobe){
			indicator = null;
		}
		finally {
			if(cursor != null)
				cursor.close();
		}

		return indicator;
	}

	public Appointment[] getAppointmentsByDoctor(int idBaby, String drName){
		return (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND " + DOCTOR_NAME + "=='" + drName + "'",
				0, -1, DATE +" ASC,"+START_TIME + " ASC");
	}

	public Appointment[] getUpcomingAppointments(int idBaby){
		return (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND datetime(" + DATE + " || " + START_TIME + " )  > datetime(\"now\", \"localtime\")"// > \'" 
				, 0, -1, DATE +" ASC,"+START_TIME + " ASC");
	}

	public Appointment[] getUpcomingAppointments(int idBaby, Date startingFrom){
		if (startingFrom == null) {
			return getUpcomingAppointments(idBaby);
		}
		return (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND datetime(" + DATE + " || " + START_TIME + " )  > datetime(" + startingFrom.getTime()/1000 + ", \"unixepoch\", \"localtime\")"// > \'" 
				, 0, -1, DATE +" ASC,"+START_TIME + " ASC");
	}

	public Appointment[] getPastAppointments(int idBaby){
		return (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND  datetime(" + DATE + " || " + START_TIME + " )  < datetime(\"now\", \"localtime\")"// > \'" 
				, 0, -1, DATE +" ASC,"+START_TIME + " ASC");
	}

	public Appointment[] getPastAppointments(int idBaby, Date startingFrom){
		if (startingFrom == null) {
			return getPastAppointments(idBaby);
		}
		return (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND  datetime(" + DATE + " || " + START_TIME + " )  < datetime(" + startingFrom.getTime()/1000 + ", \"unixepoch\", \"localtime\")"// > \'" 
				, 0, -1, DATE +" ASC,"+START_TIME + " ASC");
	}

	public Appointment[] getPastAppointments(Integer idBaby, int previousHours) {
		Date addHoursToDate = DateUtils.addHoursToDate(DateUtils.getTimestamp(), -previousHours);
		Appointment[] appts = (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND  datetime(" + DATE + " || " + START_TIME + " )  > datetime(" + addHoursToDate.getTime()/1000+", \"unixepoch\", \"localtime\")" +
				" AND  datetime(" + DATE + " || " + START_TIME + " )  < datetime(\"now\", \"localtime\")"// > \'" 
				, 0, -1, DATE +" ASC,"+START_TIME + " ASC");
		if (appts != null) {
			return appts;
		}
		else {
			return null;
		}
	}

	private Time toTime(String time){
		return Time.valueOf(time);
	}

	public Class<?> getIndicatorClass() {
		return Appointment.class;
	}

	public Appointment[] getNextAppointments(Integer idBaby, int nextHours) {
		Date addHoursToDate = DateUtils.addHoursToDate(DateUtils.getTimestamp(), nextHours);
		return (Appointment[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND  datetime(" + DATE + " || " + START_TIME + " )  < datetime(" + addHoursToDate.getTime()/1000+",\"unixepoch\", \"localtime\")" +
				" AND  datetime(" + DATE + " || " + START_TIME + " )  > datetime(\"now\", \"localtime\")"// > \'" 
				, 0, -1, DATE +" ASC,"+START_TIME + " ASC");
	}

	public Appointment getAppointmentById(int appointmentId) {
		Appointment[] a = (Appointment[]) getSetOfIndicators(WHERE + ID + "==" + appointmentId, 0, 1, DATE +" ASC,"+START_TIME + " ASC");
		if (a.length > 0) {
			return a[0];
		}
		else {
			return null;
		}
	}

	public Appointment getAppointmentByProperties(Appointment a) {
		String AND = " AND ";
		String whereClause = WHERE;
		if ( (a.getDoctorName() != null) && (a.getDoctorName().length() > 0) ) {
			whereClause += DOCTOR_NAME + "= '" + a.getDoctorName() + "'" + AND;
		}
		if ( a.getDoctorType() != null ) {
			whereClause += DOCTOR_TYPE + "= '" + a.getDoctorType().name() + "'" + AND;
		}
		if ( (a.getPhone() != null) && (a.getPhone().length() > 0) ) {
			whereClause += PHONE + "= '" + a.getPhone() + "'" + AND;
		}
		if ( (a.getLocation() != null) && (a.getLocation().length() > 0) ) {
			whereClause += LOCATION + "= '" + a.getLocation() + "'" + AND;
		}
		if ( (a.getDate() != null) && (a.getStartTime() != null) ) {
			Date d = DateUtils.combineDateAndTime(a.getDate(), a.getStartTime());
			whereClause += "datetime(" + DATE + " || " + START_TIME + " )==datetime(" + d.getTime()/1000 + ",\"unixepoch\", \"localtime\")";
		}
		// remove the last " AND " 
		if (whereClause.endsWith(AND)) {
			whereClause.substring(0, whereClause.length() - AND.length());
		}
		else if (whereClause.length() == WHERE.length()) {
			whereClause = "";
		}
		Appointment[] appts = (Appointment[]) getSetOfIndicators(whereClause, 0, 1, DATE +" ASC,"+START_TIME + " ASC");
		// make sure that this appt is marked for update (otherwise, there is some other reason it's not being pulled by id)
		if (appts.length > 0) {
			return appts[0];
		}
		return null;
	}
}
