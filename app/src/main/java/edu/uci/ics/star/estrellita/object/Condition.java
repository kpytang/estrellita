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

import java.util.ArrayList;
import java.util.List;

public class Condition {
	private List<Response> mResponses;
	private Integer mGoToQuestionId;
	
	public Condition() {
		mResponses = new ArrayList<Response>();
		mGoToQuestionId = null;
	}
	
	public Condition(int nextQuestionId) {
		mGoToQuestionId = nextQuestionId;
		mResponses = new ArrayList<Response>();
	}
	
	public void setGoToQuestionId(int id) {
		mGoToQuestionId = id;
	}
	
	public void setGoToQuestionId(String id) {
		try {
			mGoToQuestionId = Integer.parseInt(id);
		}
		catch (Exception e) {
		}
	}
	
	public int getGoToQuestionId() {
		return mGoToQuestionId;
	}
	
	public boolean isQuestionIdSet() {
		if (mGoToQuestionId != null) {
			return true;
		}
		return false;
	}
	
	public void addResponse(Response response) {
		if (mResponses != null) {
			mResponses.add(response);
		}
	}
	
	// we support an OR operation - if any of the provided responses are contained in the conditional responses, 
	// then we consider that a match
	public boolean containsResponse(List<Response> responses) {
		Response conditionalResponse;
		for (int i=0; i<mResponses.size(); i++) {
			conditionalResponse = mResponses.get(i);
			for (int j=0; j<responses.size(); j++) {
				if (responses.get(j).getData() == conditionalResponse.getData()) {
					return true;
				}
			}
		}
		return false;
	}
}
