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
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.view.MotionEvent;

public class MoodMapClickableCanvas extends MoodMapBaseCanvas {
	private static final float CIRCLE_RADIUS = 8;

	// so we can avoid over-drawing when we don't really need to 
	private static final float TOUCH_TOLERANCE = 4;

	private float mLastX = 0, mLastY = 0;
	private boolean mCirclePresent;

	private Bitmap mCurrentMoodLayer;
	private Canvas mCanvas;
	private Paint mCurrentMoodPaint;

	public MoodMapClickableCanvas(Context context) {
		super(context, MoodMapBaseCanvas.MOODMAP_WIDTH, MoodMapBaseCanvas.MOODMAP_HEIGHT);

		// sets the canvas which will hold the heat map circles
		mCurrentMoodLayer = Bitmap.createBitmap(MoodMapBaseCanvas.MOODMAP_WIDTH, MoodMapBaseCanvas.MOODMAP_HEIGHT, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mCurrentMoodLayer);
		mCurrentMoodPaint = new Paint(Paint.DITHER_FLAG);
		mCurrentMoodPaint.setColor(Color.RED);
		mCurrentMoodPaint.setStyle(Paint.Style.FILL);
		
		mCirclePresent = false;
	}


	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mCurrentMoodLayer, 0, 0, mCurrentMoodPaint);
	}

	private void processEvent(float x, float y) {
		// resetting the x & y values if you've touched beyond the boundary of the mood map
		if (x<0) {
			x = 0;
		}
		else if (x>MOODMAP_WIDTH) {
			x = MOODMAP_WIDTH;
		}
		if (y<0) {
			y = 0;
		}
		else if (y>MOODMAP_HEIGHT) {
			y = MOODMAP_HEIGHT;
		}

		float dx = Math.abs(x - mLastX);
		float dy = Math.abs(y - mLastY);

		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			drawCircle(x,y);
		}
	}
	
	private void drawCircle(float x, float y) {
		// clears the canvas
		mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		// draws a new circle
		mCanvas.drawCircle(x,y, CIRCLE_RADIUS, mCurrentMoodPaint);
		mLastX = x;
		mLastY = y;
		mCirclePresent = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			processEvent(event.getX(), event.getY());
			invalidate();
			break;
		}
		return true;
	}

	public float getX() {
		if (mCirclePresent) {
			float x = mLastX / mXChunkSize;
			return (x - X_BINS/2);
		}
		return -Integer.MAX_VALUE;
	}

	public float getY() {
		if (mCirclePresent) {
			float y = mLastY / mYChunkSize;
			if (y < (Y_BINS/2)) {
				return (Math.abs((int)y - Y_BINS/2));
			}
			else {
				return (-1*((int)y - Y_BINS/2));
			}
		}
		return -Integer.MAX_VALUE;
	}

	public void setMood(PointF p) {
		float x = p.x, y = p.y;
		x += X_BINS/2;
		x *= mXChunkSize;

		if (p.y > 0) {
			y = Y_BINS/2 - y;
		}
		else {
			y *= -1;
			y += Y_BINS/2;
		}
		y *= mYChunkSize;

		drawCircle(x,y);
		invalidate();
	}
}