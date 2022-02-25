package com.lifedawn.capstoneapp.common.interfaces;

import com.lifedawn.capstoneapp.common.repository.CalendarRepository;

public interface OnClickPromiseItemListener {
	void onClickedEdit(CalendarRepository.EventObj event, int position);

	void onClickedEvent(CalendarRepository.EventObj event, int position);

	void onClickedRefusal(CalendarRepository.EventObj event, int position);

	void onClickedAcceptance(CalendarRepository.EventObj event, int position);
}
