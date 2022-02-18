package com.lifedawn.capstoneapp.common.interfaces;

public interface BackgroundCallback<T> {
	void onResultSuccessful(T e);
	
	void onResultFailed(Exception e);
}
