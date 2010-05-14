package com.androidol.exceptions;

public class InvalidGeometryTypeException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidGeometryTypeException() {
		super();
	}
	
	public InvalidGeometryTypeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public InvalidGeometryTypeException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidGeometryTypeException(Throwable throwable) {
		super(throwable);
	}
}
