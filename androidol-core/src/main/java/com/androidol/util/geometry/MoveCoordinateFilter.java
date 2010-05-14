package com.androidol.util.geometry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

public class MoveCoordinateFilter implements CoordinateFilter {
	
	protected double dx;
	protected double dy;
	
	public MoveCoordinateFilter(double x, double y) {
		this.dx = x;
		this.dy = y;
	}
	
	@Override
	public void filter(Coordinate coordinate) {
		coordinate.x = coordinate.x + dx; 
		coordinate.y = coordinate.x + dy;
	}

}
