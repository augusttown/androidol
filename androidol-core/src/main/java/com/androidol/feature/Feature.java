package com.androidol.feature;

import java.util.HashMap;

import com.androidol.Marker;
import com.androidol.layer.Layer;
import com.androidol.popup.Popup;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

public class Feature {
	
	// ===========================================================
	// Fields
	// ===========================================================	
	
	protected Layer 								layer;
	protected String 								id;    
	protected Coordinate							coordinate;
	protected Object								data;    
	protected Marker								marker;
	protected Popup									popup;    
	protected HashMap<String, String>				attributes;    		
	protected boolean 								selected 			= false;
	
	//protected GeometryFactory	geometryFactory		= new GeometryFactory();
	
	/**
	 * Constructor 
	 * @return
	 */
	public Feature(Layer layer, Coordinate coord, Object data) {
		this.layer = layer;
        this.coordinate = coord;
        if(data != null) {
        	this.data = data;
        } else{
        	this.data = new Object();
        }        
        this.id = Util.createUniqueID(this.getClass().getName() + "_"); 
	}
	
	/**
	 * API Method: destroy
	 * 
	 * @return
	 */
	public void destroy() {
        if((this.layer!=null) && (this.layer.getMap() != null)) {
            if(this.popup != null) {
                // TODO:
            	//this.layer.getMap().removePopup(this.popup);
            }
        }
        this.layer = null;
        this.id = null;
        this.coordinate = null;
        this.data = null;
        if(this.marker != null) {
            this.destroyMarker();
            this.marker = null;
        }
        if(this.popup != null) {
            this.destroyPopup();
            this.popup = null;
        }
	}
	
	/**
     * API Method: onScreen
     * 
     * @return
     * {boolean} Whether or not the feature is currently visible on screen
     *           (based on its 'lonlat' property)
     */
    public boolean onScreen() {        
        boolean onScreen = false;
        if((this.layer!=null) && (this.layer.getMap()!=null)) {
            Envelope screenBounds = this.layer.getMap().getExtent();
            onScreen = screenBounds.contains(this.coordinate);
        }    
        return onScreen;
    }
    
    /**
     * 
     * @return
     */
    public Marker createMarker() {
    	// TODO:
    	/*
        if(this.lonlat != null) {
            this.marker = new Marker();
            this.marker.lonlat = this.lonlat;
        }
        */
        return this.marker;
	}
	
	/**
	 * 
	 */
	public void destroyMarker() {
		this.marker.destroy();
	}
	
	/**
	 * 
	 * @param closeBox
	 * @return
	 */
	public Popup createPopup(boolean closeBox) {
		// TODO:
		/*
		if(this.lonlat != null) {        
            var id:String = this.id + "_popup";
            var anchor:Icon = this.marker;

            this.popup = new Anchored(	id, 
                                        this.lonlat,
                                        this.data.popupSize,
                                        this.data.popupContentHTML,
                                        this.marker,
                                        closeBox);

            this.popup.feature = this;
        } 
        */
		return this.popup;
	}
	
	/**
	 * 
	 */
	public void destroyPopup() {
		this.popup.destroy();
	}

	/**
	 * @return the layer
	 */
	public Layer getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(Layer layer) {
		this.layer = layer;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	
}
