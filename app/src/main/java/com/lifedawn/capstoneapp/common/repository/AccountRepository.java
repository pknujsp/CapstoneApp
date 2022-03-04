package com.lifedawn.capstoneapp.common.repository;

import android.app.Application;
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
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

import java.util.Arrays;

public class AccountRepository implements IAccountRepository {
	private static final String[] CREDENTIAL_SCOPES = {CalendarScopes.CALENDAR};
	private static GoogleAccountCredential googleAccountCredential;
	private Context context;

	public AccountRepository(Application application) {
		this.context = application.getApplicationContext();
	}

	@Override
	public GoogleSignInAccount lastSignInAccount() {
		GoogleSignInAccount lastSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
		if (lastSignInAccount != null) {
			setGoogleAccountCredential(lastSignInAccount);
		}
		return lastSignInAccount;
	}

	@Override
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

	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, OnSignCallback onSignCallback) {
		final GoogleSignInAccount lastSignInAccount = lastSignInAccount();

		if (lastSignInAccount != null) {
			//로그인 불필요
			setGoogleAccountCredential(lastSignInAccount);
			onSignCallback.onSignInSuccessful(lastSignInAccount, googleAccountCredential);
			return;
		} else {
			GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
					GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
			googleAccountLifeCycleObserver.launchGoogleSignInIntent(googleSignInClient.getSignInIntent(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
					try {
						GoogleSignInAccount account = task.getResult(ApiException.class);
						setGoogleAccountCredential(account);

						CalendarRepository calendarRepository = new CalendarRepository(context);
						calendarRepository.createCalendarService(googleAccountCredential, googleAccountLifeCycleObserver, new BackgroundCallback<Calendar>() {
							@Override
							public void onResultSuccessful(Calendar e) {
								onSignCallback.onSignInSuccessful(account, googleAccountCredential);
							}

							@Override
							public void onResultFailed(Exception e) {
								onSignCallback.onSignInSuccessful(null, googleAccountCredential);
							}
						});

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
		}
	}


	public GoogleAccountCredential getGoogleAccountCredential() {
		return googleAccountCredential;
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
