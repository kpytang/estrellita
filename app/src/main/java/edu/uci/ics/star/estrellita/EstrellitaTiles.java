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

package edu.uci.ics.star.estrellita;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import uk.co.jasonfry.android.tools.ui.SwipeView;
import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.activity.AppointmentsTabs;
import edu.uci.ics.star.estrellita.activity.BabyMoodsOverview;
import edu.uci.ics.star.estrellita.activity.BondingOverview;
import edu.uci.ics.star.estrellita.activity.ContactInfo;
import edu.uci.ics.star.estrellita.activity.CustomChartsOverview;
import edu.uci.ics.star.estrellita.activity.DiapersOverview;
import edu.uci.ics.star.estrellita.activity.EstrellitaPreferences;
import edu.uci.ics.star.estrellita.activity.MyMoodsOverview;
import edu.uci.ics.star.estrellita.activity.SurveyForm;
import edu.uci.ics.star.estrellita.activity.SurveysOverview;
import edu.uci.ics.star.estrellita.activity.TileActivity;
import edu.uci.ics.star.estrellita.activity.Wall;
import edu.uci.ics.star.estrellita.activity.WeightOverview;
import edu.uci.ics.star.estrellita.customview.AppointmentRemindersDialog;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.db.table.IndicatorTable;
import edu.uci.ics.star.estrellita.exception.NoUserException;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.Baby.Gender;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.GenericIndicator;
import edu.uci.ics.star.estrellita.object.Indicator;
import edu.uci.ics.star.estrellita.object.LoadingThreadData;
import edu.uci.ics.star.estrellita.object.PromptedSpecialReminder;
import edu.uci.ics.star.estrellita.object.QueuedAction;
import edu.uci.ics.star.estrellita.object.SpecialReminder.ReminderType;
import edu.uci.ics.star.estrellita.object.UpdateCompleteAction;
import edu.uci.ics.star.estrellita.object.User;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.object.indicator.BondingSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Diaper;
import edu.uci.ics.star.estrellita.object.indicator.GenericSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.MoodMapSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.object.indicator.Weight;
import edu.uci.ics.star.estrellita.sharedprefs.api.ReminderPreferences;
import edu.uci.ics.star.estrellita.sharedprefs.api.UserPreferences;
import edu.uci.ics.star.estrellita.updateservice.OnBootReceiver;
import edu.uci.ics.star.estrellita.updateservice.SyncService;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;
import edu.uci.ics.star.estrellita.utils.WebUtils;

public class EstrellitaTiles extends Activity {
	private static final int LOGIN_ACTIVITY_ID = 0;
	private static final int APPOINTMENTS_ID = 1;
	private static final int DIAPERS_ID = 2;
	private static final int BONDING_ID = 3;
	private static final int BABYMOODS_ID = 4;
	private static final int CUSTOMCHARTS_ID = 5;
	// contains copyrighted materials
//	private static final int MYMOODS_ID = 6;
	private static final int WEIGHT_ID = 7;
	// contains copyrighted materials
//	private static final int SURVEYS_ID = 8;
	private static final int UPDATE_DIALOG = 9;
	private static final int WALL_ID = 11;

	// array of tile objects
	private List<Tile> mTiles;

	private static Database mDatabase;
	private static User mUser;
	private static List<Integer> mBabyIDs;
	private static List<Baby> mBabies;
	private static int mCurrentBabyID;
	private Baby mCurrentBaby;
	private static LoadingThread loadingThread;
	private TileAdapter mTileAdapter;
	private SwipeView mSwipeView;
	private LinearLayout mCurrentBabyLayout;
	private UpdateReceiver mReceiver;

	private Map<Baby, List<PromptedSpecialReminder>> mRemindersMap;
	private AppointmentRemindersDialog mAppointmentReminderDialog;

	private Date mLastWallView = null;
	private double dbKey = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mSwipeView = (SwipeView) findViewById(R.id.swipe_view);

		if(mDatabase == null){
			mDatabase = new Database(this);
			OnBootReceiver.startAlarm(this);
			ReminderPreferences.setIntialValues(this);
		}

		try{
			dbKey = mDatabase.open(dbKey);
			// check shared_prefs for user info
			int userID = UserPreferences.getLastUserId(this);
			// if there's a user, then: get the user object, get the kids info, start up the tiles
			if (userID != -1) {
				mBabyIDs = UserPreferences.getKidIds(this);
				mBabies = mDatabase.getBabyTable().getBabies(mBabyIDs, this);

				mUser = mDatabase.getUserTable().getUserAndKids(userID, mBabyIDs, this, mDatabase.getBabyTable());
				// more initializing of reminders (since this depends on us grabbing the user information, we have to do this separate from the other reminder preferences)
				ReminderPreferences.setInitialSurveyDate(this);

				mCurrentBabyID = mBabyIDs.get(0);
				mCurrentBaby = mDatabase.getBabyTable().getBaby(mCurrentBabyID, this);

				setupBabyHeaders(); 
				updateCurrentKidByIndex(0);
				updateInbox();

				mSwipeView.setOnPageChangedListener(new OnPageChangedListener() {
					@Override
					public void onPageChanged(int oldPage, int newPage) {
						try {
							updateCurrentKidByIndex(newPage);
							updateInbox();
						} catch (Exception e) {
							Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
							// think i got an exception for one baby. hopefully this will fix it...
						}
					}
				});

				// sets up all the ODL tiles & displays it as a grid
				setupTiles();
				GridView gridview = (GridView) findViewById(R.id.tiles_gridview);
				mTileAdapter = new TileAdapter(this);
				gridview.setAdapter(mTileAdapter);

				mDatabase.close(dbKey);
			}
			// else we couldn't find user in shared_prefs, so we should prompt for login
			else {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			}
		} 
		catch(NoUserException NUE){
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}
	}

	// set up inbox count
	private void updateInbox() {
		Log log = (Log) mDatabase.getLogTable().getLastLogForPost(mCurrentBabyID);
		Date d = DateUtils.getEarliestDate();
		if (log != null) {
			d = log.getCommonData().getTimestamp();
			mLastWallView = d;
		}
		else {
			mLastWallView = null;
		}
		Integer count = mDatabase.getPostTable().getPostCountSince(mCurrentBabyID, d);
		RelativeLayout inboxCount = (RelativeLayout) mCurrentBabyLayout.findViewById(R.id.inbox_count_holder);
		ImageView iv;
		if (count > 0) {
			iv = (ImageView) mCurrentBabyLayout.findViewById(R.id.inbox_background);
			iv.setImageResource(R.drawable.envelope);
			iv = (ImageView) mCurrentBabyLayout.findViewById(R.id.inbox_exclaim);
			iv.setVisibility(View.VISIBLE);
			inboxCount.setVisibility(View.VISIBLE);
			TextView tv = (TextView) mCurrentBabyLayout.findViewById(R.id.inbox_count);
			tv.setText(count.toString());
		}
		else {
			iv = (ImageView) mCurrentBabyLayout.findViewById(R.id.inbox_background);
			if (mCurrentBaby.getGender() == Gender.FEMALE) {
				iv.setImageResource(R.drawable.envelope_lightpink);
			} 
			else {
				iv.setImageResource(R.drawable.envelope_magicblue);
			}
			iv = (ImageView) mCurrentBabyLayout.findViewById(R.id.inbox_exclaim);
			iv.setVisibility(View.GONE);
			inboxCount.setVisibility(View.GONE);
		}
	}

	private void updateCurrentKidByIndex(int index) {
		if(index == -1){
			index = 0;
		}
		mUser.setCurrentKidIndex(index);
		mCurrentBaby = mUser.getCurrentKid();
		mCurrentBabyID = mCurrentBaby.getId();
		if (mTileAdapter != null) {
			mTileAdapter.notifyDataSetChanged();
		}
		mCurrentBabyLayout = (LinearLayout) mSwipeView.getChildContainer().getChildAt(index);
	}

	private void updateCurrentKidById(int id) {
		if(id == -1){
			id = 0;
		}
		try {
			int kidIndexById = mUser.getKidIndexById(id);
			mSwipeView.scrollToPage(kidIndexById);
			updateCurrentKidByIndex(kidIndexById);
		} catch (NoUserException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	protected void onStart() {
		super.onStart();
		dbKey = mDatabase.open(dbKey);
		mDatabase.heartbeat(mUser.getId(), mCurrentBabyID, getIntent());
		
		checkAppointmentReminders();

		profilePhotosRefresh();
		mDatabase.close(dbKey);
	}

	private void checkAppointmentReminders() {
		if ( (mAppointmentReminderDialog == null) || (!mAppointmentReminderDialog.isShowing()) ) {

			mRemindersMap = new HashMap<Baby, List<PromptedSpecialReminder>>();

			// check to see if there are pre-appointment surveys that are needed
			// step through each one to see if there are un-completed pre-appointment surveys for each appt
			for (Baby baby: mBabies) {
				dbKey = mDatabase.open(dbKey);
				mRemindersMap.put(baby, new ArrayList<PromptedSpecialReminder>());

				// grab all the appointments within the reminder timeframe
				int reminderWindow = ReminderPreferences.getAppointmentReminder(this);
				Appointment[] appts = mDatabase.getAppointmentTable().getNextAppointments(baby.getId(), reminderWindow);
				PromptedSpecialReminder reminder = null;
				for (Appointment appt: appts) {
					reminder = new PromptedSpecialReminder(ReminderType.APPOINTMENT);
					reminder.addAppointment(appt);
					mRemindersMap.get(baby).add(reminder);
				}

				// grab all the pre-appt reminders within reminder window
				reminderWindow = ReminderPreferences.getPreAppointmentReminder(this);
				appts = mDatabase.getAppointmentTable().getNextAppointments(baby.getId(), reminderWindow);
				for (Appointment appt: appts) {
					// check pre-appt surveys for these UPCOMING appointments
					GenericSurvey survey = (GenericSurvey) mDatabase.getPreAppointmentSurveyTable().getSurveyForBaby(baby.getId(), appt.getId());
					// if survey doesn't exist, then it has not been completed yet and we should reminder user
					if (survey == null) {
						reminder = new PromptedSpecialReminder(ReminderType.PRE_APPT_SURVEY);
						reminder.addAppointment(appt);
						mRemindersMap.get(baby).add(reminder);
					}
				}

				// grab all the post-appt reminders within the reminder window
				reminderWindow = ReminderPreferences.getPostAppointmentReminderStop(this);
				appts = mDatabase.getAppointmentTable().getPastAppointments(baby.getId(), reminderWindow);
				for (Appointment appt: appts) {
					// check post-appt surveys for these PAST appointments
					// only show these surveys for appointments that have already occurred in the past
					if (DateUtils.combineDateAndTime(appt.getDate(), appt.getStartTime()).before(DateUtils.getTimestamp())) {
						GenericSurvey survey = (GenericSurvey) mDatabase.getPostAppointmentSurveyTable().getSurveyForBaby(baby.getId(), appt.getId());
						// if survey doesn't exist, then it has not been completed yet and we should reminder user
						if (survey == null) {
							reminder = new PromptedSpecialReminder(ReminderType.POST_APPT_SURVEY);
							reminder.addAppointment(appt);
							mRemindersMap.get(baby).add(reminder);
						}
					}
				}

				// check to see if there are phone call reminders
				// first we check for epds phone call reminders
				if (mDatabase.getLogTable().hasEPDSCallLaterFlag(mUser.getId())) {
					reminder = new PromptedSpecialReminder(ReminderType.CALL_ST_JOSEPH);
					mRemindersMap.get(baby).add(reminder);
				}
				// then we check for appointment reschedule call reminders
				int apptId = mDatabase.getLogTable().getAppointmentIdForYesterdaySurveyCallLater(baby.getId(), SurveyType.PRE_APPT);
				// check to make sure the id doesn't refer to a stale appointment
				Appointment appt = null;
				if (apptId != -1) {
					appt = mDatabase.getAppointmentTable().getAppointmentById(apptId);
					if (appt != null) {
						reminder = new PromptedSpecialReminder(ReminderType.CALL_DOCTOR);
						reminder.addAppointment(appt);
						mRemindersMap.get(baby).add(reminder);
						Log log = new Log(new CommonData(mUser.getId(), baby.getId()), SurveyType.PRE_APPT.name(), SurveyForm.REMINDED_CALL_LATER_TAG, appt.getId().toString());
						mDatabase.insertSingle(log, false, false);
					}
				}
				apptId = mDatabase.getLogTable().getAppointmentIdForYesterdaySurveyCallLater(baby.getId(), SurveyType.POST_APPT);
				if (apptId != -1 ) {
					appt = mDatabase.getAppointmentTable().getAppointmentById(apptId);
					if (appt != null) {
						reminder = new PromptedSpecialReminder(ReminderType.CALL_DOCTOR);
						reminder.addAppointment(appt);
						mRemindersMap.get(baby).add(reminder);
						Log log = new Log(new CommonData(mUser.getId(), baby.getId()), SurveyType.POST_APPT.name(), SurveyForm.REMINDED_CALL_LATER_TAG, appt.getId().toString());
						mDatabase.insertSingle(log, false, false);
					}
				}
				mDatabase.close(dbKey);
			}

			boolean showDialog = false;
			for (Baby baby: mBabies) {
				if (mRemindersMap.get(baby).size()>0) {
					showDialog = true;
					break;
				}
			}

			if (showDialog) {
				mAppointmentReminderDialog = new AppointmentRemindersDialog(this, mRemindersMap);
				mAppointmentReminderDialog.show();
			}
		}
	}

	private void profilePhotosRefresh() {
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.babyprofile_1);
		ImageView iv = (ImageView) layout.findViewById(R.id.profile_pic);
		Bitmap image = mBabies.get(0).getConvertedImage();
		if ((image != null)) {
			iv.setImageBitmap(image);
		}

		if (mBabies.size() > 1) {
			layout = (LinearLayout) this.findViewById(R.id.babyprofile_2);
			iv = (ImageView) layout.findViewById(R.id.profile_pic);
			image = mBabies.get(1).getConvertedImage();
			if ((image != null)) {
				iv.setImageBitmap(image);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		dbKey = mDatabase.open(dbKey);
		mReceiver = new UpdateReceiver();
		registerReceiver(mReceiver, new IntentFilter(Utilities.ACTION_UPDATE_MAIN));
		mDatabase.log(mUser.getId(), mCurrentBabyID, getIntent(), Log.ONRESUME);
		if(mTileAdapter!= null){
			mTileAdapter.notifyDataSetChanged();
		}
		Intent intent = getIntent();

		try {
			if((intent != null) && intent.getData() != null && (intent.getData().getQueryParameter(Utilities.TITLE) != null)) {
				updateCurrentKidById(Integer.parseInt(intent.getData().getQueryParameter(Utilities.CHILD_ID)));
				startActivityForTileName(intent.getData().getQueryParameter(Utilities.TITLE));

				intent.setData(null);
			} else {
			}

		} catch (Exception e) {
			Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
			e.printStackTrace();
		} finally {
		}

		// because updateInbox will fail when on the login screen
		if(mUser != null){
			updateInbox(); 
		}

		checkAppointmentReminders();
		mDatabase.close(dbKey);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	protected void onNewIntent (Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mReceiver);

		// make sure the reminder dialog is closed
		if ( (mAppointmentReminderDialog != null) && (mAppointmentReminderDialog.isShowing()) ) {
			mAppointmentReminderDialog.dismiss();
		}
	}

	/*
	 * Eliminates color banding, since we're using a color gradient for the tiles 
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	/* 
	 * initialize swiping actions for switching babies
	 */
	private void setupBabyHeaders() {
		// set up first baby's profile info
		setupABabyHeader(mBabyIDs.get(0), R.id.babyprofile_1);

		// if there's only one baby, then hide second baby (also disables flipping)
		if (mBabyIDs.size() == 1) {
			mCurrentBabyLayout = (LinearLayout) this.findViewById(R.id.babyprofile_2);
			mCurrentBabyLayout.setVisibility(View.GONE);
		}
		// else initialize second baby's info
		else {
			setupABabyHeader(mBabyIDs.get(1), R.id.babyprofile_2);
		}
	}

	// sets up the baby profile panel
	private void setupABabyHeader(int babyid, int layout) {
		mCurrentBabyLayout = (LinearLayout) this.findViewById(layout);
		Resources res = this.getResources();

		TextView tv;
		ImageView iv;
		try {
			Baby baby = mDatabase.getBabyTable().getBaby(babyid, this);	

			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_name);
			tv.setText(StringUtils.capitalize(baby.getName()));

			// add button for wall messages
			RelativeLayout wallButton = (RelativeLayout) mCurrentBabyLayout.findViewById(R.id.inbox_layout);
			wallButton.setOnClickListener(mWallListener);

			int age = baby.getChronologicalAge();
			String weeksOld = StringUtils.pluralize(age, "week", "weeks");
			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_chronological_age);
			tv.setText(weeksOld);

			age = baby.getAdjustedAge();
			weeksOld = StringUtils.pluralize(age, "week", "weeks");
			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_adjusted_age);
			tv.setText(weeksOld);	
			int textColor;
			if (baby.getGender().equals(Gender.MALE)) {
				textColor = Color.BLUE;
				mCurrentBabyLayout.setBackgroundColor(res.getColor(R.color.lightblue));
			}
			else {
				textColor = res.getColor(R.color.darkpink);
				mCurrentBabyLayout.setBackgroundColor(res.getColor(R.color.pink));
			}
			tv.setTextColor(textColor);
			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_adjusted_age_header);
			tv.setTextColor(textColor);

			iv = (ImageView) mCurrentBabyLayout.findViewById(R.id.profile_pic);
			Bitmap image = baby.getConvertedImage();
			if (image != null) {
				iv.setImageBitmap(image);
			}
			else {
				if (baby.getGender().equals(Gender.MALE)) {
					iv.setImageResource(R.drawable.baby_boy);
				}
				else {
					iv.setImageResource(R.drawable.baby_girl);
				}
			}
		}
		catch (Exception e) {
			Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
			e.printStackTrace();
			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_name);
			tv.setText("Unknown");
			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_chronological_age);
			tv.setText("0 weeks old");
			tv = (TextView) mCurrentBabyLayout.findViewById(R.id.profile_adjusted_age);
			tv.setText("0 weeks old");	
		}
	}

	/** Initializes tile info, including the header & info text */
	private void setupTiles() {
		String[] tileNames = getResources().getStringArray(R.array.tile_names);    	
		mTiles = new ArrayList<Tile>();
		for(int i=0; i<tileNames.length; i++) {
			Tile tile = new Tile();
			tile.setButtonName(tileNames[i]);
			if (tileNames[i].equals(Tile.APPOINTMENTS)) {
				tile.setIcon(Tile.APPOINTMENTS_ICON);
				tile.setButtonListener(mAppointmentsListener);
				tile.setIndicatorClassString(Appointment.class);
			}
			else if (tileNames[i].equals(Tile.DIAPERS)) {
				tile.setIcon(Tile.DIAPERS_ICON);
				tile.setButtonListener(mDiapersListener);
				tile.setIndicatorClassString(Diaper.class);
			}
			else if (tileNames[i].equals(Tile.BONDING)) {
				tile.setIcon(Tile.BONDING_ICON);
				tile.setButtonListener(mBondingListener);
				tile.setIndicatorClassString(BondingSurvey.class);
			}
			else if (tileNames[i].equals(Tile.BABYMOODS)) {
				tile.setIcon(Tile.BABYMOODS_ICON);
				tile.setButtonListener(mBabyMoodsListener);
				tile.setIndicatorClassString(BabyMoodSurvey.class);
			}
			else if (tileNames[i].equals(Tile.CUSTOMCHARTS)) {
				tile.setIcon(Tile.CUSTOMCHARTS_ICON);
				tile.setButtonListener(mCustomChartsListener);
				tile.setIndicatorClassString(GenericIndicator.class);
			}
			else if (tileNames[i].equals(Tile.WEIGHT)) {
				tile.setIcon(Tile.WEIGHT_ICON);
				tile.setButtonListener(mWeightListener);
				tile.setIndicatorClassString(Weight.class);
			}
			else if (tileNames[i].equals(Tile.MYMOODS)) {
				tile.setIcon(Tile.MYMOODS_ICON);
				tile.setButtonListener(mMyMoodsListener);
				tile.setIndicatorClassString(MoodMapSurvey.class);
			}
			else if (tileNames[i].equals(Tile.SURVEYS)) {
				tile.setIcon(Tile.SURVEYS_ICON);
				tile.setButtonListener(mSurveysListener);
				tile.setIndicatorClassString(GenericSurvey.class);
			}
			mTiles.add(tile);
		}
	}

	/**
	 * draws individual tiles, called by home screen's gridview
	 */
	public class TileAdapter extends BaseAdapter {
		Typeface mCondensed, mBoldCondensed;

		public TileAdapter(Context c) {
			mCondensed = Typeface.createFromAsset(getAssets(),"fonts/MyriadPro-Cond.otf");
			mBoldCondensed = Typeface.createFromAsset(getAssets(),"fonts/MyriadPro-BoldCond.otf");
		}

		@Override
		public int getCount() {
			return mTiles.size();
		}

		@Override
		public final Tile getItem(int position) {
			return mTiles.get(position);
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		// creates a FrameLayout for each tile
		// each tile contains: button, icon, & 2 right-aligned textviews (row0: header, row1: info)
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View tileView = convertView;
			dbKey = mDatabase.open(dbKey);
			//only inflate layout when convertView is null
			if (convertView == null) {
				LayoutInflater li = getLayoutInflater();
				tileView = li.inflate(R.layout.tile, null);
			}
			//otherwise re-use old layout and set new properties
			// set tile's button properties (text & height)
			Button btn = (Button)tileView.findViewById(R.id.tile_button);
			if (isTileNeglected(mTiles.get(position).getButtonName())) {
				btn.setBackgroundResource(R.drawable.btn_with_orange_border);
			}
			else {
				btn.setBackgroundResource(R.drawable.btn_with_border);
			}
			btn.setText(mTiles.get(position).getButtonName()); 
			btn.setTypeface(mBoldCondensed);
			btn.setOnClickListener(mTiles.get(position).getButtonListener());

			// set tile's icon properties (src & offset)
			ImageView iv = (ImageView)tileView.findViewById(R.id.tile_icon);
			iv.setImageResource(mTiles.get(position).getIcon());

			// set tile's data (header & info text)
			try{
				String indicatorClassString = mTiles.get(position).getIndicatorClassString();

				Indicator lastIndicator;
				if(indicatorClassString.equals(MoodMapSurvey.class.getSimpleName())){
					lastIndicator = mDatabase.getMoodMapTable().getLastParentIndicator(mUser.getId(), null);
				} 
				else if (indicatorClassString.equals(GenericSurvey.class.getSimpleName())) {
					lastIndicator = mDatabase.getEpdsSurveyTable().getLastParentIndicator(mUser.getId(), null);
				}
				else if (indicatorClassString.equals(Appointment.class.getSimpleName())) {
					lastIndicator = mDatabase.getAppointmentTable().getNextAppointment(mCurrentBaby.getId());
				}
				else {
					lastIndicator = mDatabase.getTableForClass(indicatorClassString).getLastBabyIndicator(mCurrentBaby.getId(), null);
				}

				if (lastIndicator != null)  {
					// for appointments the date will be the appt's date (not the date it was reported) and we should only show this if the appointment is in the future
					if (lastIndicator instanceof Appointment) {
						Appointment a = (Appointment) lastIndicator;
						if (DateUtils.combineDateAndTime(a.getDate(), a.getStartTime()).after(DateUtils.getTimestamp())) {
							mTiles.get(position).setHeaderText(DateUtils.getDateAsString(a.getDate(), DateUtils.TILE_DATE_FORMAT));
							mTiles.get(position).setInfoText(lastIndicator.getTileBlurb());
						}
						else {
							mTiles.get(position).setHeaderText("---");
							mTiles.get(position).setInfoText("n/a");
						}
					} 
					else if (lastIndicator instanceof MoodMapSurvey) {
						mTiles.get(position).setHeaderText(DateUtils.getDateAsString(lastIndicator.getDateTime(), DateUtils.TILE_DATE_FORMAT));
						mTiles.get(position).setInfoText("last update:\n" + DateUtils.getDateAsString(lastIndicator.getDateTime(), DateUtils.TILE_HOUR_FORMAT));
					}
					else if (lastIndicator instanceof GenericSurvey) {
						// stress and epds are given together, so they should have the same number of competed stuff
						// we're passing the initial time to start looking for surveys - this is the date when user first joined study
						int completedSurveys = mDatabase.getStressSurveyTable().getNumberOfCompletedSurveysForUser(
								mUser.getId(), 
								mUser.getCommonData().getTimestamp(), 
								DateUtils.getTimestamp());
						mTiles.get(position).setHeaderText(DateUtils.getDateAsString(lastIndicator.getDateTime(), DateUtils.TILE_DATE_FORMAT));
						mTiles.get(position).setInfoText("completed\n" + completedSurveys + " out of " + SurveysOverview.MAX_NUMBER_OF_SURVEYS + "\nsurveys");
					}
					// for all other indicators 
					else {
						mTiles.get(position).setHeaderText(DateUtils.getDateAsString(lastIndicator.getDateTime(), DateUtils.TILE_DATE_FORMAT));
						mTiles.get(position).setInfoText(lastIndicator.getTileBlurb());
					}
				}
				// for null indicators
				else {
					mTiles.get(position).setHeaderText("---");
					mTiles.get(position).setInfoText("n/a");
				}

			} catch(Exception e){
				Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
				mTiles.get(position).setHeaderText("---");
				mTiles.get(position).setInfoText("n/a");
				e.printStackTrace();
			}
			mDatabase.close(dbKey);
			TextView tv = (TextView)tileView.findViewById(R.id.tile_header);
			tv.setText(mTiles.get(position).getHeaderText());
			tv.setTypeface(mCondensed);
			tv = (TextView)tileView.findViewById(R.id.tile_info);
			tv.setText(mTiles.get(position).getInfoText());
			tv.setTypeface(mCondensed);
			return tileView;
		}
	}

	View.OnClickListener mWallListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, Wall.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			if (mLastWallView != null) {
				intent.putExtra(Wall.LAST_TIMESTAMP, mLastWallView.getTime());
			}
			startActivityForResult(intent, WALL_ID);
		}
	};

	View.OnClickListener mAppointmentsListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, AppointmentsTabs.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivityForResult(intent, APPOINTMENTS_ID);
		}
	};

	View.OnClickListener mDiapersListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, DiapersOverview.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivityForResult(intent, DIAPERS_ID);
		}
	};

	View.OnClickListener mBondingListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, BondingOverview.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivityForResult(intent, BONDING_ID);
		}
	};

	View.OnClickListener mBabyMoodsListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, BabyMoodsOverview.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivityForResult(intent, BABYMOODS_ID);
		}
	};

	View.OnClickListener mCustomChartsListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, CustomChartsOverview.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivityForResult(intent, CUSTOMCHARTS_ID);           
		}
	};

	View.OnClickListener mMyMoodsListener = new OnClickListener() {
		public void onClick(View v) {
			// Copyrighted material, so this has been removed
//			Intent intent = new Intent(EstrellitaTiles.this, MyMoodsOverview.class); 
//			intent.putExtra(TileActivity.BABY, mCurrentBaby);
//			startActivityForResult(intent, MYMOODS_ID);
		}
	};

	View.OnClickListener mWeightListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EstrellitaTiles.this, WeightOverview.class); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivityForResult(intent, WEIGHT_ID);
		}
	};

	View.OnClickListener mSurveysListener = new OnClickListener() {
		public void onClick(View v) {
			// Copyrighted survey material, so this has been removed
//			Intent intent = new Intent(EstrellitaTiles.this, SurveysOverview.class); 
//			intent.putExtra(TileActivity.BABY, mCurrentBaby);
//			startActivityForResult(intent, SURVEYS_ID);
		}
	};

	public void startActivityForTileName(String target) throws NoUserException {
		Class<?> c;
		if (target.equals(Tile.APPOINTMENTS)) {
			c = AppointmentsTabs.class;
		}
		else if (target.equals(Tile.DIAPERS)) {
			c = DiapersOverview.class;
		}
		else if (target.equals(Tile.BONDING)) {
			c = BondingOverview.class;
		}
		else if (target.equals(Tile.BABYMOODS)) {
			c = BabyMoodsOverview.class;
		}
		else if (target.equals(Tile.CUSTOMCHARTS)) {
			c = CustomChartsOverview.class;
		}
		else if (target.equals(Tile.WEIGHT)) {
			c = WeightOverview.class;
		}
		else if (target.equals(Tile.MYMOODS)) {
			c = MyMoodsOverview.class;
		}
		else if (target.equals(Tile.SURVEYS)) {
			c = SurveysOverview.class;
		} 
		else if (target.equals(Tile.WALL)) {
			c = Wall.class;
		} 
		else {
			c = null;
		}
		if( c!= null) {
			Intent intent = new Intent(EstrellitaTiles.this, c); 
			intent.putExtra(TileActivity.BABY, mCurrentBaby);
			startActivity(intent);
		}
	}

	// checks for results from all the tiles
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			switch (requestCode) {
			// if user cancels login activity (either clicking 'cancel' button or hitting back button) then app will quit too
			case LOGIN_ACTIVITY_ID:
				finish();
				break;
			}
		}
	}

	/**
	 * updates the given table for the current kid
	 * @param table
	 * @param object 
	 */
	public void updateTable(final String table, final UpdateCompleteAction updateCompleteAction){

		final LoadingThreadData loadingThreadData = new LoadingThreadData(){

			IndicatorTable tableForClass = (IndicatorTable)mDatabase.getTableForClass(table);
			List<ContentValues>[] unSynchedIndictors;
			JSONArray updateArray2;
			private boolean shouldProceed;

			@Override
			public boolean onCompleteAction() {
				try{
					updateCompleteAction.onComplete();
				} catch(Exception e){
					Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
				}
				return false;
			}

			@Override
			public boolean onDoWork() {		
				// upload unsynched
				if(tableForClass != null) {
					mDatabase.upload(tableForClass, unSynchedIndictors);

					// get new updates
					try {
						tableForClass.updateTable(updateArray2);
					} catch (Exception e) {
						Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
						e.printStackTrace();
					}

					return true;
				}
				return false;
			}

			@Override
			public boolean shouldProceed() {
				shouldProceed = false;
				if(!WebUtils.isOnline(EstrellitaTiles.this) || mDatabase== null || 
						mDatabase.getDb() == null || !mDatabase.getDb().isOpen()){
					return false;
				}

				try{
					unSynchedIndictors = tableForClass.getUnSynchedCVs();
					if(unSynchedIndictors.length != 0){
						shouldProceed = true;
					}

					updateArray2 = tableForClass.getUpdateArray(mUser.getId(), mCurrentBabyID, EstrellitaTiles.this);
					if(updateArray2.length() != 0){
						shouldProceed = true;
					}
				} catch (Exception e){
					Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
					e.printStackTrace();
					return false;
				}


				return shouldProceed;
			}

		};
		loadingDialog(loadingThreadData, "updateTable");			
	}

	public void loadingDialog(final LoadingThreadData loadingThreadData, String origin){
		// because loginThread is called before somethings, db hasn't loaded or something weird.
		// but I want to not restart the thread for other loading screens
		if(loadingThread != null && 
				(loadingThread.getStatus() != Status.FINISHED)){
			loadingThread.getUpdateThreadData().add(new QueuedAction() {

				@Override
				public void doAction() {
					startLoadingThread(loadingThreadData);
				}
			});
		} else {
			startLoadingThread(loadingThreadData);
		}
	}

	private void startLoadingThread(
			final LoadingThreadData loadingThreadData) {
		handler.sendEmptyMessage(UPDATE_DIALOG);
		loadingThread = new LoadingThread(loadingThreadData);
		loadingThread.execute((String[]) null);
	}

	/** Nested class that performs progress calculations (counting) */
	private class LoadingThread extends AsyncTask<String, Void, String> {

		private final LoadingThreadData loadingThreadData;
		public LoadingThread(LoadingThreadData loadingThreadData){
			this.loadingThreadData = loadingThreadData;
		}

		@Override
		protected String doInBackground(String... params) {
			// Things to be done before execution of long running operation. For example showing ProgessDialog
			if(getUpdateThreadData().shouldProceed()){
				getUpdateThreadData().onDoWork();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// execution of result of Long time consuming operation
			try{
				getUpdateThreadData().onCompleteAction();
			} catch(Exception e){
				Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
			}
			onCancelled();
		}

		@Override
		protected void onPreExecute() {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
					}
					handler.sendEmptyMessage(-2);

				}
			};
			new Thread(r).start();
		}



		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onCancelled()
		 */
		protected void onCancelled(){

			while(!getUpdateThreadData().isEmpty()){
				getUpdateThreadData().poll().doAction();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// Things to be done while execution of long running operation is in progress. For example updating ProgessDialog
		}

		public LoadingThreadData getUpdateThreadData() {
			return loadingThreadData;
		}
	}

	// Define the Handler that receives messages from the thread and update the progress
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			try{
				switch(msg.what){
				}
			} catch(Exception e){
				e.printStackTrace();
				Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
			}
		}
	};

	// Define the Handler that receives messages from the thread and update the progress
	final Handler kidUpdateHandler = new Handler() {
		public void handleMessage(Message msg) {
			try{
				updateCurrentKidByIndex(mUser.getCurrentKidIndex());
				updateInbox();

			} catch(Exception e){
				e.printStackTrace();
				Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
			}
		}
	};

	//----start: UpdateService Related
	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			kidUpdateHandler.sendEmptyMessage(0);
		}
	}

	protected void onStop() {
		super.onStop();
		mDatabase.close(dbKey);

		// make sure the reminder dialog is closed
		if ( (mAppointmentReminderDialog != null) && (mAppointmentReminderDialog.isShowing()) ) {
			mAppointmentReminderDialog.dismiss();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		try{
			// Handle item selection for debug menu
			switch (item.getItemId()) {
			case R.id.debug_sync:
				SyncService.updateTables(this, mDatabase, null);
				Toast.makeText(this, "sycing", Toast.LENGTH_SHORT);
				break;
			case R.id.debug_dump_db:
				mDatabase.sendDBtoSDCard();
				Toast.makeText(this, "dumping db", Toast.LENGTH_SHORT);
				break;
			case R.id.debug_regular_check:
				SyncService.checkOccasionallyDoneIndicators(this, mDatabase);
				Toast.makeText(this, "performing regular check", Toast.LENGTH_SHORT);
				break;
			case R.id.debug_special_check:
				SyncService.performSpecialReminders(this, mDatabase);
				Toast.makeText(this, "performing special check", Toast.LENGTH_SHORT);
				break;
			case R.id.debug_update_widget:
				Utilities.updateWidget(this);
				Toast.makeText(this, "updating widget", Toast.LENGTH_SHORT);
				break;
			case R.id.change_preferences:
				startActivity(new Intent(this, EstrellitaPreferences.class));
				break;

				// user (non-debug) menu
				// disable the home button when you're on the home screen
			case R.id.home:
				break;
			case R.id.settings:
				startActivity(new Intent(this, EstrellitaPreferences.class));
				break;
			case R.id.contact:
				startActivity(new Intent(this, ContactInfo.class));
				break;

			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (Exception e){
			Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
			e.printStackTrace();
		}
		return true;
	}

	public static int getParentId(Context context) {
		if (mUser == null) {
			return UserPreferences.getLastUserId(context);
		}
		return mUser.getId();
	}

	public static int getCurrentBabyId() {
		if (mUser != null) {
			if (mCurrentBabyID <= 0) {
				return mUser.getCurrentKid().getId();
			}
			return mCurrentBabyID;
		}
		else {
			return -1;
		}
	}

	public static List<Integer> getAllBabyIds(Context context) {
		if (mBabyIDs == null) {
			return UserPreferences.getKidIds(context);
		}
		return mBabyIDs;
	}

	public static List<Baby> getAllBabies(Context context) {
		if (mBabies == null) {
			if (mBabyIDs == null) {
				mBabyIDs = UserPreferences.getKidIds(context);
			}
			return mDatabase.getBabyTable().getBabies(mBabyIDs, context);
		}
		return mBabies;
	}

	public static Baby getBaby(int id) {
		for (int i=0; i<mBabies.size(); i++) {
			if (mBabies.get(i).getId() == id) {
				return mBabies.get(i);
			}
		}
		return null;
	}

	public static User getParentUser(Context context) {
		if (mUser == null) {
			int userID = UserPreferences.getLastUserId(context);
			mBabyIDs = UserPreferences.getKidIds(context);
			try {
				mUser = mDatabase.getUserTable().getUserAndKids(userID, mBabyIDs, context, mDatabase.getBabyTable());
			} catch (NoUserException e) {
				return null;
			}
		}
		return mUser;
	}

	private boolean isTileNeglected(String tileName) {
		IndicatorTable t;
		if (tileName.equals(Tile.APPOINTMENTS)) {
			t = mDatabase.getAppointmentTable();
		}
		else if (tileName.equals(Tile.DIAPERS)) {
			t = mDatabase.getDiaperTable();
		}
		else if (tileName.equals(Tile.BONDING)) {
			t = mDatabase.getBondingTable();
		}
		else if (tileName.equals(Tile.BABYMOODS)) {
			t = mDatabase.getBabyMoodTable();
		}
		else if (tileName.equals(Tile.CUSTOMCHARTS)) {
			t = mDatabase.getGenericIndicatorTable();
		}
		else if (tileName.equals(Tile.WEIGHT)) {
			t = mDatabase.getWeightTable();
		}
		else if (tileName.equals(Tile.MYMOODS)) {
			t = mDatabase.getMoodMapTable();
		}
		else if (tileName.equals(Tile.SURVEYS)) {
			t = mDatabase.getEpdsSurveyTable();
		} else {
			t = null;
		}
		try{
			if (tileName.equals(Tile.MYMOODS) || tileName.equals(Tile.SURVEYS)) {
				if(t != null && t.isNeglected(getParentId(this), this)){
					return true;
				}
			}
			else {
				if(t != null && t.isNeglected(mCurrentBabyID, this)){
					return true;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			Utilities.writeToWeeklyErrorLogFile(EstrellitaTiles.this, e);
		}

		return false;
	}
}

