package com.lifedawn.capstoneapp.common.interfaces;

import com.lifedawn.capstoneapp.room.dto.FriendDto;

public interface OnClickFriendItemListener {
	void onClickedRemove(FriendDto friend, int position);
}
