package com.lifedawn.capstoneapp.common.viewmodel;

import android.accounts.Account;
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
	private Account signInGoogleAccount;
	private Constant usingAccountType;
	private AccountRepository accountRepository;
	private MutableLiveData<Account> signInLiveData;
	private MutableLiveData<Account> signOutLiveData;
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
	
	public LiveData<Account> getSignOutLiveData() {
		return signOutLiveData;
	}
	
	public LiveData<Account> getSignInLiveData() {
		return signInLiveData;
	}
	
	private void setSignInGoogleAccount(Account signInGoogleAccount) {
		this.signInGoogleAccount = signInGoogleAccount;
	}
	
	private void setUsingAccountType(Constant usingAccountType) {
		this.usingAccountType = usingAccountType;
	}
	
	public Account getSignInGoogleAccount() {
		return signInGoogleAccount;
	}
	
	public Constant getUsingAccountType() {
		return usingAccountType;
	}
	
	@Override
	public Account getConnectedGoogleAccount() {
		return accountRepository.getConnectedGoogleAccount();
	}
	
	@Override
	public void connectNewGoogleAccount(Account account) {
		accountRepository.connectNewGoogleAccount(account);
		setSignInGoogleAccount(account);
	}
	
	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, GoogleAccountUtil.OnSignCallback onSignCallback) {
		accountRepository.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
				onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
				setSignInGoogleAccount(signInAccount);
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
			public void onSignOutSuccessful(Account signOutAccount) {
			
			}
		});
	}
	
	@Override
	public void signOut(Account signInAccount, GoogleAccountUtil.OnSignCallback onSignCallback) {
		accountRepository.signOut(signInAccount, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
			
			}
			
			@Override
			public void onSignOutSuccessful(Account signOutAccount) {
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
