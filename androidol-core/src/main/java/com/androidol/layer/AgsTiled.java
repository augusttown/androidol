package com.androidol.layer;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.androidol.Map;
import com.androidol.basetypes.Pixel;
import com.androidol.map.schema.ArcGISOnlineTileMapSchema;
import com.androidol.map.schema.OSMTileMapSchema;
import com.androidol.tile.Image;
import com.androidol.tile.Tile;
import com.androidol.tile.schema.ArcGISOnlineTileSchema;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class AgsTiled extends Grid {

	// ===========================================================
	// fields related to AgsTiled layer itself
	// ===========================================================			
	protected	Envelope	tileFullExtent	= null;
	protected 	String		mimeType		= "jpg";
	
	public AgsTiled(Context context) {
		super(context);
		//this.setBaseLayer(true);
		// configuration options user could set through constructor:
		//     tileFullExtent
		//     mimeType	
		//
		// TODO: apply configure options
		// 		
		if(this.tileOrigin == null) {			
			// TODO: create ArcGISOnlineMapSchema and ArcGISOnlineTileMapSchema to replace 
			//this.tileOrigin = ArcGISOnlineTileSchema.TILE_ORIGIN;
			this.tileOrigin = ArcGISOnlineTileMapSchema.TILE_ORIGIN;
		}
		if(this.tileFullExtent == null) {			
			// TODO: create ArcGISOnlineMapSchema and ArcGISOnlineTileMapSchema to replace 
			this.tileFullExtent = ArcGISOnlineTileMapSchema.DEFAULT_MAX_EXTENT;
		}
	}
	
	public AgsTiled(Context context, AttributeSet attrs) {
		super(context, attrs);
		//this.setBaseLayer(true);
		// configuration options user could set through constructor:
		//     tileFullExtent
		//     mimeType	
		//
		// TODO: apply configure options
		// 
		if(this.tileOrigin == null) {			
			// TODO: create ArcGISOnlineMapSchema and ArcGISOnlineTileMapSchema to replace 
			this.tileOrigin = ArcGISOnlineTileMapSchema.TILE_ORIGIN;
		}
		if(this.tileFullExtent == null) {			
			// TODO: create ArcGISOnlineMapSchema and ArcGISOnlineTileMapSchema to replace 
			this.tileFullExtent = ArcGISOnlineTileMapSchema.DEFAULT_MAX_EXTENT;
		}
	}
	
	/**
	 * Old Constructor AgsTiled
	 * 
	 * @param name
	 * @param url
	 * @param options
	 */
	/*
	public AgsTiled(String name, String url, HashMap<String, Object> options) {		
		super(name, url, null, options);
		this.setBaseLayer(true);
		// configuration options user could set through constructor:
		//     tileFullExtent
		//     mimeType	
		//
		// TODO: apply configure options
		// 
		if(this.tileOrigin == null) {			
			this.tileOrigin = ArcGISOnlineTileSchema.TILE_ORIGIN;
		}
		if(this.tileFullExtent == null) {			
			this.tileFullExtent = ArcGISOnlineTileSchema.DEFAULT_MAX_EXTENT;
		}
	}
	*/
	/**
	 * API Method: clone
	 * 
	 * @return 
	 * the clone of the AgsTiled layer
	 */
	@Override
	public AgsTiled clone() {
		// TODO: to be implemented
		return null;
	}
	
	/**
	 * API Method: addTile
	 * 
	 * @return tile 
	 * create a tile based on given bounds and position
	 */
	@Override 
	public Tile addTile(Envelope bounds, Pixel position, Pixel cell, Canvas canvas, Paint paint)
	{
		return new Image(this, position, cell, bounds, null, this.tileSize, this.canvas, this.paint);
	}
	
	/**
	 * API Method: getURL
	 * 
	 * @return url
	 * create the url of a tile based on bounds
	 */
	@Override
	public String getUrl(Envelope bounds) {		
        bounds = this.adjustBoundsByGutter(bounds);        
        //Util.printDebugMessage("@...AgsTiled.getUrl() is called here with bounds:" + bounds.toBBOX() + "...");
        double res = this.map.getResolution();        
        String path = null;
        String url = null;
        
        if(this.tileFullExtent.intersects(bounds) == true) {
	        int col;
	        if(this.tileOrigin.x <= bounds.getMinX()) {
	        	col = (int)Math.round((bounds.getMinX() - this.tileOrigin.x) / (res * this.tileSize.getWidth()));
	        } else if(this.tileOrigin.x >= bounds.getMaxX()) {
	        	col = (int)Math.round((this.tileOrigin.x - bounds.getMaxX()) / (res * this.tileSize.getWidth()));
	        } else {
	        	Util.printErrorMessage("...invalid tileOrigin...");
	        	return getTransparentTileUrl();	        	
	        }                
	        int row;
	        if(this.tileOrigin.y >= bounds.getMaxY()) {
	        	row = (int)Math.round((this.tileOrigin.y - bounds.getMaxY()) / (res * this.tileSize.getHeight()));
	        } else if(this.tileOrigin.y <= bounds.getMinY()) {
	        	row = (int)Math.round((bounds.getMinY() - this.tileOrigin.y) / (res * this.tileSize.getHeight()));
	        } else {	        	
	        	//Util.printDebugMessage("...invalid tileOrigin...");
	        	return getTransparentTileUrl();
	        }        
	        int zoom = this.map.getZoom();         
	        if(this.mimeType.equalsIgnoreCase("")==true) {
	        	path = zoom + "/" + row + "/" + col;
	        } else {
	        	path = zoom + "/" + row + "/" + col + "." + this.mimeType;
	        }	        
	    	url = this.url;
	        
	        return url + path;
	    } else {
	    	// area outside of tiles' full extent
	    	return getTransparentTileUrl();	    	
	    }        
    }
	
	/**
	 * API Method: setMap
	 */
	@Override 
	public void setMap(Map map) {
		super.setMap(map);	    		
		if(this.tileOrigin == null) {
	    	this.tileOrigin = new Coordinate(this.map.getMaxExtent().getMinX(), this.map.getMaxExtent().getMinY());
	    }		
		if(this.tileFullExtent == null) { 
            this.tileFullExtent = this.map.getMaxExtent();
        } 
	}

	/**
	 * @return the tileFullExtent
	 */
	public Envelope getTileFullExtent() {
		return tileFullExtent;
	}

	/**
	 * @param tileFullExtent the tileFullExtent to set
	 */
	public void setTileFullExtent(Envelope tileFullExtent) {
		this.tileFullExtent = tileFullExtent;
	} 
	
	
}
