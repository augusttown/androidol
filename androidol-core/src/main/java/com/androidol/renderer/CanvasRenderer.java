package com.androidol.renderer;

import com.androidol.basetypes.Pixel;
//import com.androidol.feature.Vector;
import com.androidol.style.Style;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
//import com.androidol.util.Util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class CanvasRenderer extends Renderer {

	protected Canvas canvas;
	protected Paint paint = new Paint();
	
	public CanvasRenderer(Canvas canvas) {
		this.canvas = canvas;
	}
	
	/**
	 * 
	 */
	@Override
	public void drawGeometry(Geometry geometry, Style style) {
        
        if(geometry instanceof Point) {
        	this.drawPoint((Point)geometry, style);
        } else if(geometry instanceof LineString) {
        	this.drawLineString((LineString)geometry, style);
        } else if(geometry instanceof LinearRing) {
        	this.drawLinearRing((LinearRing)geometry, style);
        } else if(geometry instanceof Polygon) {
        	this.drawPolygon((Polygon)geometry, style);
        } else {
        	if((geometry instanceof GeometryCollection) ||
        		(geometry instanceof MultiPoint) ||
                (geometry instanceof MultiLineString) ||
                (geometry instanceof MultiPolygon)) {
                for(int i=0; i<geometry.getNumGeometries(); i++) {
                	this.drawGeometry(geometry.getGeometryN(i), style);
                }
                return;
            };
        }
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public void eraseGeometry(Geometry geometry) {
		//
	}
	
	/**
	 * 
	 */
	public void drawPoint(Point point, Style style) {
		Pixel pixel = getScreenPxFromPoint(point);
		// TODO: adjust this.paint based on style
		Paint paint1 = Style.createStrokePaint(style);
        Paint paint2 = Style.createFillPaint(style);
		//Util.printDebugMessage("@...draw point geoemtry: " + pixel.toString() + "...");
		this.canvas.drawCircle((float)pixel.getX(), (float)pixel.getY(), (float)style.getPointRadius(), paint2);
		this.canvas.drawCircle((float)pixel.getX(), (float)pixel.getY(), (float)style.getPointRadius(), paint1);
	}
	
	/**
	 * 
	 */
	public void drawLineString(LineString lineString, Style style) {
		// TODO: adjust this.paint based on style	
		Paint paint = Style.createStrokePaint(style);		
		for(int i=0; i<lineString.getNumGeometries()-1; i++) {
            Pixel start = this.getScreenPxFromPoint((Point)lineString.getGeometryN(i));
            Pixel end = this.getScreenPxFromPoint((Point)lineString.getGeometryN(i+1));
            this.canvas.drawLine((float)start.getX(), (float)start.getY(), (float)end.getX(), (float)end.getY(), paint);           
        }
	}
	
	/**
	 * 
	 */
	public void drawLinearRing(LinearRing linearRing, Style style) {
		// TODO: adjust this.paint based on style	
		Pixel start = this.getScreenPxFromPoint((Point)linearRing.getGeometryN(0));
        Path path = new Path();
		path.moveTo((float)start.getX(), (float)start.getY());
        for(int i=1; i<linearRing.getNumGeometries(); i++) {
            Pixel pixel = this.getScreenPxFromPoint((Point)linearRing.getGeometryN(i));
            path.lineTo((float)pixel.getX(), (float)pixel.getY());
        }
        path.close();
		Paint stroke_paint = Style.createStrokePaint(style);
        Paint fill_paint = Style.createFillPaint(style);
        this.canvas.drawPath(path, fill_paint);
        this.canvas.drawPath(path, stroke_paint);
	}
	
	/**
	 * 
	 */
	public void drawPolygon(Polygon polygon, Style style) {
		this.drawLinearRing((LinearRing)polygon.getGeometryN(0), style);
		for(int i=1; i<polygon.getNumGeometries(); i++) {
			// inner rings are 'empty'
			// TODO: create a empty fill style to draw inner rings
            this.drawLinearRing((LinearRing)polygon.getGeometryN(i), style);   
        }
	}
	
	/**
	 * 
	 */
	public void drawExternalGraphic(Geometry geometry, Style style) {
		
	}
	
	/**
	 * 
	 */
	public Pixel getScreenPxFromPoint(Point point) {
		double resolution = this.getResolution();
        Envelope extent = this.getExtent();
        double x = (point.getX()/resolution + (-extent.getMinX()/resolution));
        double y = ((extent.getMaxY()/resolution) - point.getY()/resolution);
		return new Pixel(x, y);
	}
}
