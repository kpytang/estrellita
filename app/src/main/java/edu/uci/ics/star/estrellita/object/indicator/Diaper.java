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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.utils.ImageUtils;


public class Diaper extends Indicator implements Parcelable {
	public static final String FILENAME_DELIMITER = ";";

	private int mWet, mDirty, mBoth;
	private List<String> mFilenames;

	public Diaper(CommonData common, int wet, int dirty, int both, String filenames) {
		super(common);
		mWet = wet;
		mDirty = dirty;
		
		// we aren't going to use this anymore
		mBoth = -1;

		mFilenames = new ArrayList<String>();
		if (filenames != null && filenames.length()>0) {
			String[] tokens = filenames.split(FILENAME_DELIMITER);
			for (int i=0; i<tokens.length; i++) {
				if (tokens[i].length()>0) {
					mFilenames.add(tokens[i]);
				}
			}
		}
	}

	public Diaper(){
		super();
		mWet = 0;
		mDirty = 0;
		mBoth = -1;
		mFilenames = new ArrayList<String>();
	}

	public int getWet() {
		return mWet;
	}

	public void setWet(int wet) {
		this.mWet = wet;
	}

	public int getDirty() {
		return mDirty;
	}

	public void setDirty(int dirty) {
		this.mDirty = dirty;
	}
	
	public int getBoth() {
		return mBoth;
	}

	public void setPhotoFilenames(List<String> filenames) {
		mFilenames = filenames;
	}

	public List<String> getPhotoFilenames() {
		return mFilenames;
	}

	public int getTotal() {
		return mWet+mDirty;
	}

	public boolean isEmpty() {
		if ( (mWet == -1) && (mDirty == -1) ) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DateTime: " + getDateTime() + ", " + toChartString();
	}

	public String toChartString() {
		return "Wet: " + getWet() + ", Poopy: " + getDirty();
	}


	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}

	// writes out: 
	// wet, dirty counts
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeInt(mWet);
		out.writeInt(mDirty);
		out.writeStringList(mFilenames);
	}

	// used to regenerate the object
	public static final Parcelable.Creator<Diaper> CREATOR = new Parcelable.Creator<Diaper>() {
		public Diaper createFromParcel(Parcel in) {
			return new Diaper(in);
		}

		public Diaper[] newArray(int size) {
			return new Diaper[size];
		}
	};

	// this constructor takes in a Parcel and gives an object populated with its values
	// look at writeToParcel for the order of things to read in 
	private Diaper(Parcel in) {
		super();
		this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
		mWet = in.readInt();
		mDirty = in.readInt();
		mFilenames = new ArrayList<String>();
		in.readStringList(mFilenames);
	}

	@Override
	public String getTileBlurb() {
		return "wet: " + mWet + "\npoopy: " +  mDirty;
	}

	// this will check to make sure each of the filenames exist
	// if it doesn't, then it will create the file using the bitmaps from the list of diapers
	// ASSUMES: diaper list is in the same order as the filenames, by default this should be listed in chronological order (oldest one first)
	public static void checkAndCreatePhotoFileNames(List<String> filenames, List<Diaper> diapers) {
		// if there are no filenames, then we will not create new files
		if (filenames == null) {
			return;
		}
		for (int i=0; i<filenames.size(); i++) {
			String filename = filenames.get(i);
			if ( (filename != null) && (filename.length()>0)) {
				File file = new File(filenames.get(i));
				// check to see if the file exists
				// if it doesn't, then revert to loading/saving the corresponding bitmap and setting the timestamp to be the one on commondata
				if (!file.exists()) {
					if (diapers.get(i).getCommonData().getImage() != null) {
						ImageUtils.saveBitmapToFile(diapers.get(i).getCommonData().getImage(), filenames.get(i));
						file = new File(filenames.get(i));
						file.setLastModified(diapers.get(i).getCommonData().getTimestamp().getTime());
					}
				}
			}
		}
	}
}
