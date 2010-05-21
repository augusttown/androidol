package com.androidol.map.schema;

import com.androidol.basetypes.Size;
import com.androidol.proj4j.map.proj.ProjectionFactory;
import com.androidol.projection.Projection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class ArcGISOnlineWgs84MapSchema implements MapSchema {
	
	public static double[] SCALES = {
		5.916590302988535E8,
		2.9582951514942676E8,
		1.4791475757471338E8,
		7.395737878735669E7,
		3.6978689393678345E7,
		1.8489344696839172E7,
		9244672.348419586,
		4622336.174209793,
		2311168.0871048965,
		1155584.0435524483,
		577792.0217762241,
		288896.01088811207,
		144448.00544405603,
		72224.00272202802,
		36112.00136101401,
		18056.000680507004,
		9028.000340253502,
		4514.000170126751,
		2257.0000850633755,		
	};
	
	public static double[] RESOLUTIONS = {
		1.40625,
		0.703125,
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
		5.36441802978515E-06
	};
	
	public static int 			NUM_ZOOM_LEVELS 		= 19;
	public static int 			MAX_ZOOM_LEVEL 			= 18;
	public static int 			MIN_ZOOM_LEVEL	 		= 0;
	public static double		MAX_RESOLUTION			= RESOLUTIONS[0];
	public static double		MIN_RESOLUTION			= RESOLUTIONS[NUM_ZOOM_LEVELS-1];
	public static double		MAX_SCALE				= SCALES[0];
	public static double		MIN_SCALE				= SCALES[NUM_ZOOM_LEVELS-1];
	
	public static int 			DPI 					= 96;
	public static String		PROJECTION				= "epsg:4326"; // mercator auxiliary??
	public static String		UNITS					= "meters";
	
	public static Envelope		DEFAULT_MAX_EXTENT		= new Envelope(-180.0, 180.0, -90.0, 90.0);
	public static Envelope		DEFAULT_MIN_EXTENT		= null;
	public static Coordinate	DEFAULT_CENTER			= new Coordinate(0.0, 0.0);
	public static int			DEFAULT_ZOOM_LEVEL		= 0;
	public static double		DEFAULT_RESOLUTION		= RESOLUTIONS[DEFAULT_ZOOM_LEVEL];
	public static double		DEFAULT_SCALE			= SCALES[DEFAULT_ZOOM_LEVEL];
	
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
	public Coordinate getDefaultCenter() {		
		//return DEFAULT_CENTER;						
		// Rancho Cucamonga    	
    	return new Coordinate(-117.5931084, 34.1063989);
	}
	
	@Override
	public int getDefaultZoomLevel() {
		return DEFAULT_ZOOM_LEVEL;
	}
	
	@Override
	public double getDefaultScale() {
		return DEFAULT_SCALE;
	}
	
	@Override
	public double getDefaultResolution() {
		return DEFAULT_RESOLUTION;
	}
}
