package com.androidol.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.androidol.feature.Vector;
import com.androidol.projection.Projection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class GeoRSSSAXHandler extends XMLSAXHandler {
	
	private boolean inPointElement = false;
	
	@Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    	if(localName.equals("point")) {
    		//Util.printDebugMessage(" ...element <point> start tag found under namespace " + namespaceURI + "...");
    		this.inPointElement = true;
    	}
    }
     
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	if(localName.equals("point")) {
    		//Util.printDebugMessage(" ...element <point> end tag found under namespace " + namespaceURI + "...");
    		this.inPointElement = false;
    	}
    }
     
    @Override
    public void characters(char[] ch, int start, int length) {
    	if(this.inPointElement == true) {
    		String coordinateString = new String(ch, start, length);
    		//Util.printDebugMessage(coordinateString);
    		
    		String[] coordinates = coordinateString.split(" ");	    		
    		double lat = Double.valueOf(coordinates[0]);
    		double lon = Double.valueOf(coordinates[1]);
    
    		Point point = Projection.wgs84ToSphericalMercator(this.geometryFactory.createPoint(new Coordinate(lon, lat)));	    		
    		this.parsedGeometries.add(point);
    		
    		Vector feature = new Vector(point, null, null);
    		this.parsedFeatures.add(feature);
    	}
    }
	
}
