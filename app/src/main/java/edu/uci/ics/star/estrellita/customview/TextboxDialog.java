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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import edu.uci.ics.star.estrellita.R;

public class TextboxDialog extends AlertDialog {
	public interface OnDialogResult {
		public void finish(String text);
	}

	private Context mContext;
	private EditText mNotesView;

	public TextboxDialog(Context context) {
		super(context);
		mContext = context;

		// show the default textbox dialog layout
		View dialogView = LayoutInflater.from(mContext).inflate(R.layout.textbox_dialog, null);
		mNotesView  = (EditText) dialogView.findViewById(R.id.alert_dialog_edit_text);
		mNotesView.setMinLines(4);

		setView(dialogView);
		setCancelable(false);
	}

	protected void setOKButton(final OnDialogResult dialogResult) {
		setButton(BUTTON_POSITIVE, mContext.getString(R.string.submit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if(dialogResult != null){
					dialogResult.finish(mNotesView.getText().toString().trim());
				}
				mNotesView.setText("");
				dialog.dismiss();
			}
		});
	}
	
	protected void setCancelButton(final OnDialogResult dialogResult) {
		setButton(BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if(dialogResult != null){
					dialogResult.finish(mNotesView.getText().toString().trim());
				}
				mNotesView.setText("");
				dialog.dismiss();
			}
		});
	}
	
	protected void setMinLines(int lines) {
		mNotesView.setMinLines(lines);
	}
	
	protected void setHintText(String text) {
		mNotesView.setHint(text);
		mNotesView.setHintTextColor(mContext.getResources().getColor(R.color.darkgrey));
	}
	
	public void setText(String text) {
		mNotesView.setText(text);
	}

	public static class Builder extends AlertDialog.Builder {
		private TextboxDialog mDialog;

		public Builder(Context context) {
			super(context);
			mDialog = new TextboxDialog(context);
		}

		@Override
		public TextboxDialog create() {
			return mDialog;
		}

		@Override
		public Builder setTitle(CharSequence title) {
			mDialog.setTitle(title);
			return this;
		}

		public Builder setPositiveButton(OnDialogResult dialogResult) {
			mDialog.setOKButton(dialogResult);
			return this;
		}

		public Builder setNegativeButton(OnDialogResult dialogResult) {
			mDialog.setCancelButton(dialogResult);
			return this;
		}
		
		public Builder setMinLines(int lines) {
			mDialog.setMinLines(lines);
			return this;
		}
		
		public Builder setHintText(String text) {
			mDialog.setHintText(text);
			return this;
		}
		
		public Builder setText(String text) {
			if (text.length()>0) {
				mDialog.setText(text);
			}
			return this;
		}
	}
}