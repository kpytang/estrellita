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

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

public class AppointmentsListViewDialog extends ListViewDialog<Appointment> {

	public AppointmentsListViewDialog(Context context, String title, List<Appointment> appointments, Intent intent) {
		super(context, CustomViews.createDialogTitleView(context, title), appointments);
		setCancelable(true);
		
		mIntent = intent;
	}
	
	// Called when the dialog is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mListView = (ListView) mDialogLayout.findViewById(R.id.data_listview);
		refreshListView();
		super.onCreate(savedInstanceState);
	}
	
	private void refreshListView() {
		if (mListView == null) {
			mListView = (ListView) mDialogLayout.findViewById(R.id.data_listview);
		}
		mListView.setAdapter(new AppointmentsTwoLineListAdapter(mContext, mItems));
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Appointment a = (Appointment) parent.getItemAtPosition(position);
				mIntent.removeExtra("appointment");
				mIntent.putExtra("appointment", a);
				mIntent.removeExtra("date");
				mIntent.putExtra("date", a.getDate());
				startActivity(mIntent);
				dismiss();
			}
		});
	}
	
	public void updateAppointments(List<Appointment> appointments) {
		mItems = appointments;
		refreshListView();
	}
	
	private class AppointmentsTwoLineListAdapter extends TwoLineListAdapter<Appointment> {
		public AppointmentsTwoLineListAdapter(Context context, List<Appointment> appointments) {
			super(context, R.layout.two_line_list_item_with_icon, R.drawable.notes_icon, appointments);
			String dateString, concernsString = "";
			for (int i=0; i<appointments.size(); i++) {
				Appointment a = appointments.get(i);
				dateString = DateUtils.getDateAsString(a.getDate(), DateUtils.APPOINTMENT_SHORT_DATE_FORMAT) + " @ " + 
				DateUtils.getDateAsString(a.getStartTime(), DateUtils.APPOINTMENT_TIME_FORMAT);
				mFirstRows.add(dateString);

				if (a.isNoConcerns()) {
					mSecondRows.add(null);
				}
				else {
					if (a.isConcernedAboutDiapers()) {
						concernsString = StringUtils.addToCommaDelimitedList("diapers", concernsString);
					}
					if (a.isConcernedAboutBabyMoods()) {
						concernsString = StringUtils.addToCommaDelimitedList("baby moods", concernsString);
					}
					if (a.isConcernedAboutCharts()) {
						concernsString = StringUtils.addToCommaDelimitedList("charts", concernsString);
					}
					if (a.isConcernedAboutWeight()) {
						concernsString = StringUtils.addToCommaDelimitedList("weight", concernsString);
					}
					mSecondRows.add("concerns: " + concernsString);
				}
				
				if (a.getCommonData().hasNotes()) {
					mImageFlags.add(true);
				}
				else {
					mImageFlags.add(false);
				}
				
				mTags.add(a.getId().toString());
			}
		}
	}
}