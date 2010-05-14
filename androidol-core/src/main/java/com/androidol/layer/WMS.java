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


public class WMS extends Grid {

	// ===========================================================
	// fields related to WMS layer itself
	// ===========================================================	
	
	public HashMap<String, String> DEFAULT_PARAMS; 
	public boolean reproject = false;
	
	public WMS(Context context) {
		super(context);
		initDefaultWMSParams();
		// since required "LAYERS" is not coming from layout xml, must call setWMSLayers() later
	}
	
	public WMS(Context context, AttributeSet attrs) {
		super(context, attrs);		
		initDefaultWMSParams();
		// take "LAYERS" parameters from layout XML
		String layers = attrs.getAttributeValue(ANDROIDOL_NAMESPACE, "layers");
		this.params.put("LAYERS", layers);
	}
	
	private void initDefaultWMSParams() {
		DEFAULT_PARAMS = new HashMap<String, String>();
		DEFAULT_PARAMS.put("SERVICE", "WMS");
		DEFAULT_PARAMS.put("VERSION", "1.1.1");
		DEFAULT_PARAMS.put("REQUEST", "GetMap");
		DEFAULT_PARAMS.put("STYLES", "");
		DEFAULT_PARAMS.put("EXCEPTIONS", "application/vnd.ogc.se_inimage");
		DEFAULT_PARAMS.put("FORMAT", "image/png");
		DEFAULT_PARAMS.put("TRANSPARENT", "true");
		DEFAULT_PARAMS.put("BGCOLOR", "0xFFFFFF");		
		// TODO: merge default params and input params
		this.params = Util.applyDefaults(this.params, DEFAULT_PARAMS);		
		// TODO: set isBaseLayer based on the "TRANSPARENT", should I?				
	}
	
	/**
	 * Old Constructor WMS
	 * 
	 * @param name
	 * @param url
	 * @param params
	 * @param options
	 */
	/*
	public WMS(String name, String url, HashMap<String, String> params, HashMap<String, Object> options) {		
		super(name, url, params, options);
		DEFAULT_PARAMS = new HashMap<String, String>();
		DEFAULT_PARAMS.put("SERVICE", "WMS");
		DEFAULT_PARAMS.put("VERSION", "1.1.1");
		DEFAULT_PARAMS.put("REQUEST", "GetMap"); 
		DEFAULT_PARAMS.put("STYLES", "");
		DEFAULT_PARAMS.put("EXCEPTIONS", "application/vnd.ogc.se_inimage");
		DEFAULT_PARAMS.put("FORMAT", "image/png");
		DEFAULT_PARAMS.put("TRANSPARENT", "true");
		DEFAULT_PARAMS.put("BGCOLOR", "0xFFFFFF");		
		// TODO: merge default params and input params
		this.params = Util.applyDefaults(this.params, DEFAULT_PARAMS);		
		// TODO: set isBaseLayer based on the "TRANSPARENT", should I?
	}
	*/
	
	/**
	 * API Method: clone
	 * 
	 * @return 
	 * the clone of the WMS layer
	 */
	@Override
	public WMS clone() {
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
	 * API Method: getUrl
	 */
	@Override
	public String getUrl(Envelope bounds) {
		bounds = this.adjustBoundsByGutter(bounds);        
		//Util.printDebugMessage("@...WMS.getUrl() is called here with bounds:" + bounds.toBBOX() + "...");		       
		HashMap<String, String> newParams = new HashMap<String, String>();
		
		if(this.singleTile == true) {
			Size imageSize = this.getImageSize();
        	newParams.put("WIDTH", String.valueOf((int)(imageSize.getWidth()*this.ratio)));
            newParams.put("HEIGHT", String.valueOf((int)(imageSize.getHeight()*this.ratio)));            
		} else {
			Size tileSize = this.getTileSize();
			newParams.put("WIDTH", String.valueOf((int)tileSize.getWidth()));
            newParams.put("HEIGHT", String.valueOf((int)tileSize.getHeight()));            
        }        
        
        newParams.put("BBOX", JTSGeometryUtils.envelopeToBBOXString(bounds));
                
        String requestString = this.getFullRequestString(newParams, null); // WMS layer has no altUrl
        return requestString;
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
        //String projectionCode = this.map.getProjection();
        String projectionCode = "EPSG:102113";
        if(this.params == null || this.params.containsKey("VERSION")==false || this.params.get("VERSION").equalsIgnoreCase("1.3.0")==false) {
			if(this.params.containsKey("SRS")==false) {
				this.params.put("SRS", projectionCode);
			}			
			this.params.remove("CRS");
		} else {
			this.params.remove("SRS");
			if(this.params.containsKey("SRS")==false) {
				this.params.put("CRS", projectionCode);
			}
		}
        mergeNewParams(newParams);
        //Util.printDebugMessage(" ...bounding box: " + this.params.get("BBOX") + "...");
        String fullRequestString = super.getFullRequestString(this.params, altUrl);
        //Util.printDebugMessage(" ...GetMap: " + fullRequestString);
        return fullRequestString;
    }
    
	// ===========================================================
	// utilities functions
	// ===========================================================
    
    /**
     * 
     */
    public void setWMSLayers(String layers) {
    	this.params.put("LAYERS", layers);
    }
    
    /**
     * 
     */
    public String getWMSLayers() {
    	String layers  = this.params.get("LAYERS");
    	return layers;
    }
}
