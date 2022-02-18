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

	private MutableLiveData<GoogleSignInAccount> signInLiveData = new MutableLiveData<>();
	private MutableLiveData<GoogleSignInAccount> signOutLiveData = new MutableLiveData<>();


	public AccountViewModel(@NonNull Application application) {
		super(application);
		this.accountRepository = new AccountRepository(application);
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
	public void signIn(GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver, AccountRepository.OnSignCallback onSignCallback) {
		accountRepository.signIn(googleAccountLifeCycleObserver, new AccountRepository.OnSignCallback() {
			@Override
			public void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential) {
				signInLiveData.setValue(signInAccount);
				onSignCallback.onSignInSuccessful(signInAccount, googleAccountCredential);
				setUsingAccountType(Constant.ACCOUNT_GOOGLE);
			}

			@Override
			public void onSignOutSuccessful(GoogleSignInAccount signOutAccount) {

			}
		});
	}

	@Override
	public void signOut(GoogleSignInAccount account, AccountRepository.OnSignCallback onSignCallback) {
		accountRepository.signOut(account, new AccountRepository.OnSignCallback() {
			@Override
			public void onSignInSuccessful(GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential) {

			}

			@Override
			public void onSignOutSuccessful(GoogleSignInAccount signOutAccount) {
				signOutLiveData.setValue(signOutAccount);
				onSignCallback.onSignOutSuccessful(signOutAccount);
				setUsingAccountType(Constant.ACCOUNT_LOCAL_WITHOUT_GOOGLE);
			}
		});
	}

	@Override
	public GoogleSignInAccount lastSignInAccount() {
		GoogleSignInAccount lastSignInAccount = accountRepository.lastSignInAccount();
		if (lastSignInAccount != null) {
			usingAccountType = Constant.ACCOUNT_GOOGLE;
		}
		return lastSignInAccount;
	}

	public GoogleAccountCredential getGoogleAccountCredential() {
		return accountRepository.getGoogleAccountCredential();
	}
}
