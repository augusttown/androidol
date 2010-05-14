package com.androidol.events;

import java.util.List;
import java.util.Vector;

//import com.esri.android.openlayers.layer.Layer;
//import com.esri.android.openlayers.util.Util;

import android.os.Handler;
//import android.os.Message;

public class MapEvents extends Events {
	
	// ===========================================================
	// Events related to layers
	// ===========================================================
	
	public static final int LAYER_ADDED 		= 10000;
	public static final int LAYER_REMOVED 		= 10001;
	public static final int LAYER_CHANGED 		= 10002;
	public static final int BASELAYER_CHANGED 	= 10003;
	
	// ===========================================================
	// Events related to pan
	// ===========================================================	
	
	public static final int MOVE_START 			= 10004;
	public static final int MOVE 				= 10005;
	public static final int MOVE_END 			= 10006;
	
	// ===========================================================
	// Events related to zoom
	// ===========================================================	
	public static final int ZOOM_END 			= 10007;
	
	// ===========================================================
	// Declare a list of supported event type
	// ===========================================================
	public final int[] EVENTS_LIST = {
		LAYER_ADDED,
		LAYER_REMOVED,
		LAYER_CHANGED,
		BASELAYER_CHANGED,		
		MOVE_START,
		MOVE,
		MOVE_END,
		ZOOM_END
	};

	// ===========================================================
	// Listeners/Callbacks
	// ===========================================================		
	
	protected List<Handler> layerAddedCallbacks = null;
	protected List<Handler> layerRemovedCallbacks = null;
	protected List<Handler> layerChangedCallbacks = null;
	protected List<Handler> baselayerChangedCallbacks = null;
	
	protected List<Handler> moveStartCallbacks = null;
	protected List<Handler> moveCallbacks = null;
	protected List<Handler> moveEndCallbacks = null;
	protected List<Handler> zoomEndCallbacks = null;
	
	public MapEvents() {
		super();
		
		layerAddedCallbacks = new Vector<Handler>();				
		layerRemovedCallbacks = new Vector<Handler>();		
		layerChangedCallbacks = new Vector<Handler>();
		baselayerChangedCallbacks = new Vector<Handler>();
		
		moveStartCallbacks = new Vector<Handler>();				
		moveCallbacks = new Vector<Handler>();		
		moveEndCallbacks = new Vector<Handler>();
		zoomEndCallbacks = new Vector<Handler>();
		
		this.listeners.put(new Integer(LAYER_ADDED), layerAddedCallbacks);
		this.listeners.put(new Integer(LAYER_REMOVED), layerRemovedCallbacks);
		this.listeners.put(new Integer(LAYER_CHANGED), layerChangedCallbacks);
		this.listeners.put(new Integer(BASELAYER_CHANGED), baselayerChangedCallbacks);
		this.listeners.put(new Integer(MOVE_START), moveStartCallbacks);
		this.listeners.put(new Integer(MOVE), moveCallbacks);
		this.listeners.put(new Integer(MOVE_END), moveEndCallbacks);
		this.listeners.put(new Integer(ZOOM_END), zoomEndCallbacks);		
	}
	
	/**
	 * API Method: getEventList
	 * 
	 * @return
	 */
	public int[] getEventList() {
		return EVENTS_LIST;
	}
	
	/*
	@Override	
	public void addDefaultCallbacks() {		
		// TODO: register all default callbacks		
	}
     
	@Override
	public void removeDefaultCallbacks() {		
		// TODO: unregister all default callbacks
	}
	*/
	
	/*
	private class LayerAddedCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == LAYER_ADDED) {
				Util.printDebugMessage(" ...default callback for LAYER_ADDED event...");
				try {
					Layer layer = (Layer)msg.obj;
					Util.printDebugMessage(" ...layer " + layer.getName() + " is added...");
				} catch(Exception e) {
					
				}
			}			
		}		
	}
	
	private class LayerRemovedCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == LAYER_REMOVED) {
				//Util.printDebugMessage(" ...default callback for LAYER_REMOVED event...");	
			}
		}		
	}
	
	private class LayerChangedCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == LAYER_CHANGED) {
				//Util.printDebugMessage(" ...default callback for LAYER_CHANGED event...");	
			}
		}		
	}
	
	private class BaseLayerChangedCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == BASELAYER_CHANGED) {
				//Util.printDebugMessage(" ...default callback for BASELAYER_CHANGED event...");	
			}			
		}		
	}
	
	private class MoveStartCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MOVE_START) {
				//Util.printDebugMessage(" ...default callback for MOVE_START event...");	
			}			
		}		
	}
	
	
	private class MoveEndCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MOVE_END) {
				Util.printDebugMessage(" ...default callback for MOVE_END event...");	
			}			
		}		
	}
	
	
	private class MoveCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MOVE) {
				//Util.printDebugMessage(" ...default callback for MOVE event...");	
			}			
		}		
	}
	
	private class ZoomEndCallback extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == ZOOM_END) {
				//Util.printDebugMessage(" ...default callback for ZOOM_END event...");	
			}			
		}		
	}
	*/
}
