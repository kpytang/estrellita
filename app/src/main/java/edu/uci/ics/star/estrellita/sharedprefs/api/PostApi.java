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
import android.content.SharedPreferences.Editor;

public class PostApi extends PreferencesApi{

	public static String getCommentPost(Context context) {
		return getPrefs(context).getString(PreferencesApi.COMMENT_POST, "no text");
	}

	public static int getCommentId(Context context) {
		return getPrefs(context).getInt(PreferencesApi.COMMENT_ID, -1);
	}

	public static int getCommentLocalId(Context context) {
		return getPrefs(context).getInt(PreferencesApi.COMMENT_ID_LOCAL, -1);
	}

	public static void clearCommentData(Context context) {
		getPrefs(context).edit()
		.remove(PreferencesApi.COMMENT_ID)
		.remove(PreferencesApi.COMMENT_POST).commit();
	}

	public static void putCommentdata(Context context, int id,
			String finalText, Integer localId) {
		Editor editor = getPrefs(context).edit();
		editor.putInt(PreferencesApi.COMMENT_ID, id);
		editor.putString(PreferencesApi.COMMENT_POST, finalText);
		editor.putInt(PreferencesApi.COMMENT_ID_LOCAL, localId);
		editor.commit();
	}
	
}
