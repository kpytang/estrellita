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


public class Response extends Indicator {
	Integer mData;
	String mText;
	
	public Response() {
		super();
	}
	
	public Response(CommonData cd, Integer data) {
		super(cd);
		mData = data;
	}
	
	public Response(CommonData cd, Integer data, String text) {
		super(cd);
		mData = data;
		mText = text;
	}
	
	public Response(Integer data) {
		super();
		mData = data;
	}
	
	public Response(String text) {
		super();
		mText = text;
	}
	
	public Integer getData() {
		if (mData == null) {
			return -1;
		}
		return mData;
	}
	
	public String getText() {
		if (mText == null) {
			return "";
		}
		return mText;
	}
	
	public void setText(String text) {
		mText = text;
	}
	
	public void setData(Integer data) {
		mData = data;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "";
		if (mData != null) {
			s += "Data (Integer) = " + mData.toString();
		}
		if (mText != null) {
			s += "Text (String) = " + mText;
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.object.Indicator#getTileBlurb()
	 */
	@Override
	public String getTileBlurb() {
		return null;
	}
}
