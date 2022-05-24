package com.lifedawn.capstoneapp.map.findroute;

import android.Manifest;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.util.FusedLocation;
import com.lifedawn.capstoneapp.common.util.Geocoding;
import com.lifedawn.capstoneapp.common.util.LocationLifeCycleObserver;
import com.lifedawn.capstoneapp.common.view.ProgressView;
import com.lifedawn.capstoneapp.databinding.FragmentFindRouteBinding;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.restapi.RequestDirections;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.naver.directions5.Root;
import com.lifedawn.capstoneapp.weather.DataProviderType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindRouteFragment extends Fragment {
	private FragmentFindRouteBinding binding;
	private LocationDto startLocationDto;
	private LocationDto goalLocationDto;
	private Bundle bundle;
	private MultipleRestApiDownloader multipleRestApiDownloader;
	private FusedLocation fusedLocation;
	private LocationLifeCycleObserver locationLifeCycleObserver;

	private OnFindRouteListener onFindRouteListener;
	private Location currentLocation;

	private final ProgressView.OnResultListener onResultListener = new ProgressView.OnResultListener() {
		@Override
		public void onSuccessful() {
			binding.updateBtn.setVisibility(View.VISIBLE);
		}

		@Override
		public void onFailed(@Nullable String message) {
			binding.updateBtn.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStarted(@Nullable String message) {
			binding.updateBtn.setVisibility(View.GONE);
		}
	};

	public FindRouteFragment setOnFindRouteListener(OnFindRouteListener onFindRouteListener) {
		this.onFindRouteListener = onFindRouteListener;
		return this;
	}

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

		locationLifeCycleObserver = new LocationLifeCycleObserver(requireActivity().getActivityResultRegistry(), requireActivity());
		getLifecycle().addObserver(locationLifeCycleObserver);

		multipleRestApiDownloader = new MultipleRestApiDownloader() {
			@Override
			public void onResult() {
				ArrayMap<RetrofitClient.ServiceType, ResponseResult> arrayMap =
						getResponseMap().get(DataProviderType.NAVER_DIRECTIONS);

				if (arrayMap.get(RetrofitClient.ServiceType.NAVER_DIRECTIONS).isSuccessful()) {
					final Root response = (Root) arrayMap.get(RetrofitClient.ServiceType.NAVER_DIRECTIONS).getResponseObj();

					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								onFindRouteListener.onResult(currentLocation, response.route.traoptimal.get(0).path, response, startLocationDto
										, goalLocationDto);
							}
						});
					}
				} else {
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								onFindRouteListener.onResult(null, null, null, null, null);
							}
						});
					}
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
	public void onDestroy() {
		multipleRestApiDownloader.cancel();
		super.onDestroy();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.progressLayout.setContentView(binding.contentLayout);
		binding.progressLayout.setOnResultListener(onResultListener);

		binding.updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findRoutes();
			}
		});

		binding.closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});

		findRoutes();
	}

	public void findRoutes() {
		binding.progressLayout.onStarted(getString(R.string.calculating_routes));

		if (!fusedLocation.isOnGps()) {
			fusedLocation.onDisabledGps(getActivity(), locationLifeCycleObserver, new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {

				}
			});
			Toast.makeText(getActivity(), getString(R.string.request_to_make_gps_on), Toast.LENGTH_SHORT).show();
			getParentFragmentManager().popBackStack();
			return;
		}

		if (fusedLocation.checkDefaultPermissions()) {
			fusedLocation.startLocationUpdates(new FusedLocation.MyLocationCallback() {
				@Override
				public void onSuccessful(LocationResult locationResult) {
					currentLocation = locationResult.getLocations().get(0);
					startLocationDto = new LocationDto();
					startLocationDto.setLatitude(String.valueOf(currentLocation.getLatitude()));
					startLocationDto.setLongitude(String.valueOf(currentLocation.getLongitude()));

					final RequestDirections.Point startPoint = new RequestDirections.Point(
							Float.parseFloat(startLocationDto.getLatitude()), Float.parseFloat(startLocationDto.getLongitude())
					);
					final RequestDirections.Point goalPoint = new RequestDirections.Point(
							Float.parseFloat(goalLocationDto.getLatitude()), Float.parseFloat(goalLocationDto.getLongitude())
					);

					Geocoding.geocoding(getContext(), startPoint.latitude.doubleValue(), startPoint.longitude.doubleValue(), new Geocoding.GeocodingCallback() {
						@Override
						public void onGeocodingResult(List<Address> addressList) {
							startLocationDto.setAddressName(addressList.get(0).getAddressLine(0));
							RequestDirections.requestDirections(startPoint, goalPoint, multipleRestApiDownloader);
						}
					});

				}

				@Override
				public void onFailed(Fail fail) {
					binding.progressLayout.onFailed(getString(R.string.failed_find_current_location));
				}
			}, false);


		} else {
			locationLifeCycleObserver.launchPermissionsLauncher(
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
					new ActivityResultCallback<Map<String, Boolean>>() {
						@Override
						public void onActivityResult(Map<String, Boolean> result) {
							if (!result.containsValue(false)) {
								binding.updateBtn.callOnClick();
							} else {
								binding.progressLayout.onFailed(getString(R.string.failed_find_current_location));
							}
						}
					});
		}

	}

	public interface OnFindRouteListener {
		void onResult(Location currentLocation, ArrayList<ArrayList<Double>> path, Root response, LocationDto startLocation, LocationDto goalLocation);
	}
}