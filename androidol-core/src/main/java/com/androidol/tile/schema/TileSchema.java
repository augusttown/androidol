package com.androidol.tile.schema;

import com.androidol.basetypes.Size;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public interface TileSchema {
	
	public abstract double[] 	getScales();
	public abstract double[] 	getResolutions();
	public abstract int 		getDpi();
	public abstract int			getNumZoomLevels();
	public abstract int 		getMaxZoomLevel();
	public abstract int 		getMinZoomLevel();
	public abstract double 		getMaxResolution();
	public abstract double 		getMinResolution();
	public abstract double 		getMaxScale();
	public abstract double		getMinScale();
	public abstract String		getProjection();
	public abstract String 		getUnits();
	public abstract Coordinate 	getTileOrigin();
	public abstract Size 		getDefaultTileSize();	
	public abstract Envelope 	getDefaultMaxExtent();
	public abstract Envelope 	getDefaultMinExtent();
	public abstract Coordinate 	getDefaultCenter();
	public abstract int 		getDefaultTileWidth();
	public abstract int 		getDefaultTileHeight();
	public abstract String 		getTransparentTileUri();
	public abstract String 		getMissingTileUri();
	
}
