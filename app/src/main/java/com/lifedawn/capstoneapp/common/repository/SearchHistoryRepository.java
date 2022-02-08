package com.lifedawn.capstoneapp.common.repository;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.ISearchHistoryRepository;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.room.AppDb;
import com.lifedawn.capstoneapp.room.dao.SearchHistoryDao;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SearchHistoryRepository implements ISearchHistoryRepository {
	private SearchHistoryDao dao;
	private ExecutorService executorService = MyApplication.EXECUTOR_SERVICE;
	private MutableLiveData<SearchHistoryDto> addedLiveData = new MutableLiveData<>();
	
	public MutableLiveData<SearchHistoryDto> getAddedLiveData() {
		return addedLiveData;
	}
	
	public SearchHistoryRepository(Application application) {
		dao = AppDb.getInstance(application.getApplicationContext()).searchHistoryDao();
	}
	
	
	@Override
	public void getAll(String type, OnDbQueryCallback<List<SearchHistoryDto>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(dao.getAll(type));
			}
		});
	}
	
	@Override
	public void get(int id, OnDbQueryCallback<SearchHistoryDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(dao.get(id));
			}
		});
	}
	
	@Override
	public void insert(SearchHistoryDto searchHistoryDto, @Nullable OnDbQueryCallback<SearchHistoryDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				int id = (int) dao.insert(searchHistoryDto);
				searchHistoryDto.setId(id);
				
				addedLiveData.postValue(searchHistoryDto);
				
				if (callback != null) {
					callback.onResult(searchHistoryDto);
				}
			}
		});
	}
	
	@Override
	public void update(SearchHistoryDto searchHistoryDto, @Nullable OnDbQueryCallback<SearchHistoryDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				dao.update(searchHistoryDto);
				if (callback != null) {
					callback.onResult(dao.get(searchHistoryDto.getId()));
				}
			}
		});
	}
	
	@Override
	public void delete(int id, @Nullable OnDbQueryCallback<Boolean> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				dao.delete(id);
				if (callback != null) {
					callback.onResult(true);
				}
			}
		});
	}
	
	@Override
	public void contains(String value, String type, OnDbQueryCallback<Boolean> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(dao.contains(value, type) == 1);
			}
		});
	}
}
