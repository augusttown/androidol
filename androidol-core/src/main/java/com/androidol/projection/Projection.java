package com.androidol.projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Projection {
	
	// ===========================================================
    // Fields
    // ===========================================================
	
	private static GeometryFactory GEOMETRYFACTORY = new GeometryFactory(); 
	
	// ===========================================================
    // Methods
    // ===========================================================
	
	/**
	 * API Method: transform
	 */
	public static void transform(Geometry geometry, Projection source, Projection dest) {}

	/**
	 * API Method: wgs84ToSphericalMercator
	 * 
	 * @param lon
	 * @param lat
	 * 
	 * @return
	 */
	public static Point wgs84ToSphericalMercator(double lon, double lat) {
		double x = lon * 20037508.34 / 180;
        double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;		        
        return GEOMETRYFACTORY.createPoint(new Coordinate(x, y));
	}
	
	/**
	 * API Method: wgs84ToSphericalMercator
	 * 
	 * @param point	 
	 * 
	 * @return
	 */
	public static Point wgs84ToSphericalMercator(Point point) {
		double lon = point.getX();
		double lat = point.getY();		
		double x = lon * 20037508.34 / 180;
		double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
        y = y * 20037508.34 / 180;		 		
		return GEOMETRYFACTORY.createPoint(new Coordinate(x, y));
	}
	
	/**
	 * API Method: sphericalMercatorToWgs84
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return
	 */	
	public static Point sphericalMercatorToWgs84(double x, double y) {		
		double lon = (x / 20037508.34) * 180;
		double lat = (y / 20037508.34) * 180;
	    lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);	        				
		return GEOMETRYFACTORY.createPoint(new Coordinate(lon, lat));
	}
	
	/**
	 * API Method: sphericalMercatorToWgs84
	 * 
	 * @param point
	 * 
	 * @return
	 */	
	public static Point sphericalMercatorToWgs84(Point point) {		
		double x = point.getX();
		double y = point.getY();		
		double lon = (x / 20037508.34) * 180;
		double lat = (y / 20037508.34) * 180;
	    lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);	        			
		return GEOMETRYFACTORY.createPoint(new Coordinate(lon, lat));
	}
}
