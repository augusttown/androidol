package com.androidol.map.schema;

import com.androidol.basetypes.Size;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class OSMTileMapSchema extends OSMMapSchema implements TileSchema{
	
	public static Coordinate	TILE_ORIGIN				= new Coordinate(-20037508.3392, -20037508.3392);	
	public static int			DEFAULT_TILE_WIDTH		= 256;
	public static int			DEFAULT_TILE_HEIGHT		= 256;
	public static Size			DEFAULT_TILE_SIZE		= new Size(DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT);
	
	public static String 		TRANSPARENT_TILE_URI 	= "R.drawable.transparent";
	public static String 		MISSING_TILE_URI 		= "R.drawable.missing";
	
	public Coordinate getTileOrigin() {
		return TILE_ORIGIN;
	}
	
	public Size getDefaultTileSize() {
		return DEFAULT_TILE_SIZE;
	}
	
	public int getDefaultTileWidth() {
		return DEFAULT_TILE_WIDTH;
	}
	
	public int getDefaultTileHeight() {
		return DEFAULT_TILE_HEIGHT;
	}
	
	public String getTransparentTileUri() {
		return TRANSPARENT_TILE_URI;
	}
	
	public String getMissingTileUri() {
		return MISSING_TILE_URI;
	}
	
}
