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
import java.util.ArrayList;

import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.utils.DateUtils;

public class User extends Indicator{
	public enum UserType { NONE, PARENT, CASE_MANAGER, WEB_USER }; 
	
	private ArrayList<Baby> mKids;
	private String mFirstName;
	private String mLastName;
	private String mUsername;
	private String mPassword;
	private UserType mUserType;
	private String mPhotoURL;
	private String mEmail;
	private String mLastLogin;
	private int mCurrentKid;

	public User(CommonData cd, 
			String firstName, String lastName, String user, String pass, String photoUrl, String type, String email) {
		super(cd);
		mPhotoURL = photoUrl;
		mKids = new ArrayList<Baby>();
		mFirstName = firstName;
		mLastName = lastName;
		setUsername(user);
		setPassword(pass);
		setUserType(type);
		setEmail(email);
		mCurrentKid = 0;
	}

	public User() {
		this(new CommonData(), "unknown", "user", "username", "pass", null, "parent", null);
	}

	public Baby getCurrentKid(){
		return mKids.get(mCurrentKid);
	}

	public int getCurrentKidIndex(){
		return mCurrentKid;
	}

	public void setCurrentKidIndex(int index){
		mCurrentKid = index;
	}

	public void setCurrentKidById(int id) throws NoUserException{
		mCurrentKid = getKidIndexById(id);
	}

	public int getKidIndexById(int id) throws NoUserException{
		for(int i = 0; i < mKids.size(); i ++){
			Baby kid = mKids.get(i);
			if(kid.getId() == id){
				return i;
			}
		}
		throw new NoUserException(); 
	}

	public ArrayList<Baby> getKids() {
		return mKids;
	}

	public void addKid(Baby kid) {
		mKids.add(kid);
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String name) {
		mFirstName = name;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setUsername(String user) {
		mUsername = user;
	}

	public String getUsername() {
		return mUsername;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setUserType(String type) {
		type = type.toUpperCase();
		try {
			if (type != null) {
				mUserType = UserType.valueOf(type);
			}
			else {
				mUserType = UserType.NONE;
			}
		}
		catch (Exception e) {
			mUserType = UserType.NONE;
		}
	}
	
	public void setUserType(UserType type) {
		mUserType = type;
	}

	public UserType getUserType() {
		return mUserType;
	}

	public void setLastLogin(Timestamp t) {
		mLastLogin = DateUtils.getDateAsString(t, DateUtils.MYSQL_DATE_TIME_FORMAT);
	}

	public String getLastLogin() {
		return mLastLogin;
	}
	
	public void setEmail(String email) {
		mEmail = email;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setPhotoURL(String photoUrl) {
		mPhotoURL = photoUrl;
	}

	public String getPhotoURL() {
		return mPhotoURL;
	}

	public CharSequence[] getKidsNamesList() {
		CharSequence[] output = new CharSequence[mKids.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = mKids.get(i).getName();
		}
		return output;
	}

	@Override
	public String getTileBlurb() {
		return null;
	}



}
