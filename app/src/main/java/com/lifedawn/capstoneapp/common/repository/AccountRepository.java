package com.lifedawn.capstoneapp.common.repository;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

public class AccountRepository implements IAccountRepository {
	private Context context;
	private GoogleAccountUtil googleAccountUtil;
	
	public AccountRepository(Application application) {
		this.context = application.getApplicationContext();
		googleAccountUtil = GoogleAccountUtil.getInstance(context);
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
		googleAccountUtil.signIn(googleAccountLifeCycleObserver, onSignCallback);
	}
	
	@Override
	public void signOut(Account signInAccount, GoogleAccountUtil.OnSignCallback onSignCallback) {
		googleAccountUtil.signOut(signInAccount, onSignCallback);
	}
	
	@Override
	public GoogleSignInAccount lastSignInAccount() {
		return googleAccountUtil.lastSignInAccount();
	}
}
