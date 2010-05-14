package com.androidol.strategy;

import android.os.Handler;
import com.androidol.layer.Vector;


public class Strategy {
	
	protected Vector 	layer;
	protected boolean 	active			= false;
	
	protected boolean 	autoDestroy 	= true;
	protected boolean 	autoActivate 	= true;
	
	protected Handler	protocolHandler = null;
	
	/**
	 * Constructor
	 */
	public Strategy() {
		this.active = false;
	}
	
	/**
	 * 
	 */
	public void destroy() {
		this.deactivate();
        this.layer = null;        
	}
	
	/**
	 * 
	 */
	public boolean activate() {
		if(this.active == false) {
            this.active = true;
            return true;
        }
        return false;
	}
	
	/**
	 * 
	 */
	public boolean deactivate() {
		if(this.active == true) {
            this.active = false;
            return true;
        }
        return false;
	}
	
	

	/**
	 * @return the protocolHandler
	 */
	public Handler getProtocolHandler() {
		return protocolHandler;
	}

	/**
	 * @param protocolHandler the protocolHandler to set
	 */
	public void setProtocolHandler(Handler protocolHandler) {
		this.protocolHandler = protocolHandler;
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
	 * @return the autoActivate
	 */
	public boolean isAutoActivate() {
		return autoActivate;
	}

	/**
	 * @param autoActivate the autoActivate to set
	 */
	public void setAutoActivate(boolean autoActivate) {
		this.autoActivate = autoActivate;
	}

	/**
	 * @return the layer
	 */
	public Vector getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(Vector layer) {
		this.layer = layer;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}
