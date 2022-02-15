package com.lifedawn.capstoneapp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.navigation.NavigationBarView;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.ProfileFragment;
import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.calendar.fragments.CalendarTransactionFragment;
import com.lifedawn.capstoneapp.calendar.util.GoogleCalendarUtil;
import com.lifedawn.capstoneapp.common.interfaces.OnHttpApiCallback;
import com.lifedawn.capstoneapp.common.view.ProgressDialog;
import com.lifedawn.capstoneapp.common.viewmodel.AccountCalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentMainTransactionBinding;
import com.lifedawn.capstoneapp.friends.FriendTransactionFragment;
import com.lifedawn.capstoneapp.promise.PromiseTransactionFragment;

import org.jetbrains.annotations.NotNull;

public class MainTransactionFragment extends Fragment {
	private FragmentMainTransactionBinding binding;
	private AccountCalendarViewModel accountCalendarViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private boolean initializing = true;
	private AlertDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountCalendarViewModel = new ViewModelProvider(requireActivity()).get(AccountCalendarViewModel.class);
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
		
		accountCalendarViewModel.getSignInLiveData().observe(getViewLifecycleOwner(), new Observer<GoogleSignInAccount>() {
			@Override
			public void onChanged(GoogleSignInAccount account) {
				if (!initializing) {
					if (account == null) {
					} else {
						onSignIn(account);
					}
				}
			}
		});
		
		accountCalendarViewModel.getSignOutLiveData().observe(getViewLifecycleOwner(), new Observer<GoogleSignInAccount>() {
			@Override
			public void onChanged(GoogleSignInAccount account) {
				if (!initializing) {
					if (account == null) {
					
					} else {
						onSignOut();
					}
				}
			}
		});
		dialog = ProgressDialog.showDialog(getActivity());
		
		final GoogleSignInAccount lastSignInAccount = accountCalendarViewModel.lastSignInAccount();
		if (lastSignInAccount == null) {
			//계정이 없거나 로그아웃된 상태
			onSignOut();
		} else {
			//로그인을 이전에 하였던 경우
			GoogleAccountUtil googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
			onSignIn(googleAccountUtil.lastSignInAccount());
		}
		
		
	}
	
	public void onSignIn(GoogleSignInAccount account) {
		binding.simpleProfileView.profileImg.setVisibility(View.VISIBLE);
		binding.simpleProfileView.profileName.setText(account.getDisplayName());
		
		GoogleCalendarUtil googleCalendarUtil = new GoogleCalendarUtil(googleAccountLifeCycleObserver);
		GoogleAccountUtil accountUtil = GoogleAccountUtil.getInstance(getContext());
		accountUtil.setGoogleAccountCredential(account);
		
		googleCalendarUtil.existingPromiseCalendar(googleCalendarUtil.getCalendarService(accountUtil.getGoogleAccountCredential()),
				new OnHttpApiCallback<CalendarListEntry>() {
					@Override
					public void onResultSuccessful(CalendarListEntry existing) {
						if (existing == null) {
							googleCalendarUtil.addPromiseCalendar(
									googleCalendarUtil.getCalendarService(accountUtil.getGoogleAccountCredential()),
									new OnHttpApiCallback<Calendar>() {
										@Override
										public void onResultSuccessful(Calendar e) {
											accountCalendarViewModel.setMainCalendarId(e.getId());
											if(getActivity() != null){
												getActivity().runOnUiThread(new Runnable() {
													@Override
													public void run() {
														dialog.dismiss();
														init();
													}
												});
												
											}
											
										}
										
										@Override
										public void onResultFailed(Exception e) {
										
										}
									});
						} else {
							accountCalendarViewModel.setMainCalendarId(existing.getId());
							
							if(getActivity() != null){
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										dialog.dismiss();
										init();
									}
								});
								
							}
					
							
						}
					}
					
					@Override
					public void onResultFailed(Exception e) {
					
					}
				});
	}
	
	public void onSignOut() {
		binding.simpleProfileView.profileImg.setVisibility(View.GONE);
		binding.simpleProfileView.profileName.setText(R.string.local);
		dialog.dismiss();
		init();
		
	}
	
	private void init() {
		PromiseTransactionFragment promiseTransactionFragment = new PromiseTransactionFragment();
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainerView.getId(), promiseTransactionFragment,
				PromiseTransactionFragment.class.getName()).setPrimaryNavigationFragment(promiseTransactionFragment).commit();
		
		initializing = false;
	}
}