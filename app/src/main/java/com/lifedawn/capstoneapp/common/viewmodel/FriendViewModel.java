package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.FriendRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.IFriendRepository;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

import java.util.List;

public class FriendViewModel extends AndroidViewModel implements IFriendRepository {
	private FriendRepository friendRepository;
	
	public FriendViewModel(@NonNull Application application) {
		super(application);
		friendRepository = new FriendRepository(application.getApplicationContext());
	}
	
	@Override
	public void getAll(OnDbQueryCallback<List<FriendDto>> callback) {
		friendRepository.getAll(callback);
	}
	
	@Override
	public void get(int id, OnDbQueryCallback<FriendDto> callback) {
		friendRepository.get(id, callback);
	}
	
	@Override
	public void insert(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback) {
		friendRepository.insert(friendDto, callback);
	}
	
	@Override
	public void update(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback) {
		friendRepository.update(friendDto, callback);
	}
	
	@Override
	public void delete(int id, OnDbQueryCallback<Boolean> callback) {
		friendRepository.delete(id, callback);
	}
	
	@Override
	public void contains(String email, OnDbQueryCallback<Boolean> callback) {
		friendRepository.contains(email, callback);
	}
}
