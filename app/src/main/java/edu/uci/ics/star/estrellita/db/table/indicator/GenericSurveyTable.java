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

import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.GenericSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;

public class GenericSurveyTable extends SurveyTable {
	
	public GenericSurveyTable(SurveyType type) {
		super(type.name());
		
		setOptional(true);
	}
	
	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.indicator.SurveyTable#getLastBabyIndicator(int, java.lang.String)
	 */
	@Override
	public GenericSurvey getLastBabyIndicator(int userid, String otherComparer){
		SurveyType type = SurveyType.valueOf(mSurveyType.toUpperCase());
		switch (type) {
		case MOODMAP:
		case EPDS:
		case STRESS:
			return (GenericSurvey) getLastParentIndicator(userid, otherComparer); 
		}
		
		if (otherComparer == null) {
			otherComparer = "";
		} else {
			otherComparer = " AND " + otherComparer;
		}
		Indicator indicator = null;
		try{
			indicator = getSetOfIndicators(WHERE + BABY_ID + "==" + userid + otherComparer, -1, -1, CREATED_AT + " DESC")[0];
		} catch(ArrayIndexOutOfBoundsException aioobe){
			// ignore for now
		}
		return (GenericSurvey) indicator;
	}
	
	public Class<?> getIndicatorClass() {
		return GenericSurvey.class;
	}
}
