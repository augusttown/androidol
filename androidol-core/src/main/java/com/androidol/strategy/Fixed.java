package com.androidol.strategy;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;

import com.androidol.events.Event;
import com.androidol.events.ProtocolEvents;
import com.androidol.feature.Vector;
import com.androidol.protocol.Protocol;
import com.androidol.util.Util;

public class Fixed extends Strategy {

	public Fixed() {
		super();
		this.protocolHandler = new FixedProtocolHandler();
	}
	
	/**
     * API Method: activate
     * Activate the strategy: reads all features from the protocol and add them 
     * to the layer.
     *
     * @return {boolean} True if the strategy was successfully activated or false if
     *      the strategy was already active.
     */
	@Override
    public boolean activate() {
        if(super.activate() == true) {        	        	        
        	Protocol protocol = this.layer.getProtocol();
        	if(protocol != null) {
        		protocol.read();
            }        	                   
        	return true;
        }
        return false;
    }
	
	/**
	 * 
	 */
	public void merge(ArrayList<Vector> features) {
		//Util.printDebugMessage(" ...merge features in Fixed strategy...");
		this.layer.addFeatures(features, false);
	}
	
	/**
	 * 
	 *
	 */
	private class FixedProtocolHandler extends Handler {		
		@Override
		public void handleMessage(final Message msg) {
			final int what = msg.what;
			switch(what) {
				case ProtocolEvents.READ_SUCCESS:
					//Util.printDebugMessage(" ...Fixed strategy receives READ_SUCCESS message from protocol...");					
					//Util.printDebugMessage(" ...Obtain response string carried by the message...");
					ArrayList<Vector> features = null;
					try {
						features = (ArrayList<Vector>)((Event)msg.obj).properties.get("data");
						//Util.printDebugMessage(" ...response body: " + "");
						//Util.printDebugMessage(" ...parse features from response...");												
						Fixed.this.merge(features);
					} catch(Exception e) {
						// TODO: more error handling
						Util.printErrorMessage(e.toString());
					}					
					break;
				case ProtocolEvents.READ_FAILURE:
					Util.printDebugMessage(" ...Fixed strategy receives READ_FAILREU message from protocol...");
					break;
				default:
			}
		}		
	}
	
}
