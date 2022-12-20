package com.lifedawn.capstoneapp.account;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.appsettings.AppSettingsFragment;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentProfileBinding;
import com.lifedawn.capstoneapp.main._MainTransactionFragment;
import com.lifedawn.capstoneapp.view.account.SignInFragment;

import org.jetbrains.annotations.NotNull;

public class ProfileFragment extends DialogFragment {
	private FragmentProfileBinding binding;
	private AccountViewModel accountViewModel;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentProfileBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		accountViewModel.getSignOutResult().observe(getViewLifecycleOwner(), it -> {
			if (it) {
				onSignOut();
			}
		});

		accountViewModel.getSignInResult().observe(getViewLifecycleOwner(), it -> {
			if (it != null) {
				onSignIn();
			}
		});

		//로그아웃(사인아웃)버튼의 기능 설정
		binding.signOutBtn.setOnClickListener(v -> {
			accountViewModel.signOut();
		});

		//로그인(사인인)버튼의 기능 설정
		binding.signInBtn.setOnClickListener(v -> {
			SignInFragment signInFragment = new SignInFragment();
			FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

			fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(_MainTransactionFragment.TAG))
					.add(R.id.fragmentContainerView, signInFragment, SignInFragment.TAG).setPrimaryNavigationFragment(signInFragment)
					.addToBackStack(SignInFragment.TAG)
					.commit();
		});

		//앱 설정 버튼의 기능 설정
		binding.appSettingsBtn.setOnClickListener(v -> {
			dismiss();
			AppSettingsFragment appSettingsFragment = new AppSettingsFragment();
			FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

			fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(_MainTransactionFragment.TAG))
					.add(R.id.fragmentContainerView, appSettingsFragment, AppSettingsFragment.TAG).setPrimaryNavigationFragment(appSettingsFragment)
					.addToBackStack(AppSettingsFragment.TAG)
					.commit();
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		final Dialog dialog = getDialog();

		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);

		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.9);
		layoutParams.height = (int) (rect.height() * 0.7);

		dialog.getWindow().setAttributes(layoutParams);
	}

	private void onSignIn() {
		final String email = accountViewModel.getSignInResult().getValue().getUser().getEmail();

		binding.profileImg.setVisibility(View.VISIBLE);
		binding.profileName.setText(email);
		binding.email.setText(email);
		binding.signInBtn.setVisibility(View.GONE);
		binding.signOutBtn.setVisibility(View.VISIBLE);
	}

	private void onSignOut() {
		binding.profileImg.setVisibility(View.GONE);
		binding.profileName.setText(R.string.local);
		binding.email.setText(R.string.local);
		binding.signInBtn.setVisibility(View.VISIBLE);
		binding.signOutBtn.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}