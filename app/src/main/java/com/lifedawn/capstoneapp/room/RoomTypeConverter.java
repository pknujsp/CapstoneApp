package com.lifedawn.capstoneapp.room;

import androidx.room.TypeConverter;

import com.lifedawn.capstoneapp.room.dto.SearchHistoryDto;

public class RoomTypeConverter {
	@TypeConverter
	public String toString(SearchHistoryDto.SearchHistoryType searchHistoryType) {
		if (searchHistoryType != null) {
			return searchHistoryType.name();
		} else {
			return "";
		}
	}
	
	@TypeConverter
	public SearchHistoryDto.SearchHistoryType toSearchHistoryType(String value) {
		if (value.isEmpty()) {
			return null;
		} else {
			return SearchHistoryDto.SearchHistoryType.valueOf(value);
		}
	}
}
