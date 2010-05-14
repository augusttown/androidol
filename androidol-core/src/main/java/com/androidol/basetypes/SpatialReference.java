/*
 *
 */
package com.androidol.basetypes;

/**
 * Class: com.esri.android.openlayers.basetypes.SpatialReference
 * 
 *
 */
public class SpatialReference {
	
	public static final String CRS_PREFIX = "CRS";
	public static final String EPSG_PREFIX = "EPSG";
	public static final String AUTO_PREFIX = "AUTO";
	
	private int id;
	private String prefix;
	
	public SpatialReference(int id, String prefix) {
		 this.id = id;
		 this.prefix = prefix;
	}
	
	public SpatialReference(int wkid) {
		this.id = wkid;
		this.prefix = EPSG_PREFIX;
	}
	
	public SpatialReference(String str) {
		String[] pair = str.split(":");
		this.id = Integer.valueOf(pair[1]);
		this.prefix = pair[0];
	}
	
	public String toString() {
		return this.prefix + ":" + String.valueOf(this.id);
	}
	
	public String getAsString() {
		return toString();
	}
	
}
