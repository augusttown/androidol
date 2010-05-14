package com.androidol.layer.osm;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;

import com.androidol.layer.TMS;
//import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Envelope;

public class OSM extends TMS {

	public OSM(Context context) {
		super(context);
	}
	
	public OSM(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * Old Constructor OSM
	 * 
	 * @param name
	 * @param url
	 * @param options
	 */
	/*
	public OSM(String name, String url, HashMap<String, Object> options) {
		super(name, url, options);
	}
	*/
	
	/**
	 * API Method: getURL
	 * 
	 * @param bounds
	 * 
	 * @return url
	 * create the url string based on bounds
	 */
	@Override 
	public String getUrl(Envelope bounds)
	{
		double res = this.map.getResolution();
    	int x = (int)Math.round((bounds.getMinX() - this.maxExtent.getMinX()) / (res * this.tileSize.getWidth()));
    	int y = (int)Math.round((this.maxExtent.getMaxY() - bounds.getMaxY()) / (res * this.tileSize.getHeight()));
    	int z = this.map.getZoom();
    	int limit = (int)Math.pow(2, z);

        if(y<0 || y>=limit) {
            // TODO: make it static variable
        	return getMissingTileUrl();      	         	
        } else {
            x = ((x%limit) + limit)% limit;
        	String url = this.url;
        	String path = z + "/" + x + "/" + y + "." + this.mimeType;        	
          	if(this.altUrls != null) {
	            url = this.selectUrl(this.url + path, this.getUrls());
	        }  			
        	return url + path;
	    }
	}
}
