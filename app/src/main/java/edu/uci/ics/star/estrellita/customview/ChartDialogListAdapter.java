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

package edu.uci.ics.star.estrellita.customview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;

public class ChartDialogListAdapter extends TwoLineListAdapter<String> {
	
	public ChartDialogListAdapter(Context context,List<String> firstRows, List<String> secondRows, List<Integer> tags) {
		super(context, R.layout.two_line_list_item, CollectionUtils.convertIntegerListToStrings(tags));
		mFirstRows = firstRows;
		List<String> dateStrings = new ArrayList<String>();
		for (int i=0; i<secondRows.size(); i++) {
			dateStrings.add(secondRows.get(i));
		}
		mSecondRows = dateStrings;
	}

}
