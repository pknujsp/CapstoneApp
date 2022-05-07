package com.lifedawn.capstoneapp.calendar.fragments;

import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;

public abstract class SyncCalendarCallback<T> implements BackgroundCallback<T> {
	public boolean syncing = false;

	@Override
	public void onResultSuccessful(T e) {
		syncing = false;
	}

	@Override
	public void onResultFailed(Exception e) {
		syncing = false;
	}

	public void onSyncStarted() {
		syncing = true;

	}

	abstract public void onAlreadySyncing();
}
