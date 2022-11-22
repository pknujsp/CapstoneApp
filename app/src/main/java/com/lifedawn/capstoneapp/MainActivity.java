package com.lifedawn.capstoneapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.ActivityMainBinding;
import com.lifedawn.capstoneapp.view.account.SignInFragment;
import com.lifedawn.capstoneapp.view.intro.IntroFragment;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getSupportFragmentManager().popBackStackImmediate()) {
				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
		init();
	}

	private void init() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		final boolean appInit = sharedPreferences.getBoolean(SharedPreferenceConstant.APP_INIT.name(), false);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

		if (appInit) {

			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainerView.getId(), mainTransactionFragment,
							MainTransactionFragment.class.getName()).setPrimaryNavigationFragment(mainTransactionFragment)
					.commit();
		} else {
			//intro
			SignInFragment signInFragment = new SignInFragment();
			fragmentTransaction.add(binding.fragmentContainerView.getId(), signInFragment, SignInFragment.TAG)
					.setPrimaryNavigationFragment(signInFragment).commit();
		}
	}

}