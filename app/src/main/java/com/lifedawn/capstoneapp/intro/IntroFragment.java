package com.lifedawn.capstoneapp.intro;

import android.accounts.Account;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.viewmodel.AccountCalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentIntroBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;

import org.jetbrains.annotations.NotNull;

public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private GoogleAccountUtil googleAccountUtil;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private AccountCalendarViewModel accountCalendarViewModel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountCalendarViewModel = new ViewModelProvider(requireActivity()).get(AccountCalendarViewModel.class);
		googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				accountCalendarViewModel.signIn(googleAccountLifeCycleObserver, new GoogleAccountUtil.OnSignCallback() {
					@Override
					public void onSignInSuccessful(Account signInAccount, GoogleAccountCredential googleAccountCredential) {
						startMainFragment();
					}
					
					@Override
					public void onSignOutSuccessful(Account signOutAccount) {
					
					}
				});
			}
		});
		
		binding.startWithoutGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startMainFragment();
			}
		});
		
	}
	
	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}
	
	private void startMainFragment() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		sharedPreferences.edit().putBoolean(SharedPreferenceConstant.APP_INIT.name(), true).commit();
		
		MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
		FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragmentContainerView, mainTransactionFragment, MainTransactionFragment.class.getName()).commit();
	}
}