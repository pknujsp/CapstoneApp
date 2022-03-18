package com.lifedawn.capstoneapp.retrofits;

import retrofit2.Response;

public abstract class JsonDownloader {
	public abstract void onResponseResult(Response<?> response, Object responseObj, String responseText);

	public abstract void onResponseResult(Throwable t);

}