package com.androidol.protocol;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.androidol.events.Event;
import com.androidol.events.ProtocolEvents;
import com.androidol.feature.Vector;
import com.androidol.format.Format;
import com.androidol.util.Util;

public class HTTP extends Protocol {
	
	protected String 					url				= "";
	protected boolean					readWithPost	= false;
	protected HashMap<String, String> 	params			= null;
			
	public HTTP(Format format, String url) {
		super(format);
		this.url = url;
	}
	
	/*
	 * 
	 */
	@Override
	public void read() {
		this.threadPool.execute(
			new Runnable() {
				@Override
				public void run() {						
					try {								
						//Util.printDebugMessage("@...read features with HTTP protocol...");
												
						DefaultHttpClient client = new DefaultHttpClient();						
						URI uri = new URI(HTTP.this.url);
						// TODO: add request parameters in this.params
						HttpGet method = new HttpGet(uri);
						HttpResponse response = client.execute(method);
						InputStream inputStream = response.getEntity().getContent();
						
						/*
						InputStreamReader reader = new InputStreamReader(inputStream);						
						BufferedReader buffer = new BufferedReader(reader);
						StringBuilder sb = new StringBuilder();
						String cur;
						while ((cur = buffer.readLine()) != null) {
							sb.append(cur + "\n");
						}
						inputStream.close();
						String resp = sb.toString();
						*/
						// TODO: parse features/geometries from response stream								
						ArrayList<Vector> parsedFeatures = HTTP.this.format.parseFeatures(inputStream);
						
						Event event = new Event();
					    event.properties.put("type", ProtocolEvents.READ_SUCCESS);					    
					    event.properties.put("data", parsedFeatures);
					    
					    //Util.printDebugMessage(" ...trigger ProtocolEvents.READ_SUCCESS event...");	
					    HTTP.this.events.triggerEvent(ProtocolEvents.READ_SUCCESS, event);
					} catch (Exception e) {							
						Event event = new Event();
						event.properties.put("type", ProtocolEvents.READ_FAILURE);							    							    
						//Util.printDebugMessage(" ...trigger ProtocolEvents.READ_FAILURE event...");
						HTTP.this.events.triggerEvent(ProtocolEvents.READ_FAILURE, event); 
						Util.printErrorMessage(e.toString());									
					} finally {
						// TODO: some error handling
					}
				}
		});
	}
	
	/*
	 * 
	 */
	public void addRequestParameter() {
		
	}
	
	/*
	 * 
	 */
	public void removeRequestParameter() {
		
	}
}
