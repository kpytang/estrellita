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

package edu.uci.ics.star.estrellita.sharedprefs.api;

import android.content.Context;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class GeneralPreferencesApi extends PreferencesApi{

	public static void storeContentTableNames(Context context, String tables) {
		PreferencesApi.putString(context, CONTENT_TABLE_NAMES, tables);
	}

	public static void storeContentTitles(Context context,  String titles) {
		PreferencesApi.putString(context, CONTENT_TITLES, titles);
	}

	public static String[] getContentTableNames(Context context){
		String string = getPrefs(context).getString(PreferencesApi.CONTENT_TABLE_NAMES, null);
		if(string != null){
			return string.split(Utilities.DEFAULT_DELIMITER);
		} else {
			return new String[0];
		}
	}

	public static String[] getContentTitles(Context context){
		String string = getPrefs(context).getString(PreferencesApi.CONTENT_TITLES, null);
		if(string != null){
			return string.split(Utilities.DEFAULT_DELIMITER);
		} else {
			return new String[0];
		}
	}

	public static void storeContentTitlesAndTableNames(Context context, String titles, String tableNames) {
		getPrefsForEdit(context).putString(CONTENT_TITLES, titles)
		.putString(CONTENT_TABLE_NAMES, tableNames).commit();
	}

}
