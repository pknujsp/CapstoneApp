package com.lifedawn.capstoneapp.map.findroute;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.databinding.FragmentMapAppUrlSchemeBinding;
import com.lifedawn.capstoneapp.model.firestore.PlaceDto;

import java.util.List;

public class MapAppUrlSchemeFragment extends DialogFragment {
	private FragmentMapAppUrlSchemeBinding binding;
	private String appName;
	private PlaceDto start;
	private PlaceDto end;
	private Bundle bundle;

	private String kakaoScheme;
	private String naverScheme;

	private enum MapApp {
		NAVER, KAKAO
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
		} else {
			bundle = getArguments();
		}

		start = (PlaceDto) bundle.getSerializable("start");
		end = (PlaceDto) bundle.getSerializable("end");
		String packageName = "com.lifedawn.capstoneapp";

		PackageManager packageManager = getContext().getPackageManager();
		ApplicationInfo appInfo = null;
		try {
			appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		appName = packageManager.getApplicationLabel(appInfo).toString();

		kakaoScheme = "kakaomap://route?" +
				"sp=" + start.getLatitude() + "," + start.getLongitude() +
				"&ep=" + end.getLatitude() + "," + end.getLongitude() +
				"&by=PUBLICTRANSIT";

		naverScheme = "nmap://route/public" +
				"?slat=" + start.getLatitude() +
				"&slng=" + start.getLongitude() +
				"&sname=" + start.getAddressName() +
				"&dlat=" + end.getLatitude() +
				"&dlng=" + end.getLongitude() +
				"&dname=" + end.getAddressName() +
				"&appname=" + appName;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentMapAppUrlSchemeBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.navermap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!open(MapApp.NAVER)) {
					Toast.makeText(getContext(), R.string.not_installed_app, Toast.LENGTH_SHORT).show();
				}
			}
		});

		binding.kakaomap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!open(MapApp.KAKAO)) {
					Toast.makeText(getContext(), R.string.not_installed_app, Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private boolean open(MapApp mapApp) {
		if (mapApp == MapApp.NAVER) {
			final String url = "nmap://actionPath?parameter=value&appname={" + appName + "}";

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addCategory(Intent.CATEGORY_BROWSABLE);

			List<ResolveInfo> list = getContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (list == null || list.isEmpty()) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")));
				return false;
			} else {
				startActivity(intent);
				return true;
			}
		}

		return false;
	}
}