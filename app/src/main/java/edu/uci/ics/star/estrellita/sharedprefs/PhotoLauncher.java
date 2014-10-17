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

package edu.uci.ics.star.estrellita.sharedprefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.db.Database;
import edu.uci.ics.star.estrellita.object.Baby;
import edu.uci.ics.star.estrellita.object.User;

public class PhotoLauncher extends Activity {
	public static final String BABY_ID = "baby_id";
	public static final String PARENT_ID = "parent_id";
	private static final int PICK_A_PHOTO_ID = 0;
	public static final String IMAGES_DIR = "/estrellita/images";
	public static final String BABY_PHOTO_PREFIX = "/baby";
	public static final String USER_PHOTO_PREFIX = "/user";
	public static final String PHOTO = "photo";
	private static final String BACKUP = "_backup";
	public static final String PNG_EXTENSION = ".png";
	public static final String IMAGE_DIR_PATH = Environment.getExternalStorageDirectory().toString() + IMAGES_DIR;
	private static final int NUMBER_PADDING = 4;

	private int mBabyId = -1;
	private int mUserId = -1;

	private Database mDatabase;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mBabyId = getIntent().getIntExtra(BABY_ID, -1);
		mUserId = getIntent().getIntExtra(PARENT_ID, -1);
		mDatabase = new Database(this);

		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, PICK_A_PHOTO_ID); 
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean success = false;

		switch(requestCode) { 
		case PICK_A_PHOTO_ID:
			try {
				if(resultCode == RESULT_OK){  
					Uri selectedImageUri = data.getData();
					String[] filePathColumn = {MediaStore.Images.Media.DATA};

					Cursor cursor = managedQuery( selectedImageUri,
							filePathColumn, // Which columns to return
							null,       // WHERE clause; which rows to return (all rows)
							null,       // WHERE clause selection arguments (none)
							null); // Order-by clause (ascending by name)
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);;
					String selectPhotoFilePath = cursor.getString(columnIndex);
					cursor.close();

					// now save this photo into the estrellita folder
					// first make sure the images directory exists
					File imageDir = new File(IMAGE_DIR_PATH);
					boolean exists = imageDir.exists();
					if (!exists) {
						boolean created = imageDir.mkdir();
					} 

					String destFilePrefix = "";
					if (mBabyId != -1) {
						destFilePrefix = IMAGE_DIR_PATH + BABY_PHOTO_PREFIX + mBabyId + PHOTO;
					}
					else if (mUserId != -1) {
						destFilePrefix = IMAGE_DIR_PATH + USER_PHOTO_PREFIX + mUserId + PHOTO;
					}

					FileInputStream fin = null;
					FileOutputStream fout = null;
					File destFile = new File(destFilePrefix + PNG_EXTENSION);
					// if this file exists, we should create a back up of it before overwriting it
					if (destFile.exists()) {
						final String destFileCopyPrefix = destFilePrefix.substring(IMAGE_DIR_PATH.length()+1) + BACKUP;
						// look through the past backups
						imageDir = new File(IMAGE_DIR_PATH);
						FilenameFilter filter = new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return (name.startsWith(destFileCopyPrefix)) && (name.endsWith(PNG_EXTENSION));
							}
						};
						List<String> backupFilenames = Arrays.asList(imageDir.list(filter));
						String number = String.format("%04d", 0);
						if (backupFilenames.size()>0) {
							// sort these filenames alphabetically
							Collections.sort(backupFilenames);
							// find the last filename
							String lastBackupFilename = backupFilenames.get(backupFilenames.size()-1);
							// get the backup number
							number = lastBackupFilename.substring(destFileCopyPrefix.length(), destFileCopyPrefix.length()+NUMBER_PADDING);
							number = getNextNumberString(number);
						}
						File backupDestFile = new File(destFilePrefix + BACKUP + number + PNG_EXTENSION);
						// create FileInputStream object for original destination file
						fin = new FileInputStream(destFile);
						// create FileOutputStream object for copy of destination file
						fout = new FileOutputStream(backupDestFile);

						// read bytes from original destination file and write to second copy of destination file
						byte[] b = new byte[1024];
						int noOfBytes = 0;
						while( (noOfBytes = fin.read(b)) != -1 ) {
							fout.write(b, 0, noOfBytes);
						}
						// close the streams
						fin.close();
						fout.close();	
					}

					// now copy the selected file with the new filename
					File sourceFile = new File(selectPhotoFilePath);
					// create FileInputStream object for source file
					fin = new FileInputStream(sourceFile);
					// create FileOutputStream object for destination file
					fout = new FileOutputStream(destFile);

					// read bytes from original destination file and write to second copy of destination file
					byte[] b = new byte[1024];
					int noOfBytes = 0;
					while( (noOfBytes = fin.read(b)) != -1 ) {
						fout.write(b, 0, noOfBytes);
					}
					// close the streams
					fin.close();
					fout.close();	

					success = true;
					break;
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(resultCode == RESULT_OK){ 
			// finish this "hidden" activity on any resultcode
			try {
				Dialog d;
				if (success) {
					// we're updating the baby photo
					if (mBabyId != -1) {
						Baby baby = mDatabase.getBabyTable().getBaby(mBabyId, this);
						String filename = IMAGE_DIR_PATH + BABY_PHOTO_PREFIX + mBabyId + PHOTO + PNG_EXTENSION;
						baby.getCommonData().setAndroidFilename(filename);
						mDatabase.update(baby, true);
					}
					// else we're updating the user photo
					else if (mUserId != -1) {
						User user = mDatabase.getUserTable().getUser(mUserId, this);
						String filename = IMAGE_DIR_PATH + USER_PHOTO_PREFIX + mUserId + PHOTO + PNG_EXTENSION;
						user.getCommonData().setAndroidFilename(filename);
						mDatabase.update(user, true);
					}
					d = finishDialog("Photo has been successfully updated.");
				} 
				else {
					d = finishDialog("Could not update photo. Please try again later.");
				}
				d.show();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Dialog finishDialog(String message) {
		return new AlertDialog.Builder(this)
		.setIcon(R.drawable.ic_dialog_info)
		.setTitle("Photo Update")
		.setMessage(message)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		}).create();
	}

	private String getNextNumberString(String number) {
		try {
			Integer i = Integer.parseInt(number);
			return String.format("%04d", (i+1));
		}
		catch (Exception e) {
			return "";
		}
	}
}