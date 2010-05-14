package com.androidol.util.tiles;

import java.util.HashMap;
import java.util.LinkedList;

import com.androidol.constants.UtilConstants;
import com.androidol.tile.Tile;
//import com.androidol.util.Util;


import android.graphics.Bitmap;
import android.util.Log;

public class LRUTileHashMapCache extends HashMap<String, Bitmap> implements UtilConstants {

	private static final long 	serialVersionUID 			= 1L;		
	private final int 			maxCacheSize; 							// Maximum cache size	
	private final 				LinkedList<String> list;				// Least Recently Used (LRU) list/

	/**
	 * Constructor LRUTileCache
	 * 
	 * @param maxCacheSize
	 */
	public LRUTileHashMapCache(final int maxCacheSize) {
		super(maxCacheSize);
		this.maxCacheSize = Math.max(0, maxCacheSize);
		this.list = new LinkedList<String>();
	}

	/**
	 * API Method: clear
	 */
	public synchronized void clear() {
		super.clear();
		list.clear();
	}
	
	/**
	 * API Method: put
	 * 
	 * @param key
	 * @param value
	 */
	public synchronized Bitmap put(final String key, final Bitmap value) {
		if(maxCacheSize == 0) {
			return null;
		}
		if(!super.containsKey(key) && !list.isEmpty() && list.size()+1>maxCacheSize) {
			final Object deadKey = list.removeLast();
			// recycle the bitmap to avoid memory leak
			super.remove(deadKey).recycle();	
			//Util.printDebugMessage(" ...bitmap get recycled...");
		}
		updateKey(key);
		//printCurrentMemoryCacheStatus();
		return super.put(key, value);
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param tile
	 * @return
	 */
	public synchronized Bitmap put(final String key, final Bitmap value, final Tile tile) {
		return put(key, value);
	}
	
	/**
	 * API Method: get
	 * 
	 * @param key 
	 * @return bitmap
	 * 
	 */
	public synchronized Bitmap get(final String key) {
		final Bitmap value = super.get(key);
		if(value != null) {
			updateKey(key);
		}
		return value;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public synchronized Bitmap remove(final String key) {
		list.remove(key);
		//printCurrentMemoryCacheStatus();
		return super.remove(key);
	}

	/**
	 * API Method: updateKey
	 * update the key to be the first in the list
	 * 
	 * @param key
	 */
	private void updateKey(final String key) {
		list.remove(key);
		list.addFirst(key);
	}
	
	/**
	 * API Method: printCurrentMemoryCacheStatus
	 */	
	/*
	private void printCurrentMemoryCacheStatus() {
		Log.i(DEBUGTAG, "	//=============================================================================");
		Log.i(DEBUGTAG, "	//...total memory cache size (in tiles): " + maxCacheSize);		
		Log.i(DEBUGTAG, "	//...used memory cache size (in tiles): " + this.list.size());		
		Log.i(DEBUGTAG, "	//=============================================================================");
	}
	*/
}
