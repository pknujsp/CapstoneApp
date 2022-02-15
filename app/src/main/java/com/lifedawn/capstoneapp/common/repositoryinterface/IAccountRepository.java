package com.lifedawn.capstoneapp.common.repositoryinterface;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;

public interface IAccountRepository {
	
	
	void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, GoogleAccountUtil.OnSignCallback onSignCallback);
	
	void signOut(GoogleSignInAccount account, GoogleAccountUtil.OnSignCallback onSignCallback);
	
	GoogleSignInAccount lastSignInAccount();
}