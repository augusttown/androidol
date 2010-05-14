package com.androidol.proj4j.test;

import java.awt.geom.Point2D;
import java.io.IOException;

import com.androidol.proj4j.map.proj.Projection;
import com.androidol.proj4j.map.proj.ProjectionFactory;
import com.vividsolutions.jts.geom.Point;

public class TestProj4j {
	
	public static void testProjection() {
								
		Projection epsg3785 = ProjectionFactory.getNamedPROJ4CoordinateSystem("epsg:3785");		
		
		/*						
		System.out.println("transform from latlon to epsg:3785");
		System.out.println("latlon: -117.5931084, 34.1063989");
		Point pEpsg3785 = epsg3785.transform(-117.5931084, 34.1063989);
		System.out.println("epsg:3785: " + pEpsg3785.getX() + ", " + pEpsg3785.getY());
		
		System.out.println("transform from epsg:3785 to latlon");
		System.out.println("epsg:3785: " + pEpsg3785.getX() + ", " + pEpsg3785.getY());
		Point latlon = epsg3785.inverseTransform(pEpsg3785);
		System.out.println("latlon: " + latlon.getX() + ", " + latlon.getY());
		*/				
		
		//+proj=tmerc +lat_0=37.5 +lon_0=-85.66666666666667 +k=0.999966667 +x_0=99999.99989839978 +y_0=249999.9998983998 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs
		String[] params = {
			"+proj=tmerc",
			"+lat_0=37.5",
			"+lon_0=-85.66666666666667",
			"+k=0.999966667",
			"+x_0=99999.99989839978",
			"+y_0=249999.9998983998",
			"+ellps=GRS80",
			"+datum=NAD83",
			"+to_meter=0.3048006096012192",
			"+no_defs"
		};
		Projection epsg2695 = ProjectionFactory.fromPROJ4Specification(params);
		
		System.out.println("transform from epsg:2695 to lonlat");
		System.out.println("epsg:2965: 166850.0, 1703850.85" );			
		Point latlon = epsg2695.inverseTransform(166850.0, 1703850.85);		
		System.out.println("latlon: " + latlon.getX() + ", " + latlon.getY());
		
		System.out.println("transform from latlon to epsg:2965");
		System.out.println("latlon: -86.24155290750238, 39.92487547623508");
		Point pEpsg2695 = epsg2695.transform(-86.24155290750238, 39.92487547623508);
		System.out.println("epsg:2965: " + pEpsg2695.getX() + ", " + pEpsg2695.getY());
		
	}
	
	/**
	 * 
	 */
	public static void testProjectionFactory() {
		
		/* 
		 * getNamedPROJ4CoordinateSystem() 
		 *   returns a Projection based on a well known name like epsg:3785
		 */
		
		System.out.println("ProjectionFactory.getNamedPROJ4CoordinateSystem(\"epsg:3785\")");		
		String name = "epsg:3785";		
		Projection proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(name);
		
		
		/*
		 * fromPROJ4Specification()
		 */
		// epsg:2965
		// +proj=tmerc +lat_0=37.5 +lon_0=-85.66666666666667 +k=0.999966667 +x_0=99999.99989839978 +y_0=249999.9998983998 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs
		/*
		String[] params = {
			// reference by name	
			//"+init=epsg:2965",
				
			// reference by projection			
			"+proj=tmerc",
			"+lat_0=37.5",
			"+lon_0=-85.66666666666667",
			"+k=0.999966667",
			"+x_0=99999.99989839978",
			"+y_0=249999.9998983998",
			"+ellps=GRS80",
			"+datum=NAD83",
			"+to_meter=0.3048006096012192",
			"+no_defs"			
		};
		Projection proj = ProjectionFactory.fromPROJ4Specification(params);		
		*/
		
		// ProjectionFactory.readProjectionFile()
		/*
		System.out.println("ProjectionFactory.getNamedPROJ4CoordinateSystem(\"epsg:900913\")");		
		//String name = "epsg:900913";	
		//Projection proj = ProjectionFactory.getNamedPROJ4CoordinateSystem(name);
		Projection proj = null;
		try {
			proj = ProjectionFactory.readProjectionFile("others", "900913");		
		} catch(IOException e) {
			e.printStackTrace();
		}
		*/
		
		if(proj != null) {
			System.out.println("...projection name: " 		+ proj.getName());
			System.out.println("...projection epsg code: " 	+ proj.getEPSGCode());
			System.out.println("...projection proj4 desc: " + proj.getPROJ4Description());			
		} else {
			System.out.println("specific projection not found");
		}		
		
	}

	public static void main(String[] args) {
		TestProj4j.testProjection();
		//TestProj4j.testProjectionFactory();
	}
}
