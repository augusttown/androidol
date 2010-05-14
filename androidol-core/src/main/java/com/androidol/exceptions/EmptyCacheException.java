package com.androidol.exceptions;

public class EmptyCacheException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public EmptyCacheException() {
		super();
	}

	public EmptyCacheException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public EmptyCacheException(String detailMessage) {
		super(detailMessage);
	}

	public EmptyCacheException(Throwable throwable) {
		super(throwable);
	}
}
