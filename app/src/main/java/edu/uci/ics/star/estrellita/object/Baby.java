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

import java.io.File;
import java.util.Date;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.sharedprefs.PhotoLauncher;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;

public class Baby extends Indicator implements Parcelable {
	public enum Gender { UNKNOWN, MALE, FEMALE };

	private String mName, mPhotoURL;
	private Gender mGender;
	private Date mActualBirthday, mExpectedBirthday;

	public Baby(Integer babyId, Integer parentId, Integer localId, 
			String firstName, Bitmap image, String gender) {
		super(new CommonData(babyId, localId, parentId, babyId, DateUtils.getTimestamp(), image, null));
		mName = firstName;
		mPhotoURL = "";
		setGender(gender);
	}

	public Baby(CommonData cd, 
			String firstName, String photoUrl, Date actualBirthday, Date expectedBirthday, String gender) {
		super(cd);
		mName = firstName;
		mPhotoURL = photoUrl;
		mActualBirthday = actualBirthday;
		mExpectedBirthday = expectedBirthday;
		setGender(gender);
	}

	public Baby() {
		this(null, null, null, "unknown", null, null);
	}

	public int getChronologicalAge() {
		return DateUtils.getWeeksSince(mActualBirthday);
	}

	public Date getActualBirthday() {
		return mActualBirthday;
	}

	public int getAdjustedAge() {
		return DateUtils.getWeeksSince(mExpectedBirthday);
	}

	public Date getExpectedBirthday() {
		return mExpectedBirthday;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setGender(String gender) {
		try {
			if (gender != null) {
				mGender = Gender.valueOf(gender.toUpperCase());
			}
			else {
				mGender = Gender.UNKNOWN;
			}
		}
		catch (Exception e) {
			mGender = Gender.UNKNOWN;
		}
	}

	public void setGender(Gender gender) {
		mGender = gender;
	}

	public Gender getGender() {
		return mGender;
	}

	public void setPhotoURL(String photoUrl) {
		mPhotoURL = photoUrl;
	}

	public String getPhotoURL() {
		return mPhotoURL;
	}

	// not really needed, so we'll use the default behavior (return 0)
	@Override
	public int describeContents() {
		return 0;
	}

	// writes out baby id, followed by baby's name
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(getCommonData(), flags);
		out.writeString(mName);
		out.writeString(mPhotoURL);
		out.writeString(mGender.toString());
		out.writeLong(mActualBirthday.getTime());
		out.writeLong(mExpectedBirthday.getTime());
	}

	// used to regenerate the object
	public static final Parcelable.Creator<Baby> CREATOR = new Parcelable.Creator<Baby>() {
		public Baby createFromParcel(Parcel in) {
			return new Baby(in);
		}

		public Baby[] newArray(int size) {
			return new Baby[size];
		}
	};

	// this constructor takes in a Parcel and gives an object populated with its values
	// look at writeToParcel for the order of things to read in 
	private Baby(Parcel in) {
		this.setCommonData((CommonData) in.readParcelable(CommonData.class.getClassLoader()));
		mName = in.readString();
		mPhotoURL = in.readString();
		setGender(in.readString());
		mActualBirthday = new Date(in.readLong());
		mExpectedBirthday = new Date(in.readLong());
	}

	@Override
	public String getTileBlurb() {
		return null;
	}

	@Override
	public Bitmap getConvertedImage() {
		if(this.getCommonData() != null){
			if(this.getCommonData().getAndroidFilename() != null)
			{
				return ImageUtils.decodeFile(this.getCommonData().getAndroidFilename());
			} else if (this.getCommonData().getImage() != null) {
				return this.getCommonData().getImage();
			}
		}
		String path = PhotoLauncher.IMAGE_DIR_PATH
			+ PhotoLauncher.BABY_PHOTO_PREFIX + this.getId() + PhotoLauncher.PHOTO + PhotoLauncher.PNG_EXTENSION;
		File f = new File(path);
		if (f.exists()) {
			return ImageUtils.decodeFile(path);
		}
		return null;
	}

}
