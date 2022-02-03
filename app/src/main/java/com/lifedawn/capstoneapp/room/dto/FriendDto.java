package com.lifedawn.capstoneapp.room.dto;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends_table")
public class FriendDto {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private int id;
	
	@ColumnInfo(name = "email")
	private String email;
	
	@ColumnInfo(name = "name")
	private String name;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
