package com.androidol.tile.schema;

import com.androidol.basetypes.Size;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class ArcGISOnlineTileSchema implements TileSchema {
	
	public static double[] SCALES = {		
		147748799.285417,
		73874399.6427087,
		36937199.8213544,
		18468599.9106772,
		9234299.95533859,
		4617149.97766929,
		2308574.98883465,
		1154287.49441732,
		577143.747208662,
		288571.873604331,
		144285.936802165,
		72142.9684010827,
		36071.4842005414,
		18035.7421002707,
		9017.87105013534,
		4508.93552506767,		
	};
	
	public static double[] RESOLUTIONS = {		
		0.3515625,
		0.17578125,
		0.087890625,
		0.0439453125,
		0.02197265625,
		0.010986328125,
		0.0054931640625,
		0.00274658203125,
		0.001373291015625,
		0.0006866455078125,
		0.00034332275390625,
		0.000171661376953125,
		8.58306884765625E-05,
		4.29153442382813E-05,
		2.14576721191407E-05,
		1.07288360595703E-05,	
	};
	
	public static int 			DPI 					= 96;	
	public static int 			NUM_ZOOM_LEVELS 		= 16;
	public static int 			MAX_ZOOM_LEVEL 			= 16;
	public static int 			MIN_ZOOM_LEVEL	 		= 1;
	public static double		MAX_RESOLUTION			= RESOLUTIONS[0];
	public static double		MIN_RESOLUTION			= RESOLUTIONS[NUM_ZOOM_LEVELS-1];
	public static double		MAX_SCALE				= SCALES[0];
	public static double		MIN_SCALE				= SCALES[NUM_ZOOM_LEVELS-1];
	public static String		PROJECTION				= "EPSG:4326";
	public static String		UNITS					= "degrees";
	
	public static Coordinate	TILE_ORIGIN				= new Coordinate(-180.0, 90.0);	
	public static int			DEFAULT_TILE_WIDTH		= 512;
	public static int			DEFAULT_TILE_HEIGHT		= 512;
	public static Size			DEFAULT_TILE_SIZE		= new Size(DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT);
	
	public static Envelope		DEFAULT_MAX_EXTENT		= new Envelope(-180.0, 180.0, -90.0, 90.0);
	public static Envelope		DEFAULT_MIN_EXTENT		= null;
	public static Coordinate	DEFAULT_CENTER			= new Coordinate(0.0, 0.0);
	
	public static String 		TRANSPARENT_TILE_URI 	= "R.drawable.transparent";
	public static String 		MISSING_TILE_URI 		= "R.drawable.missing";
	
	@Override
	public int getDefaultTileHeight() {
		return DEFAULT_TILE_HEIGHT;
	}
	
	@Override
	public int getDefaultTileWidth() {
		return DEFAULT_TILE_WIDTH;
	}
	
	@Override
	public int getDpi() {		
		return DPI;
	}
	
	@Override
	public double getMaxResolution() {		
		return MAX_RESOLUTION;
	}
	
	@Override
	public double getMaxScale() {		
		return MAX_SCALE;
	}
	
	@Override
	public int getMaxZoomLevel() {		
		return MAX_ZOOM_LEVEL;
	}
	
	@Override
	public double getMinResolution() {		
		return MIN_RESOLUTION;
	}
	
	@Override
	public double getMinScale() {	
		return MIN_SCALE;
	}
	
	@Override
	public int getMinZoomLevel() {		
		return MIN_ZOOM_LEVEL;
	}
	
	@Override
	public String getMissingTileUri() {		
		return MISSING_TILE_URI;
	}
	
	@Override
	public int getNumZoomLevels() {		
		return NUM_ZOOM_LEVELS;
	}
	
	@Override
	public String getProjection() {		
		return PROJECTION;
	}
	
	@Override
	public double[] getResolutions() {		
		return RESOLUTIONS;
	}
	
	@Override
	public double[] getScales() {		
		return SCALES;
	}
	
	@Override
	public Coordinate getTileOrigin() {		
		return TILE_ORIGIN;
	}
	
	@Override
	public String getTransparentTileUri() {
		return TRANSPARENT_TILE_URI;
	}
	
	@Override
	public String getUnits() {	
		return UNITS;
	}

	@Override
	public Envelope getDefaultMaxExtent() {		
		return DEFAULT_MAX_EXTENT;
	}

	@Override
	public Envelope getDefaultMinExtent() {		
		return DEFAULT_MIN_EXTENT;
	}
	
	@Override
	public Size getDefaultTileSize() {		
		return DEFAULT_TILE_SIZE;
	}
	
	@Override
	public Coordinate getDefaultCenter() {		
		return DEFAULT_CENTER;
	}
	
}
