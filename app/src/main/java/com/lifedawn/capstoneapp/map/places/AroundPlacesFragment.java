package com.lifedawn.capstoneapp.map.places;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.CustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.databinding.FragmentAroundPlaceListBinding;
import com.lifedawn.capstoneapp.databinding.FragmentAroundPlacesBinding;
import com.lifedawn.capstoneapp.databinding.PlaceRecyclerViewItemBinding;
import com.lifedawn.capstoneapp.kakao.search.LocalParameterUtil;
import com.lifedawn.capstoneapp.kakao.search.callback.PlaceItemCallback;
import com.lifedawn.capstoneapp.kakao.search.util.MapUtil;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.PlacesViewModel;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.BottomSheetController;
import com.lifedawn.capstoneapp.map.interfaces.IMapData;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AroundPlacesFragment extends Fragment implements OnExtraListDataListener<Constant> {
	private FragmentAroundPlacesBinding binding;
	private CustomPlaceCategoryRepository customPlaceCategoryRepository;
	private ViewPagerAdapter viewPagerAdapter;
	private LocationDto promiseLocationDto;
	private IMapData iMapData;
	private MapViewModel mapViewModel;
	private BottomSheetController bottomSheetController;
	private MarkerOnClickListener markerOnClickListener;

	private static int currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
	private static int currentSearchSortTypeCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_SORT_TYPE_ACCURACY;

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (binding.viewPager.getVisibility() == View.GONE) {
				bottomSheetController.collapseAllExpandedBottomSheets();
				binding.viewPager.setVisibility(View.VISIBLE);
			} else {
				getParentFragmentManager().popBackStack();
			}
		}
	};

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customPlaceCategoryRepository = new CustomPlaceCategoryRepository(getContext());
		mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
		iMapData = mapViewModel.getiMapData();
		bottomSheetController = mapViewModel.getBottomSheetController();
		markerOnClickListener = mapViewModel.getMarkerOnClickListener();

		Bundle bundle = getArguments();
		if (bundle != null && bundle.containsKey("locationDto")) {
			promiseLocationDto = (LocationDto) getArguments().getSerializable("locationDto");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentAroundPlacesBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.header.fragmentTitle.setText(R.string.around_place);
		binding.header.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});

		if (promiseLocationDto == null) {
			binding.searchAroundPromiseLocation.setVisibility(View.GONE);
		}

		init();
	}

	@Override
	public void onDestroy() {
		onBackPressedCallback.remove();
		super.onDestroy();
	}

	private void search() {

	}

	private void init() {
		customPlaceCategoryRepository.getAll(new OnDbQueryCallback<List<CustomPlaceCategoryDto>>() {
			@Override
			public void onResult(List<CustomPlaceCategoryDto> customPlaceCategoryDtoList) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						viewPagerAdapter = new ViewPagerAdapter(AroundPlacesFragment.this);
						List<String> placeCategoryList = new ArrayList<>();
						String[] placeCategoryArr = getResources().getStringArray(R.array.KakaoLocationitems);
						placeCategoryList.addAll(Arrays.asList(placeCategoryArr));

						for (CustomPlaceCategoryDto dto : customPlaceCategoryDtoList) {
							placeCategoryList.add(dto.getName());
						}
						Bundle bundle = null;
						PlaceFragment placeFragment = null;

						for (String name : placeCategoryList) {
							TabLayout.Tab tab = binding.categoryTabLayout.newTab();
							tab.setContentDescription(name);
							tab.setText(name);

							binding.categoryTabLayout.addTab(tab);

							bundle = new Bundle();
							bundle.putString("category", name);
							bundle.putSerializable("locationDto", promiseLocationDto);

							placeFragment = new PlaceFragment(new MarkerOnClickListener() {
								@Override
								public void onPOIItemSelectedByList(KakaoLocalDocument kakaoLocalDocument, MarkerType markerType, ClickCallback clickCallback) {
									binding.viewPager.setVisibility(View.GONE);
									markerOnClickListener.onPOIItemSelectedByList(kakaoLocalDocument, markerType, new ClickCallback() {
										@Override
										public void onClicked() {
											binding.viewPager.setVisibility(View.GONE);
										}
									});
								}

								@Override
								public void onPOIItemSelectedByBottomSheet(int position, MarkerType markerType, ClickCallback clickCallback) {
									markerOnClickListener.onPOIItemSelectedByBottomSheet(position, markerType, clickCallback);
								}
							});
							placeFragment.setArguments(bundle);
							viewPagerAdapter.addFragment(placeFragment);
						}

						binding.viewPager.setAdapter(viewPagerAdapter);

						new TabLayoutMediator(binding.categoryTabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
							@Override
							public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
								tab.setText(placeCategoryList.get(position));
							}
						}).attach();

						binding.searchCriteriaToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
							@Override
							public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
								if (isChecked) {
									switch (checkedId) {
										case R.id.search_around_promise_location:
											currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION;
											break;
										case R.id.search_around_map_center:
											currentSearchMapPointCriteria = LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_MAP_CENTER;
											break;
									}
									search();
								}
							}
						});
						binding.searchCriteriaToggleGroup.check(R.id.search_around_promise_location);
					}
				});
			}
		});

	}

	@Override
	public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {

	}

	@Override
	public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {
		int position = binding.categoryTabLayout.getSelectedTabPosition();
		PlaceFragment fragment = viewPagerAdapter.getPlaceFragment(position);

		fragment.loadExtraListData(adapterDataObserver);
	}

	public static class PlaceFragment extends Fragment implements OnExtraListDataListener<Constant> {
		private FragmentAroundPlaceListBinding binding;
		private String category;

		private MarkerOnClickListener markerOnClickListener;

		private PlacesViewModel viewModel;
		private MapViewModel mapViewModel;
		private PlacesAdapter adapter;
		private LocationDto promiseLocationDto;
		private IMapData iMapData;

		public PlaceFragment(MarkerOnClickListener markerOnClickListener) {
			this.markerOnClickListener = markerOnClickListener;
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle bundle = getArguments();
			category = bundle.getString("category");
			promiseLocationDto = (LocationDto) bundle.getSerializable("locationDto");

			mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
			iMapData = mapViewModel.getiMapData();
		}

		@Nullable
		@Override
		public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			binding = FragmentAroundPlaceListBinding.inflate(inflater);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			binding.recyclerView.setVisibility(View.GONE);
			//장소 로드

			viewModel = new ViewModelProvider(this).get(PlacesViewModel.class);

			binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
			binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

			requestPlaces();
		}


		@Override
		public void onDestroy() {
			super.onDestroy();
		}

		public void requestPlaces() {
			String latitude = null;
			String longitude = null;

			if (currentSearchMapPointCriteria == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION) {
				latitude = promiseLocationDto.getLatitude();
				longitude = promiseLocationDto.getLongitude();
			} else {
				LatLng latLng = mapViewModel.getMapCenterPoint();
				latitude = String.valueOf(latLng.latitude);
				longitude = String.valueOf(latLng.longitude);
			}

			LocalApiPlaceParameter parameter = LocalParameterUtil.getPlaceParameter(category, latitude, longitude,
					LocalApiPlaceParameter.DEFAULT_SIZE, LocalApiPlaceParameter.DEFAULT_PAGE, currentSearchSortTypeCriteria);

			adapter = new PlacesAdapter(getContext(), new OnClickedListItemListener<PlaceResponse.Documents>() {
				@Override
				public void onClicked(PlaceResponse.Documents e) {
					markerOnClickListener.onPOIItemSelectedByList(e, MarkerType.AROUND_PLACE, null);
				}
			});
			adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
				@Override
				public void onItemRangeInserted(int positionStart, int itemCount) {
					super.onItemRangeInserted(positionStart, itemCount);
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							binding.progressLayout.setVisibility(View.GONE);
							binding.recyclerView.setVisibility(View.VISIBLE);
						}
					});

					if (positionStart > 0) {
						iMapData.addExtraMarkers(adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, new MarkerOnClickListener.ClickCallback() {
							@Override
							public void onClicked() {
							}
						});
					} else {
						if (itemCount > 0) {
							iMapData.createMarkers(adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, new MarkerOnClickListener.ClickCallback() {
								@Override
								public void onClicked() {
								}
							});
						}
					}
				}
			});
			binding.recyclerView.setAdapter(adapter);

			viewModel.init(parameter, new PagedList.BoundaryCallback<PlaceResponse.Documents>() {
				@Override
				public void onZeroItemsLoaded() {
					super.onZeroItemsLoaded();
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							binding.progressCircular.setVisibility(View.GONE);
							binding.progressMsg.setText(R.string.noData);
						}
					});
				}
			});
			viewModel.getPagedListMutableLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<PlaceResponse.Documents>>() {
				@Override
				public void onChanged(PagedList<PlaceResponse.Documents> placeDocuments) {
					adapter.submitList(placeDocuments);
				}
			});
		}

		@Override
		public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {

		}

		@Override
		public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {
			adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
				@Override
				public void onItemRangeInserted(int positionStart, int itemCount) {
					super.onItemRangeInserted(positionStart, itemCount);
					adapterDataObserver.onItemRangeInserted(positionStart, itemCount);
					adapter.unregisterAdapterDataObserver(this);
				}
			});
			binding.recyclerView.scrollBy(0, 10000);
		}


		private static class PlacesAdapter extends PagedListAdapter<PlaceResponse.Documents, PlacesAdapter.ItemViewHolder> {
			private Context context;
			private final OnClickedListItemListener<PlaceResponse.Documents> onClickedListItem;

			public PlacesAdapter(Context context, OnClickedListItemListener<PlaceResponse.Documents> onClickedListItem) {
				super(new PlaceItemCallback());
				this.context = context;
				this.onClickedListItem = onClickedListItem;
			}

			private class ItemViewHolder extends RecyclerView.ViewHolder {
				private PlaceRecyclerViewItemBinding binding;

				public ItemViewHolder(View view) {
					super(view);
					binding = PlaceRecyclerViewItemBinding.bind(view);
				}

				public void bind() {
					PlaceResponse.Documents item = getItem(getBindingAdapterPosition());

					binding.placeName.setText(item.getPlaceName());
					binding.placeIndex.setText(String.valueOf(getBindingAdapterPosition() + 1));
					binding.placeCategory.setText(item.getCategoryName());
					binding.placeAddressName.setText(item.getAddressName());
					binding.placeDistance.setText(MapUtil.convertMeterToKm(Double.parseDouble(item.getDistance())));

					itemView.getRootView().setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							onClickedListItem.onClicked(getItem(getBindingAdapterPosition()));
						}
					});
				}
			}

			@NonNull
			@Override
			public PlacesAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				return new PlacesAdapter.ItemViewHolder(
						LayoutInflater.from(parent.getContext()).inflate(R.layout.place_recycler_view_item, parent, false));
			}

			@Override
			public void onBindViewHolder(@NonNull PlacesAdapter.ItemViewHolder holder, int position) {
				holder.bind();
			}

		}
	}

	private static class ViewPagerAdapter extends FragmentStateAdapter {
		List<PlaceFragment> fragmentList = new ArrayList<>();

		public void addFragment(PlaceFragment fragment) {
			fragmentList.add(fragment);
		}

		public PlaceFragment getPlaceFragment(int position) {
			return fragmentList.get(position);
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