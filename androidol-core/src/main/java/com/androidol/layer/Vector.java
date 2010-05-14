package com.androidol.layer;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import com.androidol.Map;
import com.androidol.events.Event;
import com.androidol.events.LayerEvents;

import com.androidol.protocol.Protocol;
import com.androidol.renderer.CanvasRenderer;
import com.androidol.renderer.Renderer;
import com.androidol.strategy.Strategy;
import com.androidol.style.Style;
import com.androidol.style.StyleMap;
//import com.esri.android.openlayers.util.Util;
import com.vividsolutions.jts.geom.Envelope;


public class Vector extends Layer {

	// ===========================================================
	// Fields
	// ===========================================================	
		
	protected boolean 												isBaseLayer 		= false;
	protected boolean 												isFixed 			= false;
	public 	  final boolean 										isVector 			= true;	
	
	protected ArrayList<com.androidol.feature.Vector> features			= null;
	protected ArrayList<com.androidol.feature.Vector> selectedFeatures	= null;
	
	protected Style													style				= null;
	protected StyleMap												styleMap			= null;
	
	protected ArrayList<Strategy>									strategies			= null;
	protected Protocol												protocol			= null;							
	
	protected Renderer												renderer			= null;
	protected String												rendererClass		= "";
	protected String												geometryType		= "";
	
	protected boolean												drawn				= false;
	
	public Vector(Context context) {
		super(context);
		// TODO: if options.get("style") is null give a default style
		this.style =  new Style();
		// TODO: initialize the styleMap
		
		this.features = new ArrayList<com.androidol.feature.Vector>();
		this.selectedFeatures = new ArrayList<com.androidol.feature.Vector>();				
		
		if(this.strategies != null){
            for(int i=0, len=this.strategies.size(); i<len; i++) {
                this.strategies.get(i).setLayer(this);
            }
        } else {
        	this.strategies = new ArrayList<Strategy>();
        }
		
		// register default event callback handler		
		this.events.registerAll(new DefaultFeaturesAddedCallback());
	}
	
	public Vector(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO: if options.get("style") is null give a default style
		this.style =  new Style();
		// TODO: initialize the styleMap
		
		this.features = new ArrayList<com.androidol.feature.Vector>();
		this.selectedFeatures = new ArrayList<com.androidol.feature.Vector>();				
		
		if(this.strategies != null){
            for(int i=0, len=this.strategies.size(); i<len; i++) {
                this.strategies.get(i).setLayer(this);
            }
        } else {
        	this.strategies = new ArrayList<Strategy>();
        }
		
		// register default event callback handler		
		this.events.registerAll(new DefaultFeaturesAddedCallback());
	}
	/**
	 * Old Vector constructor
	 * 
	 * @param name
	 * @param options
	 */
	/*
	public Vector(String name, HashMap<String, Object> options) {		
		
		super(name, options);		
		
		// TODO: if options.get("style") is null give a default style
		this.style =  new Style();
		// TODO: initialize the styleMap
		
		this.features = new ArrayList<com.androidol.feature.Vector>();
		this.selectedFeatures = new ArrayList<com.androidol.feature.Vector>();				
		
		if(this.strategies != null){
            for(int i=0, len=this.strategies.size(); i<len; i++) {
                this.strategies.get(i).setLayer(this);
            }
        } else {
        	this.strategies = new ArrayList<Strategy>();
        }
		
		// register default event callback handler		
		this.events.registerAll(new DefaultFeaturesAddedCallback());
	}
	*/
	/**
	 * 
	 * @param name
	 */
	/*
	public Vector(String name) {
		this(name, null);
	}
	*/
	
	/**
	 * API Method: setStrategies
	 * 
	 * @param strategies
	 */
	public void setStrategies(ArrayList<Strategy> strategies) {
		if(strategies!=null && strategies.size()>0) {
			this.strategies = strategies;
			for(int i=0, len=this.strategies.size(); i<len; i++) {
	            this.strategies.get(i).setLayer(this);
	            if(this.strategies.get(i).isAutoActivate() == true) {                   
	            	this.strategies.get(i).activate();
                }
	            if(this.protocol != null) {
	            	this.protocol.getEvents().registerAll(this.strategies.get(i).getProtocolHandler());
	            }
	        }
		}		
	}
	
	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
		if(this.strategies != null) {
            Strategy strategy;
            int i, len;
            for(i=0, len=this.strategies.size(); i<len; i++) {
                strategy = this.strategies.get(i);
                if(strategy.getProtocolHandler() != null) {                                   	
	            	this.protocol.getEvents().registerAll(strategy.getProtocolHandler());
                }
            }
        }
	}
	
	/**
	 * API Method: addStrategy
	 * @param strategy
	 */
	public void addStrategy(Strategy strategy) {
		if(this.strategies == null) {
			this.strategies = new ArrayList<Strategy>();		
		}
		strategy.setLayer(this);
		this.strategies.add(strategy);		
		if(strategy.getProtocolHandler() != null) {                   			
			this.protocol.getEvents().registerAll(strategy.getProtocolHandler());
        }
		if(strategy.isAutoActivate() == true) {                   
        	strategy.activate();
        }
	}
	
	/**
	 * API Method: removeStrategy
	 * @param strategy
	 */
	public void removeStrategy(Strategy strategy) {
		// TODO: remove strategy from this.strategies
		// TODO: unregister protocol handler of strategy from protocol.events
	}
	
	/**
	 * 
	 * @param map
	 */
	private void assignRenderer(Map map) {
		// TODO: instantiate different renderer based on this.rendererClass		
		this.renderer = new CanvasRenderer(this.canvas); 
	}
	
	/**
     * API Method: destroy
     * Destroy this layer
     */
    public void destroy() {
        if(this.strategies != null) {
        	Strategy strategy; 
            int i, len;
            for(i=0, len=this.strategies.size(); i<len; i++) {
                strategy = this.strategies.get(i);
                if(strategy.isAutoDestroy()) {
                    strategy.destroy();
                }
            }
            this.strategies = null;
        }
        if(this.protocol != null) {
            if(this.protocol.isAutoDestroy()) {
                this.protocol.destroy();
            }
            this.protocol = null;
        }
        this.destroyFeatures(null, true);
        this.features = null;
        this.selectedFeatures = null;
        if(this.renderer != null) {
            this.renderer.destroy();
        }
        this.renderer = null;
        this.geometryType = null;
        this.drawn = false;
        super.destroy(true); // destroy and reset baselayer  
    }
    
    /**
     * API Method: refresh
     * Ask the layer to request features again and redraw them.  Triggers
     *     the refresh event if the layer is in range and visible.
     *     
     * @param obj - {Object} Optional object with properties for any listener of
     *     the refresh event.
     */
    public void refresh() {
        if(this.inRange && this.visible==true) {
        	// TODO: trigger "refresh" event
            //this.events.triggerEvent("refresh", obj);
        }
    }
    
    /** 
     * API Method: setMap
     * The layer has been added to the map. 
     * 
     * If there is no renderer set, the layer can't be used. Remove it.
     * Otherwise, give the renderer a reference to the map and set its size.
     * 
     * @param map - {Map} 
     */
    @Override
    public void setMap(Map map) {        
        super.setMap(map);
        if(this.renderer == null) {
            assignRenderer(this.map);
        }
        this.renderer.setMap(this.map);
        this.renderer.setSize(this.map.getSize());
        
        if(this.strategies != null) {
            Strategy strategy;
            int i, len;
            for(i=0, len=this.strategies.size(); i<len; i++) {
                strategy = this.strategies.get(i);
                if(strategy.isAutoActivate() == true) {                   
                	strategy.activate();
                }
            }
        }
    }

    /**
     * API Method: removeMap
     * The layer has been removed from the map.
     *
     * @param map - {Map}
     */
    public void removeMap(Map map) {
        if(this.strategies != null) {
        	Strategy strategy;
            int i, len;
            for(i=0, len=this.strategies.size(); i<len; i++) {
                strategy = this.strategies.get(i);
                if(strategy.isAutoActivate()) {                  
                	strategy.deactivate();
                }
            }
        }
    }
    
    /**
     * API Method: onMapResize
     * Notify the renderer of the change in size. 
     * 
     */
    @Override
    public void onMapResize() {
        super.onMapResize();
        /*
         * OpenScales erase and redraw each feature in the layer
         * Should I do it? 
         */
        this.renderer.setSize(this.map.getSize());
    }
    
    /**
     * API Method: moveTo
     *  Reset the vector layer's div so that it once again is lined up with 
     *   the map. Notify the renderer of the change of extent, and in the
     *   case of a change of zoom level (resolution), have the 
     *   renderer redraw features.
     * 
     *  If the layer has not yet been drawn, cycle through the layer's 
     *   features and draw each one.
     * 
     * @param bounds - {Bounds} 
     * @param zoomChanged - {boolean} 
     * dragging - {boolean} 
     */
    @Override
    public void moveTo(Envelope bounds, boolean zoomChanged, boolean centerChanged) {
        super.moveTo(bounds, zoomChanged, centerChanged);
        //Util.printDebugMessage("@...moveTo() of vector layer is called...");                  
        Envelope extent = this.map.getExtent();           
        this.renderer.setExtent(extent, zoomChanged);                                      
        if(this.drawn == false || zoomChanged) {
        	//Util.printDebugMessage(" ...draw each feature in vector layer...");
            this.drawn = true;
            com.androidol.feature.Vector feature;
            for(int i=0, len=this.features.size(); i<len; i++) {                   
                feature = this.features.get(i);               
                this.drawFeature(feature);
            }
        }    
    }
    
    /**
     * API Method: addFeatures
     * Add Features to the layer.
     *
     * @param features - {ArrayList<com.androidol.feature.Vector>} 
     * @param slient - {boolean}
     */
    public void addFeatures(ArrayList<com.androidol.feature.Vector> features, boolean slient) {
        if(slient == false) {
            // TODO: trigger 'before features added' event
        }
        for(int i=0, len=features.size(); i<len; i++) {
                
        	com.androidol.feature.Vector feature = features.get(i);
            
            if(this.geometryType!=null && feature.getGeometry().getClass().getName().equalsIgnoreCase(this.geometryType)==true) {
            	// TODO: this type of geometry is banned, so throw exception
            }              
            this.features.add(feature);
            feature.setLayer(this);

            if((feature.getStyle()!=null) && (this.style!=null)) {
                // TODO: merge feature.style with this.style?
            	// TODO: or over write one with another? 
            }
            
            this.preFeatureInsert(feature);
            if(this.drawn) {
                this.drawFeature(feature);
            }
            
            if(slient == false) {
            	// TODO: trigger 'before feature added' event
                this.onFeatureInsert(feature);
            }
        }        
        if(slient == false) {
            // TODO: trigger 'features added' event  
        	Event event = new Event();
		    event.properties.put("type", LayerEvents.FEATURES_ADDED);					    
		    event.properties.put("data", features);
		    this.events.triggerEvent(LayerEvents.FEATURES_ADDED, event);
        }
    }
    
    /**
     * API Method: removeFeatures
     * 
     * @param features - {Array(Vector>)} 
     * @param options - {Object}
     */
    public void removeFeatures(ArrayList<com.androidol.feature.Vector> features, boolean slient) {
        if(features==null || features.size() == 0) {
            return;
        }       
        for(int i=features.size()-1; i>=0; i--) {
            // We remain locked so long as we're not at 0
            // and the 'next' feature has a geometry. We do the geometry check
            // because if all the features after the current one are 'null', we
            // won't call eraseGeometry, so we break the 'renderer functions
            // will always be called with locked=false *last*' rule. The end result
            // is a possible gratiutious unlocking to save a loop through the rest 
            // of the list checking the remaining features every time. So long as
            // null geoms are rare, this is probably okay.                   
        	com.androidol.feature.Vector feature = features.get(i);            
        	// TODO: ? this.unrenderedFeatures.remove(feature);
            if(slient == false) {
            	// TODO: trigger 'before feature removed' event              	
            }
            this.features.remove(feature);
            // feature has no layer at this point
            feature.setLayer(null);

            if(feature.getGeometry() != null) {
                this.renderer.eraseGeometry(feature.getGeometry());
            }
                    
            //in the case that this feature is one of the selected features, 
            // remove it from that array as well.
            this.selectedFeatures.remove(feature);            
            if(slient == false) {
                // TODO: TODO: trigger 'feature removed' event
            }
        }
        if(slient == false) {            
        	// TODO: TODO: trigger 'features removed' event
        }
    }
    
    /**
     * API Method: destroyFeatures
     * Erase and destroy features on the layer.
     *
     * @param features - {Array(<OpenLayers.Feature.Vector>)} An optional array of
     *     features to destroy.  If not supplied, all features on the layer
     *     will be destroyed.
     * options - {Object}
     */
    public void destroyFeatures(ArrayList<com.androidol.feature.Vector> features, boolean slient) {    	       
		if(features == null) { // if 'features' not defined, destroy all
			features = this.features;
		}
		if(features != null) {
			this.removeFeatures(features, slient);
			for(int i=features.size()-1; i>=0; i--) {
				features.get(i).destroy();
			}
		}
    }
    
    /**
     * API Method: drawFeature
     * Draw (or redraw) a feature on the layer.  If the optional style argument
     * is included, this style will be used.  If no style is included, the
     * feature's style will be used.  If the feature doesn't have a style,
     * the layer's style will be used.
     *  
     * @param feature - {<com.androidol.feature.Vector>} 
     * @param style - {Object} Symbolizer hash or {String} renderIntent
     */
    public void drawFeature(com.androidol.feature.Vector feature, Style style) {
    	if(style == null) {
            if(feature.getStyle() != null) {
                style = feature.getStyle();
            } else {
                style = this.style;
            }
        }
    	if(style == null) {
            //style = this.styleMap.createSymbolizer(feature, feature.getRenderIntent());
        }
    	this.renderer.drawFeature(feature, style);    	
    }
    
    /**
     * 
     */
    public void drawFeature(com.androidol.feature.Vector feature) {
    	drawFeature(feature, null);
    }
    
    /**
     * API Method: eraseFeatures
     * Erase features from the layer.
     *
     * @param features - {Array(<com.androidol.feature.Vector>)} 
     */
    public void eraseFeatures(ArrayList<com.androidol.feature.Vector> features) {
        this.renderer.eraseFeatures(features);
    }
    
    /**
     * API Method: getFeatureFromEvent
     * Given an event, return a feature if the event occurred over one.
     * Otherwise, return null.
     *
     * @param evt - {Event} 
     *
     * @return {<OpenLayers.Feature.Vector>} A feature if one was under the event.
     */
    public com.androidol.feature.Vector getFeatureFromEvent(Event evt) {           
        String featureId = this.renderer.getFeatureIdFromEvent(evt);
        return this.getFeatureById(featureId);
    }
    
    /**
     * API Method: getFeatureById
     * Given a feature id, return the feature if it exists in the features array
     *
     * @param featureId - {String} 
     *     
     * @return {<OpenLayers.Feature.Vector>} A feature corresponding to the given
     * featureId
     */
    public com.androidol.feature.Vector getFeatureById(String featureId) {
        //TBD - would it be more efficient to use a hash for this.features?
    	com.androidol.feature.Vector feature = null;
        for(int i=0, len=this.features.size(); i<len; ++i) {
            if(this.features.get(i).getId().equalsIgnoreCase(featureId)==true) {
                feature = this.features.get(i);
                break;
            }
        }
        return feature;
    }
    
    /**
     * 
     */
    public void preFeatureInsert(com.androidol.feature.Vector feature) {
    	// TODO: to be implemented by sub class
    }
    
    /**
     * 
     */
    public void onFeatureInsert(com.androidol.feature.Vector feature) {
    	// TODO: to be implemented by sub class
    }

	/**
	 * @return the protocol
	 */
	public Protocol getProtocol() {
		return protocol;
	}

	/**
	 * API Method: getStrategies
	 * @return
	 */
	public ArrayList<Strategy> getStrategies() {
		return this.strategies;
	}
    
	/**
	 * 
	 * @author ying4682
	 *
	 */
	private class DefaultFeaturesAddedCallback extends Handler {		
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == LayerEvents.FEATURES_ADDED) {
				//Util.printDebugMessage(" ...LayerEvents.FEATURE_ADDED captured...");
				//Util.printDebugMessage(" ...DefaultFeaturesAddedCallback call map.invalidate() to redraw map...");
				Vector.this.map.invalidate();
			}		
		}		
	}
}


