package com.lifedawn.capstoneapp.weather.response;

import android.content.Context;
import android.content.res.TypedArray;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.ValueUnits;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaCurrentConditions;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaDailyForecast;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaHourlyForecast;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.util.SunRiseSetUtil;
import com.lifedawn.capstoneapp.weather.util.WeatherUtil;
import com.lifedawn.capstoneapp.weather.util.WindUtil;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import com.tickaroo.tikxml.TikXml;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class KmaResponseProcessor extends WeatherResponseProcessor {
	private static final Map<String, String> WEATHER_MID_ICON_DESCRIPTION_MAP = new HashMap<>();
	private static final Map<String, String> WEATHER_WEB_ICON_DESCRIPTION_MAP = new HashMap<>();

	private static final Map<String, Integer> WEATHER_MID_ICON_ID_MAP = new HashMap<>();
	private static final Map<String, Integer> WEATHER_WEB_ICON_ID_MAP = new HashMap<>();

	private static final Map<String, String> HOURLY_TO_DAILY_DESCRIPTION_MAP = new HashMap<>();

	private static final String POP = "POP";
	private static final String PTY = "PTY";
	private static final String PCP = "PCP";
	private static final String REH = "REH";
	private static final String SNO = "SNO";
	private static final String SKY = "SKY";
	private static final String TMP = "TMP";
	private static final String TMN = "TMN";
	private static final String TMX = "TMX";
	private static final String VEC = "VEC";
	private static final String WSD = "WSD";
	private static final String T1H = "T1H";
	private static final String RN1 = "RN1";
	private static final String LGT = "LGT";

	private KmaResponseProcessor() {
	}

	public static void init(Context context) {
		if (WEATHER_MID_ICON_DESCRIPTION_MAP.isEmpty()
				|| WEATHER_MID_ICON_ID_MAP.isEmpty() || HOURLY_TO_DAILY_DESCRIPTION_MAP.isEmpty()) {

			String[] midCodes = context.getResources().getStringArray(R.array.KmaMidIconCodes);
			String[] webIconCodes = context.getResources().getStringArray(R.array.KmaWeatherDescriptionCodes);
			String[] midDescriptions = context.getResources().getStringArray(R.array.KmaMidIconDescriptionsForCode);
			String[] webIconDescriptions = context.getResources().getStringArray(R.array.KmaWeatherDescriptions);

			TypedArray midIconIds = context.getResources().obtainTypedArray(R.array.KmaMidWeatherIconForCode);
			TypedArray webIconIds = context.getResources().obtainTypedArray(R.array.KmaWeatherIconForDescriptionCode);

			WEATHER_MID_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < midCodes.length; i++) {
				WEATHER_MID_ICON_DESCRIPTION_MAP.put(midCodes[i], midDescriptions[i]);
				WEATHER_MID_ICON_ID_MAP.put(midCodes[i], midIconIds.getResourceId(i, R.drawable.ic_launcher_foreground));
			}

			WEATHER_WEB_ICON_DESCRIPTION_MAP.clear();
			for (int i = 0; i < webIconCodes.length; i++) {
				WEATHER_WEB_ICON_DESCRIPTION_MAP.put(webIconCodes[i], webIconDescriptions[i]);
				WEATHER_WEB_ICON_ID_MAP.put(webIconCodes[i], webIconIds.getResourceId(i, R.drawable.ic_launcher_foreground));
			}

			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("비", "흐리고 비");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("비/눈", "흐리고 비/눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("눈", "흐리고 눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("빗방울", "흐리고 비");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("빗방울/눈날림", "흐리고 비/눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("눈날림", "흐리고 눈");
			HOURLY_TO_DAILY_DESCRIPTION_MAP.put("구름 많음", "구름많음");
		}
	}

		/*
		sky
		<item>맑음</item>
        <item>구름 많음</item>
        <item>흐림</item>
        pty
        <item>비</item>
        <item>비/눈</item>
        <item>눈</item>
        <item>소나기</item>
        <item>빗방울</item>
        <item>빗방울/눈날림</item>
        <item>눈날림</item>
		 */

	public static String convertSkyTextToCode(String text) {
		switch (text) {
			case "맑음":
				return "1";
			case "구름 많음":
				return "3";
			case "흐림":
				return "4";
			default:
				return null;
		}
	}

	public static String convertPtyTextToCode(String text) {
		switch (text) {
			case "없음":
				return "0";
			case "비":
				return "1";
			case "비/눈":
				return "2";
			case "눈":
				return "3";
			case "소나기":
				return "4";
			case "빗방울":
				return "5";
			case "빗방울/눈날림":
				return "6";
			case "눈날림":
				return "7";
			default:
				return null;
		}
	}

	public static int getWeatherSkyIconImg(String code, boolean night) {
		/*
		if (night) {
			if (code.equals("1")) {
				return R.drawable.night_clear;
			} else if (code.equals("3")) {
				return R.drawable.night_mostly_cloudy;
			} else {
				return WEATHER_SKY_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_SKY_ICON_ID_MAP.get(code);
		*/
		return 1;

	}

	public static String getWeatherSkyIconDescription(String code) {
		//return WEATHER_SKY_ICON_DESCRIPTION_MAP.get(code);
		return null;
	}

	public static int getWeatherPtyIconImg(String code, boolean night) {
		/*
		if (night) {
			if (code.equals("0")) {
				return R.drawable.night_clear;
			} else {
				WEATHER_PTY_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_PTY_ICON_ID_MAP.get(code);
		 */
		return 1;

	}

	public static int getWeatherSkyAndPtyIconImg(String pty, String sky, boolean night) {
		/*
		if (pty.equals("0")) {
			return getWeatherSkyIconImg(sky, night);
		} else {
			return WEATHER_PTY_ICON_ID_MAP.get(pty);
		}
		 */
		return 1;
	}

	public static String getWeatherDescription(String pty, String sky) {
		if (pty.equals("0")) {
			return getWeatherSkyIconDescription(sky);
		} else {
			return getWeatherPtyIconDescription(pty);
		}
	}

	public static String getWeatherDescriptionWeb(String weatherDescriptionKr) {
		return WEATHER_WEB_ICON_DESCRIPTION_MAP.get(weatherDescriptionKr);
	}

	public static int getWeatherIconImgWeb(String weatherDescriptionKr, boolean night) {
		if (night) {
			if (weatherDescriptionKr.equals("맑음")) {
				return R.drawable.night_clear;
			} else if (weatherDescriptionKr.equals("구름많음")) {
				return R.drawable.night_mostly_cloudy;
			} else {
				return WEATHER_WEB_ICON_ID_MAP.get(weatherDescriptionKr);
			}
		} else {
			return WEATHER_WEB_ICON_ID_MAP.get(weatherDescriptionKr);
		}
	}


	public static String getWeatherMidIconDescription(String code) {
		return WEATHER_MID_ICON_DESCRIPTION_MAP.get(code);
	}

	public static int getWeatherMidIconImg(String code, boolean night) {
		if (night) {
			if (code.equals("맑음")) {
				return R.drawable.night_clear;
			} else if (code.equals("구름많음")) {
				return R.drawable.night_mostly_cloudy;
			} else {
				return WEATHER_MID_ICON_ID_MAP.get(code);
			}
		}
		return WEATHER_MID_ICON_ID_MAP.get(code);
	}

	public static String getWeatherPtyIconDescription(String code) {
		//return WEATHER_PTY_ICON_DESCRIPTION_MAP.get(code);
		return null;
	}

	public static String convertSkyPtyToMid(String sky, String pty) {
		if (pty.equals("0")) {
			switch (sky) {
				case "1":
					return "맑음";
				case "3":
					return "구름많음";
				default:
					return "흐림";
			}
		} else {
			switch (pty) {
				case "1":
				case "5":
					return "흐리고 비";
				case "2":
				case "6":
					return "흐리고 비/눈";
				case "3":
				case "7":
					return "흐리고 눈";
				default:
					return "흐리고 소나기";
			}
		}
	}

	public static String convertHourlyWeatherDescriptionToMid(String description) {
		/*
		hourly -
		<item>맑음</item>
        <item>구름 많음</item>
        <item>흐림</item>
        <item>비</item>
        <item>비/눈</item>
        <item>눈</item>
        <item>소나기</item>
        <item>빗방울</item>
        <item>빗방울/눈날림</item>
        <item>눈날림</item>
		mid -
		<item>맑음</item>
        <item>구름많음</item>
        <item>구름많고 비</item>
        <item>구름많고 눈</item>
        <item>구름많고 비/눈</item>
        <item>구름많고 소나기</item>
        <item>흐림</item>
        <item>흐리고 비</item>
        <item>흐리고 눈</item>
        <item>흐리고 비/눈</item>
        <item>흐리고 소나기</item>
        <item>소나기</item>
		 */

		if (HOURLY_TO_DAILY_DESCRIPTION_MAP.containsKey(description)) {
			return HOURLY_TO_DAILY_DESCRIPTION_MAP.get(description);
		} else {
			return description;
		}


	}


	public static List<HourlyForecastDto> makeHourlyForecastDtoListOfWEB(Context context,
	                                                                     List<KmaHourlyForecast> hourlyForecastList, double latitude, double longitude) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String mPerSec = "m/s";

		final String zeroRainVolume = "0.0mm";
		final String zeroSnowVolume = "0.0cm";
		final String zeroPrecipitationVolume = "0.0mm";
		final String percent = "%";
		final ZoneId zoneId = ZoneId.of("Asia/Seoul");

		final Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunSetRiseDataMap = SunRiseSetUtil.getDailySunRiseSetMap(
				ZonedDateTime.of(hourlyForecastList.get(0).getHour().toLocalDateTime(), zoneId),
				ZonedDateTime.of(hourlyForecastList.get(hourlyForecastList.size() - 1).getHour().toLocalDateTime(),
						zoneId), latitude, longitude);

		boolean isNight = false;
		Calendar itemCalendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
		Calendar sunRise = null;
		Calendar sunSet = null;

		List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();

		String snowVolume;
		String rainVolume;
		boolean hasRain;
		boolean hasSnow;
		String windSpeed = null;
		int humidity = 0;
		Double feelsLikeTemp = 0.0;
		String windDirectionStr = null;
		Integer windDirectionInt = 0;
		final String poong = "풍";

		for (KmaHourlyForecast finalHourlyForecast : hourlyForecastList) {
			HourlyForecastDto hourlyForecastDto = new HourlyForecastDto();
			hasRain = finalHourlyForecast.isHasRain();

			if (!hasRain) {
				rainVolume = zeroRainVolume;
			} else {
				rainVolume = finalHourlyForecast.getRainVolume();
			}

			hasSnow = finalHourlyForecast.isHasSnow();

			if (!hasSnow) {
				snowVolume = zeroSnowVolume;
			} else {
				snowVolume = finalHourlyForecast.getSnowVolume();
			}

			itemCalendar.setTimeInMillis(finalHourlyForecast.getHour().toInstant().toEpochMilli());
			sunRise = sunSetRiseDataMap.get(finalHourlyForecast.getHour().getDayOfYear()).getSunrise();
			sunSet = sunSetRiseDataMap.get(finalHourlyForecast.getHour().getDayOfYear()).getSunset();
			isNight = SunRiseSetUtil.isNight(itemCalendar, sunRise, sunSet);

			humidity = Integer.parseInt(finalHourlyForecast.getHumidity().replace(percent, ""));

			hourlyForecastDto.setHours(finalHourlyForecast.getHour())
					.setTemp(ValueUnits.convertTemperature(finalHourlyForecast.getTemp(), tempUnit) + tempDegree)
					.setRainVolume(rainVolume)
					.setHasRain(hasRain)
					.setHasSnow(hasSnow)
					.setSnowVolume(snowVolume)
					.setPrecipitationVolume(zeroPrecipitationVolume)
					.setWeatherIcon(getWeatherIconImgWeb(finalHourlyForecast.getWeatherDescription(),
							isNight))
					.setWeatherDescription(getWeatherDescriptionWeb(finalHourlyForecast.getWeatherDescription()))
					.setHumidity(finalHourlyForecast.getHumidity()).setPop(!finalHourlyForecast.getPop().contains("%") ?
					"-" : finalHourlyForecast.getPop());

			if (finalHourlyForecast.getWindDirection() != null) {
				windSpeed = finalHourlyForecast.getWindSpeed().replace(mPerSec, "");
				windDirectionStr = finalHourlyForecast.getWindDirection().replace(poong, "");
				windDirectionInt = WindUtil.parseWindDirectionStrAsInt(windDirectionStr);

				hourlyForecastDto.setWindDirectionVal(windDirectionInt)
						.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionInt.toString()))
						.setWindStrength(WindUtil.getSimpleWindSpeedDescription(windSpeed))
						.setWindSpeed(ValueUnits.convertWindSpeed(windSpeed, windUnit)
								+ MyApplication.VALUE_UNIT_OBJ.getWindUnitText());

				feelsLikeTemp = WeatherUtil.calcFeelsLikeTemperature(Double.parseDouble(finalHourlyForecast.getTemp()),
						ValueUnits.convertWindSpeed(windSpeed, ValueUnits.kmPerHour), humidity);

				hourlyForecastDto.setFeelsLikeTemp(ValueUnits.convertTemperature(feelsLikeTemp.toString(),
						tempUnit) + tempDegree);
			}

			hourlyForecastDtoList.add(hourlyForecastDto);
		}
		return hourlyForecastDtoList;
	}


	public static List<DailyForecastDto> makeDailyForecastDtoListOfWEB(List<KmaDailyForecast> dailyForecastList) {
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();

		for (KmaDailyForecast finalDailyForecast : dailyForecastList) {
			DailyForecastDto dailyForecastDto = new DailyForecastDto();
			dailyForecastDtoList.add(dailyForecastDto);

			dailyForecastDto.setDate(finalDailyForecast.getDate())
					.setMinTemp(ValueUnits.convertTemperature(finalDailyForecast.getMinTemp(), tempUnit) + tempDegree)
					.setMaxTemp(ValueUnits.convertTemperature(finalDailyForecast.getMaxTemp(), tempUnit) + tempDegree)
					.setSingle(finalDailyForecast.isSingle());

			if (finalDailyForecast.isSingle()) {
				DailyForecastDto.Values single = new DailyForecastDto.Values();
				dailyForecastDto.setSingleValues(single);

				single.setPop(finalDailyForecast.getSingleValues().getPop())
						.setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.getSingleValues().getWeatherDescription(), false))
						.setWeatherDescription(getWeatherMidIconDescription(finalDailyForecast.getSingleValues().getWeatherDescription()));
			} else {
				DailyForecastDto.Values am = new DailyForecastDto.Values();
				DailyForecastDto.Values pm = new DailyForecastDto.Values();
				dailyForecastDto.setAmValues(am).setPmValues(pm);

				am.setPop(finalDailyForecast.getAmValues().getPop())
						.setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.getAmValues().getWeatherDescription(), false))
						.setWeatherDescription(getWeatherMidIconDescription(finalDailyForecast.getAmValues().getWeatherDescription()));
				pm.setPop(finalDailyForecast.getPmValues().getPop())
						.setWeatherIcon(getWeatherMidIconImg(finalDailyForecast.getPmValues().getWeatherDescription(), false))
						.setWeatherDescription(getWeatherMidIconDescription(finalDailyForecast.getPmValues().getWeatherDescription()));
			}

		}
		return dailyForecastDtoList;
	}


	public static CurrentConditionsDto makeCurrentConditionsDtoOfWEB(Context context,
	                                                                 KmaCurrentConditions kmaCurrentConditions,
	                                                                 KmaHourlyForecast kmaHourlyForecast,
	                                                                 Double latitude,
	                                                                 Double longitude) {
		ValueUnits windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		ValueUnits tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		final String tempUnitStr = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		CurrentConditionsDto currentConditionsDto = new CurrentConditionsDto();
		ZonedDateTime currentTime = ZonedDateTime.parse(kmaCurrentConditions.getBaseDateTime());

		String currentPtyCode = kmaCurrentConditions.getPty();
		String hourlyForecastDescription = kmaHourlyForecast.getWeatherDescription();

		final TimeZone koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul");
		SunriseSunsetCalculator sunriseSunsetCalculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
				koreaTimeZone);
		Calendar calendar = Calendar.getInstance(koreaTimeZone);
		calendar.set(currentTime.getYear(), currentTime.getMonthValue() - 1, currentTime.getDayOfMonth(),
				currentTime.getHour(), currentTime.getMinute());
		Calendar sunRise = sunriseSunsetCalculator.getOfficialSunriseCalendarForDate(calendar);
		Calendar sunSet = sunriseSunsetCalculator.getOfficialSunsetCalendarForDate(calendar);

		currentConditionsDto.setCurrentTime(currentTime);
		currentConditionsDto.setWeatherDescription(getWeatherDescriptionWeb(currentPtyCode.isEmpty() ? hourlyForecastDescription : currentPtyCode));
		currentConditionsDto.setWeatherIcon(getWeatherIconImgWeb(currentPtyCode.isEmpty() ? hourlyForecastDescription : currentPtyCode,
				SunRiseSetUtil.isNight(calendar, sunRise, sunSet)));
		currentConditionsDto.setTemp(ValueUnits.convertTemperature(kmaCurrentConditions.getTemp(), tempUnit) + tempUnitStr);
		currentConditionsDto.setFeelsLikeTemp(ValueUnits.convertTemperature(kmaCurrentConditions.getFeelsLikeTemp(), tempUnit) + tempUnitStr);

		currentConditionsDto.setHumidity(kmaCurrentConditions.getHumidity());
		currentConditionsDto.setYesterdayTemp(kmaCurrentConditions.getYesterdayTemp());

		if (kmaCurrentConditions.getWindDirection() != null) {
			Integer windDirectionDegree = WindUtil.parseWindDirectionStrAsInt(kmaCurrentConditions.getWindDirection());
			currentConditionsDto.setWindDirectionDegree(windDirectionDegree);
			currentConditionsDto.setWindDirection(WindUtil.parseWindDirectionDegreeAsStr(context, windDirectionDegree.toString()));
		}
		if (kmaCurrentConditions.getWindSpeed() != null) {
			Double windSpeed = Double.parseDouble(kmaCurrentConditions.getWindSpeed());
			currentConditionsDto.setWindSpeed(ValueUnits.convertWindSpeed(windSpeed.toString(), windUnit) + MyApplication.VALUE_UNIT_OBJ.getWindUnitText());

			currentConditionsDto.setSimpleWindStrength(WindUtil.getSimpleWindSpeedDescription(windSpeed.toString()));
			currentConditionsDto.setWindStrength(WindUtil.getWindSpeedDescription(windSpeed.toString()));
		}

		if (currentPtyCode.isEmpty()) {
			currentPtyCode = "0";
		} else {
			currentPtyCode = convertPtyTextToCode(currentPtyCode);
		}
		currentConditionsDto.setPrecipitationType(getWeatherPtyIconDescription(currentPtyCode));

		if (!kmaCurrentConditions.getPrecipitationVolume().contains("-") && !kmaCurrentConditions.getPrecipitationVolume().contains("0.0")) {
			currentConditionsDto.setPrecipitationVolume(kmaCurrentConditions.getPrecipitationVolume());
		}

		return currentConditionsDto;
	}

	public static ZoneId getZoneId() {
		return ZoneId.of("Asia/Seoul");
	}
}