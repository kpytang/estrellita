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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import edu.uci.ics.star.estrellita.utils.CollectionUtils;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.PointUtils;

public abstract class BaseChart extends View {
	public interface OnChartTouchListener {
		public void OnChartTouched(View v, int closestIndex);
	};

	public enum TimeScale {DAY, WEEK, MONTH, ALL}
	public enum ChartType {LINE, BAR, HEATMAP, TABLE}
	public enum MarkerType {
		CIRCLE(0), 
		SQUARE(1),
		TRIANGLE(2),
		DIAMOND(3);

		private int value;
		private static final Map<Integer, MarkerType> markersByValue = new HashMap<Integer, MarkerType>();
		static {
			for (MarkerType marker : MarkerType.values()) {
				markersByValue.put(marker.value, marker);
			}
		}

		public static MarkerType forValue(int value) {
			return markersByValue.get(value);
		}

		private MarkerType(int value) {
			this.value = value;
		}
	}

	public static final String[] MONTH_TIME_SCALES = { "this month", "last month", "all dates" };
	public static final String[] WEEK_TIME_SCALES = { "this week", "this month", "last week", "last month", "all dates" };
	public static final String[] DAY_TIME_SCALES = { "today", "this week", "this month", "yesterday", "last week", "last month", "all dates" };

	private static final float CHART_HEIGHT = 380;
	private static final float CHART_WIDTH = 480;
	protected static float MARGIN_LEFT = 50;
	protected static float MARGIN_RIGHT = 10;
	private static final float XLABELS_OFFSET = 45;
	private static final float XLABELS_OFFSET_2 = 70;
	private static final float XLABELS_OFFSET_3 = 95;
	//	private static final float YLABELS_OFFSET = 20;
	private static final float TICK_SIZE = 10;
	private static final float TICK_SIZE_2 = 20;
	private static final float TICK_SIZE_3 = 30;
	private static final float DATA_POINT_RADIUS = 7;
	private static final float DATA_POINT_BAR_PADDING = 10;

	public static final float BOX_PADDING = 20;

	// the default implementation requires each view to have an id. Adding this means we don't get a
	// warning like this, "couldn't save which view has focus because the focused view X has no id".
	private static final int CHART_VIEW_ID = 123456789;

	private float mMaxY = 0;
	private float mMaxDataY = 0;
	private int mYTickInterval = 1;
	private List<List<PointF>> mDataSeries;
	private List<List<PointF>> mDataCoords;
	private List<Integer> mDataSeriesColors;

	private OnChartTouchListener mListener;

	private Path mTriangle, mDiamond;

	public BaseChart(Context context) {
		super(context);
		mDataSeries = new ArrayList<List<PointF>>();
		mDataCoords = new ArrayList<List<PointF>>();
		mDataSeriesColors = new ArrayList<Integer>();

		setId(CHART_VIEW_ID);

		initializeMarkerPaths();
	}

	private void initializeMarkerPaths() {
		mTriangle = new Path();
		mTriangle.moveTo(0, -DATA_POINT_RADIUS - DATA_POINT_RADIUS / 2);
		mTriangle.lineTo(-DATA_POINT_RADIUS, DATA_POINT_RADIUS);
		mTriangle.lineTo(DATA_POINT_RADIUS, DATA_POINT_RADIUS);
		mTriangle.close();

		mDiamond = new Path();
		mDiamond.moveTo(0, -DATA_POINT_RADIUS);
		mDiamond.lineTo(-DATA_POINT_RADIUS, 0);
		mDiamond.lineTo(0, DATA_POINT_RADIUS);
		mDiamond.lineTo(DATA_POINT_RADIUS, 0);
		mDiamond.close();
	}

	protected void addYDataSeries(List<Float> data, int color, ChartType chartType) {
		try {
			// convert to chartable data
			List<PointF> aSeries = new ArrayList<PointF>();
			for (int i=0; i<data.size(); i++) {
				if (data.get(i) != null) {
					aSeries.add(new PointF(i+1, data.get(i)));
				}
			}
			mDataSeries.add(aSeries);
			mDataSeriesColors.add(color);

			// figure out what the drawable coordinates of the series are
			float yChunkSize = (CHART_HEIGHT)/(mMaxDataY);
			float xChunkSize;
			if (chartType.equals(ChartType.BAR)) {
				xChunkSize = (CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT-(data.size()*DATA_POINT_BAR_PADDING))/(data.size()+1);
			}
			else {
				xChunkSize = (CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT)/(data.size()+1);
			}
			List<PointF> coords = new ArrayList<PointF>();
			for (int i=0; i<aSeries.size(); i++) {
				PointF p = aSeries.get(i);
				coords.add(new PointF(p.x*xChunkSize+MARGIN_LEFT, CHART_HEIGHT-(p.y*yChunkSize)));
			}
			mDataCoords.add(coords);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addYDataSeries(List<Float> data, int color) {
		addYDataSeries(data, color, ChartType.LINE);
	}
	
	// get maximum Y value across all the data series
	protected void setGlobalMaxY(Float f) {
		mMaxY = Math.round(f);
		if (mMaxY > 20) {
			double ceiling = Math.ceil(mMaxY/20);
			setYTickInterval(Double.valueOf(ceiling).intValue());
			mMaxY = mYTickInterval*20;
		}
		mMaxDataY = mMaxY + mYTickInterval;
	}

	// get maximum Y value across all the data series
	protected void setGlobalMaxY(List<List<Float>> series) {
		mMaxY = 0;
		if ((series==null) || (series.size() == 0)) {
			return;
		}
		float thisMax = 0;
		for (int i=0; i<series.size(); i++) {
			if (series.get(i).size() > 0) {
				thisMax = CollectionUtils.findMaxInList(series.get(i));
				thisMax = Math.round(thisMax);
				if (thisMax > mMaxY) {
					mMaxY = thisMax;
				}
			}
		}

		if (mMaxY > 20) {
			double ceiling = Math.ceil(mMaxY/20);
			setYTickInterval(Double.valueOf(ceiling).intValue());
			mMaxY = mYTickInterval*20;
		}
		mMaxDataY = mMaxY + mYTickInterval;
	}

	protected void setMaxY(List<Float> data) {
		mMaxY = 0;
		if ((data==null) || (data.size() == 0)) {
			return;
		}
		float thisMax = CollectionUtils.findMaxInList(data);
		thisMax = Math.round(thisMax);
		
		if (thisMax > mMaxY) {
			mMaxY = thisMax;
		}
		
		if (mMaxY > 20) {
			double ceiling = Math.ceil(mMaxY/20);
			setYTickInterval(Double.valueOf(ceiling).intValue());
			mMaxY = mYTickInterval*20;
		}
		mMaxDataY = mMaxY + mYTickInterval;
	}

	protected void drawBoxAroundData(Canvas canvas, int index) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(Color.YELLOW);

		// figure out how big the box needs to be to highlight all the data points at this index
		List<PointF> values = new ArrayList<PointF>(mDataSeries.size());
		for(int i=0; i<mDataCoords.size(); i++) {
			values.add(mDataCoords.get(i).get(index));
		}
		// draw yellow circle
		canvas.drawRect(PointUtils.getBoundingBox(values, BOX_PADDING), p);
		// draw black outline
		p.setStrokeWidth(2);
		p.setColor(Color.BLACK);
		p.setStyle(Paint.Style.STROKE);
		canvas.drawRect(PointUtils.getBoundingBox(values, BOX_PADDING), p);
	}

	/**
	 * @param canvas
	 */
	protected void drawSeriesAsBarGraph(Canvas canvas) {
		List<PointF> series;
		PointF point;
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw the data points as rectangles (bars), future data have no bars
		for (int i=0; i<mDataCoords.size(); i++) {
			series = mDataCoords.get(i);	
			for (int j=0; j<series.size(); j++) {
				point = series.get(j);
				p = new Paint(Paint.ANTI_ALIAS_FLAG);
				p.setStyle(Paint.Style.FILL);
				p.setColor(mDataSeriesColors.get(i));
				canvas.drawRect(getBar(point), p);
			}
		}
	}

	protected void drawSeriesAsLineGraph(Canvas canvas) {

		drawSeriesAsScatterPlot(canvas);

		List<PointF> series;
		PointF point;
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		// draw the lines that connect the data points
		for (int i=0; i<mDataCoords.size(); i++) {
			series = mDataCoords.get(i);	
			p.setColor(mDataSeriesColors.get(i));
			PointF lastPoint = series.get(0);
			if (lastPoint.y > CHART_HEIGHT) {
				lastPoint = null;
			}
			for (int j=1; j<series.size(); j++) {
				point = series.get(j);
				if (point.y <= CHART_HEIGHT) {
					// if this point is within the chart bounds & there is a last point, then draw the line
					if (lastPoint != null) {
						canvas.drawLine(lastPoint.x, lastPoint.y, point.x, point.y, p);
					}
					// else if there isn't a last point, so just store this point
					lastPoint = point;
				}
				else {
					lastPoint = null;
				}
			}
		}
	}

	protected void drawSeriesAsScatterPlot(Canvas canvas) {
		List<PointF> series;
		PointF point;
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw the data points as small circles
		// solid circles are recorded data, empty circles are placeholders for future data
		for (int i=0; i<mDataCoords.size(); i++) {
			series = mDataCoords.get(i);	
			for (int j=0; j<series.size(); j++) {
				point = series.get(j);
				// these are for future values (which end up having coordinates that are outside the chart's bounds)
				if (point.y > CHART_HEIGHT) {
					p = new Paint(Paint.ANTI_ALIAS_FLAG);
					p.setStrokeWidth(2);
					p.setColor(Color.BLACK);
					p.setStyle(Paint.Style.STROKE);
					canvas.drawCircle(point.x, CHART_HEIGHT, DATA_POINT_RADIUS, p);
				}
				else {
					p = new Paint(Paint.ANTI_ALIAS_FLAG);
					p.setStyle(Paint.Style.FILL);
					p.setColor(mDataSeriesColors.get(i));
					MarkerType marker = MarkerType.forValue(i%3);
					switch(marker) {
					case SQUARE:
						// left, top, right, bottom
						canvas.drawRect(point.x-DATA_POINT_RADIUS, point.y-DATA_POINT_RADIUS, point.x+DATA_POINT_RADIUS, point.y+DATA_POINT_RADIUS, p);
						break;
					case DIAMOND:
						canvas.save();
						canvas.translate(point.x, point.y);
						canvas.drawPath(mDiamond, p);
						canvas.restore();
						break;
					case TRIANGLE:
						canvas.save();
						canvas.translate(point.x, point.y);
						canvas.drawPath(mTriangle, p);
						canvas.restore();
						break;
					case CIRCLE:
					default:
						canvas.drawCircle(point.x, point.y, DATA_POINT_RADIUS, p);
						break;
					}

				}
			}
		}
	}

	private String getHourLabel(int i) {
		if (i==1) {
			return "12am";
		}
		else if (i>13) {
			return Integer.toString(i-12-1) + "pm";
		}
		else {
			return Integer.toString(i-1) + "am";
		}
	}

	protected void drawXCategoricalAxis(Canvas canvas, List<String> categories) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw main axis
		p.setColor(Color.BLACK);
		p.setStrokeWidth(5);
		canvas.drawLine(0, CHART_HEIGHT, CHART_WIDTH, CHART_HEIGHT, p);

		// draw ticks
		int numberOfChunks = categories.size(); 
		float totalWidth = CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT-numberOfChunks*DATA_POINT_BAR_PADDING;
		float chunkSize = totalWidth/(numberOfChunks+1);
		p.setColor(Color.BLACK);
		p.setStrokeWidth(2);
		for(int i=1; i<=numberOfChunks; i++) {
			// staggered labels - every 2nd one is medium length, every 3rd is a long length
			float tickStopLength = TICK_SIZE;
			if ((i%2)==0) {
				tickStopLength = TICK_SIZE_2;
			}
			else if ((i%3)==0) {
				tickStopLength = TICK_SIZE_3;
			}
			canvas.drawLine(MARGIN_LEFT+i*chunkSize, CHART_HEIGHT-TICK_SIZE, MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+tickStopLength, p);
		}

		// draw axis labels
		p.setColor(Color.BLACK);
		p.setTextSize(15);
		p.setTextAlign(Paint.Align.CENTER);
		for(int i=1; i<=numberOfChunks; i++) {
			float offset = XLABELS_OFFSET;
			if ((i%2)==0) {
				offset = XLABELS_OFFSET_2;
			}
			else if ((i%3)==0) {
				offset = XLABELS_OFFSET_3;
			}
			canvas.drawText(categories.get(i-1), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+offset*(float)0.5, p);
		}
	}

	protected void drawXAxis(Canvas canvas, List<String> xLabels) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw main axis
		p.setColor(Color.BLACK);
		p.setStrokeWidth(5);
		canvas.drawLine(0, CHART_HEIGHT, CHART_WIDTH, CHART_HEIGHT, p);

		// draw ticks
		// ASSUMPTION: number of ticks to draw will be the same as the number of points in any of the data series
		int numberOfChunks = mDataSeries.get(0).size(); 
		float chunkSize = (CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT)/(numberOfChunks+1);
		p.setColor(Color.BLACK);
		p.setStrokeWidth(2);
		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawLine(MARGIN_LEFT+i*chunkSize, CHART_HEIGHT-TICK_SIZE, MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+TICK_SIZE, p);
		}

		// draw axis labels
		p.setColor(Color.BLACK);
		p.setTextSize(15);
		p.setTextAlign(Paint.Align.CENTER);

		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawText(xLabels.get(i-1), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET*(float)0.5, p);
		}
	}
	
	protected void drawXAxis(Canvas canvas, List<String> xFirstRowLabels, List<String> xSecondRowLabels) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw main axis
		p.setColor(Color.BLACK);
		p.setStrokeWidth(5);
		canvas.drawLine(0, CHART_HEIGHT, CHART_WIDTH, CHART_HEIGHT, p);

		// draw ticks
		// ASSUMPTION: number of ticks to draw will be the same as the number of points in any of the data series
		int numberOfChunks = mDataSeries.get(0).size(); 
		float chunkSize = (CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT)/(numberOfChunks+1);
		p.setColor(Color.BLACK);
		p.setStrokeWidth(2);
		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawLine(MARGIN_LEFT+i*chunkSize, CHART_HEIGHT-TICK_SIZE, MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+TICK_SIZE, p);
		}

		// draw axis labels
		p.setColor(Color.BLACK);
		p.setTextSize(15);
		p.setTextAlign(Paint.Align.CENTER);

		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawText(xFirstRowLabels.get(i-1), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET*(float)0.5, p);
			canvas.drawText(xSecondRowLabels.get(i-1), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET_2*(float)0.5, p);
		}
	}

	/**
	 */
	protected void drawXTimeAxis(Canvas canvas, TimeScale scale) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw main axis
		p.setColor(Color.BLACK);
		p.setStrokeWidth(5);
		canvas.drawLine(0, CHART_HEIGHT, CHART_WIDTH, CHART_HEIGHT, p);

		// draw ticks
		// ASSUMPTION: number of ticks to draw will be the same as the number of points in any of the data series
		int numberOfChunks = mDataSeries.get(0).size(); 
		float chunkSize = (CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT)/(numberOfChunks+1);
		p.setColor(Color.BLACK);
		p.setStrokeWidth(2);
		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawLine(MARGIN_LEFT+i*chunkSize, CHART_HEIGHT-TICK_SIZE, MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+TICK_SIZE, p);
		}

		// draw axis labels
		p.setColor(Color.BLACK);
		p.setTextSize(15);
		p.setTextAlign(Paint.Align.CENTER);

		switch(scale) {
		case DAY:
			// don't draw last few labels (they'll get cut off)
			for(int i=1; i<=numberOfChunks-2; i++) {
				if (((i%4)==0) || i==1) {
					canvas.drawText(getHourLabel(i), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET*(float)0.5, p);
				}
			}
			break;
		case WEEK:
			for(int i=1; i<=numberOfChunks; i++) {
				// canvas.drawText(Integer.toString(i), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET*(float)0.5, p);
				canvas.drawText(DateUtils.DAYS_OF_WEEK[i-1], MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET, p);
			}
			break;
		case MONTH:
			for(int i=1; i<=numberOfChunks; i++) {
				canvas.drawText("Week"+Integer.toString(i), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET*(float)0.5, p);
			}
			break;
		case ALL:
			for(int i=1; i<=numberOfChunks; i++) {
				canvas.drawText("M"+Integer.toString(i), MARGIN_LEFT+i*chunkSize, CHART_HEIGHT+XLABELS_OFFSET*(float)0.5, p);
			}
			break;
		}
	}

	protected void drawYAxis(Canvas canvas) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw main axis
		p.setColor(Color.BLACK);
		p.setStrokeWidth(5);
		canvas.drawLine(MARGIN_LEFT, 0, MARGIN_LEFT, CHART_HEIGHT+TICK_SIZE, p);	

		// draw ticks
		float numberOfChunks = Math.round(mMaxY/mYTickInterval);
		float chunkSize = (CHART_HEIGHT)/(numberOfChunks+1);
		p.setColor(Color.BLACK);
		p.setStrokeWidth(2);
		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawLine(MARGIN_LEFT-TICK_SIZE, CHART_HEIGHT-i*chunkSize, MARGIN_LEFT+TICK_SIZE, CHART_HEIGHT-i*chunkSize, p);
		}

		// draw axis labels
		p.setColor(Color.BLACK);
		p.setTextSize(20);
		p.setTextAlign(Paint.Align.RIGHT);
		for(int i=1; i<=numberOfChunks; i++) {
			canvas.drawText(Integer.toString(i*mYTickInterval), MARGIN_LEFT-TICK_SIZE-8, CHART_HEIGHT-i*chunkSize+5, p);
		}
	}

	private RectF getBar(PointF p) {
		RectF rect = new RectF(p.x, p.y, p.x, CHART_HEIGHT);
		rect.left -= DATA_POINT_BAR_PADDING;
		rect.top -= DATA_POINT_BAR_PADDING;
		rect.right += DATA_POINT_BAR_PADDING;
		return rect;
	}

	protected int getNearestIndex(PointF p) {
		float minDistance = Float.MAX_VALUE;
		int nearestIndex = -1;
		for (int i=0; i<mDataCoords.size(); i++) {
			int index = PointUtils.findIndexOfNearestPoint(mDataCoords.get(i), p);
			float distance = PointUtils.getDistance(mDataCoords.get(i).get(index), p);
			if (distance < minDistance) {
				nearestIndex = index;
				minDistance = distance;
			}
		}
		return nearestIndex;
	}

	/*
	 * (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent e){
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			int closestIndex = getNearestIndex(new PointF(e.getX(), e.getY()));
			if (mListener != null)
				mListener.OnChartTouched(this, closestIndex);
		}
		return true;
	}

	public void setOnChartTouchListener(OnChartTouchListener l) {
		mListener = l;
	}

	protected void setYTickInterval(int i) {
		mYTickInterval = i;		
	}

	protected void drawLegend(Canvas canvas, List<String> labels, List<Integer> colors) {
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setTextSize(20);
		p.setTextAlign(Paint.Align.CENTER);
		p.setTypeface(Typeface.SANS_SERIF);
		float xChunkSize = (CHART_WIDTH-MARGIN_LEFT-MARGIN_RIGHT)/(labels.size()+1);
		for (int i=0; i<labels.size(); i++) {
			p.setColor(colors.get(i));
			canvas.drawText(labels.get(i), xChunkSize*(i+1), 20, p);
		}
	}
}
