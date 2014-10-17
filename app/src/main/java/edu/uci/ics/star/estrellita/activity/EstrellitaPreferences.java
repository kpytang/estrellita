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

package edu.uci.ics.star.estrellita.activity;

import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.VersionInfo;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.sharedprefs.PhotoPickerPreference;

public class EstrellitaPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		PreferenceScreen root = this.getPreferenceScreen();

		// add a category for changing profile pics
		PreferenceCategory photoCategory = new PreferenceCategory(this);
		photoCategory.setTitle("Profile Photos");
		root.addPreference(photoCategory);

		// add preferences for each baby, depending on how many babies there are
		PhotoPickerPreference photoPickerPref;
		List<Baby> babies = EstrellitaTiles.getAllBabies(this);
		for (int i=0; i<babies.size(); i++) {
			photoPickerPref = new PhotoPickerPreference(this);
			String key = "";
			if (i == 0) {
				key = PhotoPickerPreference.BABY_ONE;
			}
			else if (i == 1) {
				key = PhotoPickerPreference.BABY_TWO;
			}
			photoPickerPref.setKey(key);
			photoPickerPref.setTitle("Select a new photo for " + babies.get(i).getName());
			photoPickerPref.setSummary("Change the profile photo for " + babies.get(i).getName());
			photoCategory.addPreference(photoPickerPref);
		}
		
		// add a preference for changing user/parent's photo
		photoPickerPref = new PhotoPickerPreference(this);
		photoPickerPref.setKey("parent_photo");
		photoPickerPref.setTitle("Select a new photo for you");
		photoPickerPref.setSummary("Change your profile photo");
		photoCategory.addPreference(photoPickerPref);
		
		// add a category for version info
		PreferenceCategory versionCategory = new PreferenceCategory(this);
		versionCategory.setTitle("Version Info");
		root.addPreference(versionCategory);
		
		Preference versionPref = new Preference(this);
		versionPref.setKey("version");
		versionPref.setTitle("Current software version");
		versionPref.setSummary(VersionInfo.VERSION);
		versionCategory.addPreference(versionPref);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

}
