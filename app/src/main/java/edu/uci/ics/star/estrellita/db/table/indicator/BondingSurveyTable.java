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

import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class BondingSurveyTable extends SurveyTable {

	public static final String BONDING_SURVEY = SurveyType.BONDING.name();

	public BondingSurveyTable() {
		super(BONDING_SURVEY);
		this.setDuration(DateUtils.DAY);
		setGroupByDate(true);
		setOptional(false);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.db.table.indicator.SurveyTable#getBabySurveysForToday(int)
	 */
	@Override
	public BondingSurvey getLastBabyIndicator(int idBaby, String otherComparer) {
		BondingSurvey[] indicators;
		try{
			String whereClause = WHERE + BABY_ID + "==" + idBaby;
			if (otherComparer != null) {
				whereClause += otherComparer;
			}
			indicators = (BondingSurvey[]) getSetOfIndicators(whereClause, 0, 1, CREATED_AT + " DESC");
			if (indicators != null) {
				return BondingSurvey.flattenBondingArray(indicators);
			}
		} catch(Exception e){
			e.printStackTrace();
		} 
		return null;
	}
	
	public BondingSurvey getLastBabyIndicatorForToday(int idBaby) {
		BondingSurvey[] indicators;
		try{
			indicators = (BondingSurvey[]) getSetOfIndicators(WHERE + BABY_ID + "==" + idBaby + " AND " + CREATED_AT + " > " + DateUtils.getStartOfDay().getTime(), 0, 1, CREATED_AT + " DESC");
			if (indicators != null) {
				return BondingSurvey.flattenBondingArray(indicators);
			}
		} catch(Exception e){
			e.printStackTrace();
		} 
		return null;
	}

	

	public Class<?> getIndicatorClass() {
		return BondingSurvey.class;
	}
}
