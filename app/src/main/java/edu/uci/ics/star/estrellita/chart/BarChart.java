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

public class BarChart extends BaseChart {
	private Picture mXAxis, mYAxis, mData, mHighlight = null;
	
    public BarChart(Context context, List<String> categories, List<Float> values) {       	
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        mXAxis = new Picture();
        drawXCategoricalAxis(mXAxis.beginRecording(500, 480), categories);
        mXAxis.endRecording();   
        
        updateChart(values);
    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawPicture(mXAxis);
        canvas.drawPicture(mYAxis);
        if (mHighlight != null) {
        	canvas.drawPicture(mHighlight);
        }
        canvas.drawPicture(mData);
    }
    
    public void setHighlightBox(int index) {
    	mHighlight = new Picture();
        drawBoxAroundData(mHighlight.beginRecording(500, 480), index);
        mHighlight.endRecording();
        
        invalidate();
    }
    
    public void updateChart(List<Float> values) {
    	setMaxY(values);
    	
    	mData = new Picture();
        addYDataSeries(values, Color.RED, ChartType.BAR);
        drawSeriesAsBarGraph(mData.beginRecording(500, 480));
        mData.endRecording();
        
        mYAxis = new Picture();
        drawYAxis(mYAxis.beginRecording(500, 480));
        mYAxis.endRecording();
        
        invalidate();
    }
}
