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
import com.lifedawn.capstoneapp.intro.IntroFragment;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private AccountViewModel accountViewModel;

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
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

		init();
	}

	private void init() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		final boolean appInit = sharedPreferences.getBoolean(SharedPreferenceConstant.APP_INIT.name(), false);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

		if (appInit) {
			getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

			//maintransactionfragment
			MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
			fragmentTransaction.add(binding.fragmentContainerView.getId(), mainTransactionFragment,
					MainTransactionFragment.class.getName()).commit();
		} else {
			//intro
			IntroFragment introFragment = new IntroFragment();
			fragmentTransaction.add(binding.fragmentContainerView.getId(), introFragment, IntroFragment.class.getName()).commit();
		}
	}

}