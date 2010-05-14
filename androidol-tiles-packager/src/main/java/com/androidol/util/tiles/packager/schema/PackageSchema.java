package com.androidol.util.tiles.packager.schema;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.androidol.projection.Projection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class PackageSchema {

	protected String 		name;
	protected int 			minZoomLevel;
	protected int 			maxZoomLevel;
	
	protected int 			defaultZoomLevel;
	
	protected Envelope		extent;		
	protected Coordinate	center;
	
	protected int			buffer 				= 0;
	
	public PackageSchema(int minZoomLevel, int maxZoomLevel, int defaultZoomLevel, Envelope extent, Coordinate	center) {		
		this.minZoomLevel = minZoomLevel;
		this.maxZoomLevel = maxZoomLevel;
		this.defaultZoomLevel = defaultZoomLevel;
		this.extent = new Envelope(extent);
		this.center = new Coordinate(center);		
	}
	
	public PackageSchema() {
		readFromFile();
	}
	
	/**
	 * 
	 * @param pathToPropsFile
	 */
	public PackageSchema(String pathToPropsFile) {
		readFromFile(pathToPropsFile);
	}
	
	/**
	 * 
	 */
	public void readFromFile() {		
		readFromFile("package.properties");		
	}
	
	public void readFromFile(String inputPath) {
		//
		Properties props = new Properties();
		try {									
			BufferedReader reader = new BufferedReader(new InputStreamReader(PackageSchema.class.getResourceAsStream("/"+inputPath)));
			props.load(reader);					
			String minZoomLevelStr = props.getProperty("minZoomLevel");
			String maxZoomLevelStr = props.getProperty("maxZoomLevel");
			String defaultZoomLevelStr = props.getProperty("defaultZoomLevel");
			
			String centerStr = props.getProperty("center");
			String extentStr = props.getProperty("extent");
			String bufferStr = props.getProperty("buffer");
			String nameStr = props.getProperty("name");
			/*
			System.out.println("minZoomLevel: " 	+ minZoomLevelStr);
			System.out.println("maxZoomLevel: " 	+ maxZoomLevelStr);
			System.out.println("defaultZoomLevel: " + defaultZoomLevelStr);
			System.out.println("center: " 			+ centerStr);
			System.out.println("extent: " 			+ extentStr);
			*/			
			this.minZoomLevel = Integer.parseInt(minZoomLevelStr);
			this.maxZoomLevel = Integer.parseInt(maxZoomLevelStr);
			this.defaultZoomLevel = Integer.parseInt(defaultZoomLevelStr);
			
			// TODO: deal with projection of center based on an extra properties
			String[] centerCoords = centerStr.split(",");
			Point centerPoint = Projection.wgs84ToSphericalMercator(Double.parseDouble(centerCoords[0]), Double.parseDouble(centerCoords[1]));
			this.center = new Coordinate(centerPoint.getX(), centerPoint.getY());
			
			// TODO: deal with projection of center based on an extra properties
			String[] extentCoords = extentStr.split(",");			
			Point bottomLeft = Projection.wgs84ToSphericalMercator(Double.parseDouble(extentCoords[0]), Double.parseDouble(extentCoords[1]));
			Point topRight = Projection.wgs84ToSphericalMercator(Double.parseDouble(extentCoords[2]), Double.parseDouble(extentCoords[3]));			
			this.extent = new Envelope(bottomLeft.getX(), topRight.getX(), bottomLeft.getY(), topRight.getY());
			
			if(bufferStr != null) {
				this.buffer = Integer.parseInt(bufferStr);
			}
			if(!(this.buffer>=0 && this.buffer<=5)) {
				this.buffer = 0;
			}
			
			if(nameStr != null) {
				this.name = nameStr;
			} else {
				this.name = "package";
			}
			/*
			System.out.println("minZoomLevel: " 	+ this.minZoomLevel);
			System.out.println("maxZoomLevel: " 	+ this.maxZoomLevel);
			System.out.println("defaultZoomLevel: " + this.defaultZoomLevel);
			System.out.println("center: " 			+ this.center.toString());
			System.out.println("extent: " 			+ this.extent.toString());
			System.out.println("buffer: " 			+ this.buffer);
			*/
		} catch(IOException e) {
			System.out.println("...package.properties is missing or not valid...");
			e.printStackTrace();
		} catch(Exception e) {
			System.out.println("...package.properties contains invalid properties...");
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void writeToFile(String outputPath) {
		Properties props = new Properties();
		try {
			
			String minZoomLevelStr = String.valueOf(this.minZoomLevel);
			String maxZoomLevelStr = String.valueOf(this.maxZoomLevel);
			String defaultZoomLevelStr = String.valueOf(this.defaultZoomLevel);
			String centerStr = String.valueOf(this.center.x) + "," + String.valueOf(this.center.y);
			String extentStr = String.valueOf(this.extent.getMinX()) 
				+ "," + String.valueOf(this.extent.getMinY())
				+ "," + String.valueOf(this.extent.getMaxX())
				+ "," + String.valueOf(this.extent.getMaxY());			
			String bufferStr = String.valueOf(this.buffer);
			String nameStr = this.name;
			
			props.setProperty("minZoomLevel", minZoomLevelStr);
			props.setProperty("maxZoomLevel", maxZoomLevelStr);
			props.setProperty("defaultZoomLevel", defaultZoomLevelStr);
			// TODO: write out an extra property to indicate projection
			props.setProperty("center", centerStr);
			props.setProperty("extent", extentStr);
			props.setProperty("buffer", bufferStr);
			props.setProperty("name", nameStr);
			
			props.store(new FileOutputStream(outputPath), null); 
			
		} catch(IOException e) {
			System.out.println("...can not write to package.properties.gen...");
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void writeToFile() {
		writeToFile("package.properties.gen");
	}
	
	/**
	 * 
	 * 
	 */
	public void printOutPackageInfo() {
		System.out.println("name: " 			+ this.name);
		System.out.println("minZoomLevel: " 	+ this.minZoomLevel);
		System.out.println("maxZoomLevel: " 	+ this.maxZoomLevel);
		System.out.println("defaultZoomLevel: " + this.defaultZoomLevel);
		System.out.println("center: " 			+ this.center.toString());
		System.out.println("extent: " 			+ this.extent.toString());
		System.out.println("buffer: " 			+ this.buffer);		
	}

	public static void main(String[] args) {
		PackageSchema schema = new PackageSchema();
		schema.printOutPackageInfo();
		//schema.readFromFile();
		//schema.writeToFile();
	}
	
	// ===========================================================
	// getters & setters
	// ===========================================================
	
	public int getMinZoomLevel() {
		return minZoomLevel;
	}

	public void setMinZoomLevel(int minZoomLevel) {
		this.minZoomLevel = minZoomLevel;
	}

	public int getMaxZoomLevel() {
		return maxZoomLevel;
	}

	public void setMaxZoomLevel(int maxZoomLevel) {
		this.maxZoomLevel = maxZoomLevel;
	}

	public int getDefaultZoomLevel() {
		return defaultZoomLevel;
	}

	public void setDefaultZoomLevel(int defaultZoomLevel) {
		this.defaultZoomLevel = defaultZoomLevel;
	}

	public Envelope getExtent() {
		return extent;
	}

	public void setExtent(Envelope extent) {
		this.extent = extent;
	}

	public Coordinate getCenter() {
		return center;
	}

	public void setCenter(Coordinate center) {
		this.center = center;
	}

	public int getBuffer() {
		return buffer;
	}

	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
}
