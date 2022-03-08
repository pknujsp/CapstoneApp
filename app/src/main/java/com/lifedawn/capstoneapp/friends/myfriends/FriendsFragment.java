package com.lifedawn.capstoneapp.friends.myfriends;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.capstoneapp.MainActivity;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.view.RecyclerViewItemDecoration;
import com.lifedawn.capstoneapp.common.interfaces.OnClickFriendItemListener;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentFriendsBinding;
import com.lifedawn.capstoneapp.databinding.ItemViewFriendBinding;
import com.lifedawn.capstoneapp.friends.AddFriendDialogFragment;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
	private FragmentFriendsBinding binding;
	private FriendViewModel friendViewModel;
	private Boolean fabVisible;
	private Boolean backAfterItemClick;
	private OnClickFriendItemListener onClickFriendItemListener;
	private RecyclerViewAdapter adapter;

	public void setOnClickFriendItemListener(OnClickFriendItemListener onClickFriendItemListener) {
		this.onClickFriendItemListener = onClickFriendItemListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			Bundle bundle = getArguments();
			fabVisible = bundle.getBoolean("fabVisible", false);
			backAfterItemClick = bundle.getBoolean("backAfterItemClick", true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFriendsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);

		if (fabVisible != null && !fabVisible) {
			binding.floatingActionBtn.setVisibility(View.GONE);
		}
		binding.floatingActionBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AddFriendDialogFragment addFriendDialogFragment = new AddFriendDialogFragment();
				addFriendDialogFragment.setOnInsertedNewFriendCallback(new AddFriendDialogFragment.OnInsertedNewFriendCallback() {
					@Override
					public void onInserted(FriendDto friendDto) {
						adapter.friends.add(friendDto);
						adapter.notifyDataSetChanged();
					}
				});
				addFriendDialogFragment.show(getChildFragmentManager(), AddFriendDialogFragment.class.getName());
			}
		});

		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		binding.recyclerView.addItemDecoration(new RecyclerViewItemDecoration(getContext()));

		adapter = new RecyclerViewAdapter();
		adapter.setOnClickFriendItemListener(new OnClickFriendItemListener() {
			@Override
			public void onClickedRemove(FriendDto friend, int position) {
				friendViewModel.delete(friend.getId(), new OnDbQueryCallback<Boolean>() {
					@Override
					public void onResult(Boolean e) {

					}
				});
				adapter.friends.remove(position);
				adapter.notifyItemRemoved(position);
			}

			@Override
			public void onClickedFriend(FriendDto friend, int position) {
				if (backAfterItemClick) {
					onClickFriendItemListener.onClickedFriend(friend, position);
					getParentFragmentManager().popBackStackImmediate();
				} else {
					FriendDto friendDto = adapter.friends.get(position);
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.friend).setMessage(new String(friendDto.getName() + "\n" +
							friendDto.getEmail())).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
						}
					}).create();
					builder.show();
				}
			}
		});
		adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();

				if (adapter.getItemCount() > 0) {
					binding.warningLayout.getRoot().setVisibility(View.GONE);
				} else {
					binding.warningLayout.getRoot().setVisibility(View.VISIBLE);
					binding.warningLayout.warningText.setText(R.string.empty_friends);
					binding.warningLayout.btn.setVisibility(View.GONE);
				}
			}
		});
		binding.recyclerView.setAdapter(adapter);

		friendViewModel.getAll(new OnDbQueryCallback<List<FriendDto>>() {
			@Override
			public void onResult(List<FriendDto> e) {
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							adapter.setFriends(e);
							adapter.notifyDataSetChanged();
						}
					});
				}
			}
		});

	}

	private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		OnClickFriendItemListener onClickFriendItemListener;
		List<FriendDto> friends = new ArrayList<>();

		public void setFriends(List<FriendDto> friends) {
			this.friends = friends;
		}

		public void setOnClickFriendItemListener(OnClickFriendItemListener onClickFriendItemListener) {
			this.onClickFriendItemListener = onClickFriendItemListener;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
				binding.friend.setText(friends.get(getBindingAdapterPosition()).getName());

				binding.removeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickFriendItemListener.onClickedRemove(friends.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});

				binding.friendInfoLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickFriendItemListener.onClickedFriend(friends.get(getBindingAdapterPosition()), getBindingAdapterPosition());
					}
				});

			}
		}
	}
}