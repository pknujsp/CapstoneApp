package com.lifedawn.capstoneapp.kakao.web;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lifedawn.capstoneapp.R;
import com.lifedawn.capstoneapp.databinding.FragmentPlaceInfoWebBinding;


public class PlaceInfoWebFragment extends Fragment {
	public static final String WEB_JSON_URL = "https://place.map.kakao.com/main/v/";
	public static final String WEB_URL = "https://place.map.kakao.com/m/";

	private FragmentPlaceInfoWebBinding binding;
	private String placeId;
	private Bundle bundle;

	public final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (binding.webview.canGoBack()) {
				binding.webview.goBack();
			} else {
				getParentFragmentManager().popBackStack();
			}
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
		bundle = getArguments() != null ? getArguments() : savedInstanceState;
		placeId = bundle.getString("placeId");
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		binding = FragmentPlaceInfoWebBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWebView();
	}

	@Override
	public void onDestroy() {
		onBackPressedCallback.remove();
		super.onDestroy();
	}

	private boolean webCanGoBack() {
		return binding.webview.canGoBack();
	}

	private void webGoBack() {
		binding.webview.goBack();
	}

	// 웹뷰 초기화 함수
	private void initWebView() {
		// 1. 웹뷰클라이언트 연결 (로딩 시작/끝 받아오기)
		binding.webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				binding.progressIndicator.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				binding.progressIndicator.setVisibility(View.GONE);
			}

		});

		// 2. WebSettings: 웹뷰의 각종 설정을 정할 수 있다.
		WebSettings ws = binding.webview.getSettings();
		ws.setDomStorageEnabled(true);
		ws.setJavaScriptEnabled(true); // 자바스크립트 사용 허가
		// 3. 웹페이지 호출
		binding.webview.loadUrl(WEB_URL + placeId);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}
}