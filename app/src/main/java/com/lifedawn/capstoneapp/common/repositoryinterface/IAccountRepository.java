package com.lifedawn.capstoneapp.common.repositoryinterface;

import android.accounts.Account;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;

public interface IAccountRepository {
	Account getConnectedGoogleAccount();
	
	void connectNewGoogleAccount(Account account);
	
	void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, GoogleAccountUtil.OnSignCallback onSignCallback);
	
	void signOut(Account signInAccount, GoogleAccountUtil.OnSignCallback onSignCallback);
	
	GoogleSignInAccount lastSignInAccount();
}