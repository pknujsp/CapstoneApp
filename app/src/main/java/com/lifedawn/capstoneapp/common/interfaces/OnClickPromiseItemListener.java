package com.lifedawn.capstoneapp.common.interfaces;

import com.lifedawn.capstoneapp.model.firestore.EventDto;

public interface OnClickPromiseItemListener {
	void onClickedEdit(EventDto event, int position);

	void onClickedEvent(EventDto event, int position);

	void onClickedRefusal(EventDto event, int position);

	void onClickedAcceptance(EventDto event, int position);

	void onClickedRemoveEvent(EventDto event, int position);
}
