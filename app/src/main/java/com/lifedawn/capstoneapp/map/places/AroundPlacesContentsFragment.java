package com.lifedawn.capstoneapp.map.places;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
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
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;


public class AroundPlacesContentsFragment extends Fragment implements OnExtraListDataListener<Constant>, IConnectContents {
	private FragmentAroundPlacesBinding binding;
	private ViewPagerAdapter viewPagerAdapter;
	private IMap iMap;
	private MapViewModel mapViewModel;
	public static LatLng mapCenterPoint;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
		iMap = mapViewModel.getiMapData();

		loadMapCenterPoint();
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
		//받아온 데이터를 표시해주는 용도로만 사용
	}

	private void loadMapCenterPoint() {
		mapCenterPoint = mapViewModel.getMapCenterPoint();
	}

	@Override
	public void loadExtraListData(Constant e, RecyclerView.AdapterDataObserver adapterDataObserver) {

	}

	@Override
	public void loadExtraListData(RecyclerView.AdapterDataObserver adapterDataObserver) {

	}

	@Override
	public void loadPlaces(int tabPosition) {
		loadMapCenterPoint();
		iMap.removeMarkers(MarkerType.AROUND_PLACE);

		for (PlaceFragment fragment : viewPagerAdapter.fragmentList) {
			if (fragment.adapter != null) {
				fragment.clearResponses();
			}
		}
		viewPagerAdapter.getPlaceFragment(tabPosition).requestPlaces();
	}

	@Override
	public void loadExtraData(int tabPosition, RecyclerView.AdapterDataObserver adapterDataObserver) {
		viewPagerAdapter.getPlaceFragment(tabPosition).loadExtraListData(adapterDataObserver);
	}

	@Override
	public void setViewPager(List<PlaceFragment> fragmentList) {
		viewPagerAdapter = new ViewPagerAdapter(AroundPlacesContentsFragment.this);
		viewPagerAdapter.setFragmentList(fragmentList);

		binding.viewPager.setAdapter(viewPagerAdapter);
		binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			int lastPosition;
			boolean initializing = true;
			PlaceFragment fragment;

			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				fragment = viewPagerAdapter.getPlaceFragment(position);

				if (!initializing) {
					if (fragment.adapter != null) {
						iMap.removeMarkers(MarkerType.AROUND_PLACE);
						iMap.createMarkers(fragment.adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, fragment.markerOnClickListener);
						iMap.showMarkers(MarkerType.AROUND_PLACE);
					} else {
						fragment.getLifecycle().addObserver(new DefaultLifecycleObserver() {
							@Override
							public void onStart(@NonNull LifecycleOwner owner) {
								DefaultLifecycleObserver.super.onStart(owner);
								fragment.requestPlaces();
							}
						});
					}
				} else {
					fragment.getLifecycle().addObserver(new DefaultLifecycleObserver() {
						@Override
						public void onStart(@NonNull LifecycleOwner owner) {
							DefaultLifecycleObserver.super.onStart(owner);
							fragment.requestPlaces();
						}
					});
				}

				initializing = false;
				lastPosition = position;
			}
		});
	}

	@Override
	public ViewPager2 getViewPager2() {
		return binding.viewPager;
	}

	public static class PlaceFragment extends Fragment implements OnExtraListDataListener<Constant> {
		private FragmentAroundPlaceListBinding binding;
		private String category;

		private MarkerOnClickListener markerOnClickListener;
		private OnPoiItemClickListener onPoiItemClickListener;

		private PlacesViewModel placesViewModel;
		private MapViewModel mapViewModel;
		private PlacesAdapter adapter;
		private LocationDto promiseLocationDto;
		private IMap iMap;

		private IConnectHeader iConnectHeader;

		public PlaceFragment(MarkerOnClickListener markerOnClickListener, OnPoiItemClickListener onPoiItemClickListener, IConnectHeader iConnectHeader) {
			this.iConnectHeader = iConnectHeader;
			this.markerOnClickListener = markerOnClickListener;
			this.onPoiItemClickListener = onPoiItemClickListener;
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Bundle bundle = getArguments();
			category = bundle.getString("category");
			promiseLocationDto = (LocationDto) bundle.getSerializable("locationDto");

			mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
			iMap = mapViewModel.getiMapData();
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
			placesViewModel = new ViewModelProvider(this).get(PlacesViewModel.class);

			binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
			binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		}


		public void clearResponses() {
			binding.recyclerView.setAdapter(null);
			adapter = null;
		}

		public void requestPlaces() {
			String latitude = null;
			String longitude = null;

			if (iConnectHeader.getSearchMapPointCriteria() == LocalApiPlaceParameter.SEARCH_CRITERIA_MAP_POINT_CURRENT_LOCATION) {
				latitude = promiseLocationDto.getLatitude();
				longitude = promiseLocationDto.getLongitude();
			} else {
				latitude = String.valueOf(AroundPlacesContentsFragment.mapCenterPoint.latitude);
				longitude = String.valueOf(AroundPlacesContentsFragment.mapCenterPoint.longitude);
			}

			LocalApiPlaceParameter parameter = LocalParameterUtil.getPlaceParameter(category, latitude, longitude,
					LocalApiPlaceParameter.DEFAULT_SIZE, LocalApiPlaceParameter.DEFAULT_PAGE, iConnectHeader.getSearchSortCriteria());

			adapter = new PlacesAdapter(getContext(), new OnClickedListItemListener<PlaceResponse.Documents>() {
				@Override
				public void onClicked(PlaceResponse.Documents e) {
					onPoiItemClickListener.onPOIItemSelectedByList(e, MarkerType.AROUND_PLACE, null);
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
						iMap.addExtraMarkers(adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, markerOnClickListener);
					} else {
						if (itemCount > 0) {
							iMap.createMarkers(adapter.getCurrentList().snapshot(), MarkerType.AROUND_PLACE, markerOnClickListener);
							iMap.showMarkers(MarkerType.AROUND_PLACE);
						}
					}
				}
			});
			binding.recyclerView.setAdapter(adapter);

			placesViewModel.init(parameter, new PagedList.BoundaryCallback<PlaceResponse.Documents>() {
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
			placesViewModel.getPagedListMutableLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<PlaceResponse.Documents>>() {
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
		private List<PlaceFragment> fragmentList = new ArrayList<>();

		public void setFragmentList(List<PlaceFragment> fragmentList) {
			this.fragmentList = fragmentList;
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