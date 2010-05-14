package com.androidol;

import java.util.ArrayList;

import com.androidol.basetypes.Pixel;
import com.androidol.basetypes.Size;
import com.androidol.control.Control;
import com.androidol.events.Event;
import com.androidol.events.LayerEvents;
import com.androidol.events.MapEvents;
import com.androidol.layer.Grid;
import com.androidol.layer.Layer;
import com.androidol.layer.osm.Mapnik;
import com.androidol.map.schema.MapSchema;
import com.androidol.map.schema.OSMMapSchema;
import com.androidol.map.schema.OSMTileMapSchema;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ZoomControls;


public class Map extends ViewGroup {

	// ===========================================================
	// Constants
	// ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
	//
	protected String 				name 						= "androidol_map";
	protected String 				title 						= "androidol_map";
	private   boolean 				initialized					= false;	
	//
	protected Size 					size; 						// size of the map, should fixed to phone screen size	
	protected Coordinate 			center; 	
	protected int 					zoom;
	protected double 				resolution;
	protected double 				scale;
	protected String 				projection;
	protected String				units;
	protected final boolean 		fractionalZoom				= false;	// TODO: support fractional zoom
	protected MapSchema				schema;
	//
	protected int 					numZoomLevels;
	protected int 					maxZoomLevel;
	protected int 					minZoomLevel;	
	protected Envelope 				maxExtent;
	protected Envelope 				minExtent;
	protected double 				maxResolution;
	protected double 				minResolution;
	protected double 				maxScale;
	protected double 				minScale;		
	protected double[]				scales;
	protected double[] 				resolutions;
	//	
	protected ArrayList<Layer>		layers;
	protected ArrayList<Control>	controls;		
	protected Layer					baseLayer;
	//	
	protected Coordinate			layerContainerOrigin;
	protected final double			layerContainerCanvasLeft	= 0.0; // TODO: related to view padding
	protected final double			layerContainerCanvasTop		= 0.0; // TODO: related to view padding	
	//
	protected Context 				context;
	protected MapEvents				events						= new MapEvents();
	protected MapEventsHandler		mapEventsHandler			= new MapEventsHandler();
	protected GestureDetector 		mapViewGestureDetector 		= new GestureDetector(new MapViewGestureListener());
	
	protected ZoomControls			zoomControls;
	protected Bitmap				previousSnapShot			= null;
	protected Paint					paint						= new Paint();
	
	// ===========================================================
	// Constructors and methods inherited from ViewGroup
	// ===========================================================
	
	public Map(Context context) {
		super(context, null);
		this.context = context;
		// implement OnHierarchyChangeListener interface to track child view changes
		/*
		this.setOnHierarchyChangeListener(
			new OnHierarchyChangeListener() {
				@Override
				public void onChildViewAdded(View parent, View child) {}
				@Override
				public void onChildViewRemoved(View parent, View child) {					}
			}
		);
		*/
	}
	
	public Map(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		try {
			// TODO: initialize map with default configuration
			//       like 'name' and 'title' etc.
			// TODO: initialize map from layout configuration over default settings
			initialize(); // initialize map itself			
		} catch(Exception e) {
			// TODO: map initialization failure throw exception
			Util.printErrorMessage("...fail to initialize map..." + e.getMessage());
		}				
		// implement OnHierarchyChangeListener interface to track child view changes				
		this.setOnHierarchyChangeListener(
			new OnHierarchyChangeListener() {
				@Override
				public void onChildViewAdded(View parent, View child) {
					//Util.printDebugMessage("...onChildViewAdded...");					
				}
				@Override
				public void onChildViewRemoved(View parent, View child) {					
					//Util.printDebugMessage("...onChildViewRemoved...");
				}
			}
		);
		
		if(this.initialized == true) { 
			// set this.initialized to true only when all layers from layout xml are added into map -
			// - will set it to true in first call of dispatchDraw() of map view group
			this.initialized = false;
		}
		// TODO: research on setDrawingCacheEnabled()
		// doesn't sound helpful much here
		//this.setDrawingCacheEnabled(true);
	}

	/**
	 * Override generateDefaultLayoutParams()
	 */
	@Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }
	
	/**
	 * generateLayoutParams(AttributeSet attrs)
	 * 
	 * Override this method such that child view of map (ViewGroup) doesn't have to specify - 
	 * - LayoutParams.FILL_PARENT and LayoutParams.FILL_PARENT
	 * 
	 * Any child view that needs LayoutParams other than FILL_PARENT must call -
	 * - super.addView(child, index, layoutParams) with a different layoutParams like below:
	 * - super.addView(child, index, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {        
		// always returns new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		return generateDefaultLayoutParams();
    }
	
	/**
	 * Override onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	 */
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {				
		//Util.printDebugMessage("...onMeasure() is called...");
		// measure child views
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }		
		// measure myself
		int maxHeight = getSuggestedMinimumWidth();
        int maxWidth = getSuggestedMinimumHeight();        
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();        
        /*
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int wSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        int hSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        */
        int resolvedWidth = resolveSize(maxWidth, widthMeasureSpec);
        int resolvedHeight = resolveSize(maxHeight, heightMeasureSpec);
                
        setMeasuredDimension(
        	resolvedWidth,
        	resolvedHeight
        );	        
	}
	
	/**
	 * Override onLayout(boolean changed, int left, int top, int right, int bottom)
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {		
		//Util.printDebugMessage("...onLayout() is called...");
		// size must be set in or after onLayout is called -
		// - getWidth() and getHeight() get the actual size
        this.size = new Size(this.getWidth(), this.getHeight());
        // layout child views
		int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {                
            	final int childLeft = getPaddingLeft();
                final int childTop = getPaddingTop();
                final int childMeasuredWidth = child.getMeasuredWidth();
                final int childMeasuredHeight = child.getMeasuredHeight();
                child.layout(
                	childLeft, 
                	childTop,
                    childLeft + childMeasuredWidth,
                    childTop + childMeasuredHeight
                );
            }
        }          
	}
    
	/**
	 * addView(View child, int index, LayoutParams params)
	 * 
	 * this is the addView() method being called by other addView in ViewGroup so - 
	 * - not necessary to override addView(View child), addView(View child, int index), or addView(View child, LayoutParams params)
	 * @return
	 */   	
	@Override
    public void addView(View child, int index, LayoutParams params) {
		//Util.printDebugMessage("..." + ((Layer)child).getName() + "...");
        //Util.printDebugMessage("...addView() is called...");        
		if(child instanceof Layer) {
        	Layer layer = (Layer)child;
        	// check if layer is already added
        	for(int i=0, len=this.layers.size(); i<len; i++) {
                if(this.layers.get(i) == layer) {            	            
                	Util.printWarningMessage(" ...layer already in the map...skip adding it...");
                	return;
                }
            }        	               	
        	if(index >= this.layers.size() || index < 0) {
        		this.layers.add(layer);          
        	} else {
        		this.layers.add(index, layer);
        	}
            layer.setMap(this); 
            super.addView(child, index, params);
            // follow code doesn't seem to be necessary
            // follow code is only useful when there is no base layer and a new base layer is added
            /*
            if(this.initialized == true) { // meaning layer being added after map is initialized (probably add layer on fly instead of in layout xml)
            	if(layer.isBaseLayer()) {
                    if(this.baseLayer == null) {
                    	// switch base layer if base layer is added
                        this.setBaseLayer(layer);	// set the first base layer we add as base layer
                    } else {
                        layer.setVisible(false);	// can not have multiple base layers at the same time 
                    }
                } else {
                	// draw overlay layer right after the layer is added
                    layer.postInvalidate();
                }
            }
            */
        	// if super.addView(child, index, params); is not called then -
        	// - handlers in setOnHierarchyChangeListener won't be triggered
        } else {        	
        	// initialize zoom controls
        	if(child instanceof ZoomControls) { // deal with ZoomControls
        		if(this.zoomControls != null) {
        			Util.printWarningMessage("...map already has a zoomControls...can not add another..skip adding...");
        			return;
        		}
        		this.zoomControls = (ZoomControls)child;
        		this.zoomControls.setZoomSpeed(150); // half second
        		this.zoomControls.setOnZoomInClickListener(new OnClickListener() {
        			public void onClick(View view) {
        				Map.this.zoomIn();
        			}
    		    });
        		this.zoomControls.setOnZoomOutClickListener(new OnClickListener() {
        			public void onClick(View view) {
        				Map.this.zoomOut();
        			}
    		    });
        		/*
        		 * since I override generateLayoutParameters() to always use FILL_PARENT for width and height -
        		 * - so I have to switch WRAP_CONTENT here for zoom controls because otherwise it will masks the touch events -
        		 * - on map
        		 */
        		super.addView(child, index, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        	} else {
        		// what else type of view could it be
        		super.addView(child, index, params);
        	}        	
        }		
    }
	
	/**
	 * removeView(View child)
	 */
	@Override
	public void removeView(View child) {
		if(child instanceof Layer) { 
			Layer layer = (Layer)child;
			if(layer == this.baseLayer) {
				// by design...can not remove base layer...unless you set another new baselayer first
				Util.printWarningMessage("...skip removing...can not remove base layer...unless you set another new baselayer first...");
				return;
			} else {
				this.layers.remove(layer);
				this.layers.trimToSize();
				super.removeView(layer);
				// not necessary to call map.invalidate() because super.removeView() shall trigger that
			}
		} else {
			super.removeView(child);
		}
	}	
	
	/**
	 * 
	 */	
	@Override
    protected void dispatchDraw(Canvas canvas) {
		//Util.printDebugMessage("...dispatchDraw...");
		//super.dispatchDraw(canvas);
		// the first time when map and layers are initialized from layout xml				
		if(this.initialized == false) {
			for(int i=0; i<this.getChildCount(); i++) {
				if(this.getChildAt(i) instanceof Layer) {
					Layer layer = (Layer)this.getChildAt(i); 
					if(layer.isBaseLayer()) {
			            if(this.baseLayer == null) {                
			                this.setBaseLayer(layer);	// set the first base layer we add as base layer
			            } else {
			                layer.setVisible(false);
			            }
			        } else {			            
			            //layer.postInvalidate();
			        	layer.invalidate();
			        }
				}
			}
			this.initialized = true;
		}/* else { // when map and layers are redrawn
			// redraw all overlay layers with current state										
			if(this.baseLayer != null) {
				this.baseLayer.redraw();
			}
			// draw layers based on their view order in view group
			for(int i=0; i<this.getChildCount(); i++) {
				if(getChildAt(i) instanceof Layer) {
					Layer layer = (Layer)getChildAt(i);
					if(layer!=null && layer.isBaseLayer()==false) {					
						layer.redraw();					
					}
				}
			}			
			// draw other controls like zoom controls			
			if(this.zoomControls != null) {
				this.zoomControls.draw(canvas);
			}		
		}*/		
		super.dispatchDraw(canvas);
	}
    	
    /**
	 * 
	 */	
	@Override
	public void onDraw(final Canvas canvas) {		
		
	}	
	
    // ===========================================================
	// Map API and private interfaces
	// ===========================================================
    private void initialize() {
    	if(this.schema == null) {
    		//this.schema = new OSMMapSchema();
    		this.schema = new OSMTileMapSchema();
    	}
    	// everything comes from map schema
    	this.numZoomLevels = this.schema.getNumZoomLevels();						//				
		this.maxZoomLevel = this.schema.getMaxZoomLevel();
		this.minZoomLevel = this.schema.getMinZoomLevel();
		this.zoom = this.schema.getDefaultZoomLevel();									// initial zoom level is the minimum zoom level 
		
		this.projection = this.schema.getProjection();
		this.units = this.schema.getUnits();
		this.center = this.schema.getDefaultCenter();
		
		this.resolutions = this.schema.getResolutions();							// resolutions could be null or an empty array		
		this.maxResolution = this.schema.getMaxResolution();						//
		this.minResolution = this.schema.getMinResolution();						//	
		this.resolution = this.schema.getDefaultResolution();
		if(this.resolution < 0) {
			this.resolution = Double.POSITIVE_INFINITY;
		}
		/*
		if(this.resolutions!=null && this.resolutions.length>0) {
			this.resolution = this.resolutions[this.zoom];				
		} else {
			if(this.maxResolution > 0) {
				this.resolution = this.maxResolution;
			} else if(this.minResolution > 0) {
				this.resolution = this.minResolution;
			} else {								 
				this.resolution = Double.POSITIVE_INFINITY;
			}
		}
		*/
		
		//this.scales = this.schema.getScales();									//
		//this.scale = this.scales[this.zoom];										//
		//this.maxScale = this.schema.getMaxScale();								//
		//this.minScale = this.schema.getMinScale(); 								//
		this.scales = null;															// scales will be calculated based on resolutions and DPI
		this.scale = Double.NEGATIVE_INFINITY;										//
		this.maxScale = Double.NEGATIVE_INFINITY;									//
		this.minScale = Double.NEGATIVE_INFINITY; 									//
				
		this.maxExtent = this.schema.getDefaultMaxExtent();							// default max extent
		this.minExtent = this.schema.getDefaultMinExtent();							// default min extent														// 
		
		// size can not be determined at ViewGroup's constructor -
		// - because this.getWidth() and this.getHeight() return 0 here
		//this.size = new Size(this.getWidth(), this.getHeight());					// map view size is the size of parent viewgroup		
		//this.fractionalZoom = false;												// whether to support fractional zoom, no by default		
		
		// register call-back handlers for different types of events
		this.events.registerAll(this.mapEventsHandler);
		// register double tap listeners
		this.mapViewGestureDetector.setOnDoubleTapListener(new MapViewDoubleTapListener());	// support double-tap event on map 
		this.mapViewGestureDetector.setIsLongpressEnabled(true);
		
		this.layers = new ArrayList<Layer>();											// empty layer list initially
		this.baseLayer = null;															// empty baselayer		
		this.controls = new ArrayList<Control>();										// empty control list initially					
		
		// TODO: register MOVE_START event with this.updateSize()
		// TODO: register all call-back in this.eventListeners		
    }
    
    private void applyMapSchema(MapSchema newSchema) {
    	if(schema != null) {
    		this.numZoomLevels = newSchema.getNumZoomLevels();						//				
    		this.maxZoomLevel = newSchema.getMaxZoomLevel();
    		this.minZoomLevel = newSchema.getMinZoomLevel();
    		this.zoom = newSchema.getDefaultZoomLevel();									// initial zoom level is the minimum zoom level 
    		
    		this.projection = newSchema.getProjection();
    		this.units = newSchema.getUnits();
    		this.center = newSchema.getDefaultCenter();
    		
    		this.resolutions = newSchema.getResolutions();							// resolutions could be null or an empty array		
    		this.maxResolution = newSchema.getMaxResolution();						//
    		this.minResolution = newSchema.getMinResolution();						//	
    		this.resolution = newSchema.getDefaultResolution();
    		if(this.resolution < 0) {
    			this.resolution = Double.POSITIVE_INFINITY;
    		}
    		/*
    		if(this.resolutions!=null && this.resolutions.length>0) {
    			this.resolution = this.resolutions[this.zoom];				
    		} else {
    			if(this.maxResolution > 0) {
    				this.resolution = this.maxResolution;
    			} else if(this.minResolution > 0) {
    				this.resolution = this.minResolution;
    			} else {								 
    				this.resolution = Double.POSITIVE_INFINITY;
    			}
    		}
    		*/
    		
    		//this.scales = newSchema.getScales();									//
    		//this.scale = this.scales[this.zoom];										//
    		//this.maxScale = newSchema.getMaxScale();								//
    		//this.minScale = newSchema.getMinScale(); 								//
    		this.scales = null;															// scales will be calculated based on resolutions and DPI
    		this.scale = Double.NEGATIVE_INFINITY;										//
    		this.maxScale = Double.NEGATIVE_INFINITY;									//
    		this.minScale = Double.NEGATIVE_INFINITY; 									//
    				
    		this.maxExtent = newSchema.getDefaultMaxExtent();							// default max extent
    		this.minExtent = newSchema.getDefaultMinExtent();	
    	}
    }
    
    /**
	 * API Method: destroy
	 * destroy the map
	 */
	public void destroy() {
		//Util.printDebugMessage("@...Map.destroy() is called...");	
		if(this.layers != null) {	// destroy layers
			for(int i=0; i<this.layers.size(); i++) {
				Layer layer = this.layers.get(i);
				if(layer != null) {
					// pass 'false' to destroy so that map wont try to set a new 
	                // baselayer after each baselayer is removed
					// TODO: uncomment this
					//layer.destroy(false);
				}
			}
			this.layers.clear();
			//Util.printDebugMessage(" ...clean up layer list...");
		}
		if(this.controls != null) { // destroy layers
			for(int i=0; i<this.controls.size(); i++) {
				Control control = this.controls.get(i);
				if(control != null) {
					control.destroy();
				}
			}
			this.controls.clear();
			//Util.printDebugMessage(" ...clean up control list...");
		}		
		// TODO: unregister all call-back handlers from this.events
		this.events.unregisterAll(this.mapEventsHandler);
		// TODO: unregister all other call-back handlers
	}
	
	/**
	 * APIMethod: addLayer
	 * 
	 * 
	 * @param layer {@link Layer}
	 * @return index
	 * return the index of layer after it is successfully added into map, if not or duplicated then return -1
	 */	
	public void addLayer(Layer layer) {
		//Util.printDebugMessage("@...Map.addLayer() is called...");
		//Util.printDebugMessage(" ...add layer: " + layer.getName() + "...");				
		// always append newly added layer at the end of the layers queue -
		// - don't worry about it will cover zoom controls because this.dispatchDraw() is overrided such that -
		// - zoom controls is always drawn on top.
		addView(layer, getChildCount()-1);
        // trigger map event 'LAYER_ADDED'        
        //this.events.triggerEvent(MapEvents.LAYER_ADDED, new Event(MapEvents.LAYER_ADDED, layer));
        //return this.layers.indexOf(layer);
	}
	
	/**
	 * setLayerZIndex(Layer layer, int zIdx)
	 * 
	 * @param layer
	 * @param zIdx
	 */
	public void setLayerZIndex(Layer layer, int zIdx) {
		//Util.printDebugMessage(" ...set z index for layer to: " + zIdx + "...");
		if(this.layers.contains(layer) == false) {
			Util.printDebugMessage("...layer doesn't exist in map...skip setting z index...");
			return;
		} else {
			// TODO: validate zIdx such that layer won't be under base-layer or above zoom controls  			
			if(layer != this.baseLayer) {				
				int oldIdx = this.layers.indexOf(layer);
				if(oldIdx != zIdx) {						
					this.removeView(layer);
					this.addView(layer, zIdx);
				}
				// TODO: also reset the index of layer view in map view group				
			} else {
				Util.printDebugMessage("...base layer is always at bottom...can not change its z index...");
			}	
			// TODO: trigger LAYER_CHANGED event with layer and order
		}						
	}
	
	/**
	 * to redraw each layer in map
	 */
	public void redraw() {
		// TODO: to be implemented
		invalidate();
	}
	
	/**
	 * API Method: setBaseLayer
	 * @param newBaseLayer
	 * 
	 */
	public void setBaseLayer(Layer newBaseLayer) {
		//Util.printDebugMessage("@...Map.setBaseLayer() is called...");
		Envelope oldExtent = null;
        if(this.baseLayer != null) {
            oldExtent = this.baseLayer.getExtent();
        }
        if(newBaseLayer != this.baseLayer) {                     
            if(this.layers.contains(newBaseLayer) == true) { 			// newBaseLayer must be added through this.addLayer() first                 
                if (this.baseLayer != null) { 							// make the old base layer invisible 
                    this.baseLayer.setVisible(false);
                }                             
                this.baseLayer = newBaseLayer;               
                //Util.printDebugMessage(" ...set new base layer...");
                this.baseLayer.setVisible(true);
                //Util.printDebugMessage(" ...make new base layer visible...");
                Coordinate center = this.getCenter();
                if(center != null) {
                	Coordinate newCenter = (oldExtent!=null) ? oldExtent.centre() : center;                                                                
                    int newZoom = (oldExtent!=null) ? this.getZoomForExtent(oldExtent) : this.getZoomForResolution(this.resolution);
                    //Util.printDebugMessage(" ...center map after changing base layer...");
                    this.setCenter(newCenter, newZoom, false, false);
                }                                                         
                this.events.triggerEvent(MapEvents.LAYER_CHANGED, new Event(MapEvents.LAYER_CHANGED, this.baseLayer));                
                this.events.triggerEvent(MapEvents.BASELAYER_CHANGED, new Event(MapEvents.LAYER_CHANGED, this.baseLayer));
                // TODO: shall I set isBaseLayer to true on newBaseLayer?
            }        
        }
	}
	
	/**
	 * APIMethod: addLayers
	 * 
	 * @param layers 
	 */
	public void addLayers(Layer[] layers) {
		// TODO: to be implemented
	}
	
	/**
	 * removeLayer(Layer layer)
	 * @param layer
	 */
	public void removeLayer(Layer layer) {
		removeLayer(layer, false);
	}
	
	/**
	 * removeLayer(Layer layer, boolean setNewBaseLayer)
	 * @param layer
	 */
	public void removeLayer(Layer layer, boolean setNewBaseLayer) {
		// TODO: to be implemented
		// must remove layer from map and also remove view from view group
		removeView(layer);
		//this.events.triggerEvent(MapEvents.LAYER_REMOVED, new Event(MapEvents.LAYER_REMOVED, layer));
	}
	
	/**
     * APIMethod: resetLayersZIndex
     * Reset each layer's z-index based on layer's array index
	 */
	public void resetLayersZIndex() {
		// TODO: to be implemented		
	}
	
	/**
	 * API Method: getNumLayers
	 * 
	 * @return
	 * number of layers in the map
	 */
	public int getNumLayers() {
		return this.layers.size();
	}
	
	/**
	 * API Method: getLayerIndex
	 * 
	 * @param layer
	 * @return
	 */
	public int getLayerIndex(Layer layer) {
		// TODO: to be implemented
		return 0;
	}
	
	/**
     * API Method: setLayerIndex
     * Move the given layer to the specified (zero-based) index in the layer
     *     list, changing its z-index in the map display. Use
     *     map.getLayerIndex() to find out the current index of a layer. Note
     *     that this cannot (or at least should not) be effectively used to
     *     raise base layers above overlays.
     *     
	 * @param idx
	 */
	public void setLayerIndex(int idx) {
		// TODO: to be implemented
	}
	
	/**
     * API Method: raiseLayer
     * Change the index of the given layer by delta. If delta is positive, 
     *     the layer is moved up the map's layer stack; if delta is negative,
     *     the layer is moved down.  Again, note that this cannot (or at least
     *     should not) be effectively used to raise base layers above overlays.
	 */
	public void raiseLayer(int delta) {
		// TODO: to be implemented
	} 
	
	/**
	 * getLayer(String id)
	 * Get a layer based on its id
	 * 
	 * @param id
	 * @return
	 */
	public Layer getLayer(String id) {
		// TODO: to be implemented
		Util.printErrorMessage("...to be implemented...");
		return null;
	}
	
	/**
	 * getLayerByName(String name)
	 * 
	 * @param name
	 * @return
	 */
	public Layer getLayerByName(String name) {
		// TODO: to be implemented
		return null;
	}
	
	/**
	 * getLayersByClass(String className)
	 * 
	 * @param className
	 * @return
	 */
	public Layer[] getLayersByClass(String className) {
		// TODO: to be implemented
		return null;
	}
	
	// ===========================================================
	// Controls operations
	// ===========================================================
	
	/**
     * API Method: addControl
     * 
	 * @param control
	 * @param pixel
	 */
	public void addControl(Control control, Pixel pixel) {
		// TODO: to be implemented
	}
	
	/**
     * API Method: addControlToMap
     * 
	 * @param control
	 * @param pixel
	 */
	public void addControlToMap(Control control, Pixel pixel) { // may merge into addControl
		// TODO: to be implemented
	} 
	
	/**
     * API Method: getControl
     * 
	 * @param id
	 * @return
	 */
	public Control getControl(String id) {
		// TODO: to be implemented
		return null;
	}
	
	/**
     * APIMethod: removeControl
     * Remove a control from the map. Removes the control both from the map 
     *     object's internal array of controls, as well as from the map's 
     *     viewPort (assuming the control was not added outsideViewport)
     *     
	 * @param control
	 */
	public void removeControl(Control control) {
		// TODO: to be implemented
	}
	
	/**
	 * API Method: getControlsByClass
	 * 
	 * @param className
	 * @return
	 */
	public Control[] getControlsByClass(String className) {
		// TODO: to be implemented
		return null;
	}
	
	// ===========================================================
	// Extent, Size, Zoom, Scale and Resolution
	// ===========================================================
	
	/**
     * API Method: getSize
     * 
     * @return {@link Size} 
     * 			a Size object that represents the size, in pixels, 
     * 			of the view into which OpenLayers has been loaded. 
     * 			Note - A clone() of this locally cached variable is returned, 
     * 			so as not to allow users to modify it.
     * 
	 */
	public Size getSize() {
		Size size = null;
        if(this.size != null) {
            size = this.size.clone();
        }
        return size;
	}
	
	/**
	 * API Method: getCurrentSize
	 * Get width & height directly from View in case dimension changed outside map
	 * 
	 * @return {@link Size} 
	 */
	public Size getCurrentSize() {	
		return new Size(getWidth(), getHeight());
	}
	
	/**
     * API Method: updateSize
     * This function should be called by any external code which dynamically
     * changes the size of the map view
     * 
     */
	public void updateSize() {
		//Util.printDebugMessage("@...Map.updateSize() is called...");	
		Size newSize = this.getCurrentSize();
		Size oldSize = this.getSize();
        if(oldSize == null) {
            this.size = oldSize = newSize;
        }
        if(newSize.equals(oldSize) == false) {            
            this.size = newSize;
            for(int i=0, len=this.layers.size(); i<len; i++) {
                this.layers.get(i).onMapResize();                
            }
            if(this.baseLayer != null) {             
                int zoom = this.getZoom();
                //Util.printDebugMessage(" ...force zoom change and recenter map...");	
                this.zoom = -1; // set this.zoom to -1 to enforce the zoom change, so that setCenter redraw map correctly
                this.setCenter(this.getCenter(), zoom); 
            }
        }
	}
	
	/**
	 * API Method: calculateBounds
	 * 
	 * @param center
	 * @param resolution
	 * 
	 * @return
	 * extent based on current parent view size and resolution 
	 */
	public Envelope calculateBounds(Coordinate center, double resolution) {
		//Util.printDebugMessage("@...Map.calculateBounds() is called...");	
		Envelope extent = null;    
		if(center == null) {
			center = this.center;
		}                
		if(resolution <= 0) {
			resolution = this.resolution;
		}
		if((center != null) && (resolution > 0)) {

			double x_span = this.size.getWidth() * resolution;
			double y_span = this.size.getHeight() * resolution;
			////Util.printDebugMessage(" ...x span: " + x_span + "...");
			////Util.printDebugMessage(" ...y span: " + y_span + "...");
			extent = new Envelope(
				center.x - x_span/2,
				center.x + x_span/2,
				center.y - y_span/2,                
                center.y + y_span/2
            );
			//Util.printDebugMessage(" ...bounds calculated: " + extent.toString() + "...");
		}
		return extent;
	}
	
	/**
	 * API Method: calculateBounds
	 * calculate extent bounds based on current resolution
	 * 
	 * @return 
	 * extent bounds based on current resolution
	 */
	public Envelope calculateBounds() {
		return calculateBounds(null, -1);
	}
	
	/**
	 * API Method: getExtent
	 * 
	 * @return
	 * current extent
	 * 
	 */
	public Envelope getExtent() {
		// implementation of this.baseLayer
		/*
		return this.calculateBounds();
		 */
		Envelope extent = null;
        if(this.baseLayer != null) {
            extent = this.baseLayer.getExtent();
        } else {
        	extent = calculateBounds();
        }
        return extent;
	}
			
	/**
	 * API Method: getZoom
	 * 
	 * @return 
	 * current zoom
	 */
	public int getZoom() {
		return zoom;
	}
	
	/**
	 * API Method: getResolutionForZoom
	 * 
	 * @param zoom
	 * @return
	 * resolution based on current zoom level
	 */
	public double getResolutionForZoom(int zoom) {
		// implementation of this.baseLayer
		/*
		zoom = Math.max(0, Math.min(zoom, this.resolutions.length - 1));
        double res;
        if(this.fractionalZoom) {         
        	res = this.resolutions[Math.round(zoom)];
        } else {
            res = this.resolutions[Math.round(zoom)];
        }
        return res;
        */
		double resolution = -1;
        if(this.baseLayer != null) {
            resolution = this.baseLayer.getResolutionForZoom(zoom);
        } else {
        	int z = Math.max(0, Math.min(zoom, this.resolutions.length - 1));            
            if(this.isFractionalZoom()) {
                // TODO: deal with fractional zoom
            	resolution = this.resolutions[Math.round(z)];
            } else {
            	resolution = this.resolutions[Math.round(z)];
            }
        }
        return resolution;
	}
	
	/**
	 * API Method: getResolution
	 *  
	 * @return
	 * current resolution 
	 */
	public double getResolution() {
		// implementation of this.baseLayer
		/*
		int zoom = this.getZoom();
        return this.getResolutionForZoom(zoom);
        */
		double resolution = -1;
        if (this.baseLayer != null) {
            resolution = this.baseLayer.getResolution();
        } else {
        	resolution = getResolutionForZoom(this.zoom);
        }
        return resolution;
	}
	
	/**
	 * API Method: getZoomForResolution
	 * 
	 * @param resolution 
	 * @return 
	 * current zoom level based on resolution
	 */
	public int getZoomForResolution(double resolution) {
		// implementation in this.baseLayer
		/*
		int zoom;                
    	int i;
    	for(i=1; i<this.resolutions.length; i++) {
            if(this.resolutions[i] < resolution) {
                break;
            }
        }
        zoom = i - 1;        
        return zoom;
        */
		int zoom = -1;
        if(this.baseLayer != null) {
            zoom = this.baseLayer.getZoomForResolution(resolution);
        } else {        	        
            // TODO: deal with fractional zoom
        	int i;
        	for(i=1; i<this.resolutions.length; i++) {
                if(this.resolutions[i] < resolution) {
                    break;
                }
            }
            zoom = i - 1;                    
        }
        return zoom;
	}
	
	/**
	 * API Method: getZoomForExtent
	 * 
	 * @param extent
	 * @return
	 * current zoom level based on extent
	 */
	public int getZoomForExtent(Envelope extent) {
        // implementation in this.baseLayer
		/*
		Size viewSize = this.getSize();
        double idealResolution = Math.max(extent.getWidth()/viewSize.getWidth(), extent.getHeight()/viewSize.getHeight() );
        return this.getZoomForResolution(idealResolution);
        */
		int zoom = -1;
        if(this.baseLayer != null) {
            zoom = this.baseLayer.getZoomForExtent(extent);
        } else {
        	Size viewSize = this.getSize();
            double idealResolution = Math.max(extent.getWidth()/viewSize.getWidth(), extent.getHeight()/viewSize.getHeight());
            return getZoomForResolution(idealResolution);
        }
        return zoom;
    }

	// ===========================================================
	// Center, and Pan operations
	// ===========================================================
	
	/**
     * API Method: getCenter
     * 
     * @return {@link LonLat}
     * map center 
	 */
	public Coordinate getCenter() {
		return this.center;
	}
	
	public void pan(int dx, int dy) {				
		//Util.printDebugMessage("@...Map.pan() is called...");		
		Pixel centerPx = this.getViewPortPxFromCoordinate(this.getCenter());        
		Pixel newCenterPx = centerPx.add(dx, dy);		
        Coordinate newCenter = this.getCoordinateFromViewPortPx(newCenterPx);
        //Util.printDebugMessage(" ...old screen center: " + centerPx.toString() + "...new screen center: " + newCenterPx.toString() + "...");
		//Util.printDebugMessage(" ...old map center: " + this.getCenter().toString() + "...new map center: " + newCenter.toString() + "...");
						              
        // only call setCenter if there has been a change               
        if(!newCenterPx.equals(centerPx)) {	        	       
            final boolean forceZoomChange = false; // zoom does not change when panning
            final boolean forceCenterChange = false; //  
            //Util.printDebugMessage(" ...re-center map after pan...");
            this.setCenter(newCenter, this.getZoom(), forceZoomChange, forceCenterChange); 
        } else {
        	//Util.printDebugMessage(" ...map center doesn't change...skip panning...");
        }
		        
	}	
	
	/**
	 * dragScreenDelta(int dx, int dy)
	 * 
	 * @param dx
	 * @param dy
	 */
	public void drag(int dx, int dy) {
		//Util.printDebugMessage("@...Map.drag() is called...");		
		Pixel centerPx = this.getViewPortPxFromCoordinate(this.getCenter());        
		Pixel newCenterPx = centerPx.add(-dx, -dy);		
        Coordinate newCenter = this.getCoordinateFromViewPortPx(newCenterPx);
        //Util.printDebugMessage(" ...old screen center: " + centerPx.toString() + "...new screen center: " + newCenterPx.toString() + "...");
		//Util.printDebugMessage(" ...old map center: " + this.getCenter().toString() + "...new map center: " + newCenter.toString() + "...");
		// update map center
        this.center = new Coordinate(newCenter);
        
		if(this.baseLayer != null) {        	            
        	this.baseLayer.drag(dx, dy);        	
        	//this.baseLayer.postInvalidate();        	
        }                                  
        for(int i=0; i<this.layers.size(); i++) {
            Layer layer = (Layer)this.layers.get(i);                
            if(layer.isBaseLayer() == false) {                    
            	boolean moveLayer = false;
            	// the inRange property has changed. If the layer is
                // no longer in range, we turn it off right away. If
                // the layer is no longer out of range, the moveTo
                // call below will turn on the layer.                	
            	boolean inRange = layer.calculateInRange();
                if(layer.isInRange() != inRange) {
                    layer.setInRange(inRange);
                    moveLayer = true;                                           
                    Event event = new Event(MapEvents.LAYER_CHANGED);
                    event.properties.put("data", layer);
                    event.properties.put("incident", "visibility");
                    this.events.triggerEvent(MapEvents.LAYER_CHANGED, event);                    
                } else {
                    moveLayer = (layer.isVisible() && layer.isInRange());
                }
                if(moveLayer) {                                        	
                	layer.drag(dx, dy);
                	/*
                	Event event = new Event(LayerEvents.MOVE_END);
                    event.properties.put("data", layer);
                    event.properties.put("incident", "zoom");
                	layer.events.triggerEvent(LayerEvents.MOVE_END, event);         
                	*/
                	//Util.printDebugMessage("...postInvalidate overlay because of recentering map...");
                	//layer.postInvalidate();
                	//layer.invalidate();
                }
            }                
        } 
	}
	
	/**
     * API Method: panTo
     * Allows user to pan to a new lonlat
     * If the new lonlat is in the current extent the map will slide smoothly
     * 
	 * @param lonlat
	 */
	public void panTo(Coordinate lonlat) {
		//TODO: to be implemented
	}
	
	/**
	 * APIMethod: setCenter
	 * 
	 * @param lonlat
	 * @param zoom
	 * @param dragging
	 * @param forceZoomChange
	 */
	public void setCenter(Coordinate center, int zoom, boolean forceZoomChange, boolean forceCenterChange) {
		//Util.printDebugMessage("@...Map.setCenter() is called...");				
		if(this.center == null && !this.isValidCoordinate(center)) {
			center = this.maxExtent.centre(); // if invalid new center, reuse current center
        }        
		// if map is restricted to certain extent, then map can not be re-center there
		// TODO: deal with restrictedExtent	
		
		// check if zoom level has been changed
        boolean zoomChanged = forceZoomChange || ((this.isValidZoomLevel(zoom))&&(zoom!=this.zoom));
        // check if map center has been changed
        boolean centerChanged = (this.isValidCoordinate(center))&&(!center.equals(this.center));
        if(forceCenterChange == true) {
        	centerChanged = true;
        }
        //Util.printDebugMessage(" ...is zoom changed: " + zoomChanged  + "...");
        //Util.printDebugMessage(" ...is center changed: " + centerChanged  + "...");
        if(zoomChanged || centerChanged) {        	
            //if(dragging == false) {             	
            this.events.triggerEvent(MapEvents.MOVE_START, new Event(MapEvents.MOVE_START, null));
            //}
            if(centerChanged == true) {
                // never change this.layerContainerCanvasLeft and this.layerContainerCanvasTop
            	// always draw layers from (0, 0) of parent android.View 
            	// never move the original point where layers are drawn in parent android.View
                this.center = new Coordinate(center); 
                //Util.printDebugMessage(" ...center changed and new center is: " + this.center.toString()  + "...");
            }
            if((zoomChanged) || (this.layerContainerOrigin == null)) {
                this.layerContainerOrigin = new Coordinate(this.center);                
            }
            if(zoomChanged) {
                this.zoom = zoom;
                this.resolution = this.getResolution();
                //Util.printDebugMessage(" ...zoom changed and new zoom is: " + this.zoom  + "...");
                //Util.printDebugMessage(" ...resolution changed and new resolution is: " + this.resolution  + "...");
            
            }             
            Envelope bounds = this.getExtent();
                 
            if(this.baseLayer != null) {
            	//Util.printDebugMessage(" ...move baselayer to bounds: " + bounds.toString() + "...");            	            	  
            	this.baseLayer.cancelLoadingThreads();
            	this.baseLayer.moveTo(bounds, zoomChanged, centerChanged);
            	//Util.printDebugMessage("...postInvalidate baselayer because of recentering map...");
            	//this.baseLayer.postInvalidate(); // should I call postInvalidate() instead of invalidate()?
            	this.baseLayer.invalidate();            	
            }                                  
            for(int i=0; i<this.layers.size(); i++) {
                Layer layer = (Layer)this.layers.get(i);                
                if(layer.isBaseLayer() == false) {                    
                	boolean moveLayer = false;
                	// the inRange property has changed. If the layer is
                    // no longer in range, we turn it off right away. If
                    // the layer is no longer out of range, the moveTo
                    // call below will turn on the layer.                	
                	boolean inRange = layer.calculateInRange();
                    if(layer.isInRange() != inRange) {
                        layer.setInRange(inRange);
                        moveLayer = true;                       
                        Event event = new Event(MapEvents.LAYER_CHANGED);
                        event.properties.put("data", layer);
                        event.properties.put("incident", "visibility");
                        this.events.triggerEvent(MapEvents.LAYER_CHANGED, event);
                    } else {
                        moveLayer = (layer.isVisible() && layer.isInRange());
                    }
                    if(moveLayer) {                        
                    	//Util.printDebugMessage(" ...move layer to bounds: " + bounds.toString() + "...");
                    	layer.cancelLoadingThreads();
                    	layer.moveTo(bounds, zoomChanged, centerChanged);
                    	Event event = new Event(LayerEvents.MOVE_END);
                        event.properties.put("data", layer);
                        event.properties.put("incident", "zoom");
                    	layer.events.triggerEvent(LayerEvents.MOVE_END, event);         
                    	//Util.printDebugMessage("...postInvalidate overlay because of recentering map...");
                    	//layer.postInvalidate();
                    	layer.invalidate();
                    }
                }                
            }                        
            if(zoomChanged) {             	
            	//this.events.triggerEvent(MapEvents.ZOOM_END, new Event(MapEvents.ZOOM_END, null));
            }
        }         
        //this.events.triggerEvent(MapEvents.MOVE_END, new Event(MapEvents.MOVE_END, null));                
	}
	
	/**
	 * API Method: setCenter
	 * 
	 * @param lonlat
	 * @param zoom
	 * @param forceZoomChanged
	 */
	public void setCenter(Coordinate center, int zoom, boolean forceZoomChanged) {		
		this.setCenter(center, zoom, forceZoomChanged, false);
	}
	
	/**
	 * API Method: setCenter
	 * 
	 * @param lonlat
	 * @param zoom
	 */
	public void setCenter(Coordinate center, int zoom) {		
		this.setCenter(center, zoom, false, false);
	}
	
	/**
	 * API Method: setCenter
	 * 
	 * @param lonlat
	 */
	public void setCenter(Coordinate center) {		
		this.setCenter(center, this.getZoom(), false, false);
	}	
	
	/**
	 * createDrawSignature()
	 * @return
	 */
	public String createDrawSignature() {
		int zoom = this.getZoom();
		Coordinate center = this.getCenter();
		//long time = System.currentTimeMillis();
		final String signature = 
			String.valueOf(zoom) + "_" 
			+ String.valueOf(center.x) + "_"
			+ String.valueOf(center.y) + "_"
			+ String.valueOf(center.z);
			//+ String.valueOf(time);
		return signature;
	}
	
	/**
	 * private method: centerLayerContainer
	 * 
	 * @param lonlat
	 */
	/*
	private void centerLayerContainer(LonLat lonlat) {		
		Pixel originPx = this.getViewPortPxFromLonLat(this.layerContainerOrigin);
		Pixel newPx = this.getViewPortPxFromLonLat(lonlat);
		//Util.printDebugMessage(" ...old layer container: " + this.layerContainerCanvasLeft + "," + this.layerContainerCanvasTop + "...");
        if((originPx != null) && (newPx != null)) {        	
        	this.layerContainerOrigin = lonlat.clone();
        	this.layerContainerCanvasLeft = Math.round(originPx.getX() - newPx.getX());
            this.layerContainerCanvasTop  = Math.round(originPx.getY() - newPx.getY());
        }
        //Util.printDebugMessage(" ...new layer container: " + this.layerContainerCanvasLeft + "," + this.layerContainerCanvasTop + "...");
	}
	*/
	
	/**
	 * API Method: isValidLonLat
	 * 
	 * @param lonlat
	 * @return
	 * whether the lonlat is within the max extent of the map
	 */
	public boolean isValidCoordinate(Coordinate coord) {
		boolean valid = false;
	    if(coord != null) {
	        Envelope maxExtent = this.maxExtent;	        
	        valid = maxExtent.contains(coord);        
	    }
	    return valid;
	}
	
	/**
	 * API Method: isValidZoomLevel
	 *  
	 * @param zoomLevel
	 * @return
	 * whether the zoom is between the max and min zoom level 
	 */
	public boolean isValidZoomLevel(int zoomLevel) {
		return ((zoomLevel >= 0) && (zoomLevel < this.numZoomLevels));
	}
	
	// ===========================================================
	// Zooming operations 
	// ===========================================================
	
	/**
	 * API Method: zoomTo
	 * 
	 * @param zoom
	 */
    public void zoomTo(int zoom) {
        if(this.isValidZoomLevel(zoom)) {            
        	
        	this.setCenter(null, zoom);
        }
    }
    
    /**
	 * API Method: zoomIn
	 * 
     */
    public void zoomIn() {
    	//this.zoomTo(this.getZoom() + 1);    	
    	float dr = 2f;
	    if(dr != 1) {
	    	ScaleAnimation animation = new ScaleAnimation(1f, dr, 1f, dr, getWidth()/2, getHeight()/2);
	    	animation.setDuration(500);
	    	animation.setAnimationListener(
	    		new AnimationListener() {            	    
	    			public void onAnimationStart(Animation animation) {}            	    
	    			public void onAnimationRepeat(Animation animation) {}            	    
	    			public void onAnimationEnd(Animation animation) {            	    				    				
	    				zoomTo(getZoom() + 1);
	    			}
	    	});      	    	
	    	startAnimation(animation);	    	
	    }    	
    }
    
    /**
	 * API Method: zoomOut
     */
    public void zoomOut() {
        //this.zoomTo(this.getZoom() - 1);    	
        float dr = 0.5f;
	    if(dr != 1) {
	    	ScaleAnimation animation = new ScaleAnimation(1f, dr, 1f, dr, getWidth()/2, getHeight()/2);
	    	animation.setDuration(500);
	    	animation.setAnimationListener(
	    		new AnimationListener() {            	    
	    			public void onAnimationStart(Animation animation) {}            	    
	    			public void onAnimationRepeat(Animation animation) {}            	    
	    			public void onAnimationEnd(Animation animation) {            	    				    				
	    				zoomTo(getZoom() - 1);
	    			}
	    	});  	    	         	   
	    	startAnimation(animation);
	    }
    }
    
    /**
	 * API Method: zoomToExtent
	 * 
     * @param bounds
     */
    public void zoomToExtent(Envelope bounds) {
        this.setCenter(bounds.centre(), this.getZoomForExtent(bounds));
	}
	
    /**
	 * API Method: zoomToExtent
     */
	public void zoomToMaxExtent() {
		this.zoomToExtent(this.maxExtent);
	}
	
	/**
	 * API Method: zoomToScale
	 * 
	 * @param scale
	 */
	public void zoomToScale(double scale) {
		double res = Util.getResolutionFromScale(scale, this.baseLayer.getUnits());
		double w_deg = this.size.getWidth() * res;
		double h_deg = this.size.getHeight() * res;
		Coordinate center = this.center;

		Envelope extent = new Envelope(	center.x - w_deg / 2,
										center.x + w_deg / 2,                       
										center.y - h_deg / 2,                                       
										center.y + h_deg / 2);
    	this.zoomToExtent(extent);
	}
	
	/**
	 * API Method: getUnits
	 * 
	 * @return
	 * units of the map
	 */
	public String getUnits() {
		String units = null;
        if(this.baseLayer != null) {
            units = this.baseLayer.getUnits();
        } else {
        	units = this.units;
        }
        return units;
	}
	
	/**
	 * API Method: getScale
	 * 
	 * @return
	 * calculate scale based on current resolution
	 */
	public double getScale() {
		double scale = -1;
        if (this.baseLayer != null) {
            double res = this.getResolution();
            String units = this.baseLayer.getUnits();
            scale = Util.getScaleFromResolution(res, units);
        } else {
        	double res = this.getResolution();
            String units = getUnits();
            scale = Util.getScaleFromResolution(res, units);
        }
        return scale;
	}
	
	// ===========================================================
	// Convert between viewPort pixel and map coordinates 
	// ===========================================================
	
	/**
	 * API Method: getLonLatFromViewPortPx
	 * Returns a map location from given a screen location. 
	 * 
	 * @param viewPortPx
	 * @return
	 */
	public Coordinate getCoordinateFromViewPortPx(Pixel viewPortPx) {		
		// implementation of this.baseLayer 
		/*
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
	    */
		Coordinate coord = null; 
        if(this.baseLayer != null) {
        	coord = this.baseLayer.getCoordinateFromViewPortPx(viewPortPx);
        } else {
        	if(viewPortPx != null) {
    	        Size size = this.size;
    	        Coordinate center = this.center;
    	        if(center != null) {
    	            double res  = this.getResolution();	    
    	            double dx = viewPortPx.getX() - (size.getWidth()/2);
    	            double dy = viewPortPx.getY() - (size.getHeight()/2);	        
    	            coord = new Coordinate(center.x+dx*res, center.y-dy*res); 
    	        }
    	    }
    	    return coord;
        }
        return coord;
	}
	
	/**
	 * API Method: getViewPortPxFromLonLat
	 * Returns a pixel location given a map location.  This method will return
     *     fractional pixel values.
     *     
	 * @param lonlat
	 * @return
	 */
	public Pixel getViewPortPxFromCoordinate(Coordinate coord) {		
		// implementation of this.baseLayer
		/*
		Pixel pixel = null; 
		if(lonlat != null) {
            double res = this.resolution;
            Bounds extent = this.getExtent();
            pixel = new Pixel(
            	(1/res * (lonlat.getLon() - extent.getLeft())),
            	(1/res * (extent.getTop() - lonlat.getLat()))
            );    
        }
        return pixel;
        */
		Pixel px = null; 
        if (this.baseLayer != null) {
            px = this.baseLayer.getViewPortPxFromCoordinate(coord);
        }
        return px;
	}
	
	/**
     * API Method: getLonLatFromPixel
     * 
	 * @param pixel
	 * @return
	 */
	public Coordinate getCoordinateFromPixel(Pixel pixel) {
		return this.getCoordinateFromViewPortPx(pixel);
	}
	
	/**
     * API Method: getPixelFromLonLat
     * Returns a pixel location given a map location.  The map location is
     *     translated to an integer pixel location (in viewport pixel
     *     coordinates) by the current base layer.
     *     
	 * @param lonlat
	 * @return
	 */
	public Pixel getPixelFromCoordinate(Coordinate coord) {
		Pixel pixel = this.getViewPortPxFromCoordinate(coord);
		pixel.setX(Math.round(pixel.getX()));
		pixel.setY(Math.round(pixel.getY()));
		return pixel;
	}
	
	/**
	 * API Method: getViewPortPxFromLayerPx
	 * 
	 * @param layerPx
	 * @return
	 */
	public Pixel getViewPortPxFromLayerPx(Pixel layerPx) {
		// this.layerContainerCanvasLeft and this.layerContainerCanvasTop are always set to 0.0
		// so viewPortPx is always the same as layerPx
		Pixel viewPortPx = null;
	    if(layerPx != null) {
	        int dX = (int)(this.getLayerContainerCanvasLeft());
	        int dY = (int)(this.getLayerContainerCanvasTop());
	        viewPortPx = layerPx.add(dX, dY);            
	    }
	    return viewPortPx;
	}
	
	/**
	 * API Method: getLayerPxFromViewPortPx
	 * 
	 * @param viewPortPx
	 * @return
	 */
	public Pixel getLayerPxFromViewPortPx(Pixel viewPortPx) {
		// this.layerContainerCanvasLeft and this.layerContainerCanvasTop are always set to 0.0
		// so viewPortPx is always the same as layerPx
		Pixel layerPx = null;
	    if(viewPortPx != null) {
	        int dX = -(int)(this.getLayerContainerCanvasLeft());
	        int dY = -(int)(this.getLayerContainerCanvasTop());
	        layerPx = viewPortPx.add(dX, dY);
	    }
	    return layerPx;
	}

	/**
	 * API Method: getLonLatFromLayerPx
	 * 
	 * @param pixel
	 * @return
	 */
	public Coordinate getCoordinateFromLayerPx(Pixel pixel) {		
		pixel = this.getViewPortPxFromLayerPx(pixel);
    	return this.getCoordinateFromViewPortPx(pixel);
	}
	
	/**
	 * API Method: getLayerPxFromLonLat
	 * 
	 * @param lonlat
	 * @return
	 */
	public Pixel getLayerPxFromCoordinate(Coordinate coord) {
		Pixel px = this.getViewPortPxFromCoordinate(coord);
		return this.getLayerPxFromViewPortPx(px);
	}

	// ===========================================================
	// Popups and Markers
	// ===========================================================
	public void removePopup() {};
    
	// ===========================================================
	// UI events handling
	// ===========================================================	
	private int touchXPrevious = 0;
	private int touchYPrevious = 0;	
	
	/**
	 * onTouchEvent(final MotionEvent event)
	 */
	@Override
	public boolean onTouchEvent(final MotionEvent event) {		
		// TODO: call OnTouchEvent() on all overlays		
		//this.mapViewGestureDetector.onTouchEvent(event);		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:													
				this.touchXPrevious = (int)event.getX();
				this.touchYPrevious = (int)event.getY();				
				//Util.printDebugMessage("...touch down...");
				return true;
			case MotionEvent.ACTION_MOVE:				
				
				int dragDx = (int)event.getX() - this.touchXPrevious;
				int dragDy = (int)event.getY() - this.touchYPrevious;				
				drag(dragDx, dragDy);				
				this.touchXPrevious = this.touchXPrevious + dragDx;
				this.touchYPrevious = this.touchYPrevious + dragDy;
				
				return true;				
			case MotionEvent.ACTION_UP:																			
				
				setCenter(getCenter(), getZoom(), false, true);
				this.touchXPrevious = 0;
				this.touchYPrevious = 0;
				
				/*
				Layer overlay = null;
				for(int i=0; i<getChildCount(); i++) {
					if((getChildAt(i) instanceof Layer)) {
						Layer layer = (Layer)getChildAt(i); 
						if(layer.isBaseLayer() == false) {
							overlay = (Layer)this.getChildAt(i);
							break;
						}
					}				
				}
				//Util.printDebugMessage("...change layer order...");
				this.setLayerZIndex(overlay, getChildCount()-1); 	
				*/
				return true;				
		}	
		return super.onTouchEvent(event);
	}
	
	/**
	 * 
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent event) {		
		// TODO: to be implemented
		return super.onTrackballEvent(event);
	}
	
    // ===========================================================
	// Private classes
	// ===========================================================
    private class MapEventsHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			switch(msg.what) {
				case MapEvents.BASELAYER_CHANGED:
					//Util.printDebugMessage(" ...MapEventsHandler: base layer changed...");
					break;
				case MapEvents.LAYER_ADDED:
					//Util.printDebugMessage(" ...MapEventsHandler: layer added...");
					break;
				case MapEvents.LAYER_CHANGED:
					//Util.printDebugMessage(" ...MapEventsHandler: layer changed...");
					break;
				case MapEvents.LAYER_REMOVED:
					//Util.printDebugMessage(" ...MapEventsHandler: layer moved...");
					break;
				case MapEvents.MOVE:
					//Util.printDebugMessage(" ...MapEventsHandler: move...");
					break;
				case MapEvents.MOVE_START:
					//Util.printDebugMessage(" ...MapEventsHandler: move start...");
					break;
				case MapEvents.MOVE_END:
					//Util.printDebugMessage(" ...MapEventsHandler: move end...");
					break;
				case MapEvents.ZOOM_END:
					//Util.printDebugMessage(" ...MapEventsHandler: zoom end...");
					break;
			}
		}
	}
    
    /**
	 * Class MapViewGestureListener
	 *
	 */    
	private class MapViewGestureListener implements OnGestureListener {		
		@Override
		public boolean onDown(MotionEvent e) {
			//Util.printDebugMessage("...MapViewGestureListener.onDown() is called...");
			return false;
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			//Util.printDebugMessage("...MapViewGestureListener.onFling() is called...");
			return false;
		}
		@Override
		public void onLongPress(MotionEvent e) {
			Util.printDebugMessage("...MapViewGestureListener.onLongPress() is called...");
			Util.printDebugMessage("...long press to center the map...");			
		}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			//Util.printDebugMessage("...MapViewGestureListener.onScroll() is called...");
			return false;
		}
		@Override
		public void onShowPress(MotionEvent e) {
			//Util.printDebugMessage("...MapViewGestureListener.onShowPress() is called...");
		}
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			//Util.printDebugMessage("...MapViewGestureListener.onSingleTapUp() is called...");			
			return false;
		}
	}
    
	/**
	 * Class MapViewDoubleTapListener
	 */	
	private class MapViewDoubleTapListener implements OnDoubleTapListener {		
		@Override
		public boolean onDoubleTap(MotionEvent arg0) {
			Util.printDebugMessage("...MapViewDoubleTapListener.onDoubleTap() is called...");			
			return false;
		}
		
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Util.printDebugMessage("...MapViewDoubleTapListener.onDoubleTapEvent() is called...");
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			//Util.printDebugMessage("...MapViewDoubleTapListener.onSingleTapConfirmed() is called...");
			return false;
		}		
	}
    
    // ===========================================================
	// Getters and Setters
	// ===========================================================	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setSchema(MapSchema newSchema) {
		if(newSchema != null) {
			this.schema = newSchema;
		}
		applyMapSchema(this.schema);		
		// TODO: re-center map with force-zoom-change so that it redraws
		//setCenter(this.center);
	}
	
	
	/**
	 * @return the tileSchema
	 */
	public MapSchema getMapSchema() {
		return schema;
	}
		
	/**
	 * @return the numZoomLevels
	 */
	public int getNumZoomLevels() {
		return numZoomLevels;
	}
	
	/**
	 * @return the maxExtent
	 */
	public Envelope getMaxExtent() {
		return maxExtent;
	}
	
	/**
	 * @return the minExtent
	 */
	public Envelope getMinExtent() {
		return minExtent;
	}

	/**
	 * @return the maxResolution
	 */
	public double getMaxResolution() {
		return maxResolution;
	}

	/**
	 * @return the minResolution
	 */
	public double getMinResolution() {
		return minResolution;
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
	 * @return the scales
	 */
	public double[] getScales() {
		return scales;
	}
	
	/**
	 * @return the resolutions
	 */
	public double[] getResolutions() {
		return resolutions;
	}

	/**
	 * @return the maxZoomLevel
	 */
	public int getMaxZoomLevel() {
		return maxZoomLevel;
	}
	
	/**
	 * @return the minZoomLevel
	 */
	public int getMinZoomLevel() {
		return minZoomLevel;
	}
	
	
	/**
	 * @return the maxScale
	 */
	public double getMaxScale() {
		return maxScale;
	}
	
	/**
	 * @return the minScale
	 */
	public double getMinScale() {
		return minScale;
	}
	
	/**
	 * @return the fractionalZoom
	 */
	public boolean isFractionalZoom() {
		return fractionalZoom;
	}

	/**
	 * @param fractionalZoom the fractionalZoom to set
	 */	
	/*
	public void setFractionalZoom(boolean fractionalZoom) {
		this.fractionalZoom = fractionalZoom;
	}
	*/
	/**
	 * @return the layerContainerCanvasLeft
	 */
	public double getLayerContainerCanvasLeft() {
		return layerContainerCanvasLeft;
	}

	/**
	 * @return the layerContainerCanvasTop
	 */
	public double getLayerContainerCanvasTop() {
		return layerContainerCanvasTop;
	}
	
	/**
	 * @return the baseLayer
	 */
	public Layer getBaseLayer() {
		return baseLayer;
	}

	/**
	 * 
	 * @return
	 */
	public MapEvents getEvents() {
		return events;
	}
	
	/**
	 * 
	 * @param events
	 */
	public void setEvents(MapEvents events) {
		this.events = events;
	}			
}

// =====================================================================================================
// test code 
// =====================================================================================================
/*
// add layers and remove layer
if(this.layers.size() <= 2) {
	Layer mapnik_overlay = new Mapnik(this.getContext());
    mapnik_overlay.setName("mapnik_overlay");
    mapnik_overlay.setBaseLayer(false);
    mapnik_overlay.getPaint().setAlpha((int)(0.64*255));		        
    addLayer(mapnik_overlay);
} else {
	Layer overlay = this.layers.get(2);
	removeLayer(overlay, false);
}										

// test reorder layer					
Layer overlay = null;
for(int i=0; i<getChildCount(); i++) {
	if((getChildAt(i) instanceof Layer)) {
		Layer layer = (Layer)getChildAt(i); 
		if(layer.isBaseLayer() == false) {
			overlay = (Layer)this.getChildAt(i);
			break;
		}
	}						
}											
// zoom controls is at getChildCount()-1, so always below zoom controls view
Util.printDebugMessage("...change layer order...");
this.setLayerZIndex(overlay, getChildCount()-1); 	
*/