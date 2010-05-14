package com.androidol.renderer;

import java.util.ArrayList;

import com.androidol.Map;
import com.androidol.basetypes.Size;
import com.androidol.events.Event;
import com.androidol.feature.Vector;

import com.androidol.style.Style;
import com.vividsolutions.jts.geom.Envelope;
//import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Geometry;


public class Renderer {
	
	protected Map map;
	
	protected Size size;
	protected Envelope extent;
	protected double resolution;
	
	/**
	 * 
	 */
	public Renderer() {}
	
	/**
	 * 
	 */
	public void destroy() {		
        this.extent = null;
        this.size =  null;
        this.resolution = Double.NEGATIVE_INFINITY;
        this.map = null;
	}
	
	/**
	 * @param extent the extent to set
	 */
	public void setExtent(Envelope extent) {
		setExtent(extent, true);
		
	}

	/**
	 * @return the extent
	 */
	public Envelope getExtent() {
		return extent;
	}

	/**
     * API Method: setExtent
     * Set the visible part of the layer.
     *
     * Resolution has probably changed, so we nullify the resolution 
     * cache (this.resolution) -- this way it will be re-computed when 
     * next it is needed.
     * We nullify the resolution cache (this.resolution) if resolutionChanged
     * is set to true - this way it will be re-computed on the next
     * getResolution() request.
     *
     * @param extent - {Bounds}
     * @param resolutionChanged - {Boolean}
     */
    public void setExtent(Envelope extent, boolean resolutionChanged) {
        this.extent = new Envelope(extent);
        if(resolutionChanged == true) {
            this.resolution = Double.NEGATIVE_INFINITY;
        }
    }
    
	/**
	 * @return the size
	 */
	public Size getSize() {
		return size;
	}
    
    /**
     * API Method: setSize
     * Sets the size of the drawing surface.
     * 
     * Resolution has probably changed, so we nullify the resolution 
     * cache (this.resolution) -- this way it will be re-computed when 
     * next it is needed.
     *
     * @param size - {Size} 
     */
    public void setSize(Size size) {
        this.size = size.clone();
        this.resolution = Double.NEGATIVE_INFINITY;
    }
    
    /** 
     * API Method: getResolution
     * Uses cached copy of resolution if available to minimize computing
     * 
     * @return The current map's resolution
     */
    public double getResolution() {
        if(this.resolution <= 0) {
        	this.resolution = this.map.getResolution();
        }
        return this.resolution;
    } 
    
    /**
     * API Method: setResolution
     * @param resolution
     */
    public void setResolution(double resolution) {
    	if(resolution > 0.0) {
    		this.resolution = resolution;
    	} else {
    		this.resolution = Double.NEGATIVE_INFINITY;
    	}    	
    }

	/**
	 * @return the map
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public void setMap(Map map) {
		this.map = map;
	}
	
	/**
	 * API Method: drawFeature
	 * 
	 * @return
	 */
	public void drawFeature(Vector feature, Style style) {
		if(style == null) {
            style = feature.getStyle();
        }
        if(feature!=null && feature.getGeometry()!=null) {
        	//Util.printDebugMessage("@...draw feature: " + feature.getId() + " ...");
        	this.drawGeometry(feature.getGeometry(), style);
        }
	}
	
	/**
	 * 
	 * @return
	 */
	public void drawGeometry(Geometry geometry, Style style) {
		
	}
	
	/**
	 * 
	 */
	public void clear() {}
	
	/**
	 * 
	 * @param event
	 * @return
	 */
	public Vector getFeatureFromEvent(Event event) {
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public void eraseFeatures(ArrayList<Vector> features) {
		if(features!=null && features.size()>0) {
			for(int i=0, len=features.size(); i<len; ++i) {
	            this.eraseGeometry(features.get(i).getGeometry());
	        }
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public void eraseGeometry(Geometry geometry) {
		
	}
	
	/**
	 * 
	 */
	public String getFeatureIdFromEvent(Event event) {
		return "";
	}
}
