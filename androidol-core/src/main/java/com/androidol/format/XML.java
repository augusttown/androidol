package com.androidol.format;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.androidol.feature.Vector;
import com.androidol.util.Util;
import com.androidol.util.xml.XMLSAXHandler;
import com.vividsolutions.jts.geom.Geometry;


public class XML extends Format {

	protected XMLSAXHandler handler;
	
	/**
	 * Constructor
	 */
	public XML() {
		this.handler = new XMLSAXHandler();
	}
	
	/**
	 * API Method: parse
	 * 
	 * 
	 */	
	public void parse() {
		
	}
	
	/**
	 * API Method: encode
	 * 
	 */
	public void encode() {
		
	}
	
	/**
	 * API Method: parseGeometries
	 */
	public ArrayList<Geometry> parseGeometries(InputStream inputStream) {
		try {         
            // Get a SAXParser from the SAXPArserFactory.
            SAXParserFactory saxParserFac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFac.newSAXParser();
            // Get the XMLReader of the SAXParser.
            XMLReader xmlReader = saxParser.getXMLReader();                                  
            xmlReader.setContentHandler(this.handler);            
            xmlReader.parse(new InputSource(inputStream));            
            return this.handler.getParsedGeometries();                      
       } catch(Exception e) {
    	    Util.printErrorMessage(e.toString());
            return null;
       }
	}
	
	/**
	 * API Method: parseFeatures
	 */
	public ArrayList<Vector> parseFeatures(InputStream inputStream) {
		return null;
	}
}
