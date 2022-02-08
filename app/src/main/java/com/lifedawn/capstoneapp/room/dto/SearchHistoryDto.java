package com.lifedawn.capstoneapp.room.dto;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "search_history_table")
public class SearchHistoryDto implements Serializable {
	public enum SearchHistoryType {
		MAP
	}
	
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private int id;
	
	@ColumnInfo(name = "type")
	private SearchHistoryType searchHistoryType;
	
	@ColumnInfo(name = "value")
	private String value;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public SearchHistoryType getSearchHistoryType() {
		return searchHistoryType;
	}
	
	public void setSearchHistoryType(SearchHistoryType searchHistoryType) {
		this.searchHistoryType = searchHistoryType;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
