package com.lifedawn.capstoneapp.common.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.ICustomPlaceCategoryRepository;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.room.AppDb;
import com.lifedawn.capstoneapp.room.dao.CustomPlaceCategoryDao;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;

import java.util.List;

public class CustomPlaceCategoryRepository implements ICustomPlaceCategoryRepository {
	private CustomPlaceCategoryDao dao;

	public CustomPlaceCategoryRepository(Context context) {
		dao = AppDb.getInstance(context).customPlaceCategoryDao();
	}


	@Override
	public void getAll(OnDbQueryCallback<List<CustomPlaceCategoryDto>> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(dao.getAll());
			}
		});
	}

	@Override
	public void insert(String name, @Nullable OnDbQueryCallback<CustomPlaceCategoryDto> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				CustomPlaceCategoryDto dto = new CustomPlaceCategoryDto();
				dto.setName(name);

				long newId = dao.insert(dto);
				dto.setId((int) newId);
				callback.onResult(dto);
			}
		});
	}

	@Override
	public void delete(int id, @Nullable OnDbQueryCallback<Boolean> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				dao.delete(id);
				callback.onResult(true);
			}
		});
	}

	@Override
	public void contains(String name, OnDbQueryCallback<Boolean> callback) {
		MyApplication.EXECUTOR_SERVICE.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(dao.contains(name) == 1);
			}
		});
	}
}
