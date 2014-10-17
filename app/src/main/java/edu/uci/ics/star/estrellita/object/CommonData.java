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

import java.sql.Timestamp;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class CommonData implements Parcelable {
	private Integer id;
	private Integer localId;
	private Integer idUser;
	private Integer idBaby;
	private Timestamp timestamp;
	private Bitmap image;
	private Flag flag;
	private String notes;
	private boolean forUpdate;
	private boolean forDelete;
	private boolean isDeleted;
	private String androidFilename;

	public CommonData(){
		this(null, null, -1, -1, DateUtils.getTimestamp(), null, Flag.value, null);
	}

	public CommonData(Integer userId, Integer babyId){
		this(null, null, userId ,babyId,  DateUtils.getTimestamp(), null, Flag.value, null);
	}

	public CommonData(User user){
		this(null, null, user.getId(), user.getCurrentKid().getId(), DateUtils.getTimestamp(), null, Flag.value, null);
	}

	public CommonData(Integer id, Integer localId, Integer idUser, Integer idBaby, Timestamp timestamp,
			Bitmap image, Flag flag) {
		this(id, localId, idUser, idBaby, timestamp, image, flag, null);
	}
	
	public CommonData(int id, int userId, int babyId) {
		this(id, null, userId, babyId, DateUtils.getTimestamp(), null, Flag.value, null);
	}

	public CommonData(Integer id, Integer localId, Integer idUser, Integer idBaby, Timestamp timestamp,
			Bitmap image, Flag flag, String notes) {
		this(id, localId, idUser, idBaby, timestamp, image, flag, notes, 0, 0, 0);
	}
	public CommonData(Integer id, Integer localId, Integer idUser, Integer idBaby, Timestamp timestamp,
			Bitmap image, String androidFilename, Flag flag, String notes) {
		this(id, localId, idUser, idBaby, timestamp, image,  androidFilename, flag, notes, 0, 0, 0);
	}
	
	public CommonData(Integer id, Integer localId, Integer idUser, Integer idBaby, Timestamp timestamp,
			Bitmap image, Flag flag, String notes, int forUpdate, int forDelete, int isDeleted) {
		this(id, localId, idUser, idBaby, timestamp, image, null, flag, notes, forUpdate, forDelete, isDeleted);
	}
	
	public CommonData(Integer id, Integer localId, Integer idUser, Integer idBaby, Timestamp timestamp,
			Bitmap image, String androidFilename, Flag flag, String notes, int forUpdate, int forDelete, int isDeleted) {
		super();
		this.id = id;
		this.localId = localId;
		this.setIdBaby(idBaby);
		this.idUser = idUser;
		//		this.personId = personId;
		//		this.recordId = recordId;
		this.timestamp = timestamp;
		this.image = image;
		this.androidFilename = androidFilename;
		this.flag = flag;
		this.notes = notes;
		this.forUpdate = (forUpdate == 1);
		this.forDelete = (forDelete == 1);
		this.isDeleted = (isDeleted == 1);
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public Flag getFlag() {
		return flag;
	}

	public String getFlagString() {
		if(flag == null){
			return null;
		}
		return flag.name();
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public Integer getLocalId() {
		return localId;
	}

	public void setLocalId(Integer localId) {
		this.localId = localId;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public int getIdUser() {
		return idUser;
	}

	public void setIdBaby(Integer idBaby) {
		this.idBaby = idBaby;
	}

	public Integer getIdBaby() {
		return idBaby;
	}

	public String getNotes() {
		if (hasNotes()) {
			return notes;
		}
		else {
			return "";
		}
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean hasNotes() {
		if ((notes != null) && (notes.length()>0)) {
			return true;
		}
		return false;
	}
	
	public boolean hasPhotos() {
		if (image == null) {
			if (androidFilename == null) {
				return false;
			}
		}
		return true;
	}

	public void setForUpdate(boolean forUpdate) {
		this.forUpdate = forUpdate;
	}

	public boolean isForUpdate() {
		return forUpdate;
	}

	public void setForDelete(boolean forDelete) {
		this.forDelete = forDelete;
	}

	public boolean isForDelete() {
		return forDelete;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public String getAndroidFilename() {
		return androidFilename;
	}
	
	public void setAndroidFilename(String filename){
		this.androidFilename = filename;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "id: " + id + ", localId: " + localId + ", idUser: " + idUser +", idBaby: " + idBaby + ", timestamp: " + timestamp + ", hasImage: " + (image!=null)+ ", androidFilename: " + androidFilename ;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel out, int flags) {
		out.writeValue(id);
		out.writeValue(localId);
		out.writeValue(idUser);
		out.writeValue(idBaby);
		out.writeLong(timestamp.getTime());
		out.writeParcelable(image, flags);
		out.writeString(androidFilename);
		out.writeString(flag.toString());
		out.writeString(notes);
		out.writeInt((forUpdate?1:0));
		out.writeInt((forDelete?1:0));
		out.writeInt((isDeleted?1:0));
	}

	// used to regenerate the object
	public static final Parcelable.Creator<CommonData> CREATOR = new Parcelable.Creator<CommonData>() {
		public CommonData createFromParcel(Parcel in) {
			return new CommonData(in);
		}

		public CommonData[] newArray(int size) {
			return new CommonData[size];
		}
	};

	// this constructor takes in a Parcel and gives an object populated with its values
	// look at writeToParcel for the order of things to read in 
	public CommonData(Parcel in) {
		super();
		this.id = (Integer)in.readValue(Integer.class.getClassLoader());
		this.localId = (Integer)in.readValue(Integer.class.getClassLoader());
		try {
			this.idUser = (Integer)in.readValue(Integer.class.getClassLoader());
		} catch (Exception e){
			this.idUser = null;
		}
		try {
			this.idBaby = (Integer)in.readValue(Integer.class.getClassLoader());
		} catch (Exception e){
			this.idBaby = null;
		}
		this.timestamp = new Timestamp(in.readLong());
		this.image = in.readParcelable(Bitmap.class.getClassLoader());
		this.androidFilename = in.readString();
		this.flag = Flag.valueOf(in.readString());
		this.notes = in.readString();
		this.forUpdate = (in.readInt() == 1);
		this.forDelete = (in.readInt() == 1);
		this.isDeleted = (in.readInt() == 1);
	}

	@Override
	public int describeContents() {
		return 0;
	}



}
