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

package edu.uci.ics.star.estrellita.customview;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

/**
 * Maintains an HashMap of appointments (key: Date, value: ArrayList<Appointment>) 
 */
public class AppointmentsExpandableListAdapter extends DateBasedExpandableListAdapter<Appointment> implements Filterable {
	
    private Bitmap mNotesIcon;
    private AppointmentsFilter mFilter;
      
	public AppointmentsExpandableListAdapter(Context context, Map<Date, List<Appointment>> mapData, SortingOrder groupOrder, SortingOrder childrenOrder) {
    	super(context, mapData, groupOrder, childrenOrder);
    	
        // icons bound to (some of) the rows (those with notes)
    	mNotesIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.notes_icon);
    }
	
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
    	
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.appointment_list_item, null);
        } 

        TextView tv = (TextView) convertView.findViewById(R.id.appointment_count);
        tv.setText("#" + Integer.toString(getAppointmentNumber(groupPosition, childPosition)));
        Appointment a = (Appointment)getChild(groupPosition, childPosition);
        tv = (TextView) convertView.findViewById(R.id.doctor_info);
        tv.setText(a.getDoctorInfo());
        tv.setTypeface(mBold);
    	tv = (TextView) convertView.findViewById(R.id.time);
    	tv.setText(DateUtils.getDateAsString(a.getStartTime(), DateUtils.APPOINTMENT_TIME_FORMAT));
    	tv.setTypeface(mRegular);
    	tv = (TextView) convertView.findViewById(R.id.location);
    	tv.setVisibility(View.GONE);
    	ImageView apptNoteIcon = (ImageView) convertView.findViewById(R.id.notes_icon);
    	if (a.getCommonData().hasNotes()) {
     		apptNoteIcon.setImageBitmap(mNotesIcon);
        }

        return convertView;
    }
    
    private int getAppointmentNumber(int groupIndex, int childIndex) {
    	return previousGroupSize(groupIndex)+childIndex+1;
    }
    
    private int previousGroupSize(int groupIndex) {
    	int size = 0;
    	if (groupIndex==0) {
    		return 0;
    	}
    	for (int i=0; i<groupIndex; i++) {
    		Date key = DateUtils.parseString(mDateLabels[i], DATE_FORMAT_STRING);
    		size += mDateBasedMap.get(key).size();
    	}
    	return size;
    }
    
	@Override
	public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new AppointmentsFilter();
        }
        return mFilter;

	}

    /**
     * A filter constrains the content of the listadapter with a prefix. 
     * Each item that does not start with the supplied prefix is removed from the list.
     */
    private class AppointmentsFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                Map<Date, List<Appointment>> map = mDateBasedMap;
                results.values = map;
                results.count = map.keySet().size();
            } 
            else {
                String query = prefix.toString().toLowerCase();
                HashMap<Date, ArrayList<Appointment>> filteredAppointmentsMap = new HashMap<Date, ArrayList<Appointment>>();
                
                for(Date d: mDateBasedMap.keySet()) {
                	List<Appointment> list = mDateBasedMap.get(d);
                	int count = list.size();
                
                	for (int i=0; i<count; i++) {
                		Appointment a = list.get(i);
                		
                		// first, match against doctor's info 
                		if (StringUtils.isPrefixMatch(a.getDoctorInfo().toLowerCase(), query)) {
                			if (!filteredAppointmentsMap.containsKey(d)) {
                				filteredAppointmentsMap.put(d, new ArrayList<Appointment>());
                			}
                			filteredAppointmentsMap.get(d).add(a);
                		}
                		// next, match against location
                		if (StringUtils.isPrefixMatch(a.getLocation().toLowerCase(), query)) {
                			if (!filteredAppointmentsMap.containsKey(d)) {
                				filteredAppointmentsMap.put(d, new ArrayList<Appointment>());
                			}
                			filteredAppointmentsMap.get(d).add(a);
                		}
                		// lastly, match against time
	            		if (StringUtils.isPrefixMatch(a.getTimeRange().toLowerCase(), query)) {
	            			if (!filteredAppointmentsMap.containsKey(d)) {
                				filteredAppointmentsMap.put(d, new ArrayList<Appointment>());
                			}
                			filteredAppointmentsMap.get(d).add(a);
                 		}
                	}
                }
                
                results.values = filteredAppointmentsMap;
                results.count = filteredAppointmentsMap.keySet().size();
            }
            return results;
        }

        /* (non-Javadoc)
         * @see android.widget.Filter#publishResults(java.lang.CharSequence, android.widget.Filter.FilterResults)
         */
        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
        	mDateBasedMap = (Map<Date, List<Appointment>>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }       
    }

}