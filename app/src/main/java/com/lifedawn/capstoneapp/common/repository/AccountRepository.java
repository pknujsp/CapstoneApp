package com.lifedawn.capstoneapp.common.repository;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

public class AccountRepository implements IAccountRepository {
	private Context context;
	private GoogleAccountUtil googleAccountUtil;
	private MutableLiveData<Account> signInLiveData = new MutableLiveData<>();
	private MutableLiveData<Account> signOutLiveData = new MutableLiveData<>();
	
	public AccountRepository(Application application) {
		this.context = application.getApplicationContext();
		googleAccountUtil = GoogleAccountUtil.getInstance(context);
	}
	
	public MutableLiveData<Account> getSignInLiveData() {
		return signInLiveData;
	}
	
	public MutableLiveData<Account> getSignOutLiveData() {
		return signOutLiveData;
	}
	
	@Override
	public Account getConnectedGoogleAccount() {
		return googleAccountUtil.getConnectedGoogleAccount();
	}
	
	@Override
	public void connectNewGoogleAccount(Account account) {
		googleAccountUtil.connectNewGoogleAccount(account);
	}
	
	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, GoogleAccountUtil.OnSignCallback onSignCallback) {
		googleAccountUtil.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
				onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
				signInLiveData.setValue(signInAccount);
			}
			
			@Override
			public void onSignOutSuccessful(Account signOutAccount) {
			
			}
		});
	}
	
	@Override
	public void signOut(Account signInAccount, GoogleAccountUtil.OnSignCallback onSignCallback) {
		googleAccountUtil.signOut(signInAccount, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
			
			}
			
			@Override
			public void onSignOutSuccessful(Account signOutAccount) {
				onSignCallback.onSignOutSuccessful(signOutAccount);
				signOutLiveData.setValue(signOutAccount);
			}
		});
	}
	
	@Override
	public GoogleSignInAccount lastSignInAccount() {
		return googleAccountUtil.lastSignInAccount();
	}
}
