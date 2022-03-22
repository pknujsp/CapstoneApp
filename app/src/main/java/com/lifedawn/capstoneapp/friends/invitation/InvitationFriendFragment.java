package com.lifedawn.capstoneapp.friends.invitation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.calendar.model.EventAttendee;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.interfaces.OnClickFriendItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnFragmentCallback;
import com.lifedawn.capstoneapp.databinding.FragmentInvitationFriendBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewFriendBinding;
import com.lifedawn.capstoneapp.friends.AddFriendDialogFragment;
import com.lifedawn.capstoneapp.friends.myfriends.FriendsFragment;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

import java.util.ArrayList;
import java.util.List;

public class InvitationFriendFragment extends Fragment {
	private FragmentInvitationFriendBinding binding;
	private RecyclerViewAdapter adapter;
	private ArrayList<EventAttendee> eventAttendees = new ArrayList<>();
	private OnFragmentCallback<ArrayList<EventAttendee>> onFragmentCallback;
	
	public void setOnFragmentCallback(OnFragmentCallback<ArrayList<EventAttendee>> onFragmentCallback) {
		this.onFragmentCallback = onFragmentCallback;
	}
	
	public void setEventAttendees(ArrayList<EventAttendee> eventAttendees) {
		this.eventAttendees = eventAttendees;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments() != null) {
			Bundle bundle = getArguments();
			
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentInvitationFriendBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.getOnFriendsListBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FriendsFragment friendsFragment = new FriendsFragment();
				friendsFragment.setOnClickFriendItemListener(new OnClickFriendItemListener() {
					@Override
					public void onClickedRemove(FriendDto friend, int position) {
					
					}
					
					@Override
					public void onClickedFriend(FriendDto friend, int position) {
						EventAttendee eventAttendee = new EventAttendee();
						eventAttendee.setOrganizer(false).setEmail(friend.getEmail()).setDisplayName(friend.getName());
						eventAttendees.add(eventAttendee);
						adapter.notifyDataSetChanged();
					}
				});
				
				Bundle bundle = new Bundle();
				bundle.putBoolean("fabVisible", false);
				bundle.putBoolean("backAfterItemClick", true);
				friendsFragment.setArguments(bundle);
				
				FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
				fragmentTransaction.hide(InvitationFriendFragment.this).add(R.id.fragmentContainerView, friendsFragment,
						FriendsFragment.class.getName()).addToBackStack(FriendsFragment.class.getName()).commit();
			}
		});
		
		binding.inviteNewFriendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddFriendDialogFragment addFriendDialogFragment = new AddFriendDialogFragment();
				addFriendDialogFragment.setOnInsertedNewFriendCallback(new AddFriendDialogFragment.OnInsertedNewFriendCallback() {
					@Override
					public void onInserted(FriendDto friendDto) {
						EventAttendee eventAttendee = new EventAttendee();
						eventAttendee.setOrganizer(false).setEmail(friendDto.getEmail()).setDisplayName(friendDto.getName());
						eventAttendees.add(eventAttendee);
						adapter.notifyDataSetChanged();
						
					}
				});
				
				addFriendDialogFragment.show(getChildFragmentManager(), AddFriendDialogFragment.class.getName());
			}
		});
		
		binding.friendsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.friendsList.addItemDecoration(new RecyclerViewItemDecoration(getContext()));
		
		adapter = new RecyclerViewAdapter();
		adapter.setEventAttendeeList(eventAttendees);
		adapter.setOnClickEventAttendeeItemListener(new OnClickEventAttendeeItemListener() {
			@Override
			public void onClickedRemove(EventAttendee eventAttendee, int position) {
				eventAttendees.remove(position);
				adapter.notifyDataSetChanged();
			}
			
			@Override
			public void onClickedFriend(EventAttendee eventAttendee, int position) {
				//친구 정보 다이얼로그로 표시
			}
		});
		
		binding.friendsList.setAdapter(adapter);
	}
	
	@Override
	public void onDestroy() {
		if (onFragmentCallback != null) {
			onFragmentCallback.onResult(eventAttendees);
		}
		super.onDestroy();
	}
	
	private static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private OnClickEventAttendeeItemListener onClickEventAttendeeItemListener;
		private List<EventAttendee> eventAttendeeList;
		
		public void setEventAttendeeList(List<EventAttendee> eventAttendeeList) {
			this.eventAttendeeList = eventAttendeeList;
		}
		
		public void setOnClickEventAttendeeItemListener(OnClickEventAttendeeItemListener onClickEventAttendeeItemListener) {
			this.onClickEventAttendeeItemListener = onClickEventAttendeeItemListener;
		}
		
		@NonNull
		@Override
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			return new RecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_friend, null));
		}
		
		@Override
		public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
			holder.onBind();
		}
		
		@Override
		public void onViewRecycled(@NonNull RecyclerViewAdapter.ViewHolder holder) {
			holder.clear();
			super.onViewRecycled(holder);
		}
		
		@Override
		public int getItemCount() {
			return eventAttendeeList.size();
		}
		
		private class ViewHolder extends RecyclerView.ViewHolder {
			private ItemViewFriendBinding binding;
			
			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				binding = ItemViewFriendBinding.bind(itemView);
			}
			
			public void clear() {
			
			}
			
			public void onBind() {
				binding.name.setText(eventAttendeeList.get(getBindingAdapterPosition()).getDisplayName());
				binding.email.setText(eventAttendeeList.get(getBindingAdapterPosition()).getEmail());

				binding.removeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickEventAttendeeItemListener.onClickedRemove(eventAttendeeList.get(getBindingAdapterPosition()),
								getBindingAdapterPosition());
					}
				});
				
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickEventAttendeeItemListener.onClickedFriend(eventAttendeeList.get(getBindingAdapterPosition()),
								getBindingAdapterPosition());
					}
				});
				
			}
		}
	}
	
	public interface OnClickEventAttendeeItemListener {
		void onClickedRemove(EventAttendee eventAttendee, int position);
		
		void onClickedFriend(EventAttendee eventAttendee, int position);
	}
}