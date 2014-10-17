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

import java.util.ArrayList;
import java.util.List;

public class Question {
	public enum QuestionType {
		EOS,
		SINGLE_CHOICE,
		MULTIPLE_CHOICE,
		FREEFORM_TEXT
	};

	private int mId;
	private String mPrompt;
	private QuestionType mType;
	private List<AnswerChoice> mAnswers;
	private List<Condition> mConditions;
	private boolean mScoringReversed;
	private boolean mValidated;

	public Question(String s) {
		mValidated = false;
		mPrompt = s;
		mAnswers = new ArrayList<AnswerChoice>();
		mConditions = new ArrayList<Condition>();
	}

	public Question(String s, QuestionType type) {
		this(s);
		mType = type;
	}
	
	public Integer getId() {
		return mId;
	}

	public String getPrompt() {
		return mPrompt;
	}

	public QuestionType getQuestionType() {
		return mType;
	}
	
	public boolean getValidatedState() {
		return mValidated;
	}
	
	public void setPrompt(String s) {
		mPrompt = s;
	}

	public void setQuestionType(QuestionType type) {
		mType = type;
	}

	public void setId(int id) {
		mId = id;
	}

	public void setScoringReversed(boolean b) {
		this.mScoringReversed = b;
	}

	public boolean isScoringReversed() {
		return mScoringReversed;
	}

	public void setAnswers(List<AnswerChoice> answers) {
		this.mAnswers = answers;
	}

	public List<AnswerChoice> getAnswers() {
		return mAnswers;
	}

	public void addAnswer(AnswerChoice answer) {
		this.mAnswers.add(answer);
	}

	public void addAnswers(List<AnswerChoice> answers) {
		mAnswers.addAll(answers);
	}

	public AnswerChoice getAnswer(int i) {
		return mAnswers.get(i);
	}

	public void addConditions(List<Condition> conditions) {
		if (mConditions == null) {
			mConditions = new ArrayList<Condition>();
		}
		mConditions.addAll(conditions);
	}

	// determines whether the specified responses to a specified previous question matches 
	// the conditional question & answer attached to this question
	// assumes that conditions listed first are higher priority
	// returns: the question id to go to next; it's set to -1 if none of the conditions have been met
	public int checkConditions(List<Response> responses) {
		for(int i=0; i<mConditions.size(); i++) {
			Condition condition = mConditions.get(i);
			if (condition.containsResponse(responses)) {
				return condition.getGoToQuestionId();
			}
		}
		return -1;
	}

	public boolean isConditionalQuestion() {
		if ((mConditions != null) && (mConditions.size()>0)) {
			return true;
		}
		return false;
	}

}