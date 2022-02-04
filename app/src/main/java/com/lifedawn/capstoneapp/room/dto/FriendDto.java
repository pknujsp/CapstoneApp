package com.lifedawn.capstoneapp.room.dto;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "friends_table")
public class FriendDto implements Serializable, Parcelable {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	private int id;
	
	@ColumnInfo(name = "email")
	private String email;
	
	@ColumnInfo(name = "name")
	private String name;
	
	protected FriendDto(Parcel in) {
		id = in.readInt();
		email = in.readString();
		name = in.readString();
	}
	
	public static final Creator<FriendDto> CREATOR = new Creator<FriendDto>() {
		@Override
		public FriendDto createFromParcel(Parcel in) {
			return new FriendDto(in);
		}
		
		@Override
		public FriendDto[] newArray(int size) {
			return new FriendDto[size];
		}
	};
	
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
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeInt(id);
		dest.writeString(email);
		dest.writeString(name);
	}
}
