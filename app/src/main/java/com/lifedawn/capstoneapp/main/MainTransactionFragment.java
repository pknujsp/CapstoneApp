package com.lifedawn.capstoneapp.main;

import android.accounts.Account;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationBarView;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.ProfileFragment;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.calendar.fragments.CalendarTransactionFragment;
import com.lifedawn.capstoneapp.common.Constant;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentMainTransactionBinding;
import com.lifedawn.capstoneapp.friends.FriendTransactionFragment;
import com.lifedawn.capstoneapp.promise.PromiseTransactionFragment;

import org.jetbrains.annotations.NotNull;

public class MainTransactionFragment extends Fragment {
	private FragmentMainTransactionBinding binding;
	private AccountViewModel accountViewModel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
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
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				Fragment primaryNavigationFragment = getChildFragmentManager().getPrimaryNavigationFragment();
				Fragment newFragment = null;
				
				if (item.getItemId() == R.id.mainPage) {
					if (primaryNavigationFragment instanceof PromiseTransactionFragment) {
						return false;
					} else {
						PromiseTransactionFragment promiseTransactionFragment = new PromiseTransactionFragment();
						newFragment = promiseTransactionFragment;
					}
				} else if (item.getItemId() == R.id.friendPage) {
					if (primaryNavigationFragment instanceof FriendTransactionFragment) {
						return false;
					} else {
						FriendTransactionFragment friendTransactionFragment = new FriendTransactionFragment();
						newFragment = friendTransactionFragment;
					}
				} else {
					if (primaryNavigationFragment instanceof CalendarTransactionFragment) {
						return false;
					} else {
						CalendarTransactionFragment calendarTransactionFragment = new CalendarTransactionFragment();
						newFragment = calendarTransactionFragment;
					}
				}
				
				if (newFragment != null) {
					getChildFragmentManager().beginTransaction().replace(binding.fragmentContainerView.getId(),
							newFragment).setPrimaryNavigationFragment(newFragment).commit();
					return true;
				}
				return false;
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
		
		final GoogleSignInAccount lastSignInAccount = accountViewModel.lastSignInAccount();
		if (lastSignInAccount == null) {
			//계정이 없거나 로그아웃된 상태
			accountViewModel.setUsingAccountType(Constant.ACCOUNT_LOCAL_WITHOUT_GOOGLE);
			onSignOut();
			init();
		} else {
			//로그인이 되어있는 상태
			GoogleAccountUtil googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
			final Account connectedAccount = googleAccountUtil.getConnectedGoogleAccount();
			
			if (connectedAccount.name.equals(lastSignInAccount.getAccount().name)) {
				//같은 계정 -> 로그인 성공
				accountViewModel.setUsingAccountType(Constant.ACCOUNT_GOOGLE);
				onSignIn(connectedAccount);
				init();
			} else {
				//다른 계정 -> 로그인 실패
			}
		}
		
	}
	
	public void onSignIn(Account account) {
		binding.simpleProfileView.profileImg.setVisibility(View.VISIBLE);
		binding.simpleProfileView.profileName.setText(account.name);
	}
	
	public void onSignOut() {
		binding.simpleProfileView.profileImg.setVisibility(View.GONE);
		binding.simpleProfileView.profileName.setText(R.string.local);
	}
	
	private void init() {
		PromiseTransactionFragment promiseTransactionFragment = new PromiseTransactionFragment();
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainerView.getId(), promiseTransactionFragment,
				PromiseTransactionFragment.class.getName()).setPrimaryNavigationFragment(promiseTransactionFragment).commit();
	}
}