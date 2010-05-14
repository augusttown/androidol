package com.androidol.tile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;

import com.androidol.R;
import com.androidol.basetypes.Pixel;
import com.androidol.basetypes.Size;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.layer.Layer;
import com.androidol.util.Util;
import com.androidol.util.tiles.TileProvider;
import com.vividsolutions.jts.geom.Envelope;

public class Image extends Tile {
	
	//private TileProvider tileProvider = null;
	protected Canvas canvas = null;
	protected Paint paint = null;
	
	/**
	 * Constructor Image
	 * 
	 * @param layer
	 * @param position
	 * @param bounds
	 * @param url
	 * @param size
	 * 
	 */
	public Image(Layer layer, Pixel position, Pixel cell, Envelope bounds, String url, Size size, Canvas canvas, Paint paint) {		
		super(layer, position, cell, bounds, url, size);
		this.canvas = canvas;
		this.paint = paint;
	}
	
	/**
	 * API Method: destroy
	 */
	@Override
	public void destroy() {
		this.clear();
		super.destroy();
	}
		
	/**
	 * API Method: draw
	 * 
	 * @param tileProvider
	 * @param canvas
	 * @param paint
	 */
	@Override
	public boolean draw(TileProvider tileProvider, Canvas canvas, Paint paint) {
		return draw(tileProvider, canvas, paint, null);
	}
	
	/**
	 * API Method: draw
	 * 
	 * @param tileProvider
	 * @param canvas
	 * @param paint
	 */
	@Override
	public boolean draw(TileProvider tileProvider, Canvas canvas, Paint paint, String signature) {
		//Util.printDebugMessage("@...Image.draw() is called...");				
		//this.tileProvider = tileProvider;				
		this.clear();		
		if(this.layer != this.layer.getMap().getBaseLayer()) {
			//Util.printDebugMessage(" ...layer is not base layer...get bounds from base layer...");
            this.bounds = this.getBoundsFromBaseLayer(this.position);
        }		
        if(super.draw() == false) {        	
        	//Util.printDebugMessage(" ...not drawing tile..." + this.getLayer().getUrl(this.getBounds()) + "...");
        	return false;    
        } else {
        	//Util.printDebugMessage(" ...drawing tile..." + this.getLayer().getUrl(this.getBounds()) + "...");
        	// only attache ImageTileListener to tile when you need to draw the tile
        	//Util.printDebugMessage(" ...attache ImageTileListener to tile...");        	
        	//tileProvider.events.registerAll(new ImageTileListener());
        }
        //if(this.url == null) {
        	//Util.printDebugMessage(" ...tile bounds: " + this.bounds + "...");
        	this.url = this.layer.getUrl(this.bounds);
        //}
        //Util.printDebugMessage(" ...draw tile from url: " + this.url + "...");
        //Util.printDebugMessage(" ...draw this tile at pixel: " + this.position.toString() + "...");        
        //tileProvider.addToPendingQueue(this.url); // TODO: may have conflict when two different layer requesting same tile same time
        //tileProvider.addToPendingMatrix(this);
        Bitmap tile = null;
        
        if(this.layer.isBaseLayer() == true) {
        	tile = tileProvider.getTile(this.url, signature, this); // if it is baselayer use the 'loading' tile when loading
        } else {
        	tile = tileProvider.getTile(this.url, signature, tileProvider.getTransparentTile(), this); // if it is non-baselayer use the transparent tile when loading
        }        
        // TODO: call paint.setAlpha() to deal with layer transparency
        if(this.layer.getOpacity() < 1) {        	
        	//Util.printDebugMessage(String.valueOf((int)(this.layer.getOpacity()*255)));
        	paint.setAlpha((int)(this.layer.getOpacity()*255));
        }
        //paint.setAlpha((int)(0.64*255));
        if(tile!=null && tile.isRecycled()==false) {
        	canvas.drawBitmap(tile, (int)this.position.getX(), (int)this.position.getY(), paint);        	
        }		
        return true;
	}
	
	/**
	 * API Method: clear
	 */
	@Override
	public void clear() {
		super.clear();
		// TODO: remove event listener if there is any
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
	
	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}
	
	/*
	private class ImageTileListener extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			switch(msg.what) {
				case TileEvents.HTTP_LOAD_SUCCESS:
					//Util.printDebugMessage(" ...ImageTileListener: http load success...");	
					//Util.printDebugMessage(" ...unattache ImageTileListener from tile...");					
					// the data carried with HTTP_LOAD_SUCCESS message event is byte[] containing the image
					try {				
						
						Object obj_http = msg.obj;
						byte[] data_http = (byte[])((Event)obj_http).properties.get("data");						
						if(data_http != null) {
							Bitmap tile_http = BitmapFactory.decodeByteArray(data_http, 0, data_http.length);							
							//Util.printDebugMessage(" ...draw tile from HTTP...");
							Image.this.canvas.drawBitmap(tile_http, (int)Image.this.position.getX(), (int)Image.this.position.getY(), Image.this.paint);
						}
						
					} catch(Exception e) {
						Util.printErrorMessage(" ...exception in drawing tile directly from HTTP...");
						Util.printErrorMessage(e.toString());
					}
					Image.this.tileProvider.events.unregister(TileEvents.HTTP_LOAD_SUCCESS, this);
					break;
				case TileEvents.FS_LOAD_SUCCESS:
					//Util.printDebugMessage(" ...ImageTileListener: fs load success...");
					//Util.printDebugMessage(" ...unattache ImageTileListener from tile...");					
					// the data carried with FS_LOAD_SUCCESS message event is a bitmap
					try {
						
						Object data_fs = msg.obj;
						Bitmap tile_fs = (Bitmap)((Event)data_fs).properties.get("data");						
						if(tile_fs != null) {							
							Image.this.canvas.drawBitmap(tile_fs, (int)Image.this.position.getX(), (int)Image.this.position.getY(), Image.this.paint);
						}
						
						//Util.printDebugMessage(" ...draw tile..." + Image.this.getUrl() + " directly when received from FS...not invalidating the map...");
					} catch(Exception e) {						
						Util.printErrorMessage(" ...exception in drawing tile..." + Image.this.getUrl() + " directly from FS...");
						Util.printErrorMessage(e.toString());
					}
					Image.this.tileProvider.events.unregister(TileEvents.FS_LOAD_SUCCESS, this);
					
					// TODO: update tileProvider pending queue
					Image.this.tileProvider.removeFromPendingQueue(Image.this.getUrl());
					Util.printDebugMessage(" ...TileProvider pending queue size " + Image.this.tileProvider.getPendingQueueSize() + "...");
					
					break;
				case TileEvents.MEM_LOAD_SUCCESS:
					//Util.printDebugMessage(" ...ImageTileListener: mem load success...");
					//Util.printDebugMessage(" ...unattache ImageTileListener from tile...");
					//Util.printDebugMessage(" ...draw tile directly when received from Mem...not invalidating the map...");
					Image.this.tileProvider.events.unregister(TileEvents.MEM_LOAD_SUCCESS, this);
					break;
				case TileEvents.HTTP_LOAD_FAILURE:
					Util.printDebugMessage(" ...ImageTileListener: http load failure...");	
					Image.this.tileProvider.events.unregister(TileEvents.HTTP_LOAD_FAILURE, this);
					break;
				case TileEvents.FS_LOAD_FAILURE:
					//Util.printDebugMessage(" ...ImageTileListener: fs load failure...");		
					Image.this.tileProvider.events.unregister(TileEvents.FS_LOAD_FAILURE, this);
					break;
				case TileEvents.MEM_LOAD_FAILURE:
					//Util.printDebugMessage(" ...ImageTileListener: mem load failure...");		
					Image.this.tileProvider.events.unregister(TileEvents.MEM_LOAD_FAILURE, this);
					break;
				case TileEvents.FS_TILE_CORRUPTED:
					//Util.printDebugMessage(" ...ImageTileListener: fs tile corrupted...");		
					Image.this.tileProvider.events.unregister(TileEvents.FS_TILE_CORRUPTED, this);
					break;
				case TileEvents.HTTP_TILE_INACCESSIBLE:
					Util.printDebugMessage(" ...ImageTileListener: http tile inaccessible...");	
					Image.this.tileProvider.events.unregister(TileEvents.HTTP_TILE_INACCESSIBLE, this);
					break;
			}
		}
	}*/
}
