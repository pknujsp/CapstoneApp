package com.lifedawn.capstoneapp.account;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class GoogleAccountLifeCycleObserver implements DefaultLifecycleObserver {
	private ActivityResultRegistry resultRegistry;
	private Activity activity;

	private ActivityResultLauncher<Intent> googleSignIntentActivityResultLauncher;
	private ActivityResultLauncher<Intent> userRecoverableAuthIntentActivityResultLauncher;
	private ActivityResultCallback<ActivityResult> methodGoogleSignIntentActivityResultCallback;
	private ActivityResultCallback<ActivityResult> methodUserRecoverableAuthIntentActivityResultCallback;
	private ActivityResultCallback<ActivityResult> instanceGoogleSignIntentActivityResultCallback;
	private ActivityResultCallback<ActivityResult> instanceUserRecoverableAuthIntentActivityResultCallback;

	public GoogleAccountLifeCycleObserver(ActivityResultRegistry resultRegistry, Activity activity) {
		this.resultRegistry = resultRegistry;
		this.activity = activity;
	}

	public void setInstanceUserRecoverableAuthIntentActivityResultCallback(ActivityResultCallback<ActivityResult> instanceUserRecoverableAuthIntentActivityResultCallback) {
		this.instanceUserRecoverableAuthIntentActivityResultCallback = instanceUserRecoverableAuthIntentActivityResultCallback;
	}

	public void setInstanceGoogleSignIntentActivityResultCallback(ActivityResultCallback<ActivityResult> instanceGoogleSignIntentActivityResultCallback) {
		this.instanceGoogleSignIntentActivityResultCallback = instanceGoogleSignIntentActivityResultCallback;
	}

	@Override
	public void onCreate(@NonNull LifecycleOwner owner) {
		googleSignIntentActivityResultLauncher = resultRegistry.register("googleSignIn", owner,
				new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (methodGoogleSignIntentActivityResultCallback != null) {
							methodGoogleSignIntentActivityResultCallback.onActivityResult(result);
						}

						if (instanceGoogleSignIntentActivityResultCallback != null) {
							instanceGoogleSignIntentActivityResultCallback.onActivityResult(result);
						}

					}
				});

		userRecoverableAuthIntentActivityResultLauncher = resultRegistry.register("userRecoverableAuth", owner,
				new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (methodUserRecoverableAuthIntentActivityResultCallback != null) {
							methodUserRecoverableAuthIntentActivityResultCallback.onActivityResult(result);
						}
						if (instanceUserRecoverableAuthIntentActivityResultCallback != null) {
							instanceUserRecoverableAuthIntentActivityResultCallback.onActivityResult(result);
						}
					}
				});
	}

	public void launchGoogleSignInIntent(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
		methodGoogleSignIntentActivityResultCallback = callback;
		googleSignIntentActivityResultLauncher.launch(intent);
	}

	public void launchUserRecoverableAuthIntent(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
		methodUserRecoverableAuthIntentActivityResultCallback = callback;
		userRecoverableAuthIntentActivityResultLauncher.launch(intent);
	}
}
