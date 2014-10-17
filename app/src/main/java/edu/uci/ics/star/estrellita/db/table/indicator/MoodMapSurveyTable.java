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

package edu.uci.ics.star.estrellita.db.table.indicator;

import android.content.Context;
import edu.uci.ics.star.estrellita.object.indicator.MoodMapSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class MoodMapSurveyTable extends SurveyTable {
	
	public static final String MOODMAP_SURVEY = SurveyType.MOODMAP.name();

	public MoodMapSurveyTable() {
		super(MOODMAP_SURVEY);
		
		this.setDuration(DateUtils.DAY);
		this.setParentLevel(true);
		setOptional(false);
	}
	
	/**
	 * 
	 * @param context 
	 * @param IndicatorTable
	 */
	public boolean isNeglected(int kidId, Context context) {
		int daysPast = getDaysPastParent(kidId);

		// timePast was not null, and it is less than the target duration.
		// examples: timePast = 5, 
		if (isOptional() || (getDuration() == Utilities.NO_LIMIT || daysPast >= 0 && daysPast < getDuration())) {
			return false;
		} else {
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.indicator.SurveyTable#getLastBabyIndicator(int, java.lang.String)
	 */
	@Override
	public MoodMapSurvey getLastBabyIndicator(int userid, String otherComparer){
		return (MoodMapSurvey) getLastParentIndicator(userid, otherComparer);
	}
	
	
	public Class<?> getIndicatorClass() {
		return MoodMapSurvey.class;
	}
}
