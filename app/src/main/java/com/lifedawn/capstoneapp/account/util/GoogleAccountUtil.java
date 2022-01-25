package com.lifedawn.capstoneapp.account.util;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

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
	
	private Context context;
	
	public static GoogleAccountUtil getInstance(Context context) {
		if (instance == null) {
			instance = new GoogleAccountUtil(context);
		}
		return instance;
	}
	
	public GoogleAccountUtil(Context context) {
		this.context = context;
	}
	
	public Account getConnectedGoogleAccount() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String name = sharedPreferences.getString("connectedAccountName", "");
		String type = sharedPreferences.getString("connectedAccountType", "");
		return name.isEmpty() ? null : new Account(name, type);
	}
	
	public void connectNewGoogleAccount(Account account) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("connectedAccountName", account.name);
		editor.putString("connectedAccountType", account.type);
		editor.commit();
	}
	
	public void disconnectNewGoogleAccount() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("connectedAccountName", "");
		editor.putString("connectedAccountType", "");
		editor.commit();
	}
	
	public void signOut(Account signInAccount, OnSignCallback onSignCallback) {
		GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).setAccountName(
				signInAccount.name).build();
		GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
		googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(@NonNull Void unused) {
				disconnectNewGoogleAccount();
				onSignCallback.onSignOutSuccessful(signInAccount);
			}
		});
	}
	
	public GoogleSignInAccount lastSignInAccount() {
		return GoogleSignIn.getLastSignedInAccount(context);
	}
	
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, OnSignCallback onSignCallback) {
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
					Account signInAccount = account.getAccount();
					
					GoogleAccountCredential googleAccountCredential = GoogleAccountCredential.usingOAuth2(context,
							Arrays.asList(CREDENTIAL_SCOPES)).setBackOff(new ExponentialBackOff());
					googleAccountCredential.setSelectedAccount(signInAccount);
					
					connectNewGoogleAccount(signInAccount);
					onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
	/*
	private void test(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final HttpTransport httpTransport = new NetHttpTransport();
					final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
					
					Calendar calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential).setApplicationName(
							"promise").build();
				} catch (Exception e) {
					if (e instanceof UserRecoverableAuthIOException) {
						googleAccountLifeCycleObserver.launchUserRecoverableAuthIntent(((UserRecoverableAuthIOException) e).getIntent(),
								new ActivityResultCallback<ActivityResult>() {
									@Override
									public void onActivityResult(ActivityResult result) {
										if (result.getResultCode() == Activity.RESULT_OK) {
											test(googleAccountLifeCycleObserver);
										} else {
											Toast.makeText(context, R.string.denied_access_to_calendar, Toast.LENGTH_SHORT).show();
										}
										
									}
								});
					}
				}
				
			}
		});
		
		
	}
	
	 */
	
	public interface OnSignCallback {
		void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential);
		
		void onSignOutSuccessful(Account signOutAccount);
	}
	
	
}
