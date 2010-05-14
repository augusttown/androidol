package com.androidol.events;

import java.util.HashMap;

public class Event {
	
	/**
	 * properties of an event
	 *   'type' indicate the type and origin of the event
	 *   'data' contains the useful data for a event callback handler to process the event
	 */
	public HashMap<String, Object> properties = new HashMap<String, Object>();
	
	/**
	 * Constructor
	 */
	public Event() {}
	
	/**
	 * Constructor
	 *   event with an 'type' but no data
	 */
	public Event(int type) {
		this.properties.put("type", new Integer(type));		
	}
	
	/**
	 * Constructor
	 *   event with an 'type' and data
	 */
	public Event(int type, Object data) {
		this.properties.put("type", new Integer(type));
		this.properties.put("data", data);
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void attach(String key, Object value) {
		this.properties.put(key, value);
	}
	
}
