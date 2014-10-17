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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.sharedprefs.api.PreferencesApi;

public class Utilities {

	private static final String SYNC = "edu.uci.ics.estrellita.SYNC";
	public static final String ESTRELLITA_DIR = Environment.getExternalStorageDirectory() + "/estrellita/";
	private static final String ESTRELLITA_LOG_TXT = "estrellitaLog.txt";
	private static final String ESTRELLITA_ERROR_LOG_PREFIX = "estrellitaErrorLog_";
	private static final String TXT_FILETYPE_EXTENSION = ".txt";
	public static final String EDU_UCI_ICS_STAR_ESTRELLITA_WIDGET_MAIN_WIDGET_PROVIDER = "edu.uci.ics.star.estrellita.widget.MainWidgetProvider";
	public static final String EDU_UCI_ICS_STAR_ESTRELLITA_WIDGET_MAIN_WIDGET_PROVIDER_WIDE = "edu.uci.ics.star.estrellita.widget.MainWidgetProviderWide";
	public static final String EDU_UCI_ICS_STAR_ESTRELLITA = "edu.uci.ics.star.estrellita";
	public static final String TITLE = "title";
	public static final String CHILD_ID = "childId";

	public static final String TAG = "ESTRELLITA";
	public static final int WEIGHT_PIC_REQUEST = 0;
	public static final int DIAPER_PIC_REQUEST = 1;
	public static final String ACTION_NOTIFY = "estrellita.intent.action.NOTIFY";
	public static final String ACTION_CHANGE_CONTENT = "estrellita.intent.action.CHANGE_CONTENT";
	public static final String ACTION_UPDATE_MAIN = "estrellita.intent.action.UPDATE_MAIN";
	
	public static final String CATEGORY_INTENT_PREFIX = "estrellita.intent.category.";
	public static final String ACTION_UPDATE = "edu.uci.ics.estrellita.UPDATE_WIDGET";
	public static final String ACTION_UPDATE_WIDE = "edu.uci.ics.estrellita.UPDATE_WIDGET_WIDE";
	
	public static final String WIDGET_ID = "widget_id";
	public static final int PIE_CHART = 2;


	public static final int NO_LIMIT = -1;
	public static final String DEFAULT_DELIMITER = "" + (char)181;
	public static final String PASTE_DELIMITER = "" + (char)255;

	/**
	 * Prints an object to logcat
	 * @param output
	 */
	public static void println(Object output) {
		if (output == null) {
			println("null");
		} else {
			println(output.toString());
		}
	}

	/**
	 * Prints an int array to logcat
	 * @param output
	 */
	public static void println(int[] output) {
		if (output == null) {
			println("null");
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i : output) {
				sb.append(i);
				sb.append(",");
			}
			if(sb.length() > 0){
				sb.deleteCharAt(sb.length()-1);
			} else {
				sb.append("NULL");
			}
			println(sb.toString());
		}
	}

	/**
	 * Prints a string to logcat
	 * @param output
	 */
	public static void println(String output) {
		android.util.Log.println(android.util.Log.DEBUG, TAG, output);
	}

	/**
	 * writes a string to a file in the estrellita director
	 * the format of the log is "timestamp:  text\n"
	 * timestamp is created by formatReadableTimestamp and
	 * text is supplied in the arguments of this function
	 * @param text
	 */
	public static void writeToFileOnSD(String filename, String msg){
		try {
		    File estrellitaDir = new File(ESTRELLITA_DIR);
		    if (estrellitaDir.canWrite()){
		        File gpxfile = new File(estrellitaDir, filename);
		        FileWriter gpxwriter = new FileWriter(gpxfile, true);
		        BufferedWriter out = new BufferedWriter(gpxwriter);
		        out.write(DateUtils.formatReadableTimestamp(DateUtils.getTimestamp()));
				out.write(":   ");
				out.write(msg);
				out.write("\n");
				out.flush();
				out.close();
		    }
		} catch (Exception e) {
//		    Utilities.println("Could not write file " + e);
		}
	}
	
	public static void writeToLogFile(String msg) {
		writeToFileOnSD(ESTRELLITA_LOG_TXT, msg);
	}
	
	public static void writeToWeeklyErrorLogFile(Context context, String msg) {
		// check to see if there exists a log for this week
		String filename = getWeeklyLogFilename();
		writeToFileOnSD(filename, msg);
		PreferencesApi.putBoolean(context, PreferencesApi.NEED_TO_UPLOAD_ERROR_LOG, true);
	}
	
	public static void writeToWeeklyErrorLogFile(Context context, Throwable err) {
		// check to see if there exists a log for this week
		String filename = getWeeklyLogFilename();
		writeToFileOnSD(filename, getStackTrace(err));
		PreferencesApi.putBoolean(context, PreferencesApi.NEED_TO_UPLOAD_ERROR_LOG, true);
	}


	public static String getStackTrace(Throwable aThrowable) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	  }

	
	public static String getWeeklyLogFilename() {
		Date thisWeek = DateUtils.getDateOfPreviousSunday(DateUtils.getTimestamp());
		// get the filename, format will be = estrellitaErrorLog_2012-02-09.txt
		String filename = ESTRELLITA_ERROR_LOG_PREFIX + DateUtils.getDateAsString(thisWeek, DateUtils.DATE_FORMAT) + TXT_FILETYPE_EXTENSION;
		return filename;
	}

	/**
	 * Updates the appwidget for Estrellita
	 * @param context
	 */
	public static void updateWidget(Context context) {
		context.sendBroadcast(new Intent(ACTION_UPDATE_WIDE));
	}
	
	/**
	 * Updates the appwidget for Estrellita
	 * @param context
	 */
	public static void sync(Context context) {
		context.sendBroadcast(new Intent(SYNC)); 
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int bytel = read();
					if (bytel < 0) {
						break;  // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
	
	public static byte[] getBytesFromFile(String filename) throws IOException {
		return getBytesFromFile(new File(filename));
	}
		
	/**
	 * Takes a file and returns it as a byte[]
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
	public static Intent getIntentForContentTitle(Context context, String contentTitle, int childId){
		Intent intent = new Intent(context, EstrellitaTiles.class);

		intent.setAction(Utilities.ACTION_CHANGE_CONTENT);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
		intent.setData(Uri.parse("estrellita://intent/test?" + CHILD_ID + "="+childId + "&" + TITLE + "="+contentTitle));

		return intent;
	}

	
	public static float megabytesAvailable(String path) {
	    StatFs stat = new StatFs(path);
	    long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
	    return bytesAvailable / (1024.f * 1024.f);
	}

	public static void callPhoneNumber(String phoneNumber, Context context) {
		Intent dialerIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phoneNumber));
		context.startActivity(dialerIntent);
	}
}