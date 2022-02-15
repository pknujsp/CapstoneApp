package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.calendar.util.GoogleCalendarUtil;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnHttpApiCallback;
import com.lifedawn.capstoneapp.common.repository.AccountRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

public class AccountCalendarViewModel extends AndroidViewModel implements IAccountRepository {
	private Constant usingAccountType;
	private AccountRepository accountRepository;
	private MutableLiveData<GoogleSignInAccount> signInLiveData;
	private MutableLiveData<GoogleSignInAccount> signOutLiveData;
	private MutableLiveData<String> mainCalendarIdLiveData = new MutableLiveData<>();
	private String mainCalendarId;
	
	public AccountCalendarViewModel(@NonNull Application application) {
		super(application);
		this.accountRepository = new AccountRepository(application);
		signInLiveData = accountRepository.getSignInLiveData();
		signOutLiveData = accountRepository.getSignOutLiveData();
	}
	
	public LiveData<String> getMainCalendarIdLiveData() {
		return mainCalendarIdLiveData;
	}
	
	public void setMainCalendarId(String mainCalendarId) {
		this.mainCalendarId = mainCalendarId;
		mainCalendarIdLiveData.postValue(mainCalendarId);
	}
	
	public String getMainCalendarId() {
		return mainCalendarId;
	}
	
	public LiveData<GoogleSignInAccount> getSignOutLiveData() {
		return signOutLiveData;
	}
	
	public LiveData<GoogleSignInAccount> getSignInLiveData() {
		return signInLiveData;
	}
	
	private void setUsingAccountType(Constant usingAccountType) {
		this.usingAccountType = usingAccountType;
	}
	
	
	public Constant getUsingAccountType() {
		return usingAccountType;
	}
	

	
	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, GoogleAccountUtil.OnSignCallback onSignCallback) {
		accountRepository.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential) {
				onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
				setUsingAccountType(Constant.ACCOUNT_GOOGLE);
				
				GoogleCalendarUtil googleCalendarUtil = new GoogleCalendarUtil(googleAccountLifeCycleObserver);
				
				googleCalendarUtil.existingPromiseCalendar(googleCalendarUtil.getCalendarService(googleAccountCredential),
						new OnHttpApiCallback<CalendarListEntry>() {
							@Override
							public void onResultSuccessful(CalendarListEntry existing) {
								if (existing == null) {
									googleCalendarUtil.addPromiseCalendar(googleCalendarUtil.getCalendarService(googleAccountCredential),
											new OnHttpApiCallback<Calendar>() {
												@Override
												public void onResultSuccessful(Calendar e) {
													setMainCalendarId(e.getId());
												}
												
												@Override
												public void onResultFailed(Exception e) {
												
												}
											});
								} else {
									setMainCalendarId(existing.getId());
								}
							}
							
							@Override
							public void onResultFailed(Exception e) {
							
							}
						});
			}
			
			@Override
			public void onSignOutSuccessful(GoogleSignInAccount signOutAccount) {
			
			}
		});
	}
	
	@Override
	public void signOut(GoogleSignInAccount account, GoogleAccountUtil.OnSignCallback onSignCallback) {
		accountRepository.signOut(account, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential) {
			
			}
			
			@Override
			public void onSignOutSuccessful(GoogleSignInAccount signOutAccount) {
				onSignCallback.onSignOutSuccessful(signOutAccount);
				setUsingAccountType(Constant.ACCOUNT_LOCAL_WITHOUT_GOOGLE);
			}
		});
	}
	
	@Override
	public GoogleSignInAccount lastSignInAccount() {
		return accountRepository.lastSignInAccount();
	}
}
