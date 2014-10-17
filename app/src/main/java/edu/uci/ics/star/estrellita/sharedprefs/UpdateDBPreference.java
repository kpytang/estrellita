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

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import edu.uci.ics.star.estrellita.db.Database;

public class UpdateDBPreference extends Preference {
	
    public UpdateDBPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * (non-Javadoc)
     * @see android.preference.Preference#onClick()
     */
    @Override
    protected void onClick() {
    	Database db = null;
		double dbKey = -2;
    	try {
    		db = new Database(getContext());
			dbKey = db.open(dbKey);
    		db.restoreDBFromSDCard();
		}
    	finally {
    		if (db != null) {
    			db.close(dbKey);
    		}
    	}
    }
}
