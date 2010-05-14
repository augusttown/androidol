package com.androidol.test.proj4j;

import com.androidol.util.Util;
import com.androidol.proj4j.map.proj.Projection;
import com.androidol.proj4j.map.proj.ProjectionFactory;
import com.vividsolutions.jts.geom.Point;

public class TestProjection {
	
	public static void testProjection() {
		/* 
		 * getNamedPROJ4CoordinateSystem() 
		 *   returns a Projection based on a well known name like epsg:3785
		 */
		Util.printDebugMessage("ProjectionFactory.getNamedPROJ4CoordinateSystem(\"epsg:3785\")");		
		String name = "epsg:3785";		
		Projection proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(name);
		
		/*
		 * 
		 */
		String[] params = {
				
		};
		ProjectionFactory.fromPROJ4Specification(params);
		
		if(proj != null) {
			Util.printDebugMessage("...projection name: " 				+ proj.getName());
			Util.printDebugMessage("...projection epsg code: " 		+ proj.getEPSGCode());
			Util.printDebugMessage("...projection proj4 desc: " 		+ proj.getPROJ4Description());
		} else {
			Util.printDebugMessage("...projection " + name + " not found");
		}
		
		Util.printDebugMessage("Projection.transform()");
		Util.printDebugMessage("...rancho cucamonga: -117.5931084, 34.1063989" );
				
		Point point1 = com.androidol.projection.Projection.wgs84ToSphericalMercator(-117.5931084, 34.1063989);
		Util.printDebugMessage("...old transform: " + point1.getX() + ", " + point1.getY());
		
		// Projection.transform()
		Point point2 = proj.transform(-117.5931084, 34.1063989);
		Util.printDebugMessage("...proj4j transform: " + point2.getX() + ", " + point2.getY());
		
	}
	
	/**
	 * 
	 */
	public static void testProjectionFactory() {
		
		/* 
		 * getNamedPROJ4CoordinateSystem() 
		 *   returns a Projection based on a well known name like epsg:3785
		 */
		Util.printDebugMessage("ProjectionFactory.getNamedPROJ4CoordinateSystem(\"epsg:3785\")");		
		String name = "epsg:3785";		
		Projection proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(name);
		
		/*
		 * fromPROJ4Specification()
		 */
		// epsg:2965
		// +proj=tmerc +lat_0=37.5 +lon_0=-85.66666666666667 +k=0.999966667 +x_0=99999.99989839978 +y_0=249999.9998983998 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs
		String[] params = {
			"proj=tmerc",
			"lat_0=37.5",
			"lon_0=-85.66666666666667",
			"k=0.999966667",
			"x_0=99999.99989839978",
			"y_0=249999.9998983998",
			"ellps=GRS80",
			"datum=NAD83",
			"to_meter=0.3048006096012192",
			"no_defs"
		};
		ProjectionFactory.fromPROJ4Specification(params);
		
		if(proj != null) {
			Util.printDebugMessage("...projection name: " 		+ proj.getName());
			Util.printDebugMessage("...projection epsg code: " 	+ proj.getEPSGCode());
			Util.printDebugMessage("...projection proj4 desc: " + proj.getPROJ4Description());
		} else {
			Util.printDebugMessage("...projection " + name + " not found");
		}	
	}	
	
	
}
