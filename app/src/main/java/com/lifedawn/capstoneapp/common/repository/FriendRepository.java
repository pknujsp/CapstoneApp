package com.lifedawn.capstoneapp.common.repository;

import android.content.Context;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repositoryinterface.IFriendRepository;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.room.AppDb;
import com.lifedawn.capstoneapp.room.dao.FriendDao;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class FriendRepository implements IFriendRepository {
	private static FriendRepository instance;
	private FriendDao friendDao;
	private Context context;
	private ExecutorService executorService;

	public static FriendRepository getInstance(Context context) {
		if (instance == null) {
			instance = new FriendRepository(context);
		}
		return instance;
	}

	private FriendRepository(Context context) {
		this.context = context;
		friendDao = AppDb.getInstance(context).friendDao();
		executorService = MyApplication.EXECUTOR_SERVICE;
	}

	@Override
	public void getAll(OnDbQueryCallback<List<FriendDto>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(friendDao.getAll());
			}
		});
	}

	@Override
	public void get(int id, OnDbQueryCallback<FriendDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(friendDao.get(id));
			}
		});
	}

	@Override
	public void insert(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				int insertedId = (int) friendDao.insert(friendDto);
				callback.onResult(friendDao.get(insertedId));
			}
		});
	}

	@Override
	public void update(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				friendDao.update(friendDto);
				callback.onResult(friendDao.get(friendDto.getId()));
			}
		});
	}

	@Override
	public void delete(int id, OnDbQueryCallback<Boolean> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				friendDao.delete(id);
				callback.onResult(true);
			}
		});
	}

	@Override
	public void contains(String email, OnDbQueryCallback<Boolean> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResult(friendDao.contains(email) == 1);
			}
		});
	}
}
