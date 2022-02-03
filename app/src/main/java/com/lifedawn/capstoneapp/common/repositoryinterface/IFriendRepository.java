package com.lifedawn.capstoneapp.common.repositoryinterface;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

import java.util.List;

public interface IFriendRepository {
	void getAll(OnDbQueryCallback<List<FriendDto>> callback);
	
	void get(int id, OnDbQueryCallback<FriendDto> callback);
	
	void insert(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback);
	
	void update(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback);
	
	void delete(int id, OnDbQueryCallback<Boolean> callback);
}
