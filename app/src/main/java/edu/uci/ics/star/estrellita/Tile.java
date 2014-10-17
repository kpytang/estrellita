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

package edu.uci.ics.star.estrellita;

import android.view.View;
import edu.uci.ics.star.estrellita.object.Indicator;

public class Tile {
	// constants
	public static final String APPOINTMENTS = "appointments";
	public static final String DIAPERS = "diapers";
	public static final String BONDING = "bonding";
	public static final String BABYMOODS = "baby moods";
	public static final String CUSTOMCHARTS = "custom charts";
	public static final String MYMOODS = "my moods";
	public static final String WEIGHT = "weight";
	public static final String SURVEYS = "my surveys";
	public static final String WALL = "wall";
	public static final String REMINDER = "reminder";
	
	public static final Integer APPOINTMENTS_ICON = R.drawable.appointments_icon;
	public static final Integer DIAPERS_ICON = R.drawable.diapers_icon;
	public static final Integer BONDING_ICON = R.drawable.bonding_icon;
	public static final Integer BABYMOODS_ICON = R.drawable.babymoods_icon;
	public static final Integer CUSTOMCHARTS_ICON = R.drawable.customcharts_icon;
	public static final Integer MYMOODS_ICON = R.drawable.moodmap_icon;
	public static final Integer WEIGHT_ICON = R.drawable.weight_icon;
	public static final Integer SURVEYS_ICON = R.drawable.surveys_icon;
	public static final Integer WALL_ICON = R.drawable.wall_icon;
	public static final Integer PHONE_ICON = R.drawable.phone;
	
	public static final Integer MAX_TILE_BLURB_LENGTH = 12;
	
	// these fields will be dynamically set
	private String mHeader;
	private String mInfo;

	// layout/ui fields
	private String mName;
	private Integer mIcon;
	private View.OnClickListener mButtonListener;
	private String indicatorClassString;
	
	public Tile() {
	}
	
	public String getHeaderText() {
		return mHeader;
	}
	
	public void setHeaderText(String header) {
		mHeader = header;
	}
	
	public String getInfoText() {
		return mInfo;
	}
	
	public void setInfoText(String info) {
		mInfo = info;
	}
	
	public String getButtonName() {
		return mName;
	}
	
	public void setButtonName(String name) {
		mName = name;
	}
	
	public Integer getIcon() {
		return mIcon;
	}
	
	public void setIcon(Integer icon) {
		mIcon = icon;
	}
	
	public View.OnClickListener getButtonListener() {
		return mButtonListener;
	}
	
	public void setButtonListener(View.OnClickListener listener) {
		mButtonListener = listener;
	}
	
	/**
	 * @param indicatorClass the indicatorClassString to set
	 */
	public void setIndicatorClassString(Class<? extends Indicator> indicatorClass) {
		indicatorClassString = indicatorClass.getSimpleName();
	}
	
	/**
	 * @return the indicatorClassString
	 */
	public String getIndicatorClassString() {
		return indicatorClassString;
	}
	

}
