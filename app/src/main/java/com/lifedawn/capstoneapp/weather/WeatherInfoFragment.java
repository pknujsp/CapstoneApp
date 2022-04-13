package com.lifedawn.capstoneapp.weather;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.interfaces.BackgroundCallback;
import com.lifedawn.capstoneapp.databinding.FragmentWeatherInfoBinding;
import com.lifedawn.capstoneapp.main.MyApplication;
import com.lifedawn.capstoneapp.map.LocationDto;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.retrofits.RetrofitClient;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaCurrentConditions;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaDailyForecast;
import com.lifedawn.capstoneapp.retrofits.response.kma.KmaHourlyForecast;
import com.lifedawn.capstoneapp.weather.customview.DateView;
import com.lifedawn.capstoneapp.weather.customview.DetailDoubleTemperatureView;
import com.lifedawn.capstoneapp.weather.customview.DetailSingleTemperatureView;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.request.WeatherRequest;
import com.lifedawn.capstoneapp.weather.response.KmaResponseProcessor;
import com.lifedawn.capstoneapp.weather.util.WeatherUtil;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class WeatherInfoFragment extends DialogFragment {
	private FragmentWeatherInfoBinding binding;
	private LocationDto locationDto;
	private Bundle bundle;
	private ZonedDateTime promiseDateTime;
	private static MultipleRestApiDownloader weatherMultipleRestApiDownloader;

	private DateView hourlyForecastDateView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bundle = getArguments();
		} else {
			bundle = savedInstanceState;
		}

		locationDto = (LocationDto) bundle.getSerializable("locationDto");
		promiseDateTime = (ZonedDateTime) bundle.getSerializable("promiseDateTime");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherInfoBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.locationName.setText(locationDto.getAddressName());
		binding.progressView.setContentView(binding.weatherLayout);
		binding.progressView.onStarted(new String(locationDto.getAddressName() + "\n" + getString(R.string.loading_weather_data)));
		binding.todayandpromise.todayCurrentWeather.rainyPosibility.setVisibility(View.GONE);

		binding.todayandpromise.promiseDayCurrentWeather.rainyPosibility.setVisibility(View.GONE);
		binding.todayandpromise.promiseDayCurrentWeather.precipitationVolume.setVisibility(View.GONE);

		weatherMultipleRestApiDownloader = WeatherRequest.requestWeatherData(getContext(),
				Double.parseDouble(locationDto.getLatitude()),
				Double.parseDouble(locationDto.getLongitude()),
				new BackgroundCallback<WeatherRequest.WeatherResponseResult>() {
					@Override
					public void onResultSuccessful(WeatherRequest.WeatherResponseResult e) {
						onResultWeather(e);
					}

					@Override
					public void onResultFailed(Exception e) {
						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									binding.progressView.onFailed(getString(R.string.failed_loading_weather_data));
								}
							});
						}
					}
				});

		binding.hourlyForecastScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (hourlyForecastDateView != null) {
					hourlyForecastDateView.reDraw(scrollX);
				}
			}
		});

		binding.closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		Dialog dialog = getDialog();

		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);

		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.9);
		layoutParams.height = (int) (rect.height() * 0.8);

		dialog.getWindow().setAttributes(layoutParams);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	private void onResultWeather(WeatherRequest.WeatherResponseResult weatherResponseResult) {
		if (getActivity() == null) {
			return;
		}

		Double latitude = weatherResponseResult.getLatitude();
		Double longitude = weatherResponseResult.getLongitude();

		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap =
				weatherMultipleRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> arrayMap = responseMap.get(WeatherProviderType.KMA_WEB);

		if (arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).isSuccessful()) {

			KmaCurrentConditions kmaCurrentConditions = (KmaCurrentConditions) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseObj();
			Object[] forecasts = (Object[]) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseObj();

			ArrayList<KmaHourlyForecast> kmaHourlyForecasts = (ArrayList<KmaHourlyForecast>) forecasts[0];
			ArrayList<KmaDailyForecast> kmaDailyForecasts = (ArrayList<KmaDailyForecast>) forecasts[1];

			final CurrentConditionsDto currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(getContext(),
					kmaCurrentConditions, kmaHourlyForecasts.get(0), latitude, longitude);

			final List<HourlyForecastDto> hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(getContext(),
					kmaHourlyForecasts, latitude, longitude);

			final List<DailyForecastDto> dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(kmaDailyForecasts);

			final WeatherDataType promiseWeatherDataType = WeatherUtil.findPromiseWeatherDataType(promiseDateTime.toLocalDateTime(),
					hourlyForecastDtoList,
					dailyForecastDtoList);

			HourlyForecastDto promiseDayHourlyDto = null;
			DailyForecastDto promiseDayDailyDto = null;

			if (promiseWeatherDataType != null) {
				if (promiseWeatherDataType == WeatherDataType.hourlyForecast) {
					promiseDayHourlyDto = WeatherUtil.getPromiseDayWeatherByHourly(promiseDateTime.toLocalDateTime(), hourlyForecastDtoList);
				} else {
					promiseDayDailyDto = WeatherUtil.getPromiseDayWeatherByDaily(promiseDateTime.toLocalDateTime(), dailyForecastDtoList);
				}
			}

			final HourlyForecastDto finalPromiseDayHourlyDto = promiseDayHourlyDto;
			final DailyForecastDto finalPromiseDayDailyDto = promiseDayDailyDto;

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					binding.todayandpromise.todayCurrentWeather.amPm.setVisibility(View.GONE);
					binding.todayandpromise.todayCurrentWeather.leftWeatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
					binding.todayandpromise.todayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);
					binding.todayandpromise.todayCurrentWeather.weatherDescription.setText(currentConditionsDto.getWeatherDescription());
					binding.todayandpromise.todayCurrentWeather.temperature.setText(currentConditionsDto.getTemp());

					DateTimeFormatter lastUpdateDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a hh:mm");
					binding.lastUpdateDateTime.setText(weatherMultipleRestApiDownloader.getRequestDateTime().format(lastUpdateDateTimeFormatter));

					setHourlyForecastView(hourlyForecastDtoList);
					setDailyForecastView(dailyForecastDtoList);

					if (currentConditionsDto.isHasPrecipitationVolume()) {
						binding.todayandpromise.todayCurrentWeather.precipitationVolume.setText(currentConditionsDto.getPrecipitationVolume());
					} else {
						binding.todayandpromise.todayCurrentWeather.precipitationVolume.setVisibility(View.GONE);
					}

					if (promiseWeatherDataType == null) {
						binding.todayandpromise.promiseDayCurrentWeather.getRoot().setVisibility(View.GONE);
					} else {
						if (finalPromiseDayHourlyDto != null) {
							binding.todayandpromise.promiseDayCurrentWeather.amPm.setVisibility(View.GONE);
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d E HH시 예상");
							binding.todayandpromise.promiseDayCurrentWeather.title.setText(finalPromiseDayHourlyDto.getHours().format(dateTimeFormatter));

							binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

							binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayHourlyDto.getWeatherIcon());
							binding.todayandpromise.promiseDayCurrentWeather.temperature.setText(finalPromiseDayHourlyDto.getTemp());
							binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayHourlyDto.getWeatherDescription());
						} else {
							binding.todayandpromise.promiseDayCurrentWeather.temperature.setText(new String(finalPromiseDayDailyDto.getMinTemp() + " / " +
									finalPromiseDayDailyDto.getMaxTemp()));
							binding.todayandpromise.promiseDayCurrentWeather.title.setText(R.string.promiseDayCurrentWeather);

							if (finalPromiseDayDailyDto.isSingle()) {
								binding.todayandpromise.promiseDayCurrentWeather.amPm.setVisibility(View.GONE);
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

								binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayDailyDto.getSingleValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(finalPromiseDayHourlyDto.getWeatherDescription());
							} else {
								binding.todayandpromise.promiseDayCurrentWeather.amPm.setText("오전 | 오후");

								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.VISIBLE);

								binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(finalPromiseDayDailyDto.getAmValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setImageResource(finalPromiseDayDailyDto.getPmValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(new String(finalPromiseDayDailyDto.getAmValues().getWeatherDescription()
										+ " / " + finalPromiseDayDailyDto.getPmValues().getWeatherDescription()));
							}
						}
					}

					binding.progressView.onSuccessful();
				}
			});
		} else {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					binding.progressView.onFailed(getString(R.string.failed_loading_weather_data));
				}
			});
		}
	}

	private void setHourlyForecastView(List<HourlyForecastDto> hourlyForecastDtoList) {
		Context context = getContext();

		final int columnCount = hourlyForecastDtoList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.columnWidthInHourlyForecast);
		final int viewWidth = columnCount * columnWidth;

		hourlyForecastDateView = new DateView(context, viewWidth, columnWidth);

		List<Integer> tempList = new ArrayList<>();
		List<ZonedDateTime> dateTimeList = new ArrayList<>();

		final String mm = "mm";
		final String cm = "cm";
		final String degree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

		boolean haveSnow = false;
		boolean haveRain = false;

		for (HourlyForecastDto item : hourlyForecastDtoList) {
			if (item.isHasSnow()) {
				if (!haveSnow) {
					haveSnow = true;
				}
			}
			if (item.isHasRain()) {
				if (!haveRain) {
					haveRain = true;
				}
			}
		}

		LayoutInflater layoutInflater = getLayoutInflater();
		LinearLayout commonViewLayout = new LinearLayout(getContext());
		commonViewLayout.setOrientation(LinearLayout.HORIZONTAL);

		View itemView = null;
		LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(columnWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

		for (HourlyForecastDto item : hourlyForecastDtoList) {
			itemView = layoutInflater.inflate(R.layout.itemview_forecast, null);
			itemView.setLayoutParams(itemLayoutParams);

			itemView.findViewById(R.id.rightWeatherIcon).setVisibility(View.GONE);
			dateTimeList.add(item.getHours());

			((TextView) itemView.findViewById(R.id.dateTime)).setText(String.valueOf(item.getHours().getHour()));

			if (haveRain) {
				((TextView) itemView.findViewById(R.id.rainVolume)).setText(item.getRainVolume().replace(mm, "").replace(cm, ""));
			} else {
				itemView.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			}
			if (haveSnow) {
				((TextView) itemView.findViewById(R.id.snowVolume)).setText(item.getSnowVolume().replace(mm, "").replace(cm, ""));
			} else {
				itemView.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);
			}

			((TextView) itemView.findViewById(R.id.pop)).setText(item.getPop());
			((ImageView) itemView.findViewById(R.id.leftWeatherIcon)).setImageResource(item.getWeatherIcon());

			tempList.add(Integer.parseInt(item.getTemp().replace(degree, "")));
			commonViewLayout.addView(itemView);
		}

		hourlyForecastDateView.init(dateTimeList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		binding.hourlyForecastView.addView(hourlyForecastDateView, rowLayoutParams);

		DetailSingleTemperatureView tempRow = new DetailSingleTemperatureView(context, tempList);
		tempRow.setLineColor(Color.BLACK);
		tempRow.setCircleColor(Color.DKGRAY);
		tempRow.setTempTextSizeSp(16);

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				(int) getResources().getDimension(R.dimen.singleTemperatureRowHeightInCOMMON));

		binding.hourlyForecastView.addView(commonViewLayout);
		binding.hourlyForecastView.addView(tempRow, tempRowLayoutParams);
	}

	private void setDailyForecastView(List<DailyForecastDto> dailyForecastDtoList) {
		//시각 --------------------------------------------------------------------------
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE");
		//날씨 아이콘
		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();

		final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		final String mm = "mm";
		final String cm = "cm";

		boolean haveSnow = false;
		boolean haveRain = false;

		float rainVolume = 0f;
		float snowVolume = 0f;

		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		for (DailyForecastDto item : dailyForecastDtoList) {
			rainVolume = 0f;
			snowVolume = 0f;

			if (item.isSingle()) {
				if (item.getSingleValues().getRainVolume() != null) {
					rainVolume = Float.parseFloat(item.getSingleValues().getRainVolume().replace(mm, "").replace(cm, ""));
				}
				if (item.getSingleValues().getSnowVolume() != null) {
					snowVolume = Float.parseFloat(item.getSingleValues().getSnowVolume().replace(mm, "").replace(cm, ""));
				}
			} else {
				if (item.getAmValues().getRainVolume() != null || item.getPmValues().getRainVolume() != null) {
					rainVolume = Float.parseFloat(item.getAmValues().getRainVolume().replace(mm, "").replace(cm, ""))
							+ Float.parseFloat(item.getPmValues().getRainVolume().replace(mm, "").replace(cm, ""));
				}
				if (item.getAmValues().getSnowVolume() != null || item.getPmValues().getSnowVolume() != null) {
					snowVolume = Float.parseFloat(item.getAmValues().getSnowVolume().replace(mm, "").replace(cm, ""))
							+ Float.parseFloat(item.getPmValues().getSnowVolume().replace(mm, "").replace(cm, ""));
				}
			}

			rainVolumeList.add(String.format(Locale.getDefault(), rainVolume > 0f ? "%.2f" : "%.1f", rainVolume));
			snowVolumeList.add(String.format(Locale.getDefault(), snowVolume > 0f ? "%.2f" : "%.1f", snowVolume));

			if (!haveRain) {
				if (rainVolume > 0f) {
					haveRain = true;
				}
			}
			if (!haveSnow) {
				if (snowVolume > 0f) {
					haveSnow = true;
				}
			}
		}

		LayoutInflater layoutInflater = getLayoutInflater();
		LinearLayout commonViewLayout = new LinearLayout(getContext());
		commonViewLayout.setOrientation(LinearLayout.HORIZONTAL);

		View itemView = null;
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInSDailyOwm);
		LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(columnWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

		int index = 0;
		for (DailyForecastDto item : dailyForecastDtoList) {
			itemView = layoutInflater.inflate(R.layout.itemview_forecast, null);
			itemView.setLayoutParams(itemLayoutParams);

			((TextView) itemView.findViewById(R.id.dateTime)).setText(item.getDate().format(dateTimeFormatter));
			minTempList.add(Integer.parseInt(item.getMinTemp().replace(tempDegree, "")));
			maxTempList.add(Integer.parseInt(item.getMaxTemp().replace(tempDegree, "")));

			if (item.isSingle()) {
				((TextView) itemView.findViewById(R.id.pop)).setText(item.getSingleValues().getPop());

				((ImageView) itemView.findViewById(R.id.leftWeatherIcon)).setImageResource(item.getSingleValues().getWeatherIcon());
				itemView.findViewById(R.id.rightWeatherIcon).setVisibility(View.GONE);
			} else {
				((TextView) itemView.findViewById(R.id.pop)).setText(new String(item.getAmValues().getPop() + "/" + item.getPmValues().getPop()));

				((ImageView) itemView.findViewById(R.id.leftWeatherIcon)).setImageResource(item.getAmValues().getWeatherIcon());
				((ImageView) itemView.findViewById(R.id.rightWeatherIcon)).setImageResource(item.getPmValues().getWeatherIcon());
			}

			if (haveRain) {
				((TextView) itemView.findViewById(R.id.rainVolume)).setText(rainVolumeList.get(index));
			} else {
				itemView.findViewById(R.id.rainVolumeLayout).setVisibility(View.GONE);
			}

			if (haveSnow) {
				((TextView) itemView.findViewById(R.id.snowVolume)).setText(snowVolumeList.get(index));
			} else {
				itemView.findViewById(R.id.snowVolumeLayout).setVisibility(View.GONE);
			}

			index++;
			commonViewLayout.addView(itemView);
		}

		binding.dailyForecastView.addView(commonViewLayout);

		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), minTempList, maxTempList);

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				(int) getResources().getDimension(R.dimen.doubleTemperatureRowHeight));
		tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.tempTopMargin);
		binding.dailyForecastView.addView(tempRow, tempRowLayoutParams);

	}
}