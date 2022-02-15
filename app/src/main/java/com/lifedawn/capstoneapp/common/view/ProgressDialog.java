package com.lifedawn.capstoneapp.common.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.capstoneapp.R;

import java.util.Stack;

public class ProgressDialog {
	private ProgressDialog() {}
	
	private final static Stack<AlertDialog> dialogStack = new Stack<>();
	
	public static AlertDialog showDialog(Activity activity) {
		clearDialogs();
		View view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.progress_dialog, null);
		AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setView(view).setCancelable(false).create();
		dialogStack.push(dialog);
		
		dialog.show();
		
		return dialog;
		
	}
	
	public static void clearDialogs() {
		while (!dialogStack.empty()) {
			dialogStack.pop().dismiss();
		}
	}
}
