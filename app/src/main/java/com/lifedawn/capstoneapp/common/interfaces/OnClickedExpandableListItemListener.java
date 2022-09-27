package com.lifedawn.capstoneapp.common.interfaces;

public interface OnClickedExpandableListItemListener<T> {
	void onClicked(int groupIdx, int childIdx, T e);
}
