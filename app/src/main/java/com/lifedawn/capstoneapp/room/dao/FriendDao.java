package com.lifedawn.capstoneapp.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifedawn.capstoneapp.room.dto.FriendDto;

import java.util.List;

@Dao
public interface FriendDao {
	@Query("SELECT * FROM friends_table")
	List<FriendDto> getAll();
	
	@Query("SELECT * FROM friends_table WHERE id = :id")
	FriendDto get(int id);
	
	@Insert
	long insert(FriendDto friendDto);
	
	@Update(entity = FriendDto.class, onConflict = OnConflictStrategy.IGNORE)
	void update(FriendDto friendDto);
	
	@Query("DELETE FROM friends_table WHERE id = :id")
	void delete(int id);
}
