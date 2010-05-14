package com.androidol.protocol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.androidol.events.ProtocolEvents;
import com.androidol.format.Format;

public class Protocol {
		
	protected Format 					format;	
	protected boolean 					autoDestroy 		= true;
	
	protected ExecutorService 			threadPool 			= Executors.newFixedThreadPool(2);
	
	protected ProtocolEvents			events				= new ProtocolEvents();					
	
	/**
	 * 
	 */
	public Protocol(Format format) {
		this.format = format;
	}

	public void destroy() {
		this.format = null;
	}
	
	/**
	 * 
	 */
	public void read() {
		
	}
	
	/**
	 * 
	 */
	public void create() {
		
	}
	
	/**
	 * 
	 */
	public void delete() {
		
	}

	/**
	 * 
	 */
	public void update() {
		
	}
	
	/**
	 * 
	 */
	public void commit() {
		
	}
	
	/**
	 * @return the autoDestroy
	 */
	public boolean isAutoDestroy() {
		return autoDestroy;
	}

	/**
	 * @param autoDestroy the autoDestroy to set
	 */
	public void setAutoDestroy(boolean autoDestroy) {
		this.autoDestroy = autoDestroy;
	}

	/**
	 * @return the format
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(Format format) {
		this.format = format;
	}

	/**
	 * @return the events
	 */
	public ProtocolEvents getEvents() {
		return events;
	}

	/**
	 * @param events the events to set
	 */
	public void setEvents(ProtocolEvents events) {
		this.events = events;
	}	
	
	
}
