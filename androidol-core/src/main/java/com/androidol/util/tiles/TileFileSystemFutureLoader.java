package com.androidol.util.tiles;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.androidol.constants.UtilConstants;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.exceptions.EmptyCacheException;
import com.androidol.exceptions.TileFileCorruptedException;
import com.androidol.tile.Tile;
import com.androidol.util.Util;
import com.vividsolutions.jts.geom.Coordinate;

public class TileFileSystemFutureLoader extends TileFileSystemLoader implements UtilConstants {
	
	/**
	 * Constructor TileFileSystemFutureLoader
	 * 
	 * @param context
	 * @param cacheSizeInByte
	 * @param tileCache
	 * @param tileEvents
	 */
	public TileFileSystemFutureLoader(Context context, final int cacheSizeInByte, final TileCache tileCache, TileEvents tileEvents) {		
		super(context, cacheSizeInByte, tileCache, tileEvents);		
	}	
	
	/**
	 * 
	 * @param url
	 * @param tile
	 * @return
	 * @throws FileNotFoundException
	 * @throws TileFileCorruptedException
	 */
	public Future<?> loadTileToMemoryAsync(final String url, final String signature, final Tile tile) throws FileNotFoundException, TileFileCorruptedException {									
		if(this.pendingQueue.contains(url)) {
			//Util.printDebugMessage("...tile " + url + " already in the queue...skip loading from disk....");
			// TODO: should I kill the previous loading thread and start a new one?
			//this.pendingQueue.remove(url);
			return null;
		}				
		// determine whether the tile file exists on FS, or if so, is it corrupted
		FileInputStream fileInputStream = null;
		
		// cached tiles are stored in context
		//final String formattedUrlString = formatTileUrlToContextFilePath(url);
		// cached tiles are stores in SD card
		final String formattedUrlString = formatTileUrlToTileFilePath(url);
		try {			
			// cached tiles are stored in context
			fileInputStream = this.context.openFileInput(formattedUrlString);					
			//
			if(fileInputStream.available() == 0) {		
				//Util.printDebugMessage(" ...tile " + url + " may be corrupted on disk...");
				//this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));
				throw new TileFileCorruptedException("...load tile " + url + " from disk failed...file corrupted...");
			}	
		} catch(FileNotFoundException e) {
			throw e; // throw FileNotFoundException to TileProvider to trigger HTTP loading
		} catch(IOException e) {
			throw new TileFileCorruptedException("...load tile " + url + " from disk failed...file corrupted...");
		} finally {
			// TODO: delete this entry from disk so that - 
			// - the follow on http loader will get it again and fill the gap
		}
		final InputStream in = new BufferedInputStream(fileInputStream, IO_BUFFER_SIZE);								
		synchronized(this) {
			this.pendingQueue.add(url);
		}			
		Future<?> future = this.threadPool.submit(
			new Runnable() {
				@Override
				public void run() {
					//Util.printDebugMessage("@...started a thread for fs loading...signature: " + signature);					
					OutputStream out = null;										
					try {										
						TileFileSystemFutureLoader.this.database.incrementUse(formattedUrlString);
						final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
						out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
						StreamUtils.copy(in, out);
						out.flush();
						final byte[] data = dataStream.toByteArray();						
						//Util.printDebugMessage("...bitmap size in bytes: " + data.length + "...");
						if(data.length <= 0) {														
							Util.printWarningMessage("...load tile " + url + " from disk failed...file corrupted...");							
							//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));							
							TileFileSystemFutureLoader.this.tileCache.removeTile(url);							
							TileFileSystemFutureLoader.this.database.removeTile(formatTileUrlToTileFilePath(url));
							// TODO: update the this.cacheUsedInByte
							throw new TileFileCorruptedException("...load tile " + url + " from disk failed...file corrupted...");
						}
						final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						//Util.printDebugMessage(" ...image size: " + data.length + " bytes");								
						TileFileSystemFutureLoader.this.tileCache.putTile(url, bitmap, tile);						
						//TileFileSystemLoader.this.pendingQueue.remove(url);						
						//Util.printDebugMessage("...fs loader pending queue size: " + TileFileSystemLoader.this.pendingQueue.size());
						TileFileSystemFutureLoader.this.events.triggerEvent(TileEvents.FS_LOAD_SUCCESS, new Event(TileEvents.FS_LOAD_SUCCESS, url));						
					} catch(Exception e) {						
						//TileFileSystemFutureLoader.this.pendingQueue.remove(url);
						//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));												
						// TODO: exception caught here can not be throw out to main thread in TileProvider						
						Util.printErrorMessage("...load tile " + url + " from disk failed...file corrupted...");						
					} finally {
						StreamUtils.closeStream(in);
						StreamUtils.closeStream(out);
						TileFileSystemFutureLoader.this.pendingQueue.remove(url);							
						//Util.printDebugMessage("@...finish a thread for fs loading...signature: " + signature);
					}										
					//Util.printDebugMessage(" ...tile " + url + " removed from TileFileSystemLoader pending queue...queue size: " + TileFileSystemLoader.this.pendingQueue.size());
					//Util.printDebugMessage(" ...TileFileSystemLoader pending queue size: " + TileFileSystemLoader.this.pendingQueue.size());
				}
			}
		);			
		return future;		
	}		
}
