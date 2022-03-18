package com.lifedawn.capstoneapp.main;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.ValueUnits;
import com.lifedawn.capstoneapp.common.util.NotificationHelper;
import com.lifedawn.capstoneapp.weather.response.KmaResponseProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyApplication extends Application {
	public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);
	public final static ValueUnitObj VALUE_UNIT_OBJ = new ValueUnitObj();

	@Override
	public void onCreate() {
		super.onCreate();

		initPreferences();
		KmaResponseProcessor.init(getApplicationContext());
	}

	private void initPreferences() {
		try {
			if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getAll().isEmpty()) {
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				editor.putString("temp_unit", ValueUnits.celsius.name());
				editor.putString("visibility_unit", ValueUnits.km.name());
				editor.putString("windspeed_unit", ValueUnits.mPerSec.name());
				editor.putString("clock_format_unit", ValueUnits.clock12.name()).commit();
			}

			loadValueUnits(getApplicationContext());
		} catch (NullPointerException e) {

		}
	}

	public static void loadValueUnits(Context context) {
		if (VALUE_UNIT_OBJ.getTempUnit() == null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

			VALUE_UNIT_OBJ.setTempUnit(ValueUnits.valueOf(sharedPreferences.getString("temp_unit",
					ValueUnits.celsius.name()))).setWindUnit(ValueUnits.valueOf(sharedPreferences.getString("windspeed_unit",
					ValueUnits.mPerSec.name()))).setVisibilityUnit(ValueUnits.valueOf(sharedPreferences.getString("visibility_unit",
					ValueUnits.km.name()))).setClockUnit(ValueUnits.valueOf(
					sharedPreferences.getString("clock_format_unit", ValueUnits.clock12.name())));
		}
	}

	public static class ValueUnitObj {
		private ValueUnits tempUnit;
		private String tempUnitText;

		private ValueUnits windUnit;
		private String windUnitText;

		private ValueUnits visibilityUnit;
		private String visibilityUnitText;

		private ValueUnits clockUnit;

		public ValueUnits getTempUnit() {
			return tempUnit;
		}

		public ValueUnitObj setTempUnit(ValueUnits tempUnit) {
			this.tempUnit = tempUnit;
			tempUnitText = ValueUnits.toString(tempUnit);
			return this;
		}

		public ValueUnits getWindUnit() {
			return windUnit;
		}

		public ValueUnitObj setWindUnit(ValueUnits windUnit) {
			this.windUnit = windUnit;
			windUnitText = ValueUnits.toString(windUnit);
			return this;
		}

		public ValueUnits getVisibilityUnit() {
			return visibilityUnit;
		}

		public ValueUnitObj setVisibilityUnit(ValueUnits visibilityUnit) {
			this.visibilityUnit = visibilityUnit;
			visibilityUnitText = ValueUnits.toString(visibilityUnit);
			return this;
		}

		public ValueUnits getClockUnit() {
			return clockUnit;
		}

		public ValueUnitObj setClockUnit(ValueUnits clockUnit) {
			this.clockUnit = clockUnit;
			return this;
		}

		public String getTempUnitText() {
			return tempUnitText;
		}

		public String getWindUnitText() {
			return windUnitText;
		}

		public String getVisibilityUnitText() {
			return visibilityUnitText;
		}
	}
}
