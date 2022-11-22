package com.lifedawn.capstoneapp.intro;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.repositoryinterface.AccountRepository;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentIntroBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;

import org.jetbrains.annotations.NotNull;

public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private AccountViewModel accountViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.signInGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				accountViewModel.signIn(googleAccountLifeCycleObserver, new AccountRepository.OnSignCallback() {
					@Override
					public void onSignInResult(boolean succeed, GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential, Exception e) {
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (succeed) {
										startMainFragment();
									} else {
										Toast.makeText(getContext(), new String(getString(R.string.failed_signin_account) + "\n" + e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
									}
								}
							});
						}

					}

					@Override
					public void onSignOutResult(boolean succeed, GoogleSignInAccount signOutAccount) {

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
		sharedPreferences.edit().putBoolean(SharedPreferenceConstant.APP_INIT.name(), true).apply();

		MainTransactionFragment mainTransactionFragment = new MainTransactionFragment();
		FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragmentContainerView, mainTransactionFragment, MainTransactionFragment.class.getName()).commitAllowingStateLoss();
	}
}