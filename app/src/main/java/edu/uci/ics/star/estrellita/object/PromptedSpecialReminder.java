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

package edu.uci.ics.star.estrellita.object;

import java.util.Date;

import android.content.Context;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class PromptedSpecialReminder extends SpecialReminder {
	private Appointment mAppt;

	public PromptedSpecialReminder(ReminderType type) {
		super(type);
		mAppt = null;
	}

	public void addAppointment(Appointment a) {
		mAppt = a;
	}

	public Appointment getAppointment() {
		return mAppt;
	}

	public boolean isPreAppointmentSurvey() {
		if (mType == ReminderType.PRE_APPT_SURVEY) {
			return true;
		}
		return false;
	}

	public boolean isPostAppointmentSurvey() {
		if (mType == ReminderType.POST_APPT_SURVEY) {
			return true;
		}
		return false;
	}

	public String getSurveyReminderString() {
		String s = "";
		switch (mType) {
		case PRE_APPT_SURVEY:
			s = "Survey re: upcoming appt with\n";
			if (mAppt != null) {
				s += "Dr. " + mAppt.getDoctorInfo();
				Date apptDateTime = DateUtils.combineDateAndTime(mAppt.getDate(), mAppt.getStartTime());
				s += " at " + DateUtils.getDateAsString(apptDateTime, DateUtils.APPOINTMENT_TIME_FORMAT) + 
				" on " + DateUtils.getDateAsString(apptDateTime, DateUtils.APPOINTMENT_SHORT_DATE_FORMAT);
			}
			break;
		case POST_APPT_SURVEY:
			s = "Survey re: recent appt with\n";
			if (mAppt != null) {
				s += "Dr. " + mAppt.getDoctorInfo();
				Date apptDateTime = DateUtils.combineDateAndTime(mAppt.getDate(), mAppt.getStartTime());
				s += " at " + DateUtils.getDateAsString(apptDateTime, DateUtils.APPOINTMENT_TIME_FORMAT) + 
				" on " + DateUtils.getDateAsString(apptDateTime, DateUtils.APPOINTMENT_SHORT_DATE_FORMAT);
			}
			break;
		case CALL_ST_JOSEPH:
			s = "Your emotional well-being is very important. Click for more info.";
			break;
		case CALL_DOCTOR:
			s = "Click here to call Dr. ";
			if (mAppt != null) {
				s += mAppt.getDoctorName() + "\'s office";
				if ( (mAppt.getPhone() != null) && (mAppt.getPhone().length() > 0) ) {
					s += " at " + mAppt.getPhone();
				}
				s += " to reschedule your appointment.";
			}
			break;
		}
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.object.SpecialReminder#evaluate(android.content.Context, java.lang.Integer)
	 */
	@Override
	public boolean evaluate(Context context, Integer childId) {
		return false;
	}

}
