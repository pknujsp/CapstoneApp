package com.lifedawn.capstoneapp.common.classes;

public class AlreadySyncingException extends Exception{
	public AlreadySyncingException() {
		super();
	}

	public AlreadySyncingException(String message) {
		super(message);
	}

	public AlreadySyncingException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadySyncingException(Throwable cause) {
		super(cause);
	}

	protected AlreadySyncingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
