package com.lifedawn.capstoneapp.account;

import android.accounts.Account;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentProfileBinding;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends DialogFragment {
	private FragmentProfileBinding binding;
	private AccountViewModel accountViewModel;
	private GoogleAccountUtil googleAccountUtil;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
		googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentProfileBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.signOutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Account account = googleAccountUtil.getConnectedGoogleAccount();
				accountViewModel.signOut(account, new GoogleAccountUtil.OnSignCallback() {
					@Override
					public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
					
					}
					
					@Override
					public void onSignOutSuccessful(Account signOutAccount) {
					}
				});
			}
		});
		
		binding.signInBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				accountViewModel.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
					@Override
					public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
					}
					
					@Override
					public void onSignOutSuccessful(Account signOutAccount) {
					
					}
				});
			}
		});
		
		accountViewModel.getSignInLiveData().observe(getViewLifecycleOwner(), new Observer<Account>() {
			@Override
			public void onChanged(Account account) {
				if (account == null) {
				} else {
					Toast.makeText(getContext(), R.string.signin_successful, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		accountViewModel.getSignOutLiveData().observe(getViewLifecycleOwner(), new Observer<Account>() {
			@Override
			public void onChanged(Account account) {
				if (account == null) {
				
				} else {
					Toast.makeText(getContext(), R.string.signout_successful, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
	}
	
	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}
}