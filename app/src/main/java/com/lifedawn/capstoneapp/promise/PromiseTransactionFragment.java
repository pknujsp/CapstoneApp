package com.lifedawn.capstoneapp.promise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.account.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.common.viewmodel.AccountViewModel;
import com.lifedawn.capstoneapp.common.viewmodel.CalendarViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentPromiseTransactionBinding;
import com.lifedawn.capstoneapp.main.MainTransactionFragment;
import com.lifedawn.capstoneapp.promise.addpromise.AddPromiseFragment;
import com.lifedawn.capstoneapp.promise.fixedpromise.FixedPromiseFragment;
import com.lifedawn.capstoneapp.promise.mypromise.MyPromiseFragment;
import com.lifedawn.capstoneapp.promise.receivedinvitation.ReceivedInvitationFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PromiseTransactionFragment extends Fragment {
	private FragmentPromiseTransactionBinding binding;
	private AccountViewModel accountViewModel;
	private CalendarViewModel calendarViewModel;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	private boolean initializing = true;
	private Integer lastIndexViewPager;
	private ViewPagerAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		accountViewModel = new ViewModelProvider(getActivity()).get(AccountViewModel.class);
		calendarViewModel = new ViewModelProvider(getActivity()).get(CalendarViewModel.class);
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentPromiseTransactionBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.floatingActionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (accountViewModel.getCurrentSignInAccount() == null) {
					Toast.makeText(getContext(), R.string.no_signin_account, Toast.LENGTH_SHORT).show();
					return;
				}
				AddPromiseFragment addPromiseFragment = new AddPromiseFragment();

				getParentFragment().getParentFragmentManager().beginTransaction().hide(
						getParentFragment().getParentFragmentManager().findFragmentByTag(MainTransactionFragment.class.getName())).add(
						R.id.fragmentContainerView, addPromiseFragment, AddPromiseFragment.class.getName()).addToBackStack(
						AddPromiseFragment.class.getName()).commit();
			}
		});

		accountViewModel.getSignInLiveData().observe(getViewLifecycleOwner(), new Observer<GoogleSignInAccount>() {
			@Override
			public void onChanged(GoogleSignInAccount googleSignInAccount) {
				if (!initializing) {
					init();
				}
			}
		});


		init();
		initializing = false;

	}

	private void init() {
		lastIndexViewPager = binding.tabLayout.getSelectedTabPosition() == -1 ? 0 : binding.tabLayout.getSelectedTabPosition();

		adapter = new ViewPagerAdapter(this);
		adapter.addFragment(new FixedPromiseFragment());
		adapter.addFragment(new MyPromiseFragment());
		adapter.addFragment(new ReceivedInvitationFragment());
		binding.viewPager.setAdapter(adapter);
		new TabLayoutMediator(binding.tabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
			@Override
			public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
				switch (position) {
					case 0:
						tab.setText(R.string.fixed_promise);
						break;
					case 1:
						tab.setText(R.string.my_promise);
						break;
					default:
						tab.setText(R.string.received_invitation);
				}
			}
		}).attach();
		binding.tabLayout.selectTab(binding.tabLayout.getTabAt(lastIndexViewPager));
	}

	private static class ViewPagerAdapter extends FragmentStateAdapter {
		List<Fragment> fragmentList = new ArrayList<>();

		public void addFragment(Fragment fragment) {
			fragmentList.add(fragment);
		}

		public ViewPagerAdapter(@NonNull Fragment fragment) {
			super(fragment);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			return fragmentList.get(position);
		}

		@Override
		public int getItemCount() {
			return fragmentList.size();
		}
	}
}