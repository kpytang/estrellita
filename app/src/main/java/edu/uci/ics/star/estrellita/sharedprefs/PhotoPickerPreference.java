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

package edu.uci.ics.star.estrellita.sharedprefs;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import edu.uci.ics.star.estrellita.EstrellitaTiles;

public class PhotoPickerPreference extends Preference {
	public static final String BABY_ONE = "baby_one_photo";
	public static final String BABY_TWO = "baby_two_photo";
	private static final String PARENT = "parent_photo";

	public PhotoPickerPreference(Context context) {
		this(context, null);
	}

	public PhotoPickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/*
	 * (non-Javadoc)
	 * @see android.preference.Preference#onClick()
	 */
	@Override
	protected void onClick() {
		Intent launchHiddenActivity = new Intent(getContext(), PhotoLauncher.class);
		List<Integer> babyIds = EstrellitaTiles.getAllBabyIds(getContext());
		if (getKey().equals(BABY_ONE)) {
			if ( (babyIds != null) && (babyIds.size() > 0) ) {
				launchHiddenActivity.putExtra(PhotoLauncher.BABY_ID, babyIds.get(0));
			}
		}
		else if (getKey().equals(BABY_TWO)) {
			if ( (babyIds != null) && (babyIds.size() > 0) ) {
				launchHiddenActivity.putExtra(PhotoLauncher.BABY_ID, babyIds.get(1));
			}
		}
		else if (getKey().equals(PARENT)) {
			launchHiddenActivity.putExtra(PhotoLauncher.PARENT_ID, EstrellitaTiles.getParentId(getContext()));
		}
		getContext().startActivity(launchHiddenActivity);
	}
}
