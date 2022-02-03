package com.lifedawn.capstoneapp.common.interfaces;

public interface OnHttpApiCallback<T> {
	void onResultSuccessful(T e);
	
	void onResultFailed(Exception e);
}
