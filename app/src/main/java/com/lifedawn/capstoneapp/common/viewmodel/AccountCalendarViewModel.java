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
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.calendar.util.GoogleCalendarUtil;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.HttpCallback;
import com.lifedawn.capstoneapp.common.interfaces.OnHttpApiCallback;
import com.lifedawn.capstoneapp.common.repository.AccountRepository;
import com.lifedawn.capstoneapp.common.repository.CalendarRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICalendarRepository;

public class AccountCalendarViewModel extends AndroidViewModel implements IAccountRepository, ICalendarRepository {
	private Constant usingAccountType;
	private AccountRepository accountRepository;
	private CalendarRepository calendarRepository;
	private MutableLiveData<GoogleSignInAccount> signInLiveData;
	private MutableLiveData<GoogleSignInAccount> signOutLiveData;
	private MutableLiveData<Event> eventLiveData = new MutableLiveData<>();
	private MutableLiveData<String> mainCalendarIdLiveData = new MutableLiveData<>();
	private String mainCalendarId;

	public AccountCalendarViewModel(@NonNull Application application) {
		super(application);
		this.accountRepository = new AccountRepository(application);
		this.calendarRepository = new CalendarRepository();
		signInLiveData = accountRepository.getSignInLiveData();
		signOutLiveData = accountRepository.getSignOutLiveData();
	}

	public LiveData<String> getMainCalendarIdLiveData() {
		return mainCalendarIdLiveData;
	}

	public LiveData<Event> getEventLiveData() {
		return eventLiveData;
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

	@Override
	public void saveEvent(com.google.api.services.calendar.Calendar calendarService, Event newEvent, String calendarId, HttpCallback<Event> callback) {
		calendarRepository.saveEvent(calendarService, newEvent, calendarId, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				callback.onResponseSuccessful(result);
				eventLiveData.postValue(result);
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	public void updateEvent(com.google.api.services.calendar.Calendar calendarService, Event editEvent, String calendarId, HttpCallback<Event> callback) {
		calendarRepository.updateEvent(calendarService, editEvent, calendarId, new HttpCallback<Event>() {
			@Override
			public void onResponseSuccessful(Event result) {
				callback.onResponseSuccessful(result);
				eventLiveData.postValue(result);
			}

			@Override
			public void onResponseFailed(Exception e) {

			}
		});
	}

	@Override
	public void sendResponseForInvitedPromise(com.google.api.services.calendar.Calendar calendarService, String calendarId, String myEmail, Event event, boolean acceptance, OnHttpApiCallback<Boolean> callback) {
		calendarRepository.sendResponseForInvitedPromise(calendarService, calendarId, myEmail, event, acceptance, new OnHttpApiCallback<Boolean>() {
			@Override
			public void onResultSuccessful(Boolean e) {
				callback.onResultSuccessful(e);
				eventLiveData.postValue(null);
			}

			@Override
			public void onResultFailed(Exception e) {

			}
		});
	}
}
