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

package edu.uci.ics.star.estrellita.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import edu.uci.ics.star.estrellita.customview.MoodMapBaseCanvas;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.PointUtils;

public class HeatMap extends MoodMapBaseCanvas {
	public interface OnHeatMapTouchListener {
		public void OnHeatMapTouched(View v, List<Integer> closestDataIndices);
	};

	private static final float HEATCIRCLE_RADIUS = 25;
	private static final int MIN_ALPHA = 0x50;
	private static final int ALPHA_STEP_SIZE = 0x04;
	private static final int COLOR_FILTER=0xDDDDDD;
	private static final float RADIUS_STEP_SIZE = 1.05f;
	private static final int HEATCIRCLE_MIN_COLOR = Color.RED;
	private static final int HEATCIRCLE_MAX_COLOR = Color.YELLOW;

	private static final float NEAREST_DISTANCE_THRESHOLD = 20;

	private Paint mHeatMapPaint, mHeatCircleHighlightPaint;
	private Path mHeatCircleHighlight;
	private List<Path> mHeatCirclePaths;
	private List<Paint> mHeatCirclePaints;
	private float[] mMinHSV, mMaxHSV;

	private List<PointF> mData, mHeatCircleCoords;
	private Map<String, Integer> mCountMap;

	private OnHeatMapTouchListener mListener;

	// the default implementation requires each view to have an id. Adding this means we don't get a
	// warning like this, "couldn't save which view has focus because the focused view X has no id".
	private static final int HEATMAP_VIEW_ID = 123456789;

	public HeatMap(Context context, List<PointF> data) {
		super(context, MoodMapBaseCanvas.HEATMAP_WIDTH, MoodMapBaseCanvas.HEATMAP_HEIGHT);
		setFocusable(true);
		setFocusableInTouchMode(true);

		// sets the canvas which will hold the heatmap circles
		mHeatMapPaint = new Paint();
		mHeatMapPaint.setColorFilter(new LightingColorFilter(COLOR_FILTER, 1));
		
		mHeatCircleHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mHeatCircleHighlightPaint.setStyle(Paint.Style.STROKE);
		mHeatCircleHighlightPaint.setStrokeWidth(10);
		mHeatCircleHighlightPaint.setColor(Color.YELLOW);
		mHeatCircleHighlight = null;
		
		mMinHSV = new float[3];
		mMaxHSV = new float[3];
		Color.colorToHSV(HEATCIRCLE_MIN_COLOR, mMinHSV);
		Color.colorToHSV(HEATCIRCLE_MAX_COLOR, mMaxHSV);

		mData = data;
		translatePointsToCoords();
		convertToCountBasedMap();

		drawHeatCircles();

		setId(HEATMAP_VIEW_ID);
	}

	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for(int i=0; i<mHeatCirclePaths.size(); i++) {
			canvas.drawPath(mHeatCirclePaths.get(i), mHeatCirclePaints.get(i));
		}
		if (mHeatCircleHighlight != null) {
			canvas.drawPath(mHeatCircleHighlight, mHeatCircleHighlightPaint);
		}
	}

	private void translatePointsToCoords() {
		mHeatCircleCoords = new ArrayList<PointF>();
		for(int i=0; i<mData.size(); i++) {
			mHeatCircleCoords.add(getDrawingCoords(mData.get(i)));
		}
	}

	// this will produce a map with all the heatcircle coordinates as keys and their counts as values
	private void convertToCountBasedMap() {
		mCountMap = new HashMap<String, Integer>();
		int count;
		for (int i=0; i<mHeatCircleCoords.size(); i++) {
			count = 0;
			PointF point = mHeatCircleCoords.get(i);
			String pointString = convertPointToString(point);
			if (mCountMap.containsKey(pointString)) {
				count = mCountMap.get(pointString);
			}
			count++;
			mCountMap.put(pointString, count);
		}
	}

	private String convertPointToString(PointF p) {
		return "(" + p.x + "," + p.y + ")";
	}

	// NOTE: no error checking for this!
	private PointF convertStringToPoint(String s) {
		int delimiter = s.indexOf(',');
		float x = Float.parseFloat(s.substring(1,delimiter));
		float y = Float.parseFloat(s.substring(delimiter+1,s.length()-2));
		return new PointF(x,y);
	}


	private PointF getDrawingCoords(PointF p) {
		float x = (p.x + X_BINS/2) * mXChunkSize;
		float y = Math.abs((p.y - Y_BINS/2)) * mYChunkSize;
		return new PointF(x,y);
	}

	public void drawHeatCircles() {
		List<Integer> counts = new ArrayList<Integer>(mCountMap.values());
		int maxCount = 0;
		if (counts.size() > 0) {
			maxCount = CollectionUtils.findMaxInList(counts);
		}
		float radius;
		int alpha, color;
		float[] hsv;
		mHeatCirclePaints = new ArrayList<Paint>(mCountMap.keySet().size());
		mHeatCirclePaths = new ArrayList<Path>();
		Paint paint;
		Path path;
		for (int count: mCountMap.values()) {
			radius = HEATCIRCLE_RADIUS;
			alpha = MIN_ALPHA;
			for (int i=0; i<count; i++) {
				radius /= RADIUS_STEP_SIZE;
			}
			hsv = transitionHSV(count, maxCount);
			color = Color.HSVToColor(hsv);
			alpha += (count-1)*ALPHA_STEP_SIZE;

			// set paint for this layer
			paint = new Paint(Paint.DITHER_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(color);
			paint.setARGB(alpha, Color.red(color), Color.green(color), Color.blue(color));
			paint.setMaskFilter(new BlurMaskFilter(10, Blur.NORMAL));
			mHeatCirclePaints.add(paint);

			// draw all the circles
			path = new Path();
			for (String s: mCountMap.keySet()) {
				if (mCountMap.get(s) >= count) {
					PointF point = convertStringToPoint(s);
					path.addCircle(point.x, point.y, radius, Path.Direction.CCW);
				}
			}
			mHeatCirclePaths.add(path);
		}

		invalidate();
	}

	private float transition(int step, int maxStep, float startF, float endF) {
		return startF + (endF - startF)*step/maxStep;
	}

	private float[] transitionHSV(int step, int maxStep) {
		if (maxStep > 1) {
			float f1 = transition(step, maxStep, mMinHSV[0], mMaxHSV[0]);
			float f2 = transition(step, maxStep, mMinHSV[1], mMaxHSV[1]);
			float f3 = transition(step, maxStep, mMinHSV[2], mMaxHSV[2]);
			return new float[]{f1, f2, f3};
		}
		else {
			float[] hsv = new float[3];
			Color.colorToHSV(HEATCIRCLE_MIN_COLOR, hsv);
			return hsv;
		}
	}

	public void setOnHeatMapTouchListener(OnHeatMapTouchListener l) {
		mListener = l;
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e){
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			// convert drawing coord to 
			List<Integer> closestIndices = getNearestDataIndex(new PointF(e.getX(), e.getY()));
			if (mListener != null)
				mListener.OnHeatMapTouched(this, closestIndices);
		}
		return true;
	}

	protected List<Integer> getNearestDataIndex(PointF drawingCoord) {
		List<Integer> closestIndices = new ArrayList<Integer>();
		for (int i=0; i<mHeatCircleCoords.size(); i++) {
			float distance = PointUtils.getDistance(mHeatCircleCoords.get(i), drawingCoord);
			if (distance <= NEAREST_DISTANCE_THRESHOLD) {
				closestIndices.add(i);
			}
		}
		return closestIndices;
	}

	protected void drawHighlightCircle(PointF point) {
		mHeatCircleHighlight = new Path();
		mHeatCircleHighlight.addCircle(point.x, point.y, HEATCIRCLE_RADIUS+10, Path.Direction.CCW);
	}

	public void setHighlightCircle(int dataIndex) {
		if (dataIndex >= 0) {
			drawHighlightCircle(mHeatCircleCoords.get(dataIndex));

			invalidate();
		}
	}
	
}
