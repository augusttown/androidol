package com.androidol.util.tiles;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.androidol.constants.UtilConstants;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.tile.Tile;
import com.androidol.util.Util;

public class TileHttpLoader implements UtilConstants {

	// ===========================================================
	// fields
	// ===========================================================		
	
	//public static final int 				LOAD_SUCCESS 		= 2001;
	//public static final int 				LOAD_FAILURE 		= 2000;
	
	protected HashSet<String> 				pendingQueue 		= new HashSet<String>();
	protected Context 						context;
	protected TileEvents					events				= null;
	
	protected TileFileSystemLoader 			tileFSLoader;
	protected ExecutorService 				threadPool 			= Executors.newFixedThreadPool(4);
	
	/**
	 * Constructor TileHttpLoader
	 * 
	 * @param context
	 * @param tileFSLoader
	 */
	public TileHttpLoader(Context context, TileFileSystemLoader tileFSLoader, TileEvents tileEvents) {
		this.context = context;
		this.tileFSLoader = tileFSLoader;
		this.events = tileEvents;
	}
	
	/**
	 * API Method: loadHttpTileAsync
	 * 
	 * @param url
	 * @param callback
	 */
	public void loadHttpTileAsync(final String url, final Tile tile) {
		this.threadPool.execute(
			new Runnable() {
				@Override
				public void run() {
					InputStream in = null;
					OutputStream out = null;
					try {						
						//Util.printDebugMessage("...load tile from url: " + url + "...");										
						in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
						final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
						out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
						StreamUtils.copy(in, out);
						out.flush();
						byte[] data = dataStream.toByteArray();						
						TileHttpLoader.this.tileFSLoader.saveFile(url, data); 					
						//Util.printDebugMessage("...tile saved to: " + url + "...");
											
						/*
						final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						Event evt = new Event(TileEvents.FS_LOAD_SUCCESS);
						evt.attach("tile", tile);
						evt.attach("bitmap", bitmap);						
						TileHttpLoader.this.events.triggerEvent(TileEvents.HTTP_LOAD_SUCCESS, new Event(TileEvents.HTTP_LOAD_SUCCESS, evt));						
						*/
						TileHttpLoader.this.events.triggerEvent(TileEvents.HTTP_LOAD_SUCCESS, new Event(TileEvents.HTTP_LOAD_SUCCESS, url));
						synchronized(this) {
							TileHttpLoader.this.pendingQueue.remove(url);
						}
						//Util.printDebugMessage(" ...tile " + url + " removed from TileHttpLoader pending queue...");
						//Util.printDebugMessage(" ...TileHttpLoader pending queue size: " + TileHttpLoader.this.pendingQueue.size());
					} catch(Exception e) {
						//TileHttpLoader.this.events.triggerEvent(TileEvents.HTTP_LOAD_FAILURE, new Event(TileEvents.HTTP_LOAD_FAILURE, url));						
						Util.printErrorMessage("...error loading tile...exception: " + e.getClass().getSimpleName() + "...", e);						
						/* 
						 * TODO What to do when loading tile caused an error?
						 * Also remove it from the mPending?
						 * Doing not blocks it for the whole existence of this TileDownloder.
						 */					
				} finally {
					StreamUtils.closeStream(in);
					StreamUtils.closeStream(out);
					TileHttpLoader.this.pendingQueue.remove(url);
				}
			}
		});
	}
	
	/**
	 * API Method: getTileAsync
	 * 
	 * @param url
	 * @param callback
	 */
	public void getTileAsync(final String url, Tile tile) {
		if(this.pendingQueue.contains(url)) {
			//Util.printDebugMessage("...tile " + url + " already in the queue...skip loading from http....");
			return;
		}
		synchronized(this) {
			this.pendingQueue.add(url);
		}
		//Util.printDebugMessage(" ...tile " + url + " added in TileHttpLoader pending queue...queue size: " + this.pendingQueue.size());
		//Util.printDebugMessage(" ...TileHttpLoader pending queue size: " + TileHttpLoader.this.pendingQueue.size());
		loadHttpTileAsync(url, tile);
	}
}
