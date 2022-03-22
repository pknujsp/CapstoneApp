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

import java.util.regex.Pattern;

public class AddFriendDialogFragment extends DialogFragment {
	private FragmentFindFriendBinding binding;
	private FriendViewModel friendViewModel;
	private OnInsertedNewFriendCallback onInsertedNewFriendCallback;
	private static final String EMAIL_PATTERN = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";

	public void setOnInsertedNewFriendCallback(OnInsertedNewFriendCallback onInsertedNewFriendCallback) {
		this.onInsertedNewFriendCallback = onInsertedNewFriendCallback;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);

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

		binding.addBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = binding.emailEditText.getText().toString();
				final String name = binding.nameEditText.getText().toString();

				//이메일 형식/이름 입력 확인
				final boolean emailPass = Pattern.matches(EMAIL_PATTERN, email);
				final boolean namePass = !name.isEmpty();

				if (emailPass && namePass) {
					friendViewModel.contains(email, new OnDbQueryCallback<Boolean>() {
						@Override
						public void onResult(Boolean contain) {
							if (!contain) {
								FriendDto friendDto = new FriendDto();
								friendDto.setEmail(email);
								friendDto.setName(name);
								friendViewModel.insert(friendDto, new OnDbQueryCallback<FriendDto>() {
									@Override
									public void onResult(FriendDto e) {
										if (getActivity() != null) {
											getActivity().runOnUiThread(new Runnable() {
												@Override
												public void run() {
													onInsertedNewFriendCallback.onInserted(friendDto);
													dismiss();
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
					String msg = null;
					if (!emailPass && !namePass) {
						msg = getString(R.string.input_error_email_and_name);
					} else if (!emailPass) {
						msg = getString(R.string.input_error_email);
					} else {
						msg = getString(R.string.input_error_name);
					}

					Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
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
