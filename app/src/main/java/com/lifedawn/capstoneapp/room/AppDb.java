package com.lifedawn.capstoneapp.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.lifedawn.capstoneapp.room.dao.FriendDao;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

@Database(entities = {FriendDto.class}, version = 1, exportSchema = false)
public abstract class AppDb extends RoomDatabase {
	private static volatile AppDb instance = null;
	
	public abstract FriendDao friendDao();
	
	public static synchronized AppDb getInstance(Context context) {
		if (instance == null) {
			instance = Room.databaseBuilder(context, AppDb.class, "appdb").createFromAsset("db/appdb.db").build();
		}
		return instance;
	}
	
	public static void closeInstance() {
		instance = null;
	}
}
