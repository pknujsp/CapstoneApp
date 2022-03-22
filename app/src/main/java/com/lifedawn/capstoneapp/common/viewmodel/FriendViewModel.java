package com.lifedawn.capstoneapp.common.viewmodel;

import android.app.Application;
import android.content.ContentValues;
import android.provider.CalendarContract;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.capstoneapp.common.interfaces.OnDbQueryCallback;
import com.lifedawn.capstoneapp.common.repository.FriendRepository;
import com.lifedawn.capstoneapp.common.repositoryinterface.IFriendRepository;
import com.lifedawn.capstoneapp.room.dto.FriendDto;

import java.util.List;

public class FriendViewModel extends AndroidViewModel implements IFriendRepository {
	private FriendRepository friendRepository;
	private List<FriendDto> friendDtoList;
	private ArrayMap<String, FriendDto> friendDtoArrayMap;

	public FriendViewModel(@NonNull Application application) {
		super(application);
		friendRepository = FriendRepository.getInstance(application.getApplicationContext());
		setFriendArrayMap();
	}

	private void setFriendArrayMap() {
		friendRepository.getAll(new OnDbQueryCallback<List<FriendDto>>() {
			@Override
			public void onResult(List<FriendDto> e) {
				friendDtoList = e;
				friendDtoArrayMap = new ArrayMap<>();
				for (FriendDto friendDto : friendDtoList) {
					friendDtoArrayMap.put(friendDto.getEmail(), friendDto);
				}
			}
		});
	}

	public String getName(ContentValues contentValues) {
		String email = contentValues.getAsString(CalendarContract.Attendees.ATTENDEE_EMAIL);
		if (email == null) {
			if (contentValues.containsKey(CalendarContract.Events.ORGANIZER)) {
				email = contentValues.getAsString(CalendarContract.Events.ORGANIZER);
			}
		}
		String name = getName(email);

		if (name.equals(email)) {
			return contentValues.containsKey(CalendarContract.Attendees.ATTENDEE_NAME) ?
					contentValues.getAsString(CalendarContract.Attendees.ATTENDEE_NAME) :
					name;
		} else {
			return name;
		}

	}

	public String getName(String email) {
		if (friendDtoArrayMap.containsKey(email)) {
			return friendDtoArrayMap.get(email).getName();
		} else {
			return email;
		}
	}

	public ArrayMap<String, FriendDto> getFriendDtoArrayMap() {
		return friendDtoArrayMap;
	}

	public List<FriendDto> getFriendDtoList() {
		return friendDtoList;
	}

	@Override
	public void getAll(OnDbQueryCallback<List<FriendDto>> callback) {
		friendRepository.getAll(callback);
	}

	@Override
	public void get(int id, OnDbQueryCallback<FriendDto> callback) {
		friendRepository.get(id, callback);
	}

	@Override
	public void insert(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback) {
		friendRepository.insert(friendDto, callback);
		setFriendArrayMap();
	}

	@Override
	public void update(FriendDto friendDto, OnDbQueryCallback<FriendDto> callback) {
		friendRepository.update(friendDto, callback);
		setFriendArrayMap();
	}

	@Override
	public void delete(int id, OnDbQueryCallback<Boolean> callback) {
		friendRepository.delete(id, callback);
		setFriendArrayMap();
	}

	@Override
	public void contains(String email, OnDbQueryCallback<Boolean> callback) {
		friendRepository.contains(email, callback);
	}
}
