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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class TileActivity<T extends Indicator> extends GenericActivity {

	protected Integer mLogPersonId = null;

	protected T mIndicator = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();
		openDatabase();
		if (mBaby == null) {
			mDatabase.log(EstrellitaTiles.getParentId(this), EstrellitaTiles.getCurrentBabyId(), getIntent(), Log.ONRESUME);
		}
		else {
			mDatabase.log(EstrellitaTiles.getParentId(this), mBaby.getId(), getIntent(), Log.ONRESUME);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	protected void onPause() {
		super.onPause();
		openDatabase();
		if (mBaby == null) {
			mDatabase.log(EstrellitaTiles.getParentId(this), EstrellitaTiles.getCurrentBabyId(), getIntent(), Log.ONPAUSE);
		}
		else {
			mDatabase.log(EstrellitaTiles.getParentId(this), mBaby.getId(), getIntent(), Log.ONPAUSE);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		saveIndicator();
		return mIndicator;
	}

	protected void saveIndicator() {
		if (mIndicator != null) {
			mIndicator.getCommonData().setIdUser(EstrellitaTiles.getParentId(this));
			mIndicator.getCommonData().setIdBaby(mBaby.getId());
			mIndicator.getCommonData().setTimestamp(DateUtils.getTimestamp());
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	protected void onStop() {
		closeDatabase();
		super.onStop();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tile_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		try{
			// Handle item selection
			switch (item.getItemId()) {
			case R.id.home:
				startActivity(new Intent(this, EstrellitaTiles.class));
				break;
			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return true;
	}
}
