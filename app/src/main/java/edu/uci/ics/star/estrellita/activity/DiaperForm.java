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

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.customview.CustomViews.ToastMessage;
import edu.uci.ics.star.estrellita.customview.TextDiaperNumberPicker;
import edu.uci.ics.star.estrellita.object.indicator.Diaper;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

// Notes for how the photo thumbnail listener works:
// if there are no previous photos, then clicking on this will do nothing
// if there are previous photos, then launch the photoflipper to view past photos
// if there's a photo (so just took a pic with the camera), then show a dialog (with a larger image)
public class DiaperForm extends TileActivity<Diaper> {
	private static final int CAMERA_ACTIVITY_ID = 0;

	private ImageView mPhotoFrame, mCameraButton, mSavePhotoButton;
	private View mPhotoFlipperView;
	private TextView mPreviousPhotos;
	private Uri mImageUri;

	private String mCurrentPhotoFilename = null;
	private List<String> mPhotoFilenames;

	private TextDiaperNumberPicker mWetNumPicker;
	private TextDiaperNumberPicker mDirtyNumPicker;

	private TextView mDateTextView;
	private Calendar mDisplayDate;
	private boolean mUpdatedYesterday = false;

	private Diaper mDiaper;

	private List<Diaper> mDiapers;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diaper_form);

		// initialize header        
		setActivityHeader("diaper count", true, Tile.DIAPERS);
		setButtonFooter("update diapers", mSaveClickListener, null, null);

		// initialize the date
		LinearLayout dateButton = (LinearLayout) this.findViewById(R.id.date_header);
		dateButton.setClickable(true);
		dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Dialog d = ChangeDiaperDateDialog(v.getContext());
				d.show();
			}       
		});
		mDateTextView = (TextView) this.findViewById(R.id.date_header_text);
		mDisplayDate = Calendar.getInstance();
		updateDateTime(mDisplayDate.getTime());

		// this will update the diapers and photos based on the timestamp, as set by mDisplayDate
		updateDiapersAndPhotosByDate();

		// this is because the view is getting updated before the refresh
		ViewTreeObserver vto = mWetNumPicker.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mWetNumPicker.notifyChange();
				mWetNumPicker.updateView();

				mDirtyNumPicker.notifyChange();
				mDirtyNumPicker.updateView();

				mWetNumPicker.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}

	private void updateDiapersAndPhotosByDate() {
		// get diaper data based on the date, by default we'll check today's date
		// time range will be start of the day and end of the day
		Diaper[] dbDiapers = mDatabase.getDiaperTable().getDiapersForTimeRange(mBaby.getId(), 
				DateUtils.getStartOfDay(mDisplayDate.getTime()), DateUtils.getEndOfDay(mDisplayDate.getTime()));
		ArrayList<Diaper> diaperList = null;
		if (dbDiapers != null) {
			// make sure the diapers are sorted with the most recent at top
			// we'll use the most recent one to pull the list of photo filenames
			diaperList = new ArrayList<Diaper>(Arrays.asList(dbDiapers));
			Collections.sort(diaperList);
			Collections.reverse(diaperList);
			// check to make sure each filename actually exists
			// if it doesn't, then look at the image in commondata and save that file using the original filename
			List<String> filenames = diaperList.get(0).getPhotoFilenames();
			Diaper.checkAndCreatePhotoFileNames(filenames, diaperList);

			// now all the filenames should exist on the phone, so we'll clear out the bitmaps in commondata
			for (int i=0; i<diaperList.size(); i++) {
				diaperList.get(i).getCommonData().setImage(null);
			}
			mDiapers = diaperList;
		}
		else {
			mDiapers = new ArrayList<Diaper>();
		}

		// if there are no diaper counts for this date, then initialize this with a new (0-count) diaper object
		if(mDiapers.size() == 0) {
			mDiapers.add(new Diaper());
		}

		// sort the diapers (first chronologically, oldest first; then reverse it, so that the newest is first)
		Collections.sort(mDiapers);
		Collections.reverse(mDiapers);

		mWetNumPicker = (TextDiaperNumberPicker) this.findViewById(R.id.wet_diaper_count);
		mDirtyNumPicker = (TextDiaperNumberPicker) this.findViewById(R.id.dirty_diaper_count);

		Diaper d = mDiapers.get(0);

		mWetNumPicker.setEnd(50);
		mDirtyNumPicker.setEnd(50);
		mWetNumPicker.setStartingValue(d.getWet());
		mDirtyNumPicker.setStartingValue(d.getDirty());

		// this is so pass by reference doesn't update the original value
		mDiaper = new Diaper();
		mDiaper.setDirty(d.getDirty());
		mDiaper.setWet(d.getWet());
		mDiaper.setPhotoFilenames(d.getPhotoFilenames());

		restoreIndicator();

		mWetNumPicker.changeCurrent(mDiaper.getWet());
		mDirtyNumPicker.changeCurrent(mDiaper.getDirty());

		mPhotoFrame = (ImageView) DiaperForm.this.findViewById(R.id.current_photo);
		mPhotoFrame.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// there's a photo, so show a larger preview of it
				if ((mCurrentPhotoFilename != null) && (mPhotoFrame.getVisibility() == View.VISIBLE)) {
					Dialog d = SingleImagePreview(mCurrentPhotoFilename);
					d.show();
				}
			}
		});
		mPhotoFrame.setVisibility(View.GONE);

		// for launching the camera: when there's nothing in the mPhoto imageview then show the camera icon
		// otherwise, show the delete icon which will clear the image in the box
		showCameraIcon();

		// for view past photos: call the dialog for flipping through photos
		mPreviousPhotos = (TextView) DiaperForm.this.findViewById(R.id.previous_photos);
		mPreviousPhotos.setTypeface(regular);
		mPreviousPhotos.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// if there are prior photos, so we should show the view flipper
				if ((mPreviousPhotos.getVisibility() == View.VISIBLE) && (mPhotoFilenames.size()>0)) {
					Dialog d = ImageUtils.createPhotoFlipper(DiaperForm.this, mPhotoFlipperView, mPhotoFilenames, true);
					d.show();
				}
			}
		});

		showSaveIcon();

		// initialize the photos
		mPhotoFilenames = mDiaper.getPhotoFilenames();
		if (mPhotoFilenames == null) {
			mPhotoFilenames = new ArrayList<String>();
		}
		// we want to show the most recent photo first (plus the list of diapers are listed with the most recent first too)
		Collections.reverse(mPhotoFilenames);
		Diaper.checkAndCreatePhotoFileNames(mPhotoFilenames, mDiapers);

		updatePreviousPhotosText();

	}

	public AlertDialog ChangeDiaperDateDialog(Context context) {
		if (!mUpdatedYesterday) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.update_yesterdays_diapers);
			builder.setCancelable(true);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// they want to update yesterday's diapers, so let's update the time
					updateDateTime(DateUtils.addToDate(DateUtils.getTimestamp(), Calendar.DAY_OF_YEAR, -1));
					// and then update the data tied to the screen
					updateDiapersAndPhotosByDate();
					// and then disable this flag, because you can only change yesterday's diapers
					mUpdatedYesterday = true;
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Go back and updated today's diaper count?");
			builder.setCancelable(true);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// they want to update today's diapers, so let's update the time
					updateDateTime(DateUtils.getTimestamp());
					// and then update the data tied to the screen
					updateDiapersAndPhotosByDate();
					// and then disable this flag, because you can only change yesterday's diapers
					mUpdatedYesterday = false;
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			return builder.create();
		}
	}


	private void updateDateTime(Date d) {
		mDisplayDate.setTime(d);
		mDateTextView.setText("for "+ DateUtils.getDateAsString(d, DateUtils.DATE_HEADER_FORMAT));
	}

	// for saving a photo: clear the photo imageview, update the 'previous photos' button
	private void saveImage() {
		if (mCurrentPhotoFilename != null) {
			mPhotoFilenames.add(mCurrentPhotoFilename);
		}
		updatePreviousPhotosText();
	}    

	private void clearImage() {
		mPhotoFrame.setImageBitmap(null);
		mCurrentPhotoFilename = null;
	}

	// show the camera button (not the delete icon)
	private void showCameraIcon() {
		if (mCameraButton == null) {
			mCameraButton = (ImageView) this.findViewById(R.id.camera_icon);
		}
		mCameraButton.setImageResource(R.drawable.camera_icon);
		mCameraButton.getLayoutParams().width = 84;
		mCameraButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				launchCamera();
			}
		});
	}

	// change the camera icon from the camera icon to the delete icon
	private void showDeleteIcon() {
		if (mCameraButton == null) {
			mCameraButton = (ImageView) this.findViewById(R.id.camera_icon);
		}
		mCameraButton.setImageResource(R.drawable.delete_icon);
		mCameraButton.getLayoutParams().width = 72;
		mCameraButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// remove the image from the box
				Toast.makeText(DiaperForm.this, R.string.photo_deleted, Toast.LENGTH_SHORT).show();
				clearImage();
				// hide the photo frame and show the previous photo textview
				mPhotoFrame.setVisibility(View.GONE);
				mPreviousPhotos.setVisibility(View.VISIBLE);
				// change the delete icon to a camera icon
				showCameraIcon();
				mSavePhotoButton.setVisibility(View.INVISIBLE);
			}
		});
	}

	// change the camera icon from the camera icon to the delete icon
	private void showSaveIcon() {
		if (mSavePhotoButton == null) {
			mSavePhotoButton = (ImageView) this.findViewById(R.id.save_icon);
		}
		// only save if there's an image to save
		if (mCurrentPhotoFilename != null) {
			mSavePhotoButton.setVisibility(View.VISIBLE);
			mSavePhotoButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// only save if there's an image to save
					if (mCurrentPhotoFilename != null) {
						saveImage();
						clearImage();
						Toast.makeText(DiaperForm.this, R.string.photo_saved, Toast.LENGTH_SHORT).show();
						// show the camera button (remove the delete icon)
						showCameraIcon();
						mSavePhotoButton.setVisibility(View.INVISIBLE);

						// hide the photo frame and show the previous photo count
						mPhotoFrame.setVisibility(View.GONE);
						mPreviousPhotos.setVisibility(View.VISIBLE);
					}
				}
			});
		}
		else {
			mSavePhotoButton.setVisibility(View.INVISIBLE);
		}
	}

	// starts built-in camera activity
	private void launchCamera() {
		//create parameters for Intent with filename
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
		// this tells the camera where to save the file
		values.put("_data", ImageUtils.getTempImageName());
		//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
		mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		//create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, CAMERA_ACTIVITY_ID);
	}

	// when the camera is done taking a photo, display it as a thumbnail 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_ACTIVITY_ID) {
			if (resultCode == RESULT_OK) {
				// show the thumbnail in the box
				// use imageUri here to access the image
				mCurrentPhotoFilename = convertImageUriToFilename(mImageUri);
				mPhotoFrame.setVisibility(View.VISIBLE);
				mPhotoFrame.setImageBitmap(ImageUtils.decodeFile(new File(mCurrentPhotoFilename)));

				// hide the textview which shows how many previous photos there are
				mPreviousPhotos.setVisibility(View.GONE);

				// change the camera icon from the camera icon to the delete icon
				CustomViews.createCenteredToast(this, ToastMessage.PHOTO_TIP).show();

				showDeleteIcon();
				showSaveIcon();
			} 
		}
	}

	public String convertImageUriToFilename (Uri imageUri)  {
		Cursor cursor = null;
		try {
			String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
			cursor = this.managedQuery( imageUri,
					proj, // Which columns to return
					null, // clause; which rows to return (all rows)
					null, // WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)
			int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			// REMOVED: check for orientation and then display thumbnail accordingly
			// int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				// REMOVED: check for orientation and then display thumbnail accordingly
				// String orientation =  cursor.getString(orientation_ColumnIndex);
				return cursor.getString(file_ColumnIndex);
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	// keeps track of the filenames for the photos that have been taken so far
	// these photos can be viewed using the flipper, but none of the photos can be deleted
	public void updatePreviousPhotosText() {
		if (mPhotoFilenames.size() == 0) {
			mPreviousPhotos.setText("you haven't saved any photos today");
			mPreviousPhotos.setClickable(false);
		}
		else {
			mPreviousPhotos.setText("you've saved " + StringUtils.pluralize(mPhotoFilenames.size(), "photo", "photos"));
			mPreviousPhotos.setClickable(true);
		}
	}

	// dialog that displays a bigger photo of the thumbnail 
	private Bitmap mBitmap;
	public AlertDialog SingleImagePreview(String filename) {    	
		ImageView iv = new ImageView(this);
		iv.setAdjustViewBounds(true);
		iv.setScaleType(ScaleType.FIT_CENTER);
		mBitmap = ImageUtils.decodeFile(new File(filename));
		iv.setImageBitmap(mBitmap);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(filename);
		builder.setView(iv);
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener()  {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				mBitmap = null;
			}
		});
		return builder.create();
	}

	// cleans up any image references
	protected void onFinish() {
		mPhotoFrame.setImageBitmap(null);
	}

	// grabs all the reported info and sends it back to the overview activity
	public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(mCurrentPhotoFilename != null) {
				unsavedImageDialog();
			} else {
				saveDiaper();
			}
		}
	};

	private void unsavedImageDialog() {
		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.unsaved_photo_title)
		.setMessage(R.string.unsaved_diaper_photo)
		.setPositiveButton(R.string.save_photo_and_continue, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveImage();
				saveDiaper();
			}
		})
		.setNegativeButton(R.string.no_save_photo_and_continue, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveDiaper();
			}
		})
		.show();
	}

	private void saveDiaper() {
		saveIndicator();
		Intent intent = new Intent();
		intent.putExtra("diaper", mDiaper);
		setResult(RESULT_OK, intent);
		finish();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileActivity#saveIndicator()
	 */
	@Override
	protected void saveIndicator() {
		if(mDiaper == null){
			mDiaper = new Diaper();
		}
		mDiaper.getCommonData().setIdUser(EstrellitaTiles.getParentId(this));
		mDiaper.getCommonData().setIdBaby(mBaby.getId());
		mDiaper.setWet(mWetNumPicker.getCurrent());
		mDiaper.setDirty(mDirtyNumPicker.getCurrent());
		mDiaper.setPhotoFilenames(mPhotoFilenames);
		// if you're updating yesterday's time
		if (mUpdatedYesterday) {
			Date d = DateUtils.getStartOfDay(mDisplayDate.getTime());
			if (mDiapers.size()>0) {
				d = mDiapers.get(0).getDateTime(); 
			}
			// HACK: adding a second to the latest timestamp, because we want to make sure what we're adding is the latest diaper indicator
			d = DateUtils.addToDate(d, Calendar.SECOND, 1); 
			mDiaper.getCommonData().setTimestamp(new Timestamp(d.getTime()));
		}

		mIndicator = mDiaper;
	}

	private void restoreIndicator() {
		mIndicator = null; 
		if (getLastNonConfigurationInstance() != null) { 
			mIndicator = (Diaper) getLastNonConfigurationInstance();
			mDiaper = mIndicator;
			mDisplayDate.setTime(mIndicator.getCommonData().getTimestamp());
		}
	}
}
