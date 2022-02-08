package com.lifedawn.capstoneapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

import java.util.List;

@Dao
public interface SearchHistoryDao {
	@Query("SELECT * FROM search_history_table WHERE type = :type")
	List<SearchHistoryDto> getAll(String type);
	
	@Query("SELECT * FROM search_history_table WHERE id = :id")
	SearchHistoryDto get(int id);
	
	@Insert
	long insert(SearchHistoryDto searchHistoryDto);
	
	@Update(entity = SearchHistoryDto.class, onConflict = OnConflictStrategy.IGNORE)
	void update(SearchHistoryDto searchHistoryDto);
	
	@Query("DELETE FROM search_history_table WHERE id = :id")
	void delete(int id);
	
	@Query("SELECT EXISTS (SELECT * FROM search_history_table WHERE value = :value AND type = :type) AS SUCCESS")
	int contains(String value, String type);
}
