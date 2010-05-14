/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
 * This file was semi-automatically converted from the public-domain USGS PROJ source.
 */
package com.androidol.proj4j.map.proj;

import com.androidol.proj4j.map.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class MercatorProjection extends CylindricalProjection {
	
	public MercatorProjection() {
		minLatitude = MapMath.degToRad(-85);
		maxLatitude = MapMath.degToRad(85);
	}
	
	/**
	 * 
	 */
	/*
	public Point project(double lam, double phi, Point out) {
		
		if (spherical) {
			out.x = scaleFactor * lam;
			out.y = scaleFactor * Math.log(Math.tan(MapMath.QUARTERPI + 0.5 * phi));
		} else {
			out.x = scaleFactor * lam;
			out.y = -scaleFactor * Math.log(MapMath.tsfn(phi, Math.sin(phi), e));
		}
		return out;
	}
	*/
	
	/**
	 * 
	 */
	public Point project(double lam, double phi) {		
		double outX;
		double outY;		
		if (spherical) {
			outX = scaleFactor * lam;
			outY = scaleFactor * Math.log(Math.tan(MapMath.QUARTERPI + 0.5 * phi));
		} else {
			outX = scaleFactor * lam;
			outY = -scaleFactor * Math.log(MapMath.tsfn(phi, Math.sin(phi), e));
		}
		return this.jtsGeometryFactory.createPoint(new Coordinate(outX, outY));
	}
	
	/**
	 * 
	 */
	/*
	public Point projectInverse(double x, double y, Point out) {
		if (spherical) {
			out.y = MapMath.HALFPI - 2. * Math.atan(Math.exp(-y / scaleFactor));
			out.x = x / scaleFactor;
		} else {
			out.y = MapMath.phi2(Math.exp(-y / scaleFactor), e);
			out.x = x / scaleFactor;
		}
		return out;
	}
	*/
	
	/**
	 * 
	 */
	public Point projectInverse(double x, double y) {
		double outX;
		double outY;
		if (spherical) {
			outY = MapMath.HALFPI - 2. * Math.atan(Math.exp(-y / scaleFactor));
			outX = x / scaleFactor;
		} else {
			outY = MapMath.phi2(Math.exp(-y / scaleFactor), e);
			outX = x / scaleFactor;
		}
		return this.jtsGeometryFactory.createPoint(new Coordinate(outX, outY));
	}
	
	public boolean hasInverse() {
		return true;
	}

	public boolean isRectilinear() {
		return true;
	}

	/**
	 * Returns the ESPG code for this projection, or 0 if unknown.
	 */
	public int getEPSGCode() {
		return 9804;
	}

	public String toString() {
		return "Mercator";
	}

}
