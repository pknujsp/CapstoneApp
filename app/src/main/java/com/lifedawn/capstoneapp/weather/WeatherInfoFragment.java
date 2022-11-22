package com.lifedawn.capstoneapp.weather;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
import com.lifedawn.capstoneapp.model.firestore.PlaceDto;
import com.lifedawn.capstoneapp.retrofits.MultipleRestApiDownloader;
import com.lifedawn.capstoneapp.weather.customview.DateView;
import com.lifedawn.capstoneapp.weather.customview.DetailDoubleTemperatureView;
import com.lifedawn.capstoneapp.weather.customview.DetailSingleTemperatureView;
import com.lifedawn.capstoneapp.weather.model.CurrentConditionsDto;
import com.lifedawn.capstoneapp.weather.model.DailyForecastDto;
import com.lifedawn.capstoneapp.weather.model.HourlyForecastDto;
import com.lifedawn.capstoneapp.weather.request.WeatherRequest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class WeatherInfoFragment extends DialogFragment {
	private FragmentWeatherInfoBinding binding;
	private PlaceDto placeDto;
	private Bundle bundle;
	private ZonedDateTime promiseDateTime;
	private DateView hourlyForecastDateView;
	private MultipleRestApiDownloader multipleRestApiDownloader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			bundle = getArguments();
		} else {
			bundle = savedInstanceState;
		}

		placeDto = (PlaceDto) bundle.getSerializable("locationDto");
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

		binding.locationName.setText(placeDto.getAddressName());
		binding.progressView.setContentView(binding.weatherLayout);
		binding.progressView.onStarted(new String(placeDto.getAddressName() + "\n" + getString(R.string.loading_weather_data)));
		binding.todayandpromise.todayCurrentWeather.rainyPosibility.setVisibility(View.GONE);

		binding.todayandpromise.promiseDayCurrentWeather.rainyPosibility.setVisibility(View.GONE);
		binding.todayandpromise.promiseDayCurrentWeather.precipitationVolume.setVisibility(View.GONE);

		final Double latitude = Double.parseDouble(placeDto.getLatitude());
		final Double longitude = Double.parseDouble(placeDto.getLongitude());

		if (WeatherResponseData.getWeatherResponse(latitude, longitude) != null) {
			onResultWeather(WeatherResponseData.getWeatherResponse(latitude, longitude));
		} else {
			refreshData();
		}

		binding.hourlyForecastScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (hourlyForecastDateView != null) {
					hourlyForecastDateView.reDraw(scrollX);
				}
			}
		});

		binding.updateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshData();
			}
		});

		binding.closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	private void refreshData() {
		binding.progressView.onStarted(new String(placeDto.getAddressName() + "\n" + getString(R.string.loading_weather_data)));
		final Double latitude = Double.parseDouble(placeDto.getLatitude());
		final Double longitude = Double.parseDouble(placeDto.getLongitude());

		multipleRestApiDownloader = WeatherRequest.requestWeatherData(getContext(),
				latitude, longitude,
				new BackgroundCallback<WeatherRequest.WeatherResponseResult>() {
					@Override
					public void onResultSuccessful(WeatherRequest.WeatherResponseResult weatherResponseResult) {
						WeatherResponseData.addWeatherResponse(getContext(), weatherResponseResult.getLatitude(),
								weatherResponseResult.getLongitude(), weatherResponseResult.getMultipleRestApiDownloader());
						onResultWeather(WeatherResponseData.getWeatherResponse(latitude, longitude));
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
	}

	@Override
	public void onResume() {
		super.onResume();
		Dialog dialog = getDialog();

		Rect rect = new Rect();
		dialog.getWindow().getWindowManager().getDefaultDisplay().getRectSize(rect);

		WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
		layoutParams.width = (int) (rect.width() * 0.95);
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
		if (multipleRestApiDownloader != null) {
			if (multipleRestApiDownloader.getCallMap().size() > 0) {
				multipleRestApiDownloader.cancel();
			}
		}
		super.onDestroy();
	}

	private void onResultWeather(WeatherResponseData.WeatherResponseObj weatherResponseObj) {
		if (getActivity() == null) {
			return;
		}

		if (weatherResponseObj != null) {
			final CurrentConditionsDto currentConditionsDto = weatherResponseObj.currentConditionsDto;
			final List<HourlyForecastDto> hourlyForecastDtoList = weatherResponseObj.hourlyForecastDtoList;
			final List<DailyForecastDto> dailyForecastDtoList = weatherResponseObj.dailyForecastDtoList;

			final WeatherResponseData.PromiseWeatherForecast promiseWeatherForecast =
					WeatherResponseData.getPromiseDayHourlyForecast(promiseDateTime, hourlyForecastDtoList, dailyForecastDtoList);

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					binding.todayandpromise.todayCurrentWeather.amPm.setVisibility(View.GONE);
					binding.todayandpromise.todayCurrentWeather.leftWeatherIcon.setImageResource(currentConditionsDto.getWeatherIcon());
					binding.todayandpromise.todayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);
					binding.todayandpromise.todayCurrentWeather.weatherDescription.setText(currentConditionsDto.getWeatherDescription());
					binding.todayandpromise.todayCurrentWeather.temperature.setText(currentConditionsDto.getTemp());

					DateTimeFormatter lastUpdateDateTimeFormatter = DateTimeFormatter.ofPattern("M.d E a hh:mm");
					binding.lastUpdateDateTime.setText(weatherResponseObj.multipleRestApiDownloader.getRequestDateTime().format(lastUpdateDateTimeFormatter));

					setHourlyForecastView(hourlyForecastDtoList);
					setDailyForecastView(dailyForecastDtoList);

					if (currentConditionsDto.isHasPrecipitationVolume()) {
						binding.todayandpromise.todayCurrentWeather.precipitationVolume.setText(currentConditionsDto.getPrecipitationVolume());
					} else {
						binding.todayandpromise.todayCurrentWeather.precipitationVolume.setVisibility(View.GONE);
					}

					if (promiseWeatherForecast.dailyForecastDto == null && promiseWeatherForecast.hourlyForecastDto == null) {
						binding.todayandpromise.promiseDayCurrentWeather.getRoot().setVisibility(View.GONE);
					} else {
						if (promiseWeatherForecast.weatherDataType == WeatherDataType.hourlyForecast) {
							binding.todayandpromise.promiseDayCurrentWeather.amPm.setVisibility(View.GONE);
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d E HH시 예상");
							binding.todayandpromise.promiseDayCurrentWeather.title.setText(promiseWeatherForecast.hourlyForecastDto.getHours().format(dateTimeFormatter));

							binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

							binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(promiseWeatherForecast.hourlyForecastDto.getWeatherIcon());
							binding.todayandpromise.promiseDayCurrentWeather.temperature.setText(promiseWeatherForecast.hourlyForecastDto.getTemp());
							binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(promiseWeatherForecast.hourlyForecastDto.getWeatherDescription());
						} else {
							binding.todayandpromise.promiseDayCurrentWeather.temperature.setText(new String(promiseWeatherForecast.dailyForecastDto.getMinTemp() + " / " +
									promiseWeatherForecast.dailyForecastDto.getMaxTemp()));
							binding.todayandpromise.promiseDayCurrentWeather.title.setText(R.string.promiseDayCurrentWeather);

							if (promiseWeatherForecast.dailyForecastDto.isSingle()) {
								binding.todayandpromise.promiseDayCurrentWeather.amPm.setVisibility(View.GONE);
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.GONE);

								binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(promiseWeatherForecast.dailyForecastDto.getSingleValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(promiseWeatherForecast.dailyForecastDto.getSingleValues().getWeatherDescription());
							} else {
								binding.todayandpromise.promiseDayCurrentWeather.amPm.setText("오전 | 오후");

								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setVisibility(View.VISIBLE);

								binding.todayandpromise.promiseDayCurrentWeather.leftWeatherIcon.setImageResource(promiseWeatherForecast.dailyForecastDto.getAmValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.rightWeatherIcon.setImageResource(promiseWeatherForecast.dailyForecastDto.getPmValues().getWeatherIcon());
								binding.todayandpromise.promiseDayCurrentWeather.weatherDescription.setText(new String(promiseWeatherForecast.dailyForecastDto.getAmValues().getWeatherDescription()
										+ " / " + promiseWeatherForecast.dailyForecastDto.getPmValues().getWeatherDescription()));
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
		binding.hourlyForecastView.removeAllViews();
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
		binding.dailyForecastView.removeAllViews();

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