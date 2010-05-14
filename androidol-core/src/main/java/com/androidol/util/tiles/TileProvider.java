package com.androidol.util.tiles;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.androidol.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.androidol.constants.UtilConstants;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.exceptions.TileFileCorruptedException;
//import com.androidol.events.Event;
//import com.androidol.events.LayerEvents;
import com.androidol.layer.Layer;
import com.androidol.util.Util;
import com.androidol.test.TestActivity;
import com.androidol.tile.Tile;

public class TileProvider implements UtilConstants {

	// ===========================================================
	// fields 
	// ===========================================================	
	
	protected Bitmap 						loadingTile;			// static bitmap tile for loading
	protected Bitmap 						transparentTile;		// static transparent bitmap tile
	public    Bitmap 						missingTile;			// static error bitmap tile
	
	protected Context 						context;
	protected Layer							layer;
	protected TileCache 					tileCache;
	//protected TileHttpLoader 				httpLoader;
	protected TileHttpFutureLoader 				httpLoader;
	//protected TileFileSystemLoader 			fileSystemLoader;
	//protected TileFileSystemFutureLoader 	fileSystemLoader;
	protected TileSDCardFutureLoader 	fileSystemLoader;
	//protected TileZippedSDCardFutureLoader 	fileSystemLoader;
	
	public TileEvents						events						= new TileEvents();	
	
	protected ConcurrentHashMap<String, ArrayList<Future<?>>> fsLoaderThreadQueue	= new ConcurrentHashMap<String, ArrayList<Future<?>>>();
	protected ConcurrentHashMap<String, ArrayList<Future<?>>> httpLoaderThreadQueue	= new ConcurrentHashMap<String, ArrayList<Future<?>>>();
	
	protected boolean 						isOfflineMode				= false;
	
	/**
	 * Constructor TileProvider
	 * 
	 * @param context
	 * @param mapViewUpdateListener
	 */
	public TileProvider(final Context context) {
		
		this.loadingTile = BitmapFactory.decodeResource(context.getResources(), R.drawable.loading);		
		this.transparentTile = BitmapFactory.decodeResource(context.getResources(), R.drawable.transparent);
		this.missingTile = BitmapFactory.decodeResource(context.getResources(), R.drawable.missing);
		
		this.context = context;
		this.tileCache = new TileCache();				
		//this.fileSystemLoader = new TileFileSystemLoader(context, DISK_CACHE_SIZE, this.tileCache, this.events);
		//this.fileSystemLoader = new TileFileSystemFutureLoader(context, DISK_CACHE_SIZE, this.tileCache, this.events);
		this.fileSystemLoader = new TileSDCardFutureLoader(context, DISK_CACHE_SIZE, this.tileCache, this.events);
		//this.fileSystemLoader = new TileZippedSDCardFutureLoader(context, DISK_CACHE_SIZE, this.tileCache, this.events);
		// TODO: if it is TileZippedSDCardFutureLoader, always set isOfflineMode to true
		//this.httpLoader = new TileHttpLoader(context, this.fileSystemLoader, this.events);
		this.httpLoader = new TileHttpFutureLoader(context, this.fileSystemLoader, this.events);
		
		//this.pendingQueue = new LinkedList<String>();
		//this.pendingMatrix = new LinkedHashMap<Layer, LinkedHashMap<String, String>>();
	}
	
	/**
	 * getTile() for getting tiles in synchronize mode 
	 * 
	 * @param url
	 * @param loadingTile
	 * @param tile
	 * @return
	 */
	/*
	public Bitmap getTile(final String url, Bitmap loadingTile, Tile tile) {
		// return static tile image
		if(url.equalsIgnoreCase("R.drawable.transparent") == true) {
			return this.transparentTile;
		} else if(url.equalsIgnoreCase("R.drawable.missing") == true) {
			return this.missingTile;				
		}		
		Bitmap bitmap = null;		
		try {				
			bitmap = this.fileSystemLoader.loadTileToMemorySync(url);
		} catch(Exception e) {
			//Util.printDebugMessage("...tile " + url + " not found on disk...try loading from http....");
			//Util.printErrorMessage("...error(" + e.getClass().getSimpleName() + ") loading tile from disk...", e);
			this.events.triggerEvent(TileEvents.FS_LOAD_FAILURE, new Event(TileEvents.FS_LOAD_FAILURE, null));
			bitmap = loadingTile;							
			this.httpLoader.getTileAsync(url, tile);			
		}					
		return bitmap;
	}	
	*/
	
	/**
	 * API Method: getTile
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getTile(final String url, Tile tile) {
		return getTile(url, this.loadingTile, tile);
	}
	
	/**
	 * getTile
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getTile(final String url, final String signature, Tile tile) {
		return getTile(url, signature, this.loadingTile, tile);
	}
	
	/**
	 * getTile() for getting tiles in asynchronize mode
	 * 
	 * @param url
	 * @param loadingTile
	 * @return bitmap
	 */	
	public Bitmap getTile(final String url, Bitmap loadingTile, Tile tile) {
		return getTile(url, null, this.loadingTile, tile);
	}
	
	/**
	 * 
	 * @param url
	 * @param signature
	 * @param loadingTile
	 * @param tile
	 * @return
	 */
	public Bitmap getTile(final String url, final String signature, Bitmap loadingTile, Tile tile) {
		// return static tile image
		if(url.equalsIgnoreCase("R.drawable.transparent") == true) {
			return this.transparentTile;
		} else if(url.equalsIgnoreCase("R.drawable.missing") == true) {
			return this.missingTile;				
		}		
		Bitmap bitmap = this.tileCache.getTile(url);		
		if(bitmap != null && bitmap.isRecycled()==false){			
			//Util.printDebugMessage("...tile found in cache: " + url + "...");			
			//this.events.triggerEvent(TileEvents.MEM_LOAD_SUCCESS, new Event(TileEvents.MEM_LOAD_SUCCESS, url));
		} else{
			//Util.printDebugMessage("...tile " + url + " not found in cache...try loading from disk....");			
			//this.events.triggerEvent(TileEvents.MEM_LOAD_FAILURE, new Event(TileEvents.MEM_LOAD_FAILURE, url));
			try {						
				// TileFileSystemFutureLoader
				this.fileSystemLoader.loadTileToMemoryAsync(url, signature, tile);
				
				// TileFileSystemLoader								
				//this.fileSystemLoader.loadTileToMemoryAsync(url, tile);
				
				// apply thread canceling for FS loading
				// there seems no need to to thread canceling for FS loading cuz it's too fast 
				// uncomment out this to enable FS loading thread canceling
				/*
				Future<?> future = this.fileSystemLoader.loadTileToMemoryAsync(url, signature, tile);
				if(future != null) {
					if(signature!=null && "".equalsIgnoreCase(signature)==false) {					
						if(this.fsLoaderThreadQueue.get(signature) == null) {
							ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
							this.fsLoaderThreadQueue.put(signature, futures);				
						} 
						this.fsLoaderThreadQueue.get(signature).add(future);	
					}
				}
				*/				
				bitmap = loadingTile;											
				
			} catch(FileNotFoundException e) {				
				if(this.isOfflineMode == false) {
					//Util.printDebugMessage("...tile " + url + " not found on disk...try loading from http....");				
					//this.events.triggerEvent(TileEvents.FS_LOAD_FAILURE, new Event(TileEvents.FS_LOAD_FAILURE, null));
					bitmap = loadingTile;							
					Future<?> future = this.httpLoader.getTileAsync(url, signature, tile);
					if(future != null) {
						if(signature!=null && "".equalsIgnoreCase(signature)==false) {					
							if(this.httpLoaderThreadQueue.get(signature) == null) {
								ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
								this.httpLoaderThreadQueue.put(signature, futures);				
							} 
							this.httpLoaderThreadQueue.get(signature).add(future);	
						}
					}
				} else {
					// offline mode, no need to load tile from internet just return missing tile
					if((this.layer.isBaseLayer()==true) && (this.layer.isVisible()==true)) {
						bitmap = missingTile;
					} else {
						bitmap = transparentTile;
					}						
				}
			} catch(TileFileCorruptedException e) {	
				if(this.isOfflineMode == false) {
					Util.printDebugMessage("...tile " + url + " corrupted on disk...try loading from http....");				
					//this.events.triggerEvent(TileEvents.FS_LOAD_FAILURE, new Event(TileEvents.FS_LOAD_FAILURE, null));
					bitmap = loadingTile;							
					Future<?> future = this.httpLoader.getTileAsync(url, signature, tile);
					if(future != null) {
						if(signature!=null && "".equalsIgnoreCase(signature)==false) {					
							if(this.httpLoaderThreadQueue.get(signature) == null) {
								ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
								this.httpLoaderThreadQueue.put(signature, futures);				
							} 
							this.httpLoaderThreadQueue.get(signature).add(future);	
						}
					}
				} else {
					// offline mode, no need to load tile from internet just return missing tile
					// offline mode, no need to load tile from internet just return missing tile
					if((this.layer.isBaseLayer()==true) && (this.layer.isVisible()==true)) {
						bitmap = missingTile;
					} else {
						bitmap = transparentTile;
					}
				}
			} catch(Exception e) {	
				Util.printDebugMessage("...tile can not be loaded for other reason...use transparent tile...");
				if((this.layer.isBaseLayer()==true) && (this.layer.isVisible()==true)) {
					bitmap = missingTile;
				} else {
					bitmap = transparentTile;
				}
				Util.printErrorMessage(e.getMessage());
			}			
		}
		return bitmap;
	}
	
	/**
	 * API Method: getLoadingTile
	 */
	public Bitmap getLoadingTile() {
		return this.loadingTile;
	}
	
	/**
	 * API Method: getTransparentTile
	 */
	public Bitmap getTransparentTile() {
		return this.transparentTile;
	}
	
	/**
	 * API Method: setLoadingTile
	 */
	public void setLoadingTile(Bitmap newLoadingTile) {
		if(newLoadingTile != null) {
			this.loadingTile = newLoadingTile;
		}
	}
	
	/**
	 * API Method: setMissingTile
	 */
	public void setMissingTile(Bitmap newMissingTile) {
		if(newMissingTile != null) {
			this.missingTile = newMissingTile;
		}
	}
	
	/**
	 * API Method: setTransparentTile
	 */
	public void setTransparentTile(Bitmap newTransparentTile) {
		if(newTransparentTile != null) {
			this.transparentTile = newTransparentTile;
		}
	}
	
	public void cleanupFSLoaderThreadQueue() {
		cleanupThreadQueue(null, this.fsLoaderThreadQueue);
	}
	
	/**
	 * 
	 */
	public void cleanupFSLoaderThreadQueue(String signature) {
		// uncomment out this to enable FS loading thread canceling
		//cleanupThreadQueue(signature, this.fsLoaderThreadQueue);
	}
	
	/**
	 * 
	 */
	public void cleanupHTTPLoaderThreadQueue(String signature) {
		cleanupThreadQueue(signature, this.httpLoaderThreadQueue);
	}
	
	/**
	 * cleanupThreadQueue(String signature, ConcurrentHashMap<String, ArrayList<Future<?>>> queue)
	 */
	private void cleanupThreadQueue(String signature, ConcurrentHashMap<String, ArrayList<Future<?>>> queue) {
		Iterator<String> iterator = queue.keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String)iterator.next();
			if(key!=null && key.equalsIgnoreCase(signature)==false) {
				ArrayList<Future<?>> threads = queue.get(key);
				if(threads != null) {				
					for(int i=0; i<threads.size(); i++) {					
						if(threads.get(i) != null) {
							if(threads.get(i).isDone() == false) {
								//Util.printDebugMessage("...cancel thread with signature: " + key);
								if(threads.get(i).cancel(true)) {
									// TODO: when canceled, must remove the url from pendingQueue
									//Util.printDebugMessage("...successfully canceled thread with signature: " + key);
								} else {
									//Util.printDebugMessage("...fail to cancel thread with signature: " + key);
								}
							} else {
								//Util.printDebugMessage("...thread is finished with signature: " + key);
							}
						}					
					}
				}
				iterator.remove();
			}
		}
	}
	
	/**
	 * API Method: addToPendingQueue
	 * @param url
	 */
	/*
	public synchronized void addToPendingQueue(String url) {
		//Util.printDebugMessage(" ...try to add tile " + url + "...");
		if(this.pendingQueue.contains(url) == false) {
			//Util.printDebugMessage(" ...add tile " + url + "...");
			this.pendingQueue.add(url);
		}
	}
	*/
	
	/**
	 * API Method: addToPendingMatrix
	 * @param url
	 */
	/*
	public synchronized void addToPendingMatrix(Tile tile) {
		if(this.pendingMatrix.containsKey(tile.getLayer()) == false) {
			this.pendingMatrix.put(tile.getLayer(), new LinkedHashMap<String, String>());					
			// TODO: this does not indicate if loading of a layer is just started, find something else			
		}		
		if(this.pendingMatrix.get(tile.getLayer()).containsKey(tile.getUrl()) == false) {				
			this.pendingMatrix.get(tile.getLayer()).put(tile.getUrl(), tile.getUrl());
		}
	}
	*/
	
	/**
	 * API Method: removeFromPendingQueue
	 * @param url
	 */
	/*
	public synchronized void removeFromPendingQueue(String url) {
		//Util.printDebugMessage(" ...try to remove tile " + url + "...");
		if(this.pendingQueue.contains(url) == true) {			
			this.pendingQueue.remove(url);
			//Util.printDebugMessage(" ...tile " + url + " removed...");
		}		
	}
	*/
	
	/**
	 * API Method: removeFromPendingMatrix
	 * @param tile
	 */
	/*
	public synchronized void removeFromPendingMatrix(Tile tile) {
		if(this.pendingMatrix.containsKey(tile.getLayer()) == true) {
			if(this.pendingMatrix.get(tile.getLayer()).containsKey(tile.getUrl()) == true) {
				this.pendingMatrix.get(tile.getLayer()).remove(tile.getUrl());
				//tile.getLayer().events.triggerEvent(LayerEvents.TILE_LOADED, new Event());
			}	
			if(this.pendingMatrix.get(tile.getLayer()).size() == 0) {					
				this.pendingMatrix.remove(tile.getLayer());
				// TODO: this does not indicate if loading of a layer is finished, find something else								
			}
		}
	}
	*/
	
	/**
	 * API Method: isStillLoading
	 * 
	 * @return
	 * check whether tile provider is still loading some time from disk or Internet 
	 */
	public boolean isStillLoading() {				
		if(this.httpLoader.pendingQueue.size() == 0 && this.fileSystemLoader.pendingQueue.size() == 0 ) {						
			return false;
		}
		return true;
	}
	
	public boolean isFSStillLoading() {				
		if(this.fileSystemLoader.pendingQueue.size() == 0 ) {						
			return false;
		}
		return true;
	}
	
	public boolean isHTTPStillLoading() {				
		if(this.httpLoader.pendingQueue.size() == 0 ) {						
			return false;
		}
		return true;
	}
	
	// ==================================================================================================
	// getters & setters
	// ==================================================================================================

	public boolean isOfflineMode() {
		return isOfflineMode;
	}

	public void setOfflineMode(boolean isOfflineMode) {
		this.isOfflineMode = isOfflineMode;
	}

	public Layer getLayer() {
		return layer;
	}

	public void setLayer(Layer layer) {
		this.layer = layer;
		// pass layer name or id to TileProvider as root output folder on SD card
		if(this.layer.getName()!=null && "".equalsIgnoreCase(this.layer.getName())==false) {
			this.fileSystemLoader.setRootOutputPath(this.layer.getName().replace(' ', '_'));			
		}		
	}
		
	// ==================================================================================================
	// private class
	// ==================================================================================================	
	/*
	private class TileLoaderCallable implements Callable<Bitmap> {

		protected String url = "";
		protected String signature = "";
		protected Tile tile = null;
		
		public TileLoaderCallable(String url, String signature, Tile tile) {
			super();
			this.url = url;
			this.signature = signature;
			this.tile = tile;
		}
		
		@Override
		public Bitmap call() throws TileFileCorruptedException {
			//Util.printDebugMessage("@...started a TileLoaderCallable thread...url: " + this.url);
			Bitmap bitmap = null;
			try {
				//Thread.sleep(1000);
				
				Future<Bitmap> future = TileProvider.this.fileSystemLoader.loadTileToMemoryAsync2(url, tile); 				
				if(TileProvider.this.fsLoaderThreadQueue.get(this.signature) == null) {
					ArrayList<Future<Bitmap>> futures = new ArrayList<Future<Bitmap>>();
					TileProvider.this.fsLoaderThreadQueue.put(this.signature, futures);				
				} 
				TileProvider.this.fsLoaderThreadQueue.get(this.signature).add(future);				
				bitmap = future.get();
				
			} catch(TileFileCorruptedException e) {
				throw e;
			} catch(ExecutionException e) {
				throw new TileFileCorruptedException("");
			} catch(InterruptedException e) {
				throw new TileFileCorruptedException("");
			}			
			if(bitmap!=null && bitmap.isRecycled()==false) {				
				return bitmap;
			} else {
				throw new TileFileCorruptedException("");
			}				
			//Util.printDebugMessage("@...finished a TileLoaderCallable thread...url: " + this.url);
		}		
	}
	*/
}
