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
import java.util.concurrent.Future;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.androidol.constants.UtilConstants;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.tile.Tile;
import com.androidol.util.Util;

public class TileHttpFutureLoader extends TileHttpLoader implements UtilConstants {	
	
	/**
	 * Constructor TileHttpLoader
	 * 
	 * @param context
	 * @param tileFSLoader
	 */
	public TileHttpFutureLoader(Context context, TileFileSystemLoader tileFSLoader, TileEvents tileEvents) {
		super(context, tileFSLoader, tileEvents);
	}
	
	/**
	 * API Method: loadHttpTileAsync
	 * 
	 * @param url
	 * @param callback
	 */
	public Future<?> loadHttpTileAsync(final String url, final String signature, final Tile tile) {		
		Future<?> future = this.threadPool.submit(
			new Runnable() {
				@Override
				public void run() {
					InputStream in = null;
					OutputStream out = null;
					try {						
						//Thread.sleep(1000);
						//Util.printDebugMessage("@...started a thread for http loading...signature: " + signature);
						//Util.printDebugMessage("...load tile from url: " + url + "...");										
						in = new BufferedInputStream(new URL(url).openStream(), IO_BUFFER_SIZE);
						final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
						out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
						StreamUtils.copy(in, out);
						out.flush();
						byte[] data = dataStream.toByteArray();						
						TileHttpFutureLoader.this.tileFSLoader.saveFile(url, data); 					
						//Util.printDebugMessage("...tile saved to: " + url + "...");
											
						/*
						final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						Event evt = new Event(TileEvents.FS_LOAD_SUCCESS);
						evt.attach("tile", tile);
						evt.attach("bitmap", bitmap);						
						TileHttpLoader.this.events.triggerEvent(TileEvents.HTTP_LOAD_SUCCESS, new Event(TileEvents.HTTP_LOAD_SUCCESS, evt));						
						*/
						TileHttpFutureLoader.this.events.triggerEvent(TileEvents.HTTP_LOAD_SUCCESS, new Event(TileEvents.HTTP_LOAD_SUCCESS, url));
						synchronized(this) {
							TileHttpFutureLoader.this.pendingQueue.remove(url);
						}
						//Util.printDebugMessage(" ...tile " + url + " removed from TileHttpLoader pending queue...");
						//Util.printDebugMessage(" ...TileHttpLoader pending queue size: " + TileHttpLoader.this.pendingQueue.size());
						// TODO: when doing threads canceling, what will happen if a loading gets canceled? will it throw exception?
					} catch(Exception e) {
						//TileHttpLoader.this.events.triggerEvent(TileEvents.HTTP_LOAD_FAILURE, new Event(TileEvents.HTTP_LOAD_FAILURE, url));						
						//Util.printErrorMessage(e.getClass().getName());
						Util.printErrorMessage("...error loading tile from http...exception: " + e.getMessage());						
						/* 
						 * TODO What to do when loading tile caused an error?
						 * Also remove it from the mPending?
						 * Doing not blocks it for the whole existence of this TileDownloder.
						 */					
					} finally {
						StreamUtils.closeStream(in);
						StreamUtils.closeStream(out);			
						TileHttpFutureLoader.this.pendingQueue.remove(url);
						//Util.printDebugMessage("@...finished a thread for http loading...signature: " + signature);
					}
				}
			});
		return future;
	}
	
	/**
	 * getTileAsync
	 * 
	 * @param url
	 * @param callback
	 */
	public Future<?> getTileAsync(final String url, final String signature, Tile tile) {
		if(this.pendingQueue.contains(url)) {
			//Util.printDebugMessage("...tile " + url + " already in the queue...skip loading from http....");
			return null;
		}
		synchronized(this) {
			this.pendingQueue.add(url);
		}
		//Util.printDebugMessage(" ...tile " + url + " added in TileHttpLoader pending queue...queue size: " + this.pendingQueue.size());
		//Util.printDebugMessage(" ...TileHttpLoader pending queue size: " + TileHttpLoader.this.pendingQueue.size());
		return loadHttpTileAsync(url, signature, tile);
	}
}
