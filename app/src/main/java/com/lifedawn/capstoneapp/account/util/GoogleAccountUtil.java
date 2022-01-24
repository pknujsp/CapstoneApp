package com.lifedawn.capstoneapp.account.util;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.lifedawn.capstoneapp.main.MyApplication;

import java.util.Arrays;
import java.util.List;

public class GoogleAccountUtil {
	private static GoogleAccountUtil instance;
	private final String[] CREDENTIAL_SCOPES = {CalendarScopes.CALENDAR};
	private final String GOOGLE_SECRET_JSON_FILE = "/googleclientsecret.json";
	
	private Context context;
	private GoogleAccountCredential googleAccountCredential;
	
	public static GoogleAccountUtil getInstance(Context context) {
		if (instance == null) {
			instance = new GoogleAccountUtil(context);
		}
		return instance;
	}
	
	public GoogleAccountUtil(Context context) {
		this.context = context;
		
		
	}
	
	public static Account getConnectedGoogleAccount(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String name = sharedPreferences.getString("connectedAccountName", "");
		String type = sharedPreferences.getString("connectedAccountType", "");
		Account account = new Account(name, type);
		return account;
	}
	
	public static void connectNewGoogleAccount(Context context, Account account) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("connectedAccountName", account.name);
		editor.putString("connectedAccountType", account.type);
		editor.commit();
	}
	
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver) {
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
					Account loginAccount = account.getAccount();
					GoogleAccountUtil.connectNewGoogleAccount(context, loginAccount);
					
					googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(CREDENTIAL_SCOPES)).setBackOff(
							new ExponentialBackOff());
					googleAccountCredential.setSelectedAccount(loginAccount);
					test(googleAccountLifeCycleObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
	private void test(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				try {
					HttpTransport httpTransport = new NetHttpTransport();
					JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
					
					Calendar calendarService = new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential).setApplicationName(
							"promise").build();
					
					CalendarList calendarList = calendarService.calendarList().list().execute();
					List<CalendarListEntry> items = calendarList.getItems();
					
					int count = items.size();
				} catch (Exception e) {
					if (e instanceof UserRecoverableAuthIOException) {
						googleAccountLifeCycleObserver.launchUserRecoverableAuthIntent(((UserRecoverableAuthIOException) e).getIntent(),
								new ActivityResultCallback<ActivityResult>() {
									@Override
									public void onActivityResult(ActivityResult result) {
										
									}
								});
					}
				}
				
			}
		});
		
		
	}
	
	
}
