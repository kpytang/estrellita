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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.customview.CustomViews.ToastMessage;
import edu.uci.ics.star.estrellita.customview.ScrollList;
import edu.uci.ics.star.estrellita.object.indicator.Weight;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.ImageUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

// Notes for how the photos work for weight:
// you only take 1 photo for weight
// if you have taken a photo but haven't saved it yet, then you can preview it (and see a larger view of it)
// if you have not taken a photo, then you can click on the camera button
// if you have taken a photo and saved it, then you cannot see the image again and no icons will show up
public class WeightForm extends TileActivity<Weight> implements ListView.OnScrollListener {
	public static final int NUMBER_OF_SCALE_TICKS = 30;
	public static final int ZERO_OFFSET = 315;

	public static final int SIZE_OF_ONE_POUND = 100;

	public static final int NUMBER_OF_OUNCES_PER_POUND = 16;

	private static final int CAMERA_ACTIVITY_ID = 0;
	private static final int PICK_A_PHOTO_ID = 1;

	private boolean mReady = false;
	private TextView mPounds, mOunces;
	private ScrollList mScrollList;

	private ImageView mPhotoFrame, mSaveFolderPhotoButton, mCameraButton;
	private TextView mPreviousPhoto;
	private Uri mImageUri;

	private String mCurrentPhotoFilename = null;
	private String mSavedPhotoFilename = null;
	private Weight mWeight;
	private int lbs;
	private Double ozs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weight_form);

		// initialize header & footer
		setActivityHeader("add weight", true, Tile.WEIGHT);
		setButtonFooter("update weight", mSaveClickListener, null, null);

		mScrollList = (ScrollList) this.findViewById(R.id.scale_list);
		mScrollList.setOnScrollListener(this);
		mPounds = (TextView) this.findViewById(R.id.scale_lbs);
		mOunces = (TextView) this.findViewById(R.id.scale_ozs);

		ScaleListAdapter adapter = new ScaleListAdapter(this);
		mScrollList.setAdapter(adapter);

		// set up photo actions
		mPhotoFrame = (ImageView) this.findViewById(R.id.current_photo);
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

		// will remind users that they have already saved a photo
		mPreviousPhoto = (TextView) this.findViewById(R.id.previous_photos);
		mPreviousPhoto.setTypeface(regular);
		mPreviousPhoto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// if you previously saved a photo, so we should show the view flipper
				if ((mPreviousPhoto.getVisibility() == View.VISIBLE) && (mSavedPhotoFilename != null)) {
					Dialog d = SingleImagePreview(mSavedPhotoFilename);
					d.show();
				}
			}
		});

		// for saving a photo: show a toast message and hide all icons
		// else just show folder icon
		showSaveFolderIcon();

		updatePreviousPhotoText();

		lbs = getIntent().getIntExtra("lbs", -1);
		if(lbs != -1){
			ozs = Double.valueOf(getIntent().getIntExtra("ozs", -1));
			ozs *= 18.7;
			mScrollList.setSelectionFromTop(3+lbs, -(45)-(ozs.intValue()));
		}
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
		mCameraButton.setVisibility(View.VISIBLE);
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
				clearImage();
				// hide the photo frame and show the previous photo textview
				mPhotoFrame.setVisibility(View.GONE);
				mPreviousPhoto.setVisibility(View.VISIBLE);
				// change the delete icon to a camera icon
				showCameraIcon();
				showSaveFolderIcon();
			}
		});
		mCameraButton.setVisibility(View.VISIBLE);
	}

	// change the camera icon from the camera icon to the delete icon
	private void showSaveFolderIcon() {
		if (mSaveFolderPhotoButton == null) {
			mSaveFolderPhotoButton = (ImageView) this.findViewById(R.id.save_icon);
		}
		mSaveFolderPhotoButton.setVisibility(View.VISIBLE);
		// show save icon if there's an image to save
		if (mCurrentPhotoFilename != null) {
			mSaveFolderPhotoButton.setImageResource(R.drawable.save_icon);
			mSaveFolderPhotoButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// only save if there's an image to save
					if (mCurrentPhotoFilename != null) {
						saveImage(true);
//						clearImage();

						// show the camera button 
//						showCameraIcon();
						// show the folder icon
//						mSaveFolderPhotoButton.setImageResource(R.drawable.folder_icon);

						// hide the folder and save icons and the camera icon
						mPhotoFrame.setVisibility(View.VISIBLE);
						mPreviousPhoto.setVisibility(View.INVISIBLE);
						mCameraButton.setVisibility(View.INVISIBLE);
						mSaveFolderPhotoButton.setVisibility(View.INVISIBLE);
					}
				}
			});
		}
		// else show the folder icon
		else {
			mSaveFolderPhotoButton.setImageResource(R.drawable.folder_icon);
			mSaveFolderPhotoButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					launchPhotoPicker();
				}
			});
		}
	}

	private class ScaleListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Bitmap mScaleTicks;

		public ScaleListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			mScaleTicks = BitmapFactory.decodeResource(context.getResources(), R.drawable.scale);
		}

		// includes a buffer so that we can get access to the ends of the scale
		public int getCount() {
			return NUMBER_OF_SCALE_TICKS+10;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// ASSUMPTION: we're never going to need a scale reading that's less than 1 lbs
		public View getView(int position, View convertView, ViewGroup parent) {            
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.scale_list_item, null);
			} 

			if (position >= 0 && position <= 2) {
				convertView = mInflater.inflate(R.layout.empty_list_item, null);
			}
			else if (position <= (NUMBER_OF_SCALE_TICKS+3)) {
				convertView = mInflater.inflate(R.layout.scale_list_item, null);
				ImageView iv = (ImageView) convertView.findViewById(R.id.scale_ticks);
				// the images have a gap between them otherwise
				iv.setPadding(0, -2, 0, 0);
				TextView tv = (TextView) convertView.findViewById(R.id.scale_labels);
				tv.setPadding(10, 0, 0, 0);
				iv.setImageBitmap(mScaleTicks);	            
				tv.setText(Integer.toString(position-3));
			}
			else {
				convertView = mInflater.inflate(R.layout.empty_list_item, null);
			}
			return convertView;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mReady = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mReady = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mReady = false;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mReady) {
			float ounces = ((ScrollList)view).getScrollListOffset();
			ounces -= ZERO_OFFSET;
			int lbs = 0;

			lbs = Double.valueOf(Math.floor(ounces / SIZE_OF_ONE_POUND)).intValue();
			ounces = ounces % SIZE_OF_ONE_POUND;
			ounces /= SIZE_OF_ONE_POUND;

			ounces *= NUMBER_OF_OUNCES_PER_POUND;

			if(Double.valueOf(Math.round(ounces)).intValue() == NUMBER_OF_OUNCES_PER_POUND){
				ounces = 0;
				lbs ++;
			}

			if (lbs < 1) {
				mPounds.setText("0 lbs");
			}
			else {
				mPounds.setText(Integer.toString(lbs) + " lbs");
			}
			if (ounces < 0) {
				mOunces.setText("0 ozs");
			}
			else if (ounces < NUMBER_OF_OUNCES_PER_POUND) {
				String ouncesString = String.format("%.0f", ounces);
				StringUtils.trimTrailingZeros(String.format("%.0f", ounces));
				mOunces.setText(ouncesString + " ozs");
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	View.OnClickListener mPhotoClickListener = new OnClickListener() {
		public void onClick(View v) {
			// if there's no photo already showing, then start up the camera
			if (mCurrentPhotoFilename == null) {
				launchCamera();
			}
			// if there is a photo showing, then clicking it will load up a dialog with a bigger photo of it
			else {
				Dialog d = SingleImagePreview(mCurrentPhotoFilename);
				d.show();
			}
		}
	};

	// starts built-in camera activity
	private void launchCamera() {
		//create parameters for Intent with filename
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DESCRIPTION,"Image capture by camera");
		// this tells the camera where to save the file
		values.put("_data", ImageUtils.getTempImageName());
		//imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
		mImageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		//create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, CAMERA_ACTIVITY_ID);
	}

	// starts built-in camera activity
	private void launchPhotoPicker() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, PICK_A_PHOTO_ID);
	}

	// when the camera is done taking a photo, display it as a thumbnail 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case PICK_A_PHOTO_ID:
			if(resultCode == RESULT_OK){  
				mImageUri = data.getData();
				//SAME AS Camera : show the image 
			}
		case CAMERA_ACTIVITY_ID:
			if (resultCode == RESULT_OK) {
				// show the thumbnail in the box
				//use imageUri here to access the image
				mCurrentPhotoFilename = convertImageUriToFilename(mImageUri, this);
				mPhotoFrame.setVisibility(View.VISIBLE);
				mPhotoFrame.setImageBitmap(ImageUtils.decodeFile(new File(mCurrentPhotoFilename)));

				// hide the textview which shows how many previous photos there are
				mPreviousPhoto.setVisibility(View.GONE);

				// change the camera icon from the camera icon to the delete icon
				CustomViews.createCenteredToast(this, ToastMessage.PHOTO_TIP).show();

				showDeleteIcon();
				showSaveFolderIcon();
			}
			break;
		}
	}

	public static String convertImageUriToFilename (Uri imageUri, Activity activity)  {
		Cursor cursor = null;
		try {
			String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
			cursor = activity.managedQuery( imageUri,
					proj, // Which columns to return
					null,       // WHERE clause; which rows to return (all rows)
					null,       // WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)
			int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			// REMOVED: check for orientation and then display accordingly
			// int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				// REMOVED: check for orientation and then display accordingly
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

	private Bitmap mBitmap;
	public AlertDialog SingleImagePreview(String filename) {    	
		ImageView iv = new ImageView(this);
		iv.setAdjustViewBounds(true);
		iv.setScaleType(ScaleType.FIT_CENTER);
		// REMOVED: take care of orientation of the image
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
			Dialog d;
			if ( (mCurrentPhotoFilename != null) && (mSavedPhotoFilename == null) ) {
				d = UnsavedImageDialog();
				d.show();
			}
			else if ( (mCurrentPhotoFilename == null) && (mSavedPhotoFilename == null)) {
				d = ValidateFormDialog();
				d.show();
			}
			else {
				d = ConfirmSubmitDialog();
				d.show();
			}
		}
	};

	private AlertDialog UnsavedImageDialog() {
		return new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.unsaved_photo_title)
		.setMessage(R.string.unsaved_weight_photo)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveImage(false);
				Dialog d = ConfirmSubmitDialog();
				d.show();
				dialog.dismiss();
			}
		})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
	}

	public AlertDialog ValidateFormDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Missing information");
		builder.setMessage(R.string.missing_weight_photo)
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder.create();
	}

	public AlertDialog ConfirmSubmitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.check_weight);
		builder.setMessage(getString(R.string.submit_weight) + " " + mPounds.getText().toString() + ", " + mOunces.getText().toString() + "?")
		.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mWeight = makeWeightObject();
				saveWeight();
			}
		});
		return builder.create();
	}

	private void saveWeight() {
		Intent intent = new Intent();
		mWeight.getCommonData().setTimestamp(DateUtils.getTimestamp());
		intent.putExtra("weight", mWeight);
		intent.putExtra("photo", mSavedPhotoFilename);
		setResult(RESULT_OK, intent);
		finish();
	}

	private Weight makeWeightObject() {
		Weight w = new Weight();
		w.getCommonData().setIdUser(EstrellitaTiles.getParentId(this));
		w.getCommonData().setIdBaby(mBaby.getId());
		String s = mPounds.getText().toString();
		s = s.substring(0, s.indexOf(' '));
		w.setPounds(Integer.parseInt(s));
		s = mOunces.getText().toString();
		s = s.substring(0, s.indexOf(' '));
		w.setOunces(Integer.parseInt(s));
		return w;
	}

	private void saveImage(boolean showToast) {
		if (showToast) {
			Toast.makeText(WeightForm.this, R.string.photo_saved, Toast.LENGTH_SHORT).show();
		}
		if (mCurrentPhotoFilename != null) {
			mSavedPhotoFilename = mCurrentPhotoFilename;
		}
		updatePreviousPhotoText();
	}

	private void clearImage() {
		mPhotoFrame.setImageBitmap(null);
		mCurrentPhotoFilename = null;
	}

	// keeps track of the filenames for the photos that have been taken so far
	// these photos can be viewed using the flipper, but none of the photos can be deleted
	public void updatePreviousPhotoText() {
		if (mSavedPhotoFilename == null) {
			mPreviousPhoto.setText("you have not saved a photo yet");
		}
		else {
			mPreviousPhoto.setText("you have already saved a photo");
		}
		mPreviousPhoto.setClickable(false);
	}
}
