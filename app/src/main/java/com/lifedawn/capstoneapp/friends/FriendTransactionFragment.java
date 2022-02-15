package com.lifedawn.capstoneapp.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.lifedawn.capstoneapp.databinding.FragmentFriendTransactionBinding;
import com.lifedawn.capstoneapp.friends.myfriends.FriendsFragment;

public class FriendTransactionFragment extends Fragment {
	private FragmentFriendTransactionBinding binding;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFriendTransactionBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.add(binding.fragmentContainerView.getId(), new FriendsFragment(), FriendsFragment.class.getName()).commit();
		
	}
}