package com.lifedawn.capstoneapp.appsettings.notification;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.slider.Slider;
import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.common.constants.SharedPreferenceConstant;
import com.lifedawn.capstoneapp.databinding.FragmentDefaultNotificationSettingsBinding;

import org.jetbrains.annotations.NotNull;


public class DefaultNotificationSettingsFragment extends Fragment {
	private FragmentDefaultNotificationSettingsBinding binding;
	private boolean initializing = true;
	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentDefaultNotificationSettingsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.toolbar.fragmentTitle.setText(R.string.notification_basic_setting);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStack();
			}
		});

		binding.soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				binding.soundLayout.setVisibility(b ? View.VISIBLE : View.GONE);

				if (!initializing) {
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
					editor.putBoolean(SharedPreferenceConstant.REMINDER_SOUND_ON_OFF.getVal(), b).apply();
				}
			}
		});

		binding.vibrationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (!initializing) {
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
					editor.putBoolean(SharedPreferenceConstant.REMINDER_VIBRATION.getVal(), b).apply();
				}
			}
		});

		binding.wakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (!initializing) {
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
					editor.putBoolean(SharedPreferenceConstant.REMINDER_WAKE.getVal(), b).apply();
				}
			}
		});

		binding.soundName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

				ringtoneLauncher.launch(intent);
			}
		});

		binding.alarmSoundVolume.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
			@Override
			public void onStartTrackingTouch(@NonNull Slider slider) {

			}

			@Override
			public void onStopTrackingTouch(@NonNull Slider slider) {
				if (!initializing) {
					SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
					editor.putInt(SharedPreferenceConstant.REMINDER_SOUND_VOLUME.getVal(), (int) slider.getValue()).apply();
				}
			}
		});


		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String soundUri = sharedPreferences.getString(SharedPreferenceConstant.REMINDER_SOUND_URI.getVal(), "");

		if (!soundUri.isEmpty()) {
			Ringtone ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(soundUri));
			binding.soundName.setText(ringtone.getTitle(getContext()));
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		boolean sound = sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_SOUND_ON_OFF.getVal(), false);
		boolean vibration = sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_VIBRATION.getVal(), false);
		boolean wake = sharedPreferences.getBoolean(SharedPreferenceConstant.REMINDER_WAKE.getVal(), false);
		int soundVolume = sharedPreferences.getInt(SharedPreferenceConstant.REMINDER_SOUND_VOLUME.getVal(), 75);

		binding.soundSwitch.setChecked(sound);
		binding.vibrationSwitch.setChecked(vibration);
		binding.wakeSwitch.setChecked(wake);
		binding.alarmSoundVolume.setValue(soundVolume);

		binding.soundLayout.setVisibility(sound ? View.VISIBLE : View.GONE);

		initializing = false;
	}

	private final ActivityResultLauncher<Intent> ringtoneLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (result.getData() == null) {
						return;
					}
					Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					if (uri != null) {
						binding.soundLayout.setVisibility(View.VISIBLE);
						Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
						binding.soundName.setText(ringtone.getTitle(getContext()));

						SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
						editor.putString(SharedPreferenceConstant.REMINDER_SOUND_URI.getVal(), uri.toString()).apply();
					}
				}
			});

}