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
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public class TileFileSystemLoader implements UtilConstants {
	
	// ===========================================================
	// fields
	// ===========================================================	
	
	//public static final int 								LOAD_SUCCESS 		= 1001;
	//public static final int 								LOAD_FAILURE 		= 1000;
	
	protected final Context 								context;
	protected final TileFileSystemLoaderDB 					database;
	protected final int 									cacheSizeInByte;
	protected int 											cacheUsedInByte;
	
	protected ExecutorService 								threadPool 			= Executors.newFixedThreadPool(8);
	protected TileCache 									tileCache;
	protected HashSet<String> 								pendingQueue 		= new HashSet<String>();
	protected TileEvents									events				= null;
	
	/**
	 * Constructor TileFileSystemLoader
	 * 
	 * @param context
	 * @param cacheSizeInByte
	 * @param tileCache
	 */
	public TileFileSystemLoader(Context context, final int cacheSizeInByte, final TileCache tileCache, TileEvents tileEvents) {		
		this.context = context;
		this.cacheSizeInByte = cacheSizeInByte;
		this.tileCache = tileCache;		
		this.database = new TileFileSystemLoaderDB(context);
		this.cacheUsedInByte = this.database.getCacheUsedInByte();	
		this.events = tileEvents;
		//Util.printDebugMessage("...cache size on disk: " + this.cacheSizeInByte + " bytes" + " | " +  "used cache: " + this.cacheUsedInByte + " bytes...");		
	}
	
	/**
	 * API Method: getCacheUsedInByte
	 * 
	 * @return
	 * return used cache size
	 */
	public int getCacheUsedInByte() {
		return this.cacheUsedInByte;
	}
	
	/**
	 * 
	 * @param url
	 * @param tile
	 * @return
	 * @throws FileNotFoundException
	 * @throws TileFileCorruptedException
	 */
	public Bitmap loadTileToMemorySync(final String url) throws FileNotFoundException, TileFileCorruptedException {
		if(this.pendingQueue.contains(url)) {
			//Util.printDebugMessage("...tile " + url + " already in the queue...skip loading from disk....");			
			this.pendingQueue.remove(url);
		}
		final String formattedUrlString = formatTileUrlToTileFilePath(url);		
		FileInputStream fileInputStream = this.context.openFileInput(formattedUrlString);
		// determine whether the tile file on FS is corrupted or not
		try {
			if(fileInputStream.available() == 0) {		
				Util.printWarningMessage(" ...tile " + url + " may be corrupted on disk...");
				//this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));
				throw new TileFileCorruptedException(" ...tile " + url + " may be corrupted on disk...");
			}	
		} catch(IOException e) {
			throw new TileFileCorruptedException(e.getMessage());
		}
		final InputStream in = new BufferedInputStream(fileInputStream, IO_BUFFER_SIZE);	
		synchronized(this) {
			this.pendingQueue.add(url);
		}
		OutputStream out = null;	
		Bitmap bitmap = null;
		try {						
			TileFileSystemLoader.this.database.incrementUse(formattedUrlString);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();
		
			final byte[] data = dataStream.toByteArray();						
			//Util.printDebugMessage("...bitmap size in bytes: " + data.length + "...");
			if(data.length <= 0) {														
				Util.printWarningMessage("...load tile " + url + " from disk failed...file corrupted...");							
				//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));							
				TileFileSystemLoader.this.tileCache.removeTile(url);							
				TileFileSystemLoader.this.database.removeTile(formatTileUrlToTileFilePath(url));
				// TODO: update the this.cacheUsedInByte
				throw new Exception("...load tile " + url + " from disk failed...file corrupted...");
			}
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			TileFileSystemLoader.this.pendingQueue.remove(url);
			TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_LOAD_SUCCESS, new Event(TileEvents.FS_LOAD_SUCCESS, url));
			//Util.printDebugMessage(" ...image size: " + data.length + " bytes");						
		} catch (Exception e) {																
			Util.printErrorMessage("...error loading tile from disk...exception: " + e.getClass().getSimpleName() + "...", e);
			TileFileSystemLoader.this.pendingQueue.remove(url);
		} finally {
			StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
			TileFileSystemLoader.this.pendingQueue.remove(url);
		}
		return bitmap;
	}
	
	/**
	 * API Method: loadTileToMemoryAsync
	 * 
	 * @param url
	 * @param callback
	 * @throws FileNotFoundException
	 */
	public void loadTileToMemoryAsync(final String url, final Tile tile) throws FileNotFoundException, TileFileCorruptedException {		
		if(this.pendingQueue.contains(url)) {
			//Util.printDebugMessage("...tile " + url + " already in the queue...skip loading from disk....");
			// TODO: should I kill the previous loading thread and start a new one?
			return;
		}						
		// cached tiles are stored in context		
		final String formattedUrlString = formatTileUrlToTileFilePath(url);
		FileInputStream fileInputStream = this.context.openFileInput(formattedUrlString);				
		// determine whether the tile file on FS is corrupted or not
		try {
			if(fileInputStream.available() == 0) {		
				Util.printWarningMessage(" ...tile " + url + " may be corrupted on disk...");
				//this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));
				throw new TileFileCorruptedException(" ...tile " + url + " may be corrupted on disk...");
			}	
		} catch(IOException e) {
			throw new TileFileCorruptedException(e.getMessage());
		} finally {
			// TODO: delete this entry from disk
			// so that the follow on http loader will get it again and fill the gap
		}
		final InputStream in = new BufferedInputStream(fileInputStream, IO_BUFFER_SIZE);		
						
		synchronized(this) {
			this.pendingQueue.add(url);
		}
		//Util.printDebugMessage(" ...tile " + url + " added in TileFileSystemLoader pending queue...");
		//Util.printDebugMessage(" ...TileFileSystemLoader pending queue size: " + this.pendingQueue.size());
		this.threadPool.execute(
			new Runnable() {
				@Override
				public void run() {
					//Util.printDebugMessage("@...started a thread for fs loading...url: " + url);					
					OutputStream out = null;					
					try {						
						TileFileSystemLoader.this.database.incrementUse(formattedUrlString);
						final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
						out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
						StreamUtils.copy(in, out);
						out.flush();
					
						final byte[] data = dataStream.toByteArray();						
						//Util.printDebugMessage("...bitmap size in bytes: " + data.length + "...");
						if(data.length <= 0) {														
							Util.printWarningMessage("...load tile " + url + " from disk failed...file corrupted...");							
							//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));							
							TileFileSystemLoader.this.tileCache.removeTile(url);							
							TileFileSystemLoader.this.database.removeTile(formatTileUrlToTileFilePath(url));
							// TODO: update the this.cacheUsedInByte
							throw new Exception("...load tile " + url + " from disk failed...file corrupted...");
						}
						final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						//Util.printDebugMessage(" ...image size: " + data.length + " bytes");								
						TileFileSystemLoader.this.tileCache.putTile(url, bitmap, tile);						
						//TileFileSystemLoader.this.pendingQueue.remove(url);						
						//Util.printDebugMessage("...fs loader pending queue size: " + TileFileSystemLoader.this.pendingQueue.size());
						TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_LOAD_SUCCESS, new Event(TileEvents.FS_LOAD_SUCCESS, url));
					} catch(InterruptedException e) {
						// when thread is blocked, interrupt() call throws exception
						// TODO: thread interrupted
						//
					} catch (Exception e) {						
						TileFileSystemLoader.this.pendingQueue.remove(url);
						//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));							
						Util.printErrorMessage("...error loading tile from disk...exception: " + e.getClass().getSimpleName() + "...", e);						
					} finally {
						StreamUtils.closeStream(in);
						StreamUtils.closeStream(out);
						TileFileSystemLoader.this.pendingQueue.remove(url);	
						//Util.printDebugMessage("...finish a thread for fs loading...");
						//Util.printDebugMessage("@...finish a thread for fs loading...url: " + url);
					}										
					//Util.printDebugMessage(" ...tile " + url + " removed from TileFileSystemLoader pending queue...queue size: " + TileFileSystemLoader.this.pendingQueue.size());
					//Util.printDebugMessage(" ...TileFileSystemLoader pending queue size: " + TileFileSystemLoader.this.pendingQueue.size());
			}
		});
	}
	
	/**
	 * API Method: saveFile
	 * 
	 * @param url
	 * @param data
	 * @throws IOException
	 */
	public void saveFile(final String url, final byte[] data) throws IOException {
		// store cached tiles in context
		
		final String fileName = formatTileUrlToTileFilePath(url);
		final FileOutputStream fos = this.context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);		
		final BufferedOutputStream bos = new BufferedOutputStream(fos, StreamUtils.IO_BUFFER_SIZE);		
		bos.write(data);					
		bos.flush();
		bos.close();
				
		synchronized(this) {
			final int bytesGrown = this.database.addTileOrIncrement(fileName, data.length);
			this.cacheUsedInByte += bytesGrown;
			//Util.printDebugMessage("used cache is now: " + this.cacheUsedInByte + " bytes...");
			try {
				if(this.cacheUsedInByte > this.cacheSizeInByte){					
					//Util.printDebugMessage(" ...used cache exceeds max cache size...free cache...");					
					this.cacheUsedInByte -= this.database.deleteOldest((int)(this.cacheSizeInByte * 0.05f)); // Free 5% of cache
				}
				//printCurrentFileSystemCacheStatus();
			} catch(EmptyCacheException e) {				
				Util.printErrorMessage("...cache empty...", e);				
			}
		}
	}
	
	/**
	 * API Method: cleanupCache
	 */
	public void cleanupCache(){
		cleanupCacheBy(Integer.MAX_VALUE); // Delete all
	}
	
	/**
	 * API Method: cleanupCacheBy
	 * 
	 * @param bytesToCut
	 */
	public void cleanupCacheBy(final int bytesToCut){
		try {
			this.database.deleteOldest(bytesToCut);
			this.cacheUsedInByte = 0;
			//printCurrentFileSystemCacheStatus();
		} catch(EmptyCacheException e) {			
			Util.printErrorMessage(" ...cache empty...", e);			
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	
	protected String formatTileUrlToTileFilePath(String url) {
		return url.substring(7).replace("/", "_");
	}
		
	/**
	 * 
	 */
	public void printCurrentFileSystemCacheStatus() {
		Log.i(DEBUGTAG, "	//=============================================================================");
		Log.i(DEBUGTAG, "	//...total file system cache size (in bytes): " + this.cacheSizeInByte);		
		Log.i(DEBUGTAG, "	//...used file system cache size (in bytes): " + this.cacheUsedInByte);		
		Log.i(DEBUGTAG, "	//=============================================================================");
	}	
}
