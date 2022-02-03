package com.lifedawn.capstoneapp.common.interfaces;

import com.google.api.services.calendar.model.Event;

public interface OnClickPromiseItemListener {
	void onClickedEdit(Event event, int position);
	
	void onClickedEvent(Event event, int position);
}
