package com.lifedawn.capstoneapp.friends;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.capstoneapp.R;

public class AttendeeInfoDialog {
	public static AlertDialog show(Activity activity, String name, String email) {
		View view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.attendee_info, null);
		((TextView) view.findViewById(R.id.name)).setText(name);
		((TextView) view.findViewById(R.id.email)).setText(email);

		AlertDialog dialog = new MaterialAlertDialogBuilder(activity).setTitle(R.string.attendee_info).setView(view).setNegativeButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();

		dialog.show();

		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);

		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.8);

		dialog.getWindow().setAttributes(layoutParams);
		return dialog;
	}
}
