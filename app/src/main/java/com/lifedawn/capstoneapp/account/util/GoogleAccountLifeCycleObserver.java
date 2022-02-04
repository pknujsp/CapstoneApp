package com.lifedawn.capstoneapp.account.util;

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
    private ActivityResultCallback<ActivityResult> googleSignIntentActivityResultCallback;
    private ActivityResultCallback<ActivityResult> userRecoverableAuthIntentActivityResultCallback;

    public GoogleAccountLifeCycleObserver(ActivityResultRegistry resultRegistry, Activity activity) {
        this.resultRegistry = resultRegistry;
        this.activity = activity;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        googleSignIntentActivityResultLauncher = resultRegistry.register("googleSignIn", owner,
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (googleSignIntentActivityResultCallback != null) {
                            googleSignIntentActivityResultCallback.onActivityResult(result);
                        }

                    }
                });

        userRecoverableAuthIntentActivityResultLauncher = resultRegistry.register("userRecoverableAuth", owner,
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (userRecoverableAuthIntentActivityResultCallback != null) {
                            userRecoverableAuthIntentActivityResultCallback.onActivityResult(result);
                        }
                    }
                });
    }

    public void launchGoogleSignInIntent(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
        googleSignIntentActivityResultLauncher.launch(intent);
        googleSignIntentActivityResultCallback = callback;
    }

    public void launchUserRecoverableAuthIntent(Intent intent, @NonNull ActivityResultCallback<ActivityResult> callback) {
        userRecoverableAuthIntentActivityResultLauncher.launch(intent);
        userRecoverableAuthIntentActivityResultCallback = callback;
    }
}
