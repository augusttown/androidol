package com.androidol.util.tiles;

import java.util.HashMap;

import com.androidol.basetypes.Pixel;
import com.androidol.constants.UtilConstants;
import com.androidol.tile.Tile;
import com.androidol.util.Util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TileCache implements UtilConstants {
	
	//protected LRUTileHashMapCache mCachedTiles;
	//protected LRUTileArrayCache mCachedTiles;	
	protected Tile2DArrayCache mCachedTiles;
	
	/**
	 * Constructor TileCache
	 */
	public TileCache() {
		//this.mCachedTiles = new LRUTileHashMapCache(MAX_MEMORY_CACHE_NUM);
		//this.mCachedTiles = new LRUTileArrayCache(MAX_MEMORY_CACHE_NUM);
		this.mCachedTiles = new Tile2DArrayCache(MIN_MEMORY_CACHE_DIM);
	}
	
	/** 
	 * API Method: getTile
	 * 
	 * @param url
	 * @return
	 */
	public synchronized Bitmap getTile(final String url) {
		return this.mCachedTiles.get(url);
	}
	
	/**
	 * getTileAtCell(final Pixel cell)
	 * 
	 * @param cell
	 * @return
	 */
	/*
	public synchronized Bitmap getTileAtCell(final Pixel cell) {		
		if(this.mCachedTiles instanceof Tile2DArrayCache) {
			int row = (int)cell.getX();
			int col = (int)cell.getY();
			return this.mCachedTiles.get(row, col);
		} else {
			return null;
		}		
	}
	*/
	
	/**
	 * API Method: putTile
	 * 
	 * @param url
	 * @param tile
	 */
	public synchronized void putTile(final String url, final Bitmap bitmap, final Tile tile) {
		this.mCachedTiles.put(url, bitmap, tile);
	}	
	
	/**
	 * API Method: putTile
	 * 
	 * @param url
	 * @param data
	 */
	public synchronized void putTile(final String url, final byte[] data, final Tile tile) {		
		try {
			final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			this.mCachedTiles.put(url, bitmap, tile);
		} catch(Exception e) {
			Util.printErrorMessage(e.getMessage(), e);
		}		
	}
	
	/**
	 * API Method: removeTile
	 * 
	 * @param url
	 * @return
	 */
	public synchronized Bitmap removeTile(final String url) {
		return this.mCachedTiles.remove(url);
	}
}
