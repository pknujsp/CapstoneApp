package com.lifedawn.capstoneapp.common.repositoryinterface;

import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;

public interface ISearchHistoryRepository {
	void getAll(String type, OnDbQueryCallback<List<SearchHistoryDto>> callback);
	
	void get(int id, OnDbQueryCallback<SearchHistoryDto> callback);
	
	void insert(SearchHistoryDto searchHistoryDto, @Nullable OnDbQueryCallback<SearchHistoryDto> callback);
	
	void update(SearchHistoryDto searchHistoryDto, @Nullable OnDbQueryCallback<SearchHistoryDto> callback);
	
	void delete(int id, @Nullable OnDbQueryCallback<Boolean> callback);
	
	void contains(String value, String type, OnDbQueryCallback<Boolean> callback);
}
