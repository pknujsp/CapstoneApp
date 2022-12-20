package com.lifedawn.capstoneapp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationBarView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.firebase.auth.AuthResult;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.ProfileFragment;
import com.lifedawn.capstoneapp.calendar.fragments.CalendarTransactionFragment;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.AccountRepository;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentMainTransactionBinding;
import com.lifedawn.capstoneapp.friends.FriendTransactionFragment;
import com.lifedawn.capstoneapp.promise.PromiseMainFragment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class _MainTransactionFragment extends Fragment {
	private FragmentMainTransactionBinding binding;
	private AccountViewModel accountViewModel;
	private boolean initializing = true;

	public static final String TAG = "MainTransactionFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentMainTransactionBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
			private final Map<String, Fragment> fragmentMap = new HashMap<>();

			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				Fragment primaryNavigationFragment = getChildFragmentManager().getPrimaryNavigationFragment();
				Fragment newFragment = null;
				boolean newCreate = false;

				if (item.getItemId() == R.id.mainPage) {
					if (primaryNavigationFragment instanceof PromiseMainFragment) {
						return false;
					} else {
						if (!fragmentMap.containsKey(PromiseMainFragment.class.getName())) {
							fragmentMap.put(PromiseMainFragment.class.getName(), new PromiseMainFragment());
							newCreate = true;
						}
						newFragment = fragmentMap.get(PromiseMainFragment.class.getName());
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

		accountViewModel.getSignOutResult().observe(getViewLifecycleOwner(), (it) -> {
			if (it) {
				onSignOut();
			}
		});

		accountViewModel.getSignInResult().observe(getViewLifecycleOwner(), (it) -> {
			if (it != null) {
				onSignIn();
			}
		});

		PromiseMainFragment promiseMainFragment = new PromiseMainFragment();
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainerView.getId(), promiseMainFragment,
				PromiseMainFragment.class.getName()).setPrimaryNavigationFragment(promiseMainFragment).commit();

		initializing = false;
	}

	private void onSignIn() {
		final AuthResult authResult = accountViewModel.getSignInResult().getValue();
		if (authResult != null) {
			binding.simpleProfileView.profileName.setText(authResult.getUser().getEmail());
		}
	}

	private void onSignOut() {
		binding.simpleProfileView.profileName.setText(R.string.local);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}