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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

public class MoodMapBaseCanvas extends View {
	protected static final int X_BINS = 40;
	protected static final int Y_BINS = 40;

	public static final int HEATMAP_HEIGHT = 432;
	public static final int HEATMAP_WIDTH = 448;
	public static final int MOODMAP_HEIGHT = 460;
	public static final int MOODMAP_WIDTH = 445;

	private Bitmap mMoodMapImage;
	protected int mXChunkSize, mYChunkSize;

	public MoodMapBaseCanvas(Context context, int width, int height) {
		super(context);

		// sets the mood map background
//		mMoodMapImage = BitmapFactory.decodeResource(getResources(), R.drawable.moodmap_intel, null);
//		mMoodMapImage = Bitmap.createScaledBitmap(mMoodMapImage, width, height, false);
		
		calculateChunkSize(width, height);
	}

	public void calculateChunkSize(int width, int height) {
		mXChunkSize = width/X_BINS;
		mYChunkSize = height/Y_BINS;
	}

	@Override 
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(mMoodMapImage, 0, 0, null);
	}
}