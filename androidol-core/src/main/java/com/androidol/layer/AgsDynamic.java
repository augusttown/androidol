package com.androidol.layer;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.androidol.basetypes.Pixel;
import com.androidol.basetypes.Size;
import com.androidol.tile.Image;
import com.androidol.tile.Tile;
import com.androidol.util.Util;
import com.androidol.util.geometry.JTSGeometryUtils;
import com.vividsolutions.jts.geom.Envelope;

public class AgsDynamic extends Grid {
	
	// ===========================================================
	// fields related to AgsDynamic layer itself
	// ===========================================================	
	
	public HashMap<String, String> DEFAULT_PARAMS; 
	public boolean reproject = false;
	
	public AgsDynamic(Context context) {
		super(context);
		initDefaultAgsDynamicParams();
	}
	
	public AgsDynamic(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaultAgsDynamicParams();
		// take "LAYERS" parameters from layout XML
		String layers = attrs.getAttributeValue(ANDROIDOL_NAMESPACE, "layers");
		this.params.put("LAYERS", layers);
	}
	
	private void initDefaultAgsDynamicParams() {
		DEFAULT_PARAMS = new HashMap<String, String>();
		DEFAULT_PARAMS.put("F", "image");
		DEFAULT_PARAMS.put("DPI", "96");
		DEFAULT_PARAMS.put("FORMAT", "png");
		DEFAULT_PARAMS.put("TRANSPARENT", "true");
		// TODO: merge default params and input params
		this.params = Util.applyDefaults(this.params, DEFAULT_PARAMS);		
		// TODO: set isBaseLayer based on the "TRANSPARENT", should I?
	}
	
	/*
	public AgsDynamic(String name, String url, HashMap<String, String> params, HashMap<String, Object> options) {		
		super(name, url, params, options);
		DEFAULT_PARAMS = new HashMap<String, String>();
		DEFAULT_PARAMS.put("F", "image");
		DEFAULT_PARAMS.put("DPI", "96");
		DEFAULT_PARAMS.put("FORMAT", "png");
		DEFAULT_PARAMS.put("TRANSPARENT", "true");
		// TODO: merge default params and input params
		this.params = Util.applyDefaults(this.params, DEFAULT_PARAMS);		
		// TODO: set isBaseLayer based on the "TRANSPARENT", should I?
	}
	*/
	/**
	 * API Method: clone
	 * 
	 * @return 
	 * the clone of the AgsDynamic layer
	 */
	@Override
	public AgsDynamic clone() {
		// TODO: to be implemented
		return null;
	}
	
	/**
	 * API Method: addTile
	 */
	@Override
	public Tile addTile(Envelope bounds, Pixel position, Pixel cell, Canvas canvas, Paint paint) {		
		Size tileSize = null;
		if(this.singleTile == true) {
			if(this.imageSize == null) {
				this.imageSize = this.map.getSize();
			}
			tileSize = new Size(
				this.imageSize.getWidth()*this.ratio,
				this.imageSize.getHeight()*this.ratio
			);
		} else {
			tileSize = this.tileSize;
		}
		
		return new Image(this, position, cell, bounds, url, tileSize, this.canvas, this.paint);
	}
	
	/**
     * API Method: mergeNewParams
     * Catch changeParams and uppercase the new params to be merged in
     *     before calling changeParams on the super class.
     * 
     *     Once params have been changed, the tiles will be reloaded with
     *     the new parameters.
     * 
     * @param newParams - Hashtable of new params to use
     */
    public void mergeNewParams(HashMap<String, String> newParams) {
    	HashMap<String, String> upperParams = Util.upperCases(newParams);
        super.mergeNewParams(upperParams);
    }
    
    /**
	 * API Method: getUrl
	 */
	@Override
	public String getUrl(Envelope bounds) {
		bounds = this.adjustBoundsByGutter(bounds);        
		//Util.printDebugMessage("@...AgsDynamic.getUrl() is called here with bounds:" + bounds.toBBOX() + "...");		             
        HashMap<String, String> newParams = new HashMap<String, String>();
        newParams.put("BBOX", JTSGeometryUtils.envelopeToBBOXString(bounds));           
        if(this.singleTile == true) {
			Size imageSize = this.getImageSize();
        	//newParams.put("WIDTH", String.valueOf((int)(imageSize.getWidth()*this.ratio)));
        	//newParams.put("HEIGHT", String.valueOf((int)(imageSize.getHeight()*this.ratio)));        	
        	newParams.put("SIZE", String.valueOf((int)(imageSize.getWidth()*this.ratio)) + "," + String.valueOf((int)(imageSize.getHeight()*this.ratio)));
		} else {
			Size tileSize = this.getTileSize();
			//newParams.put("WIDTH", String.valueOf((int)tileSize.getWidth()));
            //newParams.put("HEIGHT", String.valueOf((int)tileSize.getHeight()));            
            newParams.put("SIZE", String.valueOf((int)tileSize.getWidth()) + "," + String.valueOf((int)tileSize.getHeight()));
        }         
        String requestString = this.getFullRequestString(newParams, null); 
        return requestString;
	}
	
    /** 
     * API Method: getFullRequestString
     * Combine the layer's url with its params and these newParams. 
     *   
     *     Add the SRS parameter from projection -- this is probably
     *     more eloquently done via a setProjection() method, but this 
     *     works for now and always.
     *
     * @param newParams
     * @param altUrl - Use this as the url instead of the layer's url
     * 
     * @returns
     *  
     */
    public String getFullRequestString(HashMap<String, String> newParams, String altUrl) {
        String projectionCode = this.map.getProjection();
        if(this.params.get("BBOXSR") == null) {
        	this.params.remove("BBOXSR");
        	this.params.remove("IMAGESR");
        	this.params.put("BBOXSR", projectionCode.split(":")[1]);
        	this.params.put("IMAGESR", projectionCode.split(":")[1]);        	
        } else {
        	this.params.put("IMAGESR", this.params.get("BBOXSR"));        	
        } 
        // temp code to workaround the issue of 'epsg:3785' not recognized        
        /*
        this.params.put("BGCOLOR", "white");
        this.params.put("IMAGESR", "102113");
        this.params.put("BBOXSR", "102113");
        */
        mergeNewParams(newParams);
        //Util.printDebugMessage(" ...bounding box: " + this.params.get("BBOX") + "...");
        String fullRequestString = super.getFullRequestString(this.params, altUrl);       
        return fullRequestString;
    }   
    
    /**
     * 
     */
    public void setAgsDynamicLayers(String layers) {
    	this.params.put("LAYERS", layers);
    }
    
    /**
     * 
     */
    public String getAgsDynamicLayers() {
    	String layers  = this.params.get("LAYERS");
    	return layers;
    }
}
