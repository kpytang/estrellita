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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.PointF;
import android.graphics.RectF;

public class PointUtils {

	public static int findIndexOfNearestPoint(List<PointF> points, PointF p) {
		int nearestIndex = -1;
		float minDistance = Float.MAX_VALUE;
		for (int i=0; i<points.size(); i++) {
			float distance = getDistance(points.get(i),p);
			if (distance < minDistance) {
				nearestIndex = i;
				minDistance = distance;
			}
		}
		return nearestIndex;
	}
	
	public static float getDistance(PointF p1, PointF p2) {
		float diffX = p1.x - p2.x;
		diffX *= diffX;
		float diffY = p1.y - p2.y;
		diffY *= diffY;
		return (float) Math.sqrt(diffX + diffY);
	}
	
	public static String getPointAsString(PointF p) {
		return "(" + p.x + "," + p.y + ")";
	}
	
	public static RectF getBoundingBox(List<PointF> points, float padding) {
		List<Float> xValues = new ArrayList<Float>(points.size());
		List<Float> yValues = new ArrayList<Float>(points.size());
		for(int i=0; i<points.size(); i++) {
			xValues.add(points.get(i).x);
			yValues.add(points.get(i).y);
		}
		Collections.sort(xValues);
		Collections.sort(yValues);
		RectF rect = new RectF(xValues.get(0), yValues.get(0), xValues.get(points.size()-1), yValues.get(points.size()-1));
		rect.left -= padding;
		rect.right += padding;
		rect.top -= padding;
		rect.bottom += padding;
		return rect;
	}
	
	public static PointF getPointAlignedWith(PointF referencePoint, PointF aPoint, float targetDistance) {	
		PointF normalizedPoint = normalize(referencePoint, aPoint);
		float distance = getDistance(new PointF(0,0), normalizedPoint);
		float ratio = targetDistance/distance;
		
		float newX = normalizedPoint.x * ratio;
		float newY = normalizedPoint.y * ratio;
		// if the new point is to the right of the reference point 
		if (aPoint.x > referencePoint.x){
			newX += referencePoint.x;
		}
		else {
			newX = referencePoint.x - newX;
		}
		// if the new point is below (which means higher y value) the reference point 
		if (aPoint.y > referencePoint.y) {
			newY += referencePoint.y;
		}
		else {
			newY = referencePoint.y - newY;
		}
		
		return new PointF(newX, newY);
	}
	
	public static PointF normalize(PointF referencePoint, PointF aPoint) {
		PointF p = new PointF();
		p.x = Math.abs(referencePoint.x - aPoint.x);
		p.y = Math.abs(referencePoint.y - aPoint.y);
		return p;
	}
	
	public static Float getSlopeAsAngle(PointF referencePoint, PointF p) {
		PointF normalizedPoint = normalize(referencePoint, p);
		return (float) Math.toDegrees(Math.atan(normalizedPoint.y/normalizedPoint.x));
	}
	
	public static PointF getPointAlignedWith(PointF referencePoint, float angle, float targetDistance) {
		float x = (float) (Math.cos(Math.toRadians(angle)) * targetDistance);
		float y = (float) (Math.tan(Math.toRadians(angle)) * targetDistance);
		PointF p = new PointF(x, y);
		p.x += referencePoint.x;
		p.y -= referencePoint.y;
		return p;
	}
}
