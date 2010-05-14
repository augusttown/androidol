package com.androidol.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.androidol.Map;
import com.androidol.R;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.layer.Layer;
import com.androidol.layer.osm.Mapnik;
import com.androidol.test.io.TestAndoridSDCardIO;
import com.androidol.test.proj4j.TestProjection;
import com.androidol.util.Util;
import com.androidol.util.tiles.StreamUtils;
import com.androidol.util.tiles.TileHttpLoader;
import com.vividsolutions.jts.geom.Coordinate;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;


public class TestActivity extends Activity {    

	protected static final int 		THREAD_POOL_SIZE = 8;
	protected static final int 		TEST_TILES_NUM = 8;
	protected ExecutorService 		threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	protected ExecutorService 		threadPool2 = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	protected ThreadGroup 			threadGroup = new ThreadGroup("thread_group");
	
	protected int 					zoom = 0;
	protected double 				centerX = 0.0;
	protected double 				centerY = 0.0;
	
	protected Context 				context = null;
	
	protected ConcurrentHashMap<String, ArrayList<Future<Bitmap>>> queue = new ConcurrentHashMap<String, ArrayList<Future<Bitmap>>>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		this.context = findViewById(R.id.layout).getContext();
		
		// testing thread canceling
		/*
		ZoomControls zoomControls = (ZoomControls)findViewById(R.id.zoomcontrols);
		zoomControls.setZoomSpeed(340); // half second
		zoomControls.setOnZoomInClickListener(new OnClickListener() {
			public void onClick(View view) {
				zoomIn();
			}
	    });
		zoomControls.setOnZoomOutClickListener(new OnClickListener() {
			public void onClick(View view) {
				zoomOut();
			}
	    });
	    */
		
		//testThreading();
		
		/*
		TestAndoridSDCardIO test = new TestAndoridSDCardIO();
		test.testEnvironment();
		//for(int i=0; i<100; i++) {
			//test.testReadFileFromSDCard();
			test.testWriteFileToSDCard();
			//test.testReadFileFromContext(this.context);
		//}
		*/
		
		TestProjection.testProjection();
	}
	
	/*
	private void zoomIn() {
		//Util.printDebugMessage("...zoomin...");		
		this.zoom = this.zoom + 1;
		String signature = "signature_" + String.valueOf(this.zoom);		
		// 
		
		Iterator<String> iterator = this.queue.keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String)iterator.next();
			ArrayList<Future<Bitmap>> threads = this.queue.get(key);
			if(threads != null) {
				Util.printDebugMessage("...should cancel how many threads? " + threads.size());
				for(int i=0; i<threads.size(); i++) {					
					if(threads.get(i) != null) {
						if(threads.get(i).isDone() == false) {
							Util.printDebugMessage("...##cancel thread with signature: " + key);
							if(threads.get(i).cancel(true)) {
								Util.printDebugMessage("@@...successfully cancelled thread with signature: " + key);
							} else {
								Util.printDebugMessage("...fail to cancel thread with signature: " + key);
							}
						} else {
							Util.printDebugMessage("...cuz it's done can not cancel thread with signature: " + key);
						}
					}
					//threads.remove(i);
				}
			}
			iterator.remove();
		}
		
		//ArrayList<Future<Bitmap>> futures = new ArrayList<Future<Bitmap>>();
		for(int i=0, j=79; i<TEST_TILES_NUM; i++, j++) {
			this.threadPool2.submit(
				new TileDrawerRunnable("http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/8/70/"+j, signature)
			);			
		}
		//this.queue.put(signature, futures);
	}
	
	private void zoomOut() {
		Util.printDebugMessage("...zoomout...");
	}
	
	private class TileDrawerRunnable implements Runnable {
		
		protected String url = "";
		protected String signature = "";
		
		public TileDrawerRunnable(String url, String signature) {
			super();
			this.url = url;
			this.signature = signature;
		}		
		@Override
		public void run() {
			TileLoaderCallable tileLoader = new TileLoaderCallable(url, signature);
			Future<Bitmap> future = TestActivity.this.threadPool.submit(tileLoader);
			if(TestActivity.this.queue.get(this.signature) == null) {
				ArrayList<Future<Bitmap>> futures = new ArrayList<Future<Bitmap>>();
				TestActivity.this.queue.put(this.signature, futures);				
			} 
			TestActivity.this.queue.get(this.signature).add(future);
			//Util.printDebugMessage("...add to  queue...signaturee: " + this.signature + "...tile: " + this.url.subSequence(this.url.length()-2, this.url.length()));
			try { 
				Bitmap bitmap = (Bitmap)future.get();
				//Util.printDebugMessage("...get back result...signaturee: " + this.signature + "...tile: " + this.url.subSequence(this.url.length()-2, this.url.length()));
				if(bitmap!=null && bitmap.isRecycled()==false) {
					Util.printDebugMessage("...ready to draw...signaturee: " + this.signature + "...tile: " + this.url.subSequence(this.url.length()-2, this.url.length()));
				}
			} catch(ExecutionException e) {
				Util.printDebugMessage(e.getMessage());
			} catch(InterruptedException e) {
				Util.printDebugMessage(e.getMessage());
			}
		}
	}
	
	private class TileLoaderCallable implements Callable<Bitmap> {
		protected String url = "";		
		protected String signature = "";		
		
		public TileLoaderCallable(String url, String signature) {
			super();
			this.url = url;
			this.signature = signature;
		}
		
		@Override 
		public Bitmap call() {
			InputStream in = null;
			OutputStream out = null;
			Bitmap bitmap = null;
			try {			
				Util.printDebugMessage("...start loading...signature: " + TileLoaderCallable.this.signature + "...tile: " + TileLoaderCallable.this.url.subSequence(TileLoaderCallable.this.url.length()-2, TileLoaderCallable.this.url.length()) + "...");
				Thread.sleep(5000);
				in = new BufferedInputStream(new URL(url).openStream(), 8192);
				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, 8192);
				StreamUtils.copy(in, out);
				out.flush();
				byte[] data = dataStream.toByteArray();						        						 					
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				if(bitmap!=null && bitmap.isRecycled()==false) {
					bitmap.recycle();
				}
				Util.printDebugMessage("...finished loading signature: " + TileLoaderCallable.this.signature + "...tile: " + TileLoaderCallable.this.url.subSequence(TileLoaderCallable.this.url.length()-2, TileLoaderCallable.this.url.length()) + "...");
			} catch(IOException e) {
				Util.printDebugMessage(e.getMessage());
			} catch(InterruptedException e) {
				Util.printDebugMessage(e.getMessage());
			} finally {
				Util.printDebugMessage("...do finally " + TileLoaderCallable.this.signature + "...tile: " + TileLoaderCallable.this.url.subSequence(TileLoaderCallable.this.url.length()-2, TileLoaderCallable.this.url.length()) + "...");
				
			}
			//Util.printDebugMessage("...before quiting " + TileLoaderCallable.this.signature + "...tile: " + TileLoaderCallable.this.url.subSequence(TileLoaderCallable.this.url.length()-2, TileLoaderCallable.this.url.length()) + "...");
			return bitmap;
		}
	}
	*/
	
	// ==================================================================================================
	// test code for threading
	// ==================================================================================================
	/*
	private void testThreading() {
		for(int i=0; i<THREAD_POOL_SIZE; i++) {
			
			// thread group and setUncaughtExceptionHandler
			// NOTE: thread doesn't count in group when thread passed into ExecutorService
			// NOTE: thread name doesn't apply in group when thread passed into ExecutorService
			Thread thread = new TestThread(this.threadGroup, "test-thread-"+i); //
			// NOTE: setUncaughtExceptionHandler() doesn't take effect when thread passed into ExecutorService
			thread.setUncaughtExceptionHandler(
				new UncaughtExceptionHandler() {					
					@Override
					public void uncaughtException(Thread thread, Throwable ex) {
						// TODO Auto-generated method stub
						Util.printDebugMessage("...runtime exception caught...");
					}
				}
			);
			// Or call static setDefaultUncaughtExceptionHandler() on Thread
			Thread.setDefaultUncaughtExceptionHandler(
				new UncaughtExceptionHandler() {					
					@Override
					public void uncaughtException(Thread thread, Throwable ex) {
						// TODO Auto-generated method stub
						Util.printDebugMessage("...runtime exception caught...");
					}
				}
			); 
			thread.start();
						
			
			
			this.threadPool.execute(new TestThread());			
			
			// anonymous class implements Runnable
			
			this.threadPool.execute(
				new Runnable() {
					@Override
					public void run() {
						Util.printDebugMessage("...started a thread for fs loading...");
						try {	
							Util.printDebugMessage("...is thread interrupted " + Thread.currentThread().isInterrupted() + "...");
							// interrupt() only set the interrupted flag to true, not forcibly terminating a thread
							Thread.currentThread().interrupt();					
							Util.printDebugMessage("...is thread interrupted after interrupt() call..." + Thread.currentThread().isInterrupted() + "...");
							if(Thread.currentThread().isInterrupted()) { 
								Util.printDebugMessage("...you can choose to terminate the thread here...");
							}
							Thread.interrupted(); // Thread.interrupted() reset interrupted flag to false again 
							Util.printDebugMessage("...is thread interrupted after Thread.interrupted() call..." + Thread.currentThread().isInterrupted() + "...");
							//
						} catch(InterruptedException e) {
							
						} catch(Exception e) {        								
							//						        								
						} finally {
							//
							Util.printDebugMessage("...finished a thread for fs loading...");
						}
					}
				}
			);
			
		}
	}

	private class TestThread extends Thread {
		
		public TestThread() {
			super();
		}
		
		public TestThread(ThreadGroup group, String threadName) {
			super(group, threadName);
		}
		
		@Override
		public void run() {
			Util.printDebugMessage("...started a thread: " + Thread.currentThread().getName() + "...");
			Util.printDebugMessage("...active threads in thread group: " + TestActivity.this.threadGroup.activeCount() + "...");
			Coordinate coord = null;
			coord.distance(coord);
			try {					
				//Util.printDebugMessage("...is thread interrupted " + Thread.currentThread().isInterrupted() + "...");
				// interrupt() only set the interrupted flag to true, not forcibly terminating a thread
				Thread.currentThread().interrupt();					
				//Util.printDebugMessage("...is thread interrupted after interrupt() call..." + Thread.currentThread().isInterrupted() + "...");
				if(Thread.currentThread().isInterrupted()) { 
					//Util.printDebugMessage("...you can choose to terminate the thread here...");
				}
				Thread.interrupted(); // Thread.interrupted() reset interrupted flag to false again 
				//Util.printDebugMessage("...is thread interrupted after Thread.interrupted() call..." + Thread.currentThread().isInterrupted() + "...");
				//
			} catch(InterruptedException e) {
				Util.printDebugMessage("...terminate a thread: " + Thread.currentThread().getName() + "...");
				return;
			} catch(Exception e) {        								
				//						        								
			} finally {
				//
				Util.printDebugMessage("...ended a thread: " + Thread.currentThread().getName() + "...");
			}
		}
	}
	*/
	// ==================================================================================================
	// Test the memory limit of Android application loading bitmap using BitmapFactory.decodeByteArray
	// ==================================================================================================
	/*
	
	protected Bitmap[] cache = new Bitmap[81];
	protected long totalSize = 0;	
	
	private void testLoadBitmapsSingleThread() {
		for(int i=79; i<145; i++) {
        	InputStream in = null;
			OutputStream out = null;
			final int idx = i;
			try {						    																
				String url = "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_Imagery_World_2D/MapServer/tile/8/70/" + idx + ".jpg";
				Util.printDebugMessage("...load tile from url: tile/8/70/" + idx + "...");
				in = new BufferedInputStream(new URL(url).openStream(), 8192);
				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, 8192);
				StreamUtils.copy(in, out);
				out.flush();
				byte[] data = dataStream.toByteArray();						        						 					
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				//TestActivity.this.cache[idx-79] = bitmap;
				TestActivity.this.cache[idx-79] = Bitmap.createBitmap(bitmap);
				bitmap.recycle();
				Util.printDebugMessage(" ...bytes decoded: " + data.length/1024 + " kb");
				TestActivity.this.totalSize = TestActivity.this.totalSize + data.length/1024;
				Util.printDebugMessage(" ...bytes decoded: " + TestActivity.this.totalSize + " kb");
			} catch(Exception e) {        								
				Util.printErrorMessage("...error loading tile...exception: " + e.getClass().getSimpleName() + "...", e);						        								
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
			}
        }
	}
	*/
	/*
	private void testLoadBitmapsMultiThreads() {
		for(int i=79; i<145; i++) {
	    	final int idx = i;
	    	this.threadPool.execute(
				new Runnable() {
					@Override
					public void run() {
						InputStream in = null;
						OutputStream out = null;
						try {						    																
							String url = "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_Imagery_World_2D/MapServer/tile/8/70/" + idx + ".jpg";
							Util.printDebugMessage("...load tile from url: tile/8/70/" + idx + "...");
							in = new BufferedInputStream(new URL(url).openStream(), 8192);
							final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
							out = new BufferedOutputStream(dataStream, 8192);
							StreamUtils.copy(in, out);
							out.flush();
							byte[] data = dataStream.toByteArray();						        						 					
							Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);     						
							TestActivity.this.cache[idx-79] = Bitmap.createBitmap(bitmap);
							bitmap.recycle();
							Util.printDebugMessage(" ...bytes decoded: " + data.length/1024 + " kb");
							TestActivity.this.totalSize = TestActivity.this.totalSize + data.length/1024;
							Util.printDebugMessage(" ...bytes decoded: " + TestActivity.this.totalSize + " kb");
						} catch(Exception e) {        								
							Util.printErrorMessage("...error loading tile...exception: " + e.getClass().getSimpleName() + "...", e);						        								
					} finally {
						StreamUtils.closeStream(in);
						StreamUtils.closeStream(out);
					}
				}
			});
	    }	
	}
	*/
}

