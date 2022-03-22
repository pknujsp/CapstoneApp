package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.repository.AccountRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.IAccountRepository;

public class AccountViewModel extends AndroidViewModel implements IAccountRepository {
	private Constant usingAccountType;
	private AccountRepository accountRepository;

	public AccountViewModel(@NonNull Application application) {
		super(application);
		this.accountRepository = AccountRepository.getInstance(application);
	}


	public LiveData<GoogleSignInAccount> getSignOutLiveData() {
		return accountRepository.getSignOutLiveData();
	}

	public LiveData<GoogleSignInAccount> getSignInLiveData() {
		return accountRepository.getSignInLiveData();
	}


	public String getLastSignInAccountName() {
		return accountRepository.getLastSignInAccountName();
	}

	public Constant getUsingAccountType() {
		return usingAccountType;
	}

	public GoogleSignInAccount getCurrentSignInAccount() {
		return accountRepository.getCurrentSignInAccount();
	}

	@Override
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, AccountRepository.OnSignCallback onSignCallback) {
		accountRepository.signIn(googleAccountLifeCycleObserver, new AccountRepository.OnSignCallback() {
			@Override
			public void onSignInResult(boolean succeed, GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential, Exception e) {
				if (succeed) {
					usingAccountType = Constant.ACCOUNT_GOOGLE;
				}
				onSignCallback.onSignInResult(succeed, signInAccount, googleAccountCredential, e);
			}

			@Override
			public void onSignOutResult(boolean succeed, GoogleSignInAccount signOutAccount) {
			}
		});
	}

	@Override
	public void signOut(AccountRepository.OnSignCallback onSignCallback) {
		accountRepository.signOut(new AccountRepository.OnSignCallback() {
			@Override
			public void onSignInResult(boolean succeed, GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential, Exception e) {

			}

			@Override
			public void onSignOutResult(boolean succeed, GoogleSignInAccount signOutAccount) {
				usingAccountType = Constant.ACCOUNT_LOCAL_WITHOUT_GOOGLE;
				onSignCallback.onSignOutResult(succeed, signOutAccount);
			}
		});
	}

	@Override
	public GoogleSignInAccount getLastSignInAccount() {
		GoogleSignInAccount lastSignInAccount = accountRepository.getLastSignInAccount();
		if (lastSignInAccount != null) {
			usingAccountType = Constant.ACCOUNT_GOOGLE;
		}
		return lastSignInAccount;
	}

	public GoogleAccountCredential getGoogleAccountCredential() {
		return accountRepository.getGoogleAccountCredential();
	}
}
