package com.lifedawn.capstoneapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifedawn.capstoneapp.room.dto.CustomPlaceCategoryDto;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;

@Dao
public interface CustomPlaceCategoryDao {
	@Query("SELECT * FROM custom_place_category_table")
	List<CustomPlaceCategoryDto> getAll();

	@Insert
	long insert(CustomPlaceCategoryDto customPlaceCategoryDto);

	@Query("DELETE FROM custom_place_category_table WHERE id = :id")
	void delete(int id);

	@Query("SELECT EXISTS (SELECT * FROM custom_place_category_table WHERE name = :name) AS SUCCESS")
	int contains(String name);

}
