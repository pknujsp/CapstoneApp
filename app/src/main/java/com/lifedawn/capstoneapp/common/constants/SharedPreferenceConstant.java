package com.lifedawn.capstoneapp.common.constants;

public enum SharedPreferenceConstant {
	APP_INIT("APP_INIT"), REMINDER_SOUND_ON_OFF("REMINDER_SOUND_ON_OFF"), REMINDER_SOUND_URI("REMINDER_SOUND_URI"), REMINDER_VIBRATION(
			"REMINDER_VIBRATION"), REMINDER_WAKE("REMINDER_WAKE"), LAST_LATITUDE("LAST_LATITUDE"), LAST_LONGITUDE("LAST_LONGITUDE");
	
	private final String val;
	
	SharedPreferenceConstant(String val) {
		this.val = val;
	}
	
	public String getVal() {
		return val;
	}
}