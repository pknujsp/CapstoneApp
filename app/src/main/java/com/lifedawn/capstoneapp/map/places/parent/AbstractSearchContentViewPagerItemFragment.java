package com.lifedawn.capstoneapp.map.places.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.databinding.FragmentAbstractContentResultBinding;
import com.lifedawn.capstoneapp.kakao.search.LocalParameterUtil;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.map.interfaces.OnPoiItemClickListener;
import com.lifedawn.capstoneapp.map.places.content.ContentViewPagerAdapter;
import com.lifedawn.capstoneapp.map.places.interfaces.IConnectHeader;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;

public abstract class AbstractSearchContentViewPagerItemFragment extends Fragment implements OnExtraListDataListener<Constant> {
	protected FragmentAbstractContentResultBinding binding;
	protected String query;

	protected MarkerOnClickListener markerOnClickListener;
	protected OnPoiItemClickListener onPoiItemClickListener;

	protected MapViewModel mapViewModel;
	protected Double latitude;
	protected Double longitude;
	protected IMap iMap;

	protected IConnectHeader iConnectHeader;
	protected Bundle bundle;
	protected SearchResultAdapter adapter;

	public AbstractSearchContentViewPagerItemFragment(MarkerOnClickListener markerOnClickListener, OnPoiItemClickListener onPoiItemClickListener, IConnectHeader iConnectHeader) {
		this.iConnectHeader = iConnectHeader;
		this.markerOnClickListener = markerOnClickListener;
		this.onPoiItemClickListener = onPoiItemClickListener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		query = bundle.getString("query");
		latitude = iConnectHeader.getLatitude();
		longitude = iConnectHeader.getLongitude();

		mapViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
		iMap = mapViewModel.getiMapData();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentAbstractContentResultBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.progressLayout.setContentView(binding.recyclerView);
		binding.progressLayout.onStarted(null);

		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
	}

	public void clearResponses() {
		binding.recyclerView.setAdapter(null);
		adapter = null;
	}

	public void requestQuery() {
		setAdapter(LocalParameterUtil.getPlaceParameter(query, latitude.toString(), longitude.toString(),
				LocalApiPlaceParameter.DEFAULT_SIZE, LocalApiPlaceParameter.DEFAULT_PAGE, iConnectHeader.getSearchSortCriteria(), String.valueOf(MyApplication.MAP_SEARCH_RANGE * 1000)));
	}

	public abstract void setAdapter(LocalApiPlaceParameter parameter);

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

}