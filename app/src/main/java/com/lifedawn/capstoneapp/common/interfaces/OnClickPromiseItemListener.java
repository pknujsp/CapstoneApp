package com.lifedawn.capstoneapp.common.interfaces;

import android.content.ContentValues;

public interface OnClickPromiseItemListener {
	void onClickedEdit(ContentValues event, int position);

	void onClickedEvent(ContentValues event, int position);

	void onClickedRefusal(ContentValues event, int position);

	void onClickedAcceptance(ContentValues event, int position);
}
