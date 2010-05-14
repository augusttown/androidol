package com.androidol.layer.osm;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;

public class Osmarender extends OSM {
	
	public Osmarender(Context context) {
		super(context);
		initDefaultOsmarenderParams();
	}
	
	public Osmarender(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaultOsmarenderParams();
	}
	
	private void initDefaultOsmarenderParams() {
		this.url = "http://a.tah.openstreetmap.org/Tiles/tile/";		
		String[] alturls = {
			"http://a.tah.openstreetmap.org/Tiles/tile/",
			"http://b.tah.openstreetmap.org/Tiles/tile/",
    		"http://c.tah.openstreetmap.org/Tiles/tile/"
		};
		this.altUrls = alturls;	
	}
	
	/**
	 * Old Constructor Mapnik
	 * 
	 * @param name
	 * @param options
	 */
	/*
	public Mapnik(String name, HashMap<String, Object> options)
	{
		super(name, "", options);
		this.url = "http://tile.openstreetmap.org/";
		
		String[] alturls = {
			"http://a.tile.openstreetmap.org/",
			"http://b.tile.openstreetmap.org/",
    		"http://c.tile.openstreetmap.org/"
		};		
		this.altUrls = alturls;				
	}
	*/
}
