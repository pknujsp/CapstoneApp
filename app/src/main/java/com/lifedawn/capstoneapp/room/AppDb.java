package com.lifedawn.capstoneapp.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lifedawn.capstoneapp.room.dao.FriendDao;
import com.lifedawn.capstoneapp.room.dao.KmaAreaCodesDao;
import com.lifedawn.capstoneapp.room.dao.SearchHistoryDao;
import com.lifedawn.capstoneapp.room.dto.FriendDto;
import com.lifedawn.capstoneapp.room.dto.KmaAreaCodeDto;
import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

@Database(entities = {KmaAreaCodeDto.class, FriendDto.class, SearchHistoryDto.class}, version = 1, exportSchema = false)
@TypeConverters(RoomTypeConverter.class)
public abstract class AppDb extends RoomDatabase {
	private static volatile AppDb instance = null;

	public abstract FriendDao friendDao();

	public abstract SearchHistoryDao searchHistoryDao();

	public abstract KmaAreaCodesDao kmaAreaCodesDao();

	public static synchronized AppDb getInstance(Context context) {
		if (instance == null) {
			instance = Room.databaseBuilder(context, AppDb.class, "appdb")
					.createFromAsset("db/appdb.db").build();
		}
		return instance;
	}

	public static void closeInstance() {
		instance = null;
	}
}
