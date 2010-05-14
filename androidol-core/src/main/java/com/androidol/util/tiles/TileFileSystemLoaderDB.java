package com.androidol.util.tiles;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.androidol.exceptions.EmptyCacheException;
import com.androidol.util.Util;
import com.androidol.constants.UtilConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class TileFileSystemLoaderDB implements UtilConstants {
	
	// ===========================================================
	// fields
	// ===========================================================	
	
	public static final String 			DATABASE_NAME 				= "osmaptilefscache_db";	// database name
	public static final int 			DATABASE_VERSION 			= 2;						// database version
	public static final String 			T_FSCACHE 					= "t_fscache";				// name of the database table that keeps track of tile usage
	public static final String 			T_FSCACHE_NAME 				= "name_id";				// primary key for tiles in database (tile's url)
	public static final String 			T_FSCACHE_TIMESTAMP 		= "timestamp";				// time stamp of a tile
	public static final String 			T_FSCACHE_USAGECOUNT 		= "countused";				// how many times the tile has been used
	public static final String 			T_FSCACHE_FILESIZE 			= "filesize";				// file size of tile

	public static final String 			T_FSCACHE_CREATE_COMMAND 	= "CREATE TABLE IF NOT EXISTS " + T_FSCACHE
																	+ " (" 
																	+ T_FSCACHE_NAME + " VARCHAR(255),"
																	+ T_FSCACHE_TIMESTAMP + " DATE NOT NULL,"
																	+ T_FSCACHE_USAGECOUNT + " INTEGER NOT NULL DEFAULT 1,"
																	+ T_FSCACHE_FILESIZE + " INTEGER NOT NULL,"
																	+ " PRIMARY KEY(" + T_FSCACHE_NAME + ")" 
																	+ ");";

	public static final String 			T_FSCACHE_SELECT_LEAST_USED = "SELECT " + T_FSCACHE_NAME  + "," + T_FSCACHE_FILESIZE 
																	+ " FROM " + T_FSCACHE 
																	+ " WHERE "  + T_FSCACHE_USAGECOUNT 
																	+ " = (SELECT MIN(" + T_FSCACHE_USAGECOUNT + ") FROM "  + T_FSCACHE + ")";
	
	public static final String 			T_FSCACHE_SELECT_OLDEST 	= "SELECT " + T_FSCACHE_NAME + "," + T_FSCACHE_FILESIZE 
																	+ " FROM " + T_FSCACHE 
																	+ " ORDER BY " + T_FSCACHE_TIMESTAMP + " ASC";
	
	
	protected final Context 			context;
	protected final SQLiteDatabase 		database;
	protected final SimpleDateFormat 	DATE_FORMAT_ISO8601 		= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	private String 						TMP_COLUMN 					= "tmp"; 
	
	/**
	 * Constructor TileFileSystemLoaderDB
	 * 
	 * @param context
	 */
	public TileFileSystemLoaderDB(Context context) {
		this.context = context;
		this.database = new DatabaseHelper(context).getWritableDatabase();		
	}
	
	/**
	 * API Method: incrementUse
	 * 
	 * @param aFormattedTileURLString
	 */
	public void incrementUse(final String formattedUrlString) {
		// increase the tile usage by 1 and also update the time stamp
		final Cursor c = this.database.rawQuery("UPDATE " + T_FSCACHE + " SET " + T_FSCACHE_USAGECOUNT + " = " + T_FSCACHE_USAGECOUNT + " + 1 , " + T_FSCACHE_TIMESTAMP + " = '" + getNowAsIso8601() + "' WHERE " + T_FSCACHE_NAME + " = '" + formattedUrlString + "'", null);
		c.close();
	}
	
	/**
	 * API Method: addTileOrIncrement
	 * 
	 * @param url
	 * @param fileSizeInByte
	 * 
	 * @return fileSize
	 * size of the tile image in bytes
	 */
	public int addTileOrIncrement(final String formattedUrlString, final int fileSizeInByte) {
		// select the tile from database
		final Cursor c = this.database.rawQuery("SELECT * FROM " + T_FSCACHE + " WHERE " + T_FSCACHE_NAME + " = '" + formattedUrlString + "'", null);
		// check if tile already existed
		final boolean existed = c.getCount() > 0;
		c.close();
		//Util.printDebugMessage("Tile existed: " + existed);		
		if (existed) {
			incrementUse(formattedUrlString);
			return 0;
		} else {
			insertNewTileInfo(formattedUrlString, fileSizeInByte);
			return fileSizeInByte;
		}
	}
	
	/**
	 * API Method: removeTile
	 * 
	 * @param formattedUrlString
	 * @param fileSizeInByte
	 */
	public int removeTile(final String formattedUrlString) {		
		// select the tile from database
		Util.printDebugMessage(" ...try to remove tile " + formattedUrlString + " from database...");
		final Cursor c = this.database.rawQuery("SELECT * FROM " + T_FSCACHE + " WHERE " + T_FSCACHE_NAME + " = '" + formattedUrlString + "'", null);
		// check if tile already existed
		final boolean existed = (c.getCount() > 0);				
		if(existed) {
			c.moveToFirst();
			final int tileSize = c.getInt(c.getColumnIndexOrThrow(T_FSCACHE_FILESIZE));
			this.database.delete(T_FSCACHE, T_FSCACHE_NAME + "='" + formattedUrlString + "'", null);
			Util.printDebugMessage(" ...tile " + formattedUrlString + " removed from database...tile size " + tileSize);
			c.close();
			return tileSize;
		} else {
			c.close();
			return 0;
		}
	}
	
	/**
	 * API Method: insertNewTileInfo
	 * 
	 * @param formattedUrlString
	 * @param fileSizeInByte
	 */
	public void insertNewTileInfo(final String formattedUrlString, final int fileSizeInByte) {
		final ContentValues cv = new ContentValues();
		cv.put(T_FSCACHE_NAME, formattedUrlString);
		cv.put(T_FSCACHE_TIMESTAMP, getNowAsIso8601());
		cv.put(T_FSCACHE_FILESIZE, fileSizeInByte);
		this.database.insert(T_FSCACHE, null, cv);
	}

	/**
	 * API Method: deleteOldest
	 * 
	 * @param pSizeNeeded
	 * @return size of cache being freed
	 * @throws EmptyCacheException
	 */
	public int deleteOldest(final int sizeNeeded) throws EmptyCacheException {
		final Cursor c = this.database.rawQuery(T_FSCACHE_SELECT_OLDEST, null);
		final ArrayList<String> deleteFromDB = new ArrayList<String>();
		int sizeGained = 0;
		if(c != null){
			String fileNameOfDeleted; 
			if(c.moveToFirst()) {
				do {
					final int sizeItem = c.getInt(c.getColumnIndexOrThrow(T_FSCACHE_FILESIZE));
					sizeGained += sizeItem;
					fileNameOfDeleted = c.getString(c.getColumnIndexOrThrow(T_FSCACHE_NAME));
					deleteFromDB.add(fileNameOfDeleted);
					// delete files stored in context
					//this.context.deleteFile(fileNameOfDeleted);
					// delete files stored on SD card
					try {
						File fileToDelete = new File(fileNameOfDeleted);
						if(fileToDelete.exists()) {
							fileToDelete.delete();
						}
					} catch(Exception e) {
						Util.printErrorMessage("...error deleting cached tile...file name: " + fileNameOfDeleted);
					}
					
					Util.printDebugMessage("...deleted from file system: " + fileNameOfDeleted + " for " + sizeItem + " Bytes...");					
				} while(c.moveToNext() && sizeGained < sizeNeeded);
			} else {
				c.close();
				throw new EmptyCacheException("...cache is empty....");
			}
			c.close();

			for(String fn : deleteFromDB) {
				this.database.delete(T_FSCACHE, T_FSCACHE_NAME + "='" + fn + "'", null);
			}
		}
		return sizeGained;
	}
	
	/**
	 * API Method: getCacheUsedInByte
	 * 
	 * @return total size of cache in use
	 */
	public int getCacheUsedInByte() {
		final Cursor c = this.database.rawQuery("SELECT SUM(" + T_FSCACHE_FILESIZE + ") AS " + TMP_COLUMN + " FROM " + T_FSCACHE, null);
		final int ret;
		if(c != null){
			if(c.moveToFirst()) {
				ret = c.getInt(c.getColumnIndexOrThrow(TMP_COLUMN));
			} else {
				ret = 0;
			}
		}else{
			ret = 0;
		}
		c.close();
		return ret;
	}
	
	/**
	 * private method: getNowAsIso8601
	 * Get at the moment within ISO8601 format.
	 * 
	 * @return
	 * Date and time in ISO8601 format.
	 */
	private String getNowAsIso8601() {
		return DATE_FORMAT_ISO8601.format(new Date(System.currentTimeMillis()));
	} 
	
	/**
	 * private class DatabaseHelper
	 */
	private class DatabaseHelper extends SQLiteOpenHelper {				
		DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(T_FSCACHE_CREATE_COMMAND);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {			
			//Util.printDebugMessage("...upgrading database from version " + oldVersion + " to " + newVersion + "...all old data will be erased...");			
			db.execSQL("DROP TABLE IF EXISTS " + T_FSCACHE);
			onCreate(db);
		}
	}
	
}
