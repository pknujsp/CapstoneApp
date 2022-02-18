package com.lifedawn.capstoneapp.common.repositoryinterface;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.repository.AccountRepository;

public interface IAccountRepository {
	
	
	void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, AccountRepository.OnSignCallback onSignCallback);
	
	void signOut(GoogleSignInAccount account, AccountRepository.OnSignCallback onSignCallback);
	
	GoogleSignInAccount lastSignInAccount();
}