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

package edu.uci.ics.star.estrellita.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.AppointmentsExpandableListAdapter;
import edu.uci.ics.star.estrellita.customview.DateBasedExpandableListAdapter.SortingOrder;
import edu.uci.ics.star.estrellita.customview.DateBasedList;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class AppointmentsTabs extends TileActivity<Appointment> implements TabHost.TabContentFactory {
	private static final String PAST = "past";
	private static final String UPCOMING = "upcoming";
	private static final int ADD_APPOINTMENT_FORM_ID = 0;
	private static final int VIEW_APPOINTMENT_FORM_ID = 1;
	private static final int UPDATE_PROGRESS_DIALOG_ID = 2;

	private TabHost mTabHost;
	private AppointmentsList mUpcomingApptsListView, mPastApptsListView;
	private ViewGroup mUpcomingApptsView, mPastApptsView;
	private List<Appointment> mUpcomingAppts, mPastAppts;	

	private UpdateReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs_overview);

		initializeApptListView();

		// initialize header
		setActivityHeader("Appointments", true, Tile.APPOINTMENTS);
		setButtonHeader("add new appointment");
		RelativeLayout buttonHeader = (RelativeLayout) this.findViewById(R.id.button_layout);
		buttonHeader.setOnClickListener(mAddNewAppointment);

		mTabHost = (TabHost) this.findViewById(R.id.tabs_host);
		// need this because we are not extended TabActivity
		mTabHost.setup();

		// first tab
		TabHost.TabSpec spec = mTabHost.newTabSpec(UPCOMING);
		spec.setIndicator("Today/Upcoming");
		spec.setContent(this);
		mTabHost.addTab(spec);

		// second tab
		spec = mTabHost.newTabSpec(PAST);
		spec.setIndicator("Past");
		spec.setContent(this);
		mTabHost.addTab(spec);

		// customize tabs text & height
		TabWidget tabWidget = mTabHost.getTabWidget();
		Resources res = this.getResources();
		ViewGroup layout = (ViewGroup)tabWidget.getChildAt(0);
		TextView tv = ((TextView)layout.getChildAt(1));
		tv.setTextColor(res.getColorStateList(R.color.tab_title));
		tv.setTextSize(res.getDimension(R.dimen.font_small));
		tv.setTypeface(bold);
		layout = (ViewGroup)tabWidget.getChildAt(1);
		tv = ((TextView)layout.getChildAt(1));
		tv.setTextColor(res.getColorStateList(R.color.tab_title));
		tv.setTextSize(res.getDimension(R.dimen.font_small));
		tv.setTypeface(bold);
		((ViewGroup)tabWidget.getChildAt(0)).getLayoutParams().height = 60;
		((ViewGroup)tabWidget.getChildAt(0)).getLayoutParams().width = 125;
		((ViewGroup)tabWidget.getChildAt(1)).getLayoutParams().height = 60;
	}

	private void initializeApptListView() {
		openDatabase();
		mUpcomingAppts = getAppointmentsFromDatabase(UPCOMING);
		mUpcomingApptsListView = new AppointmentsList(this, mUpcomingAppts, SortingOrder.ASCENDING, SortingOrder.ASCENDING);
		mUpcomingApptsListView.collapseGroupForToday();

		mPastAppts = getAppointmentsFromDatabase(PAST);		
		mPastApptsListView = new AppointmentsList(this, mPastAppts, SortingOrder.DESCENDING, SortingOrder.DESCENDING);
	}

	private ArrayList<Appointment> getAppointmentsFromDatabase(String tag) {
		ArrayList<Appointment> appts = new ArrayList<Appointment>();
		Appointment[] a;
		openDatabase();
		if (tag.toLowerCase().equals(UPCOMING)) {
			a = (Appointment[]) mDatabase.getAppointmentTable().getUpcomingAppointments(mBaby.getId(), DateUtils.getStartOfDay());
		}
		else if (tag.toLowerCase().equals(PAST)) {
			a = (Appointment[]) mDatabase.getAppointmentTable().getPastAppointments(mBaby.getId(), DateUtils.getStartOfDay());
		}
		else {
			a = new Appointment[0];
		}
		for(int i=0; i<a.length; i++) {
			appts.add(a[i]);
		}
		return appts;
	}

	/**
	 * creates the tab based on the tab titles
	 * (non-Javadoc)
	 * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
	 */
	public View createTabContent(String tag) {
		// show a different view if there aren't any appointments, otherwise show the appropriate list of upcoming/past appointments
		ViewGroup currentView;
		if (tag.toLowerCase().equals(UPCOMING)) {
			if(mUpcomingApptsView == null) {
				mUpcomingApptsView = new LinearLayout(this);
			}
			currentView = mUpcomingApptsView;
			currentView.removeAllViews();
			if (mUpcomingAppts.size() == 0) {
				TextView tv = new TextView(this);
				tv.setTypeface(null, Typeface.ITALIC);
				tv.setText("you have no upcoming appointments");
				currentView.addView(tv);
			}
			else {
				currentView.addView(mUpcomingApptsListView);
			}
		}
		else {
			if(mPastApptsView == null) {
				mPastApptsView = new LinearLayout(this);
			}
			currentView = mPastApptsView;
			currentView.removeAllViews();
			if (mPastAppts.size() == 0) {
				TextView tv = new TextView(this);
				tv.setTypeface(null, Typeface.ITALIC);
				tv.setText("you have no past appointments");
				currentView.addView(tv);
			}
			else {
				currentView.addView(mPastApptsListView);
			}
		}
		return currentView;
	}

	View.OnClickListener mAddNewAppointment = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(AppointmentsTabs.this, AppointmentForm.class);
			intent.putExtra(TileActivity.BABY, mBaby);
			startActivityForResult(intent, ADD_APPOINTMENT_FORM_ID);
		}
	};

	public class AppointmentsList extends DateBasedList<Appointment> {
		private Appointment mSelectedAppointment;
		private SortingOrder mGroupSortingOrder, mTimeSortingOrder;

		public AppointmentsList(Context context, List<Appointment> appointmentList, SortingOrder groupOrder, SortingOrder childrenOrder) {
			super(context);

			mSelectedAppointment = null;
			mGroupSortingOrder = groupOrder;
			mTimeSortingOrder = childrenOrder;

			mListAdapter = new AppointmentsExpandableListAdapter(this.getContext(), getMapData(appointmentList), mGroupSortingOrder, mTimeSortingOrder);
			setAdapter(mListAdapter);
			setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					Appointment a = (Appointment) mListAdapter.getChild(groupPosition,childPosition);
					Intent intent = new Intent(AppointmentsTabs.this, ViewAppointment.class);
					intent.putExtra(TileActivity.BABY, mBaby);
					intent.putExtra("appointment", a);
					startActivityForResult(intent, VIEW_APPOINTMENT_FORM_ID);
					return false;
				}
			});

			setOnDeleteSelectedListener(new OnDeleteSelectedActionListener<Appointment>() {
				public void OnContextItemSelectedAction(Appointment a) {
					mSelectedAppointment = a;
					new AlertDialog.Builder(AppointmentsTabs.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.delete_this)
					.setMessage(R.string.really_delete)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// we'll delete this from the database, log it, and then refresh the listviews
							openDatabase();
							mDatabase.delete(mSelectedAppointment);
							mDatabase.logDelete(getIntent(), mSelectedAppointment);
							Toast.makeText(AppointmentsTabs.this, "appointment has been deleted", Toast.LENGTH_LONG).show();
							refreshListViews();
						}

					})
					.setNegativeButton(R.string.no, null)
					.show();
				}
			});

			expandAllGroups();  
		}

		private Map<Date, List<Appointment>> getMapData(List<Appointment> appointmentList) {
			return CollectionUtils.getListAsSortedDateBasedMap(appointmentList, mTimeSortingOrder);
		}

		private void collapseGroupByDate(Date d, boolean onlyIfAllApptsCompleted) {
			List<Date> dates = mListAdapter.getMapKeys();
			switch(mGroupSortingOrder) {
			case ASCENDING:
				Collections.sort(dates);
				break;
			case DESCENDING:
				Collections.reverse(dates);
				break;
			}
			for (int i=0; i<dates.size(); i++) {
				if (DateUtils.getStartOfDay(dates.get(i)).equals(DateUtils.getStartOfDay(d))) {
					// so we found the date key that matches the given date
					// now we check if there are appointments for this date that haven't occurred yet, then we won't collapse it
					// otherwise, it will collapse
					Date now = DateUtils.getTimestamp();
					if (onlyIfAllApptsCompleted) {
						boolean collapse = true;
						for (int j=0; j<mListAdapter.getChildrenCount(i); j++) {
							Appointment a = mListAdapter.getChild(i, j);
							if (DateUtils.combineDateAndTime(a.getDate(), a.getStartTime()).after(now)) {
								collapse = false;
							}
						}
						if (collapse) {
							collapseGroup(i);
						}
					}
					else {
						collapseGroup(i);
					}
					break;
				}
			}
		}

		public void collapseGroupForToday() {
			collapseGroupByDate(DateUtils.getTimestamp(), true);
		}

		public void refreshMapData(List<Appointment> appointmentList) {
			mListAdapter = new AppointmentsExpandableListAdapter(this.getContext(), getMapData(appointmentList), mGroupSortingOrder, mTimeSortingOrder);
			setAdapter(mListAdapter);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case UPDATE_PROGRESS_DIALOG_ID:
			ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("adding appointment...");
            dialog.setIndeterminate(true);
            return dialog;
		}
		return null;
	}

	// checks for results from the 'adding new appointment' form
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data != null) {
			data.setClassName(this, this.getClass().getName());
		}

		switch (requestCode) {
		case ADD_APPOINTMENT_FORM_ID:
			if (resultCode == RESULT_OK && data != null){
				// get appointment info from intent
				Appointment a = (Appointment) data.getParcelableExtra("appointment");

				if (a != null) {
					//register the receivers to catch the notice from the update
					mReceiver = new UpdateReceiver();
					registerReceiver(mReceiver, new IntentFilter(Utilities.ACTION_UPDATE_MAIN));
					showDialog(UPDATE_PROGRESS_DIALOG_ID);
					
					// first insert the appointment.
					openDatabase();
					mDatabase.insertSingle(a);
					// then log that event
					data.setClassName(this, this.getClass().getName());
					mDatabase.logInsert(data, a);

					// then pull from the database again to re-populate the listview
					mIndicator = a;
					
				}
			}
			break;
		case VIEW_APPOINTMENT_FORM_ID:
			// we need to update the listview if we've update the appointment in any way
			Date oldkey = new Date(ViewAppointment.staticDate);
			if(oldkey.getTime() != -1){
				// if there's an old key, it means that the (old) appointment was deleted from the database (in ViewAppointments)
				// so we just need to refresh the listview now and pull the new list from the database
				refreshListViews();
			}
			// if staticDate is -1, it means we're adding a new appointment because we added a follow-up appointment (resultCode = CANCELED)
			else {
				if ((resultCode == RESULT_CANCELED) && (data != null)) {
					// get appointment info from intent
					Appointment a = (Appointment) data.getParcelableExtra("appointment");

					if (a != null) {
						//register the receivers to catch the notice from the update
						mReceiver = new UpdateReceiver();
						registerReceiver(mReceiver, new IntentFilter(Utilities.ACTION_UPDATE_MAIN));
						showDialog(UPDATE_PROGRESS_DIALOG_ID);
						
						// first insert the appointment in the database
						openDatabase();
						mDatabase.insertSingle(a);
						// then log that insertion
						data.setClassName(this, this.getClass().getName());
						mDatabase.logInsert(data, a);

						// then update the listview
						mIndicator = a;
					}
				}
			}
			break;
		}
	}
	
	private void setTabByAppointment(Appointment a) {
		if (DateUtils.combineDateAndTime(a.getDate(), a.getStartTime()).after(DateUtils.getStartOfDay())) {
			mTabHost.setCurrentTabByTag(UPCOMING);
		}
		else {
			mTabHost.setCurrentTabByTag(PAST);
		}
	}

	private void refreshListViews() {
		initializeApptListView();
		if (mPastApptsListView == null) {
			mTabHost.setCurrentTabByTag(PAST);
		}
		createTabContent(PAST);
		mPastApptsView.invalidate();
		
		if (mUpcomingApptsListView == null) {
			mTabHost.setCurrentTabByTag(UPCOMING);
		}
		createTabContent(UPCOMING);
		mUpcomingApptsView.invalidate();
	}
	
	// this is for catching when the updates are done
	private class UpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateHandler.sendEmptyMessage(0);
		}
	}
	
	// define the Handler that receives messages from the thread and update the progress
	final Handler updateHandler = new Handler() {
		public void handleMessage(Message msg) {
			try{
				removeDialog(UPDATE_PROGRESS_DIALOG_ID);
				refreshListViews();
				setTabByAppointment(mIndicator);
				unregisterReceiver(mReceiver);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
}