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

public enum Flag {
	urgent,
	warning,
	value;

	/**
	 * @param <T>
	 * @param enumType
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name){
		return (T) value;
	}
	
	public static Flag getValue(String value) {
        try {
            return valueOf(value);
        } catch (Exception e) {
            return Flag.value;
        }
    }

}


