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

package edu.uci.ics.star.estrellita.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.sharedprefs.api.UserPreferences;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class MainWidgetConfigurationWide extends Activity{
	static final String TAG = "ExampleAppWidgetConfigure";

	private RadioGroup kidGroup;

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private boolean dbWasOpen;

	private double dbKey = -1;

	public MainWidgetConfigurationWide() {
		super();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);

		// Set the view layout resource to use.
		setContentView(R.layout.appwidget_configuration_layout);


		// Bind the action for the save button.
		findViewById(R.id.widget_config_ok).setOnClickListener(mOnClickListener);

		findViewById(R.id.widget_config_cancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// Find the widget id from the intent. 
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		List<Integer> kidIds = UserPreferences.getKidIds(this);
		if(kidIds != null){
			Database db = new Database(this);
			dbKey = db.open(dbKey);

			kidGroup = (RadioGroup)findViewById(R.id.widget_config_kidchooser);
			for(int id : kidIds){
				try {

					dbKey = db.open(dbKey);

					Baby mCurrentBaby = db.getBabyTable().getBaby(id, this);
					RadioButton rb = new RadioButton(this);
					rb.setId(mCurrentBaby.getId());
					rb.setText(mCurrentBaby.getName());
					kidGroup.addView(rb);
				} catch (NoUserException e) {
					e.printStackTrace();
				}
			}
			db.close(dbKey);

		} else{
			// notify is null

		}


		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

	}

	View.OnClickListener mOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			final Context context = MainWidgetConfigurationWide.this;

			if(kidGroup != null){
				int selectedChild = kidGroup.getCheckedRadioButtonId();
				// save the selected child string in prefs
				UserPreferences.saveWidgetKidPref(context, mAppWidgetId, selectedChild);
				UserPreferences.saveWidgetTypePref(context, mAppWidgetId, "4x2");
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				Utilities.updateWidget(context);
			} else{
			}
			// close the activity because we're done
			finish();
		}

	};

	static void deleteTitlePref(Context context, int appWidgetId) {
	}

	static void loadAllTitlePrefs(Context context, ArrayList<Integer> appWidgetIds,
			ArrayList<String> texts) {
	}
}
