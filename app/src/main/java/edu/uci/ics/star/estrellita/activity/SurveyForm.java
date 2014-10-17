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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import edu.uci.ics.star.estrellita.EstrellitaTiles;
import edu.uci.ics.star.estrellita.R;
import edu.uci.ics.star.estrellita.Tile;
import edu.uci.ics.star.estrellita.customview.FixedFlipper;
import edu.uci.ics.star.estrellita.customview.TextboxDialog;
import edu.uci.ics.star.estrellita.customview.TextboxDialog.OnDialogResult;
import edu.uci.ics.star.estrellita.db.table.indicator.GenericSurveyTable;
import edu.uci.ics.star.estrellita.object.AnswerChoice;
import edu.uci.ics.star.estrellita.object.AnswerChoice.SpecialCase;
import edu.uci.ics.star.estrellita.object.CommonData;
import edu.uci.ics.star.estrellita.object.Condition;
import edu.uci.ics.star.estrellita.object.Flag;
import edu.uci.ics.star.estrellita.object.Question;
import edu.uci.ics.star.estrellita.object.Question.QuestionType;
import edu.uci.ics.star.estrellita.object.Response;
import edu.uci.ics.star.estrellita.object.indicator.Appointment;
import edu.uci.ics.star.estrellita.object.indicator.Appointment.AttendedState;
import edu.uci.ics.star.estrellita.object.indicator.GenericSurvey;
import edu.uci.ics.star.estrellita.object.indicator.Log;
import edu.uci.ics.star.estrellita.object.indicator.Survey;
import edu.uci.ics.star.estrellita.object.indicator.Survey.SurveyType;
import edu.uci.ics.star.estrellita.updateservice.SyncService;
import edu.uci.ics.star.estrellita.utils.DateUtils;
import edu.uci.ics.star.estrellita.utils.StringUtils;

// TITLE = the title of the survey, we will do lookups in the database this way
// INDICATOR_ID = if the survey is related to another indicator (like an appointment), then we'll pass that in
// we'll use this for the db and set the survey_id to this indicator id so that we know if a survey has already 
// been completed for a particular indicator
// SURVEY_TYPE = the type (name) as a string for the survey, so we know which kind of raw resources to load in
// the resources will point to the text file containing the survey questions
public class SurveyForm extends TileActivity<GenericSurvey> {
	public static final String TITLE = "title";
	public static final String SURVEY_TYPE = "surveyType";
	public static final String INDICATOR_ID = "indicatorId";
	private static final int VALIDATE_ANSWER_DIALOG_ID = 0;
	private static final int FINISHED_SURVEYS_DIALOG_ID = 1;
	private static final int FREEFORMTEXT_DIALOG_ID = 2;
	protected static final int MAKE_PHONE_CALL_DIALOG_ID = 3;
	private static final int EXIT_SURVEY_FORM_DIALOG_ID = 4;
	public static final String CALL_LATER_TAG = "call_later";
	public static final String ALREADY_CALLED_TAG = "already_called";
	public static final String REMINDED_CALL_LATER_TAG = "call_later_reminder_sent";

	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;

	private FixedFlipper flipper;

	private List<GenericSurvey> mSurveys;
	private int mCurrentSurveyIndex;
	private int mCurrentQuestionListIndex;	
	private int mCurrentQuestionIndex;
	private List<Integer> mQuestionIndices;

	private List<Integer> mSelectedAnswerIndices;
	private TextView mFreeFormTextBox;

	private boolean mStartedSurvey;
	private SurveyType mSurveyType;
	private int mIndicatorId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.survey_form);

		String title = getIntent().getStringExtra(TITLE);
		mIndicatorId = getIntent().getIntExtra(INDICATOR_ID, -1);
		String typeString = getIntent().getStringExtra(SURVEY_TYPE);
		mSurveyType = SurveyType.STRESS;
		if (typeString != null) {
			mSurveyType = SurveyType.valueOf(typeString);
		}

		// initialize header & footer
		switch(mSurveyType) {
		case PRE_APPT:
		case POST_APPT:
		case BONDING:
		case BABYMOOD:
			setActivityHeader(title, true, Tile.SURVEYS);
			break;
		default:
			setActivityHeader(title, false, Tile.SURVEYS);
			break;
		}

		if ((mSurveyType == SurveyType.PRE_APPT) || (mSurveyType == SurveyType.POST_APPT)) {
			openDatabase();
			// first make sure that there exists an appointment with that id
			// it could have been modified while the survey reminder was still in the notification tray
			Appointment a = mDatabase.getAppointmentTable().getAppointmentById(mIndicatorId);
			// there's no appointment by that id, so we can exit out of the survey
			if (a == null) {
				Bundle bundle = new Bundle();
				bundle.putString("prompt", "This appointment has been changed or deleted. Please disregard this reminder notification.");
				showDialog(EXIT_SURVEY_FORM_DIALOG_ID, bundle);
			}
			GenericSurveyTable surveyTable = (GenericSurveyTable) mDatabase.getSurveyTableByType(mSurveyType);
			GenericSurvey s = (GenericSurvey) surveyTable.getSurveyForBaby(mBaby.getId(), mIndicatorId);
			// you've taken this survey already, if it's not null
			if (s != null) {
				Bundle bundle = new Bundle();
				bundle.putString("prompt", "You've already taken this survey. Thank you!");
				showDialog(EXIT_SURVEY_FORM_DIALOG_ID, bundle);
			}
			// if this is a pre-appointment survey, but the appointment date is already past, then we'll throw away this survey too
			if (mSurveyType == SurveyType.PRE_APPT) {
				a = mDatabase.getAppointmentTable().getAppointmentById(mIndicatorId);
				if (a == null) {
					Bundle bundle = new Bundle();
					bundle.putString("prompt", "This appointment has been changed or deleted. Please disregard this reminder notification.");
					showDialog(EXIT_SURVEY_FORM_DIALOG_ID, bundle);
				}
				else if (DateUtils.combineDateAndTime(a.getDate(), a.getStartTime()).before(DateUtils.getTimestamp())) {
					Bundle bundle = new Bundle();
					bundle.putString("prompt", "The questions in this survey should be answered before you attend your appointment with Dr. " + a.getDoctorName() 
							+ ". Because this appointment has already passed, you do not have to take this pre-appointment survey.");
					showDialog(EXIT_SURVEY_FORM_DIALOG_ID, bundle);
				}
			}
		}

		// for all other surveys and for pre- and post-appointment surveys that haven't been taken
		int[] surveyResources = Survey.getSurveyResources(mSurveyType);

		readQuestionnaire(surveyResources);

		mQuestionIndices = new ArrayList<Integer>();
		mCurrentSurveyIndex = 0;
		mCurrentQuestionIndex = 0;
		mQuestionIndices.add(mCurrentQuestionIndex);
		mCurrentQuestionListIndex = mQuestionIndices.size()-1;

		mSelectedAnswerIndices = new ArrayList<Integer>();
		mStartedSurvey = false;

		initializeFlipper();

		restoreIndicator();

		if (!mStartedSurvey) {
			showInitialSurveyPrompt();
		}
		else {
			changeFlipperView(mCurrentQuestionIndex, true);
		}
	}

	public AlertDialog exitSurveyFormDialog(String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setMessage(message)
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				setResult(Activity.RESULT_OK);
				startActivity(new Intent(SurveyForm.this, EstrellitaTiles.class));	
			}
		});
		return dialog.create();
	}

	public void readQuestionnaire(int[] surveyList) {
		mSurveys = new ArrayList<GenericSurvey>();
		for (int i = 0; i < surveyList.length; i++) {
			mSurveys.add(readQuestionnaire(surveyList[i]));
		}

		// randomize the order of the surveys
		// by default: the epds one comes first, the stress one comes second
		// but it's possible for us to reverse the order (epds one last, stress one first)
		Random random = new Random();
		if ((random.nextInt()%2)==0) {
			Collections.reverse(mSurveys);
		}
	}

	public GenericSurvey readQuestionnaire(int surveyResourceId) {
		InputStream is = this.getResources().openRawResource(surveyResourceId);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		GenericSurvey survey = new GenericSurvey(Survey.getSurveyType(surveyResourceId));
		survey.getCommonData().setIdUser(EstrellitaTiles.getParentId(this));
		if (mBaby == null) {
			survey.getCommonData().setIdBaby(EstrellitaTiles.getCurrentBabyId());
		}
		else {
			survey.getCommonData().setIdBaby(mBaby.getId());
		}

		String line;
		try {
			Question question = null;
			List<Condition> conditions = new ArrayList<Condition>();
			Condition condition = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.trim().split("\t");
				// tokens 0: is the type of keyword, describing what this line presents
				// tokens 1: is the type of keyword, describing the value of the token
				for (int i=0; i<tokens.length; i++) {
					tokens[i] = tokens[i].trim();
				}

				if (tokens[0].equalsIgnoreCase("*")) {
					question.setScoringReversed(true);
				}
				else if (tokens[0].equalsIgnoreCase("Q")) {
					// add last question to the list
					// and add any conditions attached to the question
					if (question != null) {
						if ((conditions != null) && (conditions.size()>0)) {
							question.addConditions(conditions);
						}
						survey.addQuestion(question);
						conditions = new ArrayList<Condition>();
						condition = null;
					}
					question = new Question(tokens[1]);
				} 
				else if(tokens[0].equalsIgnoreCase("QT")) {
					try {
						question.setQuestionType(QuestionType.valueOf(tokens[1].toUpperCase()));
					} catch (Exception e) {
					}
				}
				else if (tokens[0].equalsIgnoreCase("A")) {
					question.addAnswer(new AnswerChoice(tokens[1]));
				}
				else if (tokens[0].equalsIgnoreCase("P")) {
					String prompt = "";
					// customize the prompt if this is a pre-appointment survey
					// pull in appointment info using the indicator id
					if (survey.getSurveyType() == SurveyType.PRE_APPT) {
						// get appointment object
						openDatabase();
						Appointment a = mDatabase.getAppointmentTable().getAppointmentById(mIndicatorId);
						if (a == null) {
							Bundle bundle = new Bundle();
							bundle.putString("prompt", "This appointment has been changed or deleted. Please disregard this reminder notification.");
							showDialog(EXIT_SURVEY_FORM_DIALOG_ID, bundle);
						}
						prompt = mBaby.getName() + " has an appointment with Dr. " + a.getDoctorName() + " at " + 
						DateUtils.getDateAsString(a.getStartTime(), DateUtils.TILE_HOUR_FORMAT);
						String dateString = DateUtils.getDateAsString(a.getDate(), DateUtils.TILE_DATE_FORMAT);
						if (!dateString.equalsIgnoreCase("today") && !dateString.equalsIgnoreCase("tomorrow")) {
							prompt += " on ";
						}
						else {
							prompt += " ";
						}
						prompt += dateString + ". ";
					}
					if (survey.getSurveyType() == SurveyType.POST_APPT) {
						openDatabase();
						Appointment a = mDatabase.getAppointmentTable().getAppointmentById(mIndicatorId);
						if (a == null) {
							Bundle bundle = new Bundle();
							bundle.putString("prompt", "This appointment has been changed or deleted. Please disregard this reminder notification.");
							showDialog(EXIT_SURVEY_FORM_DIALOG_ID, bundle);
						}
						prompt = "Just checking in about the appointment that " + mBaby.getName() + " had at " + 
						DateUtils.getDateAsString(a.getStartTime(), DateUtils.TILE_HOUR_FORMAT);
						String dateString = DateUtils.getDateAsString(a.getDate(), DateUtils.TILE_DATE_FORMAT);
						if (!dateString.equalsIgnoreCase("today") && !dateString.equalsIgnoreCase("yesterday")) {
							prompt += " on ";
						}
						else {
							prompt += " ";
						}
						prompt += dateString + " with Dr. " + a.getDoctorName() + ". ";
					}
					prompt += tokens[1];
					survey.setPrompt(prompt);
				}
				else if (tokens[0].equalsIgnoreCase("T")) {
					try {
						survey.setSurveyType(SurveyType.valueOf(tokens[2].toUpperCase()));
					} catch (Exception e) {
					}
				}
				else if (tokens[0].equalsIgnoreCase("!")) {
					String[] newList = tokens[1].split(",");
					for (int i = 0; i < newList.length; i++) {
						try {
							question.getAnswer(Integer.parseInt(newList[i])).setSpecialCase(SpecialCase.ALERT);
						} catch (Exception e) {
						}
					}
				}
				// here we specify which question to go to next, depending on a particular response
				// by default, if these don't exist, then we'll just go to the next question in the file
				// however, if these are specified, then we will follow the CR (conditional responses) tags to determine 
				// which CQI (condition question index) to go to 
				else if (tokens[0].equalsIgnoreCase("CQI")) {
					// if we haven't set a condition before, then do that - this happens when we want to go to a specific question regardless of the responses
					if (condition == null) {
						condition = new Condition();
					}
					// and set the question id
					condition.setGoToQuestionId(tokens[1]);
					//					Utilities.println("setting conditional gotoquestionId to be: " + tokens[1]);
					// we add this condition to the list of conditions because our assumptions is that CR is first (optional), CQI is second
					conditions.add(condition);
					condition = new Condition();
				}
				// the CR tags specifies the responses (answer indexes) that determine which question we go to next
				// currently we only support OR condition; that is, if any of the comma-delimited responses are selected then 
				// we will proceed to the specified question
				else if (tokens[0].equalsIgnoreCase("CR")) {
					// if we haven't read a condition before, then create one now
					if (condition == null) {
						condition = new Condition();
					}
					String[] newList = tokens[1].split(",");
					try {
						for (int i=0; i<newList.length; i++) {
							// add each response as one "rule" (which will be a list of responses)
							int value = Integer.parseInt(newList[i]);
							Response response = new Response(value);
							condition.addResponse(response);
						}
					}
					catch (Exception e) {
						// if you can't parse the string, then we won't store that conditional response
					}
				} else if(tokens[0].equalsIgnoreCase("TH")) {
					try {
						survey.setScoreThreshold(Integer.parseInt(tokens[1]));
					} catch (Exception e) {

					}
				}
			}

			// This is for the very last question
			if (question != null) {
				if ((conditions != null) && (conditions.size() > 0)) {
					question.addConditions(conditions); 
				}
				survey.addQuestion(question);
			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return survey;
	}

	public void initializeFlipper(){
		flipper = (FixedFlipper)findViewById(R.id.flipper);

		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftIn.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				flipper.postDelayed(new Runnable() {
					@Override
					public void run() {
					}
				}, 1);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});
		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
		slideRightIn.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				flipper.postDelayed(new Runnable() {
					@Override
					public void run() {
					}
				}, 1);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});
		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

		flipper.removeAllViews();
		flipper.addView(this.getLayoutInflater().inflate(
				R.layout.survey_question, flipper, false));

		flipper.addView(this.getLayoutInflater().inflate(
				R.layout.survey_question, flipper, false));

	}

	private void showInitialSurveyPrompt() {
		int displayedChild = flipper.getDisplayedChild();

		View childAt = flipper.getChildAt((displayedChild + 1) % 2);

		TextView tv = (TextView)childAt.findViewById(R.id.question_number);
		tv.setVisibility(View.GONE);
		tv = (TextView)childAt.findViewById(R.id.question_prompt);
		tv.setText(mSurveys.get(mCurrentSurveyIndex).getPrompt());

		LinearLayout layout = (LinearLayout) childAt.findViewById(R.id.survey_answer_container);
		layout.removeAllViews();

		flipper.setInAnimation(slideLeftIn);
		flipper.setOutAnimation(slideLeftOut);
		flipper.showNext();

		updateButtonFooter();
	}

	private void changeFlipperView(int questionIndex, boolean goForward) {

		if ((mCurrentSurveyIndex < mSurveys.size()) && (questionIndex < mSurveys.get(mCurrentSurveyIndex).getQuestions().size())) {

			int displayedChild = flipper.getDisplayedChild();

			final View childAt = flipper.getChildAt((displayedChild + 1) % 2);

			TextView tv = (TextView)childAt.findViewById(R.id.question_number);
			// if this is the last question, then we won't show a number for the question
			if (mSurveys.get(mCurrentSurveyIndex).getQuestion(questionIndex).getQuestionType() == QuestionType.EOS) {
				tv.setVisibility(View.INVISIBLE);
			}
			else {
				tv.setText((mCurrentQuestionListIndex+1) + ".");
				tv.setVisibility(View.VISIBLE);
			}
			tv = (TextView)childAt.findViewById(R.id.question_prompt);
			tv.setText(mSurveys.get(mCurrentSurveyIndex).getQuestion(questionIndex).getPrompt());

			LinearLayout layout = (LinearLayout) childAt.findViewById(R.id.survey_answer_container);
			layout.removeAllViews();

			flipper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

			mSelectedAnswerIndices.clear();
			switch(mSurveys.get(mCurrentSurveyIndex).getQuestion(questionIndex).getQuestionType()) {
			case SINGLE_CHOICE:
				layout.addView(createSingleChoiceQuestion(questionIndex));
				break;
			case MULTIPLE_CHOICE:
				List<CheckBox> checkboxes = createMultipleChoiceQuesion(questionIndex);
				for (int i=0; i<checkboxes.size(); i++) {
					layout.addView(checkboxes.get(i));
				}
				break;
			case FREEFORM_TEXT:
				mFreeFormTextBox = new EditText(this);
				mFreeFormTextBox.setMinLines(5);
				// get the text (if this is a previous answer)
				if ((mSurveys.get(mCurrentSurveyIndex).getResponseSet(mCurrentQuestionIndex) != null) && 
						(mSurveys.get(mCurrentSurveyIndex).getResponseSet(mCurrentQuestionIndex).size() > 0)) {
					mFreeFormTextBox.setText(mSurveys.get(mCurrentSurveyIndex).getResponseSet(questionIndex).get(0).getText());
				}
				mFreeFormTextBox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						showDialog(FREEFORMTEXT_DIALOG_ID);
					}
				});

				layout.addView(mFreeFormTextBox);
				break;
			}

			ViewTreeObserver vto = flipper.getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					flipper.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, childAt.getMeasuredHeight()));
					flipper.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			});

			if(goForward) {
				flipper.setInAnimation(slideLeftIn);
				flipper.setOutAnimation(slideLeftOut);
				flipper.showNext();
			} else {
				flipper.setInAnimation(slideRightIn);
				flipper.setOutAnimation(slideRightOut);
				flipper.showPrevious();
			}

			updateButtonFooter();
		}
	}

	private void updateButtonFooter() {
		// if we haven't started the survey, then the two options will be "next" and "cancel"
		if (!mStartedSurvey) {
			setButtonFooter("next", mNextClickListener, "cancel", mBackClickListener);
		}
		else {
			Question q = mSurveys.get(mCurrentSurveyIndex).getQuestion(mCurrentQuestionIndex);
			// at the start, there will only be one button that says "next"
			if ((mCurrentQuestionIndex == 0) && (mCurrentSurveyIndex == 0)){
				setButtonFooter("next", mNextClickListener, null, null);
			}
			// if we're at the end, there will only be two buttons that say "back" and "finished"
			else if (q.getQuestionType() == QuestionType.EOS) {
				setButtonFooter("finished", mNextClickListener, "back", mBackClickListener);
			}
			// if we're somewhere in the middle, then there will be two button that say "back" and "next"
			else {
				setButtonFooter("next", mNextClickListener, "back", mBackClickListener);
			}
		}
	}

	private View createSingleChoiceQuestion(int questionIndex) {
		Survey survey = mSurveys.get(mCurrentSurveyIndex);
		RadioGroup radioGroup = new RadioGroup(this);
		List<AnswerChoice> answers = survey.getQuestion(questionIndex).getAnswers();
		for (int i=0; i<answers.size(); i++) {
			RadioButton rb = new RadioButton(this);
			rb.setText(answers.get(i).getText());
			rb.setId(i);
			radioGroup.addView(rb);
		}
		// get the index of the checked answer (if this is a previous answer)
		if ((survey.getResponseSet(questionIndex) != null) && (survey.getResponseSet(questionIndex).size() > 0)) {
			radioGroup.check(survey.getResponseSet(questionIndex).get(0).getData());
			mSelectedAnswerIndices.add(survey.getResponseSet(questionIndex).get(0).getData());
		}

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			// checkedId is -1 when nothing is selected (or selection is cleared)
			@Override
			public void onCheckedChanged(RadioGroup rg, int checkedId) {
				// if something is selected
				if (checkedId != -1) {
					// clear current list because radiogroup only allows 1 selection
					mSelectedAnswerIndices.clear();
					mSelectedAnswerIndices.add(checkedId);
				}
			}
		});
		return radioGroup;
	}

	private List<CheckBox> createMultipleChoiceQuesion(int questionIndex) {
		Survey survey = mSurveys.get(mCurrentSurveyIndex);
		List<CheckBox> checkboxes = new ArrayList<CheckBox>();
		List<AnswerChoice> answers = survey.getQuestion(questionIndex).getAnswers();
		for (int i=0; i<answers.size(); i++) {
			CheckBox cb = new CheckBox(this);
			cb.setText(answers.get(i).getText());
			cb.setId(i);
			cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					CheckBox cb = (CheckBox) buttonView;
					// if you unchecked a checkbox, remove it from the list
					if (!isChecked) {
						mSelectedAnswerIndices.remove(mSelectedAnswerIndices.indexOf(cb.getId()));
					}
					// otherwise, add it to the list
					else if (isChecked) {
						// there is some double-checking going on, so let's make sure there aren't any duplicates when we add it
						if (mSelectedAnswerIndices.indexOf(cb.getId()) == -1) {
							mSelectedAnswerIndices.add(cb.getId());
						}
					}
				}
			});
			checkboxes.add(cb);
		}
		for (int i=0; i<survey.getResponseSet(questionIndex).size(); i++) {
			checkboxes.get(survey.getResponseSet(questionIndex).get(i).getData()).setChecked(true);
		}
		return checkboxes;
	}

	public View.OnClickListener mNextClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Question q = mSurveys.get(mCurrentSurveyIndex).getQuestion(mCurrentQuestionIndex);
			// if the survey hasn't started yet and the user clicks next, it means we can start the survey
			if (!mStartedSurvey) {
				mStartedSurvey = true;
				mCurrentSurveyIndex = 0;
				mCurrentQuestionIndex = 0;
				changeFlipperView(mCurrentQuestionIndex, true);
			}
			// we've started the survey, but nothing was selected for this survey question
			// and this is not an EOS type of question (which can contain no answers), so prompt the user
			else if (mStartedSurvey && 
					(((mSelectedAnswerIndices.size() == 0) && ((q.getQuestionType() == QuestionType.SINGLE_CHOICE) || (q.getQuestionType() == QuestionType.MULTIPLE_CHOICE))) ||
							((mFreeFormTextBox != null) && (mFreeFormTextBox.getText().length() == 0) && (q.getQuestionType() == QuestionType.FREEFORM_TEXT)))) {
				showDialog(VALIDATE_ANSWER_DIALOG_ID);
			}
			// we've started the survey, something was selected, so we record it and show next question
			else {
				recordSelectedAnswerChoices();
				// we've reached the end of this survey, so figure out if we need to advance to next survey or if we're reached the end of all surveys
				if ((q.getQuestionType() == QuestionType.EOS) || (mCurrentQuestionIndex == (mSurveys.get(mCurrentSurveyIndex).getQuestions().size()-1)) ) {
					mCurrentSurveyIndex++;
					// initial question of next (new) survey will never be conditional, so we won't check that
					if (mCurrentSurveyIndex < mSurveys.size()) {
						mCurrentQuestionIndex = 0;
						advanceTo(0);
						changeFlipperView(mCurrentQuestionIndex, true);
					}
					// we've showed all the questions from all the surveys, so show a finishing dialog
					else {
						showDialog(FINISHED_SURVEYS_DIALOG_ID);
					}
				}
				// else we're still inside the survey, so advance to the next question in the survey
				else {
					// there are two ways to advance
					// if there are no conditions attached to the question, then just increment the counter 
					if (!q.isConditionalQuestion()) {
						advance();
					}
					// if there are conditions, we need to check to see if the current responses match any of the condition
					// if they do, we go to the specified question index
					// otherwise, if they don't, then we just proceed to the next question (by incrementing the counter)
					else {
						List<Response> responses = mSurveys.get(mCurrentSurveyIndex).getResponseSet(mCurrentQuestionIndex);
						int questionId = q.checkConditions(responses);
						// else we found a match with the condition, so we go to the specified question index
						if (questionId != -1) {
							advanceTo(questionId);
						}
						// otherwise, we didn't find a match, so just increment the counter
						else {
							advance();
						}
					}
					changeFlipperView(mCurrentQuestionIndex, true);
				}
			}
		}
	};

	private void advance() {
		advanceTo(mCurrentQuestionIndex+1);
	}

	private void advanceTo(int index) {
		mCurrentQuestionListIndex++;

		// if the indexes are different, it means we are creating a new path 
		// (probably by backing up, changing the responses, and triggered a new conditional response (leading to a new pathway)
		if ((mCurrentQuestionListIndex < mQuestionIndices.size()) && (mQuestionIndices.get(mCurrentQuestionListIndex) != index)) {
			// remove all the old questions, from the old path
			for (int i=mCurrentQuestionListIndex; i<mQuestionIndices.size(); i++) {
				mQuestionIndices.remove(i);
			}
		}
		mCurrentQuestionIndex = index;
		mQuestionIndices.add(mCurrentQuestionIndex);
	}

	public View.OnClickListener mBackClickListener = new View.OnClickListener() {
		public void onClick(View v) { 
			// if the survey hasn't started and the user clicks back, it means that they do not want to take the survey now, so we go back to the overview screen
			if (!mStartedSurvey) {
				setResult(RESULT_CANCELED);
				finish();
			}
			// if the survey has already started and they click back, then go to previous question, WITHOUT recording any current selections
			else{
				if (mStartedSurvey && (mCurrentQuestionIndex == 0) && (mCurrentSurveyIndex>0)) {
					mCurrentSurveyIndex--;
				}
				rewind();
				changeFlipperView(mCurrentQuestionIndex, false);
			}
		}
	};

	private void rewind() {
		rewind(1);
	}

	private void rewind(int stepSize) {
		if ((mQuestionIndices.size()-stepSize) >= 0) {
			mCurrentQuestionListIndex -= stepSize;
			mCurrentQuestionIndex = mQuestionIndices.get(mCurrentQuestionListIndex);
		}
	}

	private void recordSelectedAnswerChoices() {
		// get the responses that we already have for this question, remove them all, and replace with the new selections
		Survey survey = mSurveys.get(mCurrentSurveyIndex);
		survey.removeAllResponses(mCurrentQuestionIndex);

		Response response;
		switch(survey.getQuestion(mCurrentQuestionIndex).getQuestionType()) {
		case SINGLE_CHOICE:
			response = new Response();
			response.setData(mSelectedAnswerIndices.get(0));
			survey.removeAllResponses(mCurrentQuestionIndex);
			survey.addResponse(mCurrentQuestionIndex, response);
			break;
		case MULTIPLE_CHOICE:
			List<Response> responses = new ArrayList<Response>();
			survey.removeAllResponses(mCurrentQuestionIndex);
			for (int i=0; i<mSelectedAnswerIndices.size(); i++) {
				response = new Response(); 
				response.setData(mSelectedAnswerIndices.get(i));
				responses.add(response);
			}
			survey.addResponses(mCurrentQuestionIndex, responses);
			break;
		case FREEFORM_TEXT:
			response = new Response();
			response.setText(mFreeFormTextBox.getText().toString());
			survey.removeAllResponses(mCurrentQuestionIndex);
			survey.addResponse(mCurrentQuestionIndex, response);
			break;
		}
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int, android.os.Bundle)
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		switch (id) {
		case VALIDATE_ANSWER_DIALOG_ID:
			return ValidateAnswersDialog();
		case FINISHED_SURVEYS_DIALOG_ID:
			return FinishedSurveysDialog();
		case FREEFORMTEXT_DIALOG_ID:
			return new TextboxDialog.Builder(this)
			.setTitle("Write your response below")
			.setText(mFreeFormTextBox.getText().toString().trim())
			.setPositiveButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
					if (text.length() > 0) {
						mFreeFormTextBox.setText(text);
					}
				}
			})
			.setNegativeButton(new OnDialogResult() {
				@Override
				public void finish(String text) {
				}
			})
			.create();
		case MAKE_PHONE_CALL_DIALOG_ID:
			SurveyType stype = SurveyType.valueOf(bundle.getString("survey_type"));
			String prompt = bundle.getString("prompt");
			String phoneNumber = bundle.getString("phoneNumber");
			return makePhoneCallDialog(prompt, phoneNumber,	stype);
		case EXIT_SURVEY_FORM_DIALOG_ID:
			String message = bundle.getString("message");
			return exitSurveyFormDialog(message);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
	 */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		//dialog.setCanceledOnTouchOutside(true);
		switch (id) {
		case FREEFORMTEXT_DIALOG_ID:
			((TextboxDialog) dialog).setText(mFreeFormTextBox.getText().toString().trim());
			break;
		default:
			super.onPrepareDialog(id, dialog);
			break;
		}
	}

	private AlertDialog ValidateAnswersDialog() {
		String prompt = "";
		switch (mSurveys.get(mCurrentSurveyIndex).getQuestion(mCurrentQuestionIndex).getQuestionType()) {
		case SINGLE_CHOICE:
			prompt = "Please select one of the answer choices";
			break;
		case MULTIPLE_CHOICE:
			prompt = "Please select at least one of the answer choices";
			break;
		case FREEFORM_TEXT:
			prompt = "Please fill in the textbox with your answer";
			break;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(prompt);
		builder.setCancelable(true)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dismissDialog(VALIDATE_ANSWER_DIALOG_ID);
			}
		});
		return builder.create();
	}

	private AlertDialog FinishedSurveysDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.you_finished);
		builder.setMessage(R.string.end_of_survey_prompt);
		builder.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setResult(RESULT_OK);
				openDatabase();
				boolean shouldDismiss = true;
				for (Survey s : mSurveys) {
					s.removeStaleQuestions();
					if ((s.getSurveyType() == SurveyType.PRE_APPT) || (s.getSurveyType() == SurveyType.POST_APPT)) {
						s.setSurveyId(mIndicatorId);
					}

					// score the survey
					s.score();

					mDatabase.insertSingle(s);

					// check for issues 
					if( (s.getSurveyType() == SurveyType.EPDS) && (s.isShouldAlertStJoseph() || // if a specific question was triggered
							(s.getScoreThreshold() != -1 && s.getScore() >= s.getScoreThreshold()))) { // if they hit too high of a threshold					

						s.getCommonData().setFlag(Flag.urgent); // mark survey as urgent (this will get propogated to each of the survey responses)
						// show the dialog
						shouldDismiss = false;
						Bundle bundle = new Bundle();
						bundle.putString("prompt", getResources().getString(R.string.call_st_josephs_short));
						bundle.putString("phoneNumber", getResources().getString(R.string.st_josephs_phone_number));
						bundle.putString("survey_type", s.getSurveyType().name());
						showDialog(MAKE_PHONE_CALL_DIALOG_ID, bundle);
						
					}

					if ( (s.getSurveyType() == SurveyType.PRE_APPT) || (s.getSurveyType() == SurveyType.POST_APPT) ) {

						// get rid of any notifications associated with this survey
						String reminderString;
						if (s.getSurveyType() == SurveyType.PRE_APPT) {
							reminderString = getResources().getString(R.string.pre_appt_reminder) + " for " + mBaby.getName();
							SyncService.cancelNotification(SurveyForm.this, reminderString, mIndicatorId);
						}
						if (s.getSurveyType() == SurveyType.POST_APPT) {
							reminderString = getResources().getString(R.string.post_appt_reminder) + " for " + mBaby.getName();
							SyncService.cancelNotification(SurveyForm.this, reminderString, mIndicatorId);
						}

						openDatabase();
						Appointment a = mDatabase.getAppointmentTable().getAppointmentById(mIndicatorId);

						// if it's a POST_APPT survey and we do NOT have to reschedule, it means that they attended the appointment (which is different than the default)
						if ( (s.getSurveyType() == SurveyType.POST_APPT) && !s.isShouldRescheduleAppointment()) {
							a.setAttended(AttendedState.ATTENDED);
						}
						else if ( (s.getSurveyType() == SurveyType.POST_APPT) && s.isShouldRescheduleAppointment()) {
							a.setAttended(AttendedState.MISSED);
						}
						// looking only at pre-appt survey, we can't be sure if the appt is missed or attended. that flag is only set based on the post-appt surveys.
						else if (s.getSurveyType() == SurveyType.PRE_APPT) {
							a.setAttended(AttendedState.UNKNOWN);
						}
						// so set that flag and update the database
						mDatabase.update(a);
						mDatabase.logUpdate(getIntent(), a);

						// if it's a PRE_APPT or POST_APPT and it has the reschedule flag, then we should show the reschedule dialog
						if (s.isShouldRescheduleAppointment()) {
							shouldDismiss = false;
							String prompt = "To reschedule this appointment, please call Dr. " + a.getDoctorName() + "'s office";
							String phoneNumber = "";
							if (a.getPhone().length() > 0) {
								prompt += " at " + a.getPhone();
								phoneNumber = StringUtils.extractOnlyNumbers(a.getPhone());
							}
							else {
								prompt += ".";
							}
							Bundle bundle = new Bundle();
							bundle.putString("prompt", prompt);
							bundle.putString("phoneNumber", phoneNumber);
							bundle.putString("survey_type", s.getSurveyType().name());
							showDialog(MAKE_PHONE_CALL_DIALOG_ID, bundle);
						}
					}

				}
				if(shouldDismiss) {
					finish();
					dismissDialog(FINISHED_SURVEYS_DIALOG_ID);
				}
			}
		});
		return builder.create();
	}

	private AlertDialog makePhoneCallDialog(String message, final String phoneNumber, final SurveyType surveyType) {
		return new AlertDialog.Builder(SurveyForm.this)
		.setIcon(android.R.drawable.ic_dialog_info)
		.setTitle(R.string.tip)
		.setMessage(message)
		.setPositiveButton("Call now", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent phoneDialer = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:" + phoneNumber));
				startActivity(phoneDialer);
				finish();
				dismissDialog(MAKE_PHONE_CALL_DIALOG_ID);
			}
		})
		.setNegativeButton("Call later", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// if they choose to call later, we'll log it and then remind them again the next day
				// depending on what the survey is, we'll log different things (appointment-related surveys get an indicator id (the appointment id)
				String idString = "-1";
				switch (surveyType) {
				case PRE_APPT:
				case POST_APPT:
					idString = Integer.toString(mIndicatorId);
					break;
				}
				// check to make sure that we only log call later reminders the day before a business day
				// so weekend call laters will both be logged as sunday call laters
				// this will trigger the reminder to show up on monday 
				Log log = new Log(new CommonData(EstrellitaTiles.getParentId(SurveyForm.this), mBaby.getId()), surveyType.name(), CALL_LATER_TAG, idString);
				Date now = DateUtils.getTimestamp();
				if ((surveyType == SurveyType.PRE_APPT) || (surveyType == SurveyType.POST_APPT)) {
					switch (DateUtils.getDayOfWeek(now)) {
					case Calendar.FRIDAY:
					case Calendar.SATURDAY:
						log.getCommonData().setTimestamp(new Timestamp(DateUtils.getEndOfDay(DateUtils.getDateOfFollowingSunday(now)).getTime()));
						break;
					case Calendar.SUNDAY:
					default:
						log.getCommonData().setTimestamp(new Timestamp(DateUtils.getEndOfDay(now).getTime()));
						break;
					}
				}
				// the weekday only call later reminders are only for pre/post-appt surveys
				// for the epds surveys, we'll allow weekend call later reminders too
				else {
					log.getCommonData().setTimestamp(new Timestamp(DateUtils.getEndOfDay(now).getTime()));
				}
				openDatabase();
				mDatabase.insertSingle(log, false, false);
				finish();
				dismissDialog(MAKE_PHONE_CALL_DIALOG_ID);
			}
		}).create();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.uci.ics.star.estrellita.activity.TileActivity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mSurveys.size() > 0) {
			((GenericSurvey) mSurveys.get(mSurveys.size()-1)).saveState(mStartedSurvey, mCurrentSurveyIndex, mCurrentQuestionListIndex, mQuestionIndices);
			return mSurveys;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void restoreIndicator() {
		mIndicator = null; 
		if (getLastNonConfigurationInstance() != null) { 
			Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
			mSurveys = (List<GenericSurvey>) lastNonConfigurationInstance;
			if (mSurveys.size()>0) {
				mStartedSurvey = mSurveys.get(mSurveys.size()-1).isSurveyStarted();
				mCurrentSurveyIndex = mSurveys.get(mSurveys.size()-1).getSurveyIndex();
				mCurrentQuestionListIndex = mSurveys.get(mSurveys.size()-1).getQuestionListIndex();
				mQuestionIndices = mSurveys.get(mSurveys.size()-1).getQuestionIndices();
				mCurrentQuestionIndex = mQuestionIndices.get(mCurrentQuestionListIndex);
			}
		}
	}
}