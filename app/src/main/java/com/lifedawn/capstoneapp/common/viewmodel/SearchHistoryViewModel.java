package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.SearchHistoryRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.ISearchHistoryRepository;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;

public class SearchHistoryViewModel extends AndroidViewModel implements ISearchHistoryRepository {
	private SearchHistoryRepository repository;
	private MutableLiveData<SearchHistoryDto> addedLiveData;
	
	public SearchHistoryViewModel(@NonNull Application application) {
		super(application);
		repository = new SearchHistoryRepository(application);
		addedLiveData = repository.getAddedLiveData();
	}
	
	public LiveData<SearchHistoryDto> getAddedLiveData() {
		return addedLiveData;
	}
	
	@Override
	public void getAll(String type, OnDbQueryCallback<List<SearchHistoryDto>> callback) {
		repository.getAll(type, callback);
	}
	
	@Override
	public void get(int id, OnDbQueryCallback<SearchHistoryDto> callback) {
		repository.get(id, callback);
	}
	
	@Override
	public void insert(SearchHistoryDto searchHistoryDto, @Nullable OnDbQueryCallback<SearchHistoryDto> callback) {
		repository.insert(searchHistoryDto, callback);
	}
	
	@Override
	public void update(SearchHistoryDto searchHistoryDto, @Nullable OnDbQueryCallback<SearchHistoryDto> callback) {
		repository.update(searchHistoryDto, callback);
	}
	
	@Override
	public void delete(int id, @Nullable OnDbQueryCallback<Boolean> callback) {
		repository.delete(id, callback);
	}
	
	@Override
	public void contains(String value, String type, OnDbQueryCallback<Boolean> callback) {
		repository.contains(value, type, callback);
	}
}
