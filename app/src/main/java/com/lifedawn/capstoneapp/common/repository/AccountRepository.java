package com.lifedawn.capstoneapp.common.repository;

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
	private MutableLiveData<GoogleSignInAccount> signInLiveData = new MutableLiveData<>();
	private MutableLiveData<GoogleSignInAccount> signOutLiveData = new MutableLiveData<>();
	
	public AccountRepository(Application application) {
		this.context = application.getApplicationContext();
		googleAccountUtil = GoogleAccountUtil.getInstance(context);
	}
	
	public MutableLiveData<GoogleSignInAccount> getSignInLiveData() {
		return signInLiveData;
	}
	
	public MutableLiveData<GoogleSignInAccount> getSignOutLiveData() {
		return signOutLiveData;
	}
	

	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, GoogleAccountUtil.OnSignCallback onSignCallback) {
		googleAccountUtil.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential) {
				onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
				signInLiveData.setValue(signInAccount);
			}
			
			@Override
			public void onSignOutSuccessful(GoogleSignInAccount signOutAccount) {
			
			}
		});
	}
	
	@Override
	public void signOut(GoogleSignInAccount account, GoogleAccountUtil.OnSignCallback onSignCallback) {
		googleAccountUtil.signOut(account, new GoogleAccountUtil.OnSignCallback() {
			@Override
			public void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential) {
			
			}
			
			@Override
			public void onSignOutSuccessful(GoogleSignInAccount signOutAccount) {
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
