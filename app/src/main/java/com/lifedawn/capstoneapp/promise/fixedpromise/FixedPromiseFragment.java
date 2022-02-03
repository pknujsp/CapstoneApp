package com.lifedawn.capstoneapp.promise.fixedpromise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.calendar.model.Event;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.interfaces.OnClickPromiseItemListener;
import com.lifedawn.capstoneapp.databinding.FragmentFixedPromiseBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewPromiseBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FixedPromiseFragment extends Fragment {
	private FragmentFixedPromiseBinding binding;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFixedPromiseBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.floatingActionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			
			}
		});
		
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.recyclerView.addItemDecoration(new RecyclerViewItemDecoration(getContext()));
		
		RecyclerViewAdapter adapter = new RecyclerViewAdapter();
		adapter.setOnClickPromiseItemListener(new OnClickPromiseItemListener() {
			@Override
			public void onClickedEdit(Event event, int position) {
			
			}
			
			@Override
			public void onClickedEvent(Event event, int position) {
			
			}
		});
		binding.recyclerView.setAdapter(adapter);
	}
	
	private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		List<Event> events = new ArrayList<>();
		OnClickPromiseItemListener onClickPromiseItemListener;
		
		public void setOnClickPromiseItemListener(OnClickPromiseItemListener onClickPromiseItemListener) {
			this.onClickPromiseItemListener = onClickPromiseItemListener;
		}
		
		public void setEvents(List<Event> events) {
			this.events = events;
		}
		
		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_promise, null));
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			holder.onBind();
		}
		
		@Override
		public void onViewRecycled(@NonNull ViewHolder holder) {
			holder.clear();
			super.onViewRecycled(holder);
		}
		
		@Override
		public int getItemCount() {
			return events.size();
		}
		
		private class ViewHolder extends RecyclerView.ViewHolder {
			private ItemViewPromiseBinding binding;
			
			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = ItemViewPromiseBinding.bind(itemView);
			}
			
			public void clear() {
			
			}
			
			public void onBind() {
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedEvent(events.get(getAdapterPosition()), getAdapterPosition());
					}
				});
				
				binding.editBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickPromiseItemListener.onClickedEdit(events.get(getAdapterPosition()), getAdapterPosition());
					}
				});
			}
		}
	}
}