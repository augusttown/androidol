package com.androidol.layer.osm;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;

public class Mapnik extends OSM {
	
	public Mapnik(Context context) {
		super(context);
		initDefaultMapnikParams();
	}
	
	public Mapnik(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaultMapnikParams();
	}
	
	private void initDefaultMapnikParams() {
		this.url = "http://tile.openstreetmap.org/";		
		String[] alturls = {
			"http://a.tile.openstreetmap.org/",
			"http://b.tile.openstreetmap.org/",
    		"http://c.tile.openstreetmap.org/"
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
