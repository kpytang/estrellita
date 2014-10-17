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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey.BondingActivity;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class BondingForm extends TileActivity<BondingSurvey> {
	CheckBox mSingingCheckbox, mReadingCheckbox, mTalkingCheckbox, mTummyCheckbox, mWalkingCheckbox;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bonding_form);
        
        // initialize header & footer
        setActivityHeader("Bonding", true, Tile.BONDING);
        setButtonFooter("update activities", mSaveClickListener, null, null);
        
        // initialize the date
        TextView tv = (TextView) this.findViewById(R.id.date_header);
        tv.setText("for " + DateUtils.getNowString(DateUtils.DATE_HEADER_FORMAT));
        
        mSingingCheckbox = (CheckBox) this.findViewById(R.id.singing_checkbox);
        mReadingCheckbox = (CheckBox) this.findViewById(R.id.reading_checkbox);
        mTalkingCheckbox = (CheckBox) this.findViewById(R.id.talking_checkbox);
        mTummyCheckbox = (CheckBox) this.findViewById(R.id.tummytime_checkbox);
        mWalkingCheckbox = (CheckBox) this.findViewById(R.id.walking_checkbox);
        
        // initialize checkboxes with the latest state
        BondingSurvey b = (BondingSurvey) getIntent().getParcelableExtra("bonding");
        // if there is no bonding for today, just initialize this with a new (all false flags) bonding object
        if (b == null) {
        	b = new BondingSurvey();
        }
        if (b.hasCompletedSinging()) {
        	mSingingCheckbox.setChecked(true);
        }
        if (b.hasCompletedReading()) {
        	mReadingCheckbox.setChecked(true);
        }
        if (b.hasCompletedTalking()) {
        	mTalkingCheckbox.setChecked(true);
        }
        if (b.hasCompletedTummyTime()) {
        	mTummyCheckbox.setChecked(true);
        }
        if (b.hasCompletedWalking()) {
        	mWalkingCheckbox.setChecked(true);
        }
	}
	
	// grabs all the reported info and sends it back to the overview activity
    public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			BondingSurvey b = new BondingSurvey();
			b.getCommonData().setIdUser(EstrellitaTiles.getParentId(BondingForm.this));
			b.getCommonData().setIdBaby(mBaby.getId());
			b.getCommonData().setTimestamp(DateUtils.getTimestamp());
			
			List<BondingActivity> activities = new ArrayList<BondingActivity>();			
			if (mSingingCheckbox.isChecked()) {
				activities.add(BondingActivity.SINGING);
			}
			if (mReadingCheckbox.isChecked()) {
				activities.add(BondingActivity.READING);
			}
			if (mTalkingCheckbox.isChecked()) {
				activities.add(BondingActivity.TALKING);
			}
			if (mTummyCheckbox.isChecked()) {
				activities.add(BondingActivity.TUMMY_TIME);
			}
			if (mWalkingCheckbox.isChecked()) {
				activities.add(BondingActivity.TAKE_BABY_WALKING);
			}
			if (activities.size()==0) {
				activities.add(BondingActivity.NONE);
			}
			b.addActivities(activities);

			Intent intent = new Intent();
			intent.putExtra("bonding", b);
			setResult(RESULT_OK, intent);
			finish();
		}
	};
}
