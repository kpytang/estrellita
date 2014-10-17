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

package edu.uci.ics.star.estrellita.object;

import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class GenericIndicator extends Indicator implements Parcelable {
	public enum IndicatorType { NUMERIC, TEXT };

	public static final String LIST_DELIMITER = ",";

	private String mData;
	private String mUnits;
	private String mTitle;
	private IndicatorType mType;

	public GenericIndicator() {
		super();
		mType = IndicatorType.TEXT;
	}

	public GenericIndicator(CommonData cd, String title, String units, String data) {
		super(cd);
		mData = data;
		mTitle = title;
		mUnits = units;
		
		setChartType();
	}

	public GenericIndicator(String data) {
		super();
		mData = data;
		
		setChartType();
	}

	public String getDataAsString() {
		if (mData == null) {
			return "";
		}
		return mData;
	}

	// will return null if the data is not numeric
	public Float getDataAsFloat() {
		try {
			return Float.parseFloat(mData);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public IndicatorType getChartType() {
		return mType;
	}

	public void setData(String data) {
		mData = data.trim();
	}
	
	public void setChartType() {
		if (getDataAsFloat() != null) {
			mType = IndicatorType.NUMERIC;
		}
		else {
			mType = IndicatorType.TEXT;
		}
	}

	public void setTitle(String title) {
		this.mTitle = title.trim();
	}

	public String getTitle() {
		return mTitle;
	}

	public String getUnits() {
		return mUnits;
	}

	public void setUnits(String units) {
		mUnits = units;
	}

	public boolean isDataNumeric() {
		try {
			Float.parseFloat(mData);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	// follows the same convention as the other indicators and uses -1 to indicate that there is no data for this indicator
	public boolean isEmpty() {
		if (mData.equals("-1")) {
			return true;
		}
		return false;
	}

	public String toChartString() {
		return "value: " + mData;
	}

	public String toString() {
		return "Title: " + mTitle + ", value: " + mData + " (Date: " + getCommonData().getTimestamp() + ")";
	}

	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}

	// writes out: chart title 
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeString(mTitle);
		out.writeString(mData);
		out.writeString(mUnits);
	}

	// used to regenerate the object
	public static final Parcelable.Creator<GenericIndicator> CREATOR = new Parcelable.Creator<GenericIndicator>() {
		public GenericIndicator createFromParcel(Parcel in) {
			return new GenericIndicator(in);
		}

		public GenericIndicator[] newArray(int size) {
			return new GenericIndicator[size];
		}
	};

	// this constructor takes in a Parcel and gives an object populated with its values
	// look at writeToParcel for the order of things to read in 
	private GenericIndicator(Parcel in) {
		super();
		this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
		mTitle = in.readString();
		mData = in.readString();
		mUnits = in.readString();
	}

	@Override
	public String getTileBlurb() {
		return StringUtils.subString(mTitle,0,Tile.MAX_TILE_BLURB_LENGTH) + "\n" + mData;
	}

	public String getIndicatorString() {
		if ((mUnits != null) && (mUnits.length()>0)) {
			return mData + " " + mUnits;
		}
		else {
			return mData;
		}
	}
}
