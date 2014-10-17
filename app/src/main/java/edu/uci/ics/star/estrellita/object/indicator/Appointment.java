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

package edu.uci.ics.star.estrellita.object.indicator;

import java.sql.Time;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class Appointment extends Indicator implements Parcelable, Comparable<Indicator> {
	public enum DoctorType { CARDIAC, EDAC, ENDOCRINOLOGY, GI, HEMATOLOGY, METABOLIC, NEUROLOGY, NEUROSURGERY, OPTHALMOLOGY, PEDIATRICIAN, PULMONARY, OTHER };
	public enum AttendedState { MISSED, ATTENDED, UNKNOWN };
	
	private Date mDate;
	private Time mStartTime, mEndTime;
	private String mDoctorName, mLocation, mPhone;
	private DoctorType mDoctorType;
	private AttendedState mAttended;
	private boolean mConcernedAboutDiapers, mConcernedAboutBabyMoods, mConcernedAboutCharts, mConcernedAboutWeight;

	public Appointment() {
		super();
		mLocation = "";
		mPhone = "";
		mAttended = AttendedState.UNKNOWN;
		mConcernedAboutDiapers = false;
		mConcernedAboutBabyMoods = false;
		mConcernedAboutCharts = false;
		mConcernedAboutWeight = false;
	}

	// called by getIndicatorArray in AppointmentTable
	public Appointment(CommonData common, 
			String drName, String drType, Date date, Time start, Time end, String location, String phone, int attended, 
			 int diaperFlag, int babymoodFlag, int noteFlag, int weightFlag) {
		super(common);
		
		mDoctorName = drName;
		setDoctorType(drType);
		mDate = date;
		mStartTime = start;
		mEndTime = end;
		mLocation = location;
		mPhone = phone;
		switch (attended) {
		case 0:
			mAttended = AttendedState.MISSED;
			break;
		case 1:
			mAttended = AttendedState.ATTENDED;
			break;
		default:
			mAttended = AttendedState.UNKNOWN;	
		}
		setConcerns((diaperFlag !=0), (babymoodFlag !=0), (noteFlag !=0), (weightFlag !=0));
	}
	
	public boolean isNoConcerns() {
		if (!mConcernedAboutDiapers && !mConcernedAboutBabyMoods && !mConcernedAboutCharts && !mConcernedAboutWeight) {
			return true;
		}
		return false;
	}
	
	public boolean[] getAllConcerns() {
		boolean[] concerns = new boolean[4];
		for (int i=0; i<concerns.length; i++) {
			switch(i) {
				case 0:
					concerns[i] = mConcernedAboutDiapers;
					break;
				case 1:
					concerns[i] = mConcernedAboutBabyMoods;
					break;
				case 2:
					concerns[i] = mConcernedAboutCharts;
					break;
				case 3:
					concerns[i] = mConcernedAboutWeight;
					break;
			}
		}
		return concerns;
	}
	
	public Date getDate() {
		return mDate;
	}

	public Time getEndTime() {
		return mEndTime;
	}

	public String getLocation() {
		if (mLocation == null)  {
			return "";
		}
		return mLocation;
	}

	public String getDoctorName() {
		return mDoctorName;
	}

	public String getPhone() {
		if (mPhone == null) {
			return "";
		}
		return mPhone;
	}

	public Time getStartTime() {
		return mStartTime;
	}

	public DoctorType getDoctorType() {
		return mDoctorType;
	}
	
	public String getDoctorTypeAsString() {
		if (mDoctorType == DoctorType.GI) {
			return "Gastroenterology (GI)";
		}
		else if (mDoctorType == DoctorType.EDAC) {
			return "EDAC";
		}
		else {
			return StringUtils.capitalize(mDoctorType.name().toLowerCase());
		}
	}

	public AttendedState getAttendedState() {
		return mAttended;
	}

	public boolean isConcernedAboutBabyMoods() {
		return mConcernedAboutBabyMoods;
	}

	public boolean isConcernedAboutDiapers() {
		return mConcernedAboutDiapers;
	}
	
	public boolean isConcernedAboutCharts() {
		return mConcernedAboutCharts;
	}

	public boolean isConcernedAboutWeight() {
		return mConcernedAboutWeight;
	}
	
	public void setAllConcerns(boolean[] concerns) {
		setConcerns(concerns[0], concerns[1], concerns[2], concerns[3]);
	}
	
	public void clearAllConcerns() {
		mConcernedAboutBabyMoods = false;
		mConcernedAboutDiapers = false;
		mConcernedAboutCharts = false;
		mConcernedAboutWeight = false;
	}

	public void setAttended(AttendedState attended) {
		this.mAttended = attended;
	}

	public void setConcernedAboutBabyMoods(boolean concerned) {
		mConcernedAboutBabyMoods = concerned;
	}

	public void setConcernedAboutDiapers(boolean concerned) {
		mConcernedAboutDiapers = concerned;
	}
	
	public void setConcernedAboutCharts(boolean concerned) {
		mConcernedAboutCharts = concerned;
	}

	public void setConcernedAboutWeight(boolean concerned) {
		mConcernedAboutWeight = concerned;
	}

	private void setConcerns(boolean diapers, boolean babymoods, boolean mynotes, boolean weight) {
		mConcernedAboutDiapers = diapers;
		mConcernedAboutBabyMoods = babymoods;
		mConcernedAboutCharts = mynotes;
		mConcernedAboutWeight = weight;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public void setEndTime(Time time) {
		mEndTime = time;
	}

	public void setLocation(String location) {
		mLocation = location;
	}

	public void setStartTime(Time time) {
		mStartTime = time;
	}

	public void setDoctorType(DoctorType type) {
		mDoctorType = type;
	}
	
	public void setDoctorName(String name) {
		mDoctorName = name;
	}

	public String getDoctorInfo() {
		String type = StringUtils.capitalize(mDoctorType.toString().toLowerCase());
		if (mDoctorType == DoctorType.GI) {
			type = "GI";
		}
		else if (mDoctorType == DoctorType.EDAC) {
			type = "EDAC";
		}
		return (StringUtils.capitalize(mDoctorName) + " (" + type + ")");
	}
	
	public String getTimeRange() {
		if (mEndTime == null) {
			return DateUtils.getDateAsString(mStartTime, DateUtils.APPOINTMENT_TIME_FORMAT);
		}
		return (DateUtils.getDateAsString(mStartTime, DateUtils.APPOINTMENT_TIME_FORMAT) + " - " +
				DateUtils.getDateAsString(mEndTime, DateUtils.APPOINTMENT_TIME_FORMAT));
	}
	
	public void setDoctorType(String type) {
		try {
			if (type != null) {
				type = type.toUpperCase();
				if (type.equalsIgnoreCase("Gastroenterology (GI)")) {
					mDoctorType = DoctorType.GI;
				}
				else {
					mDoctorType = DoctorType.valueOf(type);
				}
			}
			else {
				mDoctorType = DoctorType.OTHER;
			}
		}
		catch (Exception e) {
			mDoctorType = DoctorType.OTHER;
		}
	}

	public void setPhone(String phone) {
		mPhone = phone;
	}
	
	public String shortenDrType() {
		String type = mDoctorType.name();
		switch(mDoctorType) {
		case PEDIATRICIAN:
			type = "pediatric";
			break;
		case ENDOCRINOLOGY:
			type = "endocrin.";
			break;
		case NEUROSURGERY:
			type = "neurosurg.";
			break;
		case OPTHALMOLOGY:
			type = "opthalmol.";
			break;
		}
		if (mDoctorType == DoctorType.GI) {
			return "GI";
		}
		else {
			return StringUtils.capitalize(type);
		}
	}
	
	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}
	
	// writes out: 
	// doctor name, doctor type, date, start time, location, phone, attended, notes, concerned about (diapers, baby moods, notes, weight)
	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeString(mDoctorName);
		out.writeString(mDoctorType.toString());
		out.writeLong(mDate.getTime());
		out.writeLong(mStartTime.getTime());
		out.writeString(mLocation);
		out.writeString(mPhone);
		out.writeInt(mAttended.ordinal());
		out.writeBooleanArray(getAllConcerns());
	}
	
	// used to regenerate the object
    public static final Parcelable.Creator<Appointment> CREATOR = new Parcelable.Creator<Appointment>() {
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    // this constructor takes in a Parcel and gives an object populated with its values
    // look at writeToParcel for the order of things to read in 
    private Appointment(Parcel in) {
    	super();
    	this.setCommonData((CommonData)in.readParcelable(CommonData.class.getClassLoader()));
    	mDoctorName = in.readString();
    	setDoctorType(in.readString());
    	mDate = new Date(in.readLong());
    	mStartTime = new Time(in.readLong());
    	mLocation = in.readString();
    	mPhone = in.readString();
		switch (in.readInt()) {
		case 0:
			mAttended = AttendedState.MISSED;
			break;
		case 1:
			mAttended = AttendedState.ATTENDED;
			break;
		default:
			mAttended = AttendedState.UNKNOWN;	
		}
    	boolean[] concerns = new boolean[4];
    	in.readBooleanArray(concerns);
    	setAllConcerns(concerns);
    }

    public String toString() {
    	return "Appt Date/Time: " + DateUtils.combineDateAndTime(mDate, mStartTime) + ", " + " Dr Name: " + mDoctorName + ", Specialty: " + mDoctorType;
    }

	@Override
	public String getTileBlurb() {
		return shortenDrType() + "\n" + DateUtils.getDateAsString(mStartTime, DateUtils.TILE_HOUR_FORMAT);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Indicator anotherIndicator) {
		if (anotherIndicator instanceof Appointment) {
			Appointment anotherAppt = (Appointment) anotherIndicator;
			return DateUtils.combineDateAndTime(getDate(), getStartTime()).compareTo(
					DateUtils.combineDateAndTime(anotherAppt.getDate(), anotherAppt.getStartTime()));
		}
		return getCommonData().getTimestamp().compareTo(anotherIndicator.getCommonData().getTimestamp());
	}
}
