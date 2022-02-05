package com.lifedawn.capstoneapp.promise.addpromise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.promise.abstractfragment.AbstractPromiseFragment;

import java.time.ZonedDateTime;

public class AddPromiseFragment extends AbstractPromiseFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ZonedDateTime now = ZonedDateTime.now();
		
		binding.dateTime.setText(now.format(START_DATETIME_FORMATTER));
		
		setAccount(accountViewModel.getUsingAccountType(), accountViewModel.getConnectedGoogleAccount());
		
	}
	
	
}
