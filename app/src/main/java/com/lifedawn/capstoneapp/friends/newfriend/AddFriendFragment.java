package com.lifedawn.capstoneapp.friends.newfriend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.lifedawn.capstoneapp.databinding.FragmentAddFriendBinding;

public class AddFriendFragment extends Fragment {
	private FragmentAddFriendBinding binding;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAddFriendBinding.inflate(inflater);
		return binding.getRoot();
	}
}