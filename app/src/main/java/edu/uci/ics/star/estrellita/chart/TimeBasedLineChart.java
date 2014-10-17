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

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;

public class TimeBasedLineChart extends BaseChart {
	private Picture mXAxis, mYAxis, mData, mLegend = null, mHighlight = null;

	private Float mChartMaxY;
	
	public TimeBasedLineChart(Context context, List<List<Float>> data, List<Integer> colors, TimeScale scale) {       	
		this(context, data, colors, scale, null);
	}

	public TimeBasedLineChart(Context context, List<List<Float>> data, List<Integer> colors, TimeScale scale, Float f) {       	
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);

		mChartMaxY = f;
		
		// set the colors
		updateChartData(data, colors, scale);
	}

	public void setXLabels(List<String> labels) {
		mXAxis = new Picture();
		drawXAxis(mXAxis.beginRecording(500, 480), labels);
		mXAxis.endRecording(); 
	}
	
	public void setXLabels(List<String> firstRowLabels, List<String> secondRowLabels) {
		mXAxis = new Picture();
		drawXAxis(mXAxis.beginRecording(500, 480), firstRowLabels, secondRowLabels);
		mXAxis.endRecording(); 
	}

	@Override 
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		canvas.drawPicture(mXAxis);
		canvas.drawPicture(mYAxis);
		if (mHighlight != null) {
			canvas.drawPicture(mHighlight);
		}
		if (mLegend != null) {
			canvas.drawPicture(mLegend);
		}
		canvas.drawPicture(mData);
	}

	public void setHighlightBox(int index) {
		mHighlight = new Picture();
		drawBoxAroundData(mHighlight.beginRecording(500, 480), index);
		mHighlight.endRecording();

		invalidate();
	}

	public void addLegend(List<String> labels, List<Integer> colors) {
		mLegend = new Picture();
		drawLegend(mLegend.beginRecording(500,480), labels, colors);
		mLegend.endRecording();

		invalidate();
	}

	public void updateChartData(List<List<Float>> data, List<Integer> colors, TimeScale scale) {
		if (mChartMaxY != null) {
			setGlobalMaxY(mChartMaxY);
		}
		else {
			setGlobalMaxY(data);
		}

		mData = new Picture();
		for (int i=0; i<data.size(); i++) {
			addYDataSeries(data.get(i), colors.get(i));
		}
		drawSeriesAsLineGraph(mData.beginRecording(500, 480));
		mData.endRecording();

		mXAxis = new Picture();
		drawXTimeAxis(mXAxis.beginRecording(500, 480), scale);
		mXAxis.endRecording();   

		mYAxis = new Picture();
		drawYAxis(mYAxis.beginRecording(500, 480));
		mYAxis.endRecording();

		invalidate();
	}

}
