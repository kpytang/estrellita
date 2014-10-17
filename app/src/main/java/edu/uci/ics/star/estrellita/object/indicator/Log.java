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

package edu.uci.ics.star.estrellita.object.indicator;

import android.content.ComponentName;
import android.content.Intent;
import edu.uci.ics.star.estrellita.VersionInfo;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Indicator;

public class Log extends Indicator {

	public static final String HEART_BEAT = "ba-dump: " + VersionInfo.VERSION;
	public static final String ONRESUME = "onResume";
	public static final String ONPAUSE = "onPause";
	
	private String mActivity, mIntent, mAction;

	public Log(CommonData cd, String activity, String intent, String action) {
		super(cd);
		this.mActivity = activity;
		this.mIntent = intent;
		this.mAction = action;
	}

	public Log(Integer userId, Integer babyId, Intent intent, String action) {
		super(new CommonData(userId, babyId));
		if(intent != null) {
			this.mIntent = intent.toString();
			ComponentName component = intent.getComponent();
			if(component != null) {
				this.mActivity = component.getClassName();
			} else {
				this.mActivity = "???intent.getComponent() was null";
			}
		} else  {
			this.mIntent = "???intent was null";
		}
		this.mAction = action;
	}

	public String getActivity() {
		if(mActivity == null) {
			return "";
		}
		return mActivity;
	}

	public void setActivity(String activity) {
		this.mActivity = activity;
	}

	public String getIntent() {
		if(mIntent == null) {
			return "";
		}
		return mIntent;
	}

	public void setIntent(String intent) {
		this.mIntent = intent;
	}

	public String getAction() {
		if(mAction == null) {
			return "";
		}
		return mAction;
	}

	public void setAction(String action) {
		this.mAction = action;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return mActivity + ", " + mAction + ", " + mIntent;
	}

	@Override
	public String getTileBlurb() {
		return null;
	}

}
