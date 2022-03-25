package com.lifedawn.capstoneapp.weather;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.databinding.FragmentWeatherInfoBinding;
import com.lifedawn.capstoneapp.map.LocationDto;


public class WeatherInfoFragment extends DialogFragment {
	private FragmentWeatherInfoBinding binding;
	private LocationDto selectedLocationDtoInEvent;
	private Bundle bundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bundle = getArguments();
		} else {
			bundle = savedInstanceState;
		}

		selectedLocationDtoInEvent = (LocationDto) bundle.getSerializable("locationDto");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherInfoBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}