package com.androidol.events;

import java.util.List;

import android.os.Handler;

public class ProtocolEvents extends Events {	
	// ===========================================================
	// Event Types
	// ===========================================================
	
	public static final int READ_SUCCESS		= 30000;
	public static final int READ_FAILURE		= 30001;

	// ===========================================================
	// Declare a list of supported event type
	// ===========================================================
	public final int[] EVENTS_LIST = {
		READ_SUCCESS,
		READ_FAILURE
	};
	
	// ===========================================================
	// Listeners/Callbacks
	// ===========================================================		
	
	protected List<Handler> readSuccessCallbacks = null;
	protected List<Handler> readFailureCallbacks = null;
		
	/**
	 * API Method: getEventList
	 * 
	 * @return
	 */
	public int[] getEventList() {
		return EVENTS_LIST;
	}
}
