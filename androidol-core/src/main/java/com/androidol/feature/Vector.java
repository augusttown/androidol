package com.androidol.feature;

import java.util.HashMap;

import com.androidol.basetypes.Pixel;
import com.androidol.style.Style;
import com.androidol.util.Util;
import com.androidol.util.geometry.JTSGeometryUtils;
import com.androidol.util.geometry.MoveCoordinateFilter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Vector features use the Geometry classes as geometry description.
 * They have an ‘attributes’ property, which is the data object, and a ‘style’ property,
 * the default values of which are defined in the OpenLayers.Feature.Vector.style objects
 */
public class Vector extends Feature {

	// ===========================================================
	// Fields
	// ===========================================================	
	
	protected String 			fid					= null;
	protected Geometry 			geometry			= null;
	protected String 			state				= null;
	protected Style 			style				= null;
	protected Style 			originalStyle		= null;
	protected String			renderIntent		= "default";
	
	/**
	 * Constructor
	 * 
	 * @param geometry
	 * @param attributes
	 * @param style
	 */
	public Vector(Geometry geometry, HashMap<String, String> attributes, Style style) {
		super(null, null, attributes);
		
		this.coordinate = null;
		this.state = null;
		if(geometry != null) {
			this.geometry = geometry;
		}        
        this.attributes = new HashMap<String, String>();
        if(attributes != null) {
            this.attributes = Util.extend(this.attributes, attributes);
        }
        if(style != null) {
        	this.style = style;
        }        
	}
	
	/** 
     * API Method: destroy
     * nullify references to prevent circular references and memory leaks
     */
    public void destroy() {
        if(this.layer != null) {
            // TODO: remove this feature from layer?
        	//this.layer.removeFeatures(this);
            this.layer = null;
        }            
        this.geometry = null;
        super.destroy();
    }
	
    /**
     * API Method: clone
     * Create a clone of this vector feature.  Does not set any non-standard
     *     properties.
     *
     * @return {Vector} An exact clone of this vector feature.
     */
    public Vector clone() {
    	if(this.geometry != null) {
    		return new Vector((Geometry)this.geometry.clone(), this.attributes, this.style);
    	} else {
    		return new Vector(null, this.attributes, this.style);
    	}      
    }
    
    /**
     * API Method: onScreen
     * Determine whether the feature is within the map viewport.  This method
     *     tests for an intersection between the geometry and the viewport
     *     bounds.  If a more effecient but less precise geometry bounds
     *     intersection is desired, call the method with the boundsOnly
     *     parameter true.
     *
     * @param boundsOnly - {boolean} Only test whether a feature's bounds intersects
     *     the viewport bounds.  Default is false.  If false, the feature's
     *     geometry must intersect the viewport for onScreen to return true.
     * 
     * @return {boolean} The feature is currently visible on screen (optionally
     *     based on its bounds if boundsOnly is true).
     */
    public boolean onScreen(boolean boundsOnly) {
    	boolean onScreen = false;
        if(this.layer!=null && this.layer.getMap()!=null) {
            Envelope screenBounds = this.layer.getMap().getExtent();
            if(boundsOnly) {
            	Envelope featureBounds = this.geometry.getEnvelopeInternal();
                onScreen = screenBounds.intersects(featureBounds);
            } else {
            	//Polygon screenPoly = JTSGeometryUtils.envelopeToPolygon(screenBounds);            	
                // TODO: implement polygon interesects
            	//onScreen = screenPoly.intersects(this.geometry);
            }
        }    
        return onScreen;
    }
    
    /**
     * 
     * @return
     */
    /*
    @Override
    public Marker createMarker() {}
	*/
    
	/**
	 * 
	 */
    /*
    @Override
	public void destroyMarker() {}
	*/
	/**
	 * 
	 * @param closeBox
	 * @return
	 */
    /*
    @Override
	public Popup createPopup(boolean closeBox) {}
	*/
	/**
	 * 
	 */
    /*
    @Override
	public void destroyPopup() {}
    */
    
    /**
     * API Method: atPoint
     * Determines whether the feature intersects with the specified location.
     * 
     * @param lonlat - {LonLat} 
     * @param toleranceLon - {double} Optional tolerance in Geometric Coords
     * @param toleranceLat - {double} Optional tolerance in Geographic Coords
     * 
     * @return {boolean} Whether or not the feature is at the specified location
     */
    public boolean atPoint(Coordinate coord, double toleranceX, double toleranceY) {
        boolean atPoint = false;
        if(this.geometry != null) {
            Envelope envelope = this.geometry.getEnvelopeInternal();
            envelope.expandBy(toleranceX, toleranceY);
            atPoint = envelope.contains(coord);
            
        }
        return atPoint;
    }
    
    /**
     * API Method: move
     * Moves the feature and redraws it at its new location
     *
     * Parameters:
     * state - {OpenLayers.LonLat or OpenLayers.Pixel} the
     *         location to which to move the feature.
     */
    public void move(Coordinate location) {
        if(this.layer==null) {
            return;
        }
        Pixel pixel = this.layer.getViewPortPxFromCoordinate(location);
        Pixel lastPixel = this.layer.getViewPortPxFromCoordinate(this.geometry.getEnvelopeInternal().centre());
        double res = this.layer.getMap().getResolution();
        // TODO: check how performance could be affected by this
        this.geometry.apply(new MoveCoordinateFilter(res*(pixel.getX()-lastPixel.getX()), res*(lastPixel.getY()-pixel.getY())));        
    }
    
    /**
     * API Method: toState
     * Sets the new state
     *
     * @param state - {String} 
     */
    public void toState(String state) {
		if(state.equalsIgnoreCase(State.UPDATE) == true) {
            if(this.state.equalsIgnoreCase(State.UNKNOWN) || this.state.equalsIgnoreCase(State.DELETE)) {
            	this.state = state;
            }			
        } else if(state.equalsIgnoreCase(State.INSERT) == true) {
        	if(this.state.equalsIgnoreCase(State.UNKNOWN)==false) {
            	this.state = state;
            }        
        } else if(state.equalsIgnoreCase(State.DELETE) == true) {
        	if(this.state.equalsIgnoreCase(State.UNKNOWN) || this.state.equalsIgnoreCase(State.UPDATE)) {
            	this.state = state;
            }        	
        } else if(state.equalsIgnoreCase(State.UNKNOWN) == true) {
            this.state = state;
        }
	}

	/**
	 * @return the style
	 */
	public Style getStyle() {
		return style;
	}

	/**
	 * @param style the style to set
	 */
	public void setStyle(Style style) {
		this.style = style;
	}

	/**
	 * @return the geometry
	 */
	public Geometry getGeometry() {
		return geometry;
	}

	/**
	 * @param geometry the geometry to set
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * @return the renderIntent
	 */
	public String getRenderIntent() {
		return renderIntent;
	}

	/**
	 * @param renderIntent the renderIntent to set
	 */
	public void setRenderIntent(String renderIntent) {
		this.renderIntent = renderIntent;
	}

	/**
	 * @return the fid
	 */
	public String getFid() {
		return fid;
	}

	/**
	 * @param fid the fid to set
	 */
	public void setFid(String fid) {
		this.fid = fid;
	}
    
    
}
