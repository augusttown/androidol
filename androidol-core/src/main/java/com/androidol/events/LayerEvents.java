package com.androidol.events;

import java.util.List;
import java.util.Vector;

//import com.esri.android.openlayers.util.Util;

import android.os.Handler;
//import android.os.Message;

public class LayerEvents extends Events {

	// ===========================================================
	// Event Types
	// ===========================================================
	
	public static final int LOAD_START			= 20000;
	public static final int LOAD_END 			= 20001;
	public static final int LOAD_CANCELED 		= 20002;
	public static final int VISIBILITY_CHANGED 	= 20003;
	public static final int MOVE_END 			= 20004;
	public static final int TILE_LOADED 		= 20005;
	
	public static final int FEATURE_ADDED 		= 20006;
	public static final int FEATURES_ADDED 		= 20007;
	
	// ===========================================================
	// Declare a list of supported event type
	// ===========================================================
	public final int[] EVENTS_LIST = {
		LOAD_START,
		LOAD_END,
		LOAD_CANCELED,
		VISIBILITY_CHANGED,
		MOVE_END,
		TILE_LOADED,
		FEATURE_ADDED,
		FEATURES_ADDED
	};
	
	// ===========================================================
	// Listeners/Callbacks
	// ===========================================================	
	
	protected List<Handler> loadStartCallbacks = null;
	protected List<Handler> loadEndCallbacks = null;
	protected List<Handler> loadCanceledCallbacks = null;
	protected List<Handler> visibilityChangedCallbacks = null;	
	protected List<Handler> moveEndCallbacks = null;
	protected List<Handler> tileLoadedCallbacks = null;
	protected List<Handler> featureAddedCallbacks = null;
	protected List<Handler> featuresAddedCallbacks = null;
	
	public LayerEvents() {
		super();		
		loadStartCallbacks = new Vector<Handler>();
		loadEndCallbacks = new Vector<Handler>();
		loadCanceledCallbacks = new Vector<Handler>();
		visibilityChangedCallbacks = new Vector<Handler>();
		moveEndCallbacks = new Vector<Handler>();
		tileLoadedCallbacks = new Vector<Handler>(); 
		featureAddedCallbacks = new Vector<Handler>();
		featuresAddedCallbacks = new Vector<Handler>();
		
		this.listeners.put(new Integer(LOAD_START), loadStartCallbacks);
		this.listeners.put(new Integer(LOAD_END), loadEndCallbacks);
		this.listeners.put(new Integer(LOAD_CANCELED), loadCanceledCallbacks);
		this.listeners.put(new Integer(VISIBILITY_CHANGED), visibilityChangedCallbacks);
		this.listeners.put(new Integer(MOVE_END), moveEndCallbacks);
		this.listeners.put(new Integer(TILE_LOADED), tileLoadedCallbacks);
		this.listeners.put(new Integer(FEATURE_ADDED), featureAddedCallbacks);
		this.listeners.put(new Integer(FEATURES_ADDED), featuresAddedCallbacks);
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
