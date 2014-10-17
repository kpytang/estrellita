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

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.CalendarImageView;
import edu.uci.ics.star.estrellita.customview.CalendarPickerDialog;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.customview.IntervalTimePickerDialog;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;
import edu.uci.ics.star.estrellita.utils.Utilities;

public class AppointmentForm extends TileActivity<Appointment> {
	private static final int SPECIALTY_DIALOG_ID = 0;
	private static final int DATE_DIALOG_ID = 1;
	private static final int TIME_DIALOG_ID = 2;
	private static final int ADDRESS_BOOK_ID = 3;
	private static final int NOTES_HELP_DIALOG_ID = 4;
	private static final int CONCERNS_DIALOG_ID = 5;
	private static final int VALIDATE_DIALOG_ID = 6;

	private static final String SPECIALTY_DIALOG_TITLE = "Doctor's specialty is:";
	private static final String CONCERNS_DIALOG_TITLE = "Which topics do you have special concerns about?";

	private EditText mDoctorName, mLocation, mPhone, mNotes;
	private TextView mSpecialty, mDate, mTime;
	private LinearLayout mConcernsButton;
	private boolean[] mConcernFlags;

	// will hold the form's information
	private Appointment mAppointment;

	// holds date and time fields so that we can pre-populate the dialogs
	private Calendar mDisplayDate;
	private boolean mFirstTimeLoadingCalendar = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appointment_new_form);

		// initialize header & footer
		setActivityHeader("appointment", true, Tile.APPOINTMENTS);

		// initialize dialogs
		mDoctorName = (EditText) this.findViewById(R.id.drname);
		ImageView iv = (ImageView) this.findViewById(R.id.addressbook);
		iv.setOnClickListener(mAddressBookClickListener);

		mSpecialty = (TextView) this.findViewById(R.id.specialty);
		mSpecialty.setOnClickListener(mSpecialtyClickListener);
		mSpecialty.setFocusable(false);

		mDate = (TextView) this.findViewById(R.id.date);
		mDate.setOnClickListener(mDateClickListener);
		mDate.setFocusable(false);

		mTime = (TextView) this.findViewById(R.id.time);
		mTime.setOnClickListener(mTimeClickListener);
		mTime.setFocusable(false);

		mLocation = (EditText) this.findViewById(R.id.location);
		mPhone = (EditText) this.findViewById(R.id.drphone);

		iv = (ImageView) this.findViewById(R.id.notes_help);
		iv.setOnClickListener(mNotesHelpClickListener);

		mNotes = (EditText) this.findViewById(R.id.notes);
		mNotes.setOnClickListener(mNotesClickListener);
		mNotes.setOnFocusChangeListener(mNotesFocusListener);

		mConcernsButton = (LinearLayout) this.findViewById(R.id.concerns_layout); 
		mConcernsButton.setOnClickListener(mConcernsClickListener);

		//initialize fields - use the passed in parcel/appointment or (if it's null), then just set it to the default
		mAppointment = (Appointment) getIntent().getParcelableExtra("appointment");

		mConcernFlags = new boolean[4];

		String rightButtonName = "add appointment";
		restoreIndicator();
		if (mAppointment != null) {
			// all appointments at least have a dr name, specialty, date, time, and concerns
			// optional fields = location, phone number, notes
			mDoctorName.setText(mAppointment.getDoctorName());
			mSpecialty.setText(mAppointment.getDoctorTypeAsString());
			mDisplayDate = Calendar.getInstance();
			mDisplayDate.setTime(DateUtils.combineDateAndTime(mAppointment.getDate(), mAppointment.getStartTime()));
			mConcernFlags = mAppointment.getAllConcerns();
			if (mAppointment.getLocation().length() > 0) {
				mLocation.setText(mAppointment.getLocation());
			}
			if (mAppointment.getPhone().length() > 0) {
				mPhone.setText(mAppointment.getPhone());
			}
			if (mAppointment.getCommonData().hasNotes()) {
				mNotes.setText(mAppointment.getCommonData().getNotes());
			}
			rightButtonName = "update appointment";
		}
		// default: grab current date & time, initial concerns to "off" (by making it look transparent)
		else {
			mAppointment = new Appointment();
			mAppointment.getCommonData().setIdUser(EstrellitaTiles.getParentId(this));
			mAppointment.getCommonData().setIdBaby(mBaby.getId());
			mDisplayDate = Calendar.getInstance();
			mConcernFlags = new boolean[]{false, false, false, false};
		}

		setButtonFooter(rightButtonName, mSaveClickListener, null, null);
		// update date, time, concern flags
		updateDateTime(mDisplayDate.getTime());
		for (int i=0; i<mConcernFlags.length; i++) {
			toggleConcernIcon(i, mConcernFlags[i]);
		}  
	}

	/**
	 * show the different doctor specialties
	 * if user picks 'other', then the EditText becomes editable and the user can type in the specialty
	 * 
	 * @param v
	 */
	public AlertDialog SpecialtyDialog(Context context) {
		final String[] specialties = getResources().getStringArray(R.array.specialties);
		int currentSpecialtyIndex = -1;
		if ((mAppointment != null) && (mAppointment.getDoctorType() != null)) {
			currentSpecialtyIndex = mAppointment.getDoctorType().ordinal();
		}

		// create dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(SPECIALTY_DIALOG_TITLE);
		builder.setSingleChoiceItems(specialties, currentSpecialtyIndex, new DialogInterface.OnClickListener()  {
			public void onClick(DialogInterface dialog, int item) {
				mSpecialty.setText(specialties[item]);
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	public AlertDialog ConcernsDialog(Context context) {  
		String[] concerns = new String[] {"diapers", "baby moods", "custom charts", "weight" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(CONCERNS_DIALOG_TITLE);
		builder.setMultiChoiceItems(concerns, mConcernFlags, new DialogInterface.OnMultiChoiceClickListener()  {
			public void onClick(DialogInterface dialog, int item, boolean isChecked) {
				mConcernFlags[item] = isChecked;
			}
		});
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				for (int i=0; i<mConcernFlags.length; i++) {
					toggleConcernIcon(i, mConcernFlags[i]);
					removeDialog(CONCERNS_DIALOG_ID);
					dialog.dismiss();
				}
			}
		});
		return builder.create();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SPECIALTY_DIALOG_ID:
			return SpecialtyDialog(this);
		case DATE_DIALOG_ID:
			return new CalendarPickerDialog(this, mDateSetListener, mDateDismissListener, mDisplayDate);
		case TIME_DIALOG_ID:
			return new IntervalTimePickerDialog(this, mTimeSetListener, mDisplayDate.get(Calendar.HOUR_OF_DAY), mDisplayDate.get(Calendar.MINUTE), false);
		case NOTES_HELP_DIALOG_ID:
			return CustomViews.NotesTipDialog(this, getString(R.string.appointment_notes_hint));
		case CONCERNS_DIALOG_ID:
			return ConcernsDialog(this);
		case VALIDATE_DIALOG_ID:
			return ValidateFormDialog(this);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DATE_DIALOG_ID:
			((CalendarPickerDialog) dialog).setTime(mDisplayDate, !mFirstTimeLoadingCalendar);
			if (mFirstTimeLoadingCalendar) {
				mFirstTimeLoadingCalendar = false;
			}
			break;
		case TIME_DIALOG_ID:
			((IntervalTimePickerDialog) dialog).updateTime(mDisplayDate.get(Calendar.HOUR_OF_DAY), mDisplayDate.get(Calendar.MINUTE));
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}

	// define the listener that's called once a user selected the date.
	private CalendarPickerDialog.OnDateSetListener mDateSetListener = new CalendarPickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(CalendarImageView view, Calendar selectedDate) {
			mDisplayDate.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
			mDisplayDate.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
			mDisplayDate.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));

			updateDateTime(mDisplayDate.getTime());
		}
	};

	private CalendarPickerDialog.OnDismissListener mDateDismissListener = new CalendarPickerDialog.OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			removeDialog(DATE_DIALOG_ID);
		}
	};

	private IntervalTimePickerDialog.OnTimeSetListener mTimeSetListener = new IntervalTimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mDisplayDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mDisplayDate.set(Calendar.MINUTE, minute);
			updateDateTime(mDisplayDate.getTime());
		}
	};

	private void updateDateTime(Date d) {
		mDate.setText(DateUtils.getDateAsString(d, DateUtils.CALENDAR_DATEPICKER_FORMAT_SHORTER));
		mTime.setText(DateUtils.getDateAsString(d, DateUtils.APPOINTMENT_TIME_FORMAT));
	}

	private View.OnClickListener mSpecialtyClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(SPECIALTY_DIALOG_ID);
		}
	};

	private View.OnClickListener mDateClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(DATE_DIALOG_ID);
		}
	};

	private View.OnClickListener mTimeClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(TIME_DIALOG_ID);
		}
	};

	private View.OnClickListener mAddressBookClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Cursor cursor =  managedQuery(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			// only go to the address book if there are people listed there
			if (cursor.getCount() > 0) {
				cursor.close();
				// show the address book
				Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);  
				startActivityForResult(intent, ADDRESS_BOOK_ID); 
			}
		}
	};

	private View.OnClickListener mNotesHelpClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(NOTES_HELP_DIALOG_ID);
		}
	};

	private View.OnClickListener mNotesClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			mNotes.setTypeface(null, Typeface.NORMAL);
		}
	};

	private View.OnFocusChangeListener mNotesFocusListener = new View.OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				if (mNotes.getText().length() == 0) {
					mNotes.setTypeface(null, Typeface.ITALIC);
				}
			}
			else {
				mNotes.setTypeface(null, Typeface.NORMAL);
			}
		}
	};

	private View.OnClickListener mConcernsClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(CONCERNS_DIALOG_ID);
		}
	};

	public void toggleConcernIcon(int index, boolean turnOn) {
		ImageView iv = null;
		switch(index) {
		// diapers
		case 0:
			iv = (ImageView) this.findViewById(R.id.diaper_concern);
			mAppointment.setConcernedAboutDiapers(turnOn);
			break;
			// baby moods
		case 1:
			iv = (ImageView) this.findViewById(R.id.babymood_concern);
			mAppointment.setConcernedAboutBabyMoods(turnOn);
			break;
			// my notes
		case 2:
			iv = (ImageView) this.findViewById(R.id.charts_concern);
			mAppointment.setConcernedAboutCharts(turnOn);
			break;
			// weight
		case 3:
			iv = (ImageView) this.findViewById(R.id.weight_concern);
			mAppointment.setConcernedAboutWeight(turnOn);
			break;
		}
		if (iv != null) {
			if (turnOn) {
				iv.setAlpha(255);
			}
			else {
				iv.setAlpha(70);
			}
		}
	}

	// checks for results from opening the address book
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ADDRESS_BOOK_ID:

				// get the contact data
				Uri result = data.getData();
				// get the contact id from the uri
				String id = result.getLastPathSegment();  
				// query for all the phone info for this contact
				ContentResolver cr = getContentResolver();

				Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, null, ContactsContract.Contacts._ID + "=?", new String[]{id}, null);
				// get the first phone number and the accompanying display name
				try {

					if (cursor.moveToFirst()) {  

						int columnID = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
						String phoneData = cursor.getString(columnID); 
						mDoctorName.setText(StringUtils.capitalize(phoneData));

						// if this has a phone number, look it up
						if (Integer.parseInt(cursor.getString(
								cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
							Cursor pCur = cr.query(
									ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
									null, 
									ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
									new String[]{cursor.getString(
											cursor.getColumnIndex(ContactsContract.Contacts._ID))}, null);
							try {

								if (pCur.moveToFirst()) {
									columnID = pCur.getColumnIndex(ContactsContract.Data.DATA1);
									phoneData = pCur.getString(columnID);
									mPhone.setText(StringUtils.cleanUpPhoneNumber(phoneData));
								}
							} finally {
								pCur.close();
							}
						}
					}
				} catch(Exception e){
					Utilities.writeToWeeklyErrorLogFile(this, e);
					e.printStackTrace();
				}
				finally{
					cursor.close();
				}
				break;
			}
		}
	}

	public AlertDialog ValidateFormDialog(Context context) {
		String prompt = "";
		if ((mDoctorName.getText() == null) || (mDoctorName.getText().toString().length()==0)) {
			prompt += "Doctor's Name\n";
		}
		if ((mSpecialty.getText() == null) || (mSpecialty.getText().toString().length()==0)) {
			prompt += "Specialty\n";
		}
		if ((mDate.getText() == null) || (mDate.getText().toString().length()==0)) {
			prompt += "Date\n";
		}
		if ((mTime.getText() == null) || (mTime.getText().toString().length()==0)) {
			prompt += "Time\n";
		}
		if ( (mPhone.getText() != null) && !StringUtils.containsOnlyNumbers(mPhone.getText().toString())) {
			prompt += "Phone Number\n";
		}
		prompt = prompt.trim();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("The following fields are missing and/or incorrectly formatted");
		builder.setMessage(prompt)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}

	public boolean isMissingRequiredFields() {
		if ((mDoctorName.getText() == null) || (mDoctorName.getText().toString().length()==0)) {
			return true;
		}
		if ((mSpecialty.getText() == null) || (mSpecialty.getText().toString().length()==0)) {
			return true;
		}
		if ((mDate.getText() == null) || (mDate.getText().toString().length()==0)) {
			return true;
		}
		if ((mTime.getText() == null) || (mTime.getText().toString().length()==0)) {
			return true;
		}
		return false;	
	}

	public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (isMissingRequiredFields()) {
				showDialog(VALIDATE_DIALOG_ID);
			}
			else {
				// send the new appointment back to the overview tabs
				Intent intent = new Intent();
				// name, specialty, date, time, location, phone, notes (concerns are tracked onclick)
				saveIndicator();
				// time the appointment was entered in the phone
				mAppointment.getCommonData().setTimestamp(DateUtils.getTimestamp());
				intent.putExtra("appointment", mAppointment);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileActivity#saveIndicator()
	 */
	@Override
	protected void saveIndicator() {
		mAppointment.setDoctorName(mDoctorName.getText().toString().trim());
		mAppointment.setDoctorType(mSpecialty.getText().toString());
		mAppointment.setDate(mDisplayDate.getTime());
		mAppointment.setStartTime(DateUtils.getDateAsTime(mDisplayDate.getTime()));
		mAppointment.setPhone(mPhone.getText().toString());
		mAppointment.setLocation(mLocation.getText().toString());
		mAppointment.getCommonData().setNotes(mNotes.getText().toString());
		mAppointment.setAllConcerns(mConcernFlags);

		mIndicator = mAppointment;
	}

	private void restoreIndicator() {
		mIndicator = null; 
		if (getLastNonConfigurationInstance() != null) { 
			mIndicator = (Appointment) getLastNonConfigurationInstance();
			mAppointment = mIndicator;
		}
	}
}