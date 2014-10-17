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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.object.AnswerChoice.SpecialCase;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Flag;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.Question;
import edu.uci.ics.star.estrellita.object.Response;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public abstract class Survey extends Indicator{

	public enum SurveyType {
		BONDING,
		BABYMOOD,
		MOODMAP, 
		EPDS, 
		STRESS, 
		PRE_APPT,
		POST_APPT
	};

	private Map<Integer, List<Response>> mResponses;
	protected List<Question> mQuestions;

	private Integer mSurveyId;
	private Timestamp mStartTime;
	private Timestamp mFinishTime;
	private SurveyType mType;
	private int mScore = 0;
	private String mPrompt;
	private int mScoreThreshold = -1;
	private boolean mShouldAlertStJoseph = false, mShouldRescheduleAppointment = false;

	public Survey(CommonData cd, String surveyType, Integer surveyId, Integer score) {
		super(cd);
		mType = SurveyType.valueOf(surveyType);
		mSurveyId = surveyId;
		mScore = score;
		mQuestions = new ArrayList<Question>();
		mResponses = new HashMap<Integer, List<Response>>();
	}

	/**
	 * only called when not using login information (e.g. for testing)
	 * @param type	
	 */
	public Survey(SurveyType type) {
		super();
		mType = type;
		mQuestions = new ArrayList<Question>();
		mResponses = new HashMap<Integer, List<Response>>();
	}

	// when adding a question to the survey, we will automatically give it an id (which corresponds to its index + 1 in the survey)
	public Integer addQuestion(Question question) {
		question.setId(mQuestions.size());
		mQuestions.add(question);
		return question.getId();
	}

	public void addQuestions(List<Question> questions) {
		for(int i=0; i<questions.size(); i++) {
			addQuestion(questions.get(i));
		}
	}

	public Timestamp getFinishTime() {
		return mFinishTime;
	}

	public List<Question> getQuestions() {
		return mQuestions;
	}

	public Question getQuestion(int id) {
		return mQuestions.get(id);
	}

	public List<Response> getResponseSet(int questionID) {
		if (mResponses.containsKey(questionID)) {
			return mResponses.get(questionID);
		}
		else {
			return new ArrayList<Response>();
		}

	}

	public int getSurveyId() {
		return mSurveyId;
	}

	public void setSurveyId(int id) {
		mSurveyId = id;
	}

	public int getScore() {
		return mScore;
	}

	public Timestamp getStartTime() {
		return mStartTime;
	}

	public void setSurveyType(SurveyType t) {
		this.mType = t;
	}

	public SurveyType getSurveyType() {
		return mType;
	}

	public void setFinishTime(Timestamp t) {
		mFinishTime = t;
	}

	public void setScore(int score) {
		this.mScore = score;
	}

	public void setStartTime(Timestamp t) {
		mStartTime = t;
	}

	public void setPrompt(String mPrompt) {
		this.mPrompt = mPrompt;
	}

	public String getPrompt() {
		return mPrompt;
	}

	public void addResponses(int questionId, List<Response> responses) {
		mResponses.put(questionId, responses);
	}

	public abstract void addResponse(int questionId, Response response);

	public void removeAllResponses(int index) {
		mResponses.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Survey type: " + mType.name()  + " TIME: " + this.getCommonData().getTimestamp().getTime()+ "\nRESPONSES:");
		for(int qid: mResponses.keySet()){

			sb.append("Qid["+qid+"]:");
			for(Response r: mResponses.get(qid)){
				sb.append(r.toString());
				sb.append(",");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void score() {
		mScore = 0;
		for (int i = 0; i < mQuestions.size(); i++) { // for each question
			Question question = mQuestions.get(i); // grab the question

			List<Response> list = mResponses.get(i); // and corresponding list of responses
			int answerIndex;
			if(list != null) {
				for(Response r: list){// for each of the responses for the question
					// as long as this is a data question (which means that there is no text)
					if ( (r.getText() == null) || (r.getText().length() == 0) ) {
						answerIndex = r.getData(); //grab which index it is

						// flag part
						// check to see if that question has a special case. 
						if(question.getAnswer(answerIndex).getSpecialCase().equals(SpecialCase.ALERT)) {
							switch (mType) {
							case EPDS:
								setShouldAlertStJoseph(true); // in this case we want to alert st joseph
								r.getCommonData().setFlag(Flag.urgent); // and mark this as urgent (both response and survey)
								this.getCommonData().setFlag(Flag.urgent); 
								break;
							case PRE_APPT:
							case POST_APPT:
								setShouldRescheduleAppointment(true);
								r.getCommonData().setFlag(Flag.warning); // and mark this as urgent (both response and survey)
								this.getCommonData().setFlag(Flag.warning); 
								break;
							}
						}

						// scoring part
						if(question.isScoringReversed()){ // if the scoring is reversed for that question
							mScore += question.getAnswers().size() - 1 - answerIndex; // max answers - 1 for zero index then minus the index of the selected answer
						} else {
							mScore += answerIndex; // otherwise just use the selected index
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.object.Indicator#getTileBlurb()
	 */
	@Override
	public String getTileBlurb() {
		return DateUtils.formatReadableDate(getCommonData().getTimestamp()) + "\n" + mScore ;
	}

	public static int[] getSurveyResources(SurveyType type) {
		switch(type) {
		case PRE_APPT:
			return new int[]{R.raw.pre_appt};
		case POST_APPT:
			return new int[]{R.raw.post_appt};
		case STRESS:
		case EPDS:
		default:
			return new int[]{};
		}
	}

	public static SurveyType getSurveyType(int resourceId) {
		switch (resourceId) {
		case R.raw.pre_appt:
			return SurveyType.PRE_APPT;
		case R.raw.post_appt:
			return SurveyType.POST_APPT;
		}
		return SurveyType.STRESS;
	}

	public Map<Integer, List<Response>> getResponses() {
		return mResponses;
	}

	// this removes responses for questions that are "stale" 
	// aka the user backed up and created a new path so "stale" questions are from the old path, so we should delete responses attached to those old questions
	public void removeStaleQuestions() {
		List<Integer> questionIds = new ArrayList<Integer>();
		for (int i=0; i<mQuestions.size(); i++) {
			questionIds.add(mQuestions.get(i).getId());
		}
		List<Integer> temp = CollectionUtils.getSetAsSortedList(mResponses.keySet());
		// step through all the questions that have responses
		for(int i=0; i<temp.size(); i++) {
			// remove the questions & responses for questions that were not stored in the end
			if (!questionIds.contains(temp.get(i))) {
				mResponses.remove(temp.get(i));
			}
		}
	}

	public void setShouldAlertStJoseph(boolean mShouldAlert) {
		mShouldAlertStJoseph = mShouldAlert;
	}
	
	public void setShouldRescheduleAppointment(boolean mShouldAlert) {
		mShouldRescheduleAppointment = mShouldAlert;
	}

	public boolean isShouldAlertStJoseph() {
		return mShouldAlertStJoseph;
	}
	
	public boolean isShouldRescheduleAppointment() {
		return mShouldRescheduleAppointment;
	}

	public void setScoreThreshold(int threshold) {
		mScoreThreshold = threshold;
	}

	public int getScoreThreshold() {
		return mScoreThreshold;
	}
}
