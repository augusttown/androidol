package com.androidol.util.tiles.packager;

import com.androidol.basetypes.Size;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class TilesUrlBuilder {

	/**
	 * 
	 * @param maxExtent
	 * @param tileExtent
	 * @param tileSize
	 * @param res
	 * @param zoom
	 * @return
	 */
	/*
	public String createTileUrl(Envelope maxExtent, Envelope tileExtent, Size tileSize, Coordinate tileOrigin, double res, int zoom) {		
		
		//This is for OSM map tiles		 		
		
		String baseUrl = "http://tile.openstreetmap.org/";		
		String mimeType = "png";
		
		//double res = this.map.getResolution();
    	int x = (int)Math.round((tileExtent.getMinX() - maxExtent.getMinX()) / (res * tileSize.getWidth()));
    	int y = (int)Math.round((maxExtent.getMaxY() - tileExtent.getMaxY()) / (res * tileSize.getHeight()));
    	int z = zoom;
    	int limit = (int)Math.pow(2, z);

        if(y<0 || y>=limit) {            
        	return null;      	         	
        } else {
            x = ((x%limit) + limit)% limit;
        	String url = baseUrl;
        	String path = z + "/" + x + "/" + y + "." + mimeType;        	          	  			
        	return url + path;
	    }
	}
	*/
	
	public String createTileUrl(Envelope maxExtent, Envelope tileExtent, Size tileSize, Coordinate tileOrigin, double res, int zoom) {
				
		String baseUrl = "http://zeon/tms/o34117a5";
		String mimeType = "png";
		
		int x = (int)Math.round((tileExtent.getMinX() - tileOrigin.x)/(res * tileSize.getWidth()));
	   	int y = (int)Math.round(Math.abs(tileExtent.getMinY() - (tileOrigin.y)) / (res * tileSize.getHeight()));
	            
	    // to be overridden by subclass so commented
	    // use name instead of layer name, http://trac.openlayers.org/ticket/737
	    String path = "/" + zoom + "/" + x + "/" + y + "." + mimeType;	           
	    //Util.printDebugMessage(" ...tile url is: " + url + path + "...");
	    return baseUrl + path;		
	}
	/**
	 * 
	 */
	/*
	public String createTileUrl(Envelope maxExtent, Envelope tileExtent, Size tileSize, Coordinate tileOrigin, double res, int zoom) {		
		
		 * This is for ArcGIS Online tiled map service
		 		
		String baseUrl = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/";
		String mimeType = "jpg";        
        
        if(maxExtent.intersects(tileExtent) == true) {
	        int col;
	        if(tileOrigin.x <= tileExtent.getMinX()) {
	        	col = (int)Math.round((tileExtent.getMinX() - tileOrigin.x) / (res * tileSize.getWidth()));
	        } else if(tileOrigin.x >= tileExtent.getMaxX()) {
	        	col = (int)Math.round((tileOrigin.x - tileExtent.getMaxX()) / (res * tileSize.getWidth()));
	        } else {	        	
	        	return null;	        	
	        }                
	        int row;
	        if(tileOrigin.y >= tileExtent.getMaxY()) {
	        	row = (int)Math.round((tileOrigin.y - tileExtent.getMaxY()) / (res * tileSize.getHeight()));
	        } else if(tileOrigin.y <= tileExtent.getMinY()) {
	        	row = (int)Math.round((tileExtent.getMinY() - tileOrigin.y) / (res * tileSize.getHeight()));
	        } else {	        		        
	        	return null;
	        }        
	        String path = "";        
	        if(mimeType.equalsIgnoreCase("") == true) {
	        	path = zoom + "/" + row + "/" + col;
	        } else {
	        	path = zoom + "/" + row + "/" + col + "." + mimeType;
	        }	        	        
	        return baseUrl + path;
	    } else {
	    	// area outside of tiles' full extent
	    	return null;    	
	    }   
	}
	*/
}
