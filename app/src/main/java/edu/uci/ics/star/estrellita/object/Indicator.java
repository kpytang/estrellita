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

import edu.uci.ics.star.estrellita.utils.ImageUtils;

import android.graphics.Bitmap;

public abstract class Indicator implements Comparable<Indicator> {
	public CommonData common;

	public Indicator(CommonData cd){
		this.common = cd;
	}

	public Indicator (){
		common = new CommonData();
	}	
	
	public Indicator(User user){
		common = new CommonData(user);
	}
	
	public Indicator(Integer userId, Integer babyId){
		common = new CommonData(userId, babyId);
	}

	public CommonData getCommonData(){
		return common;
	}

	public void setCommonData(CommonData cd){
		this.common = cd;
	}

	public void setImage(Bitmap image) {
		this.getCommonData().setImage(image);
	}

	public Bitmap getConvertedImage() {
		if(this.getCommonData() != null){
			if(this.getCommonData().getAndroidFilename() != null)
			{
				return ImageUtils.decodeFile(this.getCommonData().getAndroidFilename());
			} else {
				return this.getCommonData().getImage();
			}
		}
		return null;
	}
	
	public Integer getId() {
		return common.getId();
	}

	public Integer getLocalId() {
		return common.getLocalId();
	}

	public void setLocalid(Integer localId){
		common.setLocalId(localId);
	}

	public void setId(Integer id) {
		common.setId(id);
	}

	public Timestamp getDateTime() {
		return common.getTimestamp();
	}

	public abstract String getTileBlurb();

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Indicator))
			return false;
		Indicator n = (Indicator)o;
		if( n.getCommonData().getId() != null) {
			return n.common.getId().equals(this.getCommonData().getId());
		} else {
			return n.common.getLocalId().equals(this.getCommonData().getLocalId());
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Indicator anotherIndicator) {
		return getCommonData().getTimestamp().compareTo(anotherIndicator.getCommonData().getTimestamp());
	}

}
