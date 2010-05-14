package com.androidol.map.schema;

import com.androidol.basetypes.Size;
import com.androidol.proj4j.map.proj.ProjectionFactory;
import com.androidol.projection.Projection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class OSMMapSchema implements MapSchema {
	
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
		156543.0339,
		78271.51695,
		39135.758475,
		19567.8792375,
		9783.93961875,
		4891.969809375,
		2445.9849046875,
		1222.99245234375,
		611.496226171875,
		305.7481130859375,
		152.87405654296876,
		76.43702827148438,
		38.21851413574219,
		19.109257067871095,
		9.554628533935547,
		4.777314266967774,
		2.388657133483887,
		1.1943285667419434,
		0.5971642833709717,
	};
	
	public static int 			NUM_ZOOM_LEVELS 		= 19;
	public static int 			MAX_ZOOM_LEVEL 			= 18;
	public static int 			MIN_ZOOM_LEVEL	 		= 1;
	public static double		MAX_RESOLUTION			= RESOLUTIONS[0];
	public static double		MIN_RESOLUTION			= RESOLUTIONS[NUM_ZOOM_LEVELS-1];
	public static double		MAX_SCALE				= SCALES[0];
	public static double		MIN_SCALE				= SCALES[NUM_ZOOM_LEVELS-1];
	
	public static int 			DPI 					= 96;
	public static String		PROJECTION				= "epsg:3785"; // mercator auxiliary??
	public static String		UNITS					= "meters";
	
	public static Envelope		DEFAULT_MAX_EXTENT		= new Envelope(-20037508.3392, 20037508.3392, -20037508.3392, 20037508.3392);
	public static Envelope		DEFAULT_MIN_EXTENT		= null;
	public static Coordinate	DEFAULT_CENTER			= new Coordinate(0.0, 0.0);
	public static int			DEFAULT_ZOOM_LEVEL		= 3;
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
		/*
		// old projection library
		Point portland = Projection.wgs84ToSphericalMercator(-122.838493, 45.432976); // zoom to portland
    	// proj4j projection library
    	Point portland = (ProjectionFactory.getNamedPROJ4CoordinateSystem("epsg:3785")).transform(-122.838493, 45.432976); // Mercator
    	return new Coordinate(portland.getX(), portland.getY());
    	*/
    	
		// old projection library
		//Point rancho_cucamonga = Projection.wgs84ToSphericalMercator(-117.5931084, 34.1063989); // zoom to Rancho Cucamonga
		// proj4j projection library
		Point rancho_cucamonga = (ProjectionFactory.getNamedPROJ4CoordinateSystem(PROJECTION)).transform(-117.5931084, 34.1063989); // Mercator    	
    	return new Coordinate(rancho_cucamonga.getX(), rancho_cucamonga.getY());
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
