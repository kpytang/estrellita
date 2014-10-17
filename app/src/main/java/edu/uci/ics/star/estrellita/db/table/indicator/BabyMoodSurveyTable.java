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

import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class BabyMoodSurveyTable extends SurveyTable {
	
	public static final String BABYMOOD_SURVEY = SurveyType.BABYMOOD.name();

	public BabyMoodSurveyTable() {
		super(BABYMOOD_SURVEY);
		this.setDuration(DateUtils.DAY);
		this.setOptional(true);
	}
	
	public Class<?> getIndicatorClass() {
		return BabyMoodSurvey.class;
	}

}
