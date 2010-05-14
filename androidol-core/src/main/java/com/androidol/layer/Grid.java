package com.androidol.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;

import com.androidol.Map;
import com.androidol.R;
import com.androidol.basetypes.Pixel;
import com.androidol.basetypes.Size;
import com.androidol.constants.UtilConstants;
import com.androidol.events.TileEvents;
import com.androidol.map.schema.TileSchema;
import com.androidol.tile.Tile;
import com.androidol.util.Util;
import com.androidol.util.tiles.TileProvider;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class Grid extends HTTPRequest implements UtilConstants {
	
	// ===========================================================
	// fields related to grid layer itself
	// ===========================================================	
	protected 	TileSchema 						schema				= null;
	protected 	Size 							tileSize			= null;
	protected 	Coordinate 						tileOrigin			= null;
	protected 	boolean 						singleTile 			= false;
	protected 	double 							ratio				= 1.5;
	
	public 		int 							numLoadingTiles		= 0;	
	protected	Pixel 							origin				= null;		
	protected 	ArrayList<ArrayList<Tile>> 		grid 				= null;
	protected 	TileProvider					tileProvider		= null;
	protected 	TileEventsHandler				tileEventsHandler	= new TileEventsHandler();
	
	// ===========================================================
	// fields related to Android View 
	// ===========================================================	
	
	
	// ===========================================================
	// Constructor
	// ===========================================================
	
	public Grid(Context context) {
		super(context);
		// TODO:
		//this.singleTile = false; // by default set this.singleTile to false
		// 		
		this.grid = new ArrayList<ArrayList<Tile>>();
		this.tileProvider = new TileProvider(context);		// utilities dealing with tiles
		this.tileProvider.events.registerAll(this.tileEventsHandler);
		this.tileProvider.setOfflineMode(this.isOfflineMode);
		this.tileProvider.setLayer(this);
	}
	
	public Grid(Context context, AttributeSet attrs) {
		super(context, attrs);
		// configuration options user could set through constructor:
		//     tileSize
		//	   tileOrigin
		//	   singleTile 
		//     ratio 	
		//
		// TODO: apply configure options
		// configure this.singleTile to true or false based on settings in layout xml
		this.singleTile = attrs.getAttributeBooleanValue(ANDROIDOL_NAMESPACE, "isSingleTile", false);
		// 
		this.grid = new ArrayList<ArrayList<Tile>>();
		this.tileProvider = new TileProvider(context);		// utilities dealing with tiles
		this.tileProvider.events.registerAll(this.tileEventsHandler);
		//
		this.tileProvider.setOfflineMode(this.isOfflineMode);
		this.tileProvider.setLayer(this);
	}
	
	/**
	 * 
	 */
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//
		this.canvas = canvas;
		/*
		if(this.dragging == true) {
			this.canvas.translate(this.dragDx, this.dragDy);
			this.dragging = false;
		}
		*/
		//Util.printDebugMessage("... layer " + this.name + " onDraw() is callled...");		
		redraw();
		//
	}
	
	/**
	 * Old Constructor Grid
	 * 
	 * @param name
	 * @param url
	 * @param params
	 * @param options
	 */
	/*
	public Grid(String name, String url, HashMap<String, String> params, HashMap<String, Object> options) {
		super(name, url, params, options);
		// configuration options user could set through constructor:
		//     tileSize
		//	   tileOrigin
		//	   singleTile 
		//     ratio 	
		//
		// TODO: apply configure options
		// 
		this.grid = new ArrayList<ArrayList<Tile>>();
	}
	*/
	
	/**
	 * API Method: destroy
	 * 
	 * @param newBaseLayer
	 * whether to set new base layer or not
	 */
	@Override 
	public void destroy(boolean newBaseLayer) {
		this.clearGrid();
        this.grid = null;
        this.tileSize = null;
        this.tileOrigin = null;
        super.destroy(newBaseLayer); 
	}
	
	/**
	 * API Method: clearGrid
	 * 
	 * clean up the this.grid matrix 
	 */
	public void clearGrid() {
		if(this.grid != null && this.grid.size()>0) {
            for(int iRow=0; iRow<this.grid.size(); iRow++) {
                ArrayList<Tile> row = this.grid.get(iRow);
                if(row!=null && row.size()>0) {
	                for(int iCol=0; iCol<row.size(); iCol++) {
	                	Tile tile = row.get(iCol);
	                    // TODO: this.removeTileMonitoringHooks(tile); 
	                    tile.destroy();
	                }
                }
                row.clear();
            }
            this.grid.clear();
        }
	}
	
	/**
	 * dragTo(Coordinate oldCenter, Coordinate newCenter)
	 * 
	 */
	@Override 
	public void drag(int dx, int dy) {
		super.drag(dx, dy);		
		// TODO: deal with big delta where it's outof bounds
		/*
		 * implement gridded tile shifting by changing the position of each by dx and dy in this.grid -
		 * - and redraw them
		 */
		shiftGriddedTiles(dx, dy);
		spiralTileLoad();			
		postInvalidate();
		
		/*
		 * implement gridded tile shifting by calling this.canvas.translate((float)dx, (float)dy) in each layer
		 */
		//shiftGriddedTilesUsingCanvas(dx, dy);
	}
	
	/**
	 * API Method: moveTo
	 * 
	 * @param bounds
	 * @param zoomChanged
	 * @param dragging
	 * 
	 */
	@Override 
	public void moveTo(Envelope bounds, boolean zoomChanged, boolean centerChanged) {		
		super.moveTo(bounds, zoomChanged, centerChanged);        		
		//Util.printDebugMessage("@...Grid.moveTo() is called...");
		if(bounds == null) {
            bounds = this.map.getExtent();
        }
		//Util.printDebugMessage("zoom changed: " + zoomChanged);
		//Util.printDebugMessage("center changed: " + centerChanged);
        if(bounds != null) {			
        	boolean forceReTile = false;
        	if(this.grid.size()==0 || zoomChanged) { // re-tiling when grid is empty or zoom level changed
        		forceReTile = true;
        	}        	 
			//Util.printDebugMessage(" ...is forced to re-tiling: " + forceReTile + "...");
        	Envelope tilesBounds = this.getTilesBounds();                  
            if(this.singleTile == true) {                                           	
            	if(forceReTile==true || tilesBounds.contains(bounds)==false) {
                	//Util.printDebugMessage(" ...it is single tile...");
                	this.initSingleTile(bounds);                	
                } else {
                	// just draw old tile
                	if(this.grid.size()>0) {
                		ArrayList<Tile> tiles = this.grid.get(0);
                		Tile singleTile = tiles.get(0);
                		if(singleTile != null) {
                			singleTile.draw(this.tileProvider, this.canvas, this.paint, createDrawSignature());
                		}
                	}                	                            	
                }
            } else {            	            	
            	
            	boolean forceInitGriddedTiles = false;
            	if(forceReTile==true || centerChanged==true) {
            		forceInitGriddedTiles = true;
            	}
            	//Util.printDebugMessage(" ...it is tile grid...");            	            	
            	if(forceInitGriddedTiles == true) {
            		this.initGriddedTiles(bounds);
            	} else {            		
            		this.spiralTileLoad();
            	}            	                
            	// old code from OpenLayers
            	/*
            	if(forceReTile == true || tilesBounds.containsBounds(bounds, true, true)==false) {
                	//Util.printDebugMessage(" ...reload all tiles...", true);
                    this.initGriddedTiles(bounds);
                    printCurrentGridStatus();
                } else {
                	//Util.printDebugMessage(" ...only shift tile grid...", true);
                	this.moveGriddedTiles(bounds);
                	printCurrentGridStatus();
                }
                */            	
                //printCurrentGridStatus();
            }
        }
	}
	
	/**
	 * API Method: setMap
	 * 
	 * @param map
	 */
	@Override
	public void setMap(Map map) {
		super.setMap(map);		
		if(this.schema!=null && this.schema instanceof TileSchema) {
			// do nothing
		} else if(map.getMapSchema() instanceof TileSchema) {
			this.schema = (TileSchema)map.getMapSchema();
		} else {
			// TODO: if map's schema only supports MapSchema interface
			//       throw exception or trying to associate a TileSchema to a MapSchema
			//throw new Exception("...map's schema doesn't support TileSchema interface...");
		}		
		if(this.singleTile == true) {
			this.setImageSize(this.map.getSize());
			this.setTileSize(this.map.getCurrentSize());
		} else {
			if(this.tileSize == null) { // inherit map's tile size if not set by constructor
	            this.tileSize = this.schema.getDefaultTileSize();
	            if(this.tileSize.getWidth() == 256 && this.tileSize.getHeight() == 256) {
	    			this.tileProvider.setLoadingTile(BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_256));
	    		}
	    		if(this.tileSize.getWidth() == 512 && this.tileSize.getHeight() == 512) {
	    			this.tileProvider.setLoadingTile(BitmapFactory.decodeResource(context.getResources(), R.drawable.loading_512));
	    		}
	        }
		}					
	}
	
	/**
	 * API Method: clone
	 * 
	 */
	@Override
	public Grid clone() {
		// TODO: to be implemented
		return null;
	}
		
	// old OpenLayers code
	/*
	private Bounds getGridBounds() {
		return this.getTilesBounds();
	}
	*/
	
	/**
	 * API Method: getTilesBounds
	 *  
	 * @return bounds
	 * the extent where all tiles in grid cover
	 */
	public Envelope getTilesBounds() {
		Envelope bounds = null;         
        if(this.grid!=null && this.grid.size()>0) {
        	int bottom = this.grid.size() - 1;
    	    Tile bottomLeftTile = this.grid.get(bottom).get(0);    	
    	    int right = this.grid.get(0).size() - 1; 
    	    Tile topRightTile = this.grid.get(0).get(right);    	
    	    bounds = new Envelope(
    	    	bottomLeftTile.getBounds().getMinX(), 
    	    	topRightTile.getBounds().getMaxX(),
    	    	bottomLeftTile.getBounds().getMinY(),    	         
    	        topRightTile.getBounds().getMaxY()
    	    );
            
        }   
        return bounds;
	}
	
	/**
	 * API Method: getTileBounds
	 * 
	 * @param viewPortPx
	 * @return bounds
	 * given a point on screen, calculate the bounds of the tile which that point is in
	 */
	public Envelope getTileBounds(Pixel viewPortPx) {
		Envelope maxExtent = this.maxExtent;
        double resolution = this.getResolution();
        double tileMapWidth= resolution * this.tileSize.getWidth();
        double tileMapHeight = resolution * this.tileSize.getHeight();
        Coordinate mapPoint = this.getCoordinateFromViewPortPx(viewPortPx);
        double tileLeft = maxExtent.getMinX() + (tileMapWidth*Math.floor((mapPoint.x-maxExtent.getMinX())/tileMapWidth));
        double tileBottom = maxExtent.getMinY() + (tileMapHeight*Math.floor((mapPoint.y-maxExtent.getMinY())/tileMapHeight));
        return new Envelope(tileLeft, tileLeft + tileMapWidth, tileBottom, tileBottom + tileMapHeight);
	}
	
	/**
	 * API Method: initSingleTile
	 * 
	 * @param bounds
	 */
	public void initSingleTile(Envelope bounds) {
		//Util.printDebugMessage("@...Grid.initSingleTile() is called...");
		Coordinate center = bounds.centre();
	    double tileWidth = bounds.getWidth() * this.ratio;
	    double tileHeight = bounds.getHeight() * this.ratio;
	                                   
	    Envelope tileBounds = 
	        new Envelope(			
	        				center.x - (tileWidth/2),
	        				center.x + (tileWidth/2),
	        				center.y - (tileHeight/2),	                        
	                        center.y + (tileHeight/2));
	
	    Coordinate ul = new Coordinate(tileBounds.getMinX(), tileBounds.getMaxY());
	    Pixel px = this.map.getLayerPxFromCoordinate(ul);
	
	    if(this.grid.size() == 0) {
	        this.grid.add(new ArrayList<Tile>());
	    }
	    
	    Tile tile = null;
	    if(this.grid.get(0).size() > 0) {
	    	tile = this.grid.get(0).get(0);
	    }	    
	    if(tile == null) {
	        tile = this.addTile(tileBounds, px, new Pixel(0,0), this.canvas, this.paint);	        
	        // TODO: this.addTileMonitoringHooks(tile);	        
	        this.grid.get(0).add(tile);
	    } else {
	        tile.moveTo(tileBounds, px);	// move the tile	        
	    }   
	    tile.draw(this.tileProvider, this.canvas, this.paint); // redraw the tile
	    this.removeExcessTiles(1,1);
	}
	
	/**
	 * shiftGriddedTiles(int dx, int dy)
	 * 
	 * @param dx
	 * @param dy
	 */
	public void shiftGriddedTiles(int dx, int dy) {
		if(this.grid != null) {
			for(int i=0; i<this.grid.size(); i++) {
				ArrayList<Tile> row = this.grid.get(i);
				if(row != null) {
					for(int j=0; j<row.size(); j++) {
						Tile tile = row.get(j);
						Pixel position = new Pixel(tile.getPosition().getX()+dx, tile.getPosition().getY()+dy);
						tile.setPosition(position);
					}
				}
			}
		}
	}
	
	/**
	 * shiftGriddedTilesUsingMatrix(int dx, int dy)
	 * 
	 * @param dx
	 * @param dy
	 */
	public void shiftGriddedTilesUsingMatrix(int dx, int dy) {
		
	}
	
	/**
	 * shiftGriddedTilesUsingMatrix(int dx, int dy)
	 * 
	 * @param dx
	 * @param dy
	 */

	
	public void shiftGriddedTilesUsingCanvas(int dx, int dy) {
		//Grid.this.canvas.translate((float)dx, (float)dy);
		if(this.dragging == false) {
			this.dragging = true;
		}
		this.dragDx = this.dragDx + (dx);
		this.dragDy = this.dragDy + (dy);
		invalidate();
	}
	
	/**
	 * API Method: initGriddedTiles
	 * 
	 * @param bounds
	 */
	public void initGriddedTiles(Envelope bounds) {
		//Util.printDebugMessage("@...Grid.initGriddedTiles() is called...");
		//Util.printDebugMessage(" ...grid bounds: " + bounds.toString() + "...");
		// get map width and height on screen e.g. 1024 * 768
		Size viewSize = this.map.getSize();
	    // minimum number of tiles needed to cover the height of the map
		int minRows = (int)Math.ceil(viewSize.getHeight()/this.tileSize.getHeight()) + Math.max(1, 2 * this.buffer);
		//Util.printDebugMessage(" ...minimum rows needed: " + minRows + "...");
		// minimum number of tiles needed to cover the width of the map
		int minCols = (int)Math.ceil(viewSize.getWidth()/this.tileSize.getWidth()) + Math.max(1, 2 * this.buffer);
		//Util.printDebugMessage(" ...minimum cols needed: " + minCols + "...");
		// maximum extent of the map
	    Envelope extent = this.getMaxExtent();
	    // map resolution meaning how many meters/degrees does one pixel represent on map 
	    double resolution = this.map.getResolution();	    
	    // 'tilelon' and 'tilelat' are how many meters/degrees does a tile cover on map
	    double tilelon = resolution * this.tileSize.getWidth();
	    double tilelat = resolution * this.tileSize.getHeight();
	    // distance between left-most point of maxExtent and left-most point of current extent
	    double offsetlon = bounds.getMinX() - extent.getMinX();
	    // number of tiles needed to cover the space 
	    //   between left-most point of maxExtent and left-most point of current extent
	    int tilecol = (int)Math.floor(offsetlon/tilelon) - this.buffer;
	    // the fractional tile needed to reach the left-most point of current extent
	    double tilecolremain = offsetlon/tilelon - tilecol;
	    // how many pixels this fractional tile represent
	    double tileoffsetx = -tilecolremain * this.tileSize.getWidth();
	    // the lon where the left-most tile starts to cover the map current extent  
	    double tileoffsetlon = extent.getMinX() + tilecol * tilelon;
	    
	    // distance between bottom-most point of maxExtent and top-most point of current extent
	    //   plus one more tile span
	    double offsetlat = bounds.getMaxY() - (extent.getMinY() + tilelat);  
	    // number of tiles needed to cover the space 
	    //   between bottom-most point of maxExtent and top-most point of current extent
	    int tilerow = (int)Math.ceil(offsetlat/tilelat) + this.buffer;
	    // the fractional tile needed to reach the top-most point of current extent
	    double tilerowremain = tilerow - offsetlat/tilelat;
	    // how many pixels this fractional tile represent
	    double tileoffsety = -tilerowremain * this.tileSize.getHeight();
	    // the lat where the bottom-most tile starts to cover the map current extent 
	    double tileoffsetlat = extent.getMinY() + tilerow * tilelat;
	    // God help us
	    tileoffsetx = Math.round(tileoffsetx); 
	    tileoffsety = Math.round(tileoffsety);
	    // the position on screen where the top-left tile should be placed
	    this.origin = new Pixel(tileoffsetx, tileoffsety);
	
	    double startX = tileoffsetx; 
	    double startLon = tileoffsetlon;
	
	   	int rowidx = 0;	
	   	int colidx = 0;	 
	    //long startTime = System.currentTimeMillis();
	   	do {
	    	ArrayList<Tile> row = null;
	    	if(rowidx >= this.grid.size() || this.grid.get(rowidx) == null) {
	    		row = new ArrayList<Tile>();
	            this.grid.add(row);
	    	} else {
	    		row = this.grid.get(rowidx);
	    	}
	    	rowidx++;
	    	
	        tileoffsetlon = startLon;
	        tileoffsetx = startX;
	        
	        colidx = 0;	        
	        do {
	        	Envelope tileBounds = new Envelope(
	        									tileoffsetlon, 
	        									tileoffsetlon + tilelon,
	        									tileoffsetlat, 	        									
	        									tileoffsetlat + tilelat);
	
	            double x = tileoffsetx;
	            //x -= (int)(this.map.getLayerContainerCanvasLeft()); // this.map.getLayerContainerCanvasLeft() should always be 0.0 
	
	            double y = tileoffsety;
	            //y -= (int)(this.map.getLayerContainerCanvasTop()); // this.map.getLayerContainerCanvasTop() should always be 0.0
	
	            Pixel px = new Pixel(x, y);
	            Tile tile = null;
	            if(colidx >= row.size() || row.get(colidx) == null) {
	            	//Util.printDebugMessage(" ...add tile..." + "bounds: " + tileBounds.toString() + " position: " + px.toString() + "...");
	            	//Util.printDebugMessage(" ...add tile..." + this.getUrl(tileBounds) + "...");
	            	tile = this.addTile(tileBounds, px, new Pixel(rowidx, colidx), this.canvas, this.paint);	            	
	            	// TODO: this.addTileMonitoringHooks(tile);
	                row.add(tile);
	            } else {
	            	//Util.printDebugMessage(" ...move tile to..." + "bounds: " + tileBounds.toString() + "...");
	            	//Util.printDebugMessage(" ...move tile to..." + this.getUrl(tileBounds) + "...");
	            	tile = row.get(colidx);
	            	tile.moveTo(tileBounds, px);
	            }
	            colidx++;
	            	 
	            tileoffsetlon += tilelon;       
	            tileoffsetx += this.tileSize.getWidth();
	            
	        } while((tileoffsetlon <= bounds.getMaxX() + tilelon * this.buffer) || colidx < minCols);  
	         
	        tileoffsetlat -= tilelat;
	        tileoffsety += this.tileSize.getHeight();
	        
	    } while((tileoffsetlat >= bounds.getMinY() - tilelat * this.buffer) || rowidx < minRows);	 
	   	//String timeSpent = Long.toString(System.currentTimeMillis()-startTime);
	   	//Util.printDebugMessage(" ...calculating tile matrix: " + timeSpent + "ms");
	    this.removeExcessTiles(rowidx, colidx);		    
	    /*
	    Util.printDebugMessage(" ...grid rows number: " + this.grid.size() + "...");
	    for(int i=0; i<this.grid.size(); i++) {
	    	Util.printDebugMessage(" ...grid row : " + i + " col: " + this.grid.get(i).size() + "...");	    	
	    }
	    */
	    this.spiralTileLoad();	 
	}
	
	/**
	 * private method: spiralTileLoad
	 * 
	 * 
	 */
	private void spiralTileLoad() {
		//Util.printDebugMessage("@...Grid.spiralTileLoad() is called...");
		ArrayList<Tile> tileQueue = new ArrayList<Tile>();		 
        String[] directions = {"right", "down", "left", "up"};

        int iRow = 0;
        int iCell = -1;
        int direction = 0; // 'right' 
        int directionsTried = 0;
        
        while(directionsTried < directions.length) {
            int testRow = iRow;
            int testCell = iCell;

            if(directions[direction].equalsIgnoreCase("right") == true) {
            	testCell++;                
            }
			if(directions[direction].equalsIgnoreCase("down") == true) {
				testRow++;				       
			}
			if(directions[direction].equalsIgnoreCase("left") == true) {
				testCell--;
			}
			if(directions[direction].equalsIgnoreCase("up") == true) {
				testRow--;
			}               
            // if the test grid coordinates are within the bounds of the 
            //  grid, get a reference to the tile.
            Tile tile = null;           
            if((testRow < this.grid.size()) && (testRow >= 0) && (testCell < this.grid.get(0).size()) && (testCell >= 0)) {
                tile = this.grid.get(testRow).get(testCell);
            }            
            if((tile != null) && (tile.isQueued()==false)) {
                //add tile to beginning of queue, mark it as queued.            	
            	tileQueue.add(0, tile);             
            	tile.setQueued(true);
            	//Util.printDebugMessage(" ...spirally load tile: (" + testRow + "," + testCell + ")...");
                //restart the directions counter and take on the new coords
                directionsTried = 0;
                iRow = testRow;
                iCell = testCell;
            } else {
                //need to try to load a tile in a different direction
                direction = (direction + 1) % 4;
                directionsTried++;
            }
        }         
        // now go through and draw the tiles in forward order
        //Util.printDebugMessage(" ...tiles are queued to be loaded...");
        //Util.printDebugMessage(" ...number of tiles to load: " + tileQueue.size() + "...");                   
        for(int i=0, len=tileQueue.size(); i<len; i++) {
        	Tile tile = tileQueue.get(i);            
        	tile.draw(this.tileProvider, this.canvas, this.paint, createDrawSignature()); 
            tile.setQueued(false);
        }
	}
	
	/**
	 * createDrawSignature()
	 * @return String
	 */
	private String createDrawSignature() {
		return this.map.createDrawSignature();
	}
	
	/**
	 * cancelLoadingThreads()
	 */
	@Override
	public void cancelLoadingThreads() {
		getTileProvider().cleanupHTTPLoaderThreadQueue(createDrawSignature());
		getTileProvider().cleanupFSLoaderThreadQueue(createDrawSignature());
	}
	
			
	/**
	 * API Method: addTile
	 * 
	 * @param bounds
	 * @param position
	 * 
	 * @return tile
	 */
	public Tile addTile(Envelope bounds, Pixel position, Pixel cell, Canvas canvas, Paint paint) {
		// to be implemented by subclass
		return null;
	}
	
	/**
	 * API Method: ,ergeNewParams
	 * 
	 * @param newParams
	 */
	@Override 
	public void mergeNewParams(HashMap<String, String> newParams) {
        super.mergeNewParams(newParams);
        if(this.map != null) {
        	// when new parameters are merged in, url for each tile could change
        	// so do a re-tiling
            //moveTo(this.map.getExtent(), false, false);
        }
	}
	
	/**
	 * API Method: removeExcessTiles
	 * 
	 * @param rows
	 * @param columns
	 */
	public void removeExcessTiles(int rows, int columns) {
		//Util.printDebugMessage("@...Grid.removeExcessTiles() is called...");		
		ArrayList<Tile> row = null;
		Tile tile = null;
		while(this.grid.size() > rows) { 
        	row = this.grid.remove(this.grid.size()-1); /* this.grid.pop() */           	
        	//Util.printDebugMessage(" ...one row of tiles removed from grid...");
        	for(int i=0,l=row.size(); i<l; i++) {
            	tile = row.get(i);
                //this.removeTileMonitoringHooks(tile);
                tile.destroy();
            }
        	this.grid.trimToSize();
        }  
        while(this.grid.get(0).size() > columns) {
        	//Util.printDebugMessage(" ...one column of tiles removed from grid...");
            for(int i=0, l=this.grid.size(); i<l; i++) {
                row = this.grid.get(i);
                tile = row.remove(row.size()-1); /* row.pop() */
                row.trimToSize();
                //this.removeTileMonitoringHooks(tile);
                tile.destroy();
            }
        }
	}
	
	/**
	 * API Method: onMapResize
	 * 
	 */
	@Override 
	public void onMapResize() {
		if(this.singleTile == true) {
			this.clearGrid();
			this.tileSize = null;
			this.setTileSize(this.map.getCurrentSize());	
			this.initSingleTile(this.map.getExtent());
		}			
	}
	
	/**
	 * API Method: setTileSize  
	 */
	public void setTileSize(Size size) {		
        if(this.singleTile == true) {
        	Size newSize = size.clone();        	
            newSize.setHeight((int)(size.getHeight() * this.ratio));
            newSize.setWidth((int)(size.getWidth() * this.ratio));
            this.tileSize = newSize;
        } else {
        	this.tileSize = size;
        }        	
	}
	
	/**
	 * 
	 * @return the tileSize
	 */
	public Size getTileSize() {
		return this.tileSize;
	}
	
	/**
	 * @return the imageSize
	 */
	@Override
	public Size getImageSize() {
		if(this.imageSize == null) {
			this.imageSize = this.map.getSize();
		}
		if(this.imageSize == null) {
			return this.tileSize;
		} else {
			return this.imageSize;
		}
	}

	/**
	 * @return the origin
	 */
	public Pixel getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(Pixel origin) {
		this.origin = origin;
	}	
	
	/**
	 * API Method: getMissingTileUrl
	 * 
	 * @return url
	 * the url to a missing tile resource
	 */
	public String getMissingTileUrl() {
		String missing_url = this.schema.getMissingTileUri(); 
    	if(missing_url != null && missing_url.equalsIgnoreCase("")==false) {
    		return missing_url;
    	} else {
    		return Util.DEFAULT_MISSING_TILE_URL;
    	}  
	}
	
	/**
	 * API Method: getTransparentTileUrl
	 * 
	 * @return url
	 * the url to a transparent tile resource
	 */
	public String getTransparentTileUrl() {
		String transparent_url = this.schema.getTransparentTileUri(); 
    	if(transparent_url != null && transparent_url.equalsIgnoreCase("")==false) {
    		return transparent_url;
    	} else {
    		return Util.DEFAULT_TRANSPARENT_TILE_URL;
    	}  
	}	
	
	/**
	 * @return the singleTile
	 */
	public boolean isSingleTile() {
		return singleTile;
	}

	/**
	 * @param singleTile the singleTile to set
	 */
	public void setSingleTile(boolean singleTile) {
		this.singleTile = singleTile;
	}
	
	/**
	 * @return the tileOrigin
	 */
	public Coordinate getTileOrigin() {
		return tileOrigin;
	}

	/**
	 * @param tileOrigin the tileOrigin to set
	 */
	public void setTileOrigin(Coordinate tileOrigin) {
		this.tileOrigin = tileOrigin;
	}
	
	

	public TileProvider getTileProvider() {
		return tileProvider;
	}

	public void setTileProvider(TileProvider tileProvider) {
		this.tileProvider = tileProvider;
	}

	// ===========================================================
	// utilities functions
	// ===========================================================		
	/**
	 * private method: printCurrentGridStatus
	 */
	protected void printCurrentGridStatus() {		
		if(this.grid.size() == 0) { return; }
		Log.i(DEBUGTAG, "	//=============================================================================");
		for(int i=0; i<this.grid.size(); i++) {
			ArrayList<Tile> row = this.grid.get(i);
			if(row.size() == 0) { continue; }
			for(int j=0; j<row.size(); j++) {
				Tile col = row.get(j);
				Log.i(DEBUGTAG, "	//...tile position: " + col.getPosition().toString());
				Log.i(DEBUGTAG, "	//...tile url: " + col.getUrl());
			}
		}
		Log.i(DEBUGTAG, "	//=============================================================================");
	}
	
	/**
	 * 
	 * @author
	 *
	 */
	private class TileEventsHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			switch(msg.what) {
				case TileEvents.HTTP_LOAD_SUCCESS:
					//Util.printDebugMessage(" ...TileEventsHandler: http load success...");											
					// uncomment this when using memory tile cache
					//Grid.this.redraw();				// TODO: redraw() or postInvalidate()? 
					// uncomment this when using file system only tile cache
					Grid.this.postInvalidate();	// TODO: redraw() or postInvalidate()?				
					break;
				case TileEvents.FS_LOAD_SUCCESS:					
					//Util.printDebugMessage(" ...TileEventsHandler: fs load success...");																		
					//if(Grid.this.getTileProvider().isFSStillLoading() == false) {
						//Util.printDebugMessage("...layer postInvalidate() is called because fs loader pending queue is 0...");
						Grid.this.postInvalidate();						
					//}										
					break;
				case TileEvents.FS_TILE_CORRUPTED:
					//Util.printDebugMessage(" ...TileEventsHandler: fs tile corrupted...");					
					break;
				case TileEvents.HTTP_TILE_INACCESSIBLE:
					//Util.printDebugMessage(" ...TileEventsHandler: http tile inaccessible...");						
					break;
				case TileEvents.MEM_LOAD_SUCCESS:
					//Util.printDebugMessage(" ...MapViewUpdateListener: mem load success...");					
					break;
				case TileEvents.HTTP_LOAD_FAILURE:
					//Util.printDebugMessage(" ...TileEventsHandler: http load failure...");							
					break;
				case TileEvents.FS_LOAD_FAILURE:
					//Util.printDebugMessage(" ...TileEventsHandler: fs load failure...");					
					break;
				case TileEvents.MEM_LOAD_FAILURE:
					//Util.printDebugMessage(" ...TileEventsHandler: mem load failure...");					
					break;
			}
		}
	}
}
