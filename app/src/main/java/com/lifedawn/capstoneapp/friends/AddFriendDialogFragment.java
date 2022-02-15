package com.lifedawn.capstoneapp.friends;

import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.viewmodel.FriendViewModel;
import com.lifedawn.capstoneapp.databinding.FragmentFindFriendBinding;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

public class AddFriendDialogFragment extends DialogFragment {
	private FragmentFindFriendBinding binding;
	private FriendViewModel friendViewModel;
	private OnInsertedNewFriendCallback onInsertedNewFriendCallback;
	
	public void setOnInsertedNewFriendCallback(OnInsertedNewFriendCallback onInsertedNewFriendCallback) {
		this.onInsertedNewFriendCallback = onInsertedNewFriendCallback;
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentFindFriendBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
		binding.addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = binding.emailEditText.getText().toString();
				final String name = binding.nameEditText.getText().toString();
				//이메일 형식에 맞는지 확인
				boolean successful = true;
				
				if (successful) {
					friendViewModel.contains(email, new OnDbQueryCallback<Boolean>() {
						@Override
						public void onResult(Boolean e) {
							if (!e) {
								FriendDto friendDto = new FriendDto();
								friendDto.setEmail(email);
								friendDto.setName(name);
								friendViewModel.insert(friendDto, new OnDbQueryCallback<FriendDto>() {
									@Override
									public void onResult(FriendDto e) {
										if(getActivity() != null){
											getActivity().runOnUiThread(new Runnable() {
												@Override
												public void run() {
													dismiss();
													onInsertedNewFriendCallback.onInserted(friendDto);
												}
											});
										}
										
									}
								});
							} else {
								//중복
								if (getActivity() != null) {
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(getContext(), R.string.existing_Value, Toast.LENGTH_SHORT).show();
										}
									});
								}
							}
						}
					});
					
					
				} else {
				
				}
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Dialog dialog = getDialog();
		
		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);
		
		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.9);
		
		dialog.getWindow().setAttributes(layoutParams);
	}
	
	public interface OnInsertedNewFriendCallback {
		void onInserted(FriendDto friendDto);
	}
}
