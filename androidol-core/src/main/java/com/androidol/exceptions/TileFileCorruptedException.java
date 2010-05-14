package com.androidol.exceptions;

import java.io.IOException;

public class TileFileCorruptedException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public TileFileCorruptedException() {
		super();
	}
	
	public TileFileCorruptedException(String message) {
		super(message);
	}
}
