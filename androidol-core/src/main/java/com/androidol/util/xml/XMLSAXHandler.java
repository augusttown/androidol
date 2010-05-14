package com.androidol.util.xml;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.androidol.feature.Vector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class XMLSAXHandler extends DefaultHandler {
		
	protected GeometryFactory 		geometryFactory 	= new GeometryFactory();
	
	protected ArrayList<Geometry> 	parsedGeometries 	= new ArrayList<Geometry>(); 
	protected ArrayList<Vector> 	parsedFeatures 		= new ArrayList<Vector>(); 
		
	// ===========================================================
    // Methods
    // ===========================================================
     
	@Override
    public void startDocument() throws SAXException {}

    @Override
    public void endDocument() throws SAXException {}

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {}
     
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {}
     
    @Override
    public void characters(char[] ch, int start, int length) {}
    
    /**
     * API Method: getParsedGeometries
     * 
     * @return
     */
    public ArrayList<Geometry> getParsedGeometries() {
    	return this.parsedGeometries;
    }
    
    /**
     * API Method: getParsedFeatures
     * 
     * @return
     */
    public ArrayList<Vector> getParsedFeatures() {
    	return this.parsedFeatures;
    }
}
