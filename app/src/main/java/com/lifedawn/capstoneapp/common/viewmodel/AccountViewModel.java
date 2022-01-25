package com.lifedawn.capstoneapp.common.viewmodel;

import android.accounts.Account;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.common.Constant;
import com.lifedawn.capstoneapp.common.repository.AccountRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

public class AccountViewModel extends AndroidViewModel implements IAccountRepository {
	private Account signInGoogleAccount;
	private Constant usingAccountType;
	private AccountRepository accountRepository;
	
	public AccountViewModel(@NonNull Application application) {
		super(application);
		this.accountRepository = new AccountRepository(application);
	}
	
	public void setSignInGoogleAccount(Account signInGoogleAccount) {
		this.signInGoogleAccount = signInGoogleAccount;
	}
	
	public void setUsingAccountType(Constant usingAccountType) {
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
		accountRepository.signIn(googleAccountLifeCycleObserver, onSignCallback);
	}
	
	@Override
	public void signOut(Account signInAccount, GoogleAccountUtil.OnSignCallback onSignCallback) {
		accountRepository.signOut(signInAccount, onSignCallback);
	}
	
	@Override
	public GoogleSignInAccount lastSignInAccount() {
		return accountRepository.lastSignInAccount();
	}
}
