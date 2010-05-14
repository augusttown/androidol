package com.androidol.tile;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.androidol.basetypes.Pixel;
import com.androidol.basetypes.Size;
import com.androidol.layer.Layer;
//import com.androidol.util.Util;
import com.androidol.util.tiles.TileProvider;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class Tile {

	// ===========================================================
	// fields 
	// ===========================================================
	
	protected String 		id;
	protected String 		url;	
	protected Envelope 		bounds;
	protected Size 			size;
	protected Pixel 		position;
	protected Pixel 		cell;
	protected Layer 		layer;
	protected boolean 		drawn;	
	protected boolean 		queued 		= false;
	
	/**
	 * Constructor Tile 
	 * 
	 * @param layer
	 * @param position
	 * @param bounds
	 * @param url
	 * @param size
	 */
	public Tile(Layer layer, Pixel position, Pixel cell, Envelope bounds, String url, Size size) {
		this.layer = layer;
        this.position = position;
        this.cell = cell;
        this.bounds = bounds;
        this.url = url;
        this.size = size;                
	}
	
	/**
	 * API Method: destroy
	 */
	public void destroy() {
		this.layer = null;
		this.bounds = null;
		this.position = null;
		this.size = null;
	}
	
	/**
	 * API Method: draw
	 * 
	 * @param tileProvider
	 * @param canvas
	 * @param paint
	 * 
	 * @return 
	 * if tile will be drawn or not
	 */
	public boolean draw(TileProvider tileProvider, Canvas canvas, Paint paint) {
		return draw(tileProvider, canvas, paint, null);		
	}
	
	/**
	 * 
	 * @param tileProvider
	 * @param canvas
	 * @param paint
	 * @return
	 */
	public boolean draw(TileProvider tileProvider, Canvas canvas, Paint paint, String signature) {
		return draw();
	}
	
	/**
	 * API Method: draw
	 * 
	 * @return
	 * if tile will be drawn or not
	 */
	public boolean draw() {
		this.clear();		
		/*
		return ((this.layer.isDisplayOutsideMaxExtent() 
				|| (this.layer.getMaxExtent()!=null && this.bounds.intersectsBounds(this.layer.getMaxExtent(), false)))
                && !(this.layer.getBuffer() == 0 && !this.bounds.intersectsBounds(this.layer.getMap().getExtent(), false)));
		*/
		if(this.layer.isDisplayOutsideMaxExtent() == false) {
			if(this.layer.getMaxExtent()!=null && this.bounds.intersects(this.layer.getMaxExtent())==false) {				
				//Util.printDebugMessage(" ...tile is not drawn because it is outside layer max extent...");
				if(this.url != null) {
					//Util.printDebugMessage(this.url);
				}				
				return false;
			}			
		}
		if(this.layer.getBuffer() == 0) {
			Envelope mapCurrentExtent = this.layer.getMap().getExtent();
			if(this.bounds.intersects(mapCurrentExtent)==false && this.bounds.contains(mapCurrentExtent)==false) {
				//Util.printDebugMessage(" ...tile is not drawn because it is outside current map extent and buffer is 0...");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * API Method: clear
	 */
	public void clear() {
		this.drawn = false;
	}
	
	/**
	 * API Method: moveTo
	 * 
	 * @param bounds
	 * @param position
	 * @param redraw
	 */
	public void moveTo(Envelope bounds, Pixel position) {		
        this.clear();
        this.bounds = new Envelope(bounds);
        this.position = position.clone();
        this.url = this.layer.getUrl(this.bounds);	
	}
	
	/**
	 * API Method: getBoundsFromBaseLayer
	 * 
	 * @param position
	 * 
	 * @return bounds
	 * calculate bounds based from base layer
	 */
	public Envelope getBoundsFromBaseLayer(Pixel position) {
		Coordinate topLeft = this.layer.getMap().getCoordinateFromLayerPx(position); 
		Pixel bottomRightPx = position.clone();
        bottomRightPx.setX(bottomRightPx.getX() + this.size.getWidth());
        bottomRightPx.setY(bottomRightPx.getY() + this.size.getHeight());
        Coordinate bottomRight = this.layer.getMap().getCoordinateFromLayerPx(bottomRightPx); 
        // Handle the case where the base layer wraps around the date line.
        // Google does this, and it breaks WMS servers to request bounds in that fashion.        
        if(topLeft.x > bottomRight.x) {
            if(topLeft.x < 0) {
            	// TODO: not sure if it works when the projection is not in Lon and Lat            	
            	topLeft.x = -180-(topLeft.x+180);
            } else {
            	// TODO: not sure if it works when the projection is not in Lon and Lat                
                bottomRight.x = 180+bottomRight.x+180;
            }        
        }
        this.bounds = new Envelope(topLeft.x, bottomRight.x, bottomRight.y, topLeft.y);  
        return this.bounds;
	}
	
	/**
	 * @return the bounds
	 */
	public Envelope getBounds() {
		return bounds;
	}

	/**
	 * @param bounds the bounds to set
	 */
	public void setBounds(Envelope bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the position
	 */
	public Pixel getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Pixel position) {
		this.position.setX(position.getX());
		this.position.setY(position.getY());
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the size
	 */
	public Size getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Size size) {
		this.size = size;
	}

	/**
	 * @return the layer
	 */
	public Layer getLayer() {
		return layer;
	}

	/**
	 * @param layer the layer to set
	 */
	public void setLayer(Layer layer) {
		this.layer = layer;
	}

	/**
	 * @return the drawn
	 */
	public boolean isDrawn() {
		return drawn;
	}

	/**
	 * @param drawn the drawn to set
	 */
	public void setDrawn(boolean drawn) {
		this.drawn = drawn;
	}

	/**
	 * @return the queued
	 */
	public boolean isQueued() {
		return queued;
	}

	/**
	 * @param queued the queued to set
	 */
	public void setQueued(boolean queued) {
		this.queued = queued;
	}

	/**
	 * 
	 * @return
	 */
	public Pixel getCell() {
		return cell;
	}
	
	/**
	 * 
	 * @return
	 */
	public void setCell(Pixel cell) {
		this.cell = cell;
	}
	
	
}
