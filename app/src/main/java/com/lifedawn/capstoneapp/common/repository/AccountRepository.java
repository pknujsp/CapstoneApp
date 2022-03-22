package com.lifedawn.capstoneapp.common.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

import java.util.Arrays;

public class AccountRepository implements IAccountRepository {
	private static AccountRepository instance;
	private static final String[] CREDENTIAL_SCOPES = new String[]{CalendarScopes.CALENDAR};
	private Context context;
	private GoogleAccountCredential googleAccountCredential;
	private GoogleSignInAccount currentSignInAccount;
	private String lastSignInAccountName;

	private MutableLiveData<GoogleSignInAccount> signInLiveData = new MutableLiveData<>();
	private MutableLiveData<GoogleSignInAccount> signOutLiveData = new MutableLiveData<>();

	public LiveData<GoogleSignInAccount> getSignInLiveData() {
		return signInLiveData;
	}

	public LiveData<GoogleSignInAccount> getSignOutLiveData() {
		return signOutLiveData;
	}

	public static AccountRepository getInstance(Application application) {
		if (instance == null) {
			instance = new AccountRepository(application);
		}
		return instance;
	}

	public String getLastSignInAccountName() {
		return lastSignInAccountName;
	}

	private AccountRepository(Application application) {
		this.context = application.getApplicationContext();
		lastSignInAccountName =
				PreferenceManager.getDefaultSharedPreferences(context).getString(SharedPreferenceConstant.LAST_SIGNIN_ACCOUNT_NAME.getVal(),
						"");
		if (lastSignInAccountName.isEmpty()) {
			lastSignInAccountName = null;
		}
	}

	@Override
	public GoogleSignInAccount getLastSignInAccount() {
		GoogleSignInAccount lastSignInAccount = GoogleSignIn.getLastSignedInAccount(context);
		if (lastSignInAccount != null) {
			setGoogleAccountCredential(lastSignInAccount);
			currentSignInAccount = lastSignInAccount;
		}
		return lastSignInAccount;
	}

	public GoogleSignInAccount getCurrentSignInAccount() {
		return currentSignInAccount;
	}

	@Override
	public void signOut(OnSignCallback onSignCallback) {
		if (currentSignInAccount == null) {
			onSignCallback.onSignOutResult(true, null);
		} else {
			GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).setAccountName(
					currentSignInAccount.getEmail()).build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);

			googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
				@Override
				public void onSuccess(@NonNull Void unused) {
					onSignCallback.onSignOutResult(true, currentSignInAccount);
					signOutLiveData.setValue(currentSignInAccount);

					currentSignInAccount = null;
				}
			});
		}
	}

	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, OnSignCallback onSignCallback) {
		GoogleSignInAccount lastSignInAccount = getLastSignInAccount();

		if (lastSignInAccount != null) {
			//로그인 불필요
			setGoogleAccountCredential(lastSignInAccount);
			currentSignInAccount = lastSignInAccount;
			onSignCallback.onSignInResult(true, currentSignInAccount, googleAccountCredential, null);
			signInLiveData.setValue(currentSignInAccount);
		} else {
			//로그인 필요
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
						currentSignInAccount = account;

						CalendarRepository calendarRepository = CalendarRepository.getInstance();
						calendarRepository.createCalendarService(googleAccountCredential, googleAccountLifeCycleObserver, new BackgroundCallback<Calendar>() {
							@Override
							public void onResultSuccessful(Calendar e) {
								lastSignInAccountName = account.getEmail();
								SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
								editor.putString(SharedPreferenceConstant.LAST_SIGNIN_ACCOUNT_NAME.getVal(), lastSignInAccountName).commit();
								onSignCallback.onSignInResult(true, currentSignInAccount, googleAccountCredential, null);
								signInLiveData.postValue(currentSignInAccount);
							}

							@Override
							public void onResultFailed(Exception e) {
								onSignCallback.onSignInResult(false, null, null, e);
							}
						});

					} catch (Exception e) {
						onSignCallback.onSignInResult(false, null, null, e);
					}

				}
			});
		}
	}


	public GoogleAccountCredential getGoogleAccountCredential() {
		return googleAccountCredential;
	}

	private void setGoogleAccountCredential(GoogleSignInAccount account) {
		googleAccountCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(CREDENTIAL_SCOPES)).setBackOff(
				new ExponentialBackOff());
		googleAccountCredential.setSelectedAccount(account.getAccount());
	}

	public interface OnSignCallback {
		void onSignInResult(boolean succeed, @Nullable GoogleSignInAccount signInAccount,
		                    @Nullable GoogleAccountCredential googleAccountCredential,
		                    @Nullable Exception e);

		void onSignOutResult(boolean succeed, @Nullable GoogleSignInAccount signOutAccount);
	}

}
