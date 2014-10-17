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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import uk.co.jasonfry.android.tools.ui.SwipeView.OnPageChangedListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.utils.Utilities.FlushedInputStream;

public class ImageUtils {

	public static final String ESTRELLITA_IMAGES = "estrellita/images";
	/**
	 * Takes a bitmap and returns it as a byte[]
	 * @param bitmap
	 * @return
	 */
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		if(bitmap == null){
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
	}

	/**
	 * Takes a byte[] and returns it as a Bitmap
	 * @param bytearray
	 * @return
	 */
	public static Bitmap toBitmap(byte[] bytearray){
		if(bytearray == null){
			return null;
		}
		ByteArrayInputStream is = new ByteArrayInputStream(bytearray);
		return BitmapFactory.decodeStream(is);
	}

	/*
	 * tries to decode an image from a url
	 */
	public static Bitmap urlToBitmap(String url){
		if (url.length()>0) {
			try {
				InputStream is = (InputStream) new URL(url).getContent();
				return BitmapFactory.decodeStream(new FlushedInputStream(is));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Bitmap getBackground (int bgcolor)
	{
		try
		{
			Bitmap.Config config = Bitmap.Config.ARGB_8888; // Bitmap.Config.ARGB_8888 Bitmap.Config.ARGB_4444 to be used as these two config constant supports transparency
			Bitmap bitmap = Bitmap.createBitmap(2, 2, config); // Create a Bitmap

			Canvas canvas =  new Canvas(bitmap); // Load the Bitmap to the Canvas
			canvas.drawColor(bgcolor); //Set the color

			return bitmap;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static Bitmap resize(Bitmap bitmap, float newWidth) {
		int width2 = bitmap.getWidth();
		int height2 = bitmap.getHeight();

		float scale;
		if(width2 > height2){
			scale = newWidth / Float.valueOf(bitmap.getWidth());
		} else {
			scale = newWidth / Float.valueOf(bitmap.getHeight());
		}

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scale, scale);

		// recreate the new Bitmap
		return Bitmap.createBitmap(bitmap, 0, 0, width2, 
				height2, matrix, true);
	}


	public static File getTempFile(Context context){
		//it will return /sdcard/image.tmp
		final File path = new File( Environment.getExternalStorageDirectory(), context.getPackageName() );
		if(!path.exists()){
			path.mkdir();
		}
		return new File(path, "image.tmp");
	}

	public static String getTempImageName(){
		//it will return /sdcard/image.tmp
		File path = getImageFilePath();
		return new File(path, "image_"+DateUtils.getNowString(DateUtils.PICTURE_DATE_TIME_FORMAT)+".jpg").getAbsolutePath();
	}

	public static File getImageFilePath() {
		File path = new File( Environment.getExternalStorageDirectory(), ESTRELLITA_IMAGES );
		if(!path.exists()){
			path.mkdir();
		}
		return path;
	}

	
	public static Bitmap decodeFile(String pathToFile) {
		return decodeFile(new File(pathToFile));
	}
	
	// decodes image and scales it to reduce memory consumption
	public static Bitmap decodeFile(File f){
		try {
			//Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,o);

			//The new size we want to scale to
			final int REQUIRED_SIZE=400;

			//Find the correct scale value. It should be the power of 2.
			int width_tmp=o.outWidth, height_tmp=o.outHeight;
			int scale=1;
			while(true){
				if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
					break;
				width_tmp/=2;
				height_tmp/=2;
				scale*=2;
			}

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {}
		return null;
	}

	// stores bitmap with the given filename 
	public static void saveBitmapToFile(Bitmap bitmap, String filename) {
		File file = new File(filename);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveBitmapToFile(bitmap, file);
	}
	
	public static void saveBitmapToFile(Bitmap bitmap, File file) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
		try {
		
			//write the bytes in file
			FileOutputStream fo = new FileOutputStream(file);
			fo.write(bytes.toByteArray());
			fo.close();
			bytes.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveBitmapToPNGFile(Bitmap bitmap, File file) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 80, bytes);
		try {
		
			//write the bytes in file
			FileOutputStream fo = new FileOutputStream(file);
			fo.write(bytes.toByteArray());
			fo.close();
			bytes.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// dialog that flips through all the previous photos
	// this will only be called if there is at least 1 previous photo taken
	private static SwipeView mSwipeView;
	public static AlertDialog createPhotoFlipper(Context context, View photoFlipperLayout, List<String> filenames, boolean showTimestamps) {
		// make sure we're calling removeView(), or we won't be able to consecutively call this 
		if (photoFlipperLayout != null) {
			((ViewGroup)photoFlipperLayout.getParent()).removeView(photoFlipperLayout);
		} 
		photoFlipperLayout = LayoutInflater.from(context).inflate(R.layout.photo_flipper, null);

		// now we have a proper photoFlipperLayout and can start populating it
		PageControl mPageControl = (PageControl) photoFlipperLayout.findViewById(R.id.page_control);
		mSwipeView = (SwipeView) photoFlipperLayout.findViewById(R.id.swipe_view);

		for(int i=0; i<filenames.size();i++)
		{
			mSwipeView.addView(new FrameLayout(context));
		}

		// load at least the first image (two, if it's available)
		// will show the photo, above a timestamp
		ImageView iv = new ImageView(context);
		File file = new File(filenames.get(0));
		iv.setImageBitmap(ImageUtils.decodeFile(file));
		((FrameLayout) mSwipeView.getChildContainer().getChildAt(0)).addView(iv);
		if (showTimestamps) {
			TextView tv = new TextView(context);
			tv.setText("Photo Taken on: " + DateUtils.getDateAsString(new Date(file.lastModified()), DateUtils.DATE_AND_TIME_FORMAT_FULL));
			((FrameLayout) mSwipeView.getChildContainer().getChildAt(0)).addView(tv);
		}

		if (filenames.size() >= 2) {
			iv = new ImageView(context);
			file = new File(filenames.get(1));
			iv.setImageBitmap(ImageUtils.decodeFile(file));
			((FrameLayout) mSwipeView.getChildContainer().getChildAt(1)).addView(iv);
			if (showTimestamps) {
				TextView tv = new TextView(context);
				tv.setText("Photo Taken on: " + DateUtils.getDateAsString(new Date(file.lastModified()), DateUtils.DATE_AND_TIME_FORMAT_FULL));
				((FrameLayout) mSwipeView.getChildContainer().getChildAt(1)).addView(tv);
			}
		}

		SwipeImageLoader mSwipeImageLoader = new SwipeImageLoader(context, filenames, showTimestamps);

		mSwipeView.setOnPageChangedListener(mSwipeImageLoader);
		mSwipeView.setPageControl(mPageControl);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Previous Photos");
		builder.setView(photoFlipperLayout);
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener()  {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}

	private static class SwipeImageLoader implements OnPageChangedListener
	{
		private Context mContext;
		private List<String> mFilenames;
		private boolean mShowTimestamps;

		public SwipeImageLoader(Context context, List<String> filenames, boolean showTimestamps) {
			mContext = context;
			mFilenames = filenames;
			mShowTimestamps = showTimestamps;
		}

		public void onPageChanged(int oldPage, int newPage) 
		{
			if(newPage>oldPage)//going forwards
			{
				if(newPage != (mSwipeView.getPageCount()-1))//if at the end, don't load one page after the end
				{
					ImageView iv = new ImageView(mContext);
					File file = new File(mFilenames.get(newPage+1));
					iv.setImageBitmap(ImageUtils.decodeFile(file));
					((FrameLayout) mSwipeView.getChildContainer().getChildAt(newPage+1)).addView(iv);
					if (mShowTimestamps) {
						TextView tv = new TextView(mContext);
						tv.setText("Photo Taken on: " + DateUtils.getDateAsString(new Date(file.lastModified()), DateUtils.DATE_AND_TIME_FORMAT_FULL));
						((FrameLayout) mSwipeView.getChildContainer().getChildAt(newPage+1)).addView(tv);
					}
				}
				if(oldPage!=0)//if at the beginning, don't destroy one before the beginning
				{
					((FrameLayout) mSwipeView.getChildContainer().getChildAt(oldPage-1)).removeAllViews();
				}
			}
			else //going backwards
			{
				if(newPage!=0)//if at the beginning, don't load one before the beginning
				{
					ImageView iv = new ImageView(mContext);
					File file = new File(mFilenames.get(newPage-1));
					iv.setImageBitmap(ImageUtils.decodeFile(file));
					((FrameLayout) mSwipeView.getChildContainer().getChildAt(newPage-1)).addView(iv);
					if (mShowTimestamps) {
						TextView tv = new TextView(mContext);
						tv.setText("Photo Taken on: " + DateUtils.getDateAsString(new Date(file.lastModified()), DateUtils.DATE_AND_TIME_FORMAT_FULL));
						((FrameLayout) mSwipeView.getChildContainer().getChildAt(newPage-1)).addView(tv);
					}
				}
				if(oldPage != (mSwipeView.getPageCount()-1))//if at the end, don't destroy one page after the end
				{
					((FrameLayout) mSwipeView.getChildContainer().getChildAt(oldPage+1)).removeAllViews();
				}
			}

		}
	}
}
