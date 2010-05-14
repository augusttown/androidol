package com.androidol.test;
/*
import java.util.ArrayList;
import com.esri.android.openlayers.projection.Projection;
*/
public class MapActivityUtil {
	/*
	public static ArrayList<com.esri.android.openlayers.feature.Vector> createDummyPointFeatures(String wkid) {		
    	Point point1 = new Point(10.0, 10.0);		 
    	Point point2 = new Point(0.0, 0.0);		 
    	Point point3 = new Point(-10.0, -10.0);		 
    	
    	if(wkid.equalsIgnoreCase("EPSG:900913") == true) {
    		point1 = Projection.wgs84ToSphericalMercator(point1);
    		point2 = Projection.wgs84ToSphericalMercator(point2);
    		point3 = Projection.wgs84ToSphericalMercator(point3);
    	}
    	com.esri.android.openlayers.feature.Vector feature1 = new com.esri.android.openlayers.feature.Vector(point1, null, null);
    	com.esri.android.openlayers.feature.Vector feature2 = new com.esri.android.openlayers.feature.Vector(point2, null, null);
    	com.esri.android.openlayers.feature.Vector feature3 = new com.esri.android.openlayers.feature.Vector(point3, null, null);
    	
    	ArrayList<com.esri.android.openlayers.feature.Vector> features = new ArrayList<com.esri.android.openlayers.feature.Vector>();
    	features.add(feature1);
    	features.add(feature2);
    	features.add(feature3);
    	
    	return features;
	}
	
	public static ArrayList<com.esri.android.openlayers.feature.Vector> createDummyLineStringFeatures(String wkid) {
		
		Point point1 = new Point(9.0, -2.0);		 
    	Point point2 = new Point(0.0, -10.0);		 
    	Point point3 = new Point(-8.0, -2.0);
    	
    	if(wkid.equalsIgnoreCase("EPSG:900913") == true) {
    		point1 = Projection.wgs84ToSphericalMercator(point1);
    		point2 = Projection.wgs84ToSphericalMercator(point2);
    		point3 = Projection.wgs84ToSphericalMercator(point3);
    	}
		
    	ArrayList<Geometry> points = new ArrayList<Geometry>(); 
    	points.add(point1);
    	points.add(point2);
    	points.add(point3);
    	    	
    	LineString line = new LineString(points);
    	com.esri.android.openlayers.feature.Vector feature = new com.esri.android.openlayers.feature.Vector(line, null, null);
    	
    	ArrayList<com.esri.android.openlayers.feature.Vector> features = new ArrayList<com.esri.android.openlayers.feature.Vector>();
    	features.add(feature);
		return features;
	}
	
	public static ArrayList<com.esri.android.openlayers.feature.Vector> createDummyPolygonFeatures(String wkid) {

		Point point1 = new Point(-5.0, -5.0);		 
    	Point point2 = new Point(5.0, -5.0);		 
    	Point point3 = new Point(5.0, 5.0);
    	Point point4 = new Point(-5.0, 5.0);
    	Point point5 = new Point(-5.0, -5.0);
    	
    	if(wkid.equalsIgnoreCase("EPSG:900913") == true) {
    		point1 = Projection.wgs84ToSphericalMercator(point1);
    		point2 = Projection.wgs84ToSphericalMercator(point2);
    		point3 = Projection.wgs84ToSphericalMercator(point3);
    		point4 = Projection.wgs84ToSphericalMercator(point4);
    		point5 = Projection.wgs84ToSphericalMercator(point5);
    	}
		
    	ArrayList<Geometry> points = new ArrayList<Geometry>(); 
    	points.add(point1);
    	points.add(point2);
    	points.add(point3);
    	points.add(point4);
    	points.add(point5);
    	
    	LinearRing ring = new LinearRing(points);
    	ArrayList<Geometry> rings = new ArrayList<Geometry>(); 
    	rings.add(ring);
    	
    	Polygon polygon = new Polygon(rings);
    	com.esri.android.openlayers.feature.Vector feature = new com.esri.android.openlayers.feature.Vector(polygon, null, null);
	
    	ArrayList<com.esri.android.openlayers.feature.Vector> features = new ArrayList<com.esri.android.openlayers.feature.Vector>();
    	features.add(feature);
    	
    	return features;
	}
	*/
}
