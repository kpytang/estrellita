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

import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import edu.uci.ics.star.estrellita.activity.SurveyForm;
import edu.uci.ics.star.estrellita.activity.Wall;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.WrongIndicatorException;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.utils.DateUtils;


public class LogTable extends IndicatorTable {

	private static final String ACTION = "action";
	private static final String INTENT = "intent";
	private static final String ACTIVITY = "activity";

	/*
	CREATE TABLE `log` (
	  `idBaby` int(11) NOT NULL,
	  `dateTime` date NOT NULL,
	  `activity` varchar(200) NOT NULL,
	  `intent` varchar(200) NOT NULL,
	  `action` varchar(200) NOT NULL,
	  `flag` enum('urgent','warning','value') NOT NULL
	) ENGINE=MyISAM DEFAULT CHARSET=latin1;
	 */
	public LogTable() {
		super("log", "," +
				"'" + ACTIVITY + "' varchar(200) NOT NULL," +
				"'" + INTENT + "' varchar(200)," +
				"'" + ACTION + "' varchar(200)"
		);
		this.setOptional(true);
		this.setGetsUpdated(true);
	}

	@Override
	public boolean addDataToCV(Indicator indicator, ContentValues cv) throws WrongIndicatorException {
		if(indicator instanceof Log){
			Log l = (Log) indicator;
			cv.put(ACTIVITY, l.getActivity());
			cv.put(INTENT, l.getIntent());
			cv.put(ACTION, l.getAction());
			return true;
		}
		throw new WrongIndicatorException();
	}

	/* (non-Javadoc)
	 * @see ics.uci.edu.star.db.table.IndicatorTable#getIndicatorArray(android.database.Cursor)
	 */
	public Indicator[] getIndicatorArray(Cursor cursor) {
		Log[] results = new Log[cursor.getCount()];
		Log w;
		int i;
		for(int k = 0; k < cursor.getCount(); k ++){
			cursor.moveToPosition(k);
			i = this.getStartUncommonData();

			w = new Log(this.getCommonData(cursor), 
					cursor.getString(i++), cursor.getString(i++), cursor.getString(i++)
			);
			results[k] = w;
		}
		return results;
	}

	public Indicator getLastLogForPost(int babyId) {
		String where = " AND " + ACTIVITY + "=='" + Wall.class.getName() + "' AND " + ACTION + "=='" + Log.ONPAUSE + "'";
		return getLastBabyIndicator(babyId, where);
	}

	@Override
	public Class<?> getIndicatorClass() {
		return Log.class;
	}

	public Indicator getLastLogForLog(Log l) {
		String where = " AND " + ACTIVITY + "=='" + l.getActivity() + "' AND " + ACTION + "=='" + l.getAction() + "' AND " + INTENT + "=='" + l.getIntent() + "'";
		return getLastBabyIndicator(l.getCommonData().getIdBaby(), where);
	}

	// check for EPDS call later tag within the past 5 days
	public boolean hasEPDSCallLaterFlag(int parentId) {
		String where = " AND " + ACTIVITY + "=='" + SurveyType.EPDS.name() + "' AND " + INTENT + "=='" + SurveyForm.CALL_LATER_TAG + "'";
		Log log = (Log) getLastParentIndicator(parentId, where);
		// if there is a log referring to a EPDS survey CALL_LATER, then we should check its date
		// the CALL_LATER tag refers to when the reminder should START
		// since we have a window of 5 days, we need to check that today's date is between the CALL_LATER timestamp and 5 day's from that date
		// let's also make sure that users haven't already called within that window of 5 days
		if (log != null) {
			Date endOfWindow = DateUtils.addToDate(log.getDateTime(), Calendar.DAY_OF_YEAR, 5);
			endOfWindow = DateUtils.getEndOfDay(endOfWindow);
			Date now = DateUtils.getTimestamp();
			// we're checking between dates, because the log date could be in the future 
			// future log dates are so that the call reminder can show up during normal business working days
			if (now.after(log.getDateTime()) && now.before(endOfWindow)) {
				where = " AND " + ACTIVITY + "=='" + SurveyType.EPDS.name() + "' AND " + 
				INTENT + "=='" + SurveyForm.ALREADY_CALLED_TAG + "' AND " + 
				ACTION + "=='" + log.getAction() + "'";
				log = (Log) getLastParentIndicator(parentId, where);
				// if we find this, then they've already called someone 
				if (log != null) {
					// and we don't need to remind them again
					return false;
				}
				else {
					// they still haven't called someone, so we keep reminding them (for 5 days)
					return true;
				}
			}
		}
		// there aren't any call later tags, so it means that they haven't received an epds call alert 
		// i.e., they haven't scored high enough on the epds to trigger a st joseph call alert
		return false;
	}

	public int getAppointmentIdForYesterdaySurveyCallLater(int babyId, SurveyType surveyType) {
		String where = " AND " + ACTIVITY + "=='" + surveyType.name() + "' AND " + INTENT + "=='" + SurveyForm.CALL_LATER_TAG + "'";
		Log log = (Log) getLastBabyIndicator(babyId, where);
		// there is a log referring to a PRE_APPT survey CALL_LATER, so let's check the date to make sure it's from yesterday
		if (log != null) {
			Date yesterday = DateUtils.getStartOfDay(DateUtils.getYesterdayDate());
			if (log.getDateTime().after(yesterday) && log.getDateTime().before(DateUtils.getStartOfDay())) {
				int id = Integer.parseInt(log.getAction());
				where = " AND " + ACTIVITY + "=='" + surveyType.name() + "' AND " + 
					INTENT + "=='" + SurveyForm.REMINDED_CALL_LATER_TAG + "' AND " + 
					ACTION + "=='" + log.getAction() + "'";
				log = (Log) getLastBabyIndicator(babyId, where);
				// if we can't find this, then we haven't reminded 
				if (log == null) {
					return id;
				}
				else {
					return -1;
				}
			}
		}
		return -1;
	}
}
