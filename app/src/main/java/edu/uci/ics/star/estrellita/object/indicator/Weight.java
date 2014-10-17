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

import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Indicator;


public class Weight extends Indicator implements Parcelable {
	private static final double OUNCES_PER_POUND = 16;
	
	private double mWeight;
	
	private int mPounds;
	private int mOunces;

	public Weight(CommonData common, int pounds, int ounces) {
		super(common);
		mWeight = pounds + ounces/OUNCES_PER_POUND;
		mPounds = pounds;
		mOunces = ounces;
	}
	
	public Weight(){
		super();
		mWeight = -1;
		mPounds = 0;
		mOunces = 0;
	}
	
	public void setWeight(double weight) {
		this.mWeight = weight;
	}
	
	public void setPounds(int pounds) {
		mPounds = pounds;
	}
	
	public void setOunces(int ounces) {
		mOunces = ounces;
	}

	public double getWeight() {
		if (mPounds>0 || mOunces>0) {
			return mPounds + mOunces/OUNCES_PER_POUND;
		}
		else {
			return -1;
		}
	}
	
	public int getPounds() {
		return mPounds;
	}
	
	public int getOunces() {
		return mOunces;
	}
	
	public boolean isEmpty() {
		if (getWeight() == -1) {
			return true;
		}
		return false;
	}
	
	public String toChartString() {
		return "Weight: " + mPounds + " lbs, " + mOunces + " ozs";
	}
		
	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}
	
	// writes out: total weight, pounds, ounces
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeInt(mPounds);
		out.writeInt(mOunces);
	}
	
	// used to regenerate the object
    public static final Parcelable.Creator<Weight> CREATOR = new Parcelable.Creator<Weight>() {
        public Weight createFromParcel(Parcel in) {
            return new Weight(in);
        }

        public Weight[] newArray(int size) {
            return new Weight[size];
        }
    };

    // this constructor takes in a Parcel and gives an object populated with its values
    // look at writeToParcel for the order of things to read in 
    private Weight(Parcel in) {
    	super();
    	this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
    	mPounds = in.readInt();
    	mOunces = in.readInt();
    	mWeight = mPounds + mOunces/OUNCES_PER_POUND;
    }
    
    @Override
	public String toString() {
		return "DateTime: " + getDateTime() + ", " + toChartString();
	}
    
    public String getWeightString() {
    	return mPounds + " lbs, " + mOunces + " ozs";
    }
	
	@Override
	public String getTileBlurb() {
		return mPounds + " lbs\n" + mOunces + " ozs";
	}
}
