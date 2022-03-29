package com.lifedawn.capstoneapp.common.repositoryinterface;

import androidx.annotation.Nullable;
import androidx.room.Insert;
import androidx.room.Query;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.room.dao.CustomPlaceCategoryDao;
import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;

public interface ICustomPlaceCategoryRepository {
	void getAll(OnDbQueryCallback<List<CustomPlaceCategoryDto>> callback);

	void insert(String name, @Nullable OnDbQueryCallback<CustomPlaceCategoryDto> callback);

	void delete(int id, @Nullable OnDbQueryCallback<Boolean> callback);

	void contains(String name, OnDbQueryCallback<Boolean> callback);
}
