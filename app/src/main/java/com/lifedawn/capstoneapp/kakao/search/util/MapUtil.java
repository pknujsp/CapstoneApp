package com.lifedawn.capstoneapp.kakao.search.util;

public class MapUtil {
	public static String convertMeterToKm(double meterDistance) {
		if (meterDistance >= 1000) {
			return meterDistance / 1000 + "km";
		} else {
			return meterDistance + "m";
		}
		
	}
}
