package com.androidol.constants;

public interface UtilConstants {
	
	public static final String 				ANDROIDOL_NAMESPACE 			= "http://www.esri.com/androidol";
	
	public static final String 				DEBUGTAG 						= "ANDROIDOL";
	public static final boolean 			ISDEBUGMODE 					= true;
	
	public static final int 				MAX_MEMORY_CACHE_NUM 			= 25;	// max number of tiles allow to be cached in memory
	public static final int 				MAX_MEMORY_CACHE_DIM 			= 8;	// max dimension (number of tile rows or number of tile columns) allow to be cached in memory
	public static final int 				MIN_MEMORY_CACHE_DIM 			= 4;	// max dimension (number of tile rows or number of tile columns) allow to be cached in memory
	public static final int 				DISK_CACHE_SIZE 				= 64 * 1024 * 1024; // max size (in byte) of tiles allow to be cached on disk
	public static final int 				IO_BUFFER_SIZE 					= 8 * 1024; // I/O buffer size in byte
	
	public static final int					DEFAULT_PAN_DX					= 320/8; // 
	public static final int					DEFAULT_PAN_DY					= 430/8; // 
	
	public static final String				DEFAULT_MISSING_TILE_URL		= "R.drawable.missing";
	public static final String				DEFAULT_TRANSPARENT_TILE_URL	= "R.drawable.transparent";
	
	public static final	int 				DOTS_PER_INCH 					= 96;
}
