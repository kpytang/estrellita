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

package edu.uci.ics.star.estrellita.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.star.estrellita.customview.DateBasedExpandableListAdapter.SortingOrder;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;

public class CollectionUtils {
	
	// general-purpose call to sort a collection (like a set)
	public static <T extends Comparable<? super T>> List<T> getSetAsSortedList(Set<T> s) {
	  List<T> list = new ArrayList<T>(s);
	  Collections.sort(list);
	  return list;
	}
	
	public static <T extends Comparable<? super T>> T findMaxInList(List<T> l) {
		if (l.size() == 0) {
			return null;
		}
		List<T> list = new ArrayList<T>(l);
		Collections.sort(list);
		return list.get(list.size()-1);
	}
	
	public static <T extends Comparable<? super T>> T findMinInList(List<T> l) {
		if (l.size() == 0) {
			return null;
		}
		List<T> list = new ArrayList<T>(l);
		Collections.sort(list);
		return list.get(0);
	}
	
	public static float findSmallestPostiveFloatInList(List<Float> l) {
		if (l.size() == 0) {
			return 0;
		}
		List<Float> list = new ArrayList<Float>(l);
		Collections.sort(list);
		int i = 0;
		for (i=0; i<list.size(); i++) {
			if (list.get(i) > 0) {
				break;
			}
		}
		if (i<list.size()) {
			return list.get(i);
		}
		return 0;
	}
	
	public static CharSequence[] getListAsCharSequence(List<String> strings) {
		CharSequence[] chars = new CharSequence[strings.size()];
		for(int i=0; i<strings.size(); i++) {
			chars[i] = strings.get(i);
		}
		return chars;
	}

	public static <T extends Indicator> Map<Date, List<T>> getListAsDateBasedMap(List<T> list) {
		Map<Date, List<T>> dateBasedMap = new HashMap<Date, List<T>>();
		// use dates as keys for hashmap
		Date d;
		for (int i=0; i<list.size(); i++) {
			if (list.get(i) instanceof Appointment) {
				d = ((Appointment) list.get(i)).getDate();
				d = DateUtils.getStartOfDay(d);
			}
			else {
				d = list.get(i).getDateTime();
			}
			if (!dateBasedMap.containsKey(d)) {
				dateBasedMap.put(d, new ArrayList<T>());
			}
			dateBasedMap.get(d).add(list.get(i));
		}
		return dateBasedMap;
	}
	
	public static <T extends Indicator> Map<Date, List<T>> getListAsSortedDateBasedMap(List<T> list, SortingOrder timeSortOrder) {
		Map<Date, List<T>> dateBasedMap = getListAsDateBasedMap(list);
		List<Date> dateKeys = new ArrayList<Date>(dateBasedMap.keySet());
		for (Date d: dateKeys) {
			List<T> indicators = dateBasedMap.get(d);
			Collections.sort(indicators);
			if (timeSortOrder == SortingOrder.DESCENDING) {
				Collections.reverse(indicators);
			}
			dateBasedMap.remove(d);
			dateBasedMap.put(d, indicators);
		}
		return dateBasedMap;
	}
	
	public static List<String> convertIntegerListToStrings(List<Integer> integers) {
		List<String> strings = new ArrayList<String>();
		for (int i=0; i<integers.size(); i++) {
			strings.add(integers.get(i).toString());
		}
		return strings;
	}
	
	public static <T extends Object> String toString(List<T> list, String delimiter) {
		String s = "";
		for (int i=0; i<list.size(); i++) {
			s += list.get(i).toString();
			s += delimiter;
		}
		// chop off the extra delimiter, which will happen if there's a non-empty list
		if (s.length() > 0) {
			s = s.substring(0, s.length()-1);
		}
		return s;
	}
}
