package com.lifedawn.capstoneapp;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.common.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.ActivityMainBinding;
import com.lifedawn.capstoneapp.intro.IntroFragment;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {
	private ActivityMainBinding binding;
	private AccountViewModel accountViewModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
		accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
		init();
	}
	
	private void getAppKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.e("Hash key", something);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("name not found", e.toString());
		}
	}
	
	private void init() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		final boolean appInit = sharedPreferences.getBoolean(SharedPreferenceConstant.APP_INIT.name(), false);
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		
		if (appInit) {
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