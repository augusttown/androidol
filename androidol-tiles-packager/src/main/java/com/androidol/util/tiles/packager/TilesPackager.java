package com.androidol.util.tiles.packager;

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
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.androidol.basetypes.Size;
import com.androidol.map.schema.ArcGISOnlineTileMapSchema;
import com.androidol.map.schema.MapSchema;
import com.androidol.map.schema.OSMTileMapSchema;
import com.androidol.map.schema.TileSchema;
import com.androidol.util.tiles.StreamUtils;
import com.androidol.util.tiles.packager.schema.PackageSchema;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class TilesPackager {
	
	private static final int		THREAD_NUM = 8;
	
	protected MapSchema	 			mapSchema;
	protected TileSchema 			tileSchema;
	protected PackageSchema 		packageSchema;
	protected ExecutorService 		threadPool;
	
	protected TilesUrlBuilder		tilesUrlBuilder;
	protected TilesFilePathBuilder	tilesFilePathBuilder;

	protected final Queue<TileTicket> queue = new LinkedBlockingQueue<TileTicket>();
	
	protected String				tilesOutputFolder;
	
	/**
	 * 
	 */
	public TilesPackager(PackageSchema packageSchema) {
		OSMTileMapSchema schema = new OSMTileMapSchema();
		//ArcGISOnlineTileMapSchema schema = new ArcGISOnlineTileMapSchema(); 
		this.mapSchema = (MapSchema)schema;
		this.tileSchema = (TileSchema)schema;
		this.packageSchema = packageSchema;
		this.threadPool = Executors.newFixedThreadPool(THREAD_NUM);
		
		this.tilesOutputFolder = "packages" + File.separator + this.packageSchema.getName();
		
		
		this.tilesUrlBuilder = new TilesUrlBuilder();
		this.tilesFilePathBuilder = new TilesFilePathBuilder(this.tilesOutputFolder);	
		
	}
	
	/**
	 * 
	 * @param mapSchema
	 * @param tileSchema
	 * @param packageSchema
	 * @param threadNum
	 */
	public TilesPackager(MapSchema mapSchema, TileSchema tileSchema, PackageSchema packageSchema, int threadNum, TilesUrlBuilder tilesUrlBuilder, TilesFilePathBuilder tilesFilePathBuilder) {		
		this.mapSchema = mapSchema;
		this.tileSchema = tileSchema;
		this.packageSchema = packageSchema;
		this.threadPool = Executors.newFixedThreadPool(threadNum);
		
		this.tilesOutputFolder = "packages" + File.separator + this.packageSchema.getName();
		
		this.tilesUrlBuilder = tilesUrlBuilder;
		this.tilesFilePathBuilder = tilesFilePathBuilder;
		this.tilesFilePathBuilder.setRootPath(this.tilesOutputFolder);
	}
	
	/**
	 * 
	 * @param schema
	 */
	public int getTotalNumOfTiles() {
		int count = 0;
		//
		int minZoomLevel = this.packageSchema.getMinZoomLevel();
		int maxZoomLevel = this.packageSchema.getMaxZoomLevel();
		//
		Size tileSize = this.tileSchema.getDefaultTileSize();
		int buffer = this.packageSchema.getBuffer();
		final Coordinate tileOrigin = this.tileSchema.getTileOrigin();
		//
		for(int i=minZoomLevel; i<=maxZoomLevel; i++) {			
			//
			double resolution = this.mapSchema.getResolutions()[i];
			
			Envelope maxExtent = this.mapSchema.getDefaultMaxExtent();
			Envelope packageExtent = this.packageSchema.getExtent();
			
			double tilelon = resolution * tileSize.getWidth();
		    double tilelat = resolution * tileSize.getHeight();
		    
		    double offsetlon = packageExtent.getMinX() - maxExtent.getMinX();
		    int tilecol = (int)Math.floor(offsetlon/tilelon) - buffer;		    
		    double tileoffsetlon = maxExtent.getMinX() + tilecol * tilelon;
		    
		    double offsetlat = packageExtent.getMinY() - maxExtent.getMinY();  		    
		    int tilerow = (int)Math.floor(offsetlat/tilelat) - buffer;		    		    		   
		    double tileoffsetlat = maxExtent.getMinY() + tilerow * tilelat;
		    	
		    double startLon = tileoffsetlon;
		    double startLat = tileoffsetlat;
				   			   	
		   	tileoffsetlat = startLat;
		   	do {		    			    	
		    	tileoffsetlon = startLon;		    			        	        
		        do {		
		        	Envelope tileExtent = new Envelope(tileoffsetlon, tileoffsetlon+tilelon, tileoffsetlat, tileoffsetlat+tilelat);		        			        	
		        	String tileUrl = this.tilesUrlBuilder.createTileUrl(maxExtent, tileExtent, tileSize, tileOrigin, resolution, i);		        	
		        	if(tileUrl != null) {
			        	String tileFilePath = this.tilesFilePathBuilder.createTileFilePath(tileUrl);		        	
			        	File tileFile = new File(tileFilePath);
						if(tileFile.exists()==false || tileFile.length()<=0) {						
							count++;
						}		
		        	}
		            tileoffsetlon += tilelon;       		       
		        } while(tileoffsetlon <= (packageExtent.getMaxX()+tilelon*buffer));  		         
		        tileoffsetlat += tilelat;		        		        
		    } while(tileoffsetlat <= (packageExtent.getMaxY()+tilelat*buffer));
		}
		if(count > 0) {
			return count;
		} else {
			return 0;
		}
	}
	
	/**
	 * 
	 */
	public void loadPackageTiles() {		
		//
		int minZoomLevel = this.packageSchema.getMinZoomLevel();
		int maxZoomLevel = this.packageSchema.getMaxZoomLevel();
		//
		final Size tileSize = this.tileSchema.getDefaultTileSize();
		final int buffer = this.packageSchema.getBuffer();
		final Coordinate tileOrigin = this.tileSchema.getTileOrigin();
		//
		for(int i=minZoomLevel; i<=maxZoomLevel; i++) {			
			//
			double resolution = this.mapSchema.getResolutions()[i];
			
			final Envelope maxExtent = this.mapSchema.getDefaultMaxExtent();
			final Envelope packageExtent = this.packageSchema.getExtent();
			
			double tilelon = resolution * tileSize.getWidth();
		    double tilelat = resolution * tileSize.getHeight();
		    
		    double offsetlon = packageExtent.getMinX() - maxExtent.getMinX();
		    int tilecol = (int)Math.floor(offsetlon/tilelon) - buffer;		    
		    double tileoffsetlon = maxExtent.getMinX() + tilecol * tilelon;
		    
		    double offsetlat = packageExtent.getMinY() - maxExtent.getMinY();  		    
		    int tilerow = (int)Math.floor(offsetlat/tilelat) - buffer;		    		    		   
		    double tileoffsetlat = maxExtent.getMinY() + tilerow * tilelat;
		    	
		    double startLon = tileoffsetlon;
		    double startLat = tileoffsetlat;
				   			   
		   	tileoffsetlat = startLat;
		   	do {		    			    
		    	tileoffsetlon = startLon;		    			        	        
		        do {
		        	Envelope tileExtent = new Envelope(tileoffsetlon, tileoffsetlon+tilelon, tileoffsetlat, tileoffsetlat+tilelat);		        			        	
		        	String tileUrl = this.tilesUrlBuilder.createTileUrl(maxExtent, tileExtent, tileSize, tileOrigin, resolution, i);
		        	if(tileUrl != null) {
			        	String tileFilePath = this.tilesFilePathBuilder.createTileFilePath(tileUrl); 
			        	//System.out.println(tileUrl);
			        	//System.out.println(tileFilePath);
			        	File tileFile = new File(tileFilePath);
						if(tileFile.exists()==false || tileFile.length()<=0) {						
							add(new TileTicket(tileUrl, tileFilePath));
						}
		        	}
		            tileoffsetlon += tilelon;       		       
		        } while(tileoffsetlon <= (packageExtent.getMaxX()+tilelon*buffer));  		         
		        tileoffsetlat += tilelat;		        		        
		    } while(tileoffsetlat <= (packageExtent.getMaxY()+tilelat*buffer));
		   	try{	
				waitEmpty();
				System.out.println("loading finished for zoom level: " + i);
			} catch(InterruptedException e) {
				System.out.println("loading interrupted at zoom level: " + i);
				e.printStackTrace();
			}
		}	
		try{	
			waitFinished();
			System.out.println("loading finished for all zoom level");
		} catch(InterruptedException e) {
			System.out.println("loading interrupted");
			e.printStackTrace();
		}
		// TODO: write out package schema along with tiles
	}
	
	/**
	 * 
	 * @param tileExtent
	 */
	public synchronized void add(final TileTicket ticket){
		this.queue.add(ticket);
		spawnNewLoadingThread();
	}
	
	/**
	 * 
	 * @return
	 */
	private synchronized TileTicket getNext() {
		final TileTicket ticket = this.queue.poll();		
		
		final int remaining = this.queue.size();		
		if(remaining%50==0 && remaining>0) {
			System.out.print(".");
		}
		
		this.notify();
		return ticket;
	}
	
	/**
	 * 
	 * @param tileUrl
	 * @param tileFilePath
	 */
	private void spawnNewLoadingThread() {				
		this.threadPool.execute(new tileLoaderRunnable());
	}
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void waitEmpty() throws InterruptedException {
		while(this.queue.size() > 0){
			this.wait();
		}
	}
	
	/**
	 * 
	 * @throws InterruptedException
	 */
	public void waitFinished() throws InterruptedException {
		waitEmpty();
		this.threadPool.shutdown();
		this.threadPool.awaitTermination(6, TimeUnit.HOURS);
	}
	
	/**
	 * 
	 * @param schema
	 */
	public void zipPackageTiles() {
		try {
			String zipPackageFilePath = (new File(this.tilesOutputFolder)).getAbsolutePath() + ".zip";
			//System.out.println("create zip package: " + zipPackageFilePath);
			final ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPackageFilePath));
			addEntryToZip(new File(this.tilesOutputFolder), zipOut, this.tilesOutputFolder.substring(this.tilesOutputFolder.lastIndexOf(File.separator)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void compressPackageTiles() {
		// TODO:
	}
	
	/**
	 * 
	 */
	private void addEntryToZip(File folderPath, ZipOutputStream zipStream, String folderName) {
		final File[] files = folderPath.listFiles();
		for(File file : files) {
			if(file.isDirectory()) {	
				folderName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(File.separator));
				addEntryToZip(file, zipStream, folderName);
			} else {				
				final String name = file.getName();
				final ZipEntry zipEntry = new ZipEntry(folderName + File.separator + name);
				final FileInputStream in;
				try {
					zipStream.putNextEntry(zipEntry);
					//System.out.println("add zip entry: " + folderName + "/" + name);
					in = new FileInputStream(file);
					StreamUtils.copy(in, zipStream);
					StreamUtils.closeStream(in);
					zipStream.closeEntry();
				} catch(IOException e) {
					e.printStackTrace();
				} finally {
					//
				}
			}
		}
		try {
			zipStream.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * deletePackageTiles()
	 */
	public void deletePackageTiles() {
		deleteFolder(new File(this.tilesOutputFolder));
	}
	
	/**
	 * 
	 */
	public void countPackageTiles() {
		getFileCount(new File(this.tilesOutputFolder));
	}
	
	/**
	 * 
	 */
	private boolean deleteFolder(final File folderPath){
		final File[] children = folderPath.listFiles();		
		for(File c : children) {
			if(c.isDirectory()) {
				if(!deleteFolder(c)) {
					System.err.println("Could not delete " + c.getAbsolutePath());
					return false;
				}
			} else {
				if(!c.delete()) {
					System.err.println("Could not delete " + c.getAbsolutePath());
					return false;
				}
			}
		}
		return folderPath.delete();
	}
	
	/**
	 * 
	 * @param pFolder
	 * @return
	 */
	private int getFileCount(final File folderPath){
		final File[] children = folderPath.listFiles();
		int count = 0;
		for(File c : children){
			if(c.isDirectory()){
				count += getFileCount(c);
			}else{
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		//PackageSchema packageSchema = new PackageSchema("mapnik_baselayer.properties");
		//PackageSchema packageSchema = new PackageSchema("agstiled_imagery_baselayer.properties");
		String propertiesFileName = "package.properties";
		if(args.length > 0) {
			if("".equalsIgnoreCase(args[0]) == false) {
				propertiesFileName = args[0];
			}			
		}
		PackageSchema packageSchema = new PackageSchema(propertiesFileName);
		TilesPackager tilesPackager = new TilesPackager(packageSchema);		
		packageSchema.printOutPackageInfo();		
		System.out.println("tiles need to be loaded: " + tilesPackager.getTotalNumOfTiles());
		tilesPackager.loadPackageTiles();
		//tilesPackager.countPackageTiles();
		//System.out.println("tiles need to be deleted: " + tilesPackager.getTotalNumOfTiles());
		//tilesPackager.deletePackageTiles();
		//tilesPackager.zipPackageTiles();
	}
	
	// ===========================================================
	// private class
	// ===========================================================
	private class tileLoaderRunnable implements Runnable {

		protected String tileUrl;
		protected String tileFilePath;
		
		public tileLoaderRunnable() {}
		
		public void init(TileTicket ticket) {
			this.tileUrl = ticket.getTileUrl();
			this.tileFilePath = ticket.getTileFilePath();
		}
		
		@Override
		public void run() {			
			//
			init(TilesPackager.this.getNext());
			//
			InputStream in = null;
			OutputStream out = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				in = new BufferedInputStream(new URL(this.tileUrl).openStream(), StreamUtils.IO_BUFFER_SIZE);
				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();
				byte[] data = dataStream.toByteArray();
												
				File tileFile = new File(this.tileFilePath);
				if(tileFile.exists()==true && tileFile.length()>0) {
					// skip loading cuz it already exists
					StreamUtils.closeStream(in);
					StreamUtils.closeStream(out);
					return;
				}
				if(tileFile.getParentFile().exists() == false) {
					tileFile.getParentFile().mkdirs();
				}
				fos = new FileOutputStream(tileFile);					
				bos = new BufferedOutputStream(fos, StreamUtils.IO_BUFFER_SIZE);		
				bos.write(data);					
				bos.flush();		   									
			} catch(IOException e) {
				//System.out.println("...fail to load tile or write tile to disk...");
				//System.out.println("...replace with a transparent tile...");
				// TODO:
				//e.printStackTrace();				
			} catch(Exception e) {
				//System.out.println("...fail to load tile for other reason...");
				//System.out.println("...replace with a transparent tile...");
				// TODO:
				//e.printStackTrace();				
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
				StreamUtils.closeStream(bos);
				StreamUtils.closeStream(fos);		   									
			}			
		}				
	}
	
	
}
