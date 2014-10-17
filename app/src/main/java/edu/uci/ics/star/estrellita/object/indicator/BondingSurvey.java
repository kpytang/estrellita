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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Question;
import edu.uci.ics.star.estrellita.object.Question.QuestionType;
import edu.uci.ics.star.estrellita.object.Response;
import edu.uci.ics.star.estrellita.utils.StringUtils;


public class BondingSurvey extends Survey implements Parcelable {

	public enum BondingActivity {
		READING(0), 
		SINGING(1),
		TALKING(2), 
		TAKE_BABY_WALKING(3), 
		TUMMY_TIME(4),
		NONE(5);

		private int value;
		private static final Map<Integer, BondingActivity> activitiesByValue = new HashMap<Integer, BondingActivity>();
		static {
			for (BondingActivity a : BondingActivity.values()) {
				activitiesByValue.put(a.value, a);
			}
		}

		public static BondingActivity forValue(int value) {
			return activitiesByValue.get(value);
		}

		private BondingActivity(int value) {
			this.value = value;
		}

		public int getActivityID() {
			return value;
		}

		public static int getSize() {
			return activitiesByValue.keySet().size()-1;
		}
	}

	private static final String BONDING_QUESTION = "What activities have you done with your baby today?";

	private boolean mTalking = false;
	private boolean mReading = false;
	private boolean mWalking = false;
	private boolean mTummyTime = false;
	private boolean mSinging = false;
	private boolean mClearActivities = false;

	public BondingSurvey(CommonData cd, String type, Integer surveyId, Integer questionId, Integer response, int score) {
		super(cd, type, surveyId, score);
		initializeSurvey();
		List<BondingActivity> list = new ArrayList<BondingActivity>();
		if ((response != null)) {
			list.add(BondingActivity.forValue(response));
			addActivities(list);
		}
	}

	public BondingSurvey(CommonData cd, String type, int surveyId, int score) {
		super(cd, type, surveyId, score);
	}

	public BondingSurvey(){
		super(SurveyType.BONDING);
		initializeSurvey();
	}

	private void initializeSurvey() {
		Question q = new Question(BONDING_QUESTION, QuestionType.MULTIPLE_CHOICE);
		mQuestions.add(q);

		// we only have 1 question in the bonding survey, so all responses are linked to the 0th key
		getResponses().put(0, new ArrayList<Response>());
	}

	// we only have 1 question in the bonding survey, so all responses are linked to the 0th key
	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.object.indicator.Survey#addResponse(int, edu.uci.ics.star.estrellita.object.Response)
	 */
	public void addResponse(int questionId, Response response) {
		List<Response> responses = getResponseSet(0);
		if (responses == null) {
			responses = new ArrayList<Response>();
		}
		responses.add(response);
		addActivity(BondingActivity.forValue(response.getData()));
		addResponses(0, responses);
	}

	private void addActivity(BondingActivity activity) {
		switch(activity) {
		case TALKING:
			mTalking = true;
			break;
		case READING:
			mReading = true;
			break;
		case TAKE_BABY_WALKING:
			mWalking = true;
			break;
		case TUMMY_TIME:
			mTummyTime = true;
			break;
		case SINGING:
			mSinging = true;
			break;
		case NONE:
			mClearActivities = true;
			break;
		}
	}

	public void addActivities(List<BondingActivity> activities) {
		List<Response> responses = new ArrayList<Response>();
		for (int i=0; i<activities.size(); i++) {
			responses.add(new Response(activities.get(i).value));
			addActivity(activities.get(i));
		}
		addResponses(0, responses);
	}

	public List<BondingActivity> getActivitiesAsList() {
		List<BondingActivity> l = new ArrayList<BondingActivity>();
		if (mTalking) {
			l.add(BondingActivity.TALKING);
		}
		else if (mReading) {
			l.add(BondingActivity.READING);
		}
		else if (mWalking) {
			l.add(BondingActivity.TAKE_BABY_WALKING);
		}
		else if (mTummyTime) {
			l.add(BondingActivity.TUMMY_TIME);
		}
		else if (mSinging) {
			l.add(BondingActivity.SINGING);
		}
		return l;
	}

	public boolean hasCompletedSinging() {
		return mSinging;
	}

	public boolean hasCompletedReading() {
		return mReading;
	}

	public boolean hasCompletedTalking() {
		return mTalking;
	}

	public boolean hasCompletedTummyTime() {
		return mTummyTime;
	}

	public boolean hasCompletedWalking() {
		return mWalking;
	}

	public boolean isClearAll() {
		// technically we only need to check status of mClearActivities flag, but in these cases all the other activities should also be false
		// so we're requiring them to be false here just to be extra sure we should clear everything
		if (!mTalking && !mReading && !mWalking && !mTummyTime && !mSinging && mClearActivities) {
			return true;
		}
		return false;
	}

	public boolean isEmpty() {
		if (!mTalking && !mReading && !mWalking && !mTummyTime && !mSinging) {
			return true;
		}
		return false;
	}

	public void setActivities(boolean talking, boolean reading, boolean walking, boolean tummy, boolean singing) {
		mTalking = talking;
		mReading = reading;
		mWalking = walking;
		mTummyTime = tummy;
		mSinging = singing;
	}

	public static List<String> getBondingActivityNames() {
		BondingActivity[] activities = BondingActivity.values();
		List<String> categories = new ArrayList<String>();
		for (int i=0; i<activities.length-1; i++) {
			String s = activities[i].toString().replaceAll("_"," ");
			s = StringUtils.capitalize(s);
			categories.add(s);
		}
		return categories;
	}

	public static List<BondingActivity> getBondingActivityValues() {
		BondingActivity[] activities = BondingActivity.values();
		return Arrays.asList(activities).subList(0, activities.length-1);
	}

	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}

	// writes out: 
	// wet, dirty, both counts
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeInt((mReading?1:0));
		out.writeInt((mSinging?1:0));
		out.writeInt((mTalking?1:0));
		out.writeInt((mWalking?1:0));
		out.writeInt((mTummyTime?1:0));
		out.writeInt((mClearActivities?1:0));
	}

	// used to regenerate the object
	public static final Parcelable.Creator<BondingSurvey> CREATOR = new Parcelable.Creator<BondingSurvey>() {
		public BondingSurvey createFromParcel(Parcel in) {
			return new BondingSurvey(in);
		}

		public BondingSurvey[] newArray(int size) {
			return new BondingSurvey[size];
		}
	};

	// this constructor takes in a Parcel and gives an object populated with its values
	// look at writeToParcel for the order of things to read in 
	private BondingSurvey(Parcel in) {
		super(SurveyType.BONDING);
		initializeSurvey();
		this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
		List<BondingActivity> activities = new ArrayList<BondingActivity>();

		if (in.readInt() != 0) {
			mReading = true;
			activities.add(BondingActivity.READING);
		}
		if (in.readInt() != 0) {
			mSinging = true;
			activities.add(BondingActivity.SINGING);
		}
		if (in.readInt() != 0) {
			mTalking = true;
			activities.add(BondingActivity.TALKING);
		}
		if (in.readInt() != 0) {
			mWalking = true;
			activities.add(BondingActivity.TAKE_BABY_WALKING);
		}
		if (in.readInt() != 0) {
			mTummyTime = true;
			activities.add(BondingActivity.TUMMY_TIME);
		}
		if (in.readInt() != 0) {
			mClearActivities = true;
			activities.add(BondingActivity.NONE);
		}

		addActivities(activities);
	}

	@Override
	public String getTileBlurb() {
		String temp="", s = "";
		if (mClearActivities) {
			return "no\nactivities";
		}
		else {
			if (mReading) {
				temp = StringUtils.addToCommaDelimitedList("read", temp);
			}
			if (mSinging) {
				temp = StringUtils.addToCommaDelimitedList("sing", temp);
			}
			// if there's no newline, it means there's no singing so remove last comma from read and add newline
			if (temp.length()>0) {
				s += temp + "\n";
			}
			temp = "";
			if (mTalking) {
				temp = StringUtils.addToCommaDelimitedList("talk", temp);
			}
			if (mWalking) {
				temp = StringUtils.addToCommaDelimitedList("walk", temp);
			}
			// if there's no newline, it means there's no walking so remove last comma from read and add newline
			if (temp.length()>0) {
				s += temp + "\n";
			}
			if (mTummyTime) {
				s += "tummy";	
			}
			s = s.trim();
			return s;
		}
	}

	public String toString() {
		String s = "";
		if (mReading) {
			s = StringUtils.addToCommaDelimitedList("reading", s);
		}
		if (mSinging) {
			s = StringUtils.addToCommaDelimitedList("singing", s);
		}
		if (mTalking) {
			s = StringUtils.addToCommaDelimitedList("talking", s);
		}
		if (mWalking) {
			s = StringUtils.addToCommaDelimitedList("walking", s);		
		}
		if (mTummyTime) {
			s = StringUtils.addToCommaDelimitedList("tummy time", s);		
		}
		s = s.trim();
		return s;
	}
	
	// each indicator only has at most 1 flag set, so we should flatten into one bonding object
	public static BondingSurvey flattenBondingArray(BondingSurvey[] bondings) {
		if ((bondings == null) || (bondings.length == 0)) {
			return null;
		}
		BondingSurvey b = bondings[0];
		List<BondingActivity> activities = new ArrayList<BondingActivity>();
		// step through each bonding survey
		for (int i=0; i<bondings.length; i++) {
			// if there is a response, then let's add that activity
			// but only add a response for a bonding survey that shares the same date
			if ( (bondings[i].getActivitiesAsList().size()>0) && (bondings[i].getDateTime().equals(b.getDateTime())) ) {
				BondingActivity activity = bondings[i].getActivitiesAsList().get(0);
				activities.add(activity);
			}
		}
		if (activities.size() > 0) {
			b.addActivities(activities);
		}
		return b;
	}
}
