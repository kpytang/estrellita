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

package edu.uci.ics.star.estrellita.activity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.CustomViews;
import edu.uci.ics.star.estrellita.customview.TextboxDialog;
import edu.uci.ics.star.estrellita.customview.TextboxDialog.OnDialogResult;
import edu.uci.ics.star.estrellita.object.indicator.BabyMoodSurvey;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.PointUtils;

public class BabyMoodForm extends TileActivity<BabyMoodSurvey> {
	private static final int NOTES_HELP_DIALOG_ID = 5;
	private static final int NOTES_DIALOG_ID = 6;
	private static final int TIME_DIALOG_ID = 7;
	private static final int MAX_FUSSYNESS = 10;

	private TextView mNotes;
	private ImageView mNotesHelp;
	private Gauge mGauge;
	
	private TextView mDateTextView;
	private Calendar mDisplayDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.babymood_form);

		// initialize header & footer
		setActivityHeader("fussy-o-meter", true, Tile.BABYMOODS);
		setButtonFooter("update mood", mSaveClickListener, null, null);

		// initialize the date
		LinearLayout dateButton = (LinearLayout) this.findViewById(R.id.date_header);
		dateButton.setClickable(true);
		dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}       
		});
		mDateTextView = (TextView) this.findViewById(R.id.date_header_text);
		mDisplayDate = Calendar.getInstance();
		updateDateTime(mDisplayDate.getTime());
		
		// initialize fussy-o-meter
		LinearLayout chartLayout = (LinearLayout)this.findViewById(R.id.gauge);
		mGauge = new Gauge(this);
		chartLayout.addView(mGauge);

		mNotesHelp = (ImageView) this.findViewById(R.id.notes_help);
		mNotesHelp.setOnClickListener(mNotesHelpClickListener);

		mNotes = (TextView) this.findViewById(R.id.notes);
		mNotes.setInputType(InputType.TYPE_NULL);
		mNotes.setOnClickListener(mNotesClickListener);

		restoreIndicator();
	}
	
	private View.OnClickListener mNotesClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(NOTES_DIALOG_ID);
		}
	};

	private class Gauge extends View {
		private static final float MARGIN_LEFT = 10;
		private static final float GAUGE_TOP = 25;
		private static final float GAUGE_RIGHT = 440;
		private static final float GAUGE_BOTTOM = 450;

		private static final float NEEDLE_WIDTH = 10;
		private static final float NEEDLE_LENGTH = 180;

		private static final int GAUGE_RIGHT_COLOR = Color.BLUE;
		private static final int GAUGE_LEFT_COLOR = Color.RED;

		// so we can avoid over-drawing when we don't really need to 
		private static final float TOUCH_TOLERANCE = 4;
		private static final float TOUCH_Y_BUFFER = 15;
		private float mLastX, mLastY;

		private Path mGaugeNeedle;
		private RectF mGaugeRect;  
		private PointF mGaugeMidPoint;
		private Float mGaugeValue = 90f;
		private float mGaugeAngle;

		public Gauge(Context context) {
			super(context);

			// Construct a wedge-shaped path for the gauge needle
			mGaugeMidPoint = new PointF();
			mGaugeMidPoint.x = (GAUGE_RIGHT-MARGIN_LEFT)/2;
			mGaugeMidPoint.x += MARGIN_LEFT;
			mGaugeMidPoint.y = GAUGE_BOTTOM/2;
			mGaugeNeedle = new Path();
			drawGaugeNeedle(mGaugeMidPoint.x, GAUGE_BOTTOM/2-NEEDLE_LENGTH);

			mGaugeRect = new RectF(MARGIN_LEFT, GAUGE_TOP, GAUGE_RIGHT, GAUGE_BOTTOM);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.BLACK);

			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
			p.setColor(Color.WHITE);
			LinearGradient gradient = new LinearGradient(MARGIN_LEFT, GAUGE_BOTTOM, GAUGE_RIGHT, GAUGE_BOTTOM, 
					GAUGE_RIGHT_COLOR, GAUGE_LEFT_COLOR, Shader.TileMode.CLAMP);
			p.setShader(gradient);
			canvas.drawArc(mGaugeRect, 0, -180, true, p);

			p = new Paint(Paint.ANTI_ALIAS_FLAG);
			p.setColor(Color.WHITE);
			p.setStyle(Paint.Style.FILL);

			canvas.drawCircle(mGaugeMidPoint.x, mGaugeMidPoint.y, NEEDLE_WIDTH*3, p);

			int w = (int) (GAUGE_RIGHT+NEEDLE_WIDTH);
			int h = (int) GAUGE_BOTTOM-20;
			int cx = w / 2;
			int cy = h / 2;
			canvas.save();
			canvas.rotate(mGaugeAngle, cx, cy);
			canvas.drawPath(mGaugeNeedle, p);
			canvas.restore();

			p = new Paint(Paint.ANTI_ALIAS_FLAG);
			p.setColor(Color.GRAY);
			p.setStyle(Paint.Style.FILL);
			p.setTextAlign(Align.CENTER);
			p.setTextSize(100);
			p.setTypeface(Typeface.DEFAULT_BOLD);
			canvas.drawText(Integer.toString(getFussyValue()), mGaugeMidPoint.x, mGaugeMidPoint.y-75, p);
		}

		private void drawGaugeNeedle(float tipX, float tipY) {
			mGaugeNeedle.reset();
			mGaugeNeedle.moveTo(mGaugeMidPoint.x-NEEDLE_WIDTH, mGaugeMidPoint.y-20);
			mGaugeNeedle.lineTo(tipX, tipY);
			mGaugeNeedle.lineTo(mGaugeMidPoint.x+NEEDLE_WIDTH, mGaugeMidPoint.y-20);
			mGaugeNeedle.close();
		}

		private void updateGaugeNeedle(float x, float y) {
			// if you're touching outside the bottom half of the gauge, then we'll ignore it
			if (y < (mGaugeMidPoint.y-TOUCH_Y_BUFFER)) {
				float dx = Math.abs(x - mLastX);
				float dy = Math.abs(y - mLastY);
				if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
					PointF newPosition = PointUtils.getPointAlignedWith(mGaugeMidPoint, new PointF(x,y), NEEDLE_LENGTH);

					// calculate the gauge's angle of its needle from its baseline
					float angle = PointUtils.getSlopeAsAngle(mGaugeMidPoint, newPosition);
					// the needle is past the halfway mark
					if (newPosition.x >= mGaugeMidPoint.x) {
						mGaugeValue = 180-angle;
						mGaugeAngle = 90-angle;
					}
					else {
						mGaugeValue = angle;
						mGaugeAngle = -1*(90-angle);
					}
					mLastX = x;
					mLastY = y;
				}
			}
			// you're touching beyond the gauge, so we'll set the angle & value to be the extreme
			else {
				if (x >= mGaugeMidPoint.x) {
					mGaugeValue = 180f;
					mGaugeAngle = 88;
				}
				else {
					mGaugeValue = 0f;
					mGaugeAngle = -88;
				}
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				getParent().requestDisallowInterceptTouchEvent(true);
				updateGaugeNeedle(event.getX(), event.getY());
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				getParent().requestDisallowInterceptTouchEvent(false);
				updateGaugeNeedle(event.getX(), event.getY());
				invalidate();
				break;
			}
			return true;
		}

		public int getFussyValue() {
			float ratio = mGaugeValue/180f;
			return (int) Math.floor(ratio*MAX_FUSSYNESS);
		}

		public void setFussyValue(int fussyValue) {
			float ratio = (float) fussyValue/ (float) MAX_FUSSYNESS;
			mGaugeValue = ratio * 180f;
			if (mGaugeValue > 90) {
				mGaugeAngle = 180 - mGaugeValue;
			}
			else if (mGaugeValue < 90) {
				mGaugeAngle = -mGaugeValue;
			}
			else {
				mGaugeAngle = 0;
			}

			invalidate();
		}
	}

	private View.OnClickListener mNotesHelpClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			showDialog(NOTES_HELP_DIALOG_ID);
		}
	};

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		dialog.setCanceledOnTouchOutside(true);
		switch (id) {
		case NOTES_DIALOG_ID:
			((TextboxDialog) dialog).setText(mNotes.getText().toString().trim());
			break;
		case TIME_DIALOG_ID:
			((TimePickerDialog) dialog).updateTime(mDisplayDate.get(Calendar.HOUR_OF_DAY), mDisplayDate.get(Calendar.MINUTE));
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NOTES_HELP_DIALOG_ID:
			return CustomViews.NotesTipDialog(this, getString(R.string.babymood_notes_hint));
		case NOTES_DIALOG_ID:
			return new TextboxDialog.Builder(this)
				.setTitle("Add a note for this mood")
				.setText(mNotes.getText().toString().trim())
				.setPositiveButton(new OnDialogResult() {
					@Override
					public void finish(String text) {
						if (text.length() > 0) {
							mNotes.setText(text);
						}
					}
				})
				.setNegativeButton(new OnDialogResult() {
					@Override
					public void finish(String text) {
					}
				})
				.create();
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, mDisplayDate.get(Calendar.HOUR_OF_DAY), mDisplayDate.get(Calendar.MINUTE), false);
		}
		return null;
	}
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mDisplayDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
			mDisplayDate.set(Calendar.MINUTE, minute);
			updateDateTime(mDisplayDate.getTime());
		}
	};
	
	private void updateDateTime(Date d) {
		mDateTextView.setText("for "+ DateUtils.getDateAsString(d, DateUtils.DATE_HEADER_FORMAT) + "\n" 
				+ "at " + DateUtils.getDateAsString(d, DateUtils.AMPM_TIME_FORMAT));
	}

	// grabs all the reported info and sends it back to the overview activity
	public View.OnClickListener mSaveClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			saveIndicator();
			Intent intent = new Intent();
			intent.putExtra("babymood", mIndicator);
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileActivity#saveIndicator()
	 */
	@Override
	protected void saveIndicator() {
		mIndicator = new BabyMoodSurvey();
		// we must put this earlier because we will manually change the timestamp of the indicator 
		// (which is set in the super.saveIndicator())
		super.saveIndicator();
		mIndicator.addFussyReport(mGauge.getFussyValue());
		mIndicator.getCommonData().setNotes(mNotes.getText().toString().trim());
		mIndicator.getCommonData().setTimestamp(new Timestamp(mDisplayDate.getTimeInMillis()));
	}

	private void restoreIndicator() {
		mIndicator = null; 
		if (getLastNonConfigurationInstance() != null) { 
			mIndicator = (BabyMoodSurvey) getLastNonConfigurationInstance();
			if (mIndicator.getFussyness() != -1) {
				mGauge.setFussyValue(mIndicator.getFussyness());
			}
			mDisplayDate.setTime(mIndicator.getDateTime());
			updateDateTime(mDisplayDate.getTime());
		}
	}
}
