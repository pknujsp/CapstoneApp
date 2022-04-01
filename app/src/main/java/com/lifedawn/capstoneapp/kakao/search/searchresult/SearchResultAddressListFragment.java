package com.lifedawn.capstoneapp.kakao.search.searchresult;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.common.interfaces.OnClickedListItemListener;
import com.lifedawn.capstoneapp.databinding.AddressRecyclerViewItemBinding;
import com.lifedawn.capstoneapp.databinding.FragmentLocationSearchResultBinding;
import com.lifedawn.capstoneapp.kakao.search.LocalParameterUtil;
import com.lifedawn.capstoneapp.kakao.search.callback.AddressItemCallback;
import com.lifedawn.capstoneapp.kakao.search.viewmodel.AddressViewModel;
import com.lifedawn.capstoneapp.map.MapViewModel;
import com.lifedawn.capstoneapp.map.MarkerType;
import com.lifedawn.capstoneapp.map.interfaces.IMap;
import com.lifedawn.capstoneapp.map.interfaces.MarkerOnClickListener;
import com.lifedawn.capstoneapp.map.interfaces.OnExtraListDataListener;
import com.lifedawn.capstoneapp.retrofits.parameters.LocalApiPlaceParameter;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;

public class SearchResultAddressListFragment extends Fragment implements OnExtraListDataListener<Constant> {
	private FragmentLocationSearchResultBinding binding;
	private final String QUERY;
	private final OnClickedListItemListener<AddressResponse.Documents> addressResponseDocumentsOnClickedListItem;
	
	private AddressViewModel addressViewModel;
	private AddressesAdapter adapter;
	private MapViewModel mapViewModel;
	private IMap iMap;
	
	public SearchResultAddressListFragment(String query,
			OnClickedListItemListener<AddressResponse.Documents> addressResponseDocumentsOnClickedListItem) {
		this.QUERY = query;
		this.addressResponseDocumentsOnClickedListItem = addressResponseDocumentsOnClickedListItem;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mapViewModel = new ViewModelProvider(getActivity()).get(MapViewModel.class);
		iMap = mapViewModel.getiMapData();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentLocationSearchResultBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.mapSearchResultHeader.setVisibility(View.GONE);
		binding.searchResultType.setText(getString(R.string.result_address));
		
		binding.searchResultRecyclerview.setLayoutManager(new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false));
		binding.searchResultRecyclerview.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
		addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
		
		adapter = new AddressesAdapter(getContext(), addressResponseDocumentsOnClickedListItem);
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				
				if (positionStart > 0) {
					iMap.addExtraMarkers(adapter.getCurrentList().snapshot(), MarkerType.SEARCH_RESULT_ADDRESS, new MarkerOnClickListener() {
						@Override
						public void onClickedMarker() {

						}
					});
				} else {
					if (itemCount > 0) {
						iMap.createMarkers(adapter.getCurrentList().snapshot(), MarkerType.SEARCH_RESULT_ADDRESS, new MarkerOnClickListener() {
							@Override
							public void onClickedMarker() {

							}
						});
					}
				}
			}
		});
		binding.searchResultRecyclerview.setAdapter(adapter);
		LocalApiPlaceParameter parameter = LocalParameterUtil.getAddressParameter(QUERY, LocalApiPlaceParameter.DEFAULT_SIZE,
				LocalApiPlaceParameter.DEFAULT_PAGE);
		
		addressViewModel.init(parameter);
		addressViewModel.getPagedListMutableLiveData().observe(getViewLifecycleOwner(),
				new Observer<PagedList<AddressResponse.Documents>>() {
					@Override
					public void onChanged(PagedList<AddressResponse.Documents> addressResponseDocuments) {
						adapter.submitList(addressResponseDocuments);
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
		binding.searchResultRecyclerview.scrollBy(0, 10000);
	}
	
	
	private static class AddressesAdapter extends PagedListAdapter<AddressResponse.Documents, AddressesAdapter.ItemViewHolder> {
		private final android.content.Context context;
		private final OnClickedListItemListener<AddressResponse.Documents> onClickedListItem;
		
		public AddressesAdapter(Context context, OnClickedListItemListener<AddressResponse.Documents> onClickedListItem) {
			super(new AddressItemCallback());
			this.context = context;
			this.onClickedListItem = onClickedListItem;
		}
		
		
		private class ItemViewHolder extends RecyclerView.ViewHolder {
			private AddressRecyclerViewItemBinding binding;
			
			public ItemViewHolder(View view) {
				super(view);
				binding = AddressRecyclerViewItemBinding.bind(view);
			}
			
			public void bind() {
				AddressResponse.Documents item = getItem(getBindingAdapterPosition());
				binding.addressLayout.addressName.setText(item.getAddressName());
				binding.addressLayout.addressIndex.setText(String.valueOf(getBindingAdapterPosition() + 1));
				
				if (item.getAddressResponseRoadAddress() != null) {
					binding.addressLayout.anotherAddressType.setText(context.getString(R.string.road_addr));
					binding.addressLayout.anotherAddressName.setText(item.getAddressResponseRoadAddress().getAddressName());
				} else if (item.getAddressResponseAddress() != null) {
					binding.addressLayout.anotherAddressType.setText(context.getString(R.string.region_addr));
					binding.addressLayout.anotherAddressName.setText(item.getAddressResponseAddress().getAddressName());
				}
				
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
		public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.address_recycler_view_item, parent, false));
		}
		
		@Override
		public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
			holder.bind();
		}
		
		@Override
		public void submitList(@Nullable PagedList<AddressResponse.Documents> pagedList) {
			super.submitList(pagedList);
		}
		
		@Override
		public void submitList(@Nullable PagedList<AddressResponse.Documents> pagedList, @Nullable Runnable commitCallback) {
			super.submitList(pagedList, commitCallback);
		}
	}
}