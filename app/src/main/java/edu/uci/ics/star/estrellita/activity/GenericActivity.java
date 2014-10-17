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

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class GenericActivity extends Activity {
	public static final String BABY = "baby";
	public static final String BABYID = "tile_baby_id";
	
	protected Typeface bold, regular;

	private double dbKey = -1;
	protected Database mDatabase = null;
	
	protected Baby mBaby;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bold = Typeface.createFromAsset(this.getAssets(),"fonts/MyriadPro-Bold.otf"); 
		regular = Typeface.createFromAsset(this.getAssets(),"fonts/MyriadPro-Regular.otf"); 
		
		// get baby info from intent
		openDatabase();

		try {
			int babyId = getIntent().getIntExtra(BABYID, -1);
			if(babyId != -1) {
				mBaby = mDatabase.getBabyTable().getBaby(babyId, this);
			} else {
				mBaby = getIntent().getParcelableExtra(BABY);
			}
		} catch (NoUserException e) {
			e.printStackTrace();
		}
	}
	
	protected void openDatabase() {
		if (mDatabase == null) {
			mDatabase = new Database(this);
		}
		dbKey = mDatabase.open(dbKey);
	}

	protected void closeDatabase() {
		if (mDatabase != null) {
			mDatabase.close(dbKey);
		}
	}

	public void setActivityHeader(String activityName, boolean showBabyName, String tileName) { 
		TextView tv = (TextView) this.findViewById(R.id.activity_name);
		tv.setText(activityName);
		tv.setTypeface(bold);

		tv = (TextView) this.findViewById(R.id.for_which_baby);
		if (!showBabyName || mBaby == null) {
			tv.setVisibility(View.GONE);
		}
		else {
			tv.setText("for " + StringUtils.capitalize(mBaby.getName()));
			tv.setTypeface(regular);
		}

		ImageView iv = (ImageView) this.findViewById(R.id.activity_icon);
		if (tileName.equals(Tile.APPOINTMENTS)) {
			iv.setImageResource(Tile.APPOINTMENTS_ICON);
		}
		else if (tileName.equals(Tile.DIAPERS)) {
			iv.setImageResource(Tile.DIAPERS_ICON);
		}
		else if (tileName.equals(Tile.BONDING)) {
			iv.setImageResource(Tile.BONDING_ICON);
		}
		else if (tileName.equals(Tile.BABYMOODS)) {
			iv.setImageResource(Tile.BABYMOODS_ICON);
		}
		else if (tileName.equals(Tile.CUSTOMCHARTS)) {
			iv.setImageResource(Tile.CUSTOMCHARTS_ICON);
		}
		else if (tileName.equals(Tile.MYMOODS)) {
			iv.setImageResource(Tile.MYMOODS_ICON);
		}
		else if (tileName.equals(Tile.WEIGHT)) {
			iv.setImageResource(Tile.WEIGHT_ICON);
		}
		else if (tileName.equals(Tile.SURVEYS)) {
			iv.setImageResource(Tile.SURVEYS_ICON);
		}
		else if (tileName.equals(Tile.WALL)) {
			iv.setImageResource(Tile.WALL_ICON);
		}
		else if (tileName.equals(Tile.REMINDER)) {
			iv.setImageResource(Tile.PHONE_ICON);
		}
	}

	public void setButtonHeader(String buttonName) {		
		TextView tv = (TextView) this.findViewById(R.id.button_text);
		tv.setText(buttonName);
		tv.setTypeface(regular);
	}

	/**
	 * we require there to be a right button (typically labeled as "save", "update"), so the right button is alaways visible
	 * @param rightButtonName
	 * @param saveListener
	 * @param leftButtonName
	 * @param deleteListener
	 */
	public void setButtonFooter(String rightButtonName, OnClickListener rightButtonListener, String leftButtonName, OnClickListener leftButtonListener) {
		TextView tv = (TextView) this.findViewById(R.id.right_button);
		tv.setText(rightButtonName);
		tv.setOnClickListener(rightButtonListener);
		tv = (TextView) this.findViewById(R.id.left_button);
		if (leftButtonName == null) {
			tv.setVisibility(View.GONE);
		}
		else {
			tv.setVisibility(View.VISIBLE);
			tv.setText(leftButtonName);
			tv.setOnClickListener(leftButtonListener);
		}
	}

	public void updateLeftButtonFooter(String leftButtonName, OnClickListener leftButtonListener) {
		TextView tv = (TextView) this.findViewById(R.id.left_button);
		if (leftButtonName == null) {
			tv.setText("");
			tv.setOnClickListener(null);
			tv.setVisibility(View.GONE);
		}
		else {
			tv.setVisibility(View.VISIBLE);
			tv.setText(leftButtonName);
			tv.setOnClickListener(leftButtonListener);
		}
	}

	public void updateRightButtonFooter(String rightButtonName, OnClickListener rightButtonListener) {
		TextView tv = (TextView) this.findViewById(R.id.right_button);
		if (rightButtonName == null) {
			tv.setText("");
			tv.setOnClickListener(null);
			tv.setVisibility(View.GONE);
		}
		else {
			tv.setVisibility(View.VISIBLE);
			tv.setText(rightButtonName);
			tv.setOnClickListener(rightButtonListener);
		}
	}
}
