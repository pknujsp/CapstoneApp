package com.lifedawn.capstoneapp.reminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.view.WindowManager;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.databinding.ActivityNotificationBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
	private ArrayList<ContentValues> eventList;
	private ActivityNotificationBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
						WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		super.onCreate(savedInstanceState);

		binding = DataBindingUtil.setContentView(this, R.layout.activity_notification);

		Bundle bundle = getIntent().getExtras();
		eventList = bundle.getParcelableArrayList("eventList");

		binding.eventTitle.setText(eventList.get(0).getAsString(CalendarContract.Events.TITLE));
		binding.closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishAndRemoveTask();
			}
		});
	}
}