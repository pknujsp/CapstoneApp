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

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.interfaces.OnClickFriendItemListener;
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
	private List<FriendDto> friends = new ArrayList<>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments() != null) {
			Bundle bundle = getArguments();
			
			friends = bundle.getParcelableArrayList("friends");
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
						friends.add(friend);
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
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									friends.add(friendDto);
									adapter.notifyDataSetChanged();
								}
							});
						}
					}
				});
				
				addFriendDialogFragment.show(getChildFragmentManager(), AddFriendDialogFragment.class.getName());
			}
		});
		
		binding.friendsList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.friendsList.addItemDecoration(new RecyclerViewItemDecoration(getContext()));
		adapter = new RecyclerViewAdapter();
		adapter.setFriends(friends);
		adapter.setOnClickFriendItemListener(new OnClickFriendItemListener() {
			@Override
			public void onClickedRemove(FriendDto friend, int position) {
				friends.remove(position);
				adapter.notifyDataSetChanged();
			}
			
			@Override
			public void onClickedFriend(FriendDto friend, int position) {
			
			}
		});
		
		binding.friendsList.setAdapter(adapter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		OnClickFriendItemListener onClickFriendItemListener;
		List<FriendDto> friends;
		
		public void setFriends(List<FriendDto> friends) {
			this.friends = friends;
		}
		
		public void addFriend(FriendDto friend) {
			this.friends.add(friend);
		}
		
		public List<FriendDto> getFriends() {
			return friends;
		}
		
		public void setOnClickFriendItemListener(OnClickFriendItemListener onClickFriendItemListener) {
			this.onClickFriendItemListener = onClickFriendItemListener;
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
			return friends.size();
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
				
				binding.removeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickFriendItemListener.onClickedRemove(friends.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});
				
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickFriendItemListener.onClickedFriend(friends.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});
				
			}
		}
	}
}