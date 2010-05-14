package com.androidol.test.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.androidol.util.Util;
import com.androidol.util.tiles.StreamUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class TestAndoridSDCardIO {
	
	public TestAndoridSDCardIO() {
		
	}
	
	public void testEnvironment() {		
		
		File rootDirectory = Environment.getRootDirectory();
		File dataDirectory = Environment.getDataDirectory();
		File externalStorageDirectory = Environment.getExternalStorageDirectory();
		String externalStorageState = Environment.getExternalStorageState();
				
		Util.printDebugMessage("root directory: " + rootDirectory.getAbsolutePath());
		Util.printDebugMessage("data directory: " + dataDirectory.getAbsolutePath());
		Util.printDebugMessage("external storage directory: " + externalStorageDirectory.getAbsolutePath());
		Util.printDebugMessage("external storage status: " + externalStorageState);
		
	}
	
	public void testReadFileFromContext(Context context) {
		InputStream in = null;
		OutputStream out = null;
		Bitmap bitmap = null;
		long start, end;
		start = System.currentTimeMillis();
		try { 			
            FileInputStream fileInputStream = context.openFileInput("a.tah.openstreetmap.org_Tiles_tile_3_1_2.png");
            in = new BufferedInputStream(fileInputStream, StreamUtils.IO_BUFFER_SIZE);              
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();
			
			final byte[] data = dataStream.toByteArray();	
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			
        } catch (Exception e) {
            Util.printDebugMessage(e.getMessage());
        } finally {
        	StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
			if(bitmap!=null && bitmap.isRecycled()==false) {
				end = System.currentTimeMillis();				
				Util.printDebugMessage("...finish loading file from context...takes " + (end-start) + " ms...");
			}
			bitmap.recycle();
			bitmap = null;
        }	
	}
	
	public void testReadFileFromSDCard() {
		InputStream in = null;
		OutputStream out = null;
		Bitmap bitmap = null;
		long start, end;
		start = System.currentTimeMillis();
		try { 			
            FileInputStream fileInputStream = new FileInputStream(new File("/sdcard/a.tah.openstreetmap.org_Tiles_tile_3_1_2.png"));
            in = new BufferedInputStream(fileInputStream, StreamUtils.IO_BUFFER_SIZE);              
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
			StreamUtils.copy(in, out);
			out.flush();
			
			final byte[] data = dataStream.toByteArray();	
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			
        } catch (Exception e) {
        	Util.printDebugMessage(e.getMessage());
        } finally {
        	StreamUtils.closeStream(in);
			StreamUtils.closeStream(out);
			if(bitmap!=null && bitmap.isRecycled()==false) {
				end = System.currentTimeMillis();				
				Util.printDebugMessage("...finish loading file from SD card...takes " + (end-start) + " ms...");
			}
			bitmap.recycle();
			bitmap = null;
        }	
	}
	
	public void testDeleteFileFromSDCard() {
		File fileFolder = new File("/sdcard/androidol/cache/");
		if(fileFolder.exists() == true) {
			fileFolder.delete();
		}
	}
	
	public void testWriteFileToSDCard() {
		for(int i=79; i<81; i++) {
        	InputStream in = null;
			OutputStream out = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			
			final int idx = i;
			try {						    																
				String url = "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_Imagery_World_2D/MapServer/tile/8/70/" + idx + ".jpg";
				//Util.printDebugMessage("...load tile from url: tile/8/70/" + idx + "...");
				in = new BufferedInputStream(new URL(url).openStream(), StreamUtils.IO_BUFFER_SIZE);
				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE);
				StreamUtils.copy(in, out);
				out.flush();
				byte[] data = dataStream.toByteArray();						        						 					
				
				File directory = new File("/sdcard/androidol/tiles/");
				if(directory.exists() == false) {
					directory.mkdirs();
				}
				fos = new FileOutputStream(new File("/sdcard/androidol/tiles/tile_" + idx + ".jpg"));							
				bos = new BufferedOutputStream(fos, StreamUtils.IO_BUFFER_SIZE);
				bos.write(data);					
				bos.flush();
				bos.close();
				
			} catch(Exception e) {        								
				Util.printErrorMessage(e.getClass().getName() + "..." + e.getMessage());						        								
			} finally {
				StreamUtils.closeStream(in);
				StreamUtils.closeStream(out);
				StreamUtils.closeStream(fos);
				StreamUtils.closeStream(bos);
			}
        }
	}
}
