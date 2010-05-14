package com.androidol.format;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.androidol.feature.Vector;
import com.androidol.projection.Projection;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class GeoRSS extends XML {

	
	/**
	 * 
	 */
	public ArrayList<Geometry> parseGeometries(InputStream inputStream) {
		try {         
            // Get a SAXParser from the SAXPArserFactory.
            SAXParserFactory saxParserFac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFac.newSAXParser();

            // Get the XMLReader of the SAXParser we created.
            XMLReader xmlReader = saxParser.getXMLReader();
            
            // Create a new ContentHandler and apply it to the XML-Reader.
            GeoRSSHandler georssHandler = new GeoRSSHandler();
            xmlReader.setContentHandler(georssHandler);
            
            // Parse the georss content
            xmlReader.parse(new InputSource(inputStream));            
            return georssHandler.getParsedGeometries();                      
       } catch(Exception e) {
    	    Util.printErrorMessage(e.toString());
            return null;
       }
	}
	
	/**
	 * 
	 */
	public ArrayList<Vector> parseFeatures(InputStream inputStream) {
		try {           
            // Get a SAXParser from the SAXPArserFactory.
            SAXParserFactory saxParserFac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFac.newSAXParser();

            // Get the XMLReader of the SAXParser we created.
            XMLReader xmlReader = saxParser.getXMLReader();
            
            // Create a new ContentHandler and apply it to the XML-Reader.
            GeoRSSHandler georssHandler = new GeoRSSHandler();
            xmlReader.setContentHandler(georssHandler);
            
            // Parse the georss content
            xmlReader.parse(new InputSource(inputStream));            
            return georssHandler.getParsedFeatures();                      
       } catch(Exception e) {
            Util.printErrorMessage(e.toString());
            return null;
       }
	}
	
	private class GeoRSSHandler extends DefaultHandler {
		 
		private boolean in_point_element = false;
		
		protected ArrayList<Geometry> parsedGeometries = new ArrayList<Geometry>(); 
		protected ArrayList<Vector> parsedFeatures = new ArrayList<Vector>(); 
		
		// ===========================================================
	    // Methods
	    // ===========================================================
	     
		@Override
	    public void startDocument() throws SAXException {
			
	    }

	    @Override
	    public void endDocument() throws SAXException {
	          
	    }

	    @Override
	    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	    	if(localName.equals("point")) {
	    		//Util.printDebugMessage(" ...element <point> start tag found under namespace " + namespaceURI + "...");
	    		this.in_point_element = true;
	    	}
	    }
	     
	    @Override
	    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	    	if(localName.equals("point")) {
	    		//Util.printDebugMessage(" ...element <point> end tag found under namespace " + namespaceURI + "...");
	    		this.in_point_element = false;
	    	}
	    }
	     
	    @Override
	    public void characters(char[] ch, int start, int length) {
	    	if(this.in_point_element == true) {
	    		String coordinateString = new String(ch, start, length);
	    		//Util.printDebugMessage(coordinateString);
	    		
	    		String[] coordinates = coordinateString.split(" ");	    		
	    		double lat = Double.valueOf(coordinates[0]);
	    		double lon = Double.valueOf(coordinates[1]);
	    			    		
	    		Point point = Projection.wgs84ToSphericalMercator(lon, lat);	    		
	    		this.parsedGeometries.add(point);
	    		
	    		Vector feature = new Vector(point, null, null);
	    		this.parsedFeatures.add(feature);
	    	}
	    }
	    
	    public ArrayList<Geometry> getParsedGeometries() {
	    	return this.parsedGeometries;
	    }
	    
	    public ArrayList<Vector> getParsedFeatures() {
	    	return this.parsedFeatures;
	    }
	}
	
}
