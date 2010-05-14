package com.androidol.map.schema;

import com.androidol.basetypes.Size;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public interface TileSchema {
	
	public abstract Coordinate 	getTileOrigin();
	public abstract Size 		getDefaultTileSize();
	public abstract int 		getDefaultTileWidth();
	public abstract int 		getDefaultTileHeight();
	public abstract String 		getTransparentTileUri();
	public abstract String 		getMissingTileUri();
	
}
