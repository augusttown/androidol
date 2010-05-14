package com.androidol.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

//import com.androidol.util.Util;

import android.os.Handler;
import android.os.Message;

public class Events extends Handler {
	
	/*
	 * List of all events
	 * 
	 * MapEvents:
	 * 
	 * 		LAYER_ADDED: 		10000
	 * 		LAYER_REMOVED: 		10001
	 * 		LAYER_CHANGED:		10002
	 * 		BASELAYER_CHANGED:	10003
	 * 		
	 * 		MOVE_START:			10004
	 * 		MOVE:				10005
	 * 		MOVE_END:			10006
	 * 		ZOOM_END:			10007
	 * 
	 * LayerEvents:
	 * 	
	 *		LOAD_START			20000
	 *		LOAD_END 			20001
	 *		LOAD_CANCELED 		20002
	 *		VISIBILITY_CHANGED	20003
	 *		MOVE_END 			20004
	 * 
	 * 
	 * TileHttpLoader event types
	 * 	
	 * 		LOAD_SUCCESS: 2001
	 * 		LOAD_FAILURE: 2000
	 * 
	 * TileFileSystemLoader event types
	 * 
	 * 		LOAD_SUCCESS: 1001
	 * 		LOAD_FAILURE: 1000
	 * 
	 * Protocol event types
	 * 
	 * 		READ_SUCCESS: 30000
	 * 		READ_FAILURE: 30001
	 */
	
	// ===========================================================
	// Declare a list of supported event type
	// ===========================================================
	public final int[] EVENTS_LIST = {};
	
	/**
	 * 'listeners' is a hash map, which contains the handler for different types of event
	 *    to have a specific type of event to be handled, just register a Handler class for that event type 
	 */
	protected	HashMap<Integer, List<Handler>>	listeners	= null;
	
	/**
	 * Constructor
	 */
	public Events() {
		// initialize with an empty list of listeners 
		this.listeners = new HashMap<Integer, List<Handler>>();
	}
	
	/**
	 * the 'handleMessage' method events itself behaves like a event dispatcher,
	 *   when an event is triggered, it first goes through this method and then being dispatched
	 *   to a list of handler based on the event type    
	 */
	@Override
	public void handleMessage(final Message msg) {		
		//Util.printDebugMessage("@...event: " + msg.what + " is captured in Events.handleMessage()...");			
		List<Handler> callbacks = this.listeners.get(new Integer(msg.what));		
		if(callbacks!=null && callbacks.size()>0) {			
			for(int i=0; i<callbacks.size(); i++) {
				Handler callback = callbacks.get(i);
				//Util.printDebugMessage(" ...event: " + msg.what + " is dispatched to: " + callback.getClass().toString() + "...");
				Message message = Message.obtain(callback, msg.what, msg.obj);
		        message.sendToTarget();
			}
		} else {
			//Util.printDebugMessage(" ...no callback registered for event: " + msg.what + "...");	
		}
	}
	
	/**
	 * Register multiple callback handlers for multiple types of event
	 * 
	 * @param callbacks
	 */
	public void registerAll(HashMap<Integer, List<Handler>> callbacks) {
		Set<Integer> types = callbacks.keySet();
		Iterator<Integer> iterator = types.iterator();
		while(iterator.hasNext()) {
			Integer type = iterator.next();
			if(this.listeners.containsKey(type) == true) {
				// append more callback handlers
				this.listeners.get(type).addAll(callbacks.get(type));
			} else {
				// register callback handlers for new event type 
				this.listeners.put(type, callbacks.get(type));
			}
		}		
	}
	
	/**
	 * Unregister multiple callback handlers 
	 * 
	 * @param callbacks
	 */
	public void unregisterAll(HashMap<Integer, List<Handler>> callbacks) {
		Set<Integer> types = callbacks.keySet();
		Iterator<Integer> iterator = types.iterator();
		while(iterator.hasNext()) {
			Integer type = iterator.next();
			if(this.listeners.containsKey(type) == true) {
				this.listeners.get(type).removeAll(callbacks.get(type));
			}
		}
	}
	
	/**
	 * Register multiple callback handlers for events defined in itself
	 * 
	 * @param callbacks
	 */
	public void registerAll(List<Handler> callbacks) {		
		int[] list = this.getEventList();
		for(int i=0; i<list.length; i++) {
			HashMap<Integer, List<Handler>> hashMap = new HashMap<Integer, List<Handler>>();
			hashMap.put(list[i], callbacks);
			registerAll(hashMap);
		}
	}
	
	/**
	 * Unregister multiple callback handlers for events defined in itself
	 * 
	 * @param callbacks
	 */
	public void unregisterAll(List<Handler> callbacks) {
		int[] list = this.getEventList();
		for(int i=0; i<list.length; i++) {
			HashMap<Integer, List<Handler>> hashMap = new HashMap<Integer, List<Handler>>();
			hashMap.put(list[i], callbacks);
			unregisterAll(hashMap);
		}
	}
	
	/**
	 * Register one callback handlers for all events defined in itself
	 * 
	 * @param callbacks
	 */
	public void registerAll(Handler callback) {		
		List<Handler> callbacks = new ArrayList<Handler>();
		callbacks.add(callback);
		registerAll(callbacks);
	}
	
	/**
	 * Register one callback handlers for all events defined in itself
	 * 
	 * @param callbacks
	 */
	public void unregisterAll(Handler callback) {		
		List<Handler> callbacks = new ArrayList<Handler>();
		callbacks.add(callback);
		unregisterAll(callbacks);
	}
	
	/**
	 * Register a single callback handler to a specific type of event
	 * 
	 * @param type
	 * @param callback
	 */
	public void register(int type, Handler callback) {
		if(this.listeners.get(new Integer(type)) != null) {
			this.listeners.get(new Integer(type)).add(callback);
		}
	}
	
	/**
	 * Unregister a single callback handler to a specific type of event
	 * 
	 * @param type
	 * @param callback
	 */
	public void unregister(int type, Handler callback) {
		if(this.listeners.get(new Integer(type)) != null) {
			this.listeners.get(new Integer(type)).remove(callback);
		}
	}
	
	/**
	 * Trigger a event
	 *   the event will be triggered by sending a message to this.handleMessage(), which then will be dispatched
	 *   to registered callback handlers; a event contains type(int) and a content object 
	 *    
	 * @param type
	 * @param event
	 */
	public void triggerEvent(int type, Event event) {
		Message message = null;
		if(event != null) {
			message = Message.obtain(this, type, event);
		} else {
			message = Message.obtain(this, type);
		}		
        message.sendToTarget();
	}
	
	/**
	 * 
	 */
	public void addDefaultCallbacks() {		
		// sub-class overrides this to register default callback handlers if necessary		
	}
	
	/**
	 * 
	 */
	public void removeDefaultCallbacks() {		
		// sub-class overrides this to unregister default callback handlers if necessary
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
