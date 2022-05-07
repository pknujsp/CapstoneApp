package com.lifedawn.capstoneapp.account;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.appsettings.AppSettingsFragment;
import com.lifedawn.capstoneapp.calendar.fragments.SyncCalendarCallback;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.common.repository.AccountRepository;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentProfileBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileFragment extends DialogFragment {
	private final DateTimeFormatter LAST_UPDATE_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd E a hh:mm");
	private FragmentProfileBinding binding;
	private AccountViewModel accountViewModel;
	private CalendarViewModel calendarViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private boolean initializing = true;
	private final SyncCalendarCallback<Boolean> syncCalendarCallback = new SyncCalendarCallback<Boolean>() {
		@Override
		public void onResultSuccessful(Boolean finished) {
			super.onResultSuccessful(finished);
			if (finished) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
							String lastUpdateDateTime = sharedPreferences.getString(SharedPreferenceConstant.LAST_UPDATE_DATETIME.getVal(), "");

							ZonedDateTime time = ZonedDateTime.parse(lastUpdateDateTime);
							binding.lastUpdateDateTime.setText(time.format(LAST_UPDATE_DATETIME_FORMATTER));
							binding.progressCircular.setVisibility(View.GONE);
						}
					});
				}
			}
		}

		@Override
		public void onResultFailed(Exception e) {
			super.onResultFailed(e);
			Toast.makeText(getContext(), R.string.failed_sync_calendar, Toast.LENGTH_SHORT).show();
			binding.progressCircular.setVisibility(View.GONE);
		}

		@Override
		public void onAlreadySyncing() {
			Toast.makeText(getContext(), R.string.already_syncing, Toast.LENGTH_SHORT).show();
			binding.progressCircular.setVisibility(syncing ? View.VISIBLE : View.GONE);
		}

		@Override
		public void onSyncStarted() {
			super.onSyncStarted();
			binding.progressCircular.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
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
		binding.progressCircular.setVisibility(View.GONE);

		//로그아웃(사인아웃)버튼의 기능 설정
		binding.signOutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				accountViewModel.signOut(new AccountRepository.OnSignCallback() {
					@Override
					public void onSignInResult(boolean succeed, GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential, Exception e) {

					}

					@Override
					public void onSignOutResult(boolean succeed, GoogleSignInAccount signOutAccount) {
						onSignOut();
					}
				});
			}
		});

		//로그인(사인인)버튼의 기능 설정
		binding.signInBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				accountViewModel.signIn(googleAccountLifeCycleObserver, new AccountRepository.OnSignCallback() {
					@Override
					public void onSignInResult(boolean succeed, GoogleSignInAccount signInAccount, GoogleAccountCredential googleAccountCredential, Exception e) {
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (succeed) {
										onSignIn(signInAccount);
									} else {
										Toast.makeText(getContext(), R.string.failed_signin_account, Toast.LENGTH_SHORT).show();
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

		//앱 설정 버튼의 기능 설정
		binding.appSettingsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
				AppSettingsFragment appSettingsFragment = new AppSettingsFragment();
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MainTransactionFragment.class.getName()))
						.add(R.id.fragmentContainerView, appSettingsFragment, AppSettingsFragment.class.getName()).addToBackStack(AppSettingsFragment.class.getName())
						.commit();
			}
		});

		binding.updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (accountViewModel.getUsingAccountType() == Constant.ACCOUNT_GOOGLE &&
						accountViewModel.getCurrentSignInAccount() != null) {
					calendarViewModel.syncCalendars(accountViewModel.getCurrentSignInAccount(), syncCalendarCallback);
				} else {
					Toast.makeText(getContext(), R.string.unavailable_update, Toast.LENGTH_SHORT).show();
				}
			}
		});

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String lastUpdateDateTime = sharedPreferences.getString(SharedPreferenceConstant.LAST_UPDATE_DATETIME.getVal(), "");

		if (lastUpdateDateTime.isEmpty()) {
			binding.lastUpdateDateTime.setText(R.string.noData);
		} else {
			ZonedDateTime time = ZonedDateTime.parse(lastUpdateDateTime);
			binding.lastUpdateDateTime.setText(time.format(LAST_UPDATE_DATETIME_FORMATTER));
		}

		if (accountViewModel.getUsingAccountType() == Constant.ACCOUNT_LOCAL_WITHOUT_GOOGLE) {
			onSignOut();
		} else {
			GoogleSignInAccount account = accountViewModel.getCurrentSignInAccount();

			if (account != null) {
				onSignIn(account);
			} else {
				onSignOut();
			}
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		Dialog dialog = getDialog();

		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);

		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.9);
		layoutParams.height = (int) (rect.height() * 0.7);

		dialog.getWindow().setAttributes(layoutParams);
	}

	public void onSignIn(GoogleSignInAccount account) {
		binding.profileImg.setVisibility(View.VISIBLE);
		binding.profileName.setText(account.getDisplayName());
		binding.email.setText(account.getEmail());
		binding.signInBtn.setVisibility(View.GONE);
		binding.signOutBtn.setVisibility(View.VISIBLE);
	}

	public void onSignOut() {
		binding.profileImg.setVisibility(View.GONE);
		binding.profileName.setText(R.string.local);
		binding.email.setText(R.string.local);
		binding.signInBtn.setVisibility(View.VISIBLE);
		binding.signOutBtn.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
		accountViewModel.getSignInLiveData().removeObservers(this);
		accountViewModel.getSignOutLiveData().removeObservers(this);
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}
}