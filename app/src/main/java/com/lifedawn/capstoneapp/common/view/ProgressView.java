package com.lifedawn.capstoneapp.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lifedawn.capstoneapp.databinding.ViewProgressBinding;

import org.jetbrains.annotations.NotNull;

public class ProgressView extends FrameLayout {
	private ViewProgressBinding binding;
	private View contentView;
	private boolean succeed;

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ProgressView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();

	}

	public ProgressView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();

	}

	public ProgressView(@NonNull @NotNull Context context) {
		super(context);
		init();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}


	private void init() {
		binding = ViewProgressBinding.inflate(LayoutInflater.from(getContext()), this, true);
	}

	public void setContentView(View contentView) {
		this.contentView = contentView;
		onSuccessful();
	}

	public void onSuccessful() {
		succeed = true;
		contentView.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
	}

	public void onFailed(@NonNull String text) {
		succeed = false;
		contentView.setVisibility(View.GONE);
		binding.status.setText(text);
		binding.status.setVisibility(VISIBLE);
		binding.progressCircular.setVisibility(GONE);
		setVisibility(View.VISIBLE);
	}

	public void onStarted(@Nullable String text) {
		succeed = false;
		contentView.setVisibility(View.GONE);
		binding.status.setVisibility(text == null ? GONE : VISIBLE);
		if (text != null) {
			binding.status.setText(text);
		}
		binding.progressCircular.setVisibility(VISIBLE);
		setVisibility(View.VISIBLE);
	}


	public boolean isSuccess() {
		return succeed;
	}

	public void setTextColor(int color) {
		binding.status.setTextColor(color);
	}
}

