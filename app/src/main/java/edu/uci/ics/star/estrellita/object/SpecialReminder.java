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

import android.content.Context;
import android.content.Intent;

public abstract class SpecialReminder {
	public enum ReminderType { APPOINTMENT, PRE_APPT_SURVEY, POST_APPT_SURVEY, CALL_DOCTOR, CALL_ST_JOSEPH, BONDING };

	private List<Indicator> mIndicators;
	private List<Intent> mIntents;
	private List<String> mTexts;
	private List<String> mTitles;
	
	protected ReminderType mType;
	
	public SpecialReminder(ReminderType type) {
		mIndicators = new ArrayList<Indicator>();
		mIntents = new ArrayList<Intent>();
		mTitles = new ArrayList<String>();
		mTexts = new ArrayList<String>();
		
		mType = type;
	}

	public List<Intent> getIntents() {
		return mIntents;
	}

	public void addIntent(Intent intent){
		mIntents.add(intent);
	}

	public boolean meetsCondition(Context context, Integer childId) {
		return evaluate(context, childId);
	}

	public List<String> getTitles() {
		return mTitles;
	}

	public void addTitle(String title) {
		mTitles.add(title);
	}

	public void setType(ReminderType type) {
		mType = type;
	}

	public ReminderType getType() {
		return mType;
	}

	public List<String> getTexts() {
		return mTexts;
	}

	public void addText(String text) {
		mTexts.add(text);
	}

	public List<Indicator> getIndicators() {
		return mIndicators;
	}

	public void addIndicator(Indicator indicator) {
		mIndicators.add(indicator);
	}

	public abstract boolean evaluate(Context context, Integer childId);
}
