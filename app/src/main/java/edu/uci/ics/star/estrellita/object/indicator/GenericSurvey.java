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

import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Response;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class GenericSurvey extends Survey{
	
	private boolean mStartedSurvey;
	private int mCurrentSurveyIndex, mCurrentQuestionListIndex;
	private List<Integer> mQuestionIndices;

	public GenericSurvey(CommonData cd, String type, Integer surveyId, Integer questionId, Integer value, String text, int score) {
		super(cd, type, surveyId, score);
		initializeSurvey();
		addSurveyAnswer(questionId, value, text);
	}

	public GenericSurvey(CommonData cd, String type, int surveyId, int score) {
		super(cd, type, surveyId, score);
	}

	public GenericSurvey(SurveyType type){
		super(type);
		initializeSurvey();
	}

	private void initializeSurvey() {
		mStartedSurvey = false;
		mCurrentSurveyIndex = 0;
		mCurrentQuestionListIndex = 0;
		mQuestionIndices = new ArrayList<Integer>();
	}

	public void addSurveyAnswer(int questionId, Integer answerId, String text) {
		List<Response> list = getResponseSet(questionId);
		if (list == null) {
			list = new ArrayList<Response>();
		}
		Response response = new Response();
		// as long as the answer id is not -1
		if (answerId != null) {
			response.setData(answerId);
		}
		// it's possible to have BOTH an answer indices (integer) AND text (string)
		if (text != null) {
			response.setText(text);
		}
		list.add(response);
		addResponses(questionId, list);
	}
	
	public void addResponse(int questionId, Response response) {
		addSurveyAnswer(questionId, response.getData(), response.getText());
	}
	
	@Override
	public String getTileBlurb() {
		return DateUtils.formatReadableDate(getCommonData().getTimestamp());
	}
	
	public void saveState(boolean started, int surveyIndex, int questionListIndex, List<Integer> questionIndices) {
		mStartedSurvey = started;
		mCurrentSurveyIndex = surveyIndex;
		mCurrentQuestionListIndex = questionListIndex;
		mQuestionIndices = new ArrayList<Integer>(questionIndices);
	}
	
	public boolean isSurveyStarted() {
		return mStartedSurvey;
	}
	
	public int getSurveyIndex() {
		return mCurrentSurveyIndex;
	}
	
	public int getQuestionListIndex() {
		return mCurrentQuestionListIndex;
	}
	
	public List<Integer> getQuestionIndices() {
		return mQuestionIndices;
	}
}
