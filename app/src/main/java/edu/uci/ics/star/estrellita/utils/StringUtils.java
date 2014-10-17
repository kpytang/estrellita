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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	/**
	 * checks if data contains the query by either: starting with the query (prefix) as a whole or
	 * having one of its words start with the query after being
	 * NOTE: search is NOT case-sensitive
	 * @param data
	 * @param prefix
	 * @return
	 */
	public static boolean isPrefixMatch(String data, String query) {
		if (data.startsWith(query)) {
			return true;
		}
		else {
			String[] words = data.split(" ");        		
			for (int i=0; i<words.length; i++) {
				if (words[i].startsWith(query)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * capitalizes the first letter of each word in the given string and the rest is lowercase
	 * @param s
	 * @return
	 */
	public static String capitalize(String s)
	{
		s = s.trim().toLowerCase();
		String[] tokens = s.split("\\s");
		s = "";
		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i].length()>1) {
				s += tokens[i].substring(0,1).toUpperCase() + tokens[i].substring(1) + " ";
			}
			else
				s += tokens[i].toUpperCase();
		}
		return s.trim();
	}


	/**
	 * returns a string that prints out the right plural/singular version
	 * @param i
	 * @param singular
	 * @param plural
	 * @return
	 */
	public static String pluralize(int i, String singular, String plural) {
		if (i == 1) {
			return Integer.toString(i) + " " + singular;
		}
		else {
			return Integer.toString(i) + " " + plural;
		}
	}

	/**
	 * returns a phone number formatted as: (123) 456-7890 or 456-7890
	 * @param phone
	 * @return
	 */
	public static String cleanUpPhoneNumber(String phone) {
		String prettyPhone = phone.replace("(","");
		prettyPhone = prettyPhone.replace(")","");
		prettyPhone = prettyPhone.replace("-","");
		prettyPhone = prettyPhone.replace(" ","");
		prettyPhone = prettyPhone.trim();

		// should only be numbers at this point
		// format: (123) 456-7890 or 456-7890
		if (prettyPhone.length() == 11) {
			prettyPhone = prettyPhone.substring(1);
		}
		if (prettyPhone.length() == 10) {
			prettyPhone = "(" + prettyPhone.substring(0,3) + ") " + prettyPhone.substring(3,6) + "-" + prettyPhone.substring(6);
		}
		else {
			prettyPhone = prettyPhone.substring(0,3) + "-" + prettyPhone.substring(3);
		}
		return prettyPhone;
	}

	/**
	 * returns a phone number formatted as: (123) 456-7890 or 456-7890
	 * @param phone
	 * @return
	 */
	public static String extractOnlyNumbers(String phone) {
		String s = "";
		if (phone != null) {
			for (int i=0; i<phone.length(); i++) {
				if (phone.charAt(i) >= '0' && phone.charAt(i) <= '9') {
					s += phone.charAt(i);
				}
			}
		}
		return s;
	}

	public static void trimTrailingZeros(String number) {
		if(number.contains(".")) {
			number = number.replaceAll("0*$", "");
			if(number.charAt(number.length() - 1) == '.') {
				number = number.substring(0, number.length() - 1);
			}
		}
	}

	public static String addToCommaDelimitedList(String newString, String baseString) {
		if (baseString.length() > 0) {
			baseString += ", " + newString;
		}
		else {
			baseString += newString;
		}
		return baseString;
	}

	/*
	 * returns either a string of the specified length, or it returns the maximum length that is at most that specified length (which is good for shorter strings)
	 */
	public static String subString(String s, int startIndex, int maxLength) {
		int length = s.length();
		if (length > maxLength) {
			return s.substring(startIndex, startIndex+maxLength);
		}
		else {
			return s.substring(startIndex, startIndex+length);
		}
	}

	public static boolean containsOnlyNumbers(String s) {
		String numbers = "";
		if (s != null)  {
			numbers = extractOnlyNumbers(s);
			for (int i = 0; i < numbers.length(); i++) {
				// if we find a non-digit character, then return false
				if (!Character.isDigit(numbers.charAt(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static String shortenStringWithEllipsis(String s, int maxLength) {
		if (s.length() < maxLength) {
			return s;
		}
		
		String ellipsis = " ...";
		maxLength = maxLength - ellipsis.length();
		int length = s.length();
		while (length > maxLength) {
			int spaceIndex = s.lastIndexOf(" ");
			if (spaceIndex != -1) {
				s = s.substring(0, spaceIndex);
				length = s.length();
			}
			else {
				s = s.substring(0, maxLength);
			}
		}
		s += ellipsis;
		return s;
	}
	
	public static List<String> extractPhoneNumbers(String input) {				
		List<String> result = new ArrayList<String>();

		Pattern pattern = Pattern.compile(
				"(\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4})");

		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			result.add(matcher.group());
		}

		return result;
	}
}
