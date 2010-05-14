package com.androidol.protocol;

import com.androidol.events.Event;
import com.androidol.events.ProtocolEvents;
import com.androidol.format.Format;
import com.androidol.util.Util;

public class Native extends Protocol {
	
	protected String 			resourceId;
	
	public Native(Format format, String resourceId) {
		super(format);
		this.resourceId = resourceId;
	}
	
	/**
	 * 
	 */
	@Override
	public void read() {
		if(this.resourceId != null) {
			this.threadPool.execute(
				new Runnable() {
					@Override
					public void run() {						
						try {								
							Util.printDebugMessage("@...read features with Native protocol...");
							Event event = new Event();
						    event.properties.put("type", ProtocolEvents.READ_SUCCESS);
						    // TODO: package data in event if there is any
						    //event.properties.put("data", null);
						    Util.printDebugMessage(" ...trigger ProtocolEvents.READ_SUCCESS event...");
						    Native.this.events.triggerEvent(ProtocolEvents.READ_SUCCESS, event);													    							
						} catch (Exception e) {														
							Event event = new Event();
							event.properties.put("type", ProtocolEvents.READ_FAILURE);							    							    
							Util.printDebugMessage(" ...trigger ProtocolEvents.READ_FAILURE event...");
							Native.this.events.triggerEvent(ProtocolEvents.READ_FAILURE, event); 
							Util.printErrorMessage(e.toString());																
						} finally {
							// TODO: some error handling
						}
				}
			});
		}
	}
}
