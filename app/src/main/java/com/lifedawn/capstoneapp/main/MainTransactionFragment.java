package com.lifedawn.capstoneapp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationBarView;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.ProfileFragment;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.calendar.fragments.CalendarTransactionFragment;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentMainTransactionBinding;
import com.lifedawn.capstoneapp.friends.FriendTransactionFragment;
import com.lifedawn.capstoneapp.promise.PromiseTransactionFragment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MainTransactionFragment extends Fragment {
	private FragmentMainTransactionBinding binding;
	private AccountViewModel accountViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private boolean initializing = true;

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
		binding = FragmentMainTransactionBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);


		binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
			private Map<String, Fragment> fragmentMap = new HashMap<>();

			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				Fragment primaryNavigationFragment = getChildFragmentManager().getPrimaryNavigationFragment();
				Fragment newFragment = null;
				boolean newCreate = false;

				if (item.getItemId() == R.id.mainPage) {
					if (primaryNavigationFragment instanceof PromiseTransactionFragment) {
						return false;
					} else {
						if (!fragmentMap.containsKey(PromiseTransactionFragment.class.getName())) {
							fragmentMap.put(PromiseTransactionFragment.class.getName(), new PromiseTransactionFragment());
							newCreate = true;
						}
						newFragment = fragmentMap.get(PromiseTransactionFragment.class.getName());
					}
				} else if (item.getItemId() == R.id.friendPage) {
					if (primaryNavigationFragment instanceof FriendTransactionFragment) {
						return false;
					} else {
						if (!fragmentMap.containsKey(FriendTransactionFragment.class.getName())) {
							fragmentMap.put(FriendTransactionFragment.class.getName(), new FriendTransactionFragment());
							newCreate = true;
						}
						newFragment = fragmentMap.get(FriendTransactionFragment.class.getName());
					}
				} else {
					if (primaryNavigationFragment instanceof CalendarTransactionFragment) {
						return false;
					} else {
						if (!fragmentMap.containsKey(CalendarTransactionFragment.class.getName())) {
							fragmentMap.put(CalendarTransactionFragment.class.getName(), new CalendarTransactionFragment());
							newCreate = true;
						}
						newFragment = fragmentMap.get(CalendarTransactionFragment.class.getName());
					}
				}

				FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
				if (newCreate) {
					fragmentTransaction.hide(primaryNavigationFragment).add(binding.fragmentContainerView.getId(), newFragment,
							newFragment.getClass().getName());
				} else {
					fragmentTransaction.hide(primaryNavigationFragment).show(newFragment);
				}

				fragmentTransaction.setPrimaryNavigationFragment(newFragment).commit();
				return true;
			}
		});

		binding.bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
			@Override
			public void onNavigationItemReselected(@NonNull MenuItem item) {

			}
		});

		binding.simpleProfileView.profileLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ProfileFragment profileFragment = new ProfileFragment();
				profileFragment.show(getChildFragmentManager(), ProfileFragment.class.getName());
			}
		});

		accountViewModel.getSignInLiveData().observe(getViewLifecycleOwner(), new Observer<GoogleSignInAccount>() {
			@Override
			public void onChanged(GoogleSignInAccount account) {
				if (!initializing) {
					onSignIn(account);
				}
			}
		});

		accountViewModel.getSignOutLiveData().observe(getViewLifecycleOwner(), new Observer<GoogleSignInAccount>() {
			@Override
			public void onChanged(GoogleSignInAccount account) {
				if (!initializing) {
					onSignOut();
				}
			}
		});

		PromiseTransactionFragment promiseTransactionFragment = new PromiseTransactionFragment();
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainerView.getId(), promiseTransactionFragment,
				PromiseTransactionFragment.class.getName()).setPrimaryNavigationFragment(promiseTransactionFragment).commit();


		final GoogleSignInAccount lastSignInAccount = accountViewModel.lastSignInAccount();
		if (lastSignInAccount == null) {
			//계정이 없거나 로그아웃된 상태
			onSignOut();
		} else {
			//로그인을 이전에 하였던 경우
			onSignIn(lastSignInAccount);
		}

		initializing = false;
	}

	private void onSignIn(GoogleSignInAccount account) {
		binding.simpleProfileView.profileImg.setVisibility(View.VISIBLE);
		binding.simpleProfileView.profileName.setText(account.getDisplayName());
	}

	private void onSignOut() {
		binding.simpleProfileView.profileImg.setVisibility(View.GONE);
		binding.simpleProfileView.profileName.setText(R.string.local);
	}


}