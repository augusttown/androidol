package com.androidol.layer;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.androidol.Map;
import com.androidol.basetypes.Pixel;
import com.androidol.tile.Image;
import com.androidol.tile.Tile;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class TMS extends Grid {

	// ===========================================================
	// fields related to TMS layer itself
	// ===========================================================			
	protected 	String 	version 		= "1.0.0";
	protected 	String	mimeType		= "png";
	
	public TMS(Context context) {
		super(context);
		// TMS based layers should always be in singleTile=false mode 
		if(this.singleTile == true) {
			this.singleTile = false;
		}
	}
	
	public TMS(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TMS based layers should always be in singleTile=false mode 
		if(this.singleTile == true) {
			this.singleTile = false;
		}
	}
	
	/**
	 * Old Constructor TMS
	 * 
	 * @param name
	 * @param url
	 * @param options
	 */
	/*
	public TMS(String name, String url, HashMap<String, Object> options) {		
		super(name, url, null, options);
		this.setBaseLayer(true);
		// configuration options user could set through constructor:
		//     version
		//     mimeType	
		//
		// TODO: apply configure options
		// 
	}
	*/
	/**
	 * API Method: TMS
	 */
	@Override
	public TMS clone() {
		// TODO: to be implemented
		return null;
	}
		
	/**
	 * API Method: getURL
	 */
	@Override
	public String getUrl(Envelope bounds)
	{
		//Util.printDebugMessage("@...TMS.getUrl() is called...");
		//Util.printDebugMessage(" ...tile origin is: " + this.tileOrigin.toString() + "...");		
		bounds = this.adjustBoundsByGutter(bounds);
		double res = this.map.getResolution();
		int x = (int)Math.round((bounds.getMinX() - this.tileOrigin.x)/(res * this.tileSize.getWidth()));
       	int y = (int)Math.round(Math.abs(bounds.getMinY() - (this.tileOrigin.y)) / (res * this.tileSize.getHeight()));
        int z = this.map.getZoom();        
        // to be overridden by subclass so commented
	    // use name instead of layer name, http://trac.openlayers.org/ticket/737
        String path = "/" + z + "/" + x + "/" + y + "." + this.mimeType;
        String url = this.url;        
        //Util.printDebugMessage(" ...tile url is: " + url + path + "...");
        return url + path;			
	}
	
	/**
	 * API Method: addTile
	 */
	@Override 
	public Tile addTile(Envelope bounds, Pixel position, Pixel cell, Canvas canvas, Paint paint)
	{
		return new Image(this, position, cell, bounds, null, this.tileSize, this.canvas, this.paint);
	}
	
	/**
	 * API Method: setMap
	 */
	@Override 
	public void setMap(Map map) {
		super.setMap(map);	    
		if(this.tileOrigin == null) {
	    	if(this.schema != null) {
	    		this.tileOrigin = this.schema.getTileOrigin(); 
	    	} else {
	    		this.tileOrigin = new Coordinate(this.map.getMaxExtent().getMinX(), this.map.getMaxExtent().getMinY());	
	    	}			
	    }
	} 
}
