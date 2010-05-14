package com.androidol.util.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.androidol.constants.UtilConstants;
import com.androidol.tile.Tile;
import com.androidol.util.Util;
//import com.androidol.util.Util;


import android.graphics.Bitmap;
import android.util.Log;

public class LRUTileArrayCache implements UtilConstants {

	private static final long 				serialVersionUID 			= 1L;		
	private final int 						maxCacheSize; 			    // Maximum cache size	
	private final 							LinkedList<String> list;	// Least Recently Used (LRU) list/
	private Bitmap[]						bitmapArray;	
	private HashMap<String, Integer>		bitmapPositionArray;
	private int 							cursor;

	/**
	 * Constructor LRUTileArrayCache
	 * 
	 * @param maxCacheSize
	 */
	public LRUTileArrayCache(final int maxCacheSize) {		
		this.maxCacheSize = Math.max(0, maxCacheSize);
		this.list = new LinkedList<String>();		
		this.bitmapArray = new Bitmap[this.maxCacheSize];	
		this.bitmapPositionArray = new HashMap<String, Integer>(maxCacheSize);
		this.cursor = 0;
	}

	/**
	 * API Method: clear
	 */
	public synchronized void clear() {		
		this.list.clear();
		this.bitmapPositionArray.clear();
		// recycle Bitmaps in array
		for(int i=0; i<this.bitmapArray.length; i++) {
			if(this.bitmapArray[i]!=null && this.bitmapArray[i].isRecycled()==false) {
				this.bitmapArray[i].recycle();
				this.bitmapArray[i] = null;
			}
		}
	}
	
	/**
	 * API Method: put
	 * 
	 * @param key
	 * @param value
	 */
	public synchronized Bitmap put(final String url, final Bitmap value) {
		if(maxCacheSize == 0) {
			return null;
		}		
		if(!this.bitmapPositionArray.containsKey(url) && !list.isEmpty() && list.size()+1 > maxCacheSize) {
			final Object deadKey = list.removeLast();
			// TODO: recycle the bitmap to avoid memory leak
			Integer deadIntKey = this.bitmapPositionArray.remove(deadKey);
			if(this.bitmapArray[deadIntKey.intValue()]!=null) {
				this.bitmapArray[deadIntKey.intValue()].recycle();
				this.bitmapArray[deadIntKey.intValue()] = null;
				this.cursor = deadIntKey;
			}
			//Util.printDebugMessage(" ...bitmap get recycled...");
		}
		updateKey(url);
		//printCurrentMemoryCacheStatus();		
		while(this.bitmapArray[this.cursor] != null) {
			this.cursor = (this.cursor + 1) % this.maxCacheSize;
		}				
		this.bitmapPositionArray.put(url, new Integer(this.cursor));			
		this.bitmapArray[this.cursor] = value;		
		return this.bitmapArray[this.cursor];		
	}	
	
	/**
	 * API Method: put
	 * 
	 * @param key
	 * @param value
	 */
	public synchronized Bitmap put(final String url, final Bitmap value, final Tile tile) {
		return put(url, value);	
	}	
	
	/**
	 * API Method: get
	 * 
	 * @param key 
	 * @return bitmap
	 * 
	 */
	public synchronized Bitmap get(final String url) {
		if(this.bitmapPositionArray.get(url) == null) {
			return null;
		};
		final Bitmap value = this.bitmapArray[this.bitmapPositionArray.get(url).intValue()];
		if(value != null) {
			updateKey(url);
		}
		return value;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public synchronized Bitmap remove(final String url) {
		list.remove(url);
		//printCurrentMemoryCacheStatus();
		if(this.bitmapPositionArray.get(url)==null) {return null;};
		return this.bitmapArray[this.bitmapPositionArray.remove(url)];
	}

	/**
	 * API Method: updateKey
	 * update the key to be the first in the list
	 * 
	 * @param key
	 */
	private void updateKey(final String url) {
		this.list.remove(url);
		this.list.addFirst(url);
	}
	
	/**
	 * API Method: printCurrentMemoryCacheStatus
	 */	
	/*
	public void printCurrentMemoryCacheStatus() {
		Log.i(DEBUGTAG, "	//=============================================================================");
		Log.i(DEBUGTAG, "	//...total memory cache size (in tiles): " + maxCacheSize);		
		Log.i(DEBUGTAG, "	//...used memory cache size (in tiles): " + this.list.size());		
		Log.i(DEBUGTAG, "	//=============================================================================");
	}
	*/
}
