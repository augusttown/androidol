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

public class TileSDCardFutureLoader extends TileFileSystemLoader implements UtilConstants {
	
	protected static final int CacheSizeInByteOnSDCard 		= 512*1024*1024;
	protected boolean 		   mergeContextCacheToSDCard 	= true;	
	protected String		   rootOutputPath			 	= "unionstation"; // just a joke
	
	/**
	 * Constructor TileFileSystemFutureLoader
	 * 
	 * @param context
	 * @param cacheSizeInByte
	 * @param tileCache
	 * @param tileEvents
	 */
	public TileSDCardFutureLoader(Context context, final int cacheSizeInByte, final TileCache tileCache, TileEvents tileEvents) {		
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
		// cached tiles are stores in SD card
		final String formattedUrlString = formatTileUrlToTileFilePath(url);
		try {										
			// cached tiles are stores in SD card
			fileInputStream = new FileInputStream(new File(formattedUrlString));
			// TODO: check file existence
			if(fileInputStream.available() == 0) {		
				Util.printDebugMessage(" ...tile " + url + " may be corrupted on disk...");
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
						// TODO: no need 
						//TileSDCardFutureLoader.this.database.incrementUse(formattedUrlString);
						
						final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
						out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
						StreamUtils.copy(in, out);
						out.flush();
						final byte[] data = dataStream.toByteArray();						
						//Util.printDebugMessage("...bitmap size in bytes: " + data.length + "...");
						if(data.length <= 0) {														
							Util.printWarningMessage("...load tile " + url + " from disk failed...file corrupted...");							
							//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));							
							TileSDCardFutureLoader.this.tileCache.removeTile(url);							
							TileSDCardFutureLoader.this.database.removeTile(formatTileUrlToTileFilePath(url));
							// TODO: update the this.cacheUsedInByte
							throw new TileFileCorruptedException("...load tile " + url + " from disk failed...file corrupted...");
						}
						final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						//Util.printDebugMessage(" ...image size: " + data.length + " bytes");								
						TileSDCardFutureLoader.this.tileCache.putTile(url, bitmap, tile);						
						//TileFileSystemLoader.this.pendingQueue.remove(url);						
						//Util.printDebugMessage("...fs loader pending queue size: " + TileFileSystemLoader.this.pendingQueue.size());
						TileSDCardFutureLoader.this.events.triggerEvent(TileEvents.FS_LOAD_SUCCESS, new Event(TileEvents.FS_LOAD_SUCCESS, url));						
						//Util.printDebugMessage("...tile loaded in memory: " + url + "...");
					} catch(Exception e) {						
						//TileFileSystemFutureLoader.this.pendingQueue.remove(url);
						//TileFileSystemLoader.this.events.triggerEvent(TileEvents.FS_TILE_CORRUPTED, new Event(TileEvents.FS_TILE_CORRUPTED, url));												
						// TODO: exception caught here can not be throw out to main thread in TileProvider						
						Util.printErrorMessage("...load tile " + url + " from disk failed...file corrupted...");						
					} finally {
						StreamUtils.closeStream(in);
						StreamUtils.closeStream(out);
						TileSDCardFutureLoader.this.pendingQueue.remove(url);							
						//Util.printDebugMessage("@...finish a thread for fs loading...signature: " + signature);
					}										
					//Util.printDebugMessage(" ...tile " + url + " removed from TileFileSystemLoader pending queue...queue size: " + TileFileSystemLoader.this.pendingQueue.size());
					//Util.printDebugMessage(" ...TileFileSystemLoader pending queue size: " + TileFileSystemLoader.this.pendingQueue.size());
				}
			}
		);			
		return future;		
	}
	
	/**
	 * API Method: saveFile
	 * 
	 * @param url
	 * @param data
	 * @throws IOException
	 */
	@Override
	public void saveFile(final String url, final byte[] data) throws IOException {		
		// store cached tiles on SD card		
		final String fileName = formatTileUrlToTileFilePath(url);
		File tileFile = new File(fileName);		
		// TODO: check status of SD card to see whether it is writable
		// TODO: be more careful on folder handling
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			if(tileFile.getParentFile().exists() == false) {
				tileFile.getParentFile().mkdirs();
			}															
			fos = new FileOutputStream(tileFile);					
			bos = new BufferedOutputStream(fos, StreamUtils.IO_BUFFER_SIZE);		
			bos.write(data);					
			bos.flush();			
		} catch(Exception e) {
			Util.printDebugMessage(e.getClass().getName());
			Util.printDebugMessage("...error saving tiles on SD card..." + e.getMessage());
		} finally {
			bos.close();
		}
		
		synchronized(this) {
			final int bytesGrown = this.database.addTileOrIncrement(fileName, data.length);
			this.cacheUsedInByte += bytesGrown;
			//Util.printDebugMessage("used cache is now: " + this.cacheUsedInByte + " bytes...");
			try {
				// TODO: SD card obviously has a much bigger cache than context
				if(this.cacheUsedInByte > CacheSizeInByteOnSDCard){	// SD card cache: 512M bytes				
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
	 * 
	 */
	@Override
	protected String formatTileUrlToTileFilePath(String url) {
		String tileFilePath = "";
		String basePath = "";
		String externalStoreDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		if(externalStoreDir!=null && "".equalsIgnoreCase(externalStoreDir)==false) {
			basePath = externalStoreDir + "/androidol/packages/" + this.rootOutputPath;			
		} else {
			basePath = "/sdcard/androidol/packages/" + this.rootOutputPath;
		}		
		String httpStripped = url.substring(7);
		String folderName = httpStripped.substring(0, httpStripped.indexOf("/"));
		String fileName = httpStripped.substring(httpStripped.indexOf("/")+1, httpStripped.length()).replace('/', '_');
		// !!!TODO: this is just a temporary workaround
		
		if(url.indexOf("WMS")>=0 || url.indexOf("Export")>=0) {
			// TODO: figure out a better algorithm to encode url to file name for WMS request
			fileName = fileName.substring(fileName.indexOf("BBOX=")+5, fileName.indexOf("&BGCOLOR"));
			fileName = fileName.replace(',', '_');
			fileName = fileName.replace('-', '_');
			fileName = fileName + ".png";					
		}
		
		tileFilePath = basePath + "/" + folderName + "/" + fileName; 
		return tileFilePath;
	}

	// ==================================================================================================
	// getters & setters
	// ==================================================================================================
	
	public String getRootOutputPath() {
		return rootOutputPath;
	}

	public void setRootOutputPath(String rootOutputPath) {
		this.rootOutputPath = rootOutputPath;
	}
	
}
