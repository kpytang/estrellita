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
import java.util.List;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Question;
import edu.uci.ics.star.estrellita.object.Question.QuestionType;
import edu.uci.ics.star.estrellita.object.Response;

public class MoodMapSurvey extends Survey implements Parcelable {
	private static final String MYMOOD_QUESTION_0 = "How positive are you feeling right now?";
	private static final String MYMOOD_QUESTION_1 = "How energetic are you feeling right now?";
	
	private Integer mX,mY;

	public MoodMapSurvey(CommonData cd, String type, Integer surveyId, Integer questionId, Integer x, Integer y, int score) {
		super(cd, type, surveyId, score);
		initializeSurvey();
		addMoodReport(x,y);
		mX = x;
		mY = y;
	}
	
	public MoodMapSurvey(CommonData cd, String type, int surveyId, int score) {
		super(cd, type, surveyId, score);
		initializeSurvey();
	}
	
	public MoodMapSurvey() {
		super(SurveyType.MOODMAP);
		initializeSurvey();
	}
	
	private void initializeSurvey() {
		Question q = new Question(MYMOOD_QUESTION_0, QuestionType.SINGLE_CHOICE);
		addQuestion(q);
		q = new Question(MYMOOD_QUESTION_1, QuestionType.SINGLE_CHOICE);
		addQuestion(q);
		
		// we have 2 questions in the  mood survey; first question is for x-axis, second question is for y-axis
		getResponses().put(0, new ArrayList<Response>());
		getResponses().put(1, new ArrayList<Response>());
	}
	
	public void addResponse(int questionId, Response response) {
		getResponses().get(questionId).add(response);
	}
	
	public void addMoodReport(int x, int y) {
		// first question is x-axis
		List<Response> list = new ArrayList<Response>();
		list.add(new Response(x));
		this.addResponses(0, list);
		
		// second question is y-axis
		list = new ArrayList<Response>();
		list.add(new Response(y));
		this.addResponses(1, list);
		mX = x;
		mY = y;
	}
	
	public PointF getMoodReport() {
		if ((mX==null) || (mY==null)) {
			return null;
		}
		return new PointF(mX, mY);
	}

	@Override
	public String getTileBlurb() {
		return "";
	}
	
	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}
	
	// writes out: 
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeInt(mX);
		out.writeInt(mY);
	}
	
	// used to regenerate the object
    public static final Parcelable.Creator<MoodMapSurvey> CREATOR = new Parcelable.Creator<MoodMapSurvey>() {
        public MoodMapSurvey createFromParcel(Parcel in) {
            return new MoodMapSurvey(in);
        }

        public MoodMapSurvey[] newArray(int size) {
            return new MoodMapSurvey[size];
        }
    };

    // this constructor takes in a Parcel and gives an object populated with its values
    // look at writeToParcel for the order of things to read in 
    private MoodMapSurvey(Parcel in) {
    	super(SurveyType.MOODMAP);
    	initializeSurvey();
    	this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
    	mX = in.readInt();
    	mY = in.readInt();
    	addMoodReport(mX, mY);
    }
    
    public String toString() {
    	return "X: " + mX + ",Y: " + mY;
    }

}
