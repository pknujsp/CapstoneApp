package com.lifedawn.capstoneapp.account.util;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

public class GoogleAccountUtil {
	private static GoogleAccountUtil instance;
	private final String[] CREDENTIAL_SCOPES = {CalendarScopes.CALENDAR};
	private GoogleAccountCredential googleAccountCredential;
	
	private Context context;
	
	public static GoogleAccountUtil getInstance(Context context) {
		if (instance == null) {
			instance = new GoogleAccountUtil(context);
		}
		return instance;
	}
	
	public GoogleAccountCredential getGoogleAccountCredential() {
		return googleAccountCredential;
	}
	
	private GoogleAccountUtil(Context context) {
		this.context = context;
	}
	
	public void signOut(GoogleSignInAccount signInAccount, OnSignCallback onSignCallback) {
		GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).setAccountName(
				signInAccount.getEmail()).build();
		GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
		googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(@NonNull Void unused) {
				onSignCallback.onSignOutSuccessful(signInAccount);
			}
		});
	}
	
	public GoogleSignInAccount lastSignInAccount() {
		return GoogleSignIn.getLastSignedInAccount(context);
	}
	
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, OnSignCallback onSignCallback) {
		GoogleSignInAccount lastSignInAccount = lastSignInAccount();
		
		if (lastSignInAccount != null) {
			setGoogleAccountCredential(lastSignInAccount);
			onSignCallback.onSignInSuccessful(lastSignInAccount, googleAccountCredential);
			return;
		}
		GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
				GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
		GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
		Intent intent = googleSignInClient.getSignInIntent();
		
		googleAccountLifeCycleObserver.launchGoogleSignInIntent(intent, new ActivityResultCallback<ActivityResult>() {
			@Override
			public void onActivityResult(ActivityResult result) {
				Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
				try {
					GoogleSignInAccount account = task.getResult(ApiException.class);
					setGoogleAccountCredential(account);
					onSignCallback.onSignInSuccessful(account, googleAccountCredential);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
	
	public void setGoogleAccountCredential(GoogleSignInAccount account) {
		googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(CREDENTIAL_SCOPES)).setBackOff(
				new ExponentialBackOff());
		googleAccountCredential.setSelectedAccount(account.getAccount());
	}
	
	public interface OnSignCallback {
		void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential);
		
		void onSignOutSuccessful(GoogleSignInAccount signOutAccount);
	}
	
	
}
