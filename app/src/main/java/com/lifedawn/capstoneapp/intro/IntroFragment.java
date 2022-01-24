package com.lifedawn.capstoneapp.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.capstoneapp.account.util.GoogleAccountLifeCycleObserver;
import com.lifedawn.capstoneapp.account.util.GoogleAccountUtil;
import com.lifedawn.capstoneapp.databinding.FragmentIntroBinding;

import org.jetbrains.annotations.NotNull;

public class IntroFragment extends Fragment {
	private FragmentIntroBinding binding;
	private GoogleAccountUtil googleAccountUtil;
	private GoogleAccountLifeCycleObserver googleAccountLifeCycleObserver;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		googleAccountUtil = GoogleAccountUtil.getInstance(getContext());
		googleAccountLifeCycleObserver = new GoogleAccountLifeCycleObserver(requireActivity().getActivityResultRegistry(),
				requireActivity());
		getLifecycle().addObserver(googleAccountLifeCycleObserver);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentIntroBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				googleAccountUtil.signIn(googleAccountLifeCycleObserver);
			}
		});
		
		binding.startWithoutGoogleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			
			}
		});
		
	}
	
	@Override
	public void onDestroy() {
		getLifecycle().removeObserver(googleAccountLifeCycleObserver);
		super.onDestroy();
	}
	
	
}