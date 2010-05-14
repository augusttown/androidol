package com.androidol.util.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Bitmap;

import com.androidol.basetypes.Pixel;
import com.androidol.constants.UtilConstants;
import com.androidol.tile.Tile;
import com.androidol.util.Util;

public class Tile2DArrayCache implements UtilConstants {
	
	private static final long 		serialVersionUID = 1L;
	private final int 				dimension;
	private Bitmap[][] 				bitmapMatrix;
	private HashMap<String, Pixel> 	bitmapCellMatrix;
	
	/**
	 * Constructor
	 */	
	public Tile2DArrayCache(final int dimension) {
		// TODO: determine minimum dimension size based on tile size
		// TODO: take into account that different layers may have different tile sizes 
		this.dimension = Math.min(Math.max(MIN_MEMORY_CACHE_DIM, dimension), MAX_MEMORY_CACHE_DIM);
		this.bitmapMatrix = new Bitmap[dimension][dimension];
		this.bitmapCellMatrix = new HashMap<String, Pixel>();
		
		for(int i=0; i<dimension; i++) {
			for(int j=0; j<dimension; j++) {
				this.bitmapMatrix[i][j] = null;
			}
		}
	}
		
	/**
	 * API Method: clear
	 */
	public synchronized void clear() {
		for(int i=0; i<this.dimension; i++) {
			for(int j=0; j<this.dimension; j++) {
				if(this.bitmapMatrix[i][j]!=null && this.bitmapMatrix[i][j].isRecycled()==false) {
					this.bitmapMatrix[i][j].recycle();
				}
				this.bitmapMatrix[i][j] = null;
			}
		}
	}	
	
	/**
	 * API Method: put
	 * 
	 * @param url
	 * @param bitmap
	 * @return {Bitmap}
	 */
	public synchronized Bitmap put(final String url, final Bitmap bitmap, final Tile tile) {								
		Pixel cell = tile.getCell();	
		removeStaledLinks(cell);				
		this.bitmapCellMatrix.put(url, cell);			
		int row = (int)cell.getX();
		int col = (int)cell.getY();
		if(this.bitmapMatrix[row][col]!=null && this.bitmapMatrix[row][col].isRecycled()==false) {			
			this.bitmapMatrix[row][col].recycle();
			this.bitmapMatrix[row][col] = null;
		}
		this.bitmapMatrix[row][col] = bitmap;
		//calculateNumOfCachedTiles();
		return bitmap;
	}
	
	/**
	 * API Method: get
	 * 
	 * @param url
	 * @return {Bitmap}
	 */
	public synchronized Bitmap get(final String url) {		
		if(this.bitmapCellMatrix.get(url) == null) {
			return null;
		};
		Pixel cell = this.bitmapCellMatrix.get(url);
		int row = (int)cell.getX();
		int col = (int)cell.getY();
		Bitmap bitmap = null;
		try {			
			bitmap = this.bitmapMatrix[row][col];
			if(bitmap== null || bitmap.isRecycled() == true) {
				bitmap = null;
			}
		} catch(IndexOutOfBoundsException e) {
			Util.printDebugMessage(" ...Tile2DArrayCache...try to get a cached tile at (" + row + "," + col + ") which doesn't exist...");
			return null;
		}
		//Util.printDebugMessage(" ...get tile: " + url + "...at (" + row + "," + col + ")...");
		return bitmap;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public synchronized Bitmap get(final int row, final int col) {		
		Bitmap bitmap = null;
		try {			
			bitmap = this.bitmapMatrix[row][col];
			if(bitmap== null || bitmap.isRecycled() == true) {
				bitmap = null;
			}
		} catch(IndexOutOfBoundsException e) {
			Util.printDebugMessage(" ...Tile2DArrayCache...try to get a cached tile at (" + row + "," + col + ") which doesn't exist...");
			return null;
		}
		//Util.printDebugMessage(" ...get tile: " + url + "...at (" + row + "," + col + ")...");
		return bitmap;
	}
	
	/**
	 * API Method: remove
	 * 
	 * @param url
	 * @return {Bitmap}
	 */
	public synchronized Bitmap remove(final String url) {
		if(this.bitmapCellMatrix.get(url)==null) {
			return null;
		};
		Pixel cell = this.bitmapCellMatrix.get(url);
		int row = (int)cell.getX();
		int col = (int)cell.getY();
		Bitmap bitmap = null;		
		try {
			if(this.bitmapMatrix[row][col]!=null && this.bitmapMatrix[row][col].isRecycled()==false) {
				this.bitmapMatrix[row][col].recycle();
				this.bitmapMatrix[row][col] = null;
			}
		} catch(IndexOutOfBoundsException e) {
			Util.printDebugMessage(" ...Tile2DArrayCache...try to remove a cached tile which doesn't exist...");
			return null;
		} finally {
			this.bitmapCellMatrix.remove(url);
		}		
		//calculateNumOfCachedTiles();
		//Util.printDebugMessage(" ...get tile: " + url + "...at (" + row + "," + col + ")...");
		return bitmap;
	}
	
	/**
	 * private method: calculateNumOfCachedTiles
	 *   print out number of cached tiles
	 */
	/*
	private void calculateNumOfCachedTiles() {		
		int numOfCachedTiles = 0;
	    for(int i=0; i<dimension; i++) {	    		    		    	
	    	for(int j=0; j<dimension; j++) {
	    		if(this.bitmapMatrix[i][j]!=null && this.bitmapMatrix[i][j].isRecycled()==false) {
	    			numOfCachedTiles++;
	    		}
	    	}	    	
	    }
	    Util.printDebugMessage(" ...number of bitmap cached: " + numOfCachedTiles + "...");
	}
	*/
	
	/**
	 * private method: removeStaledLinks
	 * 
	 * @param cell {Pixel}
	 * @return 
	 */	
	private void removeStaledLinks(Pixel cell) {
		Iterator<String> i = (Iterator<String>)this.bitmapCellMatrix.keySet().iterator();
		synchronized(this.bitmapCellMatrix) {					
			while(i.hasNext()) {
				String url = i.next();
				Pixel staledCell = this.bitmapCellMatrix.get(url);
				if(((int)staledCell.getX() == (int)cell.getX()) && ((int)staledCell.getY() == (int)cell.getY())) {
					/*
					 * Do NOT remove from HashMap directly, which causes ConcurrentModificationException exception
					 *   when multi-thread are trying to iterating and modifiying the hash map at the same time  
					 */
					// DO NOT DO THIS //this.bitmapCellMatrix.remove(url);
					/*
					 * Instead remove it from iterator
					 */
					i.remove();
				}
			}
		}
	}	
}
