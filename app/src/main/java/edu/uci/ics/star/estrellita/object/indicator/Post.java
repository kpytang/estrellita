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

import java.sql.Timestamp;

import android.graphics.Bitmap;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Flag;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.object.Baby.Gender;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class Post extends Indicator{

	public enum PostType {P, C, VC};

	private String msg;
	private PostType type;
	private Integer commentTo;

	public Post(CommonData common, String msg, String type, Integer commentTo){
		super(common);
		this.msg = msg;
		if(commentTo == null){
			this.type = PostType.P;
		} else {
			this.type = PostType.C;
		}
		this.setCommentTo(commentTo);
	}

	public Post(User user, String post) {
		this(user, null, null, post, DateUtils.getTimestamp(), null);
	}

	public Post(User user, String post, Integer commentTo) {
		this(user, null, null, post, DateUtils.getTimestamp(), commentTo);
	}

	public Post(User user, Integer postId, Integer localId, String msg, Timestamp timestamp, Integer commentTo){
		this(makeCommon(user, postId, localId, null, timestamp),msg, null, commentTo);
	}

	private static CommonData makeCommon(User user, Integer postId, Integer localId, Bitmap image, Timestamp timestamp) {
		return new CommonData(postId, localId, user.getId(), user.getCurrentKid().getId(), timestamp, image, Flag.value);
	}

	public String getMsg() {
		return msg;
	}

	public String getModifiedMsg(Baby baby) {
		String s = msg.replaceAll("\\[user\\]", baby.getName());
		if (getCommonData().getIdUser() == 0) {
			s = s.replaceAll("your baby", baby.getName());
			s = s.replaceAll("Your baby", baby.getName());
			String genderString = "him";
			if (baby.getGender() == Gender.FEMALE) {
				genderString = "her";
			}
			s = s.replaceAll("Him/Her", StringUtils.capitalize(genderString));
			s = s.replaceAll("him/her", genderString.toLowerCase());
			s = s.replaceAll("Him/her", StringUtils.capitalize(genderString));
			
			genderString = "his";
			if (baby.getGender() == Gender.FEMALE) {
				genderString = "her";
			}
			s = s.replaceAll("His/Her", StringUtils.capitalize(genderString));
			s = s.replaceAll("his/her", genderString.toLowerCase());
			s = s.replaceAll("His/her", StringUtils.capitalize(genderString));
			
			genderString = "he";
			if (baby.getGender() == Gender.FEMALE) {
				genderString = "she";
			}
			s = s.replaceAll("He/She", StringUtils.capitalize(genderString));
			s = s.replaceAll("he/she", genderString.toLowerCase());
			s = s.replaceAll("He/she", StringUtils.capitalize(genderString));
		}
		return s;
	}

	public Integer getCommentTo() {
		return commentTo;
	}

	public void setType(PostType type) {
		this.type = type;
	}

	public String getType() {
		return type.name();
	}

	public void setCommentTo(Integer commentTo) {
		if(commentTo != null) {
			this.commentTo = commentTo;
			setType(PostType.C);
		}
	}

	@Override
	public String getTileBlurb() {
		return DateUtils.formatReadableDate(getCommonData().getTimestamp()) + "\n" + msg ;
	}
}