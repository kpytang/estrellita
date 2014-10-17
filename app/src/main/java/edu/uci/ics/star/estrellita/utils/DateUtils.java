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

package edu.uci.ics.star.estrellita.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.uci.ics.star.estrellita.chart.BaseChart.TimeScale;

public class DateUtils {
	public static final String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String AMPM_TIME_FORMAT = "h:mm a";
	public static final String READABLE_DATE_FORMAT = "MM/dd/yy";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String READABLE_DATE_TIME_FORMAT = "MM/dd/yy hh:mm a";
	public static final String MYSQL_DATE_TIME_FORMAT = "MM/dd/yy hh:mm:ss a";
	public static final String PICTURE_DATE_TIME_FORMAT = "yyyyMMdd_HHmmss";
	public static final String VERSION_FORMAT = "yyyyMMdd";

	// formats for the UI
	public static final String TILE_DATE_FORMAT = "EEEE, MMM d";
	public static final String TILE_HOUR_FORMAT = "h:mm a";
	public static final String CALENDAR_DATEPICKER_FORMAT = "EEEE, MMM d, yyyy";
	public static final String CALENDAR_DATEPICKER_FORMAT_SHORTER = "EEE, MMM d, yyyy";
	public static final String APPOINTMENT_LONG_DATE_FORMAT = "EEEE, MMMM dd, yyyy";
	public static final String APPOINTMENT_SHORT_DATE_FORMAT = "EEE, MMM d";
	public static final String APPOINTMENT_TIME_FORMAT = "h:mm a";
	public static final String DATE_AND_TIME_FORMAT_FULL = "EEE, MMM d @ h:mm a";
	public static final String DATE_ONLY_FORMAT = "MMMM dd, yyyy";
	public static final String DATE_HEADER_FORMAT = "EEEE, MMMM d";
	public static final String MONTH_AND_YEAR = "MMMM yyyy";
	public static final String HOUR_ONLY = "h a";
	public static final String MONTH_ONLY = "MMM";
	public static final String WEEK_START_DATE = "M/d";
	public static final String MONTH_AND_DAY_ONLY = "MMMM d";
	public static final String DAY_AND_YEAR_ENDING = "d, yyyy";
	public static final String DAY_AND_TIME_ONLY = "EEE @ h:mm a";

	public static final int WEEK = 7;
	public static final int MONTH = 30;
	public static final int DAY = 1;

	public static final String[] DAYS_OF_WEEK = {"Sun", "Mon", "Tues", "Wed", "Thu", "Fri", "Sat"};

	/**
	 * Gets the current time and returns it as a Timestamp
	 * 	 * @return
	 */
	public static Timestamp getTimestamp(){
		return new Timestamp(getTimeInMillis());
	}

	public static long getTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * Get at the moment within ISO8601 format.
	 * @return
	 * Date and time in ISO8601 format.
	 */
	public static String getNowString(String format)
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(c.getTime());
	}

	/**
	 * Takes in a date and returns the time as string formatted as 
	 * Utilities.TIME_FORMAT which is currently HH:mm:ss
	 * @param A date with a specified time
	 * @return The time part of the date formatted
	 */
	public static String formatDateAsTime(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.TIME_FORMAT);
		return sdf.format(date);
	}
	/**
	 * Takes in a date and returns it's date part formateed as
	 * Utilities.DATE_FORMAT which is currently yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public static String formatDateAsSimpleDate(Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.DATE_FORMAT);
		return sdf.format(date);
	}

	/**
	 * @param string
	 * @return
	 */
	public static Date stringToDate(String string){
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.DATE_FORMAT);
		try {
			return sdf.parse(string);
		} catch (ParseException e) {
//			Utilities.println(e);
			return null;
		}
	}

	public static Date mysqlStringToDate(String string){
		SimpleDateFormat sdf =
			new SimpleDateFormat(DateUtils.MYSQL_DATE_TIME_FORMAT);

		try {
			return sdf.parse(string);
		} catch (ParseException e) {
//			Utilities.println(e);
			return null;
		}
	}

	/**
	 * Formats the timestamp in a way that is readible to people,
	 * "hh:mm a"
	 * @param time
	 * @return
	 */
	public static String formatTimeAsAMPM(Time time){
		return new SimpleDateFormat(AMPM_TIME_FORMAT).format(time);
	}

	/**
	 * Takes in a date and returns it's date part formateed as
	 * Utilities.DATE_FORMAT which is currently MM/dd/yy
	 * @param date
	 * @return
	 */
	public static String formatReadableDate(Date date){
		return new SimpleDateFormat(DateUtils.READABLE_DATE_FORMAT).format(date);
	}

	/**
	 * Formats the timestamp in a way that is readible to people,
	 *  "MM/dd/yy hh:mm a"
	 * @param timestamp
	 * @return
	 */
	public static String formatReadableTimestamp(Date timestamp){
		return new SimpleDateFormat(READABLE_DATE_TIME_FORMAT).format(timestamp);
	}

	/**
	 * Returns the current day of the week Sunday=1 through Saturaday=7
	 * @return
	 */
	public static int getDay() {
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * Returns the difference of days between a date and the current date.
	 * Note that DateUtils.getDaysPast(Date) counts a day as crossing midnight.
	 * so one min to midnight and one min after midnight would return "1".
	 * @param dateTime
	 * @return Number of days ago dateTime occured.
	 */
	public static int getDaysPast(Date dateTime) {
		long difference = getTimeInMillis() - getStartOfDay(dateTime).getTime();
		difference = difference / 1000/60/60/24;
		return Long.valueOf(difference).intValue();
	}

	/**
	 * returns the difference of hours between given date and the current time.
	 * @param dateTime
	 * @return number of hours ago dateTime occurred.
	 */
	public static int getHoursPast(Date dateTime) {
		long difference = getTimeInMillis() - dateTime.getTime();
		difference = difference / 1000/60/60;
		return Long.valueOf(difference).intValue();
	}


	/**
	 * returns the difference of hours between given date and the current time.
	 * @param dateTime
	 * @return number of hours ago dateTime occurred.
	 */
	public static int getHoursUntil(Date dateTime) {
		long difference =  dateTime.getTime() - getTimeInMillis();
		difference = difference / 1000/60/60;
		return Long.valueOf(difference).intValue();
	}

	/**
	 * returns the difference of hours between given date and the current time.
	 * @param dateTime
	 * @return number of hours ago dateTime occurred.
	 */
	public static int getMinutesUntil(Date dateTime) {
		long difference =  dateTime.getTime() - getTimeInMillis();
		difference = difference / 1000/60;
		return Long.valueOf(difference).intValue();
	}

	/**
	 * converts Date to String, following the specified format 
	 * 
	 * @param d date that you want to format
	 * @param format format string (for how Date will print out)
	 * @return
	 */
	public static String getDateAsString(Date d, String format) {
		if (format == TILE_DATE_FORMAT) {
			int dayOfYear = getDayOfYear(d);
			if (dayOfYear == getDayOfYear(DateUtils.getTimestamp())) {
				return "today";
			}
			else if (dayOfYear == (getDayOfYear(DateUtils.getTimestamp()) - 1)) {
				return "yesterday";
			}
			else if (dayOfYear == (getDayOfYear(DateUtils.getTimestamp()) + 1)) {
				return "tomorrow";
			}
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(d);
	}

	/**
	 * uses specified format to parse String as a Date
	 *  
	 * @param s date string that you want to be parsed
	 * @param format format string (for how to parse String)
	 * @return
	 */
	public static Date parseString(String s, String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			return formatter.parse(s);
		} catch (ParseException e) {
//			Utilities.println(e);
			return null;
		}
	}

	public static boolean containsDayOfWeek(List<Timestamp> dates, int dayOfWeek) {
		for(int i=0; i<dates.size(); i++) {
			if (dates.get(i).getDay() == dayOfWeek) {
				return true;
			}
		}
		return false;
	}

	public static List<Timestamp> getDatesForAWeek(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		List<Timestamp> dates = new ArrayList<Timestamp>();
		// java Calendar's enum starts from 1 (Sunday) and goes to 7 (Saturday)
		for (int i=1; i<=7; i++) {
			dates.add(new Timestamp(getNthDayOfWeek(c, i).getTime()));
		}
		return dates;
	}

	public static List<Timestamp> getDatesForAMonth(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		List<Timestamp> dates = new ArrayList<Timestamp>();
		for (int i=1; i<=getNumberOfDaysInMonth(d); i++) {
			dates.add(new Timestamp(getNthDayOfMonth(c,i).getTime()));
		}
		return dates;
	}

	public static List<Timestamp> getHoursForADay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		List<Timestamp> dates = new ArrayList<Timestamp>();
		Date newDate = getStartOfDay(d);
		for (int i=0; i<24; i++) {
			newDate.setHours(i);
			newDate.setMinutes(0);
			dates.add(new Timestamp(newDate.getTime()));
		}
		return dates;
	}

	public static Date getNthDayOfWeek(Calendar c, int weekday) {
		return getDateOfWeekDay(weekday, c.get(Calendar.WEEK_OF_YEAR), c.get(Calendar.YEAR));	
	}

	public static Date getNthDayOfMonth(Calendar c, int dayOfMonth) {
		return getDateOfDayInMonth(dayOfMonth, c.get(Calendar.MONTH), c.get(Calendar.YEAR));	
	}

	public static Date getDateOfWeekDay(int weekDay, int weekId, int year) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.DAY_OF_WEEK, weekDay);
		c.set(Calendar.WEEK_OF_YEAR, weekId);
		return c.getTime();
	}

	public static Date getDateOfDayInMonth(int dayOfMonth, int monthId, int year)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.DAY_OF_MONTH, dayOfMonth); 
		c.set(Calendar.MONTH, monthId);
		return c.getTime();
	}

	public static Time getDateAsTime(Date d) {
		String s = getDateAsString(d, TIME_FORMAT);
		return Time.valueOf(s);
	}

	/**
	 * 
	 * @return the 0th hour of the current day
	 */
	public static Date getStartOfDay(){
		return getStartOfDay(DateUtils.getTimestamp());
	}

	/**
	 * @param date
	 * @return the 0th hour of the given date
	 */
	public static Date getStartOfDay(Date date){
		return new Date(date.getYear(), date.getMonth(), date.getDate(), 0, 0, 0);
	}

	public static Date getEndOfDay(Date d) {
		return new Date(d.getYear(), d.getMonth(), d.getDate(), 23, 59, 59);
	}

	/**
	 * combines a date and time into a date
	 * @param date
	 * @param time
	 * @return Date set with the given time.
	 */
	public static Date combineDateAndTime(Date date, Time time) {
		date.setHours(time.getHours());
		date.setMinutes(time.getMinutes());
		date.setSeconds(time.getSeconds());
		return date;
	}

	public static int getDayOfYear(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DAY_OF_YEAR);
	}

	public static int getDayOfWeek(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	public static int getDayOfMonth() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.DAY_OF_MONTH);
	}

	public static int getDayOfMonth(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	public static int getHourOfDay(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.HOUR_OF_DAY);
	}

	public static int getNumberOfDaysInMonth(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static int getNumberOfWeeksInMonth(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.getActualMaximum(Calendar.WEEK_OF_MONTH);
	}

	public static List<Timestamp> getDatesAsTimestampList(Date d, TimeScale scale) {
		switch(scale) {
		case MONTH:
			return DateUtils.getDatesForAMonth(d);
		case WEEK:
			return DateUtils.getDatesForAWeek(d);
		}
		return new ArrayList<Timestamp>();
	}

	public static Date getYesterdayDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(DateUtils.getTimestamp());
		// will roll back 1 month, accounting for differences in max days in a month
		c.roll(Calendar.DAY_OF_YEAR, false);
		return c.getTime();
	}

	public static Date getLastWeekDate() {
		return addToDate(DateUtils.getTimestamp(), Calendar.DAY_OF_MONTH, -7);
	}

	public static Date getLastMonthDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(DateUtils.getTimestamp());
		// will roll back 1 month, accounting for differences in max days in a month
		c.roll(Calendar.MONTH, false);
		return c.getTime();
	}

	public static Date getNthMonthDate(Date d, int i) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		// skip ahead by i month(s), c.add accounts for differences in max days in a month
		c.add(Calendar.MONTH, i);
		return c.getTime();
	}

	public static Date getEarliestDate() {
		Calendar c = Calendar.getInstance();
		c.set(
				c.getActualMinimum(Calendar.YEAR), 
				c.getActualMinimum(Calendar.MONTH), 
				c.getActualMinimum(Calendar.DAY_OF_MONTH), 
				c.getActualMinimum(Calendar.HOUR), 
				c.getActualMinimum(Calendar.MINUTE), 
				c.getActualMinimum(Calendar.SECOND)
		);

		c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
		return c.getTime();
	}

	public static Date getLatestDate() {
		Calendar c = Calendar.getInstance();
		c.set(
				c.getActualMaximum(Calendar.YEAR), 
				c.getActualMaximum(Calendar.MONTH), 
				c.getActualMaximum(Calendar.DAY_OF_MONTH), 
				c.getActualMaximum(Calendar.HOUR), 
				c.getActualMaximum(Calendar.MINUTE), 
				c.getActualMaximum(Calendar.SECOND)
		);

		c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
		return c.getTime();
	}

	public static int getWeekOfMonth(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.WEEK_OF_MONTH);
	}

	public static int getWeekOfYear(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.WEEK_OF_YEAR);
	}

	public static boolean isSameWeek(Date d, Date d2) {
		int weekOfYear = DateUtils.getWeekOfYear(d);
		int weekOfYear2 = DateUtils.getWeekOfYear(d2);

		if(d.getYear() == d2.getYear() && weekOfYear == weekOfYear2){  // years and weeks are the same
			return true;
		} else if( Math.abs(Integer.valueOf(d.getYear()).compareTo(Integer.valueOf(d2.getYear()))) == 1 // if they are one year apart
				&& d.getMonth() != d2.getMonth() // and not the same month so jan 1, 2010 and jan 1, 2011 will not make true
				&& weekOfYear == 1 && weekOfYear2 == 1) {
			// and they are both the first week ( the last week in december that overlaps is week 1)

			return true;
		} else {
			return false;
		}
	}

	public static Date addToDate(Date d, int field, int i) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(field, i);
		return c.getTime();
	}

	public static Date addHoursToDate(Timestamp timestamp, int nextHours) {
		return new Date(timestamp.getTime() + (nextHours*60*60*1000));
	}

	public static Date getDateOfFollowingSunday(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int days = Calendar.SATURDAY-c.get(Calendar.DAY_OF_WEEK)+1;
		c.set(Calendar.DATE, c.get(Calendar.DATE) + days);
		return c.getTime();
	}

	public static Date getDateOfPreviousSunday(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int daysAhead = c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		c.set(Calendar.DATE, c.get(Calendar.DATE) - daysAhead);
		return c.getTime();
	}

	/**
	 * months are zero-based!
	 * @param d
	 * @return
	 */
	public static int getMonthOfYear(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.MONTH);
	}

	public static int getMonthDiff(Date startDate, Date endDate) {
		Calendar start = Calendar.getInstance(); 
		start.setTime(startDate);

		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		// get the months and add 1 to compensate for zero index
		int startMonth = start.get(Calendar.MONTH) + 1;
		int endMonth = end.get(Calendar.MONTH) + 1;
		int months = -1;



		int startYear = start.get(Calendar.YEAR);
		int endYear = end.get(Calendar.YEAR);

		// if the years are the same, just return the abs of the difference
		if(startYear == endYear){
			return (int) Math.abs(startMonth - endMonth);
		} else {
			months = (int) Math.abs((endYear-startYear) * 12);
		}

		// if startDate is smaller
		if(startDate.compareTo(endDate) < 0){
			if(startYear < endYear){
				if(startMonth < endMonth){
					months += (endMonth - startMonth);
				} else if(startMonth > endMonth) {
					months -= startMonth;
					months += endMonth;
				}
			}
		} else { // startDate is bigger
			if(startYear > endYear) {
				if(startMonth > endMonth){
					months += (startMonth - endMonth);
				} else if (startMonth < endMonth) {
					months -= endMonth;
					months += startMonth;
				}
			}
		}

		/*
		12/2011 - 1/2012 = 1
		if
		1/2011- 2/2012 = 13
		8/2010 - 2/2012 = 18
		 */
		return months;
	}

	/*
	 * determines if testDate is between date1 and date2. this only compares the date part (not the time part)
	 * is inclusive of date1, but not date2
	 */
	public static boolean isDateBetween(Date testDate, Date d1, Date d2) {
		Date d1start = getStartOfDay(d1);
		Date d2start = getStartOfDay(d2);

		return isDateTimeBetween(testDate, d1start, d2start);
	}

	public static boolean isHourBetween(Date testDate, Date d1, Date d2) {
		Date d1hour = getStartOfDay(d1);
		d1hour.setHours(d1.getHours());
		Date d2hour = getStartOfDay(d2);
		d2hour.setHours(d2.getHours());

		return isDateTimeBetween(testDate, d1hour, d2hour);
	}

	private static boolean isDateTimeBetween(Date testDate, Date d1, Date d2) {
		if ( (testDate.after(d1) || testDate.equals(d1)) && (testDate.before(d2) || testDate.equals(d2))) {
			return true;
		}
		return false;
	}

	/*
	 * determines if testDate is on or before baseDate. this only compares the date part (not the time part)
	 */
	public static boolean isDateOnOrAfter(Date testDate, Date baseDate) {
		Date baseDateStart = getStartOfDay(baseDate);
		Date testDateStart = getStartOfDay(testDate);

		if ( (testDateStart.after(baseDateStart)) || (testDateStart.equals(baseDateStart))) {
			return true;
		}
		return false;
	}

	public static int getWeeksSince(Date d) {
		return (int) Math.floor(getDaysPast(d) / 7);
	}

	public static int getNumberOfUnitsSinceToday(Date testDate, TimeScale scale) {
		int daysSince = getDaysPast(testDate);
		switch (scale) {
		case DAY:
			return daysSince;
		case WEEK:
			return (int) Math.floor(daysSince / 7);
		case MONTH:

			Timestamp timestamp = DateUtils.getTimestamp();
			int testMonth = DateUtils.getMonthOfYear(testDate);
			int nowMonth = DateUtils.getMonthOfYear(timestamp);
			int months = (int) Math.abs(nowMonth - testMonth);
			return months;
		}
		return -1;
	}

	public static int getHourOfDay() {
		return getTimestamp().getHours();
	}

	public static String convertStringDateToAnotherStringDate(String s, String originalFormat, String newFormat) {
		Date d = parseString(s, originalFormat);
		if (d != null) {
			return getDateAsString(d, newFormat);
		}
		else {
			return s;
		}
	}


}