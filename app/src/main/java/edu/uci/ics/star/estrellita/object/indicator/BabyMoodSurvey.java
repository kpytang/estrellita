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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Question;
import edu.uci.ics.star.estrellita.object.Question.QuestionType;
import edu.uci.ics.star.estrellita.object.Response;

public class BabyMoodSurvey extends Survey implements Parcelable {
	
	private static final String BABYMOOD_QUESTION = "How fussy is your baby right now?";
	
	private int mFussy = -1;
	private float mAvgFussy = -1;

	public BabyMoodSurvey(CommonData cd, String type, Integer surveyId, Integer questionId, Integer value, int score) {
		super(cd, type, surveyId, score);
		initializeSurvey();
		addFussyReport(value);
		mFussy = value;
	}
	
	public BabyMoodSurvey(CommonData cd, String type, int surveyId, int score) {
		super(cd, type, surveyId, score);
	}
	
	public BabyMoodSurvey(){
		super(SurveyType.BABYMOOD);
		initializeSurvey();
	}
	
	private void initializeSurvey() {
		Question q = new Question(BABYMOOD_QUESTION, QuestionType.SINGLE_CHOICE);
		addQuestion(q);
		
		// we have 1 question in the baby mood survey, so all responses are linked to the 0th key
		getResponses().put(0, new ArrayList<Response>());
	}
	
	// baby moods only has one questions, so we can ignore questionID and just call the addFussyReport method
	// this is called by the SurveyTable, so that's we have it here
	public void addResponse(int questionId, Response response) {
		addFussyReport(response.getData());
	}
	
	public void addFussyReport(int fussyness) {
		List<Response> list = new ArrayList<Response>();
		list.add(new Response(fussyness));
		// baby moods only has one questions, so questionID is 0
		this.addResponses(0, list);
		mFussy = fussyness;
	}
	
	// this will only be used internally, and won't be passed by passed between activities, so we won't put this in parcels
	public void addAverageFussyReport(float fussyness) {
		mAvgFussy = fussyness;
	}
	
	public Float getAverageFussyness() {
		return mAvgFussy;
	}
	
	public Integer getFussyness() {
		return mFussy;
	}
	
	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeInt(mFussy);
	}
	
	// used to regenerate the object
    public static final Parcelable.Creator<BabyMoodSurvey> CREATOR = new Parcelable.Creator<BabyMoodSurvey>() {
        public BabyMoodSurvey createFromParcel(Parcel in) {
            return new BabyMoodSurvey(in);
        }

        public BabyMoodSurvey[] newArray(int size) {
            return new BabyMoodSurvey[size];
        }
    };

    // this constructor takes in a Parcel and gives an object populated with its values
    // look at writeToParcel for the order of things to read in 
    private BabyMoodSurvey(Parcel in) {
    	super(SurveyType.BABYMOOD);
    	initializeSurvey();
    	this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
    	mFussy = in.readInt();
    	addFussyReport(mFussy);
    }
    
    public String toString() {
    	return "DateTime: " + getCommonData().getTimestamp() + ", Fussy: " + mFussy;
    }
    
    public String toChartString() {
    	return "Fussyness: " + mFussy;
    }
    
    public String toAverageChartString() {
    	return "Fussyness: " + new DecimalFormat("#.##").format(mAvgFussy);
    }
    
    public boolean isEmpty() {
    	if (getFussyness() == -1) {
    		return true;
    	}
    	return false;
    }

	@Override
	public String getTileBlurb() {
		return "fussyness\n" + getFussyness().toString() + "/10";
	}
}
