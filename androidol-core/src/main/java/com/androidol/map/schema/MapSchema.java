package com.androidol.map.schema;

import com.androidol.basetypes.Size;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public interface MapSchema {
	
	public abstract double[] 	getScales();
	public abstract double[] 	getResolutions();
	
	public abstract int			getNumZoomLevels();
	public abstract int 		getMaxZoomLevel();
	public abstract int 		getMinZoomLevel();
	public abstract double 		getMaxResolution();
	public abstract double 		getMinResolution();
	public abstract double 		getMaxScale();
	public abstract double		getMinScale();
	public abstract int 		getDpi();
	public abstract String		getProjection();
	public abstract String 		getUnits();
	
	public abstract Envelope 	getDefaultMaxExtent();
	public abstract Envelope 	getDefaultMinExtent();
	public abstract Coordinate 	getDefaultCenter();
	public abstract int		 	getDefaultZoomLevel();
	public abstract double		getDefaultScale();
	public abstract double		getDefaultResolution();
	//public abstract Coordinate 	getTileOrigin();
	//public abstract Size 		getDefaultTileSize();
	//public abstract int 		getDefaultTileWidth();
	//public abstract int 		getDefaultTileHeight();
	//public abstract String 		getTransparentTileUri();
	//public abstract String 		getMissingTileUri();
	
}
