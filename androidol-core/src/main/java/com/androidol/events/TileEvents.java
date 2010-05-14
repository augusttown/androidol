package com.androidol.events;

import java.util.List;
import java.util.Vector;

import android.os.Handler;

public class TileEvents extends Events {
	
	// ===========================================================
	// Events
	// ===========================================================
		
	public static final int HTTP_LOAD_FAILURE			=	2000;
	public static final int HTTP_LOAD_SUCCESS			=	2001;
	public static final int HTTP_TILE_INACCESSIBLE		=	2002;
	
	public static final int FS_LOAD_FAILURE				=	1000;
	public static final int FS_LOAD_SUCCESS				=	1001;
	public static final int FS_TILE_CORRUPTED			=	1002;
		
	public static final int MEM_LOAD_FAILURE			=	3000;
	public static final int MEM_LOAD_SUCCESS			=	3001;
	
	// ===========================================================
	// Declare a list of supported event type
	// ===========================================================
	
	public final int[] EVENTS_LIST = {
		HTTP_LOAD_SUCCESS,
		HTTP_LOAD_FAILURE,
		HTTP_TILE_INACCESSIBLE,
		FS_LOAD_SUCCESS,
		FS_LOAD_FAILURE,
		FS_TILE_CORRUPTED,
		MEM_LOAD_SUCCESS,
		MEM_LOAD_FAILURE
	};
	
	// ===========================================================
	// Listeners/Callbacks
	// ===========================================================		
	
	protected List<Handler> httpLoadSuccessCallbacks = null;
	protected List<Handler> httpLoadFailureCallbacks = null;
	protected List<Handler> fsLoadSuccessCallbacks = null;
	protected List<Handler> fsLoadFailureCallbacks = null;
	protected List<Handler> memLoadSuccessCallbacks = null;
	protected List<Handler> memLoadFailureCallbacks = null;
	
	/**
	 * Constructor
	 */
	public TileEvents() {
		super();
		
		httpLoadSuccessCallbacks = new Vector<Handler>();				
		httpLoadFailureCallbacks = new Vector<Handler>();		
		fsLoadSuccessCallbacks = new Vector<Handler>();
		fsLoadFailureCallbacks = new Vector<Handler>();
		memLoadSuccessCallbacks = new Vector<Handler>();
		memLoadFailureCallbacks = new Vector<Handler>();
		
		this.listeners.put(new Integer(HTTP_LOAD_SUCCESS), httpLoadSuccessCallbacks);
		this.listeners.put(new Integer(HTTP_LOAD_FAILURE), httpLoadFailureCallbacks);
		this.listeners.put(new Integer(FS_LOAD_SUCCESS), fsLoadSuccessCallbacks);
		this.listeners.put(new Integer(FS_LOAD_FAILURE), fsLoadFailureCallbacks);
		this.listeners.put(new Integer(MEM_LOAD_SUCCESS), memLoadSuccessCallbacks);
		this.listeners.put(new Integer(MEM_LOAD_FAILURE), memLoadFailureCallbacks);
	}
	
	/**
	 * API Method: getEventList
	 * 
	 * @return
	 */
	public int[] getEventList() {
		return EVENTS_LIST;
	}
	
	
}
