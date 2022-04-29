package com.lifedawn.capstoneapp.map.findroute;

import android.app.Dialog;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.util.FusedLocation;
import com.lifedawn.capstoneapp.databinding.FragmentFindRouteBinding;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.restapi.RequestDirections;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.naver.Directions.DirectionsResponse;
import com.lifedawn.capstoneapp.weather.DataProviderType;

public class FindRouteFragment extends DialogFragment {
	private FragmentFindRouteBinding binding;
	private LocationDto startLocationDto;
	private LocationDto goalLocationDto;
	private Bundle bundle;
	private MultipleRestApiDownloader multipleRestApiDownloader;
	private FusedLocation fusedLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bundle = getArguments();
		} else {
			bundle = savedInstanceState;
		}
		goalLocationDto = (LocationDto) bundle.getSerializable("goalLocationDto");
		fusedLocation = FusedLocation.getInstance(getContext());

		multipleRestApiDownloader = new MultipleRestApiDownloader() {
			@Override
			public void onResult() {
				ArrayMap<RetrofitClient.ServiceType, ResponseResult> arrayMap =
						getResponseMap().get(DataProviderType.NAVER_DIRECTIONS);

				if (arrayMap.get(RetrofitClient.ServiceType.NAVER_DIRECTIONS).isSuccessful()) {
					DirectionsResponse response = (DirectionsResponse) arrayMap.get(RetrofitClient.ServiceType.NAVER_DIRECTIONS).getResponseObj();
				}
			}

			@Override
			public void onCanceled() {

			}
		};

	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentFindRouteBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		Dialog dialog = getDialog();

		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);

		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.9);
		layoutParams.height = (int) (rect.height() * 0.8);

		dialog.getWindow().setAttributes(layoutParams);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Toast.makeText(getContext(), R.string.finding_current_location, Toast.LENGTH_SHORT).show();
		fusedLocation.startLocationUpdates(new FusedLocation.MyLocationCallback() {
			@Override
			public void onSuccessful(LocationResult locationResult) {
				Location currentLocation = locationResult.getLocations().get(0);
				startLocationDto = new LocationDto();
				startLocationDto.setLatitude(String.valueOf(currentLocation.getLatitude()));
				startLocationDto.setLongitude(String.valueOf(currentLocation.getLongitude()));

				final RequestDirections.Point startPoint = new RequestDirections.Point(
						Float.parseFloat(startLocationDto.getLatitude()), Float.parseFloat(startLocationDto.getLongitude())
				);
				final RequestDirections.Point goalPoint = new RequestDirections.Point(
						Float.parseFloat(goalLocationDto.getLatitude()), Float.parseFloat(goalLocationDto.getLongitude())
				);

				RequestDirections.requestDirections(startPoint, goalPoint, multipleRestApiDownloader);
			}

			@Override
			public void onFailed(Fail fail) {
				Toast.makeText(getContext(), R.string.failed_find_current_location, Toast.LENGTH_SHORT).show();
			}
		}, false);


	}
}