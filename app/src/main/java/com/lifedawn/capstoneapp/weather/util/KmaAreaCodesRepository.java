package com.lifedawn.capstoneapp.weather.util;

import android.content.Context;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.room.AppDb;
import com.lifedawn.capstoneapp.room.dao.KmaAreaCodesDao;
import com.lifedawn.capstoneapp.room.dto.KmaAreaCodeDto;

import java.util.ArrayList;
import java.util.List;

public class KmaAreaCodesRepository {
	private KmaAreaCodesDao kmaAreaCodesDao;

	public KmaAreaCodesRepository(Context context) {
		this.kmaAreaCodesDao = AppDb.getInstance(context).kmaAreaCodesDao();
	}


	public void getAreaCodes(double latitude, double longitude, OnDbQueryCallback<List<KmaAreaCodeDto>> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<KmaAreaCodeDto> list = kmaAreaCodesDao.getAreaCodes(latitude, longitude);
				if (list == null) {
					callback.onResult(new ArrayList<>());
				} else {
					callback.onResult(list);
				}
			}
		}).start();
	}

}