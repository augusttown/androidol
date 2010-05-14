package com.androidol.util.geometry;

import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class JTSGeometryUtils {
	
	public static GeometryFactory jtsGeometryFactory = new GeometryFactory();
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static Point createPoint(double x, double y) {
		return jtsGeometryFactory.createPoint(new Coordinate(x, y));
	}
	
	/**
	 * 
	 * @param envelope
	 * @return
	 */
	public static Polygon envelopeToPolygon(Envelope envelope) {
		try {
			Polygon polygon = (Polygon)jtsGeometryFactory.toGeometry(envelope);
			return polygon;
    	} catch(Exception e) {    		
    		// TODO: error message invalid extent
    		Util.printErrorMessage("...can not convert envelope to polygon...empty or invalid envelope", e);
    		return null;
    	}	
	}
	
	/**
	 * 
	 * @param polygon
	 * @return
	 */
	public static Envelope polygonToEnvelope(Polygon polygon) {
		// TODO: to be implemented
		return null;		
	}	
	
	/**
	 * 
	 * @param decimal
	 * @return
	 */
	public static String envelopeToBBOXString(Envelope envelope, int decimal) {
		if(decimal <= -1) {
            decimal = 6; 
        }
        double mult = Math.pow(10, decimal);
        String bbox = Math.round(envelope.getMinX() * mult) / mult + "," + 
                      Math.round(envelope.getMinY() * mult) / mult + "," + 
                      Math.round(envelope.getMaxX() * mult) / mult + "," + 
                      Math.round(envelope.getMaxY() * mult) / mult;
        return bbox;
	}
	
	/**
	 * 
	 * @param envelope
	 * @return
	 */
	public static String envelopeToBBOXString(Envelope envelope) {
		return envelopeToBBOXString(envelope, 6);
	}
	
}
