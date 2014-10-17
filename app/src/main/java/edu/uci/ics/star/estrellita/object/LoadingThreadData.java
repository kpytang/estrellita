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


import java.util.HashMap;
import java.util.LinkedList;

public abstract class LoadingThreadData {

		private HashMap<String, String> extras;
		private LinkedList<QueuedAction> queuedActions;

		public LoadingThreadData(){
			extras = new HashMap<String, String>();
			queuedActions = new LinkedList<QueuedAction>();
		}


		public void putExtra(String key, String value){
			extras.put(key, value);
		}

		/**
		 * Returns the value of the mapping with the specified key.
		 * @param key the key. 
		 * @return the value of the mapping with the specified key, or null if no mapping for the specified key is found. 

		 */
		public String getExtra(String key){
			return extras.get(key);
		}
		
		public abstract boolean onCompleteAction();
		
		public abstract boolean onDoWork();
		
		public abstract boolean shouldProceed();

		public LinkedList<QueuedAction> getQueuedActions() {
			return queuedActions;
		}
		
		public boolean isEmpty(){
			return queuedActions.isEmpty();
		}
		
		public QueuedAction poll(){
			return queuedActions.poll();
		}
		
		public void add(QueuedAction a){
			queuedActions.add(a);
		}



	}