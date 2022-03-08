package com.lifedawn.capstoneapp.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.Map;

public class PermissionsLifeCycleObserver implements DefaultLifecycleObserver {
	private final ActivityResultRegistry mRegistry;
	private Activity activity;

	private ActivityResultLauncher<String[]> calendarPermissionsLauncher;
	private ActivityResultCallback<Boolean> calendarPermissionsCallback;

	public PermissionsLifeCycleObserver(FragmentActivity activity) {
		this.mRegistry = activity.getActivityResultRegistry();
		this.activity = activity;
	}

	@Override
	public void onCreate(@NonNull LifecycleOwner owner) {
		calendarPermissionsLauncher = mRegistry.register("calendar", new ActivityResultContracts.RequestMultiplePermissions(),
				new ActivityResultCallback<Map<String, Boolean>>() {
					@Override
					public void onActivityResult(Map<String, Boolean> result) {
						if (calendarPermissionsCallback != null) {
							calendarPermissionsCallback.onActivityResult(!result.containsValue(false));
						}
					}
				});
	}

	public void launchCalendarPermissionsLauncher(@NonNull ActivityResultCallback<Boolean> callback) {
		calendarPermissionsCallback = callback;
		calendarPermissionsLauncher.launch(new String[]{
				Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR});
	}

	public boolean checkCalendarPermissions() {
		if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			return false;
		}
	}
}
