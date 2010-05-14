package com.androidol.layer;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.androidol.Map;
import com.androidol.basetypes.Pixel;
import com.androidol.basetypes.Size;
import com.androidol.constants.UtilConstants;
import com.androidol.events.Event;
import com.androidol.events.LayerEvents;
import com.androidol.events.MapEvents;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class Layer extends View implements UtilConstants {
	
	// ===========================================================
	// fields related to layer itself
	// ===========================================================	
	protected 	Context				context						= null;
	
	protected 	String 				name						= "";
	protected 	boolean 			isBaseLayer 				= false;
	public 		boolean 			isVector 					= false;
	protected 	boolean 			alwaysInRange 				= false;
	protected 	boolean				visible 					= true;		
	protected 	int 				gutter						= 0;		
	protected 	boolean 			displayOutsideMaxExtent 	= false;
	protected 	boolean 			wrapDateLine				= false;
	protected 	double 				opacity						= 1.0;
	protected 	int 				buffer						= 0;	
	protected   boolean				isOfflineMode				= false;

	protected 	Map					map 						= null;	
	protected 	Size 				imageSize 					= null;		
	protected 	Pixel 				imageOffset					= new Pixel(0.0, 0.0);
	protected 	boolean 			inRange 					= false;
	
	protected 	Bitmap				previousSnapshot			= null;	
	
	// ===========================================================
	// fields related to Map
	// ===========================================================	
	
	protected 	Envelope 			maxExtent					= null;
	protected	double 				maxResolution				= Double.NEGATIVE_INFINITY;
	protected 	double 				minResolution				= Double.NEGATIVE_INFINITY;
	protected 	int 				numZoomLevels				= 0;
	protected 	double[]			scales						= null;	
	protected 	double[] 			resolutions					= null;
	protected 	String 				projection					= "EPSG:3857";
	protected 	String				units						= "meters";
	protected 	double 				maxScale					= Double.NEGATIVE_INFINITY;
	protected 	double 				minScale					= Double.NEGATIVE_INFINITY;
	
	public 		LayerEvents			events						= new LayerEvents();
	
	protected 	LayerEventsHandler	layerEventsHandler			= new LayerEventsHandler();
	
	protected 	Canvas				canvas						= null;
	protected 	final Paint 		paint 						= new Paint();
	
	public int dragDx = 0;
	public int dragDy = 0;
	protected boolean dragging = false; 
	
	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * 
	 */
	public Layer(Context context) {
		super(context);
		this.context = context;
		//
		// TODO: give it a random default name
		this.name = "defaut_name";
		// TODO: give default value to other map related parameters if necessary
		if(this.wrapDateLine == true) {
            this.displayOutsideMaxExtent = true;
        }		
		// to register callback handlers on this.events
		this.events.registerAll(this.layerEventsHandler);
		//
	}
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public Layer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// 'name'
		this.name = attrs.getAttributeValue(ANDROIDOL_NAMESPACE, "name");
		//Util.printDebugMessage("...layer name: " + this.name);
		// 'isBaseLayer'
		this.isBaseLayer = attrs.getAttributeBooleanValue(ANDROIDOL_NAMESPACE, "isBaseLayer", false);
		// 'isOfflineMode'
		this.isOfflineMode = attrs.getAttributeBooleanValue(ANDROIDOL_NAMESPACE, "isOfflineMode", false);
		//Util.printDebugMessage("...is baselayer: " + this.isBaseLayer);
		// 'transparency'
		double opacity = attrs.getAttributeFloatValue(ANDROIDOL_NAMESPACE, "opacity", 1.0f);		
		this.paint.setAlpha((int)(opacity*255)); 
		//Util.printDebugMessage("...layer opacity: " + (int)(opacity*255));
		
		// '' 
		
		/*
		this.alwaysInRange = attrs.getAttributeBooleanValue(ANDROIDOL_NAMESPACE, "alwaysInRange", true);
		Util.printDebugMessage("...is alwaysInRange: " + this.alwaysInRange);
		*/
		//
		// configuration options user could set through constructor:
		//     isBaseLayer
		//	   isVector
		//	   alwaysInRange 
		//     visible 						
		//	   gutter								
		//	   displayOutsideMaxExtent
		//     wrapDateLine				
		//	   opacity						
		//     buffer
		//
		// TODO: apply configurations in options to layer
		//
		if(this.wrapDateLine == true) {
            this.displayOutsideMaxExtent = true;
        }		
		// to register callback handlers on this.events
		this.events.registerAll(this.layerEventsHandler);
		
		this.setDrawingCacheEnabled(true);
	}
	
	/**
	 * 
	 */	
	@Override
	public void onDraw(final Canvas canvas) {
		//Util.printDebugMessage("...layer onDraw() called...");		
	}
	
	
	// ===========================================================
	// Public API and private methods
	// ===========================================================
	
	/**
	 * API Method: destroy
	 * 
	 * @param setNewBaseLayer
	 */
	public void destroy(boolean setNewBaseLayer) {
		if(this.map != null) {
			this.map.removeLayer(this, setNewBaseLayer);
		}
		this.events.unregisterAll(this.layerEventsHandler);
		this.map = null;
	}	
	
	/**
	 * API Method: onMapResize
	 */
	public void onMapResize() {
		// TODO: this method can be implemented by sub-classes
	}
	
	/**
	 * API Method: redraw
	 * 
	 * @return redrawn
	 */
	public boolean redraw() {
		//Util.printDebugMessage("@...Layer.redraw() is called...");
		boolean redrawn = false;
        if(this.map != null) {
            // min/max range may have changed, check if layer is still in range
            this.inRange = this.calculateInRange();
            // map's center might not yet be set
            Envelope extent = this.getExtent();                                                           
            if(extent!=null && this.inRange==true && this.visible==true) {            	
                this.moveTo(extent, false, false); // always set 'zoomChanged' to false 
                redrawn = true;
            }
        }        
        return redrawn;
	}
	
	/**
	 * API Method: setMap
	 * Set the map property for the layer. subclasses can override this 
	 * and take special action once they have their map variable set. 
     * 
     * Here we take care to bring over any of the necessary default 
     * properties from the map.
	 * 
	 * @param map
	 * 
	 */
	public void setMap(Map map) {
		//Util.printDebugMessage("@...Layer.setMap() is called...");
		if(this.map == null) {	        
            this.map = map;
            if(this.maxExtent == null) {
            	this.maxExtent = this.map.getMaxExtent();
            }
            if(this.projection == null) {
            	this.projection = this.map.getProjection();
            }
            if(this.units == null) {
            	this.units = this.map.getUnits();
            }      
            if(this.imageSize == null) {
            	this.imageSize = this.map.getSize();
            }
            // initialize the resolutions and scales
            this.initResolutions();            
            if(this.isBaseLayer == false) {
                this.inRange = this.calculateInRange();
                boolean show = ((this.visible) && (this.inRange));
                this.visible = (show ? true : false);
            }
        }
	}

	
	
	/**
	 * APIMethod: removeMap
     * Just as setMap() allows each layer the possibility to take a 
     *     personalized action on being added to the map, removeMap() allows
     *     each layer to take a personalized action on being removed from it. 
     *     For now, this will be mostly unused, except for the EventPane layer,
     *     which needs this hook so that it can remove the special invisible
     *     pane. 
     *      
	 * @param map
	 */
	// TODO: public void removeMap(Map map) {}
	
	/**
	 * APIMethod: initResolutions
     * This method's responsibility is to set up the 'resolutions' array 
     *     for the layer -- this array is what the layer will use to interface
     *     between the zoom levels of the map and the resolution display 
     *     of the layer.
     * 
     * The user has several options that determine how the array is set up.
     * 
     * 
	 */
	public void initResolutions() {				
		//Util.printDebugMessage("@...Layer.initResolutions() is called...");		
		int 		numZoomLevels 		= this.map.getNumZoomLevels();
		int 		maxZoomLevel 		= this.map.getMaxZoomLevel();		
		double[] 	scales 				= this.map.getScales();
		double[] 	resolutions 		= this.map.getResolutions();
		String 		units 				= this.map.getUnits();
		
		double 		maxScale			= this.map.getMaxScale();
		double 		minScale			= this.map.getMinScale();
		double 		maxResolution		= this.map.getMaxResolution();
		double 		minResolution		= this.map.getMinResolution();
		Envelope 	maxExtent			= this.map.getMaxExtent();
		Envelope 	minExtent			= this.map.getMinExtent();
		
		if(numZoomLevels<0 && maxZoomLevel>=0) {
			numZoomLevels = maxZoomLevel + 1;
		}
		if(scales!=null || resolutions!=null) {
			if(scales!=null && scales.length>0) { // if map has scales, calculate resolutions based on scales
				Util.printDebugMessage(" ...resolutions are calculated based on scales...");
				resolutions = new double[scales.length];
				for(int i=0; i<scales.length; i++) {
					double scale = scales[i];
					resolutions[i] = Util.getResolutionFromScale(scale, units);
				}
			}			
			numZoomLevels = resolutions.length;
		} else {
			if(minScale > 0) {				
				Util.printDebugMessage(" ...maxResolution calculated based on minScale...");
				maxResolution = Util.getResolutionFromScale(minScale, units);
			} else if(maxResolution <= 0) {
				Util.printDebugMessage(" ...maxResolution calculated based on map max extent...");
				Size viewSize = this.map.getSize();
            	double wRes = maxExtent.getWidth() / viewSize.getWidth();
            	double hRes = maxExtent.getHeight()/ viewSize.getHeight();
            	maxResolution = Math.max(wRes, hRes);				
			}			
			if(maxScale > 0) {
				// calculate minResolution
				Util.printDebugMessage(" ...minResolution calculated based on maxScale...");
				minResolution = Util.getResolutionFromScale(maxScale, units);
			} else if(minResolution<0 && minExtent != null) {
				// calculate based on this.map.minExtent
				Util.printDebugMessage(" ...minResolution calculated based on map min extent...");
				Size viewSize = this.map.getSize();
                double wRes = minExtent.getWidth() / viewSize.getWidth();
                double hRes = minExtent.getHeight()/ viewSize.getHeight();
                minResolution = Math.max(wRes, hRes);
			}			
			if(minResolution > 0) {								
				double ratio = maxResolution / minResolution;
				numZoomLevels = (int)Math.round(Math.floor(Math.log(ratio)/Math.log(2)) + 1);				
			}			
			resolutions = new double[numZoomLevels];
	        for(int i=0; i<numZoomLevels; i++) {
                double res = maxResolution / Math.pow(2, i);
                resolutions[i] = res;
            }    	        	    	      
		}
		// TODO: sort resolutions descending, is it necessary?
        this.resolutions = resolutions;
        this.maxResolution = resolutions[0];        
        this.minResolution = resolutions[resolutions.length-1];
        
        this.scales = new double[resolutions.length];
        for(int i=0; i<resolutions.length; i++) {
            this.scales[i] = Util.getScaleFromResolution(resolutions[i], units);
        }
        this.minScale = this.scales[0];
        this.maxScale = this.scales[this.scales.length - 1];        
        this.numZoomLevels = numZoomLevels;
        
        // print out resolutions and scales information        
        /*
        for(int i=0; i<this.numZoomLevels; i++) {
        	Util.printDebugMessage(" ...zoom level " + i + ": " + "resolution: " + this.resolutions[i] + " scale: " + this.scales[i] + "...");
        }
        */
	}
	
	/**
	 * API Method: clone
	 * 
	 * @return layer
	 * a clone of the current layer
	 */
	public Layer clone() {
		// TODO:
		return null;
	}
	
	/**
	 * API Method: getExtent
	 * 
	 * @return extent
	 * get the current visible extent from map
	 */
	public Envelope getExtent() {
		return this.map.calculateBounds();
	}
	
	/**
	 * API Method: getResolution
	 * 
	 * @return the resolution
	 * get the current resolution from map
	 */
	public double getResolution() {
		int zoom = this.map.getZoom();
        return this.map.getResolutionForZoom(zoom);
	}
	
	/**
	 * API Method: getZoomForExtent
	 * 
	 * @param extent
	 * @return zoom
	 * calculate zoom based on current extent
	 */
	public int getZoomForExtent(Envelope extent) {
        Size viewSize = this.map.getSize();
        double idealResolution = Math.max(extent.getWidth()/viewSize.getWidth(), extent.getHeight()/viewSize.getHeight());
        return this.getZoomForResolution(idealResolution);
    }
	
	/**
	 * API Method: getZoomForResolution
	 * 
	 * @param resolution 
	 * @return zoom
	 * calculate zoom based on current resolution
	 */
	public int getZoomForResolution(double resolution) {
		int zoom;        
        // TODO: deal with fractional zoom
    	int i;
    	for(i=1; i<this.resolutions.length; i++) {
            if(this.resolutions[i] < resolution) {
                break;
            }
        }
        zoom = i - 1;        
        return zoom;
	}
	
	/**
	 * API Method: getLonLatFromViewPortPx
	 * Returns a map location given a pixel location. 
	 * 
	 * @param viewPortPx
	 * @return lonlat
	 * calculate the map coordinate from a screen location.
	 */
	public Coordinate getCoordinateFromViewPortPx(Pixel viewPortPx) {		
		Coordinate coord  = null;
	    if(viewPortPx != null) {
	        Size size = this.map.getSize();
	        Coordinate center = this.map.getCenter();
	        if(center != null) {
	            double res  = this.map.getResolution();	    
	            double dx = viewPortPx.getX() - (size.getWidth()/2);
	            double dy = viewPortPx.getY() - (size.getHeight()/2);	        
	            coord = new Coordinate(center.x+dx*res, center.y-dy*res); 
	        }
	    }
	    if(this.wrapDateLine == true) {
	    	// TODO: deal with wrapDateLine
	    }	  
	    return coord;
	}
	
	/**
	 * API Method: getViewPortPxFromLonLat
	 * calculate the screen location from map coordinate.
	 * 
	 * @param lonlat
	 * @return viewPortPx
	 * calculate the screen location from map coordinate.
	 */
	public Pixel getViewPortPxFromCoordinate(Coordinate coord) {		
		Pixel pixel = null; 
		if(coord != null) {
            double res = this.map.getResolution();
            Envelope extent = this.map.getExtent();
            pixel = new Pixel(
            	(1/res * (coord.x - extent.getMinX())),
            	(1/res * (extent.getMaxY() - coord.y))
            );    
        }
        return pixel;        
	}
	
	/**
	 * API Method: getResolutionForZoom
	 * 
	 * @param zoom 
	 * @return resolution
	 * calculate current resolution based on zoom level
	 */
	public double getResolutionForZoom(int zoom) {
		zoom = Math.max(0, Math.min(zoom, this.resolutions.length - 1));
        double res;
        if(this.map.isFractionalZoom()) {
            // TODO: deal with fractional zoom
        	res = this.resolutions[Math.round(zoom)];
        } else {
            res = this.resolutions[Math.round(zoom)];
        }
        return res;
	}
	
	/**
	 * API Method: moveTo
	 * 
	 * @param bounds
	 * @param zoomChanged
	 * @param dragging
	 * 
	 */
	public void moveTo(Envelope bounds, boolean zoomChanged) {
	    // it's always called by layer.redraw() so always set 'centerChanged' to false
		moveTo(bounds, zoomChanged, false);
	}
	
	/**
	 * API Method: moveTo
	 * 
	 * @param bounds
	 * @param zoomChanged
	 * @param centerChanged
	 * @param dragging
	 */
	public void moveTo(Envelope bounds, boolean zoomChanged, boolean centerChanged) {
		// just re-check if layer is still in range and visible after move
		// leave the real move to subclass of layer
		//Util.printDebugMessage("@...Layer.moveTo() is called...");
		boolean display = this.visible;
	    if(this.isBaseLayer == false) {
	        display = display && this.inRange;
	    }
	    this.visible = display;	    
	    //Util.printDebugMessage(" ...is layer visible: " + this.visible + "...");
	}
	
	/**
	 * 
	 * @param bounds
	 * @param zoomChanged
	 * @param centerChanged
	 * @param dragging
	 */
	public void drag(int dx, int dy) {
		// just re-check if layer is still in range and visible after move
		// leave the real move to subclass of layer
		boolean display = this.visible;
	    if(this.isBaseLayer == false) {
	        display = display && this.inRange;
	    }
	    this.visible = display;	    	    
	}
	
	/**
	 * API Method: calculateInRange
	 * 
	 * @return
	 * whether the resolution of layer is between max and min resolution level 
	 */
	public boolean calculateInRange() {
		boolean inRange = false;
	    if(this.alwaysInRange == true) {
	    	inRange = true;
	    } else {
	    	if(this.map != null) {	
	    		// TODO: this has problem when map.baselayer is not set, resolution is returned as -1 - 
	    		// - which causes the inRange is always false for non baselayer
	    		double resolution = this.map.getResolution();
	    		inRange = ((resolution >= this.minResolution) && (resolution <= this.maxResolution));
	    	}
	    }
	    return inRange;
	}	
	
	/**
	 * API Method: adjustBoundsByGutter
	 * 
	 * @param bounds
	 * @return
	 * add gutter margin to the actual extent
	 */
	public Envelope adjustBoundsByGutter(Envelope bounds) {
		double mapGutter = this.gutter * this.map.getResolution();
        bounds = new Envelope(	
        						bounds.getMinX() - mapGutter,
        						bounds.getMaxX() + mapGutter,
        						bounds.getMinY() - mapGutter,                                
                                bounds.getMaxY() + mapGutter);
        return bounds;
	}	
	
	/**
	 * public void cancelLoadingThreads()
	 * 
	 */
	public void cancelLoadingThreads() {
		// do nothing in Layer
		// must be implemented by subclass to apply thread canceling
	}
	
	
	// ===========================================================
	// Getters & Setters
	// ===========================================================	
	
	/**
	 * API Method: getUrl
	 */
	public String getUrl(Envelope bounds) {
		// for subclass to override
		return "";
	}
	
	/**
	 * @return the map
	 */
	public Map getMap() { 
		return this.map; 
	}
	
	/**
	 * @return the units
	 */
	public String getUnits() {
		return this.units;
	}
	
	/**
	 * @return the inRange
	 */
	public boolean isInRange() {
		return inRange;
	}

	/**
	 * @param inRange the inRange to set
	 */
	public void setInRange(boolean inRange) {
		this.inRange = inRange;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		if(this.visible != visible) {
			this.visible = visible;
			this.redraw();
	        if(this.map != null) {
	        	Event event = new Event();
				event.properties.put("data", this);
				event.properties.put("incident", "visibility");
	            this.map.getEvents().triggerEvent(MapEvents.LAYER_CHANGED, event);
	        }	        
	        this.events.triggerEvent(LayerEvents.VISIBILITY_CHANGED, new Event(LayerEvents.VISIBILITY_CHANGED, null));
		}
	}

	/**
	 * @return the isBaseLayer
	 */
	public boolean isBaseLayer() {
		return isBaseLayer;
	}

	/**
	 * @param isBaseLayer the isBaseLayer to set
	 */
	public void setBaseLayer(boolean isBaseLayer) {
		if(this.isBaseLayer != isBaseLayer) {
			this.isBaseLayer = isBaseLayer;			
	        if(this.map != null) { // in case layer is already added into map, and being switched to a baselayer 
				this.map.getEvents().triggerEvent(MapEvents.BASELAYER_CHANGED, new Event(MapEvents.BASELAYER_CHANGED, this));	        	
	        }
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if(this.name.equals(name) == false) {
			this.name = name;
			if(this.map != null) {
				Event event = new Event();
				event.properties.put("data", this);
				event.properties.put("incident", "name");
	            this.map.getEvents().triggerEvent(MapEvents.LAYER_CHANGED, event);
	        }
		}
	}

	/**
	 * @return the alwaysInRange
	 */
	public boolean isAlwaysInRange() {
		return alwaysInRange;
	}

	/**
	 * @param alwaysInRange the alwaysInRange to set
	 */
	public void setAlwaysInRange(boolean alwaysInRange) {
		this.alwaysInRange = alwaysInRange;
	}

	/**
	 * @return the imageOffset
	 */
	public Pixel getImageOffset() {
		return imageOffset;
	}

	/**
	 * @param imageOffset the imageOffset to set
	 */
	public void setImageOffset(Pixel imageOffset) {
		this.imageOffset = imageOffset;
	}

	/**
	 * @return the gutter
	 */
	public int getGutter() {
		return gutter;
	}

	/**
	 * @param gutter the gutter to set
	 */
	public void setGutter(int gutter) {
		this.gutter = gutter;
	}

	/**
	 * @return the displayOutsideMaxExtent
	 */
	public boolean isDisplayOutsideMaxExtent() {
		return displayOutsideMaxExtent;
	}

	/**
	 * @param displayOutsideMaxExtent the displayOutsideMaxExtent to set
	 */
	public void setDisplayOutsideMaxExtent(boolean displayOutsideMaxExtent) {
		this.displayOutsideMaxExtent = displayOutsideMaxExtent;
	}

	/**
	 * @return the buffer
	 */
	public int getBuffer() {
		return buffer;
	}

	/**
	 * @param buffer the buffer to set
	 */
	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}

	/**
	 * @return the imageSize
	 */
	public Size getImageSize() {
		return imageSize;
	}

	/**
	 * @param imageSize the imageSize to set
	 */
	public void setImageSize(Size imageSize) {
		this.imageSize = imageSize;
	}

	/**
	 * @return the maxExtent
	 */
	public Envelope getMaxExtent() {
		return maxExtent;
	}

	/**
	 * @param maxExtent the maxExtent to set
	 */
	public void setMaxExtent(Envelope maxExtent) {
		this.maxExtent = maxExtent;
	}

	/**
	 * @return the maxResolution
	 */
	public double getMaxResolution() {
		return maxResolution;
	}

	/**
	 * @param maxResolution the maxResolution to set
	 */
	public void setMaxResolution(double maxResolution) {
		this.maxResolution = maxResolution;
	}

	/**
	 * @return the minResolution
	 */
	public double getMinResolution() {
		return minResolution;
	}

	/**
	 * @param minResolution the minResolution to set
	 */
	public void setMinResolution(double minResolution) {
		this.minResolution = minResolution;
	}

	/**
	 * @return the numZoomLevels
	 */
	public int getNumZoomLevels() {
		return numZoomLevels;
	}

	/**
	 * @param numZoomLevels the numZoomLevels to set
	 */
	public void setNumZoomLevels(int numZoomLevels) {
		this.numZoomLevels = numZoomLevels;
	}

	/**
	 * @return the scales
	 */
	public double[] getScales() {
		return scales;
	}

	/**
	 * @param scales the scales to set
	 */
	public void setScales(double[] scales) {
		this.scales = scales;
	}

	/**
	 * @return the resolutions
	 */
	public double[] getResolutions() {
		return resolutions;
	}

	/**
	 * @param resolutions the resolutions to set
	 */
	public void setResolutions(double[] resolutions) {
		this.resolutions = resolutions;
	}

	/**
	 * @return the projection
	 */
	public String getProjection() {
		return projection;
	}

	/**
	 * @param projection the projection to set
	 */
	public void setProjection(String projection) {
		this.projection = projection;
	}

	/**
	 * @return the maxScale
	 */
	public double getMaxScale() {
		return maxScale;
	}

	/**
	 * @param maxScale the maxScale to set
	 */
	public void setMaxScale(double maxScale) {
		this.maxScale = maxScale;
	}

	/**
	 * @return the minScale
	 */
	public double getMinScale() {
		return minScale;
	}

	/**
	 * @param minScale the minScale to set
	 */
	public void setMinScale(double minScale) {
		this.minScale = minScale;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @return the opacity
	 */
	public double getOpacity() {
		return opacity;
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}

	/**
	 * @return the wrapDateLine
	 */
	public boolean isWrapDateLine() {
		return wrapDateLine;
	}

	/**
	 * @param wrapDateLine the wrapDateLine to set
	 */
	public void setWrapDateLine(boolean wrapDateLine) {
		this.wrapDateLine = wrapDateLine;
	}
	
	/**
	 * 
	 * @return
	 */
	public Paint getPaint() {
		return paint;
	}
	
	public Bitmap getPreviousSnapshot() {
		return previousSnapshot;
	}
	public void setPreviousSnapshot(Bitmap previousSnapshot) {
		this.previousSnapshot = previousSnapshot;
	}

	/**
	 * 
	 * @author ying4682
	 *
	 */
	private class LayerEventsHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			switch(msg.what) {
				case LayerEvents.FEATURE_ADDED:
					//Util.printDebugMessage(" ...LayerEventsHandler: feature added...");
					break;
				case LayerEvents.FEATURES_ADDED:
					//Util.printDebugMessage(" ...LayerEventsHandler: features added...");
					break;
				case LayerEvents.LOAD_CANCELED:
					//Util.printDebugMessage(" ...LayerEventsHandler: load canceled...");
					break;
				case LayerEvents.LOAD_END:
					//Util.printDebugMessage(" ...LayerEventsHandler: load end...");
					break;
				case LayerEvents.LOAD_START:
					//Util.printDebugMessage(" ...LayerEventsHandler: load start...");
					break;
				case LayerEvents.MOVE_END:
					//Util.printDebugMessage(" ...LayerEventsHandler: move end...");
					break;
				case LayerEvents.TILE_LOADED:
					//Util.printDebugMessage(" ...LayerEventsHandler: tile loaded...");
					break;
				case LayerEvents.VISIBILITY_CHANGED:
					//Util.printDebugMessage(" ...LayerEventsHandler: visibility changed...");
					break;
			}
		}
	}
	
}
