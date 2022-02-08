package com.lifedawn.capstoneapp.common.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.BundleConstant;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.viewmodel.SearchHistoryViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentSearchHistoryBinding;
import com.lifedawn.capstoneapp.databinding.SearchHistoryItemBinding;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;

public class SearchHistoryFragment extends Fragment {
	private FragmentSearchHistoryBinding binding;
	private SearchHistoryDto.SearchHistoryType searchHistoryType;
	private OnClickedHistoryItemListener onClickedHistoryItemListener;
	private HistoryListAdapter adapter;
	private SearchHistoryViewModel viewModel;
	private List<SearchHistoryDto> searchHistoryDtoList;
	private boolean initializing = true;
	
	public void setOnClickedHistoryItemListener(OnClickedHistoryItemListener onClickedHistoryItemListener) {
		this.onClickedHistoryItemListener = onClickedHistoryItemListener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getArguments();
		searchHistoryType = (SearchHistoryDto.SearchHistoryType) bundle.getSerializable(BundleConstant.SEARCH_HISTORY_TYPE.name());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSearchHistoryBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		viewModel = new ViewModelProvider(requireActivity()).get(SearchHistoryViewModel.class);
		viewModel.getAddedLiveData().observe(getViewLifecycleOwner(), new Observer<SearchHistoryDto>() {
			@Override
			public void onChanged(SearchHistoryDto searchHistoryDto) {
				if (!initializing) {
					if (searchHistoryDto.getSearchHistoryType() == SearchHistoryDto.SearchHistoryType.MAP) {
						
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									searchHistoryDtoList.add(searchHistoryDto);
									adapter.notifyDataSetChanged();
								}
							});
						}
					}
				}
			}
		});
		
		adapter = new HistoryListAdapter(new OnClickedHistoryItemListener() {
			@Override
			public void onClickedValue(SearchHistoryDto searchHistoryDto, int position) {
				onClickedHistoryItemListener.onClickedValue(searchHistoryDto, position);
			}
			
			@Override
			public void onClickedRemove(SearchHistoryDto searchHistoryDto, int position) {
				viewModel.delete(searchHistoryDto.getId(), null);
				searchHistoryDtoList.remove(position);
				adapter.notifyItemRemoved(position);
				onClickedHistoryItemListener.onClickedRemove(searchHistoryDto, position);
			}
		});
		
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		
		viewModel.getAll(SearchHistoryDto.SearchHistoryType.MAP.name(), new OnDbQueryCallback<List<SearchHistoryDto>>() {
			@Override
			public void onResult(List<SearchHistoryDto> e) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							searchHistoryDtoList = e;
							adapter.setSearchHistoryDtoList(searchHistoryDtoList);
							binding.recyclerView.setAdapter(adapter);
							initializing = false;
						}
					});
				}
			}
		});
	}
	
	@Override
	public void onDestroyView() {
		viewModel.getAddedLiveData().removeObservers(getViewLifecycleOwner());
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public interface OnClickedHistoryItemListener {
		void onClickedValue(SearchHistoryDto searchHistoryDto, int position);
		
		void onClickedRemove(SearchHistoryDto searchHistoryDto, int position);
	}
	
	private static class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder> {
		private List<SearchHistoryDto> searchHistoryDtoList;
		private OnClickedHistoryItemListener onClickedHistoryItemListener;
		
		
		public HistoryListAdapter(OnClickedHistoryItemListener onClickedHistoryItemListener) {
			this.onClickedHistoryItemListener = onClickedHistoryItemListener;
		}
		
		public void setSearchHistoryDtoList(List<SearchHistoryDto> searchHistoryDtoList) {
			this.searchHistoryDtoList = searchHistoryDtoList;
		}
		
		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_history_item, null));
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.onBind(searchHistoryDtoList.get(position));
		}
		
		@Override
		public int getItemCount() {
			return searchHistoryDtoList.size();
		}
		
		private class ViewHolder extends RecyclerView.ViewHolder {
			private SearchHistoryItemBinding binding;
			
			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = SearchHistoryItemBinding.bind(itemView);
			}
			
			public void onBind(SearchHistoryDto searchHistoryDto) {
				int position = getBindingAdapterPosition();
				
				binding.value.setText(searchHistoryDto.getValue());
				
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedHistoryItemListener.onClickedValue(searchHistoryDto, position);
					}
				});
				binding.removeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedHistoryItemListener.onClickedRemove(searchHistoryDto, position);
					}
				});
			}
		}
	}
}